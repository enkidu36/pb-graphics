(ns pbranes.graphics.page.geometry
  (:require [helix.core :refer [defnc $ <>]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            ["dat.gui" :as dg]
            [pbranes.graphics.common.dgutils :as dgutils]
            [pbranes.graphics.common.utils :as u]
            [pbranes.graphics.common.program :as p]))

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



(def render-opts ["TRIANGLES"
                  "LINES"
                  "POINTS"
                  "LINE_LOOP"
                  "LINE_STRIP"
                  "TRIANGLE_STRIP"
                  "TRIANGLE_FAN"])
(def render-mode (atom nil))
(def trapizoid-vertices
  [-0.5 -0.5 0.0
   -0.25 0.5 0.0
   0.0 -0.5 0.0
   0.25 0.5 0.0
   0.5 -0.5 0.0])
(def init-indices [0 1 2  1 2 3  2 3 4])
(def bg-color (atom [0.9 0.9 0.9 0]))


(defn init-render-mode! []
  (reset! render-mode (first render-opts)))

(defn render-element [gl type indices]
  (.bufferData gl (.-ELEMENT_ARRAY_BUFFER gl) (js/Uint16Array. indices) (.-STATIC_DRAW gl))
  (.drawElements gl type (count indices) (.-UNSIGNED_SHORT gl) 0))

(def square
  {:vertices [-0.5  0.5  0.0
              -0.5 -0.5  0.0
               0.5 -0.5  0.0
               0.5  0.5  0.0]
   :indices [0 1 1 2 2 3]})

(defn clear-color
  [gl [r g b a]]
  (.clearColor gl r g b a))

(defn draw [gl buffers]
  (u/clear-scene gl)

  (.bindVertexArray gl (:vertex-array buffers))
  (.bindBuffer gl (.-ELEMENT_ARRAY_BUFFER gl) (:index-buffer buffers))
  (clear-color gl @bg-color)

  (render-element gl (.-LINE_LOOP gl) (:indices square))
  
  ;; (case @render-mode
  ;;   "TRIANGLES"
  ;;   (render-element gl (.-TRIANGLES gl) [0 1 2  2 3 4])
  ;;   "LINES"
  ;;   (render-element gl (.-LINES gl) [1 3 0 4 1 2 2 3])
  ;;   "POINTS"
  ;;   (render-element gl (.-POINTS gl) [1 2 3])
  ;;   "LINE_LOOP"
  ;;   (render-element gl (.-LINE_LOOP gl) [2 3 4 1 0])
  ;;   "LINE_STRIP"
  ;;   (render-element gl (.-LINE_STRIP gl) [0 1 2 3 4])
  ;;   "TRIANGLE_STRIP"
  ;;   (render-element gl (.-TRIANGLE_STRIP gl) [0 1 2 3 4])
  ;;   "TRIANGLE_FAN"
  ;;   (render-element gl (.-TRIANGLE_FAN gl) [0 1 2 3 4]))

  (u/clear-all-arrays-buffers gl))

(defn init-controls
  [gl controls]
  (dgutils/configure-controls
   (-> {}
       (into {"Render Mode"
              {:value @render-mode
               :options render-opts
               :onChange (fn [v]
                           (reset! render-mode v))}})
       (into  {:Background
               {:value (dgutils/de-normalize-color (take 3 @bg-color))
                :onChange (fn [v]
                            (let [colors (dgutils/normalize-color v)
                                  [r g b] colors]
                              (reset! bg-color [r g b 1])))}}))
   {:gui controls :open true}))

(defn init-buffers [gl program vertices indices]
  (let [vertexArray (u/create-vertex-array gl)
        indexBuffer (u/create-index-buffer gl indices)]
    
    (.bindVertexArray gl vertexArray)

    (u/create-vertex-buffer gl vertices)

    (.enableVertexAttribArray gl (.-aVertexPosition program))
    (.vertexAttribPointer gl (.-aVertexPosition program) 3 (.-FLOAT gl) false 0 0)

    (u/clear-all-arrays-buffers gl)

    {:vertex-array vertexArray
     :index-buffer indexBuffer}))

(defn init [gl controls]
  (init-render-mode!)
  ;;  (u/auto-resize-canvas (.-canvas gl))
  (clear-color gl @bg-color)

  (init-controls gl controls)


  (let [program (p/create-shader-program gl v-shader f-shader)
        buffers (init-buffers gl program (:vertices square) (:indices square))
        render (fn render []
                 (draw gl buffers)
                 (js/requestAnimationFrame render))]
    (js/requestAnimationFrame render)))

(defnc page []
  (let [canvas (hooks/use-ref nil)]

    (hooks/use-effect [render-mode]
                      :always
                      (let [gl (u/get-context canvas)
                            controls (dg/GUI. (clj->js {:width 350}))]
                          (init gl controls)

                        (fn unmount []
                          (.destroy (.getRoot controls))
                          (init-render-mode!))))

    (d/canvas {:ref canvas :className "webgl-canvas" :width 800 :height 800}
              "Your browser does not support HTML5 canvas.")))
