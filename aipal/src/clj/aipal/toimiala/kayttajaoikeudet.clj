(ns aipal.toimiala.kayttajaoikeudet
  "https://knowledge.solita.fi/pages/viewpage.action?pageId=61901330"
  (:require [aipal.toimiala.kayttajaroolit :refer :all]
            [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]
            [aipal.arkisto.kysely :as kysely-arkisto]
            [aipal.arkisto.kyselykerta :as kyselykerta-arkisto]
            [aipal.arkisto.kysymysryhma :as kysymysryhma-arkisto]
            [aipal.arkisto.kyselypohja :as kyselypohja-arkisto]
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(defn ->int
  "Merkkijono numeroksi tai numero sellaisenaan."
  [str-or-int]
  {:pre [(or (integer? str-or-int)
             (string? str-or-int))]
   :post [(integer? %)]}
  (if (string? str-or-int)
    (Integer/parseInt str-or-int)
    str-or-int))

(defn aipal-kayttaja?
  ([x] (aipal-kayttaja?))
  ([]
    true))

(defn sisaltaa-jonkin-rooleista? [roolit roolirivit]
  (not (empty? (clojure.set/select roolit (set (map :rooli roolirivit))))))

(defn kayttajalla-on-jokin-rooleista? [roolit]
  (sisaltaa-jonkin-rooleista? roolit (:voimassaolevat-roolit *kayttaja*)))

(defn kayttajalla-on-jokin-rooleista-kyselyssa? [roolit kyselyid]
  (let [kyselyn-koulutustoimija (kysely-arkisto/hae-koulutustoimija (->int kyselyid))
        koulutustoimijan-roolit (filter #(= kyselyn-koulutustoimija (:organisaatio %)) (:voimassaolevat-roolit *kayttaja*))]
  (sisaltaa-jonkin-rooleista? roolit koulutustoimijan-roolit)))

(defn kayttajalla-on-lukuoikeus-kysymysryhmaan? [kysymysryhmaid]
  (let [organisaatiotieto (kysymysryhma-arkisto/hae-organisaatiotieto (->int kysymysryhmaid))]
    (or (:valtakunnallinen organisaatiotieto)
        (= (:koulutustoimija organisaatiotieto) (:aktiivinen-koulutustoimija *kayttaja*)))))

(defn kayttajalla-on-lukuoikeus-kyselypohjaan? [kyselypohjaid]
  (let [organisaatiotieto (kyselypohja-arkisto/hae-organisaatiotieto (->int kyselypohjaid))]
    (or (:valtakunnallinen organisaatiotieto)
        (= (:koulutustoimija organisaatiotieto) (:aktiivinen-koulutustoimija *kayttaja*)))))

(defn yllapitaja? []
  (kayttajalla-on-jokin-rooleista?
    #{"YLLAPITAJA"}))

(defn paakayttaja-tai-vastuukayttaja? []
  (kayttajalla-on-jokin-rooleista?
    #{"OPL-PAAKAYTTAJA"
      "OPL-VASTUUKAYTTAJA"}))

(defn impersonoiva-yllapitaja? []
  (not= (:oid *kayttaja*) (:aktiivinen-oid *kayttaja*)))

(defn kyselyiden-listaaminen?
  "Onko kyselyiden listaaminen sallittua yleisesti toimintona?"
  []
  (or (yllapitaja?)
      (paakayttaja-tai-vastuukayttaja?)
      (kayttajalla-on-jokin-rooleista?
        #{"OPL-KAYTTAJA"
          "OPL-KATSELIJA"})))

(defn kysely-luonti? []
  (or (yllapitaja?)
      (paakayttaja-tai-vastuukayttaja?)))

(defn kysely-muokkaus? [kyselyid]
  (or (yllapitaja?)
      (kayttajalla-on-jokin-rooleista-kyselyssa?
        #{"OPL-PAAKAYTTAJA"
          "OPL-VASTUUKAYTTAJA"}
        kyselyid)))

(defn kysely-luku? [kyselyid]
  (or (yllapitaja?)
      (kayttajalla-on-jokin-rooleista-kyselyssa?
        #{"OPL-PAAKAYTTAJA"
          "OPL-VASTUUKAYTTAJA"
          "OPL-KAYTTAJA"
          "OPL-KATSELIJA"}
        kyselyid)))

(defn kyselykerta-luku? [kyselykertaid]
  (let [kyselykerta (kyselykerta-arkisto/hae-yksi (->int kyselykertaid))]
    (kysely-luku? (:kyselyid kyselykerta))))

(defn kysymysryhma-listaaminen? []
  (or (yllapitaja?)
      (paakayttaja-tai-vastuukayttaja?)
      (kayttajalla-on-jokin-rooleista?
        #{"OPL-KAYTTAJA"
          "OPL-KATSELIJA"})))

(defn kysymysryhma-luku? [kysymysryhmaid]
  (or (yllapitaja?)
      (kayttajalla-on-lukuoikeus-kysymysryhmaan? kysymysryhmaid)))

(defn kysymysryhma-luonti? []
  (or (yllapitaja?)
      (paakayttaja-tai-vastuukayttaja?)))

(defn kyselypohja-listaaminen? []
  (or (yllapitaja?)
      (paakayttaja-tai-vastuukayttaja?)))

(defn kyselypohja-luku? [kyselypohjaid]
  (or (yllapitaja?)
      (kayttajalla-on-lukuoikeus-kyselypohjaan? kyselypohjaid)))

(defn kyselykerta-luonti? [kyselyid]
  (or (yllapitaja?)
      (kayttajalla-on-jokin-rooleista-kyselyssa?
        #{"OPL-PAAKAYTTAJA"
          "OPL-VASTUUKAYTTAJA"
          "OPL-KAYTTAJA"}
        kyselyid)))

(defn kyselykerta-muokkaus? [kyselykertaid]
  (let [kyselyid (kyselykerta-arkisto/kyselykertaid->kyselyid (->int kyselykertaid))]
    (kyselykerta-luonti? kyselyid)))

(def kayttajatoiminnot
  `{:logitus aipal-kayttaja?
    :kieli aipal-kayttaja?
    :vastaajatunnus aipal-kayttaja?
    :kysely kyselyiden-listaaminen?
    :kysely-luonti kysely-luonti?
    :kysely-luku kysely-luku?
    :kysely-muokkaus kysely-muokkaus?
    :kyselykerta-luku kyselykerta-luku?
    :kyselykerta-luonti kyselykerta-luonti?
    :kyselykerta-muokkaus kyselykerta-muokkaus?
    :kysymysryhma-listaaminen kysymysryhma-listaaminen?
    :kysymysryhma-luku kysymysryhma-luku?
    :kysymysryhma-luonti kysymysryhma-luonti?
    :kyselypohja-listaaminen kyselypohja-listaaminen?
    :kyselypohja-luku kyselypohja-luku?
    :impersonointi yllapitaja?
    :impersonointi-lopetus impersonoiva-yllapitaja?
    :kayttajan_tiedot aipal-kayttaja?
    :rahoitusmuoto aipal-kayttaja?
    :omat_tiedot aipal-kayttaja?})

(def toiminnot kayttajatoiminnot)
