(ns pbranes.graphics.page.lighting.final
  (:require [helix.core :refer [defnc <>]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            [pbranes.graphics.common.dgutils :as dg]
            [pbranes.graphics.common.lights :as lights]
            [pbranes.graphics.common.mat4 :as m4]
            ["dat.gui" :as datgui]
            [pbranes.webgl.constants :as c]
            [pbranes.graphics.common.utils :as u]
            [pbranes.graphics.common.program :refer [create-shader-program load-program! create-program-map]]
            [pbranes.graphics.page.lighting.objects :refer [sphere]]
            [oops.core :refer [oget oset!]]))

(set! *warn-on-infer* false)

(def vertex-shader
  "#version 300 es
    precision mediump float;

    uniform mat4 uModelViewMatrix;
    uniform mat4 uProjectionMatrix;
    uniform mat4 uNormalMatrix;
    uniform vec3 uLightDirection;
    uniform vec3 uLightDiffuse;
    uniform vec3 uMaterialDiffuse;

    in vec3 aVertexPosition;
    in vec3 aVertexNormal;

    out vec4 vVertexColor;

    void main(void) {
      // Calculate the normal vector
      vec3 N = normalize(vec3(uNormalMatrix * vec4(aVertexNormal, 1.0)));

      // Attach the light to the model transformation matrix
      vec3 light=vec3(uModelViewMatrix*vec4(uLightDirection,0.));
      
      // Normalized light direction
      vec3 L = normalize(light);
 
      // Dot product of the normal product and negative light direction vector
      float lambertTerm = dot(N, -L);

      // Calculating the diffuse color based on the Lambertian reflection model
      vec3 Id = uMaterialDiffuse * uLightDiffuse * lambertTerm;

      // Set the varying to be used inside of the fragment shader
      vVertexColor = vec4(Id, 1.0);

      // Setting the vertex position
      gl_Position = uProjectionMatrix * uModelViewMatrix *  vec4(aVertexPosition, 1.0);

    }
")

(def fragment-shader
  "#version 300 es
    precision mediump float;

    // Expect the interpolated value from the vertex shader
     in vec4 vVertexColor;

    // Return the final color as fragColor
    out vec4 fragColor;

    void main(void)  {
      // Simply set the value passed in from the vertex shader
       fragColor = vVertexColor;

    }
")

(def gl (atom nil))
(def program-map (atom nil))
(def scene (atom nil))
(def floor (atom {:visible true}))
(def clock (atom nil))
(def clear-color [0.9 0.9 0.9])
(def light-diffuse-color (atom  [1 1 1]))
(def light-direction (atom [0 -1 -1]))
(def sphere-color (atom  [0.5 0.8 0.1]))

(def sphere-VAO (atom nil))
(def sphere-IBO (atom nil))

(def animate? (atom false))

(def model-view-matrix (atom nil))
(def projection-matrix (atom nil))
(def normal-matrix (atom nil))
(def angle (atom 0))
(def last-time (atom 0))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Controls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def floor-control
  {:Floor
   {:value (:visible @floor)
    :onChange (fn [v] (swap! floor assoc :visible))}})

(def background-control
  {:Background
   {:value (dg/de-normalize-color clear-color)
    :onChange (fn [v]
                (let [[x y z] (dg/normalize-color v)]
                  (.clearColor @gl x y z 1)
                  (.clear @gl (.-COLOR_BUFFER_BIT @gl))
                  (.viewport @gl 0 0 0 0)))}})

(def sphere-color-control
  {:SphereColor
   {:value (dg/de-normalize-color @sphere-color)
    :onChange (fn [v]
                (.uniform3fv @gl (:uMaterialDiffuse @program-map)
                             (clj->js (reset! sphere-color (dg/normalize-color v)))))}})

(def light-diffuse-color-control
  {"Light Diffuse Color"
   {:value (dg/de-normalize-color @light-diffuse-color)
    :onChange (fn [v]
                (.uniform3fv @gl (:uLightDiffuse @program-map)
                             (clj->js (reset! light-diffuse-color (dg/normalize-color v)))))}})

(def light-control
  (loop [positions ["Translate X" "Translate Y" "Translate Z"]
         i 0
         result {}]

    (if (empty? positions)
      result
      (let [control {(keyword (first positions))
                     {:value (nth @light-direction i)
                      :min -10 :max 10 :step 0.1
                      :onChange (fn [v]
                                  (.uniform3fv @gl
                                               (:uLightDirection @program-map)
                                               (clj->js (swap! light-direction assoc i v))))}}]
        (recur (rest positions) (inc i) (into result control))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn init-lights [ctx]
  (.uniform3fv ctx (:uLightDirection @program-map) @light-direction)
  (.uniform3fv ctx (:uLightDiffuse @program-map) @light-diffuse-color)
  (.uniform3fv ctx (:uMaterialDiffuse @program-map) (clj->js @sphere-color)))

(defn init-program [ctx]
  ;; Configure canvas
  (reset! gl ctx)
;;  (u/auto-resize-canvas (.-canvas ctx))

  (.clearColor ctx  0.9 0.9 0.9 1)
  (.enable ctx (.-DEPTH_TEST ctx))

  (let [attributes ["aVertexPosition"
                    "aVertexNormal"]
        uniforms ["uProjectionMatrix"
                  "uModelViewMatrix"
                  "uNormalMatrix"
                  "uMaterialDiffuse"
                  "uLightDiffuse"
                  "uLightDirection"]]

    (reset! program-map (-> (create-shader-program ctx vertex-shader fragment-shader)
                            (load-program! ctx attributes uniforms)
                            (create-program-map ctx attributes uniforms))))

  ;; Projection Matrix     
  (reset! projection-matrix (-> (m4/create-matrix)
                                (m4/perspective-matrix
                                 (* 45 (/ Math/PI 180))
                                 (/ (.. ctx -canvas -width) (.. ctx -canvas -height))
                                 0.1
                                 10000)))

  ;; Model View Matrix
  (reset! model-view-matrix (-> (m4/create-matrix)
                                (m4/identity-matrix)
                                (m4/translate-matrix [0.0 0.0 -1.5])
                                (m4/rotate-matrix (* @angle (/ Math/PI 180)) [0 1 0])))

  ;; Normal Matrix
  (reset! normal-matrix (-> (m4/create-matrix)
                            (m4/copy-matrix @model-view-matrix)
                            (m4/invert-matrix)
                            (m4/transpose-matrix))))

(defn init-buffers [ctx]
  (let [sphere-mesh sphere
        normals (js/utils.calculateNormals
                 (clj->js (:vertices sphere-mesh))
                 (clj->js (:indices sphere-mesh)))]

    ;; Create and bind sphere Vertex Array Object
    (reset! sphere-VAO (.createVertexArray ctx))
    (.bindVertexArray ctx @sphere-VAO)

    ;; Vertices
    (.bindBuffer ctx c/ARRAY-BUFFER (.createBuffer ctx))
    (.bufferData ctx c/ARRAY-BUFFER (js/Float32Array. (clj->js (:vertices sphere-mesh))) c/STATIC-DRAW)

    ;; Configure VAO instructions
    (.enableVertexAttribArray ctx (:aVertexPosition @program-map))
    (.vertexAttribPointer ctx (:aVertexPosition @program-map) 3 (.-FLOAT ctx) false 0 0)

    ;; Normals
    (.bindBuffer ctx c/ARRAY-BUFFER (.createBuffer ctx))
    (.bufferData ctx c/ARRAY-BUFFER (js/Float32Array. (clj->js normals)) c/STATIC-DRAW)

    ;; Configure VAO instructions
    (.enableVertexAttribArray ctx (:aVertexNormal @program-map))
    (.vertexAttribPointer ctx (:aVertexNormal @program-map) 3 (.-FLOAT ctx) false 0 0)

    ;; Indices
    (reset! sphere-IBO (.createBuffer ctx))
    (.bindBuffer ctx c/ELEMENT-ARRAY-BUFFER @sphere-IBO)
    (.bufferData ctx c/ELEMENT-ARRAY-BUFFER (js/Uint16Array. (clj->js  (:indices sphere-mesh))) c/STATIC-DRAW)

    ;; Clean
    (.bindVertexArray ctx nil)
    (.bindBuffer ctx c/ARRAY-BUFFER nil)
    (.bindBuffer ctx c/ELEMENT-ARRAY-BUFFER nil))

  (reset! gl ctx))

(defn draw []
  (let [ctx @gl
        aspect (/ (.. ctx -canvas -width) (.. ctx -canvas -height))
        fovy (* 45 (/ Math/PI 180))]

    (.viewport ctx  0 0 (.. ctx -canvas -width) (.. ctx -canvas -height))
    (.clear ctx (bit-or c/COLOR-BUFFER-BIT c/DEPTH-BUFFER-BIT))

    ;; Projection Matrix
    (reset! projection-matrix (-> (m4/create-matrix)
                                  (m4/perspective-matrix fovy aspect 0.1 10000)))

    ;; Model View Matrix
    (reset! model-view-matrix (-> @model-view-matrix
                                  (m4/identity-matrix)
                                  (m4/translate-matrix [0.0 0.0 -1.5])
                                  (m4/rotate-matrix (* @angle (/ Math/PI 180)) [0 1 0])))

;; Normal Matrix
    (reset! normal-matrix (-> @normal-matrix
                              (m4/copy-matrix @model-view-matrix)
                              (m4/transpose-matrix)))

    (.uniformMatrix4fv ctx (:uModelViewMatrix @program-map) false @model-view-matrix)
    (.uniformMatrix4fv ctx (:uProjectionMatrix @program-map) false @projection-matrix)
    (.uniformMatrix4fv ctx (:uNormalMatrix @program-map) false @normal-matrix)

    (try
      ;; Bind and Draw elements. Catch any errors.
      (.bindVertexArray ctx @sphere-VAO)
      (.bindBuffer ctx c/ELEMENT-ARRAY-BUFFER @sphere-IBO)

      (.drawElements ctx c/TRIANGLES (count (:indices sphere)) c/UNSIGNED-SHORT 0)

      (.bindVertexArray ctx nil)
      (.bindBuffer ctx c/ARRAY-BUFFER nil)
      (.bindBuffer ctx c/ELEMENT-ARRAY-BUFFER nil)

      (catch js/Error e (js/console.error e)))))

(defn animate []
  (let [time-now (.getTime (js/Date.))]

    (when @last-time
      (let [elapsed (- time-now @last-time)]
        (swap! angle + (-> 90 (* elapsed) (/ 1000.0)))))

    (reset! last-time time-now)))

(defn render []
  (when @animate?
    (animate))
  (draw)
  (js/requestAnimationFrame render))

(defn init [controls]
  (let [ctx (-> "final-canvas" (u/get-canvas) (u/get-gl-context))]
    (init-program ctx)
    (init-buffers ctx)
    (init-lights ctx)
    (render)

    (dg/configure-controls
     (-> {}

         (into sphere-color-control)
         (into light-diffuse-color-control)
         (into light-control)
         (into {"Animate" {:value @animate?
                           :onChange (fn [v]
                                       (reset! animate? v))}}))
     {:gui controls :open true})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Final Helix/React
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defnc final-page []
  (hooks/use-effect
   :once
   (let [controls (datgui/GUI. (clj->js {:width 350}))]
     (init controls)

     (fn unmount []
       (.destroy (.getRoot controls)))))

  (<>

   (d/canvas
    {:id "final-canvas" :width "400px" :height "400px"}
    "Your browser does not support HTML5 canvas.")))

