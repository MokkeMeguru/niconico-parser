(ns niconico-parser.corpus.core
  (:require [clojure.string :refer [trim blank? split] :as str]
            [hickory.select :as s]
            [clojure.java.jdbc :as sql]
            [clojure.data.csv :as csv]
            [clojure.data.json :as json]
            [niconico-parser.utils :refer :all])
  (:use [hickory.core]
        [hickory.render]))

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


(def tmpfl (get-file-lists "/home/meguru/Documents/nico-dict/zips" 9 14))
(def tmpf (first tmpfl))

(defn read-csv
  "read csv"
  [^java.io.File file]
  (with-open [f (clojure.java.io/reader file)]
    (let [lines (csv/read-csv f)]
      (doall (map #(identity %) (take 5 lines))))))




(def tmparticle (nth (read-csv tmpf) 4))
(clojure.pprint/pprint tmparticle)

(-> tmparticle
    second
    (str/replace #"\\\n" "")
    clojure.pprint/pprint)
