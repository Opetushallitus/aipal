;; Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
;;
;; This program is free software:  Licensed under the EUPL, Version 1.1 or - as
;; soon as they will be approved by the European Commission - subsequent versions
;; of the EUPL (the "Licence");
;;
;; You may not use this work except in compliance with the Licence.
;; You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; European Union Public Licence for more details.

(ns aipal.sql.test-data-util
  (:require [aipal.arkisto.vastaajatunnus]
    [aipal.arkisto.kysely]
    [aipal.arkisto.kyselykerta]
    [aipal.arkisto.koulutustoimija]
    [aipal.arkisto.vastaajatunnus]
    [clj-time.core :as ctime]
    [korma.core :as sql]
    [oph.korma.common :refer [joda-datetime->sql-timestamp]]
    [aipal.integraatio.sql.korma :as taulut]))

(def default-koulutusala
  {:koulutusalatunnus "1"
   :nimi_fi "Koulutusala"})

(def default-opintoala
  {:opintoalatunnus "123"
   :koulutusala "1"
   :nimi_fi "Opintoala"})

(def default-oppilaitos
   {:oppilaitoskoodi "11111"
    :koulutustoimija "1234567-8"
    :nimi_fi "TTY"})

(def  default-tutkinto
  {:tutkintotunnus "123456"
   :nimi_fi "Autoalan perustutkinto"
   :opintoala "123"})

(def default-koulutustoimija
  {:ytunnus "1234567-8"
   :nimi_fi "Pörsänmäen opistokeskittymä"})

(def default-kysymysryhma
  {:nimi_fi "Kysymysryhma"})

(def default-kyselypohja
  {:nimi_fi "Kyselypohja"
   :tila "julkaistu"})

(def default-kysymys
  {:pakollinen true
   :poistettava false
   :vastaustyyppi "likert_asteikko"
   :kysymys_fi "Kysymys"})

(def default-jatkokysymys
  {:kylla_teksti_fi "Kyllä teksti"})

(def default-monivalintavaihtoehto
  {:teksti_fi "Oletusvaihtoehto"
   :teksti_sv "Oletusvaihtoehto (sv)"})

(defn lisaa-koulutusala!
  ([koulutusala]
   (let [k (merge default-koulutusala koulutusala)]
     (sql/insert :koulutusala
       (sql/values k))))
  ([]
   (lisaa-koulutusala! default-koulutusala)))


(defn lisaa-opintoala!
  ([opintoala]
   (let [o (merge default-opintoala opintoala)]
     (sql/insert :opintoala
       (sql/values o))))
  ([]
   (lisaa-koulutusala!)
   (lisaa-opintoala! default-opintoala)))

(defn lisaa-tutkinto!
  ([tutkinto]
   (let [t (merge default-tutkinto tutkinto)]
     (sql/insert :tutkinto
       (sql/values t))))
  ([]
   (lisaa-opintoala!)
   (lisaa-tutkinto! default-tutkinto)))

(defn lisaa-koulutustoimija!
  ([koulutustoimija]
   (let [t (merge default-koulutustoimija koulutustoimija)]
     (sql/insert :koulutustoimija
       (sql/values t))))
  ([]
   (lisaa-koulutustoimija! default-koulutustoimija)))

(defn anna-koulutustoimija!
  "Palauttaa koulutustoimijan kannasta tai lisää uuden"
  []
  (let [k (aipal.arkisto.koulutustoimija/hae-kaikki)]
    (or (first k)
      (aipal.arkisto.koulutustoimija/lisaa! default-koulutustoimija))))

;oppilaitos needs a valid koulutustoimija
(defn lisaa-oppilaitos!
  ([oppilaitos]
   (let [t (merge default-oppilaitos oppilaitos)]
     (sql/insert :oppilaitos
       (sql/values t))))
  ([]
   (lisaa-koulutustoimija!)
   (lisaa-oppilaitos! default-oppilaitos)))

;Returns default koulutustoimija as it is the only one matching requirements
(defn anna-avop-koulutustoimija!
  "Palauttaa oletus koulutustoimijan kannasta"
  []
  (let [k (aipal.arkisto.koulutustoimija/hae (:ytunnus default-koulutustoimija))]
   (or k
     (aipal.arkisto.koulutustoimija/lisaa! default-koulutustoimija))))

(def kysely-num (atom 12))

(defn lisaa-kysely!
  ([]
   (lisaa-kysely! {}))
  ([kysely]
   (let [koulutustoimija (anna-koulutustoimija!)]
     (lisaa-kysely! kysely koulutustoimija)))
  ([kysely koulutustoimija]
   (aipal.arkisto.kysely/lisaa! (merge {:nimi_fi (str "oletuskysely, testi " (swap! kysely-num inc))
                                        :koulutustoimija (:ytunnus koulutustoimija)
                                        :tila "julkaistu"}
                                       kysely))))

;kysely avop.fi needs to make sure that koulutustoimija from oppilaitos is the same as the one for the kysely
(defn lisaa-avop-kysely!
  ([]
   (lisaa-avop-kysely! {}))
  ([kysely]
   (let [koulutustoimija (anna-avop-koulutustoimija!)]
     (lisaa-avop-kysely! kysely koulutustoimija)))
  ([kysely koulutustoimija]
   (aipal.arkisto.kysely/lisaa! (merge {:nimi_fi (str "avop oletuskysely, testi " (swap! kysely-num inc))
                                        :koulutustoimija (:ytunnus koulutustoimija)
                                        :tila "julkaistu"}
                                       kysely))))

(defn lisaa-kysymysryhma-kyselyyn!
  [kysymysryhma {:keys [kyselyid]}]
  (aipal.arkisto.kysely/lisaa-kysymysryhma! kyselyid kysymysryhma))

(defn lisaa-kyselykerta!
  ([]
   (lisaa-kyselykerta! {} (lisaa-kysely!)))
  ([kyselykerta]
   (lisaa-kyselykerta! kyselykerta (lisaa-kysely!)))
  ([kyselykerta kysely]
   (aipal.arkisto.kyselykerta/lisaa! (:kyselyid kysely) (merge {:nimi "oletuskyselykerta, testi"
                                                                :voimassa_alkupvm (joda-datetime->sql-timestamp (ctime/now))
                                                                :voimassa_loppupvm (joda-datetime->sql-timestamp (ctime/now))}
                                                               kyselykerta))))

;;AVOP.FI:sta tarvitaan kyselykerran nimi ja oppilaitos -> kyselykerran id
;problem is that oppilaitos must exist prior to avop-kyselykerta and must refer to a valid koulutustoimija
;order is koulutustoimija -> oppilaitos -> kyselykerta
(defn lisaa-avop-kyselykerta!
  ([]
   (lisaa-tutkinto!)
   (lisaa-oppilaitos!)
   (lisaa-avop-kyselykerta! {} (lisaa-avop-kysely!)))
  ([kyselykerta kysely]
   (aipal.arkisto.kyselykerta/lisaa! (:kyselyid kysely) (merge {:lukittu false
                                                                :nimi "avop oletuskyselykerta, testi"
                                                                :voimassa_alkupvm (joda-datetime->sql-timestamp (ctime/now))
                                                                :voimassa_loppupvm (joda-datetime->sql-timestamp (ctime/now))}
                                                               kyselykerta))))

(defn lisaa-kyselykerrat!
  ([kyselykerrat]
   (lisaa-kyselykerrat! kyselykerrat (lisaa-kysely!)))
  ([kyselykerrat kysely]
   (doall (for [kyselykerta kyselykerrat]
            (lisaa-kyselykerta! kyselykerta kysely)))))

(defn lisaa-vastaajatunnus!
  ([]
   (lisaa-vastaajatunnus! {} (lisaa-kyselykerta!)))
  ([vastaajatunnus]
   (lisaa-vastaajatunnus! vastaajatunnus (lisaa-kyselykerta!)))
  ([vastaajatunnus kyselykerta]
   (aipal.arkisto.vastaajatunnus/lisaa! (merge {:kyselykertaid (:kyselykertaid kyselykerta)
                                                :kohteiden_lkm 1}
                                               vastaajatunnus))))

(defn lisaa-vastaaja!
  [vastaaja vastaajatunnus]
  (sql/insert taulut/vastaaja
    (sql/values (merge {:kyselykertaid (:kyselykertaid vastaajatunnus)
                        :vastaajatunnusid (:vastaajatunnusid vastaajatunnus)}
                       vastaaja))))

(defn lisaa-vastaajat!
  ([vastaajat]
   (lisaa-vastaajat! vastaajat (lisaa-vastaajatunnus!)))
  ([vastaajat vastaajatunnus]
   (doall (for [vastaaja vastaajat]
            (lisaa-vastaaja! vastaaja vastaajatunnus)))))

(defn lisaa-kysymysryhma!
  ([uusi-kysymysryhma koulutustoimija]
   (let [default-kysymysryhma (assoc default-kysymysryhma :koulutustoimija (:ytunnus koulutustoimija))
         kysymysryhma (merge default-kysymysryhma uusi-kysymysryhma)]
     (sql/insert taulut/kysymysryhma (sql/values [kysymysryhma]))))
  ([uusi-kysymysryhma]
   (lisaa-kysymysryhma! uusi-kysymysryhma (lisaa-koulutustoimija!)))
  ([]
   (lisaa-kysymysryhma! default-kysymysryhma)))

(defn lisaa-kyselypohja!
  ([uusi-kyselypohja koulutustoimija]
   (let [default-kyselypohja (assoc default-kyselypohja :koulutustoimija (:ytunnus koulutustoimija))
         kyselypohja (merge default-kyselypohja uusi-kyselypohja)]
     (sql/insert taulut/kyselypohja (sql/values [kyselypohja]))))
  ([uusi-kyselypohja]
   (lisaa-kyselypohja! uusi-kyselypohja (lisaa-koulutustoimija!)))
  ([]
   (lisaa-kyselypohja! default-kyselypohja)))

(defn lisaa-kysymys!
  [uusi-kysymys]
  (let [kysymys (merge default-kysymys uusi-kysymys)]
    (sql/insert :kysymys (sql/values [kysymys]))))

(defn lisaa-monivalintavaihtoehto!
  [uusi-monivalintavaihtoehto]
  (let [monivalintavaihtoehto (merge default-monivalintavaihtoehto uusi-monivalintavaihtoehto)]
    (sql/insert :monivalintavaihtoehto (sql/values [monivalintavaihtoehto]))))
