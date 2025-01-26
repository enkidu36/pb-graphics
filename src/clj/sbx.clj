(ns sbx
  (:require [clojure.walk :as w]))
(def expression-tree
  {:function +
   :children
   [1 {:function *
       :children [2 6]}]})

(defn evaluate [node]
  (if-let [f (:function node)]
    (do (prn (:function node) "   " (:children node))
        (apply f (:children node)))
    node))

(defn watch [x] (println "visiting: " x) x)

(w/postwalk watch expression-tree)

(w/postwalk evaluate expression-tree)

