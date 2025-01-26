(ns server
  (:require ;; Uncomment to use 
 ;;[reitit.ring.middleware.dev :as dev]
   [nextjournal.clerk :as clerk]
   [muuntaja.core :as m]
   [reitit.dev.pretty :as pretty]
   [reitit.ring :as ring]
   [reitit.coercion.spec]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters] ;; Uncomment to use 
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.util.response :as r]
   [clojure.java.io :as io]))

(defn load-geometry-handler [{{{:keys [filename]} :query} :parameters}]
  (-> (str "public/model/geometries/" filename)
      (slurp)
      (r/response)
      (r/content-type "application/json")))

(defn load-image-handler [{{{:keys [filename]} :query} :parameters}]
  (-> (str "public/images/" filename)
      (io/file)
      (r/response)
      (r/content-type "image/png")))


(def app
  (ring/ring-handler

   (ring/router
    [["/swagger.json"
      {:get {:no-doc true
             :swagger {:info {:title "pb-graphics-api"
                              :description "reitit ring with swagger, spec"}}
             :handler (swagger/create-swagger-handler)}}]

     ["/load"
      {:swagger {:tags ["files"]}}

      ["/geometry"
       {:middleware [#(wrap-cors % :access-control-allow-origin [#".*"]
                                 :access-control-allow-methods [:get])]
        :get {:summary "Download a json geometry"
              :swagger {:info {:title "load geometries"
                               :description "load files with geometries"}
                        :produces ["text/json"]}
              :parameters {:query {:filename string?}}
              :handler load-geometry-handler}}]

      ["/image"
       {:middleware [#(wrap-cors % :access-control-allow-origin [#".*"]
                                 :access-control-allow-methods [:get])]
        :get {:summary "Download an image"
              :swagger {:info {:title "load image"
                               :description "load files with images"}
                        :produces ["image/png"]}
              :parameters {:query {:filename string?}}
              :handler load-image-handler}}]]]

    { ;;:reitit.middleware/transform dev/print-request-diffs ;; pretty diffs
     ;;:validate spec/validate ;; enable spec validation for route data
     ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
     :exception pretty/exception
     :data {:coercion reitit.coercion.spec/coercion
            :muuntaja m/instance
            :middleware [ ;; swagger feature
                         swagger/swagger-feature
                         ;; query-params & form-params
                         parameters/parameters-middleware
                         ;; content-negotiation
                         muuntaja/format-negotiate-middleware
                         ;; encoding response body
                         muuntaja/format-response-middleware
                         ;; exception handling
                         (exception/create-exception-middleware
                          {::exception/default (partial exception/wrap-log-to-console exception/default-handler)})
                         ;; decoding request body
                         muuntaja/format-request-middleware
                         ;; coercing response bodys
                         coercion/coerce-response-middleware
                         ;; coercing request parameters
                         coercion/coerce-request-middleware
                         ;; multipart
                         multipart/multipart-middleware]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path "/"
      :config {:validatorUrl nil
               :operationsSorter "alpha"}})
    (ring/create-default-handler))))

(defonce server  (jetty/run-jetty (-> #'app
                                      wrap-reload
                                      (wrap-resource "public"))
                                  {:port 3000, :join? false}))

(defn my-test [] (prn "hello"))
(my-test)

(defn start []
  (.start server)
  (println "server running in port 3000"))

(defn stop []
  (.stop server))

(start)

;;(clerk/serve! {:browse true})
(comment
  (start)
  (stop)

  (stop)
  ;; start Clerk's built-in webserver on the default port 7777, opening the browser when done
  ;;  (clerk/serve! {:browse true})

  ;; either call `clerk/show!` explicitly
  ;; (clerk/show! "notebooks/rule_30.clj")   

  ;; or let Clerk watch the given `:paths` for changes
 (clerk/serve! { :watch-paths ["src"]})

  ;; start with watcher and show filter function to enable notebook pinning
  ;; (clerk/serve! {:watch-paths ["notebooks" "src"] :show-filter-fn #(clojure.stri;;ng/starts-with? % "notebooks"
  ;;                                                                   )})

  ;; Build a html file from the given notebook notebooks.
  ;; See the docstring for more options.
                                        ;(clerk/build! {:paths ["notebooks/rule_30.clj"]})
  )
