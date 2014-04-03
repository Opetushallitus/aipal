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

(ns aipal.rest-api.raportti.kyselykerta
  (:require [compojure.core :as c]
            [schema.core :as schema]
            [aipal.rest-api.http-util :refer [json-response]]))

(c/defroutes reitit
  (c/GET "/:kyselykertaid" [kyselykertaid]
         (let [id (Integer/parseInt kyselykertaid)]
           (json-response
             {:kyselykerta {:kyselykertaid id}
              :raportti [{:kysymys_fi "kysymys 1"
                          :jakauma [{:vaihtoehto "vaihtoehto 1"
                                     :lukumaara 1}
                                    {:vaihtoehto "vaihtoehto 2"
                                     :lukumaara 2}]}
                         {:kysymys_fi "kysymys 2"
                          :jakauma [{:vaihtoehto "vaihtoehto 1"
                                     :lukumaara 2}
                                    {:vaihtoehto "vaihtoehto 2"
                                     :lukumaara 0}]}]}))))
