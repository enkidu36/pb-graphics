(ns pbranes.graphics.page.camera.camera

  (:require [helix.core :refer [defnc <>]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            ["dat.gui" :as datgui]
            [pbranes.webgl.constants :as c]
            [pbranes.graphics.common.utils :as u]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [pbranes.graphics.common.scene :as scene]
            [pbranes.graphics.common.program :refer [create-shader-program load-program! create-program-map]]))

(set! *warn-on-infer* false)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; vertex-shader
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def vertex-shader
  "#version 300 es
    precision mediump float;

    uniform mat4 uModelViewMatrix;
    uniform mat4 uProjectionMatrix;
    uniform mat4 uNormalMatrix;
    uniform vec3 uLightPosition;
    uniform vec4 uLightAmbient;
    uniform vec4 uLightDiffuse;
    uniform vec4 uMaterialDiffuse;
    uniform bool uWireframe;
    uniform bool uFixedLight;

    in vec3 aVertexPosition;
    in vec3 aVertexNormal;
    in vec4 aVertexColor;

    out vec4 vFinalColor;

    void main(void) {
      // If wireframe is enabled, set color to the diffuse property exclusnying lights
      if (uWireframe) {
        vFinalColor = uMaterialDiffuse;
      }
      else {
        // Normal
        vec3 N = vec3(uNormalMatrix * vec4(aVertexNormal, 0.0));
        // Normalized light position
        vec3 L = normalize(-uLightPosition);

        // If true, then ensure that light position
        // is appropruately updated
        if (uFixedLight) {
          L = vec3(uNormalMatrix * vec4(L, 0.0));
        }

        float lambertTerm = dot(N, -L);
        if (lambertTerm == 0.0) {
          lambertTerm = 0.01;
        }

        // Ambient
        vec4 Ia = uLightAmbient;
        // Diffuse
        vec4 Id = uMaterialDiffuse * uLightDiffuse * lambertTerm;

        // Set varying to be used inside of fragment shader
        vFinalColor = vec4(vec3(Ia + Id), 1.0);
      }

      gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aVertexPosition, 1.0);
    }
")
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; fragment-shader
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def fragment-shader
  "#version 300 es
    precision mediump float;

    in vec4 vFinalColor;

    out vec4 fragColor;

    void main(void) {
      fragColor = vFinalColor;
    }
")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; global variables
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def ctx (atom nil)) ; webgl 3D context
(def program-map (atom nil))
(def clock (atom nil))
(def scene (atom nil))
(def camera (atom nil))

(defn configure [ctx]

  ;; Configure context
  (.clearColor ctx  0.9 0.9 0.9 1)
  (.clearDepth ctx 100)
  (.enable ctx c/DEPTH-TEST)
  (.depthFunc ctx c/LEQUAL)

  (reset! clock (js/Clock.))

  ;; Configure program
  (let [attributes ["aVertexPosition"
                    "aVertexColor"
                    "aVertexNormal"]
        uniforms ["uProjectionMatrix"
                  "uModelViewMatrix"
                  "uNormalMatrix"
                  "uMaterialDiffuse"
                  "uLightAmbient"
                  "uLightDiffuse"
                  "uLightPosition"
                  "uWireFrame"
                  "uFixedLight"]]

    (reset! program-map (-> (create-shader-program ctx vertex-shader fragment-shader)
                            (load-program! ctx attributes uniforms)
                            (create-program-map ctx attributes uniforms))))

  (reset! camera (js/Camera.  js/Camera.TRACKING_TYPE))

  (reset! scene (js/Scene.)))

(defn init [controls]
  (let [ctx (-> "final-canvas" (u/get-canvas) (u/get-gl-context))]
    (u/auto-resize-canvas ctx)
    (configure ctx)
    
    ))

(defnc camera-page []
  (hooks/use-effect
   :once
   (let [controls (datgui/GUI. (clj->js {:width 350}))]
     (init controls)

     (fn unmount []
       (.destroy (.getRoot controls)))))

  (<>
   (d/div {:style { :color "black"}} "Camera")

   (d/canvas
    {:id "final-canvas" :width "400px" :height "400px"}
    "Your browser does not support HTML5 canvas.")
   (d/div {:id "info"}
          (d/p "Camera Matrix")
          (d/table
           {:id "matrix"}
           (d/tbody
            (d/tr
             (d/td {:id "m0"})
             (d/td {:id "m4"})
             (d/td {:id "m8"})
             (d/td {:id "m12"}))
            (d/tr
             (d/td {:id "m1"})
             (d/td {:id "m5"})
             (d/td {:id "m9"})
             (d/td {:id "m13"}))
            (d/tr
             (d/td {:id "m2"})
             (d/td {:id "m6"})
             (d/td {:id "m10"})
             (d/td {:id "m14"}))
            (d/tr
             (d/td {:id "m3"})
             (d/td {:id "m7"})
             (d/td {:id "m11"})
             (d/td {:id "m15"})))))))


