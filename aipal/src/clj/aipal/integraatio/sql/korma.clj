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

(ns aipal.integraatio.sql.korma
  (:require
    [korma.core :as sql]
    [oph.korma.common :refer [defentity]]))

(declare kysymys kysymysryhma jatkokysymys kysely_kysymysryhma vastaus koulutustoimija oppilaitos toimipaikka koulutusala opintoala tutkinto tutkintotyyppi)

(defentity kyselykerta
  (sql/pk :kyselykertaid))

(defentity kysely
  (sql/pk :kyselyid)
  (sql/has-many kyselykerta {:fk :kyselyid}))

(defentity kysymysryhma
  (sql/pk :kysymysryhmaid)
  (sql/has-many kysymys {:fk :kysymysryhmaid}))

(defentity kysymysryhma_kyselypohja
  (sql/table :kysymysryhma_kyselypohja))

(defentity kyselypohja)

(defentity kysely_kysymysryhma)

(defentity kysely_kysymys)

(defentity kysymys
  (sql/pk :kysymysid)
  (sql/belongs-to kysymysryhma {:fk :kysymysryhmaid}))

(defentity jatkokysymys)

(defentity vastaajatunnus
  (sql/pk :vastaajatunnusid))

(defentity vastaaja
  (sql/pk :vastaajaid)
  (sql/belongs-to vastaajatunnus {:fk :vastaajatunnusid}))

(defentity vastaus
  (sql/pk :vastausid))

(defentity kieli
  (sql/pk :kieli))

(defentity tutkintotyyppi
  (sql/pk :tutkintotyyppi))

(defentity kayttaja
  (sql/pk :oid))

(defentity koulutusala
  (sql/pk :koulutusalatunnus)
  (sql/has-many opintoala {:fk :koulutusala}))

(defentity opintoala
  (sql/pk :opintoalatunnus)
  (sql/belongs-to koulutusala {:fk :koulutusala})
  (sql/has-many tutkinto {:fk :opintoala}))

(defentity tutkinto
  (sql/pk :tutkintotunnus)
  (sql/belongs-to opintoala {:fk :opintoala}))

(defentity rooli_organisaatio)

(defentity koulutustoimija
  (sql/pk :ytunnus)
  (sql/has-many oppilaitos {:fk :koulutustoimija}))

(defentity oppilaitos
  (sql/pk :oppilaitoskoodi)
  (sql/belongs-to koulutustoimija {:fk :koulutustoimija})
  (sql/has-many toimipaikka {:fk :oppilaitos}))

(defentity toimipaikka
  (sql/pk :toimipaikkakoodi)
  (sql/belongs-to oppilaitos {:fk :oppilaitos}))

(defentity koulutustoimija_ja_tutkinto)

(defentity kayttajarooli)

(defentity tiedote)

(defentity organisaatiopalvelu_log
  (sql/pk :id))

(defentity vipunen_view)

(defentity tutkintotyyppi
  (sql/pk :tutkintotyyppi))

(defentity monivalintavaihtoehto)

(defentity oppilaitostyyppi_tutkintotyyppi)

(defentity jatkokysymys)

(defentity jatkovastaus)

(defentity vastaajatunnus_tiedot)

(defentity kysymys_jatkokysymys)