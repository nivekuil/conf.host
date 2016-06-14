(ns confhost.routes
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [stencil.core :refer [render-string render-file]]
   [ring.util.response :refer [redirect]]))

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
  (:value (get (:cookies request) cookie)))

(defmacro cookie-or
  "Return the value of a cookie if it exists or a default value otherwise."
  [cookie default]
  `(or (get-cookie ~cookie ~'request) ~default))

(defn not-found []
  (ring.util.response/not-found "You found the 404 page!  Try again."))

(defn index-get [request]
  (let [username (cookie-or "username" "{username}")
        lights (cookie-or "lights" "on")
        not-lights (if (= lights "off") "on" "off")]
    (render-template "index"
                     {:username username
                      :lights lights
                      :not-lights not-lights})))

(defn index-post [request]
  (let [username (get (:params request) "username")
        pubkey (get (:params request) "pubkey")]
    ;; TODO: Input validation
    (println username pubkey)
    (if (and username pubkey)
      (assoc (redirect "/")
             :cookies {"username" {:value username}})
      (not-found))))

(defroutes index
  (GET "/" [] index-get)
  (POST "/" [] index-post))
