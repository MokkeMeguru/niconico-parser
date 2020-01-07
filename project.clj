 (defproject niconico-parser "0.1.0-SNAPSHOT"
   :description "niconico dataset or web parser"
   :dependencies [[org.clojure/clojure "1.10.0"]

                  [metosin/compojure-api "2.0.0-alpha30"]
                  [clj-http "3.10.0"]
                  [enlive "1.1.6"]
                  [hickory "0.7.1"]
                  [org.clojure/tools.cli "0.4.2"]

                  [cheshire "5.9.0"]
                  [org.clojure/data.csv "0.1.4"]
                  [com.taoensso/nippy "2.14.0"]
                  [org.clojure/java.jdbc "0.7.9"]
                  [org.xerial/sqlite-jdbc "3.28.0"]
                  [org.clojure/data.json "0.2.6"]]
   ;; :ring {:handler niconico-parser.handler/app}
   ;; :uberjar-name "server.jar"
   :jvm-opts ["-Xmx8G"]
   :plugins [[cider/cider-nrepl "0.23.0-SNAPSHOT"]
             [refactor-nrepl "2.5.0-SNAPSHOT"]]
   :aliases {"parse-from-web" ["run" "-m" "niconico-parser.web.cli"]
             "preprocess-corpus" ["run" "-m" "niconico-parser.corpus.cli"]}
   :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]]
                   :plugins [[lein-ring "0.12.5"]]}})
