(ns niconico-parser.web.related
  (:require [clojure.string :as str]
            [hickory.select :as s]))

(def separate-keywords ["-" "…" "..." ])
(def extra-keywords ["/" "・" ])

(defn starts-with-keywords? [s keywords]
  (some true? (map #(str/starts-with? s %) keywords)))

(defn interpret-related-items [li-content]
  (if (string? li-content) [li-content]
      (->>
       (loop [data li-content
              res []]
         (if (-> data count zero?)
           res
           (cond
             (-> data first string?)
             (cond
               (->  data first str/trim (starts-with-keywords? separate-keywords)) res
               (-> data first str/trim (starts-with-keywords? extra-keywords)) (recur (rest data) (conj res "///"))
               :default (recur (rest data) (conj res (first data))))
             (-> data first map?)
             (recur (rest data)
                    (concat res (mapv interpret-related-items (-> data first :content))))
             :default (recur (rest data) res))))
       vec)))
;; (partition-by (partial = "/"))
;; (filter (partial not= (list "/")))
;; (map #(if (every? string? %) (str/join %) %))

(defn get-related-item-header-list [article]
  (->> article (s/select (s/child (s/tag :h2)))
       (filter #(clojure.string/includes? % "関連項目"))
       (map #(-> % :attrs :id))))

(defn get-related-item-list [article header-id]
  (->> article (s/select-locs (s/attr :id (partial = header-id))) first second :r
       (take-while #(-> % :tag (= :h2) not))
       (filter #(-> % :tag (= :ul)))))



 (def example_data ["label1" " / " "label2" "_label2" ["hoge" "bar"] "...hoge" "bar"])
 (interpret-related-items example_data)



;; (def separate-keywords ["-" "・" "…" "..."])
;; (let [s (-> "hoge" trim)]
;;   (every? false? (map #(clojure.string/starts-with? s %)
;;                       ["-" "・" "…" "..."])))
