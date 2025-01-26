(ns pbranes.graphics.app
  (:require [helix.core :refer [defnc $ <>]]
            [helix.dom :as d]
            [react-router-dom :as rr]
            [pbranes.graphics.layout :refer [Layout]]
            [pbranes.graphics.page.render-modes :refer [modes-page]]
            [pbranes.graphics.page.lighting.final :refer [final-page]]
            [pbranes.graphics.page.camera.camera :refer [camera-page]]
            ["react-dom/client" :as rdom]))

(defnc Home []
  (<>
   (d/div
     {:style {:color "white" :height "400px" :width "200px" :background-color "blue"}} "Hello world")))

(defnc Router []
  ($ rr/Routes
     ($ rr/Route {:path "/" :element ($ Layout)}
        ($ rr/Route {:path "/home" :element ($ Home)})
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
