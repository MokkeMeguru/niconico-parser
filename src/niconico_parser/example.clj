(ns niconico-parser.example
  (:require
   [clojure.data.json :as json]
   [niconico-parser.web.core :as nw]))

(defn parse-nicodict-web [url fname]
  (with-open [writer (clojure.java.io/writer fname :encoding "UTF-8")]
    (.write writer (json/write-str (nw/read-to-parsed url)))))

;; example
;; (parse-nicodict-web "https://dic.nicovideo.jp/a/アップランド" "./resources/アップランド.json")

;; (parse-nicodict-web "https://dic.nicovideo.jp/a/%E3%82%A4%E3%82%AD%E3%83%AA%E9%AF%96%E5%A4%AA%E9%83%8E" "./resources/イキリ鯖太郎.json")
