(ns aipal.arkisto.csv
  (:require [korma.core :as sql]
            [aipal.integraatio.sql.korma :as taulut]))

(defn hae-vastaukset [kyselyid]
  (sql/select taulut/vastaus
    (sql/join taulut/kysymys (= :vastaus.kysymysid :kysymys.kysymysid))
    (sql/join taulut/kysymysryhma (= :kysymysryhma.kysymysryhmaid :kysymys.kysymysryhmaid))
    (sql/join taulut/kysely_kysymysryhma (= :kysely_kysymysryhma.kysymysryhmaid :kysymysryhma.kysymysryhmaid))
    (sql/join taulut/vastaaja (= :vastaus.vastaajaid :vastaaja.vastaajaid))
    (sql/join taulut/vastaajatunnus (= :vastaaja.vastaajatunnusid :vastaajatunnus.vastaajatunnusid))
    (sql/join taulut/jatkovastaus (= :vastaus.jatkovastausid :jatkovastaus.jatkovastausid))
    (sql/fields :vastausid :kysymysid :vastaajaid :vapaateksti :numerovalinta :vaihtoehto :jatkovastausid :en_osaa_sanoa
                [:vastaajatunnus.tunnus :vastaajatunnus]
                [:kysymys.kysymysryhmaid :kysymysryhmaid]
                [:kysymys.vastaustyyppi :vastaustyyppi]
                [:kysely_kysymysryhma.kyselyid :kyselyid]
                [:jatkovastaus.kylla_asteikko :jatkovastaus_kylla]
                [:jatkovastaus.ei_vastausteksti :jatkovastaus_ei])
    (sql/where {:kysely_kysymysryhma.kyselyid kyselyid})))

(defn hae-kysymykset [kyselyid]
  (sql/select taulut/kysymys
    (sql/join taulut/kysymysryhma (= :kysymysryhma.kysymysryhmaid :kysymys.kysymysryhmaid))
    (sql/join taulut/kysely_kysymysryhma (= :kysely_kysymysryhma.kysymysryhmaid :kysymysryhma.kysymysryhmaid))
    (sql/join taulut/jatkokysymys (= :jatkokysymysid :jatkokysymys.jatkokysymysid))
    (sql/fields :kysymysid :jatkokysymysid :kysymys_fi :jarjestys :vastaustyyppi
                [:kysely_kysymysryhma.jarjestys :kysymysryhma_jarjestys]
                [:kysymysryhma.nimi_fi :kysymysryhma_nimi]
                [:jatkokysymys.kylla_teksti_fi :jatkokysymys_kylla]
                [:jatkokysymys.ei_teksti_fi :jatkokysymys_ei])
    (sql/where {:kysely_kysymysryhma.kyselyid kyselyid})))

(defn hae-monivalinnat [kysymysidt]
  (sql/select taulut/monivalintavaihtoehto
    (sql/where {:kysymysid [in kysymysidt]})))