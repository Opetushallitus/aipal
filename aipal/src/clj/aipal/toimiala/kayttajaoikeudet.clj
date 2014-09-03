(ns aipal.toimiala.kayttajaoikeudet
  "https://knowledge.solita.fi/pages/viewpage.action?pageId=61901330"
  (:require
    [aipal.toimiala.kayttajaroolit :refer :all]
    [oph.korma.korma-auth :as ka]
    [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]
    [aipal.arkisto.kyselykerta :as kyselykerta-arkisto]))

(def ^:dynamic *current-user-authmap*)

(defn c->int
  "merkkijono numeroksi tai numero sellaisenaan"
  [str-or-int]
  {:post [(or (nil? %) (integer? %))]}
  (or (and (string? str-or-int) (Integer/parseInt str-or-int)) str-or-int))

(defn aipal-kayttaja?
  ([x] (aipal-kayttaja?))
  ([]
    true))

(defn yllapitaja?
  []
  (some #(= (:rooli %) "YLLAPITAJA") (:roolit *current-user-authmap*)))

(defn kyselyn-luonti?
  []
  (boolean (some #(contains? #{"YLLAPITAJA", "OPL-PAAKAYTTAJA", "OPL-VASTUUKAYTTAJA", "OPL-KAYTTAJA"} (:rooli %)) (:roolit *current-user-authmap*))))

(defn kysely-muokkaus-sallittu?
  [kysely-oikeudet]
  (boolean (some #(contains? #{"OPL-PAAKAYTTAJA", "OPL-VASTUUKAYTTAJA", "OPL-KAYTTAJA"} (:rooli %)) kysely-oikeudet)))

(defn kysely-luku-sallittu?
  [kysely-oikeudet]
  (boolean (some #(contains? #{"OPL-PAAKAYTTAJA", "OPL-VASTUUKAYTTAJA", "OPL-KAYTTAJA", "OPL-KATSELIJA"} (:rooli %)) kysely-oikeudet)))

(defn kysely-muokkaus?
  [kyselyid]
  (or (yllapitaja?)
    (kysely-muokkaus-sallittu? (kayttajaoikeus-arkisto/hae-kyselylla (c->int kyselyid) @ka/*current-user-oid*))))

(defn kysely-luku?
  [kyselyid]
  (or (yllapitaja?)
    (kysely-luku-sallittu? (kayttajaoikeus-arkisto/hae-kyselylla (c->int kyselyid) @ka/*current-user-oid*))))

(defn kyselykerta-luku?
  [kyselykertaid]
  (let [kyselykerta (kyselykerta-arkisto/hae-yksi (c->int kyselykertaid))]
    (kysely-luku? (:kyselyid kyselykerta))))

(def kayttajatoiminnot
  `{:logitus aipal-kayttaja?
    :kieli aipal-kayttaja?
    :vastaajatunnus aipal-kayttaja?
    :kysely yllapitaja?
    :kysely-luonti kyselyn-luonti?
    :kysely-luku kysely-luku?
    :kysely-muokkaus kysely-muokkaus?
    :kyselykerta-luku kyselykerta-luku?
    :kysymysryhma-luku aipal-kayttaja?
    :impersonointi yllapitaja?
    :kayttajan_tiedot aipal-kayttaja?
    :omat_tiedot aipal-kayttaja?})

(def toiminnot kayttajatoiminnot)
