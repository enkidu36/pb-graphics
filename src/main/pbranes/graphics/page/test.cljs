(ns pbranes.graphics.page.test)

(def vertices
  [-0.5 0.5 0.0
   -0.5 -0.5 0.0
   0.5 -0.5 0.0
   0.5 0.5 0.0])

(def indices
  [0 1 2
   0 2 3])

(defn cross-product [vs ind]
  (loop [i 0]
    (when (< i (count indices))
      (prn i)
      (recur (+ i 3)))))

(cross-product vertices indices)
(+ 2 2)
