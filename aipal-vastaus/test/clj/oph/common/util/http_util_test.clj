;; Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
;;
;; This program is free software:  Licensed under the EUPL, Version 1.1 or - as
;; soon as they will be approved by the European Commission - subsequent versions
;; of the EUPL (the "Licence");
;;
;; You may not use this work except in compliance with the Licence.
;; You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; European Union Public Licence for more details.

(ns oph.common.util.http-util-test
  (:require [clojure.test :refer [deftest testing is are]]
            [clj-time.core :as time]
            [cheshire.core :as json]
            [oph.common.util.http-util :refer :all]))

(deftest file-download-response-test
  (testing "file-download-response"
    (testing "Palauttaa datan unicode-merkkijonona, jos koodausta ei ole määritelty"
      (is (= "åäö" (-> (file-download-response (.getBytes "åäö") "foo.txt" "text/plain")
                     :body
                     slurp))))

    (testing "Palauttaa datan määritellyssä koodauksessa"
      (is (= "åäö" (-> (file-download-response "åäö" "foo.txt" "text/plain"
                                                   {:charset "CP1252"})
                     :body
                     (slurp :encoding "CP1252")))))))

(deftest validointi-test
  (testing "validoi*"
    (testing "palauttaa funktion paluuarvon, jos map validoituu"
      (is (= (validoi {:foo 1} [[:foo pos? :virhe]] {}
               :tulos)
             :tulos)))

    (testing "ei suorita runkoa, jos map ei validoidu"
      (let [kutsuttu? (atom false)]
        (validoi {:foo 0} [[:foo pos? :virhe]] {}
           (reset! kutsuttu? true))
        (is (not @kutsuttu?))))

    (testing "palauttaa HTTP-virheen, jos map ei validoidu"
      (let [vastaus (validoi {:foo 0} [[:foo pos? :virhe]] {}
                      :tulos)]
        (is (= (:status vastaus) 400))))

    (testing "listaa virheellisten kenttien virheet JSON:na vastauksen rungossa"
      (let [vastaus (validoi {:foo 1
                              :bar -3}
                             [[:foo pos? :oltava-positiivinen]
                              [:bar pos? :oltava-positiivinen]
                              [:bar #(zero? (mod % 2)) :oltava-parillinen]]
                             {}
                      :tulos)]
        (is (= (get-in vastaus [:headers "Content-Type"]) "application/json"))
        (is (= (json/parse-string (:body vastaus))
               {"errors" {"bar" ["oltava-positiivinen" "oltava-parillinen"]}}))))

    (testing "käyttää annettuja virhetekstejä"
      (let [vastaus (validoi {:foo 0} [[:foo pos? :oltava-positiivinen]]
                             {:oltava-positiivinen "Arvon on oltava positiivinen"}
                      :tulos)]
        (is (= (-> vastaus :body json/parse-string (get-in ["errors" "foo"]))
               ["Arvon on oltava positiivinen"]))))))

(deftest response-or-404-test
  (testing "response-or-404"
    (testing "palauttaa 404-vastauksen nil-syötteellä"
      (is (= (:status (response-or-404 nil)) 404)))

    (testing "palauttaa 200-vastauksen ei-nil-syötteellä"
      (let [data {:foo "Bar"}]
        (is (= (:status (response-or-404 data)) 200))))

    (testing "no-cache toimii oikein"
       (let [data {:kung :fury}]
         (is (= (get (:headers (response-nocache data)) "Cache-control")
                "max-age=0"))))))

(deftest parse-iso-date-test
  (testing "parse-iso-date"
    (testing "parsii täydellisen ISO-päivämäärän Suomen aikavyöhykkeellä"
      (is (= (parse-iso-date "2016-12-31T23:00:00.000Z") (time/local-date 2017 1 1))))
    (testing "parsii ISO-päivämäärän ilman millisekunteja Suomen aikavyöhykkeellä"
      (is (= (parse-iso-date "2016-12-31T23:00:00Z") (time/local-date 2017 1 1))))
    (testing "parsii täydellisen ISO-päivämäärän jossa on paikallinen aikavyöhyke"
      (is (= (parse-iso-date "2016-12-31T23:00:00.000+02") (time/local-date 2016 12 31))))
    (testing "parsii ISO-päivämäärän jossa on vain päivämääräosuus"
      (is (= (parse-iso-date "2016-12-31") (time/local-date 2016 12 31))))
    (testing "parsii suomalaisen päivämäärän"
      (is (= (parse-iso-date "31.12.2016") (time/local-date 2016 12 31))))))