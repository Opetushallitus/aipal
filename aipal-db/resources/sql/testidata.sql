set session aipal.kayttaja='JARJESTELMA';

insert into koulutusala (koulutusalatunnus, nimi_fi, nimi_sv)
values ('5', 'Tekniikan ja liikenteen ala', 'Teknik och kommunikation');

insert into koulutusala (koulutusalatunnus, nimi_fi, nimi_sv)
 values ('7', 'Sosiaali-, terveys- ja liikunta-ala', 'Social-, hälso- och idrottsområdet');

insert into opintoala (opintoalatunnus, koulutusala, nimi_fi, nimi_sv)
  values ('509', '5', 'Ajoneuvo- ja kuljetustekniikka', 'Fordons- och transportteknik');

insert into opintoala (opintoalatunnus, koulutusala, nimi_fi, nimi_sv)
  values ('702', '7', 'Terveysala', 'Hälsoområdet');

insert into tutkinto (tutkintotunnus, opintoala, nimi_fi, nimi_sv, tutkintotyyppi)
  values ('064122', '509', 'Insinööri, kuljetustekniikka', 'Ingenjör, transportteknik', '12');
  
insert into tutkinto (tutkintotunnus, opintoala, nimi_fi, nimi_sv, tutkintotyyppi)
  values ('056115', '702', 'Sairaanhoitaja, lasten sairaanhoito', 'Sjukskötare, barnsjukvård', '06');
 
 

insert into koulutustoimija (ytunnus, nimi_fi, nimi_sv)
  values ('2325448-4', 'Hikimäen urheiluopisto', 'Hikibacka iddrotsklubben');
  
insert into oppilaitos (oppilaitoskoodi, koulutustoimija, nimi_fi, nimi_sv)
 values ('46572', '2325448-4', 'Hikipajan koulu', 'Hikiklubben skola');

insert into oppilaitos (oppilaitoskoodi, koulutustoimija, nimi_fi, oppilaitostyyppi)
 values ('99991', '9876543-2', 'Testi-Oppilaitos (AMK)', '41');



insert into kysely (kyselyid, voimassa_alkupvm, nimi_fi, nimi_sv, selite_fi, koulutustoimija, tila)
  values (-1, to_date('2016-01-04', 'YYYY-MM-DD'), 'testikysely', 'test frågande', '-', '2345678-0', 'julkaistu');

insert into kyselykerta (kyselykertaid, kyselyid, nimi, voimassa_alkupvm, lukittu)
  values (-1, -1, 'testikerta', to_date('2016-01-04', 'YYYY-MM-DD'), false);

insert into kysely_kysymysryhma (kyselyid, kysymysryhmaid, jarjestys)
  values (-1, 3341884, 0),
   	     (-1, 3341886, 1);

insert into kysely_kysymys (kyselyid, kysymysid)
  values (-1, 7312034),
         (-1, 7312035),
         (-1, 7312036),
         (-1, 7312037),
         (-1, 7312038),
         (-1, 7312039),
         (-1, 7312040),
         (-1, 7312017),
         (-1, 7312018),
         (-1, 7312019),
         (-1, 7312020),
         (-1, 7312021),
         (-1, 7312022),
         (-1, 7312023),
         (-1, 7312024),
         (-1, 7312025),
         (-1, 7312026);
                  

insert into vastaajatunnus (vastaajatunnusid, kyselykertaid, rahoitusmuotoid, tutkintotunnus, tunnus, vastaajien_lkm, lukittu, voimassa_alkupvm, suorituskieli)
  values (-1, -1, 1, 'X00001', '3CXHJF', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (-2, -1, 1, 'X00001', 'JMHYJE', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (-3, -1, 1, null, 'JYTA7A', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
	     (-4, -1, 1, null, 'CCEMTC', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (-5, -1, 1, null, 'RFMC7H', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'sv');
   	     
insert into vastaaja (vastaajaid, kyselykertaid, vastaajatunnusid, vastannut)
  values (-1, -1, -1, true),
   	     (-2, -1, -2, true),
         (-3, -1, -3, true),
         (-4, -1, -4, true),
         (-5, -1, -5, true);

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312034,-1,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312035,-1,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312036,-1,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312037,-1,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312038,-1,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312039,-1,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312040,-1,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312017,-1,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312018,-1,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312019,-1,to_date('2016-02-04', 'YYYY-MM-DD'),5, null),
	(7312020,-1,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),		
	(7312021,-1,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312022,-1,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312023,-1,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312024,-1,to_date('2016-02-04', 'YYYY-MM-DD'),null,'ei'),		
	(7312025,-1,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312026,-1,to_date('2016-02-04', 'YYYY-MM-DD'),5, null);

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312034,-2,to_date('2016-02-05', 'YYYY-MM-DD'),0, null),
	(7312035,-2,to_date('2016-02-05', 'YYYY-MM-DD'),0, null),
	(7312036,-2,to_date('2016-02-05', 'YYYY-MM-DD'),1, null),
	(7312037,-2,to_date('2016-02-05', 'YYYY-MM-DD'),1, null),
	(7312038,-2,to_date('2016-02-05', 'YYYY-MM-DD'),1, null),
	(7312039,-2,to_date('2016-02-05', 'YYYY-MM-DD'),2, null),
	(7312040,-2,to_date('2016-02-05', 'YYYY-MM-DD'),0, null),
	(7312017,-2,to_date('2016-02-05', 'YYYY-MM-DD'),2, null),
	(7312018,-2,to_date('2016-02-05', 'YYYY-MM-DD'),1, null),
	(7312019,-2,to_date('2016-02-05', 'YYYY-MM-DD'),5, null),
	(7312020,-2,to_date('2016-02-05', 'YYYY-MM-DD'),4, null),
	(7312021,-2,to_date('2016-02-05', 'YYYY-MM-DD'),null, 'kylla'),
	(7312022,-2,to_date('2016-02-05', 'YYYY-MM-DD'),null, 'ei'),
	(7312023,-2,to_date('2016-02-05', 'YYYY-MM-DD'),1, null),
	(7312024,-2,to_date('2016-02-05', 'YYYY-MM-DD'),null,'ei'),
	(7312025,-2,to_date('2016-02-05', 'YYYY-MM-DD'),4, null),
	(7312026,-2,to_date('2016-02-05', 'YYYY-MM-DD'),5, null);

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312034,-3,to_date('2016-02-07', 'YYYY-MM-DD'),0, null),
	(7312035,-3,to_date('2016-02-07', 'YYYY-MM-DD'),0, null),
	(7312036,-3,to_date('2016-02-07', 'YYYY-MM-DD'),1, null),
	(7312037,-3,to_date('2016-02-07', 'YYYY-MM-DD'),1, null),
	(7312038,-3,to_date('2016-02-07', 'YYYY-MM-DD'),1, null),
	(7312039,-3,to_date('2016-02-07', 'YYYY-MM-DD'),2, null),
	(7312040,-3,to_date('2016-02-07', 'YYYY-MM-DD'),0, null),
	(7312017,-3,to_date('2016-02-07', 'YYYY-MM-DD'),2, null),
	(7312018,-3,to_date('2016-02-07', 'YYYY-MM-DD'),1, null),
	(7312019,-3,to_date('2016-02-07', 'YYYY-MM-DD'),5, null),
	(7312020,-3,to_date('2016-02-07', 'YYYY-MM-DD'),4, null),
	(7312021,-3,to_date('2016-02-07', 'YYYY-MM-DD'),null, 'ei'),
	(7312022,-3,to_date('2016-02-07', 'YYYY-MM-DD'),null, 'kylla'),
	(7312023,-3,to_date('2016-02-07', 'YYYY-MM-DD'),1, null),
	(7312024,-3,to_date('2016-02-07', 'YYYY-MM-DD'),null,'ei'),
	(7312025,-3,to_date('2016-02-07', 'YYYY-MM-DD'),4, null),
	(7312026,-3,to_date('2016-02-07', 'YYYY-MM-DD'),5, null);
	

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312034,-4,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312035,-4,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312036,-4,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312037,-4,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312038,-4,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312039,-4,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312040,-4,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312017,-4,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312018,-4,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312019,-4,to_date('2016-02-04', 'YYYY-MM-DD'),5, null),
	(7312020,-4,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),		
	(7312021,-4,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312022,-4,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312023,-4,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312024,-4,to_date('2016-02-04', 'YYYY-MM-DD'),null,'ei'),		
	(7312025,-4,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312026,-4,to_date('2016-02-04', 'YYYY-MM-DD'),3, null);

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312034,-5,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312035,-5,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312036,-5,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312037,-5,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312038,-5,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312039,-5,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312040,-5,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312017,-5,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312018,-5,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312019,-5,to_date('2016-02-04', 'YYYY-MM-DD'),5, null),
	(7312020,-5,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),		
	(7312021,-5,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312022,-5,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312023,-5,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312024,-5,to_date('2016-02-04', 'YYYY-MM-DD'),null,'ei'),		
	(7312025,-5,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312026,-5,to_date('2016-02-04', 'YYYY-MM-DD'),2, null);

-- toinen kysely valtakunnallista vertailutietoa varten

insert into kysely (kyselyid, voimassa_alkupvm, nimi_fi, nimi_sv, selite_fi, koulutustoimija, tila)
  values (-2, to_date('2016-01-04', 'YYYY-MM-DD'), 'testikysely', 'test frågande', '-', '2325448-4', 'julkaistu');

insert into kysely_kysymys (kyselyid, kysymysid)
  values (-2, 7312034),
         (-2, 7312035),
         (-2, 7312036),
         (-2, 7312037),
         (-2, 7312038),
         (-2, 7312039),
         (-2, 7312040),
         (-2, 7312017),
         (-2, 7312018),
         (-2, 7312019),
         (-2, 7312020),
         (-2, 7312021),
         (-2, 7312022),
         (-2, 7312023),
         (-2, 7312024),
         (-2, 7312025),
         (-2, 7312026);
                  

insert into kysely_kysymysryhma (kyselyid, kysymysryhmaid, jarjestys)
  values (-2, 3341884, 0),
   	     (-2, 3341886, 1);

insert into kyselykerta (kyselykertaid, kyselyid, nimi, voimassa_alkupvm, lukittu)
  values (-2, -2, 'testikerta', to_date('2016-01-04', 'YYYY-MM-DD'), false);

-- kyselykertaid sekvenssi alkaa numerosta 1 kun kanta luodaan.
insert into vastaajatunnus (vastaajatunnusid, kyselykertaid, rahoitusmuotoid, tutkintotunnus, tunnus, vastaajien_lkm, lukittu, voimassa_alkupvm, suorituskieli)
  values (-6, -2, 1, 'X00001', '3XXHJF', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (-7, -2, 1, 'X00001', 'JXHYJE', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (-8, -2, 1, 'X00001', 'JXTA7A', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
	     (-9, -2, 1, null, 'CXEMTC', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (-10, -2, 1, 'X00001', 'RXMC7H', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'sv');
   	     
insert into vastaaja (vastaajaid, kyselykertaid, vastaajatunnusid, vastannut)
  values (-10, -2, -6, true),
   	     (-20, -2, -7, true),
         (-30, -2, -8, true),
         (-40, -2, -9, true),
         (-50, -2, -10, true);


insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312034,-10,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312035,-10,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312036,-10,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312037,-10,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312038,-10,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312039,-10,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312040,-10,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312017,-10,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312018,-10,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312019,-10,to_date('2016-02-04', 'YYYY-MM-DD'),5, null),
	(7312020,-10,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),		
	(7312021,-10,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312022,-10,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312023,-10,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312024,-10,to_date('2016-02-04', 'YYYY-MM-DD'),null,'ei'),		
	(7312025,-10,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312026,-10,to_date('2016-02-04', 'YYYY-MM-DD'),4, null);

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312034,-20,to_date('2016-02-05', 'YYYY-MM-DD'),0, null),
	(7312035,-20,to_date('2016-02-05', 'YYYY-MM-DD'),0, null),
	(7312036,-20,to_date('2016-02-05', 'YYYY-MM-DD'),1, null),
	(7312037,-20,to_date('2016-02-05', 'YYYY-MM-DD'),1, null),
	(7312038,-20,to_date('2016-02-05', 'YYYY-MM-DD'),1, null),
	(7312039,-20,to_date('2016-02-05', 'YYYY-MM-DD'),2, null),
	(7312040,-20,to_date('2016-02-05', 'YYYY-MM-DD'),0, null),
	(7312017,-20,to_date('2016-02-05', 'YYYY-MM-DD'),2, null),
	(7312018,-20,to_date('2016-02-05', 'YYYY-MM-DD'),1, null),
	(7312019,-20,to_date('2016-02-05', 'YYYY-MM-DD'),5, null),
	(7312020,-20,to_date('2016-02-05', 'YYYY-MM-DD'),4, null),
	(7312021,-20,to_date('2016-02-05', 'YYYY-MM-DD'),null, 'kylla'),
	(7312022,-20,to_date('2016-02-05', 'YYYY-MM-DD'),null, 'ei'),
	(7312023,-20,to_date('2016-02-05', 'YYYY-MM-DD'),1, null),
	(7312024,-20,to_date('2016-02-05', 'YYYY-MM-DD'),null,'ei'),
	(7312025,-20,to_date('2016-02-05', 'YYYY-MM-DD'),4, null),
	(7312026,-20,to_date('2016-02-05', 'YYYY-MM-DD'),2, null);

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312034,-30,to_date('2016-02-07', 'YYYY-MM-DD'),0, null),
	(7312035,-30,to_date('2016-02-07', 'YYYY-MM-DD'),0, null),
	(7312036,-30,to_date('2016-02-07', 'YYYY-MM-DD'),1, null),
	(7312037,-30,to_date('2016-02-07', 'YYYY-MM-DD'),1, null),
	(7312038,-30,to_date('2016-02-07', 'YYYY-MM-DD'),1, null),
	(7312039,-30,to_date('2016-02-07', 'YYYY-MM-DD'),2, null),
	(7312040,-30,to_date('2016-02-07', 'YYYY-MM-DD'),0, null),
	(7312017,-30,to_date('2016-02-07', 'YYYY-MM-DD'),2, null),
	(7312018,-30,to_date('2016-02-07', 'YYYY-MM-DD'),1, null),
	(7312019,-30,to_date('2016-02-07', 'YYYY-MM-DD'),5, null),
	(7312020,-30,to_date('2016-02-07', 'YYYY-MM-DD'),4, null),
	(7312021,-30,to_date('2016-02-07', 'YYYY-MM-DD'),null, 'ei'),
	(7312022,-30,to_date('2016-02-07', 'YYYY-MM-DD'),null, 'kylla'),
	(7312023,-30,to_date('2016-02-07', 'YYYY-MM-DD'),1, null),
	(7312024,-30,to_date('2016-02-07', 'YYYY-MM-DD'),null,'ei'),
	(7312025,-30,to_date('2016-02-07', 'YYYY-MM-DD'),4, null),
	(7312026,-30,to_date('2016-02-07', 'YYYY-MM-DD'),1, null);
	

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312034,-40,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312035,-40,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312036,-40,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312037,-40,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312038,-40,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312039,-40,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312040,-40,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312017,-40,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312018,-40,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312019,-40,to_date('2016-02-04', 'YYYY-MM-DD'),5, null),
	(7312020,-40,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),		
	(7312021,-40,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312022,-40,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312023,-40,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312024,-40,to_date('2016-02-04', 'YYYY-MM-DD'),null,'ei'),		
	(7312025,-40,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312026,-40,to_date('2016-02-04', 'YYYY-MM-DD'),3, null);

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312034,-50,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312035,-50,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312036,-50,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312037,-50,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312038,-50,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312039,-50,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312040,-50,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312017,-50,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312018,-50,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312019,-50,to_date('2016-02-04', 'YYYY-MM-DD'),5, null),
	(7312020,-50,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),		
	(7312021,-50,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312022,-50,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312023,-50,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312024,-50,to_date('2016-02-04', 'YYYY-MM-DD'),null,'ei'),		
	(7312025,-50,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312026,-50,to_date('2016-02-04', 'YYYY-MM-DD'),2, null);
	
	
-- suoritusvaiheen kysely 

insert into kysely (kyselyid, voimassa_alkupvm, nimi_fi, nimi_sv, selite_fi, koulutustoimija, tila)
  values (-3, to_date('2016-01-04', 'YYYY-MM-DD'), 'testikysely, suoritusvaihe', 'test frågande, görande', '-', '2325448-4', 'julkaistu');

insert into kysely_kysymys (kyselyid, kysymysid)
  values (-3, 7312027),
         (-3, 7312028),
         (-3, 7312029),
         (-3, 7312030),
         (-3, 7312031),
         (-3, 7312032),
         (-3, 7312033),
         (-3, 7312008),
         (-3, 7312009),
         (-3, 7312010),
         (-3, 7312011),
         (-3, 7312012),
         (-3, 7312013),
         (-3, 7312014),
         (-3, 7312015),
         (-3, 7312016);
                  

insert into kysely_kysymysryhma (kyselyid, kysymysryhmaid, jarjestys)
  values (-3, 3341885, 0),
   	     (-3, 3341887, 1);

insert into kyselykerta (kyselykertaid, kyselyid, nimi, voimassa_alkupvm, lukittu)
  values (-3, -3, 'testikerta', to_date('2016-01-04', 'YYYY-MM-DD'), false);

insert into vastaajatunnus (vastaajatunnusid, kyselykertaid, rahoitusmuotoid, tutkintotunnus, tunnus, vastaajien_lkm, lukittu, voimassa_alkupvm, suorituskieli)
  values (-11, -3, 1, 'X00002', 'AXXHJF', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (-12, -3, 1, 'X00002', 'AXHYJE', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (-13, -3, 1, 'X00002', 'AXTA7A', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
	     (-14, -3, 1, null, 'AXEMTC', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (-15, -3, 1, 'X00002', 'AXMC7H', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'sv');
   	     
insert into vastaaja (vastaajaid, kyselykertaid, vastaajatunnusid, vastannut)
  values (-100, -3, -11, true),
   	     (-101, -3, -12, true),
         (-102, -3, -13, true),
         (-103, -3, -14, true),
         (-104, -3, -15, true);
  

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312027,-100,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312028, -100,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312029, -100,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312030,-100,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312031,-100,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312032,-100,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312033,-100,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312008,-100,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312009,-100,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312010,-100,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312011,-100,to_date('2016-02-04', 'YYYY-MM-DD'),3, null),		
	(7312012,-100,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312013,-100,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312014,-100,to_date('2016-02-04', 'YYYY-MM-DD'),3, null),
	(7312015,-100,to_date('2016-02-04', 'YYYY-MM-DD'), 4, null),		
	(7312016,-100,to_date('2016-02-04', 'YYYY-MM-DD'), 5, null);
	
insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312027,-101,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312028, -101,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312029, -101,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312030,-101,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312031,-101,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312032,-101,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312033,-101,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312008,-101,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312009,-101,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312010,-101,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312011,-101,to_date('2016-02-04', 'YYYY-MM-DD'),3, null),		
	(7312012,-101,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312013,-101,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312014,-101,to_date('2016-02-04', 'YYYY-MM-DD'),3, null),
	(7312015,-101,to_date('2016-02-04', 'YYYY-MM-DD'), 4, null),		
	(7312016,-101,to_date('2016-02-04', 'YYYY-MM-DD'), 5, null);

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312027,-102,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312028, -102,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312029, -102,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312030,-102,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312031,-102,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312032,-102,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312033,-102,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312008,-102,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312009,-102,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312010,-102,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312011,-102,to_date('2016-02-04', 'YYYY-MM-DD'),3, null),		
	(7312012,-102,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312013,-102,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312014,-102,to_date('2016-02-04', 'YYYY-MM-DD'),3, null),
	(7312015,-102,to_date('2016-02-04', 'YYYY-MM-DD'), 4, null),		
	(7312016,-102,to_date('2016-02-04', 'YYYY-MM-DD'), 1, null);

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312027,-103,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312028, -103,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312029, -103,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312030,-103,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312031,-103,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312032,-103,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312033,-103,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312008,-103,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312009,-103,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312010,-103,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312011,-103,to_date('2016-02-04', 'YYYY-MM-DD'),3, null),		
	(7312012,-103,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312013,-103,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312014,-103,to_date('2016-02-04', 'YYYY-MM-DD'),3, null),
	(7312015,-103,to_date('2016-02-04', 'YYYY-MM-DD'), 4, null),		
	(7312016,-103,to_date('2016-02-04', 'YYYY-MM-DD'), 3, null);


insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312027,-104,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312028, -104,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312029, -104,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312030,-104,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312031,-104,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312032,-104,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312033,-104,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312008,-104,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312009,-104,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312010,-104,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312011,-104,to_date('2016-02-04', 'YYYY-MM-DD'),3, null),		
	(7312012,-104,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312013,-104,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312014,-104,to_date('2016-02-04', 'YYYY-MM-DD'),3, null),
	(7312015,-104,to_date('2016-02-04', 'YYYY-MM-DD'), 4, null),		
	(7312016,-104,to_date('2016-02-04', 'YYYY-MM-DD'), 2, null);


-- vanhat taustakysmyykset ovat id-numeroilla 1..4 joten resetoidaan sekvenssi
ALTER sequence kysymysryhma_kysymysryhmaid_seq RESTART with 10;


-- Testikysely, jossa on omia koulutuksen järjestäjän kysymyksiä mukana
INSERT INTO kysymysryhma (kysymysryhmaid, voimassa_alkupvm, voimassa_loppupvm, taustakysymykset, valtakunnallinen, nimi_fi, nimi_sv, selite_fi, selite_sv, koulutustoimija, oppilaitos, toimipaikka, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, tila, kuvaus_fi, kuvaus_sv, ntm_kysymykset)
  VALUES (-1010, NULL, NULL, false, false, 'Omat kyssät', NULL, NULL, NULL, '2345678-0', NULL, NULL, 'OID.T-1001', 'OID.T-1001', '2016-06-07 08:54:17.726058+00', '2016-06-07 08:54:22.100875+00', 'julkaistu', NULL, NULL, false);

INSERT INTO kysymys (kysymysid, pakollinen, poistettava, vastaustyyppi, kysymysryhmaid, kysymys_fi, kysymys_sv, jarjestys, jatkokysymysid, monivalinta_max, max_vastaus, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, eos_vastaus_sallittu)
  VALUES (-1001, true, false, 'kylla_ei_valinta', -1010, 'Onko koulussa käytävä?', '', 0, NULL, NULL, NULL, 'OID.T-1001', 'OID.T-1001', '2016-06-07 08:54:17.726058+00', '2016-06-07 08:54:17.726058+00', true);
INSERT INTO kysymys (kysymysid, pakollinen, poistettava, vastaustyyppi, kysymysryhmaid, kysymys_fi, kysymys_sv, jarjestys, jatkokysymysid, monivalinta_max, max_vastaus, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, eos_vastaus_sallittu)
  VALUES (-1002, true, false, 'vapaateksti', -1010, 'Wo kan man einen angenehmende nacht werbringen?', '', 1, NULL, NULL, 1000, 'OID.T-1001', 'OID.T-1001', '2016-06-07 08:54:17.726058+00', '2016-06-07 08:54:17.726058+00', NULL);


INSERT INTO kysely (kyselyid, voimassa_alkupvm, voimassa_loppupvm, nimi_fi, nimi_sv, selite_fi, selite_sv, koulutustoimija, oppilaitos, toimipaikka, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, tila)
  VALUES (-1001, '2016-06-01', NULL, 'Uteliaiden ryhmäkysely', NULL, NULL, NULL, '2345678-0', NULL, NULL, 'OID.T-1001', 'OID.T-1001', '2016-06-07 08:55:53.249328+00', '2016-06-07 08:55:58.271004+00', 'julkaistu');
INSERT INTO kysely (kyselyid, voimassa_alkupvm, voimassa_loppupvm, nimi_fi, nimi_sv, selite_fi, selite_sv, koulutustoimija, oppilaitos, toimipaikka, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, tila)
  VALUES (-1002, '2016-06-07', NULL, 'Utelointi', NULL, NULL, NULL, '2345678-0', NULL, NULL, 'OID.T-1001', 'OID.T-1001', '2016-06-07 09:21:53.248571+00', '2016-06-07 09:21:57.150769+00', 'julkaistu');

insert into kysely_kysymysryhma (kyselyid, kysymysryhmaid, jarjestys)
  values (-1001, -1010, 0),
         (-1002, 3341884, 0),
         (-1002, -1010, 1);

INSERT INTO kyselykerta (kyselykertaid, kyselyid, nimi, voimassa_alkupvm, voimassa_loppupvm, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, lukittu)
  VALUES (-1001, -1001, 'Utelo', '2016-06-01', NULL, 'OID.T-1001', 'OID.T-1001', '2016-06-07 08:56:09.790164+00', '2016-06-07 08:56:09.790164+00', false);
INSERT INTO kyselykerta (kyselykertaid, kyselyid, nimi, voimassa_alkupvm, voimassa_loppupvm, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, lukittu)
  VALUES (-1002, -1002, 'Kiinnostaa.jpeg', '2016-06-07', NULL, 'OID.T-1001', 'OID.T-1001', '2016-06-07 09:22:13.206293+00', '2016-06-07 09:22:13.206293+00', false);

INSERT INTO vastaajatunnus (vastaajatunnusid, kyselykertaid, rahoitusmuotoid, tutkintotunnus, tunnus, vastaajien_lkm, lukittu, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, voimassa_alkupvm, voimassa_loppupvm, valmistavan_koulutuksen_oppilaitos, suorituskieli)
  VALUES (-1001, -1001, 5, 'X00001', 'MWL7H7', 1, false, 'OID.T-1001', 'OID.T-1001', '2016-06-07 08:56:25.782641+00', '2016-06-07 08:56:25.782641+00', '2016-06-07', NULL, NULL, 'fi');
INSERT INTO vastaajatunnus (vastaajatunnusid, kyselykertaid, rahoitusmuotoid, tutkintotunnus, tunnus, vastaajien_lkm, lukittu, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, voimassa_alkupvm, voimassa_loppupvm, valmistavan_koulutuksen_oppilaitos, suorituskieli)
  VALUES (-1002, -1002, 4, 'X00001', '37CJMJ', 1, false, 'OID.T-1001', 'OID.T-1001', '2016-06-07 09:22:21.136518+00', '2016-06-07 09:22:21.136518+00', '2016-06-07', NULL, NULL, 'fi');


INSERT INTO vastaaja (vastaajaid, kyselykertaid, vastaajatunnusid, vastannut, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika)
  VALUES (-1001, -1001, -1001, true, 'VASTAAJA', 'VASTAAJA', '2016-06-07 09:02:34.804066+00', '2016-06-07 09:02:34.804066+00');
INSERT INTO vastaaja (vastaajaid, kyselykertaid, vastaajatunnusid, vastannut, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika)
  VALUES (-1002, -1002, -1002, true, 'VASTAAJA', 'VASTAAJA', '2016-06-07 09:23:14.083583+00', '2016-06-07 09:23:14.083583+00');

INSERT INTO vastaus (vastausid, kysymysid, vastaajaid, vastausaika, vapaateksti, numerovalinta, vaihtoehto, jatkovastausid, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, en_osaa_sanoa)
  VALUES (-100251, -1001, -1001, '2016-06-07', NULL, NULL, 'kylla', NULL, 'VASTAAJA', 'VASTAAJA', '2016-06-07 09:02:34.804066+00', '2016-06-07 09:02:34.804066+00', false);
INSERT INTO vastaus (vastausid, kysymysid, vastaajaid, vastausaika, vapaateksti, numerovalinta, vaihtoehto, jatkovastausid, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, en_osaa_sanoa)
  VALUES (-100252, -1002, -1001, '2016-06-07', 'Ich weiss nicht.', NULL, NULL, NULL, 'VASTAAJA', 'VASTAAJA', '2016-06-07 09:02:34.804066+00', '2016-06-07 09:02:34.804066+00', false);
INSERT INTO vastaus (vastausid, kysymysid, vastaajaid, vastausaika, vapaateksti, numerovalinta, vaihtoehto, jatkovastausid, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, en_osaa_sanoa)
  VALUES (-100260, -1001, -1002, '2016-06-07', NULL, NULL, 'kylla', NULL, 'VASTAAJA', 'VASTAAJA', '2016-06-07 09:23:14.083583+00', '2016-06-07 09:23:14.083583+00', false);
INSERT INTO vastaus (vastausid, kysymysid, vastaajaid, vastausaika, vapaateksti, numerovalinta, vaihtoehto, jatkovastausid, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, en_osaa_sanoa)
  VALUES (-100261, -1002, -1002, '2016-06-07', 'Ich weiss nicht.', NULL, NULL, NULL, 'VASTAAJA', 'VASTAAJA', '2016-06-07 09:23:14.083583+00', '2016-06-07 09:23:14.083583+00', false);
INSERT INTO vastaus (vastausid, kysymysid, vastaajaid, vastausaika, vapaateksti, numerovalinta, vaihtoehto, jatkovastausid, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, en_osaa_sanoa)
  VALUES (-100253, 7312034, -1002, '2016-06-07', NULL, 1, NULL, NULL, 'VASTAAJA', 'VASTAAJA', '2016-06-07 09:23:14.083583+00', '2016-06-07 09:23:14.083583+00', false);
INSERT INTO vastaus (vastausid, kysymysid, vastaajaid, vastausaika, vapaateksti, numerovalinta, vaihtoehto, jatkovastausid, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, en_osaa_sanoa)
  VALUES (-100254, 7312035, -1002, '2016-06-07', NULL, 1, NULL, NULL, 'VASTAAJA', 'VASTAAJA', '2016-06-07 09:23:14.083583+00', '2016-06-07 09:23:14.083583+00', false);
INSERT INTO vastaus (vastausid, kysymysid, vastaajaid, vastausaika, vapaateksti, numerovalinta, vaihtoehto, jatkovastausid, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, en_osaa_sanoa)
  VALUES (-100255, 7312036, -1002, '2016-06-07', NULL, 1, NULL, NULL, 'VASTAAJA', 'VASTAAJA', '2016-06-07 09:23:14.083583+00', '2016-06-07 09:23:14.083583+00', false);
INSERT INTO vastaus (vastausid, kysymysid, vastaajaid, vastausaika, vapaateksti, numerovalinta, vaihtoehto, jatkovastausid, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, en_osaa_sanoa)
  VALUES (-100256, 7312037, -1002, '2016-06-07', NULL, 0, NULL, NULL, 'VASTAAJA', 'VASTAAJA', '2016-06-07 09:23:14.083583+00', '2016-06-07 09:23:14.083583+00', false);
INSERT INTO vastaus (vastausid, kysymysid, vastaajaid, vastausaika, vapaateksti, numerovalinta, vaihtoehto, jatkovastausid, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, en_osaa_sanoa)
  VALUES (-100257, 7312038, -1002, '2016-06-07', NULL, 1, NULL, NULL, 'VASTAAJA', 'VASTAAJA', '2016-06-07 09:23:14.083583+00', '2016-06-07 09:23:14.083583+00', false);
INSERT INTO vastaus (vastausid, kysymysid, vastaajaid, vastausaika, vapaateksti, numerovalinta, vaihtoehto, jatkovastausid, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, en_osaa_sanoa)
  VALUES (-100258, 7312039, -1002, '2016-06-07', NULL, 1, NULL, NULL, 'VASTAAJA', 'VASTAAJA', '2016-06-07 09:23:14.083583+00', '2016-06-07 09:23:14.083583+00', false);
INSERT INTO vastaus (vastausid, kysymysid, vastaajaid, vastausaika, vapaateksti, numerovalinta, vaihtoehto, jatkovastausid, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, en_osaa_sanoa)
  VALUES (-100259, 7312040, -1002, '2016-06-07', NULL, 0, NULL, NULL, 'VASTAAJA', 'VASTAAJA', '2016-06-07 09:23:14.083583+00', '2016-06-07 09:23:14.083583+00', false);

--- päivitetään näkymät, jotta raportointi toimii.
	
REFRESH MATERIALIZED VIEW CONCURRENTLY vastaus_jatkovastaus_valtakunnallinen_view;
REFRESH MATERIALIZED VIEW vastaaja_taustakysymysryhma_view;
REFRESH MATERIALIZED VIEW kysymysryhma_taustakysymysryhma_view;
refresh materialized view vipunen_view;
