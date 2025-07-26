(ns pbranes.graphics.hooks.canvas-hook
  (:require [helix.hooks :as hooks]
            [pbranes.graphics.common.utils :as utils]))

(defn use-init-canvas [canvas f]
  ^{:doc "Get context for canvas ref and pass to function."}
  (hooks/use-effect
   :once
   (let [gl (utils/get-context canvas)]
     (f gl))))
