set session aipal.kayttaja='JARJESTELMA';

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa)
values ('OID.AIPAL-E2E', 'AIPAL-E2E', 'Test', 'End-to-End', true);

-- Testiorganisaatio opetushallituskäyttäjille
insert into koulutustoimija (ytunnus, nimi_fi, oid )
  values ('9876543-2', 'Testi-Opetushallitus', '1.2.246.777.888.1111111111');

-- manuaaliseen testaukseen tarkoitetut testikäyttäjät

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa)
values ('OID.T-1001', 'T-1001', 'Pekka', 'Pääkäyttäjä', true);

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa)
values ('OID.T-800', 'T-800', 'Arska', 'Katselija', true);

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa)
values ('OID.T-850', 'T-850', 'Marjatta', 'Toimikunta-Katselija', true);

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa)
values ('OID.T-700', 'T-700', 'Uolevi', 'Katselija', true);

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values ('9876543-2', 'YLLAPITAJA', 'OID.T-1001', 'true');

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values ('9876543-2', 'OPH-KATSELIJA', 'OID.T-800', 'true');

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values (null, 'TTK-KATSELIJA', 'OID.T-850', 'true');

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values (null, 'KATSELIJA', 'OID.T-700', 'true');


-- testiorganisaatiot käyttöoikeuksia varten
insert into koulutustoimija (ytunnus, nimi_fi, sahkoposti, puhelin, osoite, postinumero, postitoimipaikka, www_osoite, oid )
  values ('2345678-0', 'Ruikonperän opistoaste', 'ruikonpera@solita.fi', '+35850505050', 'Perämäkkylän ohitustie 2', '00310', 'Ylijyrmylä', 'http://www.solita.fi', '1.2.246.777.888.9999999999');

insert into oppilaitos (oppilaitoskoodi, koulutustoimija, nimi_fi, sahkoposti, puhelin, osoite, postinumero, postitoimipaikka, www_osoite )
  values ('12345', '2345678-0', 'Ruikonperän multakurkkuopisto', 'ruikonpera@solita.fi', '+35850505050', 'Perämäkkylän ohitustie 2', '00310', 'Ylijyrmylä', 'http://www.solita.fi'  );


insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa)
values ('OID.T-X', 'T-X', 'Krista', 'Vastuukäyttäjä', true);

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa )
values ('OID.T-101', 'T-101', 'Aarno', 'Oppilaitos-Katselija', true );

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa )
values ('OID.T-H', 'T-H', 'Pirjo', 'Oppilaitos-Käyttäjä', true );

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values ('2345678-0', 'OPL-VASTUUKAYTTAJA', 'OID.T-X', true);

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values ('2345678-0', 'OPL-KATSELIJA', 'OID.T-101', true);

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values ('2345678-0', 'OPL-KAYTTAJA', 'OID.T-H', true);

-- toinen testiorganisaatio ja sille käyttäjät testaamisen helpottamiseksi

insert into koulutustoimija (ytunnus, nimi_fi, sahkoposti, puhelin, osoite, postinumero, postitoimipaikka, www_osoite )
  values ('7654321-2', 'Viuluhiomo Viola', 'viuluhiomo@solita.fi', '+35850505050', 'Van Helsingin katu 24', '00310', 'Helsinki', 'http://www.solita.fi' );

insert into oppilaitos (oppilaitoskoodi, koulutustoimija, nimi_fi, sahkoposti, puhelin, osoite, postinumero, postitoimipaikka, www_osoite )
  values ('54321', '7654321-2', 'Kallion viuluhiomo', 'viuluhiomo@solita.fi', '+35850505050', 'Van Helsingin katu 24', '00310', 'Helsinki', 'http://www.solita.fi' );

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa)
values ('OID.8086', '8086', 'Pertti', 'Kompura', true );

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa )
values ('OID.6502', '6502', 'Carl', 'Mosse', true );

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa )
values ('OID.68000', '68000', 'Mirka', 'Otorola', true );

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values ('7654321-2', 'OPL-VASTUUKAYTTAJA', 'OID.8086', true);

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values ('7654321-2', 'OPL-NTMVASTUUKAYTTAJA', 'OID.8086', true);

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values ('7654321-2', 'OPL-KATSELIJA', 'OID.6502', true);

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values ('7654321-2', 'OPL-KAYTTAJA', 'OID.68000', true);

-- Tutkinnot testikäyttöä varten

insert into koulutusala (koulutusalatunnus, nimi_fi, nimi_sv) values
  ('X', 'Testikoulutusala', 'Testikoulutusala (sv)');

insert into opintoala (opintoalatunnus, koulutusala, nimi_fi, nimi_sv) values
  ('X00', 'X', 'Testiopintoala', 'Testiopintoala (sv)');

insert into tutkinto (tutkintotunnus, opintoala, nimi_fi, nimi_sv) values
  ('X00001', 'X00', 'Maanviljelyalan testitutkinto', 'Maanviljelyalan testitutkinto (sv)'),
  ('X00002', 'X00', 'Metsätalousalan testitutkinto', 'Metsätalousalan testitutkinto (sv)'),
  ('X00003', 'X00', 'Viulunhionnan testitutkinto', 'Viulunhionnan testitutkinto (sv)'),
  ('X00004', 'X00', 'Viulunhionnan erikoistestitutkinto', 'Viulunhionnan erikoistestitutkinto (sv)');

insert into koulutustoimija_ja_tutkinto (koulutustoimija, tutkinto) values
  ('2345678-0', 'X00001'),
  ('2345678-0', 'X00002'),
  ('7654321-2', 'X00003'),
  ('7654321-2', 'X00004');
