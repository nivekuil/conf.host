(ns confhost.api
  (:require [confhost.search :as search]
            [clojure.string :as string]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found]]
            [clj-ipfs-api.core :as ipfs]
            [ring.util.response :refer [redirect charset]]))

(defn wrap-api
  "((TODO)) Match the Content-Type of a response to the provided Accept header,
  or text/plain if there is none."
  [body]
  (charset {:headers {"Content-Type" "text/plain"}
            :body body} "UTF-8"))

(def api-index
  (GET "/" [request]
       "Welcome to the conf.host API!<br>Valid paths:<br>
/ipfs/:hash -- get the contents of an IPFS hash<br>
/:username/hashes -- get list of IPFS hashes associated with the given user"))

(def api-user-hashes
  (GET "/:username/hashes" [username]
       (let [query (search/query-user username)]
         (wrap-api (string/join " " (map :hash
                                         (map search/parse-files
                                              (:hits query))))))))

(def api-user-file
  ;; Need the regex for :filename because compojure treats "." as a separator
  (GET ["/:username/raw/:filename"] [username filename]
       (let [query (search/query-file username filename)]
         (wrap-api (ipfs/cat (-> query :hits first :_id))))))

(def api-ipfs-hash
  "Get a file by its IPFS hash"
  (GET ["/ipfs/:hash"] [hash]
       (wrap-api (ipfs/cat hash))))

(defroutes api-routes #'api-index #'api-ipfs-hash #'api-user-file
  #'api-user-hashes (not-found "API 404"))
