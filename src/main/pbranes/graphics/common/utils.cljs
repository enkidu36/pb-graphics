(ns pbranes.graphics.common.utils)

(set! *warn-on-infer* false)

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

(defn compile-shader [gl source type]
  (let [shader (.createShader gl type)]

    ;; Compile the source code for shader
    (.shaderSource gl shader source)
    (.compileShader gl shader)

    ;; Ensure source code compiled
    (if (.getShaderParameter gl shader (.-COMPILE_STATUS gl))
      shader
      (js/console.log (.getShaderInfoLog gl shader)))))


(defn create-vertex-buffer [gl buffer-data]
  (when buffer-data
    (let [vertex-buffer (.createBuffer gl)]
      
      (.bindBuffer gl (.-ARRAY_BUFFER gl) vertex-buffer)
      (.bufferData gl (.-ARRAY_BUFFER gl) (js/Float32Array. buffer-data) (.-STATIC_DRAW gl))
      
      vertex-buffer)))

(defn create-index-buffer [gl buffer-data]
  (when buffer-data
    (let [index-buffer (.createBuffer gl)]
      (.bindBuffer gl (.-ELEMENT_ARRAY_BUFFER gl) index-buffer)
      (.bufferData gl (.-ELEMENT_ARRAY_BUFFER gl) (js/Uint16Array. buffer-data) (.-STATIC_DRAW gl))

      index-buffer)))

(defn clear-array-buffer [gl]
  (.bindBuffer gl (.-ARRAY_BUFFER gl) nil))

(defn clear-element-array-buffer [gl]
  (.bindBuffer gl (.-ELEMENT_ARRAY_BUFFER gl) nil))

(defn clear-scene [gl]
  (.clear gl (bit-or (.-COLOR_BUFFER_BIT gl) (.-DEPTH_BUFFER_BIT gl)))
  (.viewport gl 0 0 (.. gl -canvas -width) (.. gl -canvas -height)))
