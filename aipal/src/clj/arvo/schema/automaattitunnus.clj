(ns arvo.schema.automaattitunnus
  (:require [schema.core :as s]))

(s/defschema Vastaajatunnus-metatiedot
  {(s/optional-key :tila) (s/enum "success" "failure" "bounced")})

(def Kieli (s/constrained String #(= 2 (count %))))

(s/defschema Amispalaute-tunnus
  {:vastaamisajan_alkupvm                           s/Str ;ISO formaatti
   :kyselyn_tyyppi                                  s/Str  ;kyselykerran metatieto tarkenne
   :tutkintotunnus                                  s/Str ;6 merkkiä
   :tutkinnon_suorituskieli                         Kieli
   :koulutustoimija_oid                             s/Str ;organisaatio-oid
   (s/optional-key :osaamisala)                     (s/maybe [s/Str])
   (s/optional-key :oppilaitos_oid)                 (s/maybe s/Str) ;organisaatio-oid
   (s/optional-key :toimipiste_oid)                 (s/maybe s/Str) ;organisaatio-oid
   (s/optional-key :hankintakoulutuksen_toteuttaja) (s/maybe s/Str)
   :request_id                                      s/Str
   (s/optional-key :metatiedot)                     Vastaajatunnus-metatiedot})

(s/defschema Tyoelamapalaute-tunnus
  {:koulutustoimija_oid                        s/Str
   :tyonantaja                                 s/Str
   :tyopaikka                                  s/Str
   :tutkintotunnus                             s/Str
   (s/optional-key :tutkinnon_osa)             (s/maybe s/Str)
   (s/optional-key :paikallinen_tutkinnon_osa) (s/maybe s/Str)
   :tutkintonimike                             [s/Str]
   :osaamisala                                 [s/Str]
   :tyopaikkajakson_alkupvm                    s/Str
   :tyopaikkajakson_loppupvm                   s/Str
   :sopimustyyppi                              s/Str
   :vastaamisajan_alkupvm                      s/Str
   (s/optional-key :vastaamisajan_loppupvm)    s/Str
   (s/optional-key :tyopaikkajakson_kesto)     s/Int
   (s/optional-key :osa_aikaisuus)             (s/maybe s/Int)
   (s/optional-key :oppilaitos_oid)            (s/maybe s/Str)
   (s/optional-key :toimipiste_oid)            (s/maybe s/Str)
   (s/optional-key :metatiedot)                (s/maybe {(s/optional-key :tila) s/Str})
   :request_id                                 s/Str})

(s/defschema Nippulinkki
  {:tunniste            s/Str
   :koulutustoimija_oid s/Str
   :tutkintotunnus      s/Str
   :tyonantaja          s/Str
   :tyopaikka           s/Str
   :tunnukset           [s/Str]
   :voimassa_alkupvm    s/Str
   :request_id          s/Str})


(s/defschema Automaattitunnus
  {:oppilaitos                     s/Str
   :koulutus                       s/Str
   :kunta                          s/Str
   :kieli                          s/Str
   :kyselytyyppi                   s/Str
   (s/optional-key :tarkenne)      s/Str
   (s/optional-key :koulutusmuoto) s/Int})