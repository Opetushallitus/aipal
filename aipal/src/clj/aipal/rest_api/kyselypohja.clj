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
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [oph.common.util.http-util :refer [json-response]]))

(c/defroutes reitit
  (cu/defapi :kyselypohja-listaaminen nil :get "/" [voimassa]
    (json-response (arkisto/hae-kyselypohjat (:aktiivinen-koulutustoimija *kayttaja*) (Boolean/parseBoolean voimassa))))

  (cu/defapi :kyselypohja-luku kyselypohjaid :get "/:kyselypohjaid" [kyselypohjaid]
    (json-response (arkisto/hae-kyselypohja (Integer/parseInt kyselypohjaid))))

  (cu/defapi :kyselypohja-luku kyselypohjaid :get "/:kyselypohjaid/kysymysryhmat" [kyselypohjaid]
    (json-response (kysymysryhma-arkisto/hae-kyselypohjasta (Integer/parseInt kyselypohjaid)))))
