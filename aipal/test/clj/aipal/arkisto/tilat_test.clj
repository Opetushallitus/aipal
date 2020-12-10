(ns aipal.arkisto.tilat-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [clj-time.core :as time]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.sql.test-util :refer :all]
            [aipal.sql.test-data-util :refer :all]
            [aipal.arkisto.vastaajatunnus :refer :all]))

(use-fixtures :each tietokanta-fixture)

(deftest ^:integraatio kyselykerran-tilat-kun-kysely-julkaistu-test
  (let [kysely (lisaa-kysely! {:tila "julkaistu"})
        lukittu-kyselykerta (lisaa-kyselykerta! {:lukittu true} kysely)
        kyselykerta (lisaa-kyselykerta! {:lukittu false} kysely)]
    (testing "lukittu kyselykerta ei ole käytettävissä"
      (is (not (:kaytettavissa (first
                                (sql/select :kyselykerta
                                  (sql/fields :kyselykerta.kaytettavissa)
                                  (sql/where {:kyselykertaid (:kyselykertaid lukittu-kyselykerta)})))))))
    (testing "lukitsematon kyselykerta on käytettävissä"
      (is (:kaytettavissa (first
                            (sql/select :kyselykerta
                              (sql/fields :kyselykerta.kaytettavissa)
                              (sql/where {:kyselykertaid (:kyselykertaid kyselykerta)}))))))))

(deftest ^:integraatio kyselykerran-voimassaolo-test
  (let [kysely (lisaa-kysely! {:tila "julkaistu"})
        vanhentunut-kyselykerta (lisaa-kyselykerta! {:voimassa_alkupvm (time/minus (time/today) (time/days 1))
                                                     :voimassa_loppupvm (time/minus (time/today) (time/days 1))}
                                                    kysely)
        voimaantuleva-kyselykerta (lisaa-kyselykerta! {:voimassa_alkupvm (time/plus (time/today) (time/days 1))
                                                       :voimassa_loppupvm (time/plus (time/today) (time/days 1))}
                                                      kysely)
        kyselykerta (lisaa-kyselykerta! {:voimassa_alkupvm (time/minus (time/today) (time/days 1))
                                         :voimassa_loppupvm (time/plus (time/today) (time/days 1))}
                                        kysely)]
    (are [tulos kyselykertaid] (= tulos (:kaytettavissa (first
                                                          (sql/select :kyselykerta
                                                            (sql/fields :kyselykerta.kaytettavissa)
                                                            (sql/where {:kyselykertaid kyselykertaid})))))
         false (:kyselykertaid vanhentunut-kyselykerta)
         false (:kyselykertaid voimaantuleva-kyselykerta)
         true (:kyselykertaid kyselykerta))))

(deftest ^:integraatio kyselykerran-tilat-kun-kysely-luonnostilassa-test
  (let [kysely (lisaa-kysely! {:tila "luonnos"})
        lukittu-kyselykerta (lisaa-kyselykerta! {:lukittu true} kysely)
        kyselykerta (lisaa-kyselykerta! {:lukittu false} kysely)]
    (testing "lukittu kyselykerta ei ole käytettävissä"
      (is (not (:kaytettavissa (first
                                (sql/select :kyselykerta
                                  (sql/fields :kyselykerta.kaytettavissa)
                                  (sql/where {:kyselykertaid (:kyselykertaid lukittu-kyselykerta)})))))))
    (testing "lukitsematon kyselykerta ei ole käytettävissä"
      (is (not (:kaytettavissa (first
                                 (sql/select :kyselykerta
                                   (sql/fields :kyselykerta.kaytettavissa)
                                   (sql/where {:kyselykertaid (:kyselykertaid kyselykerta)})))))))))

(deftest ^:integraatio kyselykerran-tilat-kun-kysely-suljettu-test
  (let [kysely (lisaa-kysely! {:tila "suljettu"})
        lukittu-kyselykerta (lisaa-kyselykerta! {:lukittu true} kysely)
        kyselykerta (lisaa-kyselykerta! {:lukittu false} kysely)]
    (testing "lukittu kyselykerta ei ole käytettävissä"
      (is (not (:kaytettavissa (first
                                (sql/select :kyselykerta
                                  (sql/fields :kyselykerta.kaytettavissa)
                                  (sql/where {:kyselykertaid (:kyselykertaid lukittu-kyselykerta)})))))))
    (testing "lukitsematon kyselykerta ei ole käytettävissä"
      (is (not (:kaytettavissa (first
                                 (sql/select :kyselykerta
                                   (sql/fields :kyselykerta.kaytettavissa)
                                   (sql/where {:kyselykertaid (:kyselykertaid kyselykerta)})))))))))

;(deftest ^:integraatio vastaajatunnus-ei-kaytettavissa-kun-kysely-suljettu-test
;  (let [kysely (lisaa-kysely! {:tila "suljettu"})
;        kyselykerta (lisaa-kyselykerta! {:lukittu false} kysely)
;        vastaatunnus (lisaa-vastaajatunnus! {:lukittu false} kyselykerta)]
;    (is (not (:kaytettavissa
;               (first
;                 (sql/select :vastaajatunnus
;                   (sql/fields :vastaajatunnus.kaytettavissa)
;                   (sql/where {:vastaajatunnusid (:vastaajatunnusid vastaatunnus)}))))))))

;(deftest ^:integraatio vastaajatunnuksen-tilat-kun-kyselykerta-ei-ole-lukittu-test
;  (let [kysely (lisaa-kysely! {:tila "julkaistu"})
;        kyselykerta (lisaa-kyselykerta! {:lukittu false} kysely)
;        vastaajatunnus (first (lisaa-vastaajatunnus! {:lukittu false} kyselykerta))
;        lukittu-vastaajatunnus (first (lisaa-vastaajatunnus! {:lukittu true} kyselykerta))]
;    (testing "lukittu vastaajatunnus ei ole käytettävissä"
;      (is (not (:kaytettavissa (first
;                         (sql/select :vastaajatunnus
;                           (sql/fields :vastaajatunnus.kaytettavissa)
;                           (sql/where {:vastaajatunnusid (:vastaajatunnusid lukittu-vastaajatunnus)})))))))
;    (testing "lukitsematon vastaajatunnus on käytettävissä"
;      (is (:kaytettavissa (first
;                            (sql/select :vastaajatunnus
;                              (sql/fields :vastaajatunnus.kaytettavissa)
;                              (sql/where {:vastaajatunnusid (:vastaajatunnusid vastaajatunnus)}))))))))

;(deftest ^:integraatio vastaajatunnuksen-tilat-kun-kyselykerta-on-lukittu-test
;  (let [kysely (lisaa-kysely! {:tila "julkaistu"})
;        kyselykerta (lisaa-kyselykerta! {:lukittu true} kysely)
;        vastaajatunnus (first (lisaa-vastaajatunnus! {:lukittu false} kyselykerta))
;        lukittu-vastaajatunnus (first (lisaa-vastaajatunnus! {:lukittu true} kyselykerta))]
;    (testing "lukittu vastaajatunnus ei ole käytettävissä"
;      (is (not (:kaytettavissa (first
;                         (sql/select :vastaajatunnus
;                           (sql/fields :vastaajatunnus.kaytettavissa)
;                           (sql/where {:vastaajatunnusid (:vastaajatunnusid lukittu-vastaajatunnus)})))))))
;    (testing "lukitsematon vastaajatunnus ei ole käytettävissä"
;      (is (not (:kaytettavissa (first
;                                (sql/select :vastaajatunnus
;                                  (sql/fields :vastaajatunnus.kaytettavissa)
;                                  (sql/where {:vastaajatunnusid (:vastaajatunnusid vastaajatunnus)})))))))))

(deftest ^:integraatio julkaistun-kyselyn-voimassaolo-test
  (let [kysely (lisaa-kysely! {:tila "julkaistu"
                               :voimassa_alkupvm (time/minus (time/today) (time/days 1))
                               :voimassa_loppupvm (time/plus (time/today) (time/days 1))})
        vanhentunut-kysely (lisaa-kysely! {:tila "julkaistu"
                                           :voimassa_alkupvm (time/minus (time/today) (time/days 1))
                                           :voimassa_loppupvm (time/minus (time/today) (time/days 1))})
        voimaantuleva-kysely (lisaa-kysely! {:tila "julkaistu"
                                             :voimassa_alkupvm (time/plus (time/today) (time/days 1))
                                             :voimassa_loppupvm (time/plus (time/today) (time/days 1))})]
    (are [tulos kyselyid] (= tulos (:kaytettavissa (first
                                                     (sql/select :kysely
                                                       (sql/fields :kysely.kaytettavissa)
                                                       (sql/where {:kyselyid kyselyid})))))
         false (:kyselyid vanhentunut-kysely)
         false (:kyselyid voimaantuleva-kysely)
         true (:kyselyid kysely))))

(deftest ^:integraatio kyselyn-tilat-test
  (let [luonnos-kysely (lisaa-kysely! {:tila "luonnos"})
        suljettu-kysely (lisaa-kysely! {:tila "suljettu"})]
    (are [tulos kyselyid] (= tulos (:kaytettavissa (first
                                                     (sql/select :kysely
                                                       (sql/fields :kysely.kaytettavissa)
                                                       (sql/where {:kyselyid kyselyid})))))
         false (:kyselyid luonnos-kysely)
         false (:kyselyid suljettu-kysely))))

(deftest ^:integraatio kyselypohja-kaytettavissa-test
  (let [koulutustoimija (anna-koulutustoimija!)
        luonnos-kyselypohja (lisaa-kyselypohja! {:tila "luonnos"} koulutustoimija)
        julkaistu-kyselypohja (lisaa-kyselypohja! {:tila "julkaistu"} koulutustoimija)
        suljettu-kyselypohja (lisaa-kyselypohja! {:tila "suljettu"} koulutustoimija)
        voimassaoleva-kyselypohja (lisaa-kyselypohja! {:tila "julkaistu"
                                                       :voimassa_alkupvm (time/minus (time/today) (time/days 1))
                                                       :voimassa_loppupvm (time/plus (time/today) (time/days 1))}
                                                      koulutustoimija)
        voimaantuleva-kyselypohja (lisaa-kyselypohja! {:tila "julkaistu"
                                                       :voimassa_alkupvm (time/plus (time/today) (time/days 1))
                                                       :voimassa_loppupvm (time/plus (time/today) (time/days 1))}
                                                      koulutustoimija)
        vanhentunut-kyselypohja (lisaa-kyselypohja! {:tila "julkaistu"
                                                     :voimassa_alkupvm (time/minus (time/today) (time/days 1))
                                                     :voimassa_loppupvm (time/minus (time/today) (time/days 1))}
                                                    koulutustoimija)]
    (are [tulos kyselypohjaid] (= tulos (:kaytettavissa (first
                                                          (sql/select :kyselypohja
                                                            (sql/fields :kyselypohja.kaytettavissa)
                                                            (sql/where {:kyselypohjaid kyselypohjaid})))))
         false (:kyselypohjaid luonnos-kyselypohja)
         true (:kyselypohjaid julkaistu-kyselypohja)
         false (:kyselypohjaid suljettu-kyselypohja)
         true (:kyselypohjaid voimassaoleva-kyselypohja)
         false (:kyselypohjaid voimaantuleva-kyselypohja)
         false (:kyselypohjaid vanhentunut-kyselypohja))))
