(ns niconico-parser.web.cli
  (:require
   [clojure.data.json :as json]
   [niconico-parser.web.core :as nw]
   [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(defn parse-nicodict-web
  ([url]
   (let [refine-data (nw/read-to-parsed url)]
     (with-open [writer (clojure.java.io/writer (clojure.string/replace (str  (:title refine-data) ".json") #"\s" "_")  :encoding "UTF-8" :append false)]
       (.write writer (json/write-str refine-data)))))
  ([url fname]
   (with-open [writer (clojure.java.io/writer fname :encoding "UTF-8")]
     (.write writer (json/write-str (nw/read-to-parsed url))))))


;; example
;; (parse-nicodict-web "https://dic.nicovideo.jp/a/アップランド" "./resources/アップランド.json")

;; (parse-nicodict-web "https://dic.nicovideo.jp/a/%E3%82%A4%E3%82%AD%E3%83%AA%E9%AF%96%E5%A4%AA%E9%83%8E" "./resources/イキリ鯖太郎.json")

(def cli-options
  [["-u" "--url URL" "URL start with  https://dic.nicovideo.jp/a/"
    :default nil
    :validate [#(clojure.string/starts-with? % "https://dic.nicovideo.jp/a/") "must start with 'https://dic.nicovideo.jp/a/'"]]])

(defn -main [& args]
  (let [opts (parse-opts args cli-options)]
    (if (or (-> opts :errors) (-> opts :options :url nil?))
      (println (:summary (parse-opts {} cli-options)))
      (parse-nicodict-web (-> opts :options :url)))))

;;(clojure.string/starts-with? "test" "t")

