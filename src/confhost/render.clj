(ns confhost.render
  (:require [confhost.util :as util]
            [hiccup.def :refer [defhtml]]
            [clojure.contrib.humanize :as humanize]
            [clojure.string :as string]))

(def base-uri "api/ipfs/")
(def icon-dir "/icons/")
;;; Set of file extensions that have associated icons
(def icons (confhost.util/get-filenames (str "resources/public" icon-dir)))

(defhtml files [results]
  (for [x results]
    (let [ext (:extension x)
          filename (:filename x)
          filesize (:filesize x)
          mtime (:mtime x)]
      [:tr
       [:td {:class "extension" :sorttable_customkey ext :title ext}
        (if (contains? icons ext) [:img {:src (str icon-dir ext ".svg")}]
            (humanize/truncate ext 11))]
       [:td {:class "filename"}
        [:a {:href (str base-uri (:hash x))} filename]]
       [:td {:class "filesize" :sorttable_customkey filesize}
        (if (>= filesize 1000)
          ;; humanize prints single bytes as decimal, which is ugly
          (humanize/filesize filesize) (str filesize "B"))]
       [:td {:class "mtime" :sorttable_customkey mtime :title mtime}
        (humanize/datetime mtime)]])))
