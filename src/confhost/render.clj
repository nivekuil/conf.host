(ns confhost.render
  (:require [hiccup.core :refer [html]]
            [clojure.contrib.humanize :as humanize]))

(def file-base "http://ipfs.io/ipfs/")

(defn files [results]
  (html
   (for [x results]
     (let [extension (:extension x)
           filename (:filename x)
           filesize (:filesize x)
           mtime (:mtime x)]
       [:tr
        [:td {:class "extension"} extension]
        [:td {:class "filename"}
         [:a {:href (str file-base (:hash x))} filename]]
        [:td {:class "filesize" :sorttable_customkey filesize}
         (if (> filesize 1000)
           (humanize/filesize filesize) (str filesize "B"))]
        [:td {:class "mtime" :title mtime}
         (humanize/datetime mtime)]]))))
