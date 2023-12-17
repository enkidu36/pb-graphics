(ns pbranes.graphics.app
  (:require [helix.core :refer [defnc $]]
            [helix.dom :as d]
            [react-router-dom :as rr]
            [pbranes.graphics.layout :refer [Layout]]
            [pbranes.graphics.page.canvas :refer [CanvasPage]]
            ["react-dom/client" :as rdom]))

(defnc Home []
  (d/h1 "Home"))

(defnc Router []
  ($ rr/Routes
     ($ rr/Route {:path "/" :element ($ Layout)}
        ($ rr/Route {:path "/" :element ($ Home)})
        ($ rr/Route {:path "/canvas" :element ($ CanvasPage)}))))

(defnc app []
  ($ rr/BrowserRouter
     ($ Router)))

;; start your app with your favorite React renderer
(defonce root (rdom/createRoot (js/document.getElementById "root")))

(defn ^:dev/after-load init! []
  (.render root ($ app)))
