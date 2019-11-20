 (defproject niconico-parser "0.1.0-SNAPSHOT"
   :description "niconico dataset or web parser"
   :dependencies [[org.clojure/clojure "1.10.0"]
                  [metosin/compojure-api "2.0.0-alpha30"]
                  [clj-http "3.10.0"]
                  [enlive "1.1.6"]
                  [hickory "0.7.1"]
                  [org.clojure/tools.cli "0.4.2"]]
   :ring {:handler niconico-parser.handler/app}
   :uberjar-name "server.jar"
   :aliases {"parse-from-web" ["run" "-m" "niconico-parser.web.cli"]}
   :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]]
                   :plugins [[lein-ring "0.12.5"]]}})
