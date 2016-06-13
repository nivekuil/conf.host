(ns confhost.routes
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [stencil.core :refer [render-string render-file]]
   [ring.middleware.cookies :refer [cookies-request cookies-response]]))

;;; Set TTL to 0 so templates are instantly reloaded
(stencil.loader/set-cache (clojure.core.cache/ttl-cache-factory {} :ttl 0))

(defn render-template
  "Wraps render-file to use a default template-dir."
  [template-name data-map]
  (let [template-dir "templates/"]
    (render-file (str template-dir template-name) data-map)))

(defn get-cookie
  "Return the value of a cookie in the HTML request."
  [cookie request]
  (:value ((:cookies request) cookie)))

(defmacro cookie-or
  "Return the value of a cookie if it exists or a default value otherwise."
  [cookie default]
  `(or (get-cookie ~cookie ~'request) ~default))

(defn index-get [request]
  (let [username (cookie-or "username" "{username}")
        lights (cookie-or "lights" "on")
        not-lights (if (= lights "off") "on" "off")]
    (render-template "index"
                     {:username "{username}"
                      :lights lights
                      :not-lights not-lights})))

(defn index-post [request]
  (index-get [request]))

(defroutes index
  (GET "/" [] index-get)
  (POST "/" [] index-post))
