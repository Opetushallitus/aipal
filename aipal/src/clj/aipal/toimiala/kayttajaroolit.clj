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
  "https://confluence.csc.fi/pages/viewpage.action?pageId=53514648")

(def ldap-ryhma->rooli {"YLLAPITAJA" "YLLAPITAJA"
                        "AMKVASTUUKAYTTAJA" "OPL-VASTUUKAYTTAJA"
                        "ARVO-YO-VASTUUKAYTTAJA" "OPL-VASTUUKAYTTAJA"
                        "AMKKAYTTAJA" "OPL-KAYTTAJA"
                        "ARVO-YO-KAYTTAJA" "OPL-KAYTTAJA"
                        "AMKKATSELIJA" "OPL-KATSELIJA"
                        "ARVO-YO-KATSELIJA" "OPL-KATSELIJA"
                        "KATSELIJA" "KATSELIJA"})


;; roolit jotka liittyvät koulutustoimijaan
(def koulutustoimija-roolit #{"OPL-VASTUUKAYTTAJA" "OPL-KAYTTAJA" "OPL-KATSELIJA"})

(def oph-roolit #{"YLLAPITAJA"})

(def roolijarjestys
  "Roolien prioriteettijärjestys, jolla valitaan aktiivinen rooli jos käyttäjä ei ole sitä valinnut"
  {"YLLAPITAJA" 0
   "OPL-VASTUUKAYTTAJA" 2
   "OPL-KAYTTAJA" 3
   "OPL-KATSELIJA" 4
   "KATSELIJA" 6})
