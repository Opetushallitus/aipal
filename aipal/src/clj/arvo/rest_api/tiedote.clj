(ns arvo.rest-api.tiedote
  (:require [compojure.api.core :refer [defroutes DELETE GET POST]]
            [schema.core :as s]
            [arvo.db.tiedote :as arkisto]
            [oph.common.util.http-util :refer [response-or-404]]))

(defroutes reitit
  (GET "/" []
    :kayttooikeus :katselu
    (response-or-404 (or (arkisto/hae) {})))

  (POST "/" request
    :body-params [fi :- s/Str
                  sv :- s/Str
                  en :- s/Str]
    :kayttooikeus :yllapitaja
    (arkisto/poista-ja-lisaa! {:fi fi
                               :sv sv
                               :en en})
    {:status 200 :body "OK"})

  (DELETE "/" []
    :kayttooikeus :yllapitaja
    (arkisto/poista!)
    {:status 200}))
