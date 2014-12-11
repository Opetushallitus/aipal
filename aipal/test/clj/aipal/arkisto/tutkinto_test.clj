(ns aipal.arkisto.tutkinto-test
  (:require
    [clojure.test :refer :all]
    [aipal.arkisto.tutkinto :as arkisto]))

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
