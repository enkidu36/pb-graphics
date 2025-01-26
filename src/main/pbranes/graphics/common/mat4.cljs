(ns pbranes.graphics.common.mat4
  (:require ["gl-matrix" :as m]))
(defn create-matrix
  ^{:doc "Create a 4 x 4 matrix"}
  []
  (m/mat4.create))

(defn copy-matrix
  ^{:doc "Returns a copy of a 4 x 4 matrix"}
  [out in]
  (m/mat4.copy out in))

(defn identity-matrix
  ^{:doc "Returns an identity"}
  [in]
  (m/mat4.identity in))

(defn invert-matrix
  ^{:doc "Returns inverted 4 x 4 matrix"}
  [in]
  (m/mat4.invert in in))

(defn transpose-matrix
  ^{:doc "Returns transposed 4 x 4 matrix"}
  [in]
  (m/mat4.transpose in in))

(defn translate-matrix
  ^{:doc "Returns vector translated by vector 'v' "}
  [in v]
  (m/mat4.translate in in (clj->js v)))

(defn perspective-matrix
  ^{:doc "Return a perspective matrix with given bounds."}
  [in fovy, aspect, near, far]
  (m/mat4.perspective in fovy aspect near far))

(defn rotate-matrix
  ^{:doc "Return matrix rotated"}
  [in rad axis]
  (m/mat4.rotate in in rad (clj->js axis)))


