(ns pbranes.graphics.page.rendering
  (:require [helix.core :refer [defnc $ <>]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            [pbranes.graphics.hooks.canvas-hook :refer [use-init-canvas]]
            [pbranes.graphics.common.utils :as u]
            [cljs.core :as core]))

(set! *warn-on-infer* false)

;; Square vertices in clip space.
;; Clipspace coordinates go from -1 to 1 regardless of size of canvas
(def vertices
  [-0.5 0.5 0.0
   -0.5 -0.5 0.0
   0.5 -0.5 0.0
   0.5 0.5 0.0])

(def indices
  [0 1 2
   0 2 3])

(def vs-shader
  "#version 300 es
precision mediump float;

// Supplied vertex position attribute
 in  vec3 aVertexPosition;

void main(void) {
  // Set the position in the clipspace coordinates
  gl_Position = vec4(aVertexPosition, 1.0);
}
")

(def fs-shader
  "#version 300 es
  precision mediump float;

 // Color that is the result of this shader
  out  vec4 fragColor;

  void main(void) {
      fragColor = vec4(1.0, 0.0, 1.0, 1.0);
  }
")

(defn init-program [gl]
  (let [vertex-shader (u/compile-shader gl vs-shader (.-VERTEX_SHADER gl))
        fragment-shader (u/compile-shader gl fs-shader (.-FRAGMENT_SHADER gl))
        program (.createProgram gl)]

    (.attachShader gl program vertex-shader)
    (.attachShader gl program fragment-shader)
    (.linkProgram gl program)

    (when (not (.getProgramParameter gl program (.-LINK_STATUS gl)))
      (js/console.error "Could not initialize shaders"))

    ;; Use this program instance
    (.useProgram gl program)

      ;; Attaching for easy access in the code
    (set! (.-aVertexPosition program) (.getAttribLocation gl program  "aVertexPosition"))

    ;; return program
    program))

(defn init-buffers [gl program vbo ibo]
  (let [vertex-buffer (u/create-vertex-buffer gl vbo)
        index-buffer (u/create-index-buffer gl ibo)]
    ;; Provide instructions for VAO to use later in Draw
    (.enableVertexAttribArray gl (.-aVertexPosition program))
    (.vertexAttribPointer gl (.-aVertexPosition program) 3 (.-FLOAT gl) false 0 0)

    (u/create-index-buffer gl ibo)

    (u/clear-array-buffer gl)
    (u/clear-element-array-buffer gl)

    {:vertex-buffer vertex-buffer :index-buffer index-buffer}))

(defn draw [gl program buffers]
  ;; clear the scene
  (u/clear-scene gl)

  ;;use the buffers we constructed
  (.bindBuffer gl (.-ARRAY_BUFFER gl) (:vertex-buffer buffers))
  (.vertexAttribPointer gl (.-aVertexPosition program) (.-FLOAT gl) 3 false 0 0)
  (.enableVertexAttribArray gl (.-aVertexPosition program))

  ;; bind IBO
  (.bindBuffer gl (.-ELEMENT_ARRAY_BUFFER gl) (:index-buffer buffers))

  ;; Draw the scene using primitive triangles

  (.drawElements gl (.-TRIANGLES gl) (count indices) (.-UNSIGNED_SHORT gl) 0)

  ;; clean
  (u/clear-array-buffer gl)
  (u/clear-element-array-buffer gl)
  )

(defn init [gl]
  (.clearColor gl 0 0 0 1)
  (let [program (init-program gl)
        buffers (init-buffers gl program vertices indices)]
    (js/console.log "init")
    (draw gl program buffers)))

(defnc rendering-page []
  (let [canvas (hooks/use-ref nil)]

    (use-init-canvas canvas init)

    (d/canvas {:ref canvas :className "webgl-canvas" :height 600 :width 800}
              "Your browser does not support HTML5 canvas.")))
