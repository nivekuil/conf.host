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

(def not-found
  (ring.util.response/not-found "You found the 404 page!  Try again."))

(def index-get
  (GET
   "/" request
   (let [username (cookie-or "username" "{username}")
         lights (cookie-or "lights" "on")
         not-lights (if (= lights "off") "on" "off")]
     (render-template "index"
                      {:username username
                       :lights lights
                       :not-lights not-lights}))))

(def index-post
  (POST
   "/" {{username "username"} :params, {pubkey "pubkey"} :params}
   ;; (println (true? ( username pubkey)))
   ;; TODO: Input validation
   ;; (confhost.httpclient/register username pubkey)
   (if (or (clojure.string/blank? username)
           (clojure.string/blank? pubkey))
     not-found                          ;TODO: AJAX message
     (assoc (redirect "/")
            :cookies {"username" {:value username}}))))

(def user-get
  (GET "/:username" [username]
       (render-template "index"
                        {:username username})))

(defroutes routes #'index-get #'index-post #'user-get #'not-found)
