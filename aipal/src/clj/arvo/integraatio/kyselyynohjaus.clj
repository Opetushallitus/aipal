(ns arvo.integraatio.kyselyynohjaus
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.walk :refer [keywordize-keys]]
            [aipal.asetukset :refer [asetukset]]))

(defn get-opiskeluoikeus-data [oppilaitokset]
  (let [{url :url
         user :user
         password :password }(@asetukset :kyselyynohjaus)]
    (-> (http/post url {:basic-auth [user password]
                        :form-params {:oppilaitokset oppilaitokset}
                        :insecure? (:development-mode @asetukset)
                        :content-type :json})

        :body
        json/parse-string
        keywordize-keys)))
