(ns pbranes.graphics.common.utils)

(defn get-context
  ^{:doc "Multi arity method to get the canvas context by type
    default to 3d graphics. Type can be \"webgl2\" or \"2d\""}
  ([canvas] (get-context canvas "webgl2"))
  ([canvas type]
   (if @canvas
     (.getContext @canvas type)
     (js/console.error "No HTML5 canvas found!"))))


(defn update-clear-color [gl [r g b a]]
  (.clearColor gl r g b a)
  (.clear gl (.-COLOR_BUFFER_BIT gl))
  (.viewport gl 0 0 0 0))

(defn check-key [gl]
  (fn [event]
    ;; Match on key codes for numbers 1 - 4
    (case (.-keyCode event)
      49 (update-clear-color gl [0.2 0.8 0.2 1.0])
      50 (update-clear-color gl [0.2 0.2 0.8 1.0])
      51 (update-clear-color gl [(Math/random) (Math/random) (Math/random) 1.0])
      52 (let [color (vec (.getParameter gl (.-COLOR_CLEAR_VALUE gl)))]
           (js/alert (str "`clearColor = (" (.toFixed (nth color 0) 1) ",  " (.toFixed (get color 1)) ",  " (.toFixed (get color 2) 1) ")`"))
           (js/window.focus))
      :default)))
