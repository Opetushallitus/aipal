(ns aipal.toimiala.kayttajaoikeudet
  "https://knowledge.solita.fi/pages/viewpage.action?pageId=61901330"
  (:require [clojure.set :as set]
            [aipal.arkisto.kysely :as kysely-arkisto]
            [aipal.arkisto.kyselykerta :as kyselykerta-arkisto]
            [aipal.arkisto.kysymysryhma :as kysymysryhma-arkisto]
            [aipal.arkisto.kyselypohja :as kyselypohja-arkisto]
            [aipal.infra.kayttaja :as kayttaja :refer [*kayttaja*]]))

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

(defn ^:private ntm-kysely? [kyselyid]
  (kysely-arkisto/ntm-kysely? (->int kyselyid)))

(defn ^:private ntm-kysymysryhma? [kysymysryhmaid]
  (:ntm_kysymykset (kysymysryhma-arkisto/hae (->int kysymysryhmaid) false)))

(defn ^:private ntm-kyselypohja? [kyselypohjaid]
  (kyselypohja-arkisto/ntm-kyselypohja? (->int kyselypohjaid)))

(defn kayttajalla-on-jokin-rooleista-koulutustoimijassa? [roolit koulutustoimija]
  (let [aktiivinen-rooli (:aktiivinen-rooli *kayttaja*)
        rooli-koulutustoimijassa (when (= koulutustoimija (:organisaatio aktiivinen-rooli))
                                   (:rooli aktiivinen-rooli))]
    (boolean (some #{rooli-koulutustoimijassa} roolit))))

(def ^:private ntm-roolit
  #{"OPL-NTMVASTUUKAYTTAJA"})

(defn kayttajalla-on-jokin-rooleista-kyselyssa? [roolit kyselyid]
  (let [koulutustoimija (:koulutustoimija (kysely-arkisto/hae-organisaatiotieto (->int kyselyid)))]
    (kayttajalla-on-jokin-rooleista-koulutustoimijassa?
     (cond
       (ntm-kysely? kyselyid) (set/intersection roolit ntm-roolit)
       :else                  (set/difference roolit ntm-roolit))
     koulutustoimija)))

(defn kayttajalla-on-jokin-rooleista-kysymysryhmassa? [roolit kysymysryhmaid]
  (let [koulutustoimija (:koulutustoimija (kysymysryhma-arkisto/hae-organisaatiotieto (->int kysymysryhmaid)))]
    (kayttajalla-on-jokin-rooleista-koulutustoimijassa?
     (cond
       (ntm-kysymysryhma? kysymysryhmaid) (set/intersection roolit ntm-roolit)
       :else                              (set/difference roolit ntm-roolit))
     koulutustoimija)))

(defn kayttajalla-on-jokin-rooleista-kyselypohjassa? [roolit kyselypohjaid]
  (let [koulutustoimija (:koulutustoimija (kyselypohja-arkisto/hae-organisaatiotieto (->int kyselypohjaid)))]
    (kayttajalla-on-jokin-rooleista-koulutustoimijassa?
     (cond
       (ntm-kyselypohja? kyselypohjaid) (set/intersection roolit ntm-roolit)
       :else                            (set/difference roolit ntm-roolit))
     koulutustoimija)))

(defn hae-kyselyn-tila [kyselyid]
  (:tila (kysely-arkisto/hae (->int kyselyid))))

(defn hae-kyselypohjan-tila [kyselypohjaid]
  (:tila (kyselypohja-arkisto/hae-kyselypohja (->int kyselypohjaid))))

(defn kysely-on-luonnostilassa? [kyselyid]
  (= "luonnos" (hae-kyselyn-tila kyselyid)))

(defn kysely-on-julkaistu? [kyselyid]
  (= "julkaistu" (hae-kyselyn-tila kyselyid)))

(defn kyselypohja-on-luonnostilassa? [kyselypohjaid]
  (= "luonnos" (hae-kyselypohjan-tila kyselypohjaid)))

(defn kayttajalla-on-lukuoikeus-kysymysryhmaan? [kysymysryhmaid]
  (let [organisaatiotieto (kysymysryhma-arkisto/hae-organisaatiotieto (->int kysymysryhmaid))]
    (or (:valtakunnallinen organisaatiotieto)
        (= (:koulutustoimija organisaatiotieto) (:aktiivinen-koulutustoimija *kayttaja*)))))

(defn kayttajalla-on-lukuoikeus-kyselypohjaan? [kyselypohjaid]
  (let [organisaatiotieto (kyselypohja-arkisto/hae-organisaatiotieto (->int kyselypohjaid))]
    (or (:valtakunnallinen organisaatiotieto)
        (= (:koulutustoimija organisaatiotieto) (:aktiivinen-koulutustoimija *kayttaja*)))))

(defn impersonoiva-yllapitaja? []
  (not= (:oid *kayttaja*) (:aktiivinen-oid *kayttaja*)))

(defn kyselyiden-listaaminen?
  "Onko kyselyiden listaaminen sallittua yleisesti toimintona?"
  []
  (or (kayttaja/yllapitaja?)
      (kayttaja/vastuukayttaja?)
      (kayttaja/ntm-vastuukayttaja?)
      (kayttaja/kayttajalla-on-jokin-rooleista?
        #{"OPL-KAYTTAJA"
          "OPL-KATSELIJA"
          "OPH-KATSELIJA"})))

(defn kysely-luonti? []
  (or (kayttaja/yllapitaja?)
      (kayttaja/vastuukayttaja?)
      (kayttaja/ntm-vastuukayttaja?)))

(defn kysely-yleinen-muokkausoikeus?
  [kyselyid]
  (or (kayttaja/yllapitaja?)
      (kayttajalla-on-jokin-rooleista-kyselyssa? #{"OPL-VASTUUKAYTTAJA" "OPL-NTMVASTUUKAYTTAJA"} kyselyid)))

(defn kysely-muokkaus?
  "Onko kyselyn muokkaus sallittu."
  [kyselyid]
  (and (kysely-on-luonnostilassa? kyselyid)
       (kysely-yleinen-muokkausoikeus? kyselyid)))

(defn kysely-luku? [kyselyid]
  (or (kayttaja/yllapitaja?)
      (kayttajalla-on-jokin-rooleista-kyselyssa?
        #{"OPL-VASTUUKAYTTAJA"
          "OPL-KAYTTAJA"
          "OPL-KATSELIJA"
          "OPL-NTMVASTUUKAYTTAJA"
          "OPH-KATSELIJA"}
        kyselyid)))

(defn kysymysryhma-listaaminen? []
  (or (kayttaja/yllapitaja?)
      (kayttaja/vastuukayttaja?)
      (kayttaja/ntm-vastuukayttaja?)
      (kayttaja/kayttajalla-on-jokin-rooleista?
        #{"OPL-KAYTTAJA"
          "OPL-KATSELIJA"
          "OPH-KATSELIJA"})))

(defn kysymysryhma-luku? [kysymysryhmaid]
  (or (kayttaja/yllapitaja?)
      (kayttajalla-on-lukuoikeus-kysymysryhmaan? kysymysryhmaid)))

(defn kysymysryhma-luonti? []
  (or (kayttaja/yllapitaja?)
      (kayttaja/vastuukayttaja?)
      (kayttaja/ntm-vastuukayttaja?)))

(defn kysymysryhma-on-tilassa? [tila kysymysryhmaid]
  (= tila (:tila (kysymysryhma-arkisto/hae (->int kysymysryhmaid) false))))

(defn kysymysryhma-on-luonnostilassa? [kysymysryhmaid]
  (kysymysryhma-on-tilassa? "luonnos" kysymysryhmaid))

(defn kysymysryhma-on-suljettu? [kysymysryhmaid]
  (kysymysryhma-on-tilassa? "suljettu" kysymysryhmaid))

(defn kysymysryhma-on-julkaistu? [kysymysryhmaid]
  (kysymysryhma-on-tilassa? "julkaistu" kysymysryhmaid))

(defn kysymysryhma-muokkaus? [kysymysryhmaid]
  (and (kysymysryhma-on-luonnostilassa? kysymysryhmaid)
       (or (kayttaja/yllapitaja?)
           (kayttajalla-on-jokin-rooleista-kysymysryhmassa?
             #{"OPL-VASTUUKAYTTAJA" "OPL-NTMVASTUUKAYTTAJA"}
             kysymysryhmaid))))

(defn kysymysryhma-palautus-luonnokseksi? [kysymysryhmaid]
  (and (kysymysryhma-on-julkaistu? kysymysryhmaid)
       (or (kayttaja/yllapitaja?)
           (kayttajalla-on-jokin-rooleista-kysymysryhmassa?
             #{"OPL-VASTUUKAYTTAJA" "OPL-NTMVASTUUKAYTTAJA"}
             kysymysryhmaid))))

(defn kysymysryhma-julkaisu? [kysymysryhmaid]
  (and (or (kysymysryhma-on-suljettu? kysymysryhmaid)
           (kysymysryhma-on-luonnostilassa? kysymysryhmaid))
       (or (kayttaja/yllapitaja?)
           (kayttajalla-on-jokin-rooleista-kysymysryhmassa?
             #{"OPL-VASTUUKAYTTAJA" "OPL-NTMVASTUUKAYTTAJA"}
             kysymysryhmaid))))

(defn kysymysryhma-sulkeminen? [kysymysryhmaid]
  (or (kayttaja/yllapitaja?)
      (kayttajalla-on-jokin-rooleista-kysymysryhmassa?
        #{"OPL-VASTUUKAYTTAJA" "OPL-NTMVASTUUKAYTTAJA"}
        kysymysryhmaid)))

(defn kyselypohja-muokkaus? [kyselypohjaid]
  (or (kayttaja/yllapitaja?)
      (kayttajalla-on-jokin-rooleista-kyselypohjassa?
        #{"OPL-VASTUUKAYTTAJA" "OPL-NTMVASTUUKAYTTAJA"}
        kyselypohjaid)))

(defn kyselypohja-poisto? [kyselypohjaid]
  (and (kyselypohja-on-luonnostilassa? kyselypohjaid)
       (or (kayttaja/yllapitaja?)
           (kayttajalla-on-jokin-rooleista-kyselypohjassa?
             #{"OPL-VASTUUKAYTTAJA" "OPL-NTMVASTUUKAYTTAJA"}
             kyselypohjaid))))

(defn kyselypohja-luonti? []
  (or (kayttaja/yllapitaja?)
      (kayttaja/vastuukayttaja?)
      (kayttaja/ntm-vastuukayttaja?)))

(defn kyselypohja-listaaminen? []
  (or (kayttaja/yllapitaja?)
      (kayttaja/vastuukayttaja?)
      (kayttaja/ntm-vastuukayttaja?)))

(defn kyselypohja-luku? [kyselypohjaid]
  (or (kayttaja/yllapitaja?)
      (kayttajalla-on-lukuoikeus-kyselypohjaan? kyselypohjaid)))

(defn kyselykerta-luonti? [kyselyid]
  (and (kysely-on-julkaistu? kyselyid)
       (or (kayttaja/yllapitaja?)
           (kayttajalla-on-jokin-rooleista-kyselyssa?
             #{"OPL-VASTUUKAYTTAJA"
               "OPL-KAYTTAJA"
               "OPL-NTMVASTUUKAYTTAJA"}
             kyselyid))))

(defn kyselykerta-luku? [kyselykertaid]
  (let [kyselykerta (kyselykerta-arkisto/hae-yksi (->int kyselykertaid))]
    (kysely-luku? (:kyselyid kyselykerta))))

(defn kyselykerta-lukittu? [kyselykertaid]
  (:lukittu (kyselykerta-arkisto/hae-yksi (->int kyselykertaid))))

(defn kyselykerta-muokkaus? [kyselykertaid]
  (let [kyselyid (kyselykerta-arkisto/kyselykertaid->kyselyid (->int kyselykertaid))]
    (and (kysely-on-julkaistu? kyselyid)
         (kyselykerta-luonti? kyselyid)
         (not (kyselykerta-lukittu? kyselykertaid)))))

(defn kyselykerta-tilamuutos? [kyselykertaid]
  (let [kyselyid (kyselykerta-arkisto/kyselykertaid->kyselyid (->int kyselykertaid))]
    (kyselykerta-luonti? kyselyid)))

(defn kyselykerta-poisto? [kyselykertaid]
  (let [kyselyid (kyselykerta-arkisto/kyselykertaid->kyselyid (->int kyselykertaid))]
    (or (kayttaja/yllapitaja?)
        (kayttajalla-on-jokin-rooleista-kyselyssa?
          #{"OPL-VASTUUKAYTTAJA"
            "OPL-KAYTTAJA"
            "OPL-NTMVASTUUKAYTTAJA"}
          kyselyid))))

(defn raportti-koulutustoimijoista? [koulutustoimijat]
  (or (kayttaja/yllapitaja?)
      (let [oma-koulutustoimija (-> *kayttaja* :aktiivinen-rooli :organisaatio)]
        (set/subset? (set koulutustoimijat) #{oma-koulutustoimija}))))

(defn vastaajatunnus-muokkaus? [kyselykertaid]
  (let [kyselyid (kyselykerta-arkisto/kyselykertaid->kyselyid (->int kyselykertaid))]
    (and (kysely-on-julkaistu? kyselyid)
         (kyselykerta-luonti? kyselyid))))

(def kayttajatoiminnot
  `{:logitus aipal-kayttaja?
    :kieli aipal-kayttaja?
    :vastaajatunnus aipal-kayttaja?
    :vastaajatunnus-tilamuutos kyselykerta-muokkaus?
    :vastaajatunnus-poisto vastaajatunnus-muokkaus?
    :vastaajatunnus-luonti kyselykerta-muokkaus?
    :vastaajatunnus-muokkaus vastaajatunnus-muokkaus?
    :kysely kyselyiden-listaaminen?
    :kysely-luonti kysely-luonti?
    :kysely-luku kysely-luku?
    :kysely-muokkaus kysely-muokkaus?
    :kysely-tilamuutos kysely-yleinen-muokkausoikeus?
    :kysely-poisto kysely-yleinen-muokkausoikeus?
    :kysely-raportti kysely-luku?
    :kyselykerta-luku kyselykerta-luku?
    :kyselykerta-raportti kyselykerta-luku?
    :kyselykerta-luonti kyselykerta-luonti?
    :kyselykerta-muokkaus kyselykerta-muokkaus?
    :kyselykerta-tilamuutos kyselykerta-tilamuutos?
    :kyselykerta-poisto kyselykerta-poisto?
    :kysymysryhma-listaaminen kysymysryhma-listaaminen?
    :kysymysryhma-luku kysymysryhma-luku?
    :kysymysryhma-luonti kysymysryhma-luonti?
    :kysymysryhma-muokkaus kysymysryhma-muokkaus?
    :kysymysryhma-poisto kysymysryhma-muokkaus?
    :kysymysryhma-julkaisu kysymysryhma-julkaisu?
    :kysymysryhma-sulkeminen kysymysryhma-sulkeminen?
    :kysymysryhma-palautus-luonnokseksi kysymysryhma-palautus-luonnokseksi?
    :kyselypohja-listaaminen kyselypohja-listaaminen?
    :kyselypohja-luku kyselypohja-luku?
    :kyselypohja-muokkaus kyselypohja-muokkaus?
    :kyselypohja-poisto kyselypohja-poisto?
    :kyselypohja-luonti kyselypohja-luonti?
    :impersonointi kayttaja/yllapitaja?
    :impersonointi-lopetus impersonoiva-yllapitaja?
    :roolin-valinta aipal-kayttaja?
    :kayttajan_tiedot aipal-kayttaja?
    :ohjeet_luku aipal-kayttaja?
    :ohje_muokkaus kayttaja/yllapitaja?
    :tutkinto aipal-kayttaja?
    :tutkintotyyppi aipal-kayttaja?
    :oppilaitos aipal-kayttaja?
    :toimipaikka aipal-kayttaja?
    :koulutustoimija aipal-kayttaja?
    :valtakunnallinen-raportti raportti-koulutustoimijoista?
    :omat_tiedot aipal-kayttaja?
    :tiedote-luku aipal-kayttaja?
    :tiedote-muokkaus kayttaja/yllapitaja?})

(def toiminnot kayttajatoiminnot)
