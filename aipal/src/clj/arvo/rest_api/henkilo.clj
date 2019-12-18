(ns arvo.rest-api.henkilo
  (:require [compojure.api.core :refer [defroutes, GET]]
            [schema.core :as s]
            [aipal.integraatio.kayttooikeuspalvelu :as kayttooikeuspalvelu]))

(defroutes hae-kaikki-oidit
  (GET "/kaikki-oidit/:oid" []
       :path-params [oid :- s/Str]
       :summary "Palauttaa henkilön kaikki oidit"
       :description "Palauttaa listan oideja. Ei kerro, löytyykö henkilö oppijanumerorekisteristä."
       (kayttooikeuspalvelu/kaikki-oidit oid)))
