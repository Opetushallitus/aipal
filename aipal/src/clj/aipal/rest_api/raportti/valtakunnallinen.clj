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

(ns aipal.rest-api.raportti.valtakunnallinen
  (:require [compojure.core :as c]
            [aipal.compojure-util :as cu]
            [korma.db :as db]
            [oph.common.util.http-util :refer [json-response]]
            [aipal.toimiala.raportti.valtakunnallinen :as raportti]))

(defn reitit [asetukset]
  (cu/defapi :valtakunnallinen-raportti nil :post "/" [& parametrit]
    (db/transaction
      (let [raportti (raportti/muodosta parametrit)
            vaaditut-vastaajat (:raportointi-minimivastaajat asetukset)]
        (json-response
          (if (>= (:vastaajien-lkm raportti) vaaditut-vastaajat)
            raportti
            (assoc (dissoc raportti :raportti) :virhe "ei-riittavasti-vastaajia")))))))
