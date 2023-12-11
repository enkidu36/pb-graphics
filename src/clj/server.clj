(ns server
  (:require [reitit.ring :as ring]
            [reitit.coercion.spec]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring.coercion :as coercion]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            ;; Uncomment to use 
            ;;[reitit.ring.middleware.dev :as dev]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as r]
            [muuntaja.core :as m]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.cors :refer [wrap-cors]]))

(defn testing-handler [_]
  (prn "testing too")
  (r/response {:total 1}))


(def app
  (ring/ring-handler
   (ring/router
    [["/swagger.json"
      {:get {:no-doc true
             :swagger {:info {:title "pb-grapics-api"
                              :description "reitit ring with swagger, spec"}}
             :handler (swagger/create-swagger-handler)}}]
     
     ["/testing"
      {:middleware [#(wrap-cors % :access-control-allow-origin [#".*"]
                                :access-control-allow-methods [:get])]
       :get {:summary "testing"
             :responses {200 {:body {:total int?}}}
             :handler  testing-handler}}]]

    {;;:reitit.middleware/transform dev/print-request-diffs ;; pretty diffs
       ;;:validate spec/validate ;; enable spec validation for route data
       ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
     :exception pretty/exception
     :data {:coercion reitit.coercion.spec/coercion
            :muuntaja m/instance
            :middleware [;; swagger feature
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

(defonce server  (jetty/run-jetty (wrap-reload  #'app)
                                  {:port 3000, :join? false}))

(defn start []
  (.start server)
  (println "server running in port 3000"))

(defn stop []
  (.stop server))

(comment
  (start)
  (stop)

  (stop))
