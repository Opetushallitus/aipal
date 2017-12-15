-- :name poista-koulutustoimijoiden-tutkinnot! :! :n
DELETE FROM koulutustoimija_ja_tutkinto;

-- :name lisaa-koulutustoimijan-tutkinto! :! :n
INSERT INTO koulutustoimija_ja_tutkinto (koulutustoimija, tutkinto, voimassa_alkupvm, voimassa_loppupvm)
    SELECT :ytunnus, :tutkintotunnus, to_date(:alkupvm, 'YYYY-MM-DD'), to_date(:loppupvm, 'YYYY-MM-DD')
    WHERE EXISTS (SELECT ytunnus FROM koulutustoimija WHERE ytunnus = :ytunnus)
    AND   EXISTS(SELECT tutkintotunnus FROM tutkinto WHERE tutkintotunnus = :tutkintotunnus);

-- :name poista-koulutustoimijan-tutkinto! :! :n
DELETE FROM koulutustoimija_ja_tutkinto WHERE koulutustoimija = :koulutustoimija AND tutkinto = :tutkinto;