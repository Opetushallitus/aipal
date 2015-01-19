(ns aipal.arkisto.tutkinto-test
  (:require
    [clojure.test :refer :all]
    [aipal.arkisto.tutkinto :as arkisto]
    [clj-time.core :as time]))

(deftest tutkinnot-hierarkiaksi-test
  (testing "tutkintojen hierarkiaksi muunto"
    (let [tutkinnot [{:nimi_sv "Ingenjör, elkraftsteknik",
                      :opintoala_nimi_fi "Sähkö- ja automaatiotekniikka",
                      :nimi_fi "Insinööri, sähkövoimatekniikka",
                      :opintoala_nimi_sv "El- och automationsteknik",
                      :koulutusala_nimi_sv "Teknik och kommunikation",
                      :opintoalatunnus "503",
                      :koulutusala_nimi_fi "Tekniikan ja liikenteen ala",
                      :tutkintotunnus "064151",
                      :koulutusalatunnus "5"}]
          koulutusalat (arkisto/tutkinnot-hierarkiaksi tutkinnot)
          koulutusala1 (first koulutusalat)
          opintoalat (:opintoalat koulutusala1)
          opintoala1 (first opintoalat)
          tutkinto1 (first (:tutkinnot opintoala1))]
      (is (= (count koulutusalat) 1))
      (is (= (count opintoalat) 1))
      (is (= (:opintoalatunnus opintoala1) "503"))
      (is (= tutkinto1 {:nimi_sv "Ingenjör, elkraftsteknik", :nimi_fi "Insinööri, sähkövoimatekniikka", :tutkintotunnus "064151"})))))

(deftest tutkinto-voimassa-test
  (let [menneisyydessa (time/minus (time/today) (time/days 1))
        tanaan (time/today)
        tulevaisuudessa (time/plus (time/today) (time/days 1))]
    (testing "Tutkinto on voimassa jos alkupäivää ja loppupäivää ei ole asetettu"
      (is (arkisto/tutkinto-voimassa? {})))
    (testing "Tutkinto on voimassa jos alkupäivä on menneisyydessä"
      (is (arkisto/tutkinto-voimassa? {:voimassa_alkupvm menneisyydessa})))
    (testing "Tutkinto on voimassa jos loppupäivä on tulevaisuudessa"
      (is (arkisto/tutkinto-voimassa? {:voimassa_loppupvm tulevaisuudessa})))
    (testing "Tutkinto on voimassa alkupäivänä ja loppupäivänä"
      (is (arkisto/tutkinto-voimassa? {:voimassa_alkupvm tanaan
                                       :voimassa_loppupvm tanaan})))
    (testing "Tutkinto on voimassa siirtymäajalla"
      (is (arkisto/tutkinto-voimassa? {:voimassa_loppupvm menneisyydessa
                                       :siirtymaajan_loppupvm tulevaisuudessa})))
    (testing "Tutkinto ei ole voimassa jos alkupäivä on tulevaisuudessa"
      (is (not (arkisto/tutkinto-voimassa? {:voimassa_alkupvm tulevaisuudessa}))))
    (testing "Tutkinto ei ole voimassa jos loppupäivä on menneisyydessä"
      (is (not (arkisto/tutkinto-voimassa? {:voimassa_loppupvm menneisyydessa}))))
    (testing "Tutkinto ei ole voimassa jos siirtymäajan loppupäivä on menneisyydessä"
      (is (not (arkisto/tutkinto-voimassa? {:voimassa_loppupvm menneisyydessa
                                            :siirtymaajan_loppupvm menneisyydessa}))))))
