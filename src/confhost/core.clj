(ns confhost.core
  (:require [compojure.handler :as handler]
            [compojure.response :as response]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.params :refer [wrap-params]])
  (:require [confhost.routes]))

(def app
  (-> (compojure.core/routes
       confhost.routes/index
       (route/resources "/")
       (confhost.routes/not-found))
      wrap-cookies wrap-params))
#_
(do (.stop server)
    (def server (run-jetty app {:port 3000 :join? false})))

(defn -main []
  (def server (run-jetty app {:port 3000 :join? false})))
