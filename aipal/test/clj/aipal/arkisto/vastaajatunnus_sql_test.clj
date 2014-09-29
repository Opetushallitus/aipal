(ns aipal.arkisto.vastaajatunnus-sql-test
  (:require
    [clj-time.core :as time]
    [aipal.sql.test-util :refer :all]
    [aipal.sql.test-data-util :refer :all]
    [aipal.arkisto.vastaajatunnus :as vastaajatunnus-arkisto]
    )
  (:use clojure.test))

(use-fixtures :each tietokanta-fixture)

(deftest ^:integraatio tunnuksen-luonti
  (testing "Haku palauttaa lisää-kutsulla luodun vastaajatunnuksen"
    (let [tutkinto (lisaa-tutkinto!)
          rahoitusmuotoid 1 ; koodistodata
          kyselykerta (lisaa-kyselykerta!)
          vastaajatunnus (vastaajatunnus-arkisto/lisaa!
                           (:kyselykertaid kyselykerta)
                           {:rahoitusmuotoid rahoitusmuotoid
                            :tutkintotunnus (:tutkintotunnus tutkinto)
                            :voimassa_alkupvm (time/now)
                            :voimassa_loppupvm nil
                            :vastaajien_lkm 1})
          viimeksi-lisatty (first (vastaajatunnus-arkisto/hae-kaikki))]
      (is (= (:kyselykertaid viimeksi-lisatty) (:kyselykertaid vastaajatunnus)))
      (is (= (:tutkintotunnus viimeksi-lisatty) (:tutkintotunnus vastaajatunnus))))))


(deftest ^:integraatio kyselykerralla-haku
  (testing "Haku filtteröi oikein kyselykerran perusteella"
    (let [tutkinto (lisaa-tutkinto!)
          rahoitusmuotoid 1 ; koodistodata
          kyselykerta-ilman-tunnuksia (lisaa-kyselykerta!)
          kyselykerta (lisaa-kyselykerta!)
          vastaajatunnus (vastaajatunnus-arkisto/lisaa!
                           (:kyselykertaid kyselykerta)
                           {:rahoitusmuotoid rahoitusmuotoid
                            :tutkintotunnus (:tutkintotunnus tutkinto)
                            :voimassa_alkupvm (time/now)
                            :voimassa_loppupvm nil
                            :vastaajien_lkm 1})
          viimeksi-lisatyt (vastaajatunnus-arkisto/hae-kyselykerralla (:kyselykertaid kyselykerta))
          tyhja (vastaajatunnus-arkisto/hae-kyselykerralla (:kyselykertaid kyselykerta-ilman-tunnuksia))]
      (is (= (count viimeksi-lisatyt) 1))
      (is (empty? tyhja)))))
