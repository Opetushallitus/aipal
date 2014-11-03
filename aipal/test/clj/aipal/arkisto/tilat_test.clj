(ns aipal.arkisto.tilat-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
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

(deftest ^:integraatio kyselykerran-tilat-kun-kysely-poistettu-test
  (let [kysely (lisaa-kysely! {:tila "poistettu"})
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

(deftest ^:integraatio vastaajatunnus-ei-kaytettavissa-kun-kysely-poistettu-test
  (let [kysely (lisaa-kysely! {:tila "poistettu"})
        kyselykerta (lisaa-kyselykerta! {:lukittu false} kysely)
        vastaatunnus (lisaa-vastaajatunnus! {:lukittu false} kyselykerta)]
    (is (not (:kaytettavissa
               (first
                 (sql/select :vastaajatunnus
                   (sql/fields :vastaajatunnus.kaytettavissa)
                   (sql/where {:vastaajatunnusid (:vastaajatunnusid vastaatunnus)}))))))))

(deftest ^:integraatio vastaajatunnuksen-tilat-kun-kyselykerta-ei-ole-lukittu-test
  (let [kysely (lisaa-kysely! {:tila "julkaistu"})
        kyselykerta (lisaa-kyselykerta! {:lukittu false} kysely)
        vastaajatunnus (first (lisaa-vastaajatunnus! {:lukittu false} kyselykerta))
        lukittu-vastaajatunnus (first (lisaa-vastaajatunnus! {:lukittu true} kyselykerta))]
    (testing "lukittu vastaajatunnus ei ole käytettävissä"
      (is (not (:kaytettavissa (first
                         (sql/select :vastaajatunnus
                           (sql/fields :vastaajatunnus.kaytettavissa)
                           (sql/where {:vastaajatunnusid (:vastaajatunnusid lukittu-vastaajatunnus)})))))))
    (testing "lukitsematon vastaajatunnus on käytettävissä"
      (is (:kaytettavissa (first
                            (sql/select :vastaajatunnus
                              (sql/fields :vastaajatunnus.kaytettavissa)
                              (sql/where {:vastaajatunnusid (:vastaajatunnusid vastaajatunnus)}))))))))

(deftest ^:integraatio vastaajatunnuksen-tilat-kun-kyselykerta-on-lukittu-test
  (let [kysely (lisaa-kysely! {:tila "julkaistu"})
        kyselykerta (lisaa-kyselykerta! {:lukittu true} kysely)
        vastaajatunnus (first (lisaa-vastaajatunnus! {:lukittu false} kyselykerta))
        lukittu-vastaajatunnus (first (lisaa-vastaajatunnus! {:lukittu true} kyselykerta))]
    (testing "lukittu vastaajatunnus ei ole käytettävissä"
      (is (not (:kaytettavissa (first
                         (sql/select :vastaajatunnus
                           (sql/fields :vastaajatunnus.kaytettavissa)
                           (sql/where {:vastaajatunnusid (:vastaajatunnusid lukittu-vastaajatunnus)})))))))
    (testing "lukitsematon vastaajatunnus ei ole käytettävissä"
      (is (not (:kaytettavissa (first
                                (sql/select :vastaajatunnus
                                  (sql/fields :vastaajatunnus.kaytettavissa)
                                  (sql/where {:vastaajatunnusid (:vastaajatunnusid vastaajatunnus)})))))))))
