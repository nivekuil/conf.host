(ns confhost.core
  (:require [confhost.routes :refer [routes]]
            [confhost.api :refer [api-routes]]
            [compojure.core :as compojure]
            [compojure.route :refer [resources]]
            [immutant.web :as web]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]))

(def app
  (-> #'routes
      (wrap-cookies) (wrap-params) (wrap-resource "public")
      (wrap-content-type)))

(def api
  (-> #'api-routes
      (wrap-cookies) (wrap-params) (wrap-resource "public")
      (wrap-content-type)))
#_
(do (web/run app {:host "0.0.0.0" :port 3000})
    (web/run api {:host "0.0.0.0" :port 3000
                  :path "/api"})
    (web/stop {:host "0.0.0.0 :port 3000"})
    (web/stop {:host "0.0.0.0 :port 3000" :path "/api"})
    (def server (web/run app {:host "0.0.0.0" :port 3000}))
    (def api-server (web/run api {:host "0.0.0.0" :port 3000
                                  :path "/api"})))

(defn -main [& args]
  (defonce server (web/run app {:host "0.0.0.0" :port 3000}))
  (defonce api-server (web/run api {:host "0.0.0.0" :port 3000 :path "/api"})))

#_
(defn -main [& args]
  (defonce server (run-jetty app {:port 3000 :join? false})))
#_
(do (.stop server)
    (def server (run-jetty app {:port 3000 :join? false})))
