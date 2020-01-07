(ns niconico-parser.corpus.boundary
  (:require
   [clojure.java.jdbc :as sql]))

(def article-categories {"a" "単語" "v" "動画" "i" "商品" "l" "生放送"})

(defn gen-db-spec [db-path]
  {:subprotocol "sqlite"
   :subname db-path})

(defn get-article-info [db-spec article-id]
  (-> (sql/query db-spec ["select * from article_header where article_id = ?" article-id])
      first
      (dissoc :article_id)
      (dissoc :article_date)
      ;;(update :article_category #(get article-categories %))
      ))

;; (let [db-spec (gen-db-spec "/home/meguru/Documents/nico-dict/zips/head/headers.db")
;;        article-id "1"]
;;   (get-article-info db-spec article-id))
