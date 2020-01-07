(ns niconico-parser.web.core
  (:require [clojure.string :refer [trim blank? split]]
            [clj-http.client :as client]
            [hickory.select :as s]
            [clojure.data.json :as json]
            [niconico-parser.utils :refer :all]
            [clojure.string :as str]
            [niconico-parser.web.related :as related]
            [niconico-parser.web.remove-tag :as rtag])
  (:use [hickory.core]
        [hickory.render])
  (:gen-class))

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
  (->> article :content
       (take-while  #(-> % :attrs :class (not=  "adsense-728 a-banner_space-bottom")))))

(defn remove-empty-content
  ([hick]
   (remove-empty-content hick #{}))
  ([hick remove-tags]
   (cond
     (map? hick)
     (when-not (-> hick :type (= :comment))
       (if (->> hick :tag (contains? remove-tags))
         (-> hick :content (remove-empty-content  remove-tags))
         (when-not (-> hick :content count zero?)
           (let [content  (->  hick :content (remove-empty-content remove-tags))]
             (when-not (-> content count zero?)
               (assoc hick :content content))))))
     (vector? hick)
     (->>
      (mapv #(-> % (remove-empty-content remove-tags)) hick)
      (remove nil?)
      flatten
      vec)
     (string? hick)
     hick
     :default
     nil)))

(defn concat-strs [sodl]
  (let [merge-strs
        (fn [sl]
          (loop [in sl
                 out []]
            (if (-> in count zero?) (-> out reverse vec)
                (if (-> in first string?)
                  (recur
                   (drop-while string? in)
                   (cons (clojure.string/join "" (take-while string? in)) out))
                  (recur
                   (rest in)
                   (cons (-> in first  concat-strs) out))))))]
    (cond
      (map? sodl) (update sodl :content concat-strs)
      (vector? sodl) (vec (merge-strs sodl))
      :default
      sodl)))

(defn rec-dissoc-attrs [article]
  (let [rec-content (fn [art]
                      (if (:content art)
                        (update art :content rec-dissoc-attrs)
                        art))]
    (cond
      (map? article)
      (-> article
          (dissoc :attrs)
          (dissoc :type)
          rec-content)
      (vector? article)
      (mapv rec-dissoc-attrs article)
      :default
      article)))

(defn custom-take-while
  [func item]
  (loop [acc []
         _item item]
    (println acc)
    (if (and  (func (first _item)) (-> _item count zero? not))
      (recur (conj acc (first _item)) (rest _item))
      acc)))

(defn append-related-words [article]
  (let [key-ids (related/get-related-item-header-list article)]
    (mapv
     (fn [key-id]
       (->> article
            :content
            (take-while #(-> % :attrs :class (not= "adsense-728 a-banner_space-bottom")))
            (map #(remove-empty-content % #{:div}))
            flatten
            (filter #(-> % nil? not))
            (drop-while #(-> % :attrs :id (not= key-id)))
            rest
            (filter #(-> % nil? not))
            (take-while
             #(-> % :tag name (clojure.string/starts-with? "h") not))
            (map #(-> %
                      (remove-empty-content rtag/remove-tags)
                      concat-strs))
            (map #(-> %
                      (remove-empty-content #{:td :li})))
            (map rec-dissoc-attrs)
            (filter #(-> % nil? not))
            ))
     key-ids)))

(defn read-to-parsed [url]
  (let [raw (read-html-from-url-n url)]
    (-> raw
        (assoc
         :related_words
         (append-related-words (:article raw)))
        (assoc
         :article
         (->> raw
             :article
             filter-article-content
             (map #(->
                    %
                    (remove-empty-content rtag/remove-tags)
                    concat-strs))
             (filter #(-> % nil? not)))))))

;; ----------------- Example for debug ---------------------------------------------

;; (clojure.pprint/pprint
;;  (->> raw2
;;      :article
;;      :content
;;      (take-while #(-> % :attrs :class (not=  "adsense-728 a-banner_space-bottom")))
;;      (map #(remove-empty-content % #{:div}))
;;      flatten
;;      (map #(-> % :attrs))
;;      ))


;; (clojure.pprint/pprint
;;  (let [_raw raw2
;;        article-content
;;        (-> _raw
;;            :article
;;             :content)
;;        key-ids (related/get-related-item-header-list (:article _raw))]
;;     (map
;;      (fn [key-id]
;;        (->> article-content
;;             (take-while #(-> % :attrs :class (not=  "adsense-728 a-banner_space-bottom")))
;;             (map #(remove-empty-content % #{:div}))
;;             flatten
;;             (drop-while #(-> % :attrs :id (not= key-id)))
;;              rest
;;             (take-while #(-> % :tag str (clojure.string/starts-with? "h") not))
;;             (map #(-> %
;;                       (remove-empty-content rtag/remove-tags)
;;                       concat-strs))
;;             (map #(-> %
;;                       (remove-empty-content #{:td :li})))
;;             (map rec-dissoc-attrs)
;;             (filter #(-> % nil? not))
;;             ))
;;      key-ids)))

;; (def raw2 (read-html-from-url-n "https://dic.nicovideo.jp/a/%E9%88%B4%E5%8E%9F%E3%82%8B%E3%82%8B"))
;; (def raw (read-html-from-url-n  "https://dic.nicovideo.jp/a/%E6%A1%90%E7%94%9F%E3%82%B3%E3%82%B3"))
;; (->> raw
;;     :article
;;     filter-article-content
;;     (map #(->
;;            %
;;            (remove-empty-content rtag/remove-tags)
;;            concat-strs)))


;; (defn append-related_words-v2 [article]
;;   (mapv
;;    (fn [header]
;;      (-> (related/get-related-item-list article header)
;;          first
;;          filter-article-content
;;          parse-example-item
;;          flatten-string
;;          concat-strs
;;          (as-> $
;;              (map
;;               #(-> % :content
;;                    flatten
;;                    related/interpret-related-items) $))
;;          (as-> $
;;              (map #(if (every? string? %) [(str/join %)] %) $))))
;;    (related/get-related-item-header-list article)))

;; (append-related_words-v2 (:article raw))

;; (append-related_words (:article raw))

;; (def related
;;   (let [headers (related/get-related-item-header-list (:article raw2))]
;;     (mapv
;;      #(related/get-related-item-list (:article raw) %)
;;      headers)))
;; (println related)

;; (related/get-related-item-list (:article raw2) (related/get-related-item-header-list (:article raw2)))



;; (count
;;  (map
;;    #(->>
;;      raw2
;;      :article
;;      ;; filter-article-content
;;      (s/select-locs
;;       (s/attr :id (partial = "h2-5"))))
;;    (related/get-related-item-header-list (:article raw2))))

;; (first (related/get-related-item-header-list (:article raw2)))
;; (first  (related/get-related-item-header-list (:article raw2)))
;; (mapv
;;  (-> parse-example-item
;;      concat-strs)
;;  related)

;; (def testt
;;   (->> (:article raw)
;;        (s/select
;;         (s/child
;;          (s/tag :h2)))
;;        ))
;; (filter #(-> % count zero? not) (map #(filter (fn [word] (clojure.string/includes? word "関連項目")) (:content %)) testt))

;; (->>
;;  (:article raw)
;;  (s/select
;;   (s/child
;;    (s/tag :h2)))
;;  (filter
;;   (fn [block]
;;     (->
;;      (filter (fn [word] (clojure.string/includes? word "関連項目")) (:content block))
;;      count zero? not)))
;;  (map #(-> % :attrs :id)))
