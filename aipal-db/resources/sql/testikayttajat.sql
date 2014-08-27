set session aipal.kayttaja='JARJESTELMA';

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa)
values ('OID.AIPAL-E2E', 'AIPAL-E2E', 'Test', 'End-to-End', true);

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values (null, 'YLLAPITAJA', 'OID.AIPAL-E2E', 'true');

-- manuaaliseen testaukseen tarkoitetut testikäyttäjät

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa)
values ('OID.T-1001', 'T-1001', 'Yrjö', 'Ylläpitäjä', true);
 
insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa)
values ('OID.T-800', 'T-800', 'Arska', 'Katselija', true);

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa)
values ('OID.T-850', 'T-850', 'Marjatta', 'Toimikunta-Katselija', true);

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa)
values ('OID.T-700', 'T-700', 'Uolevi', 'Katselija', true);

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values (null, 'YLLAPITAJA', 'OID.T-1001', 'true');

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values (null, 'OPH-KATSELIJA', 'OID.T-1001', 'true');

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values (null, 'TTK-KATSELIJA', 'OID.T-1001', 'true');

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values (null, 'KATSELIJA', 'OID.T-1001', 'true');



-- testiorganisaatiot käyttöoikeuksia varten
insert into koulutustoimija (ytunnus, nimi_fi, sahkoposti, puhelin, osoite, postinumero, postitoimipaikka, www_osoite )
  values ('2345678-0', 'Ruikonperän opistoaste', 'ruikonpera@solita.fi', '+35850505050', 'Perämäkkylän ohitustie 2', '00310', 'Ylijyrmylä', 'http://www.solita.fi'   );
  
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
values ('7654321-2', 'OPL-KATSELIJA', 'OID.6502', true);

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values ('7654321-2', 'OPL-KAYTTAJA', 'OID.68000', true);
