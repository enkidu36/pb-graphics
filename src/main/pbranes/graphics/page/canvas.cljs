(ns pbranes.graphics.page.canvas
  (:require [helix.core :refer [defnc $ <>]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            [pbranes.graphics.common.utils :as utils]))

(defn init [gl]
  (utils/update-clear-color gl [1.0 1.0 1.0 1.0]))

(defnc CanvasPage []
  (let [canvas (hooks/use-ref nil)]

    (hooks/use-effect
     :once
      (let [gl (utils/get-context canvas)]
        (init gl)
        (js/window.addEventListener "keydown" (utils/check-key gl))
        
        (fn unmount []
          (js/window.removeEventListener "keydown" (fn [_] (js/console.log "Remove keydown listener"))))
        ))

    (d/canvas {:ref canvas :className "webgl-canvas" :height 600 :width 800}
              "Your browser does not support HTML5 canvas.")))
