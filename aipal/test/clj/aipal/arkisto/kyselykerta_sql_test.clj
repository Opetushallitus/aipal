(ns aipal.arkisto.kyselykerta-sql-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.arkisto.kyselykerta :refer :all]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.sql.test-data-util :as test-data]
            [aipal.sql.test-util :refer [tietokanta-fixture] :as test-util]
            [oph.common.util.util :refer [some-value
                                          some-value-with]]))

(use-fixtures :each tietokanta-fixture)

(defn lisaa-kyselykerta-johon-on-vastattu!
  ([kyselykerta]
   (lisaa-kyselykerta-johon-on-vastattu! kyselykerta (test-data/lisaa-kysely!)))
  ([kyselykerta kysely]
   (let [kyselykerta      (test-data/lisaa-kyselykerta! kyselykerta kysely)
         [vastaajatunnus] (test-data/lisaa-vastaajatunnus! {:vastaajien_lkm 1} kyselykerta)
         _                (test-data/lisaa-vastaaja! {:vastannut true} vastaajatunnus)]
     kyselykerta)))

(defn lisaa-kyselykerta-ilman-vastaajia!
  ([kyselykerta]
   (lisaa-kyselykerta-ilman-vastaajia! kyselykerta (test-data/lisaa-kysely!)))
  ([kyselykerta kysely]
   (test-data/lisaa-kyselykerta! kyselykerta kysely)))

;; Kyselykerta on poistettavissa, jos sillä ei ole yhtään vastaajaa.
(deftest ^:integraatio hae-kaikki-kyselykerta-poistettavissa
  (let [kysely       (test-data/lisaa-kysely!)
        _            (lisaa-kyselykerta-johon-on-vastattu! {} kysely)
        kyselykerta2 (lisaa-kyselykerta-ilman-vastaajia! {} kysely)]
    (is (:poistettavissa (some-value-with :kyselykertaid (:kyselykertaid kyselykerta2)
                                          (hae-kaikki (:koulutustoimija kysely)))))))

(deftest ^:integraatio kyselykerta-poistettavissa
  (let [kysely       (test-data/lisaa-kysely!)
        _            (lisaa-kyselykerta-johon-on-vastattu! {} kysely)
        kyselykerta2 (lisaa-kyselykerta-ilman-vastaajia! {} kysely)]
    (is (poistettavissa? (:kyselykertaid kyselykerta2)))
    (poista! (:kyselykertaid kyselykerta2)))
  (let [kysely (test-data/lisaa-kysely!)
        kyselykerta (test-data/lisaa-kyselykerta! {} kysely)
        vastaajatunnus (test-data/lisaa-vastaajatunnus! {} kyselykerta)]
    (is (poistettavissa? (:kyselykertaid kyselykerta)))
    (poista! (:kyselykertaid kyselykerta))))

;; Kyselykerta ei ole poistettavissa, jos sillä on yksikin vastaaja.
(deftest ^:integraatio hae-kaikki-kyselykerta-ei-poistettavissa
  (let [kysely      (test-data/lisaa-kysely!)
        kyselykerta (lisaa-kyselykerta-johon-on-vastattu! {} kysely)]
    (is (not (:poistettavissa (some-value-with :kyselykertaid (:kyselykerta kyselykerta)
                                               (hae-kaikki (:koulutustoimija kysely))))))))

(deftest ^:integraatio kyselykerta-ei-poistettavissa
  (let [kyselykerta (lisaa-kyselykerta-johon-on-vastattu! {})]
    (is (not (poistettavissa? (:kyselykertaid kyselykerta))))))

;; Poistaminen poistaa kyselykerran.
(deftest ^:integraatio poista-kyselykerta
  (let [kysely                      (test-data/lisaa-kysely!)
        [kyselykerta1 kyselykerta2] (test-data/lisaa-kyselykerrat! [{} {}] kysely)]
    (poista! (:kyselykertaid kyselykerta1))
    (let [kyselykerrat (set (map :kyselykertaid (hae-kaikki (:koulutustoimija kysely))))]
      (is (not (contains? kyselykerrat (:kyselykertaid kyselykerta1))))
      (is (contains? kyselykerrat (:kyselykertaid kyselykerta2)))) ))

;; Poistaminen poistaa kyselykertaan liittyvät vastaajatunnukset.
(deftest ^:integraatio poista-vastaajatunnukset
  (let [[kyselykerta1 kyselykerta2] (test-data/lisaa-kyselykerrat! [{} {}])
        [vastaajatunnus1] (test-data/lisaa-vastaajatunnus! {:vastaajien_lkm 1} kyselykerta1)
        [vastaajatunnus2] (test-data/lisaa-vastaajatunnus! {:vastaajien_lkm 1} kyselykerta2)]
    (poista! (:kyselykertaid kyselykerta1))
    (is (= (map :vastaajatunnusid (sql/select taulut/vastaajatunnus (sql/where {:luotu_kayttaja test-util/testikayttaja-oid})))
           [(:vastaajatunnusid vastaajatunnus2)]))))

(deftest ^:integraatio hae-kaikki-kyselykerta-jolla-monta-vastaajaa-test
  (testing
    "vastaajien määrä ei vaikuta kyselykertoihin (OPH-1254)"
    (let [kysely         (test-data/lisaa-kysely!)
          kyselykerta    (test-data/lisaa-kyselykerta! {} kysely)
          [vastaajatunnus] (test-data/lisaa-vastaajatunnus! {:vastaajien_lkm 2} kyselykerta)]
      (test-data/lisaa-vastaajat! [{} {}] vastaajatunnus)
      (is (= (count (filter #(= (:kyselykertaid %) (:kyselykertaid kyselykerta))
                            (hae-kaikki (:koulutustoimija kysely))))
             1)))))

(deftest ^:integraatio hae-kaikki-ntm-kyselykerrat-test
  (let [kysymysryhma          (test-data/lisaa-kysymysryhma! {:ntm_kysymykset true
                                                              :taustakysymykset false
                                                              :tila "julkaistu"
                                                              :valtakunnallinen true})
        kysely                (test-data/lisaa-kysely!)
        _                     (test-data/lisaa-kysymysryhma-kyselyyn! kysymysryhma kysely)
        kyselykerta           (test-data/lisaa-kyselykerta! {} kysely)
        sisaltaa-kyselykerran (fn [kyselykerrat kyselykertaid]
                                (some #{kyselykertaid}
                                      (map :kyselykertaid kyselykerrat)))]
    (testing
      "pääkäyttäjä näkee NTM-kyselykerran"
      (with-redefs [aipal.infra.kayttaja/yllapitaja? (constantly true)]
        (is (sisaltaa-kyselykerran (hae-kaikki (:koulutustoimija kysely))
                                   (:kyselykertaid kyselykerta)))))
    (testing
      "NTM-vastuukäyttäjä näkee NTM-kyselykerran"
      (with-redefs [aipal.infra.kayttaja/yllapitaja? (constantly false)
                    aipal.infra.kayttaja/ntm-vastuukayttaja? (constantly true)]
        (is (sisaltaa-kyselykerran (hae-kaikki (:koulutustoimija kysely))
                                   (:kyselykertaid kyselykerta)))))
    (testing
      "tavallinen käyttäjä ei näe NTM-kyselykertaa"
      (with-redefs [aipal.infra.kayttaja/yllapitaja? (constantly false)
                    aipal.infra.kayttaja/ntm-vastuukayttaja? (constantly false)]
        (is (not (sisaltaa-kyselykerran (hae-kaikki (:koulutustoimija kysely))
                                        (:kyselykertaid kyselykerta))))))))

(deftest ^:integraatio hae-kaikki-vastaajien-lukumaara-test
  (let [kysely (test-data/lisaa-kysely!)
        kyselykerta (test-data/lisaa-kyselykerta! {} kysely)
        [vastaajatunnus1] (test-data/lisaa-vastaajatunnus! {:vastaajien_lkm 3} kyselykerta)
        [vastaajatunnus2] (test-data/lisaa-vastaajatunnus! {:vastaajien_lkm 4} kyselykerta)]
    (test-data/lisaa-vastaajat! [{} {}] vastaajatunnus1)
    (test-data/lisaa-vastaajat! [{}] vastaajatunnus2)
    (let [tulos (first (filter #(= (:kyselykertaid %) (:kyselykertaid kyselykerta)) (hae-kaikki (:koulutustoimija kysely))))]
      (is (= 7 (:vastaajatunnuksia tulos)))
      (is (= 3 (:vastaajia tulos))))))