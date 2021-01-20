-- :name lisaa-tiedote! :! :n
INSERT INTO tiedote(teksti_fi, teksti_sv, teksti_en, muutettuaika, luotuaika)
VALUES (:teksti_fi, :teksti_sv, :teksti_en, now(), now());

-- :name poista-tiedotteet! :! :n
DELETE FROM tiedote;

-- :name hae-tiedote :? :1
SELECT * FROM tiedote LIMIT 1;