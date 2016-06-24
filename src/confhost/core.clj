(ns confhost.core
  (:require [compojure.core :refer [routes]]
            [compojure.route :refer [resources]]
            [immutant.web :as web]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.params :refer [wrap-params]])
  (:require [confhost.routes]))

(def app
  (-> (routes
       #'confhost.routes/routes
       (resources "/"))
      wrap-cookies wrap-params))
#_
(do (web/stop server)
    (def server (web/run app {:host "192.168.1.18" :port 3000})))

(defn -main [& args]
  (defonce server (web/run app {:host "192.168.1.18" :port 3000})))

#_
(defn -main [& args]
  (defonce server (run-jetty app {:port 3000 :join? false})))
#_
(do (.stop server)
    (def server (run-jetty app {:port 3000 :join? false})))
