(ns aipal.rest-api.raportti.yhdistaminen)

(defn yhdistä-kentästä [kenttä datat]
  {kenttä (map kenttä datat)})

(defn yhdistä-kaikki-kentät [datat]
  (reduce (fn [result key] (merge result (yhdistä-kentästä key datat))) {} (keys (first datat))))

(defn yhdistä-vektorit [datat]
  (apply map vector datat))

(defn yhdistä-samat [xs]
  {:pre [(or (nil? xs) (empty? xs) (apply = xs))]}
  (first xs))

(defn päivitä-polusta [[k & ks] päivitä rakenne]
  (let [päivitä-seuraavat (if ks
                            (partial päivitä-polusta ks päivitä)
                            päivitä)]
    (if (sequential? rakenne)
      (map päivitä-seuraavat rakenne)
      (update-in rakenne [k] päivitä-seuraavat))))

(defn päivitä-kentät [kentät päivitä rakenne]
  (reduce (fn [rakenne kenttä] (update-in rakenne [kenttä] päivitä)) rakenne kentät))

(defn käsittele-kyllä-jatkovastaukset [jatkovastaukset]
  (if (not-every? nil? (:kylla jatkovastaukset))
    (->> jatkovastaukset
      (päivitä-polusta [:kylla] yhdistä-kaikki-kentät)
      (päivitä-polusta [:kylla :jakauma] yhdistä-vektorit)
      (päivitä-polusta [:kylla :jakauma :*] yhdistä-kaikki-kentät)
      (päivitä-polusta [:kylla] (partial päivitä-kentät [:kysymys_fi :kysymys_sv :vastaustyyppi] first))
      (päivitä-polusta [:kylla :jakauma :*] (partial päivitä-kentät [:vaihtoehto-avain] first)))
    jatkovastaukset))

(defn käsittele-ei-jatkovastaukset [jatkovastaukset]
  (if (not-every? nil? (:ei jatkovastaukset))
    jatkovastaukset
    (assoc jatkovastaukset :ei nil)))

(defn käsittele-kysymyksen-jatkovastaukset [kysymys]
  (if (not-every? nil? (:jatkovastaukset kysymys))
    (->> kysymys
      (päivitä-polusta [:jatkovastaukset] yhdistä-kaikki-kentät)
      (päivitä-polusta [:jatkovastaukset] käsittele-kyllä-jatkovastaukset)
      (päivitä-polusta [:jatkovastaukset] käsittele-ei-jatkovastaukset))
    (assoc kysymys :jatkovastaukset nil)))

(defn yhdista-raportit [raportit]
  (->> raportit
    yhdistä-kaikki-kentät
    (päivitä-polusta [:raportti] yhdistä-vektorit)
    (päivitä-polusta [:raportti :*] yhdistä-kaikki-kentät)
    (päivitä-polusta [:raportti :* :kysymykset] yhdistä-vektorit)
    (päivitä-polusta [:raportti :* :kysymykset :*] yhdistä-kaikki-kentät)
    (päivitä-polusta [:raportti :* :kysymykset :* :jakauma] yhdistä-vektorit)
    (päivitä-polusta [:raportti :* :kysymykset :* :jakauma :*] yhdistä-kaikki-kentät)
    (päivitä-polusta [:raportti :* :kysymykset :*] käsittele-kysymyksen-jatkovastaukset)
    (päivitä-polusta [:raportti :* :kysymykset :* :jakauma :*] (partial päivitä-kentät [:jarjestys :vaihtoehto_fi :vaihtoehto_sv :vaihtoehto-avain] yhdistä-samat))
    (päivitä-polusta [:raportti :* :kysymykset :*] (partial päivitä-kentät [:jarjestys :eos_vastaus_sallittu :kysymys_fi :kysymys_sv :vastaustyyppi] yhdistä-samat))
    (päivitä-polusta [:raportti :*] (partial päivitä-kentät [:kysymysryhmaid :nimi_fi :nimi_sv] yhdistä-samat))
    (päivitä-kentät [:luontipvm :parametrit] first)))

