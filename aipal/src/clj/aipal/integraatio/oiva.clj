(ns aipal.integraatio.oiva
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.walk :refer [keywordize-keys]]))

(defn hae-koulutustoimijoiden-tutkinnot []
  (-> (http/get "https://oivadev.csc.fi/api/export/koulutusluvat")
      :body
      json/parse-string
      keywordize-keys))
