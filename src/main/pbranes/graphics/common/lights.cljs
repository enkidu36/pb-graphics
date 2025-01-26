(ns pbranes.graphics.common.lights)

(defrecord Light
    [id position diffuse specular])

(comment

  ;; light positions to play with
  (def light-positions
    {:far-left [-1000 1000 -1000]
     :far-right [1000 1000 -1000]
     :near-left [-1000 1000 1000]
     :near-right [1000 1000 1000]})

  (prn (map (fn [[k v]]
            (Light. k v [0.4 0.4 0.4] [0.8 0.8 0.8])) light-positions))
  )


 





