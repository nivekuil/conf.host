(ns confhost.routes
  (:require
   [clojure.string :as string]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :refer [not-found]]
   [stencil.core :refer [render-string render-file]]
   [ring.util.response :refer [redirect charset]]
   [clj-ipfs-api.core :as ipfs]
   [confhost.httpclient :as httpclient]
   [confhost.render :as render]
   [confhost.search :as search]))

;;; Set TTL to 0 so templates are instantly reloaded
(stencil.loader/set-cache (clojure.core.cache/ttl-cache-factory {} :ttl 0))

(defn render-template
  "Wraps render-file to use a default template-dir."
  [template-name data-map]
  (let [template-dir "templates/"]
    (render-file (str template-dir template-name) data-map)))

(defmacro cookie-or
  "Return the value of a cookie if it exists or a default value otherwise."
  [cookie default]
  `(or (get-in ~'request [:cookies ~cookie :value]) ~default))

(defn wrap-plain
  "Add the text/plain header to the response body."
  [body]
  (charset {:headers {"Content-Type" "text/plain"}
            :body body} "UTF-8"))

(def index-get
  (GET "/" request
       (let [username (cookie-or "username" "$USER")
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
   (if (or (string/blank? username)
           (string/blank? pubkey))
     (redirect "/")                     ;TODO: AJAX message
     (do (httpclient/register username pubkey)
         (assoc (redirect "/")
                :cookies {"username" {:value username}})))))

(def user-get
  (GET "/:username" [username]
       (let [query (search/query-user username)
             files (map search/parse-files (:hits query))
             total (:total query)]
         (render-template "user"
                          {:username username
                           :rows (render/files files)
                           :total total}))))

(def user-file
  ;; Need the regex for :filename because compojure treats "." as a separator
  (GET ["/:username/:filename", :filename #"[[a-z]\.]+"] [username filename]
       (let [query (search/query-file username filename)
             body (ipfs/cat (-> query :hits first :_id))]
         (wrap-plain body))))

(defroutes routes
  #'index-get #'index-post #'user-get #'user-file (not-found "404"))
