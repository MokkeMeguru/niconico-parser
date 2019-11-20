(ns niconico-parser.web.core
  (:require [clojure.string :refer [trim blank? split]]
            [clj-http.client :as client]
            [hickory.select :as s]
            [niconico-parser.utils :refer :all])
  (:use [hickory.core]
        [hickory.render]))

;; ------ for niconico dict (web) --------------------

(defn read-html-from-url-n [url]
  (let [;;  raw の 子要素の、tag が html である要素の子要素
        raw (->> (read-html-from-url url)
                 (s/select
                  (s/child
                   (s/tag :html)))
                 first)
        ;; tag が head であるものの第一要素を取り出す。
        head (->> raw
                  (s/select
                   (s/child
                    (s/tag :head)))
                  first)
        title-head (->> raw (s/select (s/child (s/tag :title))) first :content first)
        keywords (split
                  (->> raw
                       (s/select (s/child (s/attr :name (partial = "keywords"))))
                       first :attrs :content) #",")
        article (->> raw (s/select (s/child (s/class :article))) first)
        title (->> raw (s/select (s/child (s/class :article-title-text))) first (s/select (s/child (s/tag :h1))) first :content first)
        ]
    {:title-head title-head
     :keywords keywords
     :article article
     :title title}))

(defn filter-article-content [article]
  (->> article :content (take-while #(not= (-> % :attrs :class) "adsense-728 a-banner_space-bottom")) ))


;; TODO: Please Edit this function!
;; ref: https://github.com/MokkeMeguru/htmlparser/blob/master/doc/adv-doc.org
(defn parse-example-item [item]
  (mapv (fn [element]
          (if (string? element) element
              (condp = (:tag element)
                :span (parse-example-item (:content element))

                :em (parse-example-item (:content element))
                :a (parse-example-item (:content element))
                :strong (parse-example-item (:content element))
                :sub (parse-example-item (:content element))
                :b (parse-example-item (:content element))
                :nobr (parse-example-item (:content element))
                :i (parse-example-item (:content element))
                :hr (parse-example-item (:content element))
                :br (parse-example-item (:content element))

                :p (parse-example-item (:content element))

                :div (parse-example-item (:content element))
                :blockquote (parse-example-item (:content element))
                (if (-> element :content count zero?)
                  element
                  (assoc element :content (parse-example-item (:content element)))))))
        item))


(defn flatten-string [parsed-item]
  (let [parsed-item  (doall parsed-item)]
    (cond
      (map? parsed-item)
      (assoc parsed-item :content [(flatten-string (:content parsed-item))])
      (or (vector? parsed-item) (list? parsed-item))
      (let [filtered-parsed-item (filter #(-> % count zero? not) parsed-item)]
        (if (= 1 (count  filtered-parsed-item))
          (flatten-string (first filtered-parsed-item))
          (into [] (concat (map
                     #(flatten-string %) filtered-parsed-item))))
        )
      :default parsed-item)))

(defn concat-strs [str-or-dict-lst]
  (loop [res []
         sodl str-or-dict-lst]
    (if (or (string? sodl) (zero? (count sodl))) res
        (cond
          (-> sodl  map?)  sodl
          (-> sodl first string?)
          ;; TODO: remove flatten
          (recur (conj  res (apply str  (filter string? (flatten (take-while #(not (map? %)) sodl)))))
                 (drop-while #(not (map? %)) sodl))
          (-> sodl #(or (-> % first vector?) (-> % first list?))) (mapv concat-strs sodl)
          :default  (recur (conj res (first sodl))
                           (rest sodl)))))
    )


(defn read-to-parsed [url]
  (let [raw (read-html-from-url-n url)]
    (assoc
     raw
     :article
     (-> raw
         :article
         filter-article-content
         parse-example-item
         flatten-string
         concat-strs
         ))
    ))

;; ;; example for debug

;; (def tmp (read-html-from-url-n "https://dic.nicovideo.jp/a/%E3%82%A2%E3%83%83%E3%83%97%E3%83%A9%E3%83%B3%E3%83%89"))
;; (keys tmp)

;; (:title-head tmp)


 ;; (map concat-strs (flatten-string (parse-example-item
 ;;                                  (filter-article-content                                    (:article tmp)))))


;; (filter-article-content (:article tmp))

;; (def tmp2 (read-html-from-url-n "https://dic.nicovideo.jp/a/%E3%82%B7%E3%83%A3%E3%83%9F%E5%AD%90%E3%81%8C%E6%82%AA%E3%81%84%E3%82%93%E3%81%A0%E3%82%88"))

;; ;; (filter-article-content (:article tmp2))



;; TODO: fix error on #concat-strs
;; [
;;  [
;;   "「",
;;   "グランド",
;;   "くそ野郎や穀潰しや",
;;   "乳上",
;;   "や",
;;   "太陽",
;;   "ゴリラ",
;;   "が許されて"
;;   ],
;;  "イキリ鯖太郎",
;;  "が許されないんですか」"
;;  ],
