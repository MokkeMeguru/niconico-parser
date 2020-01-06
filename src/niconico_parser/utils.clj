(ns niconico-parser.utils
  (:require
   [clojure.string :as str]
   [clj-http.client :as client]
   [hickory.select :as s])
  (:use [hickory.core]
        [hickory.render]))

;; ------ default utility ------------------
(defn thick-trim [s]
  (-> s
      (str/replace "\u00A0" " ")
      (str/replace "\u3000" " ")
      str/trim))

(defn trim-by-content
"trim string elements
  ex. \"      hello      \" ->  \"hello\" "
[d]
(let [_content (:content d)]
  (if (nil? _content) d
    (update d :content
            #(->> %
                  (map (fn [e]
                         (cond
                           (string? e) (thick-trim e)
                           (map? e) (trim-by-content e)
                           :default e)))
                  (filter (fn [e] (or  (not (string? e)) (not (str/blank? e)))))
                  vec)))))

(defn read-html-from-url [url]
(-> (client/get url) :body parse as-hickory trim-by-content))

(defn read-html-from-file [fname]
(-> fname clojure.java.io/resource slurp parse as-hickory trim-by-content))


;; example
;; (def example-html
;;   "<!DOCTYPE html>
;;   <html>
;;   <body>
;;   <h1>これがヘッダー</h1>
;;   <p>簡単なパラグラフ1</p>
;;   <p>簡単なパラグラフ2 <a href=\"パラグラフ2にあるリンク\">link</a></p>
;;   <ul>
;;   <li>li-1
;;   <li>li-2
;;   <li>li-3
;;   </ul>
;;   </body>
;;   </html>"
;;   )

;; (defn parse-str-html [str-html]
;;   (-> str-html
;;       parse
;;       as-hickory
;;       trim-by-content))

;; (def tmp (parse-str-html example-html))
;; (->> tmp
;;     (s/select
;;      (s/child
;;       (s/tag :body)))
;;     first
;;     (s/select
;;      (s/child
;;       (s/tag :p)))
;;     second)
