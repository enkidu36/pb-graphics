(ns pbranes.graphics.page.home
  [:require [helix.core :refer [defnc <>]]
            [helix.dom :as d]])

(defnc Home []
  (<>
   (d/div {:class "container"}
          (d/h1 "Jane Doe")
          (d/div {:class "job-title"} "Web Developer")

          (d/p "Far far away, behind the word mountains, far from the countries Vokalia and
  Consonantia, there live the blind texts. Separated they live in Bookmarksgrove
  right at the coast of the Semantics, a large language ocean.")

          (d/p "A small river named Duden flows by their place and supplies it with the
  necessary regelialia. It is a paradisematic country, in which roasted parts of
  sentences fly into your mouth.")

          (d/h2 "Contact Information")

          (d/ul
           (d/li "Email: " (d/a {:href "mailto:jane@example.com"} "jane@example.com"))
           (d/li "Web: " (d/a {:href "http://example.com"} "http:example.com"))
           (d/li "Tel: 123 45678")))))

