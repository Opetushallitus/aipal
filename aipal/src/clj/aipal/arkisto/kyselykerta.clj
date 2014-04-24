;; Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.arkisto.kyselykerta
  (:require [korma.core :as sql]))

(defn hae-kaikki
  "Hae kaikki kyselykerrat, joissa on kyllä/ei-valintakysymyksiä"
  []
  (->
    (sql/select* :kyselykerta)
    (sql/modifier "DISTINCT")
    (sql/fields :kyselykerta.kyselykertaid :kyselykerta.nimi_fi :kyselykerta.nimi_sv)
    (sql/order :kyselykerta.kyselykertaid :ASC)

    (sql/join :inner {:table :kysely}
             (= :kyselykerta.kyselyid
                :kysely.kyselyid))

    (sql/join :inner {:table :kysely_kysymysryhma}
             (= :kysely.kyselyid
                :kysely_kysymysryhma.kyselyid))

    (sql/join :inner {:table :kysymysryhma}
             (= :kysely_kysymysryhma.kysymysryhmaid
                :kysymysryhma.kysymysryhmaid))

    (sql/join :inner {:table :kysymys}
             (= :kysymysryhma.kysymysryhmaid
                :kysymys.kysymysryhmaid))
    (sql/where (= :kysymys.vastaustyyppi "kylla_ei_valinta"))

    sql/exec))
