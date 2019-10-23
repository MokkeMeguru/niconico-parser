(ns niconico-parser.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [clojure.data.json :as json]
            [niconico-parser.web.core :as nw]))

(s/defschema Pizza
  {:name s/Str
   (s/optional-key :description) s/Str
   :size (s/enum :L :M :S)
   :origin {:country (s/enum :FI :PO)
            :city s/Str}})

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Niconico-parser"
                    :description "Compojure Api example"}
             :tags [{:name "api", :description "some apis"}]}}}

    (context "/api" []
      :tags ["api"]

      (GET "/get-info" []
        :return {:result s/Any}
        :query-params [url :- String]
        :summary "dic.nicovideo.jp のページを解析し、結果を返します。"
        (ok {:result (nw/read-to-parsed url)}))

      (POST "/get-and-save-info" []
        :return {:result s/Str}
        :query-params [url :- String fname :- String]
        :summary "dic.nicovideo.jp のページを解析し、結果を json ファイルにして保存します。 reources フォルダを参照してください。"
        (do
          (with-open [writer (clojure.java.io/writer (str "./resources/" fname ".json") :encoding "UTF-8")]
            (.write writer (json/write-str (nw/read-to-parsed url))))
          (ok {:result
               (str "save as ./resources/" fname)})))
      )))
