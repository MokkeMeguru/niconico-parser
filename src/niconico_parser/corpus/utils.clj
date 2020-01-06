(ns niconico-parser.corpus.utils
  (:require
   [clojure.string :refer [trim blank? split] :as str]
   [niconico-parser.utils :refer :all])
  (:use [hickory.core]
        [hickory.render]))

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
      as-hickory
      trim-by-content))
