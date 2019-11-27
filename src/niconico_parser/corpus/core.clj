(ns niconico-parser.corpus.core
  (:require [clojure.string :refer [trim blank? split] :as str]
            [hickory.select :as s]
            [clojure.java.jdbc :as sql]
            [clojure.data.csv :as csv]
            [clojure.data.json :as json]
            [niconico-parser.utils :refer :all]
            [clojure.zip :as zip]
            [taoensso.nippy :as nippy]
            [clojure.tools.cli :refer [parse-opts]])
  (:use [hickory.core]
        [hickory.render])
  (:gen-class))

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
   (filter #(str/ends-with? % ".csv"))))

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

(defn parse-csv
  "parse csv for niconico daihyakka's article
  csv format is
  <integer> <string> <integer>
  article-id article updated-date
  "
  [raw-csv]
  (pmap (fn [[idx raw-html timestamp]]
          [(Long. (re-find #"[0-9]*" idx)) (html->hickory raw-html) (Long. (re-find #"[0-9]*" timestamp))])
        raw-csv))

(defn preprocess-csv
  "read csv"
  [^java.io.File file fun]
  (with-open [f (clojure.java.io/reader file)]
    (let [lines (csv/read-csv f)]
      (fun lines))))

(defn save-serialized-data
  "serialize as nippy"
  [data write-file]
  (nippy/freeze-to-file
   write-file
   data))

(defn print-log [_] (println "readed a csv ..."))
(defn preprocess-all
  "preprocess all csv file
  first 
"
  [files]
  (map
   #(preprocess-csv
     %
     (fn [lines]
       (println "start reading ...")
       (-> lines
           parse-csv
           (save-serialized-data (str (subs (str %) 0 (- (count (str %)) 4)) "-raw.npy"))
           print-log)))
   files))

(def cli-options
  [["-s" "--source SOURCE" "Source Directory"
    :default "/home/meguru/Documents/nico-dict/zips"
    :parse-fn str]
   ["-n" "--npy-path NPY-PATH" "NPY PATH"
    :default "./resources/rev201402-raw.npy"
    :parse-fn str]
   ["-h" "--help"]])

(defn -main
  [& args]
  (let [argdic (parse-opts args cli-options)
        zips-folder (-> argdic :options :source)
        argument (-> argdic :arguments first)]
    (case argument
      "preprocess-raw-data"
      (doall (preprocess-all (get-file-lists zips-folder 8 14)))
      :default (println "see. README"))))

;; (defn main []
;;   (let [dir "/home/meguru/Documents/nico-dict/zips"
;;         files (get-file-lists dir 14 14)]
;;     (preprocess-all files)))

;; (main)



;; 8 - 14
;; (def tmpfl (get-file-lists "/home/meguru/Documents/nico-dict/zips" 10 10))
;; (count tmpfl)
;; (def tmpf (nth tmpfl 1))
;; (str tmpf)


;; (preprocess-csv tmpf
;;                 (fn [lines] (-> lines
;;                                 parse-csv
;;                                 (save-serialized-data (str (subs (str tmpf) 0 (- (count (str tmpf)) 4)) ".npy"))
;;                                 #(println "read a csv..." (str tmpf)))))

;; (str (subs (str tmpf) 0 (- (count (str tmpf)) 4)) ".npy")
;; (first (nippy/thaw-from-file (str (subs (str tmpf) 0 (- (count (str tmpf)) 4)) ".npy")))
;; (def tmparticle (nth (read-csv tmpf) 1))
;; (clojure.pprint/pprint tmparticle)

;; (-> tmparticle
;;     second
;;     (str/replace #"\\\n" "")
;;     (str/replace #"\u00A0" " ")
;;     (str/replace #"&nbsp;" " ")
;;     (str/replace #"(?<!(src|href))=\"(.*?)\""  "=\"\"")
;;     (str/replace #"[a-zA-Z]+=\"\"" "")
;;     (str/replace #"[\s|　]+" " ")
;;     (str/replace #"<br[^<>]*>" "")
;;     parse
;;     as-hickory
;;     clojure.pprint/pprint)
