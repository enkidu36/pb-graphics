(ns test
  (:require [clojure.math :as math]))

;; (def vertices
;;   [-0.5 0.5 0.0
;;    -0.5 -0.5 0.0
;;    0.5 -0.5 0.0
;;    0.5 0.5 0.0])


;; (def indices
;;   [0 1 2
;;    0 2 3])

;; (def x 0)
;; (def y 1)
;; (def z 2)

;; (defn v1
;;   "vertex p2 - p1"
;;   [i vs ind]
;;   {:x (- (nth vs (+ (* 3 (nth ind (+ i 2))) x)) (nth vs (+ (* 3 (nth ind (+ i 1))) x)))
;;    :y (- (nth vs (+ (* 3 (nth ind (+ i 2))) y)) (nth vs (+ (* 3 (nth ind (+ i 1))) y)))
;;    :z (- (nth vs (+ (* 3 (nth ind (+ 1 2))) z)) (nth vs (+ (* 3 (nth ind (+ i 1))) z)))})

;; (defn v2
;;   "vertex p0 - p1"
;;   [i vs ind]
;;   {:x (- (nth vs (+ (* 3 (nth ind i)) x)) (nth vs (+ (* 3 (nth ind (+ i 1))) x)))
;;    :y (- (nth vs (+ (* 3 (nth ind i)) y)) (nth vs (+ (* 3 (nth ind (+ i 1))) y)))
;;    :z (- (nth vs (+ (* 3 (nth ind i)) z)) (nth vs (+ (* 3 (nth ind (+ i 1))) z)))})

;; (defn calc-length
;;   "Return length of 3 dimensional vector."
;;   [[x y z]]
;;   (math/sqrt (+ (math/pow x 2) (math/pow y 2) (math/pow z 2))))

;; (defn normalize
;;   "Returns a normalize sequence 3 numbers"
;;   [[x y z]]
;;   (let [calc-len (calc-length [x y z])
;;         normal-len (if (zero? calc-len) 1.0 calc-len)]
;;     [(/ x normal-len) (/ y normal-len) (/ z normal-len)]))

;; (defn -x [vec]
;;   (nth vec x))

;; (defn -y [vec]
;;   (nth vec y))

;; (defn -z [vec]
;;   (nth vec z))

;; (defn cross-product [vec1 vec2]
;;   (list
;;    (- (* (-y vec1) (-z vec2)) (* (-z vec1) (-y vec2)))
;;    (- (* (-z vec1) (-x vec2)) (* (-x vec1) (-z vec2)))
;;    (- (* (-x vec1) (-y vec2)) (* (-y vec1) (-x vec2)))))

;; (comment
;;   (cross-product '(3 0 1) '(4 5 2))
;;   (cross-product [-4 4 1] [2 1 0])
;;   (normalize (cross-product [3 0 1] [4 5 2])))

;; (defn calc-cross-product [vs, ind]
;;   (loop [i 0 ns []]
;;     (if (>= i  (count ind))
;;       ns
;;       (recur (+ i 3)
;;              (let [v1 (v1 i vs ind)
;;                    v2 (v2 i vs ind)
;;                    nx (- (* (:y v1) (:z v2)) (* (:z v1) (:y v2)))
;;                    ny (- (* (:z v1) (:x v2)) (* (:x v1) (:z v2)))
;;                    nz (- (* (:x v1) (:y v2)) (* (:y v1) (:x v2)))]

;;                (concat ns [nx ny nz]))))))

;; (calc-cross-product vertices indices)


;; (->> vertices
;;      (partition 3)
;;      (map #(normalize (vec %))))

;; (defn normalize-vectors_bak
;;   [vectors vs ind]

;;   (loop [i 0 ns []]
;;     (if (>= i (count vectors))
;;       ns
;;       (recur (+ i 3)
;;              (let [nx (nth vectors (+ i x))
;;                    ny (nth vectors (+ i y))
;;                    nz (nth vectors (+ i z))]
;;                (concat ns (normalize [nx ny nz])))))))

;; (defn normalize-vectors-copy [vertices]
;;   (let [vectors (map #(vec %) (partition 3 vertices))]
;;     (loop [i 0 ns []]
;;       (if (>= i (count vectors)))
;;       nil
;;       (concat ns (normalize (nth vectors i))))))

(defn normalize-vectors [vertices]
  (reduce (fn [acc item] (conj! acc (vec item))) [] (partition 3 vertices)))

;;(normalize-vectors vertices)

;; (defn calc-normals
;;   "Returns computed normals for provided vertices.
;;    Note: Indices have to be completely defined. No TRIANGLE_STRIP only Triangles"
;;   [vs, ind]
;;   (-> (calc-cross-product vs ind)
;;       (normalize-vectors vs ind)))

;; (calc-normals vertices indices)

(comment
  ;; (calc-normals vertices indices)
  ;; (calc-cross-product vertices indices)

  
  ;; (defn calc-optimal-energy [weight-lbs fat-percent energy-factor]

  ;;   (let [weight (/ weight-lbs 2.2) ;; convert to kilograms
  ;;         body-fat (* weight fat-percent)
  ;;         fat-free-mass (- weight body-fat)]
  ;;     (* fat-free-mass energy-factor)))
  
  ;; (calc-optimal-energy 211 0.301 35)
  )
