(ns aipal.toimiala.kayttajaoikeudet
  "https://knowledge.solita.fi/pages/viewpage.action?pageId=61901330"
  (:require 
    [aipal.toimiala.kayttajaroolit :refer :all]
    [oph.korma.korma-auth :as ka]
    [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]))

(def ^:dynamic *current-user-authmap*)

(defn aipal-kayttaja? 
  ([x] (aipal-kayttaja?))
  ([]
    true))

(defn yllapitaja?
  []
  (some #(= (:rooli %) "YLLAPITAJA") (:roolit *current-user-authmap*)))

(defn kysely-muokkaus-sallittu?
  [kysely-oikeudet]
  (some #(contains? (set '("OPL-VASTUUKAYTTAJA", "OPL-KAYTTAJA")) %) (map :rooli kysely-oikeudet)))

(defn kysely-muokkaus?
  [kyselyid]
  (kysely-muokkaus-sallittu? (kayttajaoikeus-arkisto/hae-kyselylla kyselyid @ka/*current-user-oid*)))

(def kayttajatoiminnot
  `{:logitus aipal-kayttaja?
    :kieli aipal-kayttaja?
    :vastaajatunnus aipal-kayttaja?
    :kysely aipal-kayttaja?
    :kysely-muokkaus #(or (yllapitaja?) (kysely-muokkaus? (Integer/parseInt %)))
    :impersonointi yllapitaja?
    :kayttajan_tiedot aipal-kayttaja?
    :omat_tiedot aipal-kayttaja?
    })
 
(def toiminnot kayttajatoiminnot)
