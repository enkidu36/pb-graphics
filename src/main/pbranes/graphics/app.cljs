(ns pbranes.graphics.app
  (:require [helix.core :refer [defnc $]]
            [react-router-dom :as rr]
            [pbranes.graphics.layout :refer [Layout]]
            [pbranes.graphics.page.home :refer [Home]]
            [pbranes.graphics.page.geometry :as g]
            [pbranes.graphics.page.render-modes :refer [modes-page]]
            [pbranes.graphics.page.lighting.final :refer [final-page]]
            [pbranes.graphics.page.camera.camera :refer [camera-page]]
            ["react-dom/client" :as rdom]))

(defnc Router []
  ($ rr/Routes
     ($ rr/Route {:path "/" :element ($ Layout)}
        ($ rr/Route {:path "/home" :element ($ Home )})
        ($ rr/Route {:path "/geometry" :element ($ g/page)})
        ($ rr/Route {:path "/modes" :element ($ modes-page)})
        ($ rr/Route {:path "/lighting" :element ($ final-page)})
        ($ rr/Route {:path "/camera" :element ($ camera-page)})

        )))

(defnc app []
  ($ rr/BrowserRouter
     ($ Router)))

;; start your app with your favorite React renderer
(defonce root (rdom/createRoot (js/document.getElementById "root")))

(defn ^:dev/after-load init! []
  (.render root ($ app)))
