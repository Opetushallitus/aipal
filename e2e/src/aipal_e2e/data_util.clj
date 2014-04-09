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

(ns aipal-e2e.data-util
  (:require [clj-time.core :as time]
            [aipal-e2e.arkisto.kysely :as kysely]
            [aipal-e2e.arkisto.kysely-kysymys :as kysely-kysymys]
            [aipal-e2e.arkisto.kysely-kysymysryhma :as kysely-kysymysryhma]
            [aipal-e2e.arkisto.kyselykerta :as kyselykerta]
            [aipal-e2e.arkisto.kyselypohja :as kyselypohja]
            [aipal-e2e.arkisto.kysymys :as kysymys]
            [aipal-e2e.arkisto.kysymysryhma :as kysymysryhma]
            [aipal-e2e.arkisto.vastaustunnus :as vastaustunnus]
            [aipal-e2e.arkisto.vastaus :as vastaus]
            [aipal-e2e.tietokanta.data]))

(def ^:private kysely-tiedot {:luo-fn kysely/lisaa!
                              :poista-fn #(kysely/poista! (:kyselyid %))
                              :default (for [i (iterate inc 1)]
                                         {:kyselyid i})})

(def ^:private kysely-kysymys-tiedot {:luo-fn kysely-kysymys/lisaa!
                                      :poista-fn #(kysely-kysymys/poista! (:kyselyid %) (:kysymysid %))
                                      :default (for [i (iterate inc 1)]
                                                 {})})

(def ^:private kysely-kysymysryhma-tiedot {:luo-fn kysely-kysymysryhma/lisaa!
                                           :poista-fn #(kysely-kysymysryhma/poista! (:kyselyid %) (:kysymysryhmaid %))
                                           :default (for [i (iterate inc 1)]
                                                      {})})

(def ^:private kyselykerta-tiedot {:luo-fn kyselykerta/lisaa!
                                   :poista-fn #(kyselykerta/poista! (:kyselykertaid %))
                                   :default (for [i (iterate inc 1)]
                                              {:nimi_fi (str "Kyselykerta " i)
                                               :voimassa_alkaen (time/today)})})

(def ^:private kyselypohja-tiedot {:luo-fn kyselypohja/lisaa!
                                   :poista-fn #(kyselypohja/poista! (:kyselypohjaid %))
                                   :default (for [i (iterate inc 1)]
                                              {:valtakunnallinen true})})

(def ^:private kysymys-tiedot {:luo-fn kysymys/lisaa!
                               :poista-fn #(kysymys/poista! (:kysymysid %))
                               :default (for [i (iterate inc 1)]
                                          {:pakollinen false
                                           :poistettava true
                                           :vastaustyyppi "vastaustyyppi"
                                           :kysymys_fi (str "Kysymys " i)})})

(def ^:private kysymysryhma-tiedot {:luo-fn kysymysryhma/lisaa!
                                    :poista-fn #(kysymysryhma/poista! (:kysymysryhmaid %))
                                    :default (for [i (iterate inc 1)]
                                               {:nimi_fi (str "Kysymysryhma " i)})})

(def ^:private vastaustunnus-tiedot {:luo-fn vastaustunnus/lisaa!
                                     :poista-fn #(vastaustunnus/poista! (:vastaustunnusid %))
                                     :default (for [i (iterate inc 1)]
                                                {})})

(def ^:private vastaus-tiedot {:luo-fn vastaus/lisaa!
                                     :poista-fn #(vastaus/poista! (:vastausid %))
                                     :default (for [i (iterate inc 1)]
                                                {})})

(def ^:private entity-tiedot {:kysely kysely-tiedot
                              :kyselykerta kyselykerta-tiedot
                              :kysymysryhma kysymysryhma-tiedot
                              :kysymys kysymys-tiedot
                              :kyselypohja kyselypohja-tiedot
                              :kysely-kysymys kysely-kysymys-tiedot
                              :kysely-kysymysryhma kysely-kysymysryhma-tiedot
                              :vastaustunnus vastaustunnus-tiedot
                              :vastaus vastaus-tiedot})

(def ^:private taulut [:kysely
                       :kyselykerta
                       :kysymysryhma
                       :kysymys
                       :kyselypohja
                       :kysely-kysymysryhma
                       :kysely-kysymys
                       :vastaustunnus
                       :vastaus])

(defn ^:private luo
  [entityt luo-fn]
  (doseq [entity entityt]
    (luo-fn entity)))

(defn ^:private poista
  [entityt poista-fn]
  (doseq [entity entityt]
    (poista-fn entity)))

(defn ^:private taydenna-data
  [data]
  (into {}
        (for [[taulu entityt] data
              :let [default (get-in entity-tiedot [taulu :default])]]
          {taulu (map merge default entityt)})))

(defn with-data*
  [data body-fn]
  (let [taydennetty-data (taydenna-data data)]
    (doseq [taulu taulut
            :let [data (taydennetty-data taulu)
                  luo-fn (get-in entity-tiedot [taulu :luo-fn])]
            :when data]
      (luo data luo-fn))
    (try
      (body-fn)
      (finally
        (doseq [taulu (reverse taulut)
                  :let [data (taydennetty-data taulu)
                        poista-fn (get-in entity-tiedot [taulu :poista-fn])]
                  :when data]
            (poista data poista-fn))))))

(defmacro with-data
  [data & body]
  `(with-data* ~data (fn [] ~@body)))
