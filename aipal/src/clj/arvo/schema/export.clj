(ns arvo.schema.export
  (:require [schema.core :as s]))

(s/defschema Kyselykerta
  {:kyselyid s/Int
   :koulutustoimija s/Str
   :tyyppi s/Str
   :kysely_fi (s/maybe s/Str)
   :kysely_sv (s/maybe s/Str)
   :kysely_en (s/maybe s/Str)
   :kysely_voimassa_alkupvm s/Any
   :kysely_voimassa_loppupvm (s/maybe s/Any)
   :kysely_tila s/Str
   :kyselykertaid s/Int
   :kyselykerta s/Str
   :kyselykerta_vuosi (s/maybe s/Str)
   :kyselypohjaid (s/maybe s/Int)
   :kyselypohja_nimi (s/maybe s/Str)
   :kyselypohja_tarkenne (s/maybe s/Str)
   s/Any s/Any})

(s/defschema Vastaus
  {:vastausid s/Int
   :vastaajatunnusid s/Int
   :vastaajaid s/Int
   :kysymysid s/Int
   :vastausaika s/Any
   :numerovalinta (s/maybe s/Int)
   :vapaateksti (s/maybe s/Str)
   :vaihtoehto (s/maybe s/Str)
   :koulutustoimija s/Str
   :kyselyid s/Int
   :kyselykertaid s/Int
   :monivalintavaihtoehto_fi (s/maybe s/Any)
   :monivalintavaihtoehto_sv (s/maybe s/Str)
   :monivalintavaihtoehto_en (s/maybe s/Str)
   s/Any s/Any})

(s/defschema Kysymyskategoria
  {(s/optional-key :rahoitusmallikysymys) (s/maybe s/Bool)
   (s/optional-key :taustakysymyksen_tyyppi) (s/maybe s/Str)})

(s/defschema Kysymys
  {:kysymysid s/Int
   :kysymysryhmaid s/Int
   :vastaustyyppi s/Str
   :kysymys_fi (s/maybe s/Str)
   :kysymys_sv (s/maybe s/Str)
   :kysymys_en (s/maybe s/Str)
   :kategoria (s/maybe Kysymyskategoria)
   :jatkokysymys s/Bool
   :jatkokysymys_kysymysid (s/maybe s/Int)
   :kysymysryhma_fi (s/maybe s/Str)
   :kysymysryhma_sv (s/maybe s/Str)
   :kysymysryhma_en (s/maybe s/Str)
   :valtakunnallinen s/Bool
   s/Any s/Any})

(s/defschema Taustatiedot
  {(s/optional-key :toimipaikka) (s/maybe s/Str)
   (s/optional-key :tutkinto) (s/maybe s/Str)
   (s/optional-key :kieli) (s/maybe s/Str)
   (s/optional-key :kunta) (s/maybe s/Str)
   (s/optional-key :koulutusmuoto) (s/maybe s/Str)
   (s/optional-key :hankintakoulutuksen_toteuttaja) (s/maybe s/Str)
   (s/optional-key :henkilonumero) (s/maybe s/Str)
   (s/optional-key :haun_numero) (s/maybe s/Str)
   s/Any s/Any})

(s/defschema Vastaajatunnus
  {:vastaajaid       s/Int
   :vastaajatunnusid s/Int
   :vastaajatunnus   s/Str
   :oppilaitos       (s/maybe s/Str)
   :taustatiedot     (s/maybe Taustatiedot)
   :vastaajatunnus_alkupvm (s/maybe s/Str)
   :vastaajatunnus_loppupvm (s/maybe s/Str)
   s/Any s/Any})

(s/defschema Kysely-kysymysryhma
  {:kyselyid s/Int
   :kysymysryhmaid s/Int
   :jarjestys s/Int})

(s/defschema Opiskeluoikeus
  {:vastaajatunnus s/Str
   :opiskeluoikeus s/Str})




