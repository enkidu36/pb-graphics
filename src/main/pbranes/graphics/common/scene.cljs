(ns pbranes.graphics.common.scene
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :as async]))

(def base-url "http://localhost:3000")
(def geometry-url "/load/geometry")

(js/console.log "Test")


(defn load
  [filename alias coll]
  (go (let [response (async/<! (http/get (str base-url geometry-url)
                                         {:with-credentials? false
                                          :query-params {"filename" filename}}))]

        (swap! coll conj (-> response
                             :body
                             (merge (when (some? alias) {:alias alias}))
                             (into {:visible true}))))))

(defn load-by-parts
  [path count alias coll]

  (dotimes [n count]
    (let [path (str path (inc n) ".json")]
      (prn path)
      (load path "alias" coll))))

(comment
  (def files (atom nil))
  (prn   (load "test.json" "my test" files))
  (prn @files) 
  (prn (load-by-parts "audi-r8/part" 1 "audi" files))
  (prn @files)
  (some? "my")
  (prn "close comment"))




