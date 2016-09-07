(ns confhost.httpclient
  (:require [clj-http.client :as client]))

(def storage-url "http://localhost:5000")

(defn register
  [username pubkey]
  (client/post storage-url
               {:form-params
                {:username username, :pubkey pubkey}}))
