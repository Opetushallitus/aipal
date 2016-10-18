(ns aipal.basic-auth-test
  (:require [clojure.test :refer :all]
            [aipal.basic-auth :refer :all]))

(deftest hae-tunnus-test
  (testing "hae-tunnus"
    (testing "purkaa Basic-autorisaatioheaderista tunnuksen ja salasanan"
      (is (= ["tunnus" "salasana"] (hae-tunnus {:headers {"authorization" "Basic dHVubnVzOnNhbGFzYW5h"}}))))
    (testing "palauttaa nil jos headeria ei ole"
      (is (nil? (hae-tunnus {}))))
    (testing "palauttaa nil jos header on epämuodostunut"
      (is (nil? (hae-tunnus {:headers {"authorization" "dHVubnVzOnNhbGFzYW5h"}})))
      (is (nil? (hae-tunnus {:headers {"authorization" "Basic dHVubnVzOnNhbGFzY"}})))
      (is (nil? (hae-tunnus {:headers {"authorization" "Basic "}}))))))

(deftest wrap-basic-authentication-test
  (let [asetukset {:basic-auth {:tunnus "tunnus"
                                :salasana "salasana"}}
        handler (->
                  (constantly ::success)
                  (wrap-basic-authentication asetukset))]
    (testing "wrap-basic-authentication"
      (testing "palauttaa 401-vastauksen jos autentikointi epäonnistuu"
        (let [response (handler {})]
          (is (= 401 (:status response)))
          (is (.startsWith (get-in response [:headers "www-authenticate"] "") "Basic realm=")))
        (let [response (handler {:headers {"authorization" "Basic dHVubnVzOnbDpMOkcsOk"}})]
          (is (= 401 (:status response)))
          (is (.startsWith (get-in response [:headers "www-authenticate"] "") "Basic realm="))))
      (testing "päästää validin pyynnön läpi"
        (is (= ::success (handler {:headers {"authorization" "Basic dHVubnVzOnNhbGFzYW5h"}})))))))