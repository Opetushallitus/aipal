(ns aipal.toimiala.kayttajaoikeudet
  "https://knowledge.solita.fi/pages/viewpage.action?pageId=61901330"
  (:require 
    [aipal.toimiala.kayttajaroolit :refer :all]))

(def ^:dynamic *current-user-authmap*)

(defn aipal-kayttaja? 
  ([x] (aipal-kayttaja?))
  ([]
    true))

(def kayttajatoiminnot
  `{:logitus aipal-kayttaja?
    :kieli aipal-kayttaja?
    :vastaajatunnus aipal-kayttaja?
    :kysely aipal-kayttaja?
    })

(def toiminnot kayttajatoiminnot)
