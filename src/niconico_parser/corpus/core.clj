(ns niconico-parser.corpus.core
  (:require
   [clojure.string :refer [trim blank? split] :as str]
   [hickory.select :as s]
   [clojure.java.jdbc :as sql]
   [clojure.data.csv :as csv]
   [clojure.data.json :as json]
   [niconico-parser.utils :refer :all]
   [clojure.zip :as zip]
   [taoensso.nippy :as nippy]
   [clojure.java.io :as io]
   [clojure.tools.cli :refer [parse-opts]]
   [cheshire.core :as ches]
   [niconico-parser.corpus.remove-tag :as rtag]
   [niconico-parser.corpus.boundary :as bd])
  (:use [hickory.core]
        [hickory.render])
  (:gen-class))

;; ------ settings --------------------------------------------
(def start-year 8)
(def end-year 14)

;; ------ for niconico dict (corpus) --------------------

(defn get-file-lists-per-year
  "get a list of files in the folder"
  [^String folder-path]
  (-> folder-path clojure.java.io/file file-seq))

(defn get-file-lists
  "get a list of file for the structure
  ref. doc/corpus_structure.org
  usage:
  (get-file-lists \"/home/meguru/Documents/nico-dict/zips\" 8 14)"
  [^String zips-folder
   ^Integer start
   ^Integer end]
  (->>
   (range start (inc end))
   (mapcat (fn [year]
             (get-file-lists-per-year (str zips-folder "/" (format "rev20%02d" year)))))
   (filter #(and (not (str/ends-with? % "jsoned.csv")) (str/ends-with? % ".csv")))))

(defn html->hickory
  "html to hickory data
  ex.
  <div><h2>Hello!</h2><p>I'm happy your trying.</p></div>
  => 
  {:type :document,
  :content
  [{:type :element,
   :attrs nil,
   :tag :html,
   :content
   [{:type :element, :attrs nil, :tag :head, :content nil}
    {:type :element,
     :attrs nil,
     :tag :body,
     :content
     [{:type :element,
       :attrs nil,
       :tag :div,
       :content
       [{:type :element, :attrs nil, :tag :h2, :content [\"Hello!\"]}
        {:type :element,
         :attrs nil,
         :tag :p,
         :content [\"I'm happy your trying.\"]}]}]}]}]}"
  [^String html]
  (-> html
      (str/replace #"\\\n" "")
      (str/replace #"\u00A0" " ")
      (str/replace #"&nbsp;" " ")
      (str/replace #"(?<!(src|href))=\"(.*?)\""  "=\"\"")
      (str/replace #"[a-zA-Z]+=\"\"" "")
      (str/replace #"[\s|　]+" " ")
      (str/replace #"<br[^<>]*>" "")
      parse
      as-hickory))

(defn get-body-from-hick [hick]
  (s/select
   (s/child
    (s/tag :body))
   hick))

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

(defn get-links
  [extracted-hick]
  (remove-empty-content
   (s/select
    (s/child
     (s/tag :a))
    extracted-hick)
   (disj rtag/remove-tags :a)))

(defn parse-a-line [line db-spec]
  (let [[idx raw-html timestamp] line
        extracted-hick (-> raw-html html->hickory get-body-from-hick first)]
    (concat
     [idx
      (ches/generate-string (remove-empty-content  extracted-hick rtag/remove-tags))
       timestamp
      (ches/generate-string (get-links extracted-hick))]
     (vals (bd/get-article-info db-spec idx)))))

(defn parse-a-file [source db-spec]
  (let
      [from (io/as-file source)
       to (io/as-file (clojure.string/replace source #"\.csv$" "-jsoned.csv"))
       ]
    (if (.exists to)
      (println "[Info] process was finished")
      (do
        (println "[Info] process start: " (str from) " to "  (str to))
        (try
          (with-open
            [reader (io/reader from)
             writer (io/writer to)]
            (->>
             (csv/read-csv reader)
             (map #(parse-a-line % db-spec))
             (csv/write-csv writer)
             ))
            (catch Exception e (println "[ERROR] CSV READ ERROR at " source " exception " e)))
        (println "[Info]process finish: " (str to) )))))

(defn process-whole
  [root-path]
  (let [db-path (str root-path "/head/headers.db")
        db-spec (bd/gen-db-spec db-path)
        file-lists (map str (get-file-lists root-path start-year end-year))]
    (cond
      (not (.isDirectory (io/file root-path)))
      (println "folder is not exists. please check the file structure: " root-path)
      (not (.exists (io/as-file db-path)))
      (println "db-file is not exists. please check the file structure: " db-path)
      :default
      (doall (pmap #(parse-a-file % db-spec) file-lists))
      )))

;; ------------ example for debug -----------------------------------------
;; (let
;;     [source "/home/meguru/Documents/nico-dict/zips/rev2010/rev201012-b.csv"
;;      db-path "/home/meguru/Documents/nico-dict/zips/head/headers.db"
;;      db-spec (bd/gen-db-spec db-path)]
;;   (parse-a-file source db-spec))

;; ;; (clojure.string/replace "/home/meguru/Documents/nico-dict/zips/rev2008/rev2008.csv" #"\.csv$"
;; ;;                         "-jsoned.csv")
;; (with-open
;;   [file (io/reader (io/resource "sandbox/rev2008.csv"))]
;;   (apply +  (map #(count % ) (csv/read-csv file))))

;; (with-open
;;   [file (io/reader "/home/meguru/Documents/nico-dict/zips/rev2010/rev201012-b.csv")]
;;   (doall (map #(println (count %)) (csv/read-csv file))))
