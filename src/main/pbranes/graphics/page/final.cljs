(ns pbranes.graphics.page.final
  (:require [helix.core :refer [defnc]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            [pbranes.graphics.common.dgutils :as dg]
            ["gl-matrix" :as glmatrix]
            [pbranes.graphics.common.utils :as u]
            [gl-matrix :as glmatrix]))

(set! *warn-on-infer* false)

(def vertex-shader
  "#version 300 es
   precision mediump float;

   void main(void) {
   
   }")

(def fragment-shader
  "#version 300 es
   precision mediump float;
   
   void main(void) {
   
   }")

(def gl (atom nil))
(def program (atom nil))
(def scene (atom nil))
(def clock (atom nil))

(defn load-shader [id type program]
  (let [script (js/document.createElement "script")
        head (nth (array-seq (js/document.getElementsByTagName "head")) 0)]

    (set! (.-id script) id)
    (set! (.-type script) type)
    (set! (.-innerHTML script) program)
    (.appendChild head script)

    script))

(defn configure []
  (reset! gl (-> "final-canvas"
                 (u/get-canvas)
                 (u/get-gl-context)))
  (u/auto-resize-canvas (.-canvas @gl))

  (dg/configure-controls  {"Color"
                           {"Sphere Color" {:value "#ff0000"}
                            "Ball Color" {:value "#00ff00"}}
                           }  {:width 300 :open true})

  (.clearColor @gl 0.9 0.9 0.9 1)
  (.clearDepth @gl 1)
  (.enable @gl (.-DEPTH_TEST @gl))
  (.depthFunc @gl (.-LESS @gl))
  (.blendFunc @gl (.-SRC_ALPHA @gl) (.-ONE_MINUS_SRC_ALPHA @gl))

  ;; Create/Load shaders script add to doc head element so they
  ;; can be loaded by the program
  (load-shader "vertex-shader" "x-shader/x-vertex" vertex-shader)
  (load-shader "fragment-shader" "x-shader/x-fragment" fragment-shader)

  (reset! program (js/Program. @gl "vertex-shader" "fragment-shader"))
  (reset! scene (js/Scene. @gl @program))
  (reset! clock (js/Clock.)))

(defn draw []
;;  (.-viewport @gl 0 0 (-> @gl .-canvas .-width) (-> @gl .-canvas .-width))
;;  (.clear @gl (bit-or (.-COLOR_BIT @gl) (.-DEPTH_BUFFER_BIT @gl)))
  )

(defn init []
  (configure)
  (.on @clock draw)
  (js/console.log (clj->js {:width 800 :name "My gui"})))

(defnc final-page []
  (hooks/use-effect
   :once
   (init))

  (d/div
   (d/canvas
    {:id "final-canvas" :className "webgl-canvas"}
    "Your browser does not support HTML5 canvas.")))
