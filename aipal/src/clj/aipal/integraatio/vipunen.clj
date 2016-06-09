(ns aipal.integraatio.vipunen
  (:require [clojure.string :refer [join]]))

(def sql-alku "CREATE MATERIALIZED VIEW vipunen_view AS
")

(def sql-select "SELECT vastaus.vastausid,
vastaus.vastausaika,
vastaus.numerovalinta,
kysymysryhma.valtakunnallinen,
CASE vastaus.vaihtoehto WHEN 'kylla' THEN 1 WHEN 'ei' THEN 0 END AS vaihtoehto,
REGEXP_REPLACE(COALESCE(monivalintavaihtoehto.teksti_fi, monivalintavaihtoehto.teksti_sv), '\\n', ' ', 'g') AS monivalintavaihtoehto,
kysymys.kysymysid,
REGEXP_REPLACE(kysymys.kysymys_fi, '\\n', ' ', 'g') AS kysymys_fi,
REGEXP_REPLACE(kysymys.kysymys_sv, '\\n', ' ', 'g') AS kysymys_sv,
kysymys.kysymysryhmaid,
REGEXP_REPLACE(kysymysryhma.nimi_fi, '\\n', ' ', 'g') AS kysymysryhma_fi,
REGEXP_REPLACE(kysymysryhma.nimi_sv, '\\n', ' ', 'g') AS kysymysryhma_sv,
kysymys.vastaustyyppi,
vastaaja.vastaajaid,
rahoitusmuoto.rahoitusmuoto,
tutkinto.tutkintotunnus,
REGEXP_REPLACE(tutkinto.nimi_fi, '\\n', ' ', 'g') AS tutkinto_fi,
REGEXP_REPLACE(tutkinto.nimi_sv, '\\n', ' ', 'g') AS tutkinto_sv,
opintoala.opintoalatunnus,
REGEXP_REPLACE(opintoala.nimi_fi, '\\n', ' ', 'g') AS opintoala_fi,
REGEXP_REPLACE(opintoala.nimi_sv, '\\n', ' ', 'g') AS opintoala_sv,
vastaajatunnus.suorituskieli,
vastaajatunnus.valmistavan_koulutuksen_jarjestaja,
REGEXP_REPLACE(valmistava_koulutustoimija.nimi_fi, '\\n', ' ', 'g') AS valmistavan_koulutuksen_jarjestaja_fi,
REGEXP_REPLACE(valmistava_koulutustoimija.nimi_sv, '\\n', ' ', 'g') AS valmistavan_koulutuksen_jarjestaja_sv,
vastaajatunnus.valmistavan_koulutuksen_oppilaitos,
REGEXP_REPLACE(valmistava_oppilaitos.nimi_fi, '\\n', ' ', 'g') AS valmistavan_koulutuksen_oppilaitos_fi,
REGEXP_REPLACE(valmistava_oppilaitos.nimi_sv, '\\n', ' ', 'g') AS valmistavan_koulutuksen_oppilaitos_sv,
kyselykerta.kyselykertaid,
REGEXP_REPLACE(kyselykerta.nimi, '\\n', ' ', 'g') AS kyselykerta,
kysely.kyselyid,
REGEXP_REPLACE(kysely.nimi_fi, '\\n', ' ', 'g') AS kysely_fi,
REGEXP_REPLACE(kysely.nimi_fi, '\\n', ' ', 'g') AS kysely_sv,
kysely.koulutustoimija,
REGEXP_REPLACE(koulutustoimija.nimi_fi, '\\n', ' ', 'g') AS koulutustoimija_fi,
REGEXP_REPLACE(koulutustoimija.nimi_sv, '\\n', ' ', 'g') AS koulutustoimija_sv,
")

(def sql-from "FROM vastaus
INNER JOIN kysymys ON vastaus.kysymysid = kysymys.kysymysid
INNER JOIN kysymysryhma ON kysymys.kysymysryhmaid = kysymysryhma.kysymysryhmaid
LEFT JOIN monivalintavaihtoehto ON kysymys.vastaustyyppi = 'monivalinta' AND monivalintavaihtoehto.kysymysid = kysymys.kysymysid AND vastaus.numerovalinta = monivalintavaihtoehto.jarjestys
INNER JOIN vastaaja ON vastaus.vastaajaid = vastaaja.vastaajaid
INNER JOIN vastaajatunnus ON vastaaja.vastaajatunnusid = vastaajatunnus.vastaajatunnusid
LEFT JOIN tutkinto ON vastaajatunnus.tutkintotunnus = tutkinto.tutkintotunnus
LEFT JOIN opintoala ON tutkinto.opintoala = opintoala.opintoalatunnus
LEFT JOIN koulutustoimija AS valmistava_koulutustoimija ON vastaajatunnus.valmistavan_koulutuksen_jarjestaja = valmistava_koulutustoimija.ytunnus
LEFT JOIN oppilaitos AS valmistava_oppilaitos ON vastaajatunnus.valmistavan_koulutuksen_oppilaitos = valmistava_oppilaitos.oppilaitoskoodi
INNER JOIN rahoitusmuoto ON vastaajatunnus.rahoitusmuotoid = rahoitusmuoto.rahoitusmuotoid
INNER JOIN kyselykerta ON vastaajatunnus.kyselykertaid = kyselykerta.kyselykertaid
INNER JOIN kysely ON kyselykerta.kyselyid = kysely.kyselyid
LEFT JOIN koulutustoimija ON kysely.koulutustoimija = koulutustoimija.ytunnus
")

(def sql-where "WHERE kysymys.vastaustyyppi <> 'vapaateksti' AND
")

(def taustakysymykset
  [{:id [7312027 7312034]
    :nimi "sukupuoli"}
   {:id [7312028 7312035]
    :nimi "aidinkieli"}
   {:id [7312029 7312036]
    :nimi "ika"}
   {:id [7312030 7312037]
    :nimi "tutkinto"}
   {:id [7312031 7312038]
    :nimi "syy"}
   {:id 7312032
    :nimi "tuleva_tilanne"}
   {:id 7312039
    :nimi "aiempi_tilanne"}
   {:id [7312033 7312040]
    :nimi "tavoite"}])

(def tks
  (for [{:keys [nimi id]} taustakysymykset]
    {:nimi nimi
     :id (if (vector? id)
           id
           [id])}))

(defn vastaus-taulu [id]
  (str "vastaus" id))

(defn kysymys-taulu [id]
  (str "kysymys" id))

(defn monivalinta-taulu [id]
  (str "monivalinta" id))

(defn kentta [tk]
  (str "taustakysymys_" (:nimi tk)))

(def taustakysymykset-select
  (join ",\n" (for [kysymys tks]
                (str "REGEXP_REPLACE(COALESCE("
                     (join "," (for [id (:id kysymys)]
                                 (str (monivalinta-taulu id) ".teksti_fi")))
                     "), '\\n', ' ', 'g') AS " (kentta kysymys)))))

(def taustakysymykset-from
  (join \newline (for [kysymys tks]
                   (join \newline (for [id (:id kysymys)
                                        :let [vt (vastaus-taulu id)
                                              mt (monivalinta-taulu id)]]
                                    (str "LEFT JOIN vastaus AS " vt " ON " vt ".vastaajaid = vastaaja.vastaajaid AND " vt ".kysymysid = " id \newline
                                         "LEFT JOIN monivalintavaihtoehto AS " mt " ON " mt ".kysymysid = " vt ".kysymysid AND " vt ".numerovalinta = " mt ".jarjestys"))))))

(def taustakysymykset-where
  (str "("
       (join " OR\n" (for [kysymys tks
                           id (:id kysymys)]
                       (str (monivalinta-taulu id) ".teksti_fi is not null")))
       ")\nAND vastaus.kysymysid NOT IN ("
       (join "," (for [kysymys tks
                       id (:id kysymys)]
                   id))
       ")"))

(def generoitu-sql
  (str sql-alku
       sql-select
       taustakysymykset-select
       \newline
       sql-from
       taustakysymykset-from
       \newline
       sql-where
       taustakysymykset-where
       \;))