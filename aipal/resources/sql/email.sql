-- :name lisaa-lahetystieto! :! :n
INSERT INTO email_log(sahkoposti, taustatiedot, tunniste, status)
VALUES (:sahkoposti, :taustatiedot, :tunniste, :status);