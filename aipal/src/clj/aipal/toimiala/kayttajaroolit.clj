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

(ns aipal.toimiala.kayttajaroolit
  "https://knowledge.solita.fi/pages/viewpage.action?pageId=61901330")

(def ldap-roolit {:paakayttaja "CRUD"
                  :vastuukayttaja "CRUD2"
                  :katselija "READ"
                  :kayttaja "RU"})

;; roolit joilla on koulutustoimijaorganisaatio
(def organisaatio-roolit {:oppilaitos-vastuukayttaja "OPL-VASTUUKAYTTAJA"
                          :oppilaitos-paakayttaja "OPL-PAAKAYTTAJA"
                          :oppilaitos-kayttaja "OPL-KAYTTAJA"
                          :oppilaitos-katselija "OPL-KATSELIJA"})

(def toimikunta-roolit {:toimikuntakatselija "TTK-KATSELIJA"})

;; kayttajarooli-taulun arvot
(def kayttajaroolit (merge organisaatio-roolit
                           toimikunta-roolit
                           {:paakayttaja "YLLAPITAJA"      ; oph pääkäyttäjä
                            :oph-katselija "OPH-KATSELIJA"
                            :katselija "KATSELIJA"         ; yleinen katselija
                            }))
