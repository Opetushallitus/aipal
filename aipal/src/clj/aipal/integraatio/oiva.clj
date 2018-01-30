(ns aipal.integraatio.oiva
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [aipal.asetukset :refer [asetukset]]
            [clojure.walk :refer [keywordize-keys]]))

(defn hae-koulutustoimijoiden-tutkinnot []
  (let [{url :url
         user :user
         password :password }(@asetukset :oiva)]
    (-> (http/get url
                  {:basic-auth [user password]})
        :body
        json/parse-string
        keywordize-keys)))
