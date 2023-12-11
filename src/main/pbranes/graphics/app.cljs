(ns pbranes.graphics.app
  (:require [helix.core :refer [defnc $ <>]]
            [helix.dom :as d]
            [react-router-dom :as rr]
            ["react-dom/client" :as rdom]))

(defnc Blogs []
  (d/h1 "Blogs"))

(defnc Home []
  (d/h1 "Home"))

(defnc Contacts []
  (d/h1 "Contacts"))

(defnc Layout []
  (<>
   (d/nav
    (d/ul
     (d/li ($ rr/Link {:to "/"} "Home"))
     (d/li ($ rr/Link {:to "/blogs"} "Blogs"))
     (d/li ($ rr/Link {:to "/contacts"} "Contacts"))))
   ($ rr/Outlet)))

(defnc Router []
  ($ rr/Routes
     ($ rr/Route {:path "/" :element ($ Layout)}
        ($ rr/Route {:path "/" :element ($ Home)} )
        ($ rr/Route {:path "/blogs" :element ($ Blogs)})
        ($ rr/Route {:path "/contacts" :element ($ Contacts)}))))

(defnc app []
  ($ rr/BrowserRouter
     ($ Router)))

;; start your app with your favorite React renderer
(defonce root (rdom/createRoot (js/document.getElementById "root")))

(defn ^:dev/after-load init! []
  (.render root ($ app)))
