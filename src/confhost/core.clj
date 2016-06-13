(ns confhost.core
  (:require [compojure.handler :as handler]
            [compojure.response :as response]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.cookies :refer [wrap-cookies]])p
  (:require [confhost.routes]))

(def app
  (-> (compojure.core/routes
       confhost.routes/index
       (route/resources "/")
       (route/not-found "You found the 404 page!  Try again."))
      wrap-cookies))
#_
(do (.stop server)
    (def server (run-jetty app {:port 3000 :join? false})))

(defn -main []
  (def server (run-jetty app {:port 3000 :join? false})))
