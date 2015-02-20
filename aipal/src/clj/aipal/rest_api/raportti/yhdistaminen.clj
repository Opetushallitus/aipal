(ns aipal.rest-api.raportti.yhdistaminen)

(defn yhdistä-kentästä [kenttä datat]
  {kenttä (map kenttä datat)})

(defn yhdistä-kaikki-kentät [datat]
  (reduce (fn [result key] (merge result (yhdistä-kentästä key datat))) {} (keys (first datat))))

(defn yhdistä-vektorit [datat]
  (apply map vector datat))

(defn päivitä-polusta [[k & ks] päivitä rakenne]
  (let [päivitä-seuraavat (if ks
                            (partial päivitä-polusta ks päivitä)
                            päivitä)]
    (if (sequential? rakenne)
      (map päivitä-seuraavat rakenne)
      (update-in rakenne [k] päivitä-seuraavat))))

(defn päivitä-kentät [kentät päivitä rakenne]
  (reduce (fn [rakenne kenttä] (update-in rakenne [kenttä] päivitä)) rakenne kentät))

(defn yhdista-raportit [raportit]
  (->> raportit
    yhdistä-kaikki-kentät
    (päivitä-polusta [:raportti] yhdistä-vektorit)
    (päivitä-polusta [:raportti :*] yhdistä-kaikki-kentät)
    (päivitä-polusta [:raportti :* :kysymykset] yhdistä-vektorit)
    (päivitä-polusta [:raportti :* :kysymykset :*] yhdistä-kaikki-kentät)
    (päivitä-polusta [:raportti :* :kysymykset :* :jakauma] yhdistä-vektorit)
    (päivitä-polusta [:raportti :* :kysymykset :* :jakauma :*] yhdistä-kaikki-kentät)
    (päivitä-polusta [:raportti :* :kysymykset :* :jakauma :*] (partial päivitä-kentät [:jarjestys :vaihtoehto_fi :vaihtoehto_sv :vaihtoehto-avain] first))
    (päivitä-polusta [:raportti :* :kysymykset :*] (partial päivitä-kentät [:jarjestys :eos_vastaus_sallittu :kysymys_fi :kysymys_sv :vastaustyyppi] first))
    (päivitä-polusta [:raportti :*] (partial päivitä-kentät [:kysymysryhmaid :nimi_fi :nimi_sv] first))
    (päivitä-kentät [:luontipvm :parametrit] first)))

