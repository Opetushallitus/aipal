set session aipal.kayttaja='JARJESTELMA';

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa, rooli)
values ('OID.E2E', 'E2E', 'Test', 'End-to-End', true, 'YLLAPITAJA');

-- manuaaliseen testaukseen tarkoitetut testikäyttäjät

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa, rooli)
values ('OID.T-1001', 'T-1001', 'Yrjö', 'Ylläpitäjä', true, 'YLLAPITAJA');
 
insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa, rooli)
values ('OID.T-800', 'T-800', 'Arska', 'Katselija', true, 'OPH-KATSELIJA');

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa, rooli)
values ('OID.T-850', 'T-1001', 'Marjatta', 'Toimikunta-Katselija', true, 'TTK-KATSELIJA');

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa, rooli)
values ('OID.T-700', 'T-1001', 'Uolevi', 'Katselija', true, 'KATSELIJA');



-- testiorganisaatiot käyttöoikeuksia varten
insert into koulutustoimija (ytunnus, nimi_fi, sahkoposti, puhelin, osoite, postinumero, postitoimipaikka, www_osoite )
  values ('2345678-0', 'Ruikonperän opistoaste', 'ruikonpera@solita.fi', '+35850505050', 'Perämäkkylän ohitustie 2', '00310', 'Ylijyrmylä', 'http://www.solita.fi'   );
  
insert into oppilaitos (oppilaitoskoodi, koulutustoimija, nimi_fi, sahkoposti, puhelin, osoite, postinumero, postitoimipaikka, www_osoite )
  values ('12345', '2345678-0', 'Ruikonperän multakurkkuopisto', 'ruikonpera@solita.fi', '+35850505050', 'Perämäkkylän ohitustie 2', '00310', 'Ylijyrmylä', 'http://www.solita.fi'  ); 
 
 
insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa, rooli)
values ('OID.T-X', 'T-1001', 'Krista', 'Vastuukäyttäjä', true, 'OPL-VASTUUKAYTTAJA' );

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa, rooli )
values ('OID.T-101', 'T-1001', 'Aarno', 'Oppilaitos-Katselija', true, 'OPL-KATSELIJA' );

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa, rooli )
values ('OID.T-H', 'T-1001', 'Pirjo', 'Oppilaitos-Käyttäjä', true, 'OPL-KAYTTAJA' );

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
  
insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa, rooli)
values ('OID.8086', '8086', 'Pertti', 'Kompura', true, 'OPL-VASTUUKAYTTAJA' );

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa, rooli )
values ('OID.6502', '6502', 'Carl', 'Mosse', true, 'OPL-KATSELIJA' );

insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa, rooli )
values ('OID.68000', '68000', 'Mirka', 'Otorola', true, 'OPL-KAYTTAJA' );

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values ('7654321-2', 'OPL-VASTUUKAYTTAJA', 'OID.8086', true);

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values ('7654321-2', 'OPL-KATSELIJA', 'OID.6502', true);

insert into rooli_organisaatio(organisaatio, rooli, kayttaja, voimassa)
values ('7654321-2', 'OPL-KAYTTAJA', 'OID.68000', true);
