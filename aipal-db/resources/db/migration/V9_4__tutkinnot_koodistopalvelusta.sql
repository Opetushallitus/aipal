create table tutkinto_tmp (
  tutkintotunnus varchar(6) primary key,
  opintoala varchar(3),
  nimi_fi text,
  nimi_sv text,
  voimassa_alkupvm date,
  voimassa_loppupvm date);

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324104';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '327503';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324115';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('324599', 'Muu tai tuntematon teatteri- ja tanssialan ammattitutkinto', 'Annan eller okänd yrkesexamen inom teater och dans', '1997-01-01', null, '204');
update tutkinto set voimassa_alkupvm='2003-01-01', nimi_fi='Tekstiilialan ammattitutkinto, käsi- ja taideteollisuusala' where tutkintotunnus = '324126';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '337106';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('381172', 'Ruokapalvelujen perustutkinto', 'Matservice, grundstudielinjen', '1997-01-01', null, '802');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327107';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327118';

update tutkinto set voimassa_alkupvm='2006-01-01' where tutkintotunnus = '374121';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '387101';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354310';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '355410';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '354211';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354101';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '355201';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '367301';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '354112';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '377102';

update tutkinto set voimassa_alkupvm='2004-01-01' where tutkintotunnus = '355212';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '321101';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357401';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '355102';

update tutkinto set voimassa_loppupvm='2011-02-28', voimassa_alkupvm='1997-01-01', nimi_sv='Plantskolemästare, specialyrkesexamen' where tutkintotunnus = '367202';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('387299', 'Muu tai tuntematon nuoriso- ja vapaa-aikatoiminnan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen för ungdomsledare och fritidsledare', '1997-01-01', null, '101');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('344102', 'Mikrotukihenkilön ammattitutkinto', 'Persondatorkonsulent, yrkesexamen', '1999-01-01', '2014-12-24', '402');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357302';

update tutkinto set voimassa_alkupvm='2004-01-01' where tutkintotunnus = '367103';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('364199', 'Muu tai tuntematon maatilatalouden ammattitutkinto', 'Annan eller okänd yrkesexamen inom lantbruk', '1997-01-01', null, '601');
update tutkinto set voimassa_loppupvm='2005-03-31', voimassa_alkupvm='1997-01-01', nimi_sv='Sömmarmästare, specialyrkesexamen' where tutkintotunnus = '358402';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357203';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('354299', 'Muu tai tuntematon LVI-alan ammattitutkinto', 'Annan eller okänd yrkesexamen inom VVS-branschen', '1997-01-01', null, '501');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '334103';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '334114';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357104';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '358204';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('357599', 'Muu tai tuntematon tietotekniikan ja tietoliikenteen erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom datateknik och datakommunikation', '1997-01-01', null, '504');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('324699', 'Muu tai tuntematon viestintäalan ammattitutkinto', 'Annan eller okänd yrkesexamen inom mediekultur', '1997-01-01', null, '202');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324105';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324116';

update tutkinto set voimassa_alkupvm='2003-01-01', nimi_fi='Vaatetusalan ammattitutkinto, käsi- ja taideteollisuusala' where tutkintotunnus = '324127';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '337107';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '384110';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327108';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327119';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '374111';

update tutkinto set voimassa_alkupvm='2006-01-01', nimi_sv='Omsorgsarbete för utvecklingsstörda, yrkesexamen' where tutkintotunnus = '374122';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '387201';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364101';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '387102';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '354311';

update tutkinto set voimassa_alkupvm='2003-01-01' where tutkintotunnus = '355411';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354201';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '331101';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '355301';

update tutkinto set voimassa_alkupvm='2003-01-01' where tutkintotunnus = '354212';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354102';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('374199', 'Muu tai tuntematon terveys- ja sosiaalialan ammattitutkinto', 'Annan eller okänd yrkesexamen inom hälso- och socialvård', '1997-01-01', null, '703');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357501';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Timmerman, yrkesexamen' where tutkintotunnus = '355202';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '367302';

update tutkinto set voimassa_alkupvm='2002-01-01' where tutkintotunnus = '354113';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '377103';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('387399', 'Muu tai tuntematon kauneudenhoitoalan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom skönhetsbranschen', '1997-01-01', null, '710');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('364299', 'Muu tai tuntematon puutarhatalouden ammattitutkinto', 'Annan eller okänd yrkesexamen inom trädgårdsskötsel', '1997-01-01', null, '602');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357402';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '355103';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '367203';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Scenassistent, yrkesexamen' where tutkintotunnus = '324501';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '358502';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '357303';

update tutkinto set voimassa_alkupvm='2004-01-01' where tutkintotunnus = '367104';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('354399', 'Muu tai tuntematon auto- ja kuljetusalan ammattitutkinto', 'Annan eller okänd yrkesexamen inom bil- och transportbranschen', '1997-01-01', null, '509');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('358403', 'Pukuompelijan erikoisammattitutkinto', 'Klädsömmare, specialyrkesexamen', '1997-01-01', '2005-03-31', '508');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('355499', 'Muu tai tuntematon tekstiili- ja vaatetusalan ammattitutkinto', 'Annan eller okänd yrkesexamen inom textil- och beklädnadsbranschen', '1997-01-01', null, '508');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357204';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '334104';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('357699', 'Muu tai tuntematon paperi- ja kemianteollisuuden erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom pappersindustrin och kemiska industrin', '1997-01-01', null, '507');
update tutkinto set voimassa_alkupvm='2000-01-01', nimi_sv='Yrkesexamen inom finansbranschen', nimi_fi='Finanssialan ammattitutkinto' where tutkintotunnus = '334115';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357105';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '358205';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('324106', 'Koristeveistäjän ammattitutkinto, käsi- ja taideteollisuusala', 'Finsnickare, yrkesexamen, hantverks- och konstindustribranschen', '1997-01-01', '2014-12-24', '201');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324117';

update tutkinto set voimassa_alkupvm='2006-01-01' where tutkintotunnus = '324128';

update tutkinto set voimassa_alkupvm='2000-01-01', nimi_sv='Specialyrkesexamen för försäljare inom bilbransche' where tutkintotunnus = '337108';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '384111';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327109';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('010001', 'Välinehuoltoalan perustutkinto (kokeilu)', '', '2014-01-01', null, '706');
update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Damfrisörmästare, specialyrkesexamen' where tutkintotunnus = '387301';

update tutkinto set voimassa_loppupvm='2009-12-31', voimassa_alkupvm='1997-01-01', nimi_sv='Skolgångsbiträde, yrkesexamen' where tutkintotunnus = '374112';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364201';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '374123';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '352101';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '387202';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '341101';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354301';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_fi='Tuotantoeläinten hoidon ja hyvinvoinnin ammattitutkinto ' where tutkintotunnus = '364102';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '387103';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('384199', 'Muu tai tuntematon majoitus-, ravitsemis- ja talousalan ammattitutkinto', 'Annan eller okänd yrkesexamen inom inkvarterings-, kosthålls- och hushållsbranschen', '1997-01-01', null, '801');
update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Modist, yrkesexamen' where tutkintotunnus = '355401';

update tutkinto set voimassa_alkupvm='2006-01-01' where tutkintotunnus = '354312';

update tutkinto set voimassa_alkupvm='2003-01-01', nimi_fi='Tekstiilialan ammattitutkinto, tekstiili- ja vaatetustekniikka' where tutkintotunnus = '355412';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354202';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '321301';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357601';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Merkant, grundexamen inom handel och administration', nimi_fi='Merkantti, kaupan ja hallinnon perustutkinto' where tutkintotunnus = '331102';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('387499', 'Muu tai tuntematon liikenteen ja merenkulun erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom transport och sjöfart', '1997-01-01', null, '509');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354103';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('364399', 'Muu tai tuntematon metsätalouden ammattitutkinto', 'Annan eller okänd yrkesexamen inom skogsbruk', '1997-01-01', null, '604');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357502';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Glasmästeribransch, yrkesexamen' where tutkintotunnus = '355203';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Skogstjänsteman, specialyrkesexamen' where tutkintotunnus = '367303';

update tutkinto set voimassa_alkupvm='2002-01-01' where tutkintotunnus = '354114';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '377104';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324601';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357403';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '355104';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('354499', 'Muu tai tuntematon sähköalan ammattitutkinto', 'Annan eller okänd yrkesexamen inom elbranschen', '1997-01-01', null, '503');
update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '324502';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '358503';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('355599', 'Muu tai tuntematon graafisen alan ammattitutkinto', 'Annan eller okänd yrkesexamen inom grafiska branschen', '1997-01-01', null, '505');
update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '357304';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Skomakarmästare, specialyrkesexamen' where tutkintotunnus = '358404';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('357799', 'Muu tai tuntematon puualan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom träbranschen', '1997-01-01', null, '507');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357205';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '334105';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '334116';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357106';

update tutkinto set voimassa_alkupvm='2003-01-01' where tutkintotunnus = '358206';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324107';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('324118', 'Savenvalajakisällin ammattitutkinto', 'Krukmakargesäll, yrkesexamen', '1997-01-01', '2014-12-24', '201');
update tutkinto set voimassa_alkupvm='2006-01-01', nimi_fi='Sisustusalan ammattitutkinto, käsi- ja taideteollisuusala' where tutkintotunnus = '324129';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '371110';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '337109';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '384101';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '384112';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '351101';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '387401';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '352201';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364301';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Herrfrisörmästare, specialyrkesexamen' where tutkintotunnus = '387302';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '374113';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364202';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '374124';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354401';

update tutkinto set voimassa_alkupvm='2011-01-01' where tutkintotunnus = '387203';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('384299', 'Muu tai tuntematon nuoriso- ja vapaa-aikatoiminnan ammattitutkinto', 'Annan eller okänd yrkesutbildning för ungdomsledare och fritidsledare', '1997-01-01', null, '101');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '355501';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354302';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364103';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '387104';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Finsnickare, specialyrkesexamen, process-, kemi- och materialteknik', nimi_fi='Koristeveistäjän erikoisammattitutkinto, prosessi-, kemian- ja materiaalitekniikka' where tutkintotunnus = '357701';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '355402';

update tutkinto set voimassa_alkupvm='2011-01-01' where tutkintotunnus = '354313';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('387599', 'Muu tai tuntematon suojelualan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom skyddsbranschen', '1997-01-01', null, '969');
update tutkinto set voimassa_alkupvm='2003-01-01', nimi_fi='Vaatetusalan ammattitutkinto, tekstiili- ja vaatetustekniikka' where tutkintotunnus = '355413';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354203';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('364499', 'Muu tai tuntematon kalatalouden ammattitutkinto', 'Annan eller okänd yrkesexamen inom fiskeri', '1997-01-01', null, '603');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357602';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354104';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Murare, yrkesexamen' where tutkintotunnus = '355204';

update tutkinto set voimassa_alkupvm='1998-01-01', nimi_sv='Puunkorjuun erikoisammattitutkinto' where tutkintotunnus = '367304';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('354115', 'Kunnossapidon ammattitutkinto', 'Underhåll, yrkesexamen', '2007-01-01', null, '599');
update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '377105';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('354599', 'Muu tai tuntematon tietotekniikan ja tietoliikenteen ammattitutkinto', 'Annan eller okänd yrkesexamen inom datateknik och datakommunikation', '1997-01-01', null, '504');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357404';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '355105';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '358504';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('357899', 'Muu tai tuntematon pintakäsittelyalan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom ytbehandlingsbranschen', '1997-01-01', null, '507');
update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '357305';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('358999', 'Muu tai tuntematon tekniikan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen i teknik', '1997-01-01', null, '599');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '358405';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357206';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '334106';

update tutkinto set voimassa_alkupvm='2002-01-01' where tutkintotunnus = '334117';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357107';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324108';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324119';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '384201';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '361101';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Kock, yrkesexamen' where tutkintotunnus = '384102';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='VVS-branschen, grundexamen' where tutkintotunnus = '351201';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '387501';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '384113';

update tutkinto set voimassa_alkupvm='2003-01-01', nimi_fi='Puusepänalan ammattitutkinto, prosessi-, kemian- ja materiaalitekniikka' where tutkintotunnus = '354710';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '352301';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364401';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Verkstadsmekanik, grundexamen' where tutkintotunnus = '351102';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364302';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354501';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '387303';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '374114';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364203';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354402';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Mjölkhantering, grundexamen' where tutkintotunnus = '352103';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '321501';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357801';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '355502';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '358901';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Bilmekaniker, yrkesexamen' where tutkintotunnus = '354303';

update tutkinto set voimassa_loppupvm='2011-02-28', voimassa_alkupvm='1997-01-01', nimi_sv='Naturenlig produktion, yrkesexamen' where tutkintotunnus = '364104';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '387105';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357702';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Symaskinsmekaniker, yrkesexamen' where tutkintotunnus = '355403';

update tutkinto set voimassa_alkupvm='2011-01-01' where tutkintotunnus = '354314';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '357603';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('354699', 'Muu tai tuntematon paperi- ja kemianteollisuuden ammattitutkinto', 'Annan eller okänd yrkesexamen inom pappersindustrin och kemiska industrin', '1997-01-01', null, '507');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354105';

update tutkinto set voimassa_alkupvm='1998-01-01' where tutkintotunnus = '321204';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Byggnadsmaskinskötare, yrkesexamen' where tutkintotunnus = '355205';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1999-01-01', nimi_sv='Mångsidig användning av skog, specialyrkesexamen' where tutkintotunnus = '367305';

update tutkinto set voimassa_alkupvm='2001-01-01', nimi_fi='Koulunkäynnin ja aamu- ja iltapäivätoiminnan ohjauksen                     erikoisammattitutkinto' where tutkintotunnus = '377106';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '355106';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '358505';

update tutkinto set voimassa_alkupvm='2003-01-01' where tutkintotunnus = '357306';

update tutkinto set voimassa_loppupvm='2005-03-31', voimassa_alkupvm='1997-01-01', nimi_sv='Skräddarmästare, specialyrkesexamen' where tutkintotunnus = '358406';

update tutkinto set voimassa_alkupvm='2002-01-01' where tutkintotunnus = '357207';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Dekoratör, yrkesexamen' where tutkintotunnus = '334107';

update tutkinto set voimassa_alkupvm='2002-01-01', nimi_fi='Kiinteistönvälitysalan ammattitutkinto' where tutkintotunnus = '334118';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357108';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324109';

update tutkinto set voimassa_alkupvm='2000-01-01', nimi_sv='Rengöringsskötsel, grundexamen' where tutkintotunnus = '381111';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Grundexamen inom social- och hälsovård' where tutkintotunnus = '371101';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '384301';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Grundexamen inom trädgårdsskötsel' where tutkintotunnus = '361201';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '384202';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '351301';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Pälsdjursuppfödning, grundexamen' where tutkintotunnus = '361102';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '352401';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '384103';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Fastighetsskötsel, grundexamen', nimi_fi='Kiinteistöhoitoalan perustutkinto' where tutkintotunnus = '351202';

update tutkinto set voimassa_alkupvm='2003-01-01' where tutkintotunnus = '384114';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364402';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354601';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Maskinmontering, grundexamen' where tutkintotunnus = '351103';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('384499', 'Muu tai tuntematon liikenteen ja merenkulun ammattitutkinto', 'Annan eller okänd yrkesexamen inom transport och sjöfart', '1997-01-01', null, '509');
update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Skogsarbetare, yrkesexamen' where tutkintotunnus = '364303';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354502';

update tutkinto set voimassa_alkupvm='2002-01-01' where tutkintotunnus = '387304';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '374115';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Mediekultur, grundexamen' where tutkintotunnus = '321601';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364204';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354403';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_fi='Maalarimestarin erikoisammattitutkinto, prosessi-, kemian- ja materiaalitekniikka' where tutkintotunnus = '357802';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '355503';

update tutkinto set voimassa_alkupvm='2001-01-01', nimi_fi='Tuotekehittäjän erikoisammattitutkinto' where tutkintotunnus = '358902';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364105';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '387106';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Snickarmästare, specialyrkesexamen, process-, kemi- och materialteknik', nimi_fi='Puuseppämestarin erikoisammattitutkinto, prosessi-, kemian- ja materiaalitekniikka' where tutkintotunnus = '357703';

update tutkinto set voimassa_loppupvm='2005-03-31', voimassa_alkupvm='1997-01-01', nimi_sv='Klädsömmare, yrkesexamen' where tutkintotunnus = '355404';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('354799', 'Muu tai tuntematon puualan ammattitutkinto', 'Annan eller okänd yrkesexamen inom träbranschen', '1997-01-01', null, '507');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354205';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('367999', 'Muu tai tuntematon maa- ja metsätalousalan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom lant- och skogsbruk', '1997-01-01', null, '699');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354106';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Byggnadsarbetare, yrkesexamen' where tutkintotunnus = '355206';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1999-01-01', nimi_sv='Skogsarbetare, specialyrkesexamen' where tutkintotunnus = '367306';

update tutkinto set voimassa_alkupvm='2004-01-01' where tutkintotunnus = '377107';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '355107';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '358506';

update tutkinto set voimassa_alkupvm='2011-01-01', nimi_fi='Lentokonetekniikan erikoisammattitutkinto' where tutkintotunnus = '357307';

update tutkinto set voimassa_loppupvm='2005-03-31', voimassa_alkupvm='1999-01-01', nimi_sv='Modellmästare, specialyrkesexamen' where tutkintotunnus = '358407';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '334108';

update tutkinto set voimassa_alkupvm='2006-01-01' where tutkintotunnus = '334119';

update tutkinto set voimassa_alkupvm='2002-01-01' where tutkintotunnus = '357109';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Hotell-, restaurang- och storhushållsbranschen, grundexamen' where tutkintotunnus = '381101';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '381112';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '384401';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '361301';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Elbranschen, grundexamen' where tutkintotunnus = '351401';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Grafiska branschen, grundexamen' where tutkintotunnus = '352501';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '384203';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Fordonsmekaniker (grundexamen)', nimi_fi='Ajoneuvoasentaja (perustutkinto)' where tutkintotunnus = '351302';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('352402', 'Pukuompelija (perustutkinto)', 'Klädsömmare (grundexamen)', '1997-01-01', null, '508');
update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Turism, yrkesexamen' where tutkintotunnus = '384104';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Finsnickare, yrkesexamen, process-, kemi- och materialteknik', nimi_fi='Koristeveistäjän ammattitutkinto, prosessi-, kemian- ja materiaalitekniikka' where tutkintotunnus = '354701';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '351203';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('384599', 'Muu tai tuntematon suojelualan ammattitutkinto', 'Annan eller okänd yrkesexamen inom skyddsbranschen', '1997-01-01', null, '969');
update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Luonnontuotealan erikoisammattitutkinto', nimi_fi='Luonnontuotealan erikoisammattitutkinto' where tutkintotunnus = '367901';

update tutkinto set voimassa_alkupvm='2004-01-01' where tutkintotunnus = '364403';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354602';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Plåtslagar- och svetsningsbranschen, grundexamen' where tutkintotunnus = '351104';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364304';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1999-01-01', nimi_sv='Ambulansförare, yrkesexamen' where tutkintotunnus = '374116';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '321602';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '364205';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354404';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('354899', 'Muu tai tuntematon pintakäsittelyalan ammattitutkinto', 'Annan eller okänd yrkesexamen inom ytbehandlingsbranschen', '1997-01-01', null, '507');
update tutkinto set voimassa_alkupvm='2002-01-01' where tutkintotunnus = '357803';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '355504';

update tutkinto set voimassa_alkupvm='2006-01-01', nimi_fi='Sisustusalan erikoisammattitutkinto, tekniikan ja liikenteen ala' where tutkintotunnus = '358903';

update tutkinto set voimassa_loppupvm='2009-12-31', voimassa_alkupvm='1997-01-01', nimi_sv='Bilelmekaniker, yrkesexamen' where tutkintotunnus = '354305';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('355999', 'Muu tai tuntematon tekniikan ammattitutkinto', 'Annan eller okänd yrkesexamen i teknik', '1997-01-01', null, '599');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364106';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Underhållsmästare inom såg- och skivindustrin, specialyrkesexamen' where tutkintotunnus = '357704';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Skomakare, yrkesexamen' where tutkintotunnus = '355405';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354206';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354107';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('355207', 'Torninosturinkuljettajan ammattitutkinto', 'Kranförare, yrkesexamen', '1997-01-01', '2014-12-24', '501');
update tutkinto set voimassa_alkupvm='2006-01-01' where tutkintotunnus = '377108';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '355108';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='2000-01-01', nimi_sv='Tryckeritekniker, specialyrkesexamen' where tutkintotunnus = '358507';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '358408';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '334109';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Försäljning och kundbetjäning, grundexamen' where tutkintotunnus = '381102';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '384501';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '381113';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '361401';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '384402';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Datateknik, grundexamen' where tutkintotunnus = '351501';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Elkraftsteknik, grundexamen' where tutkintotunnus = '351402';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354801';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Bildframställning, grundexamen' where tutkintotunnus = '352502';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '384204';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Bilplåtslagare (grundexamen)', nimi_fi='Autokorinkorjaaja (perustutkinto)' where tutkintotunnus = '351303';

update tutkinto set voimassa_alkupvm='2008-01-01' where tutkintotunnus = '361104';

update tutkinto set voimassa_alkupvm='1998-01-01' where tutkintotunnus = '355901';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('352403', 'Vaatturi (perustutkinto)', 'Skräddare (grundexamen)', '1997-01-01', null, '508');
update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Guide, yrkesexamen' where tutkintotunnus = '384105';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('331954', 'Vakuutusalan perustutkinto', 'Försäkringsbranschen, grundexamen', '1997-01-01', null, '303');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354702';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '351204';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '367902';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('387999', 'Muu tai tuntematon palvelualojen erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom servicebranscher', '1997-01-01', null, '969');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354603';

update tutkinto set voimassa_alkupvm='1998-01-01' where tutkintotunnus = '364305';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '374117';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354405';

update tutkinto set voimassa_alkupvm='2002-01-01' where tutkintotunnus = '355505';

update tutkinto set voimassa_alkupvm='2008-01-01' where tutkintotunnus = '358904';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Dieselmekaniker, yrkesexamen' where tutkintotunnus = '354306';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364107';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357705';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Textilmaskinställare, yrkesexamen' where tutkintotunnus = '355406';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354207';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354108';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '355208';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '377109';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '355109';

update tutkinto set voimassa_alkupvm='2006-01-01' where tutkintotunnus = '358508';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '358409';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '381410';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_fi='Nuoriso- ja vapaa-ajan ohjauksen perustutkinto' where tutkintotunnus = '381201';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Matservice, grundexamen' where tutkintotunnus = '381103';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Kemiska industrin, grundexamen' where tutkintotunnus = '351601';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '384403';

update tutkinto set voimassa_alkupvm='2008-01-01' where tutkintotunnus = '351502';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Automationsteknik, grundexamen' where tutkintotunnus = '351403';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354802';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '352503';

update tutkinto set voimassa_alkupvm='2006-01-01' where tutkintotunnus = '384205';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('351304', 'Automaalari (perustutkinto)', 'Billackerare (grundexamen)', '1997-01-01', null, '509');
update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '321901';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '355902';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('352404', 'Modisti (perustutkinto)', 'Modist (grundexamen)', '1997-01-01', null, '508');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '384106';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('331955', 'Sosiaaliturvan perustutkinto', 'Socialskydd, grundexamen', '1997-01-01', null, '303');
update tutkinto set voimassa_loppupvm='2003-12-31', voimassa_alkupvm='1997-01-01', nimi_sv='Snickare, yrkesexamen, process-, kemi- och materialteknik', nimi_fi='Puusepän ammattitutkinto, prosessi-, kemian- ja materiaalitekniikka' where tutkintotunnus = '354703';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('364999', 'Muu tai tuntematon maa- ja metsätalousalan ammattitutkinto', 'Annan eller okänd yrkesexamen inom lant- och skogsbruk', '1997-01-01', null, '699');
update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '367903';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354604';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '351106';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1999-01-01', nimi_sv='Skogsarbetare, yrkesexamen' where tutkintotunnus = '364306';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '374118';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354406';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354307';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '364108';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Sågbladsmästare, specialyrkesexamen' where tutkintotunnus = '357706';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '355407';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('354208', 'Putkieristäjän ammattitutkinto', 'Rörisolerare, yrkesexamen', '1997-01-01', '2014-12-24', '501');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354109';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '355209';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('381521', 'Vankeinhoidon perustutkinto', 'Fångvårdens grundexamen', '1997-01-01', null, '904');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '351701';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Huslig ekonomi och rengöringsservice, grundexamen' where tutkintotunnus = '381104';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364901';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Pappersindustrin, grundexamen' where tutkintotunnus = '351602';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('384404', 'Huoltamotyöntekijän ammattitutkinto', 'Servicestationsarbetare, yrkesexamen', '1999-01-01', '2014-12-24', '301');
update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Lager- och transportfunktioner, grundexamen' where tutkintotunnus = '331901';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_fi='Maalarin ammattitutkinto, prosessi-, kemian- ja materiaalitekniikka' where tutkintotunnus = '354803';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Fartygsmekaniker (grundexamen)', nimi_fi='Laivamekaanikko (perustutkinto)' where tutkintotunnus = '351305';

update tutkinto set voimassa_alkupvm='2006-01-01' where tutkintotunnus = '321902';

update tutkinto set voimassa_alkupvm='2006-01-01' where tutkintotunnus = '355903';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Skobranschen, grundexamen' where tutkintotunnus = '352405';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Försäljning och kundbetjäning, yrkesexamen', nimi_fi='Myynti- ja asiakaspalvelun ammattitutkinto' where tutkintotunnus = '384107';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Underhåll inom såg- och skivindustrin, yrkesexamen' where tutkintotunnus = '354704';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '367904';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '354605';

update tutkinto set voimassa_alkupvm='2008-01-01' where tutkintotunnus = '351107';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '364307';

update tutkinto set voimassa_alkupvm='2002-01-01' where tutkintotunnus = '374119';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '354407';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Jordschaktningsmaskinförare, yrkesexamen', nimi_fi='Maanrakennuskoneenkuljettajan ammattitutkinto' where tutkintotunnus = '354308';

update tutkinto set voimassa_alkupvm='2003-01-01' where tutkintotunnus = '364109';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_fi='Venemestarin erikoisammattitutkinto, prosessi-, kemian- ja materiaalitekniikka' where tutkintotunnus = '357707';

update tutkinto set voimassa_loppupvm='2005-03-31', voimassa_alkupvm='1997-01-01', nimi_sv='Skräddare, yrkesexamen' where tutkintotunnus = '355408';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354209';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Transportbranschen, grundexamen' where tutkintotunnus = '381401';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '381203';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Sågbladsskötsel, grundexamen' where tutkintotunnus = '351702';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364902';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '351603';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('384999', 'Muu tai tuntematon palvelualojen ammattitutkinto', 'Annan eller okänd yrkesexamen inom servicebranscher', '1997-01-01', null, '969');
update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '384405';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354804';

update tutkinto set voimassa_alkupvm='2006-01-01', nimi_fi='Sisustusalan ammattitutkinto, tekniikan ja liikenteen ala' where tutkintotunnus = '355904';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Pälsbranschen, grundexamen' where tutkintotunnus = '352406';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '384108';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354705';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '367905';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '364308';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '354408';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354309';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357708';

update tutkinto set voimassa_loppupvm='2005-03-31', voimassa_alkupvm='1999-01-01', nimi_sv='Textilteknik, yrkesexamen' where tutkintotunnus = '355409';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '381402';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '381303';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Industriytbehandling, grundexamen' where tutkintotunnus = '351801';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '381204';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Teknisk ritare (grundexamen)', nimi_fi='Teknisen piirtäjän perustutkinto' where tutkintotunnus = '352901';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '351703';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '381106';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Vildmarksguide, yrkesexamen' where tutkintotunnus = '364903';

update tutkinto set voimassa_alkupvm='2000-01-01', nimi_sv='Kemisk teknik, grundexamen' where tutkintotunnus = '351604';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '351307';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '355905';

update tutkinto set voimassa_alkupvm='2000-01-01', nimi_sv='Textilbranschen, grundexamen' where tutkintotunnus = '352407';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '384109';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Industrisnickare, yrkesexamen' where tutkintotunnus = '354706';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='2001-01-01', nimi_sv='Mångsidig användning av skog, yrkesexamen' where tutkintotunnus = '364309';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '354409';

update tutkinto set voimassa_alkupvm='2003-01-01', nimi_fi='Puusepänalan erikoisammattitutkinto, prosessi-, kemian- ja materiaalitekniikka' where tutkintotunnus = '357709';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Skeppare-undermaskinmästare (grundexamen)', nimi_fi='Laivuri-alikonemestari (perustutkinto)' where tutkintotunnus = '381403';

update tutkinto set voimassa_alkupvm='2000-01-01', nimi_sv='Skönhetsbranschen, grundexamen' where tutkintotunnus = '381304';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Specialytbehandling, grundexamen' where tutkintotunnus = '351802';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '352902';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '351704';

update tutkinto set voimassa_alkupvm='2000-01-01', nimi_sv='Cateringbranschen, grundexamen' where tutkintotunnus = '381107';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364904';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '351605';

update tutkinto set voimassa_alkupvm='2008-01-01' where tutkintotunnus = '351407';

update tutkinto set voimassa_alkupvm='2011-01-01' where tutkintotunnus = '355906';

update tutkinto set voimassa_alkupvm='2000-01-01', nimi_sv='Beklädnadsbranschen, grundexamen' where tutkintotunnus = '352408';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Sågbladsskötare, yrkesexamen' where tutkintotunnus = '354707';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Natur- och skogsbruk, grundexamen' where tutkintotunnus = '361901';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('381514', 'Poliisin perustutkinto', 'Grundexamen för polis', '1997-01-01', null, '903');
update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Målningsbranschen, grundexamen' where tutkintotunnus = '351803';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '352903';

update tutkinto set voimassa_alkupvm='2000-01-01', nimi_sv='Hotell- och restaurangbranchen, grundexamen' where tutkintotunnus = '381108';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '364905';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '371109';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_fi='Veneenrakentajan ammattitutkinto, prosessi-, kemian- ja materiaalitekniikka' where tutkintotunnus = '354708';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '361902';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '381504';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Specialmålning, grundexamen' where tutkintotunnus = '351804';

update tutkinto set voimassa_alkupvm='2000-01-01', nimi_sv='Huslig ekonomi och konsumentservice, grundexamen' where tutkintotunnus = '381109';

update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '364906';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '354709';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '351805';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '381408';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('381409', 'Korjaajan perustutkinto', 'Reparatör, grundexamen', '1999-01-01', null, '509');
update tutkinto set voimassa_loppupvm='2005-03-31', voimassa_alkupvm='1997-01-01', nimi_sv='Studiovävare, specialyrkesexamen' where tutkintotunnus = '327120';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_fi='Maalarimestarin erikoisammattitutkinto, käsi- ja taideteollisuusala' where tutkintotunnus = '327110';

update tutkinto set voimassa_loppupvm='2005-03-31', voimassa_alkupvm='1997-01-01', nimi_sv='Studiosömmerska, specialyrkesexamen' where tutkintotunnus = '327121';

update tutkinto set voimassa_alkupvm='2002-01-01' where tutkintotunnus = '337110';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327111';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_fi='Venemestarin erikoisammattitutkinto, käsi- ja taideteollisuusala' where tutkintotunnus = '327122';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '317101';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('327199', 'Muu tai tuntematon käsi- ja taideteollisuuden erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom hantverk och konstindustri', '1997-01-01', null, '201');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324120';

update tutkinto set voimassa_alkupvm='2006-01-01' where tutkintotunnus = '337111';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327101';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Sömnadsmästare, specialyrkesexamen' where tutkintotunnus = '327112';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Färgarmästare, specialyrkesexamen' where tutkintotunnus = '327123';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('337199', 'Muu tai tuntematon kaupan ja hallinnon erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom handel och administration', '1997-01-01', null, '301');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324110';

update tutkinto set voimassa_loppupvm='2005-03-31', voimassa_alkupvm='1997-01-01', nimi_sv='Studiovävare, yrkesexamen' where tutkintotunnus = '324121';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Ledarskap, specialyrkesexamen' where tutkintotunnus = '337101';

update tutkinto set voimassa_alkupvm='2007-01-01' where tutkintotunnus = '337112';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('358199', 'Muu tai tuntematon elintarvikealan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom livsmedelsbranschen', '1997-01-01', null, '506');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327102';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('327113', 'Puuseppämestarin erikoisammattitutkinto, käsi- ja taideteollisuusala', 'Snickarmästare, specialyrkesexamen, hantverks- och konstindustribranschen', '1997-01-01', '2014-12-24', '201');
update tutkinto set voimassa_alkupvm='2003-01-01', nimi_sv='SSpecialyrkesexamen inom snickeribranchen (hantverk och konstindustri)', nimi_fi='Puusepänalan erikoisammattitutkinto, käsi- ja taideteollisuusala' where tutkintotunnus = '327124';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('327399', 'Muu tai tuntematon kuvataiteen erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen i bildkonst', '1997-01-01', null, '206');
update tutkinto set voimassa_alkupvm='2006-01-01' where tutkintotunnus = '357110';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '358101';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_fi='Maalarin ammattitutkinto, käsi- ja taideteollisuusala' where tutkintotunnus = '324111';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '347101';

update tutkinto set voimassa_loppupvm='2005-03-31', voimassa_alkupvm='1997-01-01', nimi_sv='Studiosömmerska, yrkesexamen' where tutkintotunnus = '324122';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327301';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '337102';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('357199', 'Muu tai tuntematon kone- ja metallialan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom maskin- och metallbranschen', '1997-01-01', null, '502');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('358299', 'Muu tai tuntematon rakennus- ja yhdyskunta-alan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom byggnadsbranschen', '1997-01-01', null, '501');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('324199', 'Muu tai tuntematon käsi- ja taideteollisuuden ammattitutkinto', 'Annan eller okänd yrkesexamen inom hantverk och konstindustri', '1997-01-01', null, '201');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327103';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327114';

update tutkinto set voimassa_alkupvm='2003-01-01', nimi_fi='Tekstiilialan erikoisammattitutkinto, käsi- ja taideteollisuusala' where tutkintotunnus = '327125';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '357111';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324101';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '358102';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324112';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_fi='Veneenrakentajan ammattitutkinto, käsi- ja taideteollisuusala' where tutkintotunnus = '324123';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('367199', 'Muu tai tuntematon maatilatalouden erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom lantbruk', '1997-01-01', null, '601');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('357299', 'Muu tai tuntematon LVI-alan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom VVS-branschen', '1997-01-01', null, '501');
update tutkinto set voimassa_alkupvm='2001-01-01' where tutkintotunnus = '327302';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Skeppsmäklare, specialyrkesexamen' where tutkintotunnus = '337103';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('334199', 'Muu tai tuntematon kaupan ja hallinnon ammattitutkinto', 'Annan eller okänd yrkesexamen inom handel och administration', '1997-01-01', null, '301');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327104';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('327599', 'Muu tai tuntematon teatteri- ja tanssialan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom teater och dans', '1997-01-01', null, '204');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327115';

update tutkinto set voimassa_alkupvm='2003-01-01', nimi_fi='Vaatetusalan erikoisammattitutkinto, käsi- ja taideteollisuusala' where tutkintotunnus = '327126';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '377110';

update tutkinto set voimassa_alkupvm='2009-01-01' where tutkintotunnus = '355110';

update tutkinto set voimassa_alkupvm='2003-01-01' where tutkintotunnus = '358410';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '334111';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357101';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Timmerman, specialyrkesexamen' where tutkintotunnus = '358201';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324102';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '358103';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('355199', 'Muu tai tuntematon elintarvikealan ammattitutkinto', 'Annan eller okänd yrkesexamen inom livsmedelsbranschen', '1997-01-01', null, '506');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('367299', 'Muu tai tuntematon puutarhatalouden erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom trädgårdsskötsel', '1997-01-01', null, '602');
update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Maskerare, specialyrkesexamen' where tutkintotunnus = '327501';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Sömnadsgesäll, yrkesexamen' where tutkintotunnus = '324113';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Färgargesäll, yrkesexamen' where tutkintotunnus = '324124';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('357399', 'Muu tai tuntematon auto- ja kuljetusalan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom bil- och transportbranschen', '1997-01-01', null, '509');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('358499', 'Muu tai tuntematon tekstiili- ja vaatetusalan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom textil- och beklädnadsbranschen', '1997-01-01', null, '508');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '337104';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('324399', 'Muu tai tuntematon kuvataiteen ammattitutkinto', 'Annan eller okänd yrkesexamen i bildkonst', '1997-01-01', null, '206');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('327699', 'Muu tai tuntematon viestintäalan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom mediekultur', '1997-01-01', null, '202');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327105';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '327116';

update tutkinto set voimassa_alkupvm='2006-01-01', nimi_fi='Sisustusalan erikoisammattitutkinto, käsi- ja taideteollisuusala' where tutkintotunnus = '327127';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '354110';

update tutkinto set voimassa_alkupvm='2000-01-01' where tutkintotunnus = '355210';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '367101';

update tutkinto set voimassa_alkupvm='2003-01-01', nimi_fi='Tekstiilialan erikoisammattitutkinto, tekstiili- ja vaatetustekniikka' where tutkintotunnus = '358411';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357201';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '334101';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '334112';

update tutkinto set voimassa_alkupvm='1997-01-01', nimi_sv='Maskinmontörsmästare, specialyrkesexamen' where tutkintotunnus = '357102';

update tutkinto set voimassa_alkupvm='2006-01-01' where tutkintotunnus = '324201';

update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Murare, specialyrkesexamen' where tutkintotunnus = '358202';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('377199', 'Muu tai tuntematon terveys- ja sosiaalialan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom hälso- och socialvård', '2001-01-01', null, '703');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324103';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('357499', 'Muu tai tuntematon sähköalan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom elbranschen', '1997-01-01', null, '503');
update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Teaterteknik, specialyrkesexamen' where tutkintotunnus = '327502';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('324114', 'Puusepän ammattitutkinto, käsi- ja taideteollisuusala', 'Snickare, yrkesexamen, hantverks- och konstindustribranschen', '1997-01-01', '2003-12-31', '201');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('358599', 'Muu tai tuntematon graafisen alan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom grafiska branschen', '1997-01-01', null, '505');
update tutkinto set voimassa_alkupvm='2003-01-01', nimi_fi='Puusepänalan ammattitutkinto, käsi- ja taideteollisuusala' where tutkintotunnus = '324125';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('327106', 'Koristeveistäjämestarin erikoisammattitutkinto, käsi- ja taideteollisuusala', 'Finsnickare, specialyrkesexamen, hantverks- och konstindustribranschen', '1997-01-01', '2014-12-24', '201');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('327117', 'Savenvalajamestarin erikoisammattitutkinto', 'Krukmakarmästare, specialyrkesexamen', '1997-01-01', '2014-12-24', '201');
update tutkinto set voimassa_alkupvm='2008-01-01' where tutkintotunnus = '327128';

update tutkinto set voimassa_loppupvm='2009-12-31', voimassa_alkupvm='2004-01-01', nimi_sv='Ledare i morgon- och eftermiddagsverksamhet för skolelever, yrkesexamen' where tutkintotunnus = '374120';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('354210', 'Teollisuuseristäjän ammattitutkinto', 'Industriisolerare, yrkesexamen', '1997-01-01', '2014-12-24', '501');
update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '354111';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '377101';

update tutkinto set voimassa_alkupvm='2003-01-01' where tutkintotunnus = '355211';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '355101';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '367201';

update tutkinto set voimassa_alkupvm='1999-01-01' where tutkintotunnus = '344101';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357301';

update tutkinto set voimassa_alkupvm='2002-01-01' where tutkintotunnus = '367102';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('387199', 'Muu tai tuntematon majoitus-, ravitsemis- ja talousalan erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom inkvarterings-, kosthålls- och hushållsbranschen', '1997-01-01', null, '802');
update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Modistmästare, specialyrkesexamen' where tutkintotunnus = '358401';

update tutkinto set voimassa_alkupvm='2003-01-01', nimi_fi='Vaatetusalan erikoisammattitutkinto, tekstiili- ja vaatetustekniikka' where tutkintotunnus = '358412';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('357202', 'Kaukolämpöasentajan erikoisammattitutkinto', 'Fjärrvärmemontör, specialyrkesexamen', '1997-01-01', null, '501');
update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '324301';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '334102';

update tutkinto set voimassa_alkupvm='1999-01-01', nimi_fi='Tieto- ja kirjastopalveluiden ammattitutkinto' where tutkintotunnus = '334113';

update tutkinto set voimassa_alkupvm='1997-01-01' where tutkintotunnus = '357103';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('354199', 'Muu tai tuntematon kone- ja metallialan ammattitutkinto', 'Annan eller okänd yrkesexamen inom maskin- och metallbranschen', '1997-01-01', null, '502');
update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Byggnadsarbetare, specialyrkesexamen' where tutkintotunnus = '358203';

insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('355299', 'Muu tai tuntematon rakennus- ja yhdyskunta-alan ammattitutkinto', 'Annan eller okänd yrkesexamen inom byggnadsbranschen', '1997-01-01', null, '501');
insert into tutkinto_tmp (tutkintotunnus, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, opintoala) values ('367399', 'Muu tai tuntematon metsätalouden erikoisammattitutkinto', 'Annan eller okänd specialyrkesexamen inom skogsbruk', '1997-01-01', null, '604');
update tutkinto set voimassa_loppupvm='2014-12-24', voimassa_alkupvm='1997-01-01', nimi_sv='Mediebransch, specialyrkesexamen' where tutkintotunnus = '327601';

insert into tutkinto (tutkintotunnus, opintoala, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm)
select t.tutkintotunnus, t.opintoala, t.nimi_fi, t.nimi_sv, t.voimassa_alkupvm, t.voimassa_loppupvm from tutkinto_tmp t
join opintoala o on t.opintoala = o.opintoalatunnus;
drop table tutkinto_tmp;
