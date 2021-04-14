(ns aipal.toimiala.kayttajaroolit)

(def kyselytyypit {:amk        ["avop" "amk-uraseuranta" "rekrykysely"]
                   :yo         ["kandipalaute" "yo-uraseuranta" "rekrykysely"]
                   :amis       ["amispalaute" "tyoelamapalaute"]
                   :yllapitaja ["itsearvionti" "ohjaus" "move" "tyoelamapalaute"]})

(def yllapito-kyselytyypit (distinct (apply concat (vals kyselytyypit))))

(def kayttooikeus->rooli {"YLLAPITAJA"                  {:rooli "YLLAPITAJA" :kyselytyypit yllapito-kyselytyypit}
                          "KATSELIJA"                   {:rooli "KATSELIJA" :kyselytyypit yllapito-kyselytyypit}
                          "AMKVASTUUKAYTTAJA"           {:rooli "VASTUUKAYTTAJA" :kyselytyypit(:amk kyselytyypit)}
                          "AMKKAYTTAJA"                 {:rooli "KAYTTAJA" :kyselytyypit (:amk kyselytyypit)}
                          "AMKKATSELIJA"                {:rooli "KATSELIJA" :kyselytyypit (:amk kyselytyypit)}
                          "ARVO-YO-VASTUUKAYTTAJA"      {:rooli "VASTUUKAYTTAJA" :kyselytyypit (:yo kyselytyypit)}
                          "ARVO-YO-KAYTTAJA"            {:rooli "KAYTTAJA" :kyselytyypit (:yo kyselytyypit)}
                          "ARVO-YO-KATSELIJA"           {:rooli "KATSELIJA" :kyselytyypit (:yo kyselytyypit)}
                          "ARVO-KT-VASTUUKAYTTAJA"      {:rooli "VASTUUKAYTTAJA" :kyselytyypit (:amis kyselytyypit)}
                          "ARVO-KT-KYSELYKERTAKAYTTAJA" {:rooli "KYSELYKERTAKAYTTAJA" :kyselytyypit (:amis kyselytyypit)}
                          "ARVO-KT-KAYTTAJA"            {:rooli "KAYTTAJA" :kyselytyypit (:amis kyselytyypit)}
                          "ARVO-KT-KATSELIJA"           {:rooli "KATSELIJA" :kyselytyypit (:amis kyselytyypit)}})

(def roolijarjestys
  "Roolien prioriteettijärjestys, jolla valitaan aktiivinen rooli jos käyttäjä ei ole sitä valinnut"
  {"YLLAPITAJA" 0
   "VASTUUKAYTTAJA" 2
   "KYSELYKERTAKAYTTAJA" 3
   "KAYTTAJA" 4
   "KATSELIJA" 5})
