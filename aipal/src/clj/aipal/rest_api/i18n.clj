(ns aipal.rest-api.i18n
  (:import (java.util Locale
                      ResourceBundle))
  (:require [compojure.core :as c]
            [schema.core :as schema]
            [aitu.rest-api.http-util :refer [json-response]]
            [aitu.util :refer [pisteavaimet->puu]]))

(defn validoi-kieli []
  (schema/pred (fn[k] (or (= k "fi")(= k "sv")))))

(defn hae-tekstit [kieli]
  (ResourceBundle/clearCache)
  (let [bundle (ResourceBundle/getBundle "i18n/tekstit" (Locale. kieli))]
    (->> (for [key (.keySet bundle)]
           [(keyword key) (.getString bundle key)])
         (into {})
         pisteavaimet->puu)))

(c/defroutes reitit
  (c/GET "/:kieli" [kieli :as req]
    (schema/validate (validoi-kieli) kieli)
    (json-response (hae-tekstit kieli))))
