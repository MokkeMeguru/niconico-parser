(ns niconico-parser.corpus.cli
  (:require
   [niconico-parser.corpus.core :as npc]
   [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def cli-options
  [["-r" "--root ROOT_PATH" "ROOT PATH ex. /home/meguru/Documents/nico-dict/zips"
    :default nil]])

(defn -main [& args]
  (let [opts (parse-opts args cli-options)]
    (if (or (-> opts :errors) (-> opts :options :root nil?))
      (println (:summary (parse-opts {} cli-options)))
      (npc/process-whole (-> opts :options :root)))))
