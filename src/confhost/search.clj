(ns confhost.search
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.native.document :as esd]))

(def endpoints [["127.0.0.1" 9300]])
(def cluster-name "elasticsearch")
(def conn (es/connect endpoints {"cluster.name" cluster-name}))
(def index "resource")

(defn parse-user [result]
  "Parse each file returned from the search for relevant metadata."
  (assoc (select-keys
          (:_source result)
          [:extension :filename :filesize :mtime])
         :hash (:_id result)))

(defn query-user
  "Get all documents belonging to some user and return the metadata needed to
  generate the file browser interface."
  [username]
  (let [search (:hits
                (esd/search conn index username
                            {:query {:type {:value username}} :size 500}))
        ;; Use total from Elasticsearch to avoid redundant work
        num-results (:total search)
        results (:hits search)]
    {:num-results num-results
     :results (map parse-user results)}))
