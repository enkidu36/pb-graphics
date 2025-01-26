(ns pbranes.graphics.common.program
  (:require [pbranes.graphics.common.utils :as u]
            [pbranes.webgl.constants :as c]
            [oops.core :refer [oset! oset!+]]))


(defn create-shader-program
  "Create the the program, compile and attach shaders."
  [gl vertex-shader fragment-shader]
  (let [program (.createProgram gl)]
    (.attachShader gl program (u/compile-shader gl vertex-shader c/VERTEX-SHADER))
    (.attachShader gl program (u/compile-shader gl fragment-shader c/FRAGMENT-SHADER))
    (.linkProgram gl program)

    (when (not (.getProgramParameter gl program c/LINK-STATUS))
      (let [msg (.getProgramInfoLog gl program)]
        (js/console.error "Could not initialize shaders: " msg)))

    (.useProgram gl program)
    program))

(defn set-attribute-locations
  "Sets program attribute locations"
  [program gl attributes]
  (loop [list attributes]
    (when (> (count list) 0)
      (let [attrib (first list)]
        ;; in the line below adding "!" to the uniform string will create missing key.        
        (oset!+ program (str "!" attrib) (.getAttribLocation gl program attrib)))
      (recur (rest list))))
  )

(defn set-uniform-locations
  "Sets program uniform locations"
  [program gl uniforms]
  (loop [list uniforms]
    (when (> (count list) 0)
      (let [uniform (first list)]
        ;; in the line below adding "!" to the uniform string will create missing key.
        (oset!+ program (str "!" uniform) (.getUniformLocation gl program uniform
                                                               )))
      (recur (rest list)))))

(defn map-attrib-locations
  "Returns a map of program attribute locations"
  [program gl attributes]

  (reduce (fn [result attrib]
            (assoc result (keyword  attrib) (.getAttribLocation gl program attrib)))
          {}
          attributes))

(defn map-uniform-locations
  "Returns a map of uniform locations"
  [program gl uniforms]
  (reduce (fn [result uniform]
            (assoc result (keyword uniform) (.getUniformLocation gl program uniform)))
          {}
          uniforms))

(defn load-program!
  "Loads program - sets program attribute and uniform locations"
  [program gl attributes uniforms]
  (set-attribute-locations program gl attributes)
  (set-uniform-locations program gl uniforms)
  program)

(defn create-program-map
  "Returns a map with the program, attribute locations and uniform locations"
  [program gl attributes uniforms]
  (-> {}
      (assoc :program program)
      (into (map-attrib-locations program gl attributes))
      (into (map-uniform-locations program gl uniforms))))







