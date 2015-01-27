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

(ns aipal.rest-api.kyselypohja
  (:require [compojure.core :as c]
            [aipal.compojure-util :as cu]
            [korma.db :as db]
            [aipal.arkisto.kyselypohja :as arkisto]
            [aipal.arkisto.kysymysryhma :as kysymysryhma-arkisto]
            [aipal.infra.kayttaja :refer [*kayttaja* yllapitaja?]]
            [oph.common.util.util :refer [paivita-arvot]]
            [oph.common.util.http-util :refer [json-response parse-iso-date]]))

(c/defroutes reitit
  (cu/defapi :kyselypohja-listaaminen nil :get "/" [voimassa]
    (json-response (arkisto/hae-kyselypohjat (:aktiivinen-koulutustoimija *kayttaja*) (Boolean/parseBoolean voimassa))))

  (cu/defapi :kyselypohja-luku kyselypohjaid :get "/:kyselypohjaid" [kyselypohjaid]
    (let [kyselypohjaid (Integer/parseInt kyselypohjaid)
          kyselypohja (arkisto/hae-kyselypohja kyselypohjaid)
          kysymysryhmat (kysymysryhma-arkisto/hae-kyselypohjasta kyselypohjaid)]
      (when kyselypohja
        (json-response (assoc kyselypohja :kysymysryhmat kysymysryhmat)))))

  (cu/defapi :kyselypohja-muokkaus kyselypohjaid :put "/:kyselypohjaid/julkaise" [kyselypohjaid]
    (json-response (arkisto/julkaise-kyselypohja! (Integer/parseInt kyselypohjaid))))

  (cu/defapi :kyselypohja-muokkaus kyselypohjaid :put "/:kyselypohjaid/palauta" [kyselypohjaid]
    (json-response (arkisto/palauta-kyselypohja-luonnokseksi! (Integer/parseInt kyselypohjaid))))

  (cu/defapi :kyselypohja-muokkaus kyselypohjaid :put "/:kyselypohjaid/sulje" [kyselypohjaid]
    (json-response (arkisto/sulje-kyselypohja! (Integer/parseInt kyselypohjaid))))

  (cu/defapi :kyselypohja-muokkaus kyselypohjaid :put "/:kyselypohjaid" [kyselypohjaid & kyselypohja]
    (let [kyselypohja (paivita-arvot kyselypohja [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date)
          valtakunnallinen (and (yllapitaja?) (true? (:valtakunnallinen kyselypohja)))]
      (json-response (arkisto/tallenna-kyselypohja! (Integer/parseInt kyselypohjaid) (assoc kyselypohja :valtakunnallinen valtakunnallinen)))))

  (cu/defapi :kyselypohja-luonti nil :post "/" [& kyselypohja]
    (let [kyselypohja (paivita-arvot kyselypohja [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date)
          valtakunnallinen (and (yllapitaja?) (true? (:valtakunnallinen kyselypohja)))]
      (json-response
        (arkisto/luo-kyselypohja!
          (assoc kyselypohja
                 :koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*)
                 :valtakunnallinen valtakunnallinen)))))

  (cu/defapi :kyselypohja-luku kyselypohjaid :get "/:kyselypohjaid/kysymysryhmat" [kyselypohjaid]
    (json-response (kysymysryhma-arkisto/hae-kyselypohjasta (Integer/parseInt kyselypohjaid))))

  (cu/defapi :kyselypohja-poisto kyselypohjaid :delete "/:kyselypohjaid" [kyselypohjaid]
    (let [kyselypohjaid (Integer/parseInt kyselypohjaid)]
      (arkisto/poista-kyselypohja! kyselypohjaid))))
