(ns pbranes.graphics.page.render-modes
  (:require [helix.core :refer [defnc $ <>]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            ["dat.gui" :as dg]
            [pbranes.graphics.common.utils :as u]))

(set! *warn-on-infer* false)

(def v-shader
  "#version 300 es
   precision mediump float;

// Suppliced vertex position attribute
in vec3 aVertexPosition;

void main(void) {
// Set the position in the clipspace coordinates
gl_Position = vec4(aVertexPosition, 1.0);
gl_PointSize = 5.0;

}
")

(def f-shader
  "#version 300 es
  precision mediump float;

 // Color that is the result of this shader
  out  vec4 fragColor;

  void main(void) {
      fragColor = vec4(0.5, 0.5, 1.0, 1.0);
  }
")

(def render-mode (atom "TRIANGLES"))
(def settings (clj->js {:render-mode "TRIANGLES"}))
(def render-opts (clj->js ["TRIANGLES" "LINES" "POINTS" "LINE_LOOP" "LINE_STRIP" "TRIANGLE_STRIP" "TRIANGLE_FAN"]))

(defn init-program [gl]
  (let [vertex-shader (u/compile-shader gl v-shader (.-VERTEX_SHADER gl))
        fragment-shader (u/compile-shader gl f-shader (.-FRAGMENT_SHADER gl))
        program (.createProgram gl)]

    (.attachShader gl program vertex-shader)
    (.attachShader gl program fragment-shader)
    (.linkProgram gl program)

    (when (not (.getProgramParameter gl program (.-LINK_STATUS gl)))
      (let [msg (.getProgramInfoLog gl program)]
        (js/console.error "Could not initialize shaders")))

    (.useProgram gl program)
    program))

(def trapizoid-vertices
  [-0.5 -0.5 0.0
   -0.25 0.5 0.0
   0.0 -0.5 0.0
   0.25 0.5 0.0
   0.5 -0.5 0.0])

(def init-indices [0 1 2  1 2 3  2 3 4])


(defn render-element [gl type indices]
  (.bufferData gl (.-ELEMENT_ARRAY_BUFFER gl) (js/Uint16Array. indices) (.-STATIC_DRAW gl))
  (.drawElements gl type (count indices) (.-UNSIGNED_SHORT gl) 0))

(defn init-buffers [gl program]
  (let [trapizoidVertexArray (u/create-vertex-array gl)
        trapizoidIndexBuffer (u/create-index-buffer gl init-indices)]
    (.bindVertexArray gl trapizoidVertexArray)

    (u/create-vertex-buffer gl trapizoid-vertices)

    (.enableVertexAttribArray gl (.-aVertexPosition program))
    (.vertexAttribPointer gl (.-aVertexPosition program) 3 (.-FLOAT gl) false 0 0)

    (u/clear-all-arrays-buffers gl)

    {:vertex-array trapizoidVertexArray
     :index-buffer trapizoidIndexBuffer}))

(defn draw [gl buffers]
  (u/clear-scene gl)

  (.bindVertexArray gl (:vertex-array buffers))
  (.bindBuffer gl (.-ELEMENT_ARRAY_BUFFER gl) (:index-buffer buffers))

  (case @render-mode
    "TRIANGLES"
    (render-element gl (.-TRIANGLES gl) [0 1 2  2 3 4])
    "LINES"
    (render-element gl (.-LINES gl) [1 3 0 4 1 2 2 3])
    "POINTS"
    (render-element gl (.-POINTS gl) [1 2 3])
    "LINE_LOOP"
    (render-element gl (.-LINE_LOOP gl) [2 3 4 1 0])
    "LINE_STRIP"
    (render-element gl (.-LINE_STRIP gl) [0 1 2 3 4])
    "TRIANGLE_STRIP"
    (render-element gl (.-TRIANGLE_STRIP gl) [0 1 2 3 4])
    "TRIANGLE_FAN"
    (render-element gl (.-TRIANGLE_FAN gl) [0 1 2 3 4]))

  (u/clear-all-arrays-buffers gl))


(defn init [gl controls]
  (.clearColor gl 0 0 0 1)
  (.enable gl (.-DEPTH_TEST gl))
  (-> controls
      (.add settings "render-mode" render-opts)
      (.onChange (fn [v]
                   (reset! render-mode v))))

  (.open controls)

  (let [program (init-program gl)
        buffers (init-buffers gl program)
        render (fn render []
                 (draw gl buffers)
                 (js/requestAnimationFrame render))]
    (js/requestAnimationFrame render)))

(defnc modes-page []
  (let [canvas (hooks/use-ref nil)]

    (hooks/use-effect [render-mode]
                      :always
                      (let [gl (u/get-context canvas)
                            controls (dg/GUI.)]
                        (js/console.log @render-mode)
                        (init gl controls)

                        (fn unmount []
                          (.destroy (.getRoot controls))
                          (reset! render-mode "TRIANGLES"))))

    (d/canvas {:ref canvas :className "webgl-canvas" :height 600 :width 800}
              "Your browser does not support HTML5 canvas.")))
