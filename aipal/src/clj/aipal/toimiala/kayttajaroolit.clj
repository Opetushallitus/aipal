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

(def ^:private roolit {:paakayttaja {:ldap-rooli "OPHPAAKAYTTAJA"
                                     :aipal-rooli "YLLAPITAJA"}
                       :oph-katselija {:ldap-rooli "OPHKATSELIJA"
                                       :aipal-rooli "OPH-KATSELIJA"}
                       :oppilaitos-vastuukayttaja {:ldap-rooli "KTVASTUUKAYTTAJA"
                                                   :aipal-rooli "OPL-VASTUUKAYTTAJA"}
                       :oppilaitos-kayttaja {:ldap-rooli "KTKAYTTAJA"
                                             :aipal-rooli "OPL-KAYTTAJA"}
                       :oppilaitos-katselija {:ldap-rooli "KTKATSELIJA"
                                              :aipal-rooli "OPL-KATSELIJA"}
                       :katselija {:ldap-rooli "YLKATSELIJA"
                                   :aipal-rooli "KATSELIJA"}
                       :toimikuntakatselija {:ldap-rooli "TKTKATSELIJA"
                                             :aipal-rooli "TTK-KATSELIJA"}})

(defn roolityypin-roolit [tyyppi]
  (into {} (for [[avain tyypit] roolit]
             {avain (tyyppi tyypit)})))

(def ldap-roolit (roolityypin-roolit :ldap-rooli))

;; roolit joilla on koulutustoimijaorganisaatio
(def organisaatio-roolit (select-keys (roolityypin-roolit :aipal-rooli)
                                      [:oppilaitos-vastuukayttaja :oppilaitos-kayttaja :oppilaitos-katselija]))

(def toimikunta-roolit (select-keys (roolityypin-roolit :aipal-rooli)
                                    [:toimikuntakatselija]))

(def oph-roolit (select-keys (roolityypin-roolit :aipal-rooli)
                             [:paakayttaja :oph-katselija]))

;; kayttajarooli-taulun arvot
(def kayttajaroolit (roolityypin-roolit :aipal-rooli))
