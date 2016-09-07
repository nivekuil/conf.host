(set-env! :dependencies
          '[[org.clojure/clojure "1.8.0"]
            [clj-http "2.2.0"]
            [clojure-humanize "0.2.0"]
            [compojure "1.5.1"]
            [hiccup "1.0.5"]
            [stencil "0.5.0"]
            [ring "1.5.0"]
            [clojurewerkz/elastisch "3.0.0-beta1"]
            [org.immutant/web "2.1.4"]
            [clj-ipfs-api "1.2.1"]]
          :resource-paths #{"src" "resources"})

(task-options!
 pom {:project 'confhost
      :version "0.1.0"}
 jar )

(deftask build [] (comp (pom) (jar) (install)))
(use 'confhost.core)
(deftask run [] (-main))
