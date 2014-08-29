(ns aipal.toimiala.kayttajaoikeudet
  "https://knowledge.solita.fi/pages/viewpage.action?pageId=61901330"
  (:require 
    [aipal.toimiala.kayttajaroolit :refer :all]))

(def ^:dynamic *current-user-authmap*)

(defn aipal-kayttaja? 
  ([x] (aipal-kayttaja?))
  ([]
    true))

(defn yllapitaja?
  []
  (some #(= (:rooli %) "YLLAPITAJA") (:roolit *current-user-authmap*)))
 
(def kayttajatoiminnot
  `{:logitus aipal-kayttaja?
    :kieli aipal-kayttaja?
    :vastaajatunnus aipal-kayttaja?
    :kysely aipal-kayttaja?
    :impersonointi yllapitaja?
    :kayttajan_tiedot aipal-kayttaja?
    :omat_tiedot aipal-kayttaja?
    })
 
(def toiminnot kayttajatoiminnot)
