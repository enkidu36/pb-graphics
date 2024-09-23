(ns pbranes.graphics.common.dgutils-test
  (:require [cljs.test :refer-macros [testing is deftest]]
            [pbranes.graphics.common.dgutils :as dg]))

(set! *warn-on-infer* false)

(deftest normalize-color-test
  (testing "Normalize color maps 0-255 to 0-1  "
    (is (= [0 0 0] (dg/normalize-color [0 0 0])))
    (is (= [1 1 1] (dg/normalize-color [255 255 255])))))

(deftest de-normalize-color-test
  (testing "Normalize color maps 0-255 to 0-1"
    (is (= [0 0 0] (dg/de-normalize-color [0 0 0])))
    (is (= [255 255 255] (dg/de-normalize-color [1 1 1])))))

(deftest folder-test
  (is (dg/folder? {})))

(deftest action-test
  (is (dg/action? #(prn "hi"))))


