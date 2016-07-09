(ns confhost.util
  (:require [clojure.string :as string]))

(defn get-filenames [dir]
  "Return the set of file extensions that have icons to display."
  (let [f (clojure.java.io/file dir)]
    (set
     (rest
      (map
       (fn [x]
         (string/join (butlast (string/split (.getName x) #"\."))))
       (file-seq f))))))
