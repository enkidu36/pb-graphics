(ns pbranes.graphics.page.lighting.controls
  (:require [pbranes.graphics.common.dgutils :as dg]
            [pbranes.graphics.common.utils :as u]
            ["dat.gui" :as datgui]))

(def car-data
  [{:car "BMW i8"
    :paint-alias "BMW"
    :parts-count 25
    :path "http://localhost:3000/load/geometry?filename=bmw-i8/part"}

   {:car "Audi R8"
    :paint-alias "Lack"
    :parts-count 150
    :path "http://localhost:3000/load/geometry?filename=audi-r8/part"}

   {:car "Ford Mustang"
    :paint-alias "pintura_carro"
    :parts-count 103
    :path "http://localhost:3000/load/geometry?filename=ford-mustang/part"}])

(def car-controls
  {:Car
   {:Model
    {:options (map #(:car %) car-data)
     :value "pick car"
     :onChange
     (fn [v]
       (js/console.log "onChange Model TODO: " v))}
    :Color
    {:value [255 255 255]
     :onChange (fn [v]
                 (js/console.log "onChange Color TODO: " v))}
    :Shininess
    {:value 0.5
     :min 0 :max 50 :step 0.1
     :onChange (fn [v]
                 (js/console.log "onChange Shininess TODO: " v))}}})



(defn create-light-control [light-key]
  ;; 
  (reduce
   (fn [result prop]
     (into result
           {prop {:value 0.5 :min 0 :max 1 :step 0.1
                  :onChange (fn [v] (js/console.log "TODO - create for key: " light-key " value: " v))}}))
   {}
   [:diffuse :specular]))

(defn light-controls [lights]
  ;; build configuration for a list of lights
  {:Lights (loop [result {}  lite-keys (keys @lights)]
             (if (empty?  lite-keys)
               result
               (let [light-key (first lite-keys)]
                 (recur (into result {light-key (create-light-control light-key)})
                        (rest lite-keys)))))})

(def background-control
  {:Background
   {:value (dg/de-normalize-color clear-color)
    :onChange (fn [v]
                (js/console.log (str "color: " (dg/normalize-color v))))}})

