-- :name oppilaitos :? :1
SELECT * FROM oppilaitos WHERE oppilaitoskoodi = :oppilaitoskoodi;

-- :name tutkinto :? :1
SELECT * FROM tutkinto WHERE tutkintotunnus = :tutkintotunnus;

-- :name hae-koodiston-koodit :? :*
SELECT * FROM koodi WHERE koodisto_uri = :koodistouri;

-- :name lisaa-koodiston-koodi! :! :n
INSERT INTO koodi (koodisto_uri, nimi_fi, nimi_sv, nimi_en, koodi_arvo, voimassa_alkupvm, voimassa_loppupvm)
    VALUES (:koodisto_uri, :nimi_fi, :nimi_sv, :nimi_en, :koodi_arvo, :voimassa_alkupvm, :voimassa_loppupvm);

-- :name paivita-koodiston-koodi! :! :n
UPDATE koodi SET nimi_fi = :nimi_fi, nimi_sv = :nimi_sv, nimi_en = :nimi_en, voimassa_alkupvm = :voimassa_alkupvm, voimassa_loppupvm = :voimassa_loppupvm
    WHERE koodisto_uri = :koodisto_uri AND koodi_arvo = :koodi_arvo;
