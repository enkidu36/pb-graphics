(ns pbranes.graphics.layout
  (:require [helix.core :refer [defnc $ <>]]
            [helix.dom :as d]
            ["@mui/material" :refer [Grid Typography]]
            [react-router-dom :refer [Outlet, Link]]))

(defnc NavItem [{:keys [path label]}]
  (d/li  ($ Link {:to path}
            ($ Typography {:variant "h6"} label))))

(defnc Layout []
  (<>
   (d/nav
    (d/ul {:class-name "nav" :style {:list-style-type "none"}}
      ($ NavItem {:path "/" :label "Home"})
      ($ NavItem {:path "/canvas" :label "Canvas"})
      ($ NavItem {:path "/rendering" :label "Rendering"})))
   ($ Outlet)))
