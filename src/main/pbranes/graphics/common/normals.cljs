(ns pbranes.graphics.common.normals
  (:require [clojure.walk :refer [walk]]))

(def vertices
  [-0.5 0.5 0.0
   -0.5 -0.5 0.0
   0.5 -0.5 0.0
   0.5 0.5 0.0])

(def indices
  [0 1 2
   0 2 3])

(def x 0)
(def y 1)
(def z 2)

(def test-normals [0 0 1 0 0 1 0 0 1 0 0 1])

(defn calc-vertex-ndx
  "Returns the coordinate index of a vertex represented
   from a list of vertex indices"
  [pt coord offset ind-triad]
  (+ coord (* offset (nth ind-triad pt))))

(defn find-vertex-coord
  [pt coord vs ind-triad]
  (nth vs (calc-vertex-ndx pt coord 3 ind-triad)))

(defn find-vertex-line
  [p2 p1 vs ind-triad]
  [(- (find-vertex-coord p2 x vs ind-triad) (find-vertex-coord p1 x vs ind-triad))
   (- (find-vertex-coord p2 y vs ind-triad) (find-vertex-coord p1 y vs ind-triad))
   (- (find-vertex-coord p2 z vs ind-triad) (find-vertex-coord p1 z vs ind-triad))])

(defn find-vectors
  ;; Return an array of vertices
  [ind-triad vs]

  [(find-vertex-line 2 1 vs ind-triad) (find-vertex-line 1 0 vs ind-triad)])

(defn calculate-normals [vs ind]

  (->> ind
       (partition 3)
       (map #(find-vectors % vs))))

(prn "indices" (partition 3 indices))

(prn (calculate-normals vertices indices))

(compare
 test-normals
 (js->clj  (js/utils.calculateNormals (clj->js vertices) (clj->js  indices))))
