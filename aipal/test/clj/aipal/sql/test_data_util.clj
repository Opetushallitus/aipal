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
    [clj-time.core :as time]
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
   :vastaustyyppi "asteikko"
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

(def kysely-num (atom 0))

(defn lisaa-kysely!
  ([]
    (lisaa-kysely! {}))
  ([kysely]
    (let [koulutustoimija (anna-koulutustoimija!)]
      (aipal.arkisto.kysely/lisaa! (merge {:nimi_fi (str "oletuskysely, testi " (swap! kysely-num inc))
                                           :koulutustoimija (:ytunnus koulutustoimija)
                                           :tila "julkaistu"}
                                          kysely)))))

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

(defn lisaa-vastaajatunnus!
  ([]
    (lisaa-vastaajatunnus! {} (lisaa-kyselykerta!)))
  ([vastaajatunnus]
    (lisaa-vastaajatunnus! vastaajatunnus (lisaa-kyselykerta!)))
  ([vastaajatunnus kyselykerta]
    (aipal.arkisto.vastaajatunnus/lisaa! (merge {:kyselykertaid (:kyselykertaid kyselykerta)
                                                 :vastaajien_lkm 1}
                                                vastaajatunnus))))

(defn lisaa-kysymysryhma!
  ([uusi-kysymysryhma koulutustoimija]
    (let [default-kysymysryhma (assoc default-kysymysryhma :koulutustoimija (:ytunnus koulutustoimija))
          kysymysryhma (merge default-kysymysryhma uusi-kysymysryhma)]
      (sql/insert taulut/kysymysryhma (sql/values [kysymysryhma]))))
  ([]
    (let [koulutustoimija (lisaa-koulutustoimija!)]
      (lisaa-kysymysryhma! default-kysymysryhma koulutustoimija))))

(defn lisaa-kyselypohja!
  ([uusi-kyselypohja koulutustoimija]
    (let [default-kyselypohja (assoc default-kyselypohja :koulutustoimija (:ytunnus koulutustoimija))
          kyselypohja (merge default-kyselypohja uusi-kyselypohja)]
      (sql/insert taulut/kyselypohja (sql/values [kyselypohja]))))
  ([]
    (let [koulutustoimija (lisaa-koulutustoimija!)]
      (lisaa-kyselypohja! default-kyselypohja koulutustoimija))))

(defn lisaa-kysymys!
  [uusi-kysymys]
  (let [kysymys (merge default-kysymys uusi-kysymys)]
    (sql/insert :kysymys (sql/values [kysymys]))))

(defn lisaa-jatkokysymys!
  [uusi-jatkokysymys]
  (let [jatkokysymys (merge default-jatkokysymys uusi-jatkokysymys)]
    (sql/insert :jatkokysymys (sql/values [jatkokysymys]))))

(defn lisaa-monivalintavaihtoehto!
  [uusi-monivalintavaihtoehto]
  (let [monivalintavaihtoehto (merge default-monivalintavaihtoehto uusi-monivalintavaihtoehto)]
    (sql/insert :monivalintavaihtoehto (sql/values [monivalintavaihtoehto]))))
