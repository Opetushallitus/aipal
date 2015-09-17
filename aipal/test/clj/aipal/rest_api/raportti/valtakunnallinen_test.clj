(ns aipal.rest-api.raportti.valtakunnallinen-test
  (:require [clojure.test :refer :all]
            [aipal.rest-api.raportti.valtakunnallinen :refer :all :as valtakunnallinen]))

(deftest muodosta-tutkintovertailun-parametrit-test
  (are [opintoalat koulutusalat odotettu-tulos]
    (= (#'valtakunnallinen/muodosta-tutkintovertailun-parametrit opintoalat koulutusalat)
       odotettu-tulos)
    [799]     []    {:tutkintorakennetaso "opintoala", :opintoalat [799]}
    [799 799] []    {:tutkintorakennetaso "opintoala", :opintoalat [799]}
    [799 801] [7 7] {:tutkintorakennetaso "koulutusala", :koulutusalat [7]}
    [603 703] [6 7] {:tutkintorakennetaso "koulutusala", :koulutusalat nil}))
