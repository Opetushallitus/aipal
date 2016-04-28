set session aipal.kayttaja='JARJESTELMA';

insert into koulutusala (koulutusalatunnus, nimi_fi, nimi_sv)
values ('5', 'Tekniikan ja liikenteen ala', 'Teknik och kommunikation');

insert into koulutusala (koulutusalatunnus, nimi_fi, nimi_sv)
 values ('7', 'Sosiaali-, terveys- ja liikunta-ala', 'Social-, hälso- och idrottsområdet');

insert into opintoala (opintoalatunnus, koulutusala, nimi_fi, nimi_sv)
  values ('509', '5', 'Ajoneuvo- ja kuljetustekniikka', 'Fordons- och transportteknik');

insert into opintoala (opintoalatunnus, koulutusala, nimi_fi, nimi_sv)
  values ('702', '7', 'Terveysala', 'Hälsoområdet');

insert into tutkinto (tutkintotunnus, opintoala, nimi_fi, nimi_sv)
  values ('064122', '509', 'Insinööri, kuljetustekniikka', 'Ingenjör, transportteknik');
  
insert into tutkinto (tutkintotunnus, opintoala, nimi_fi, nimi_sv)
  values ('056115', '702', 'Sairaanhoitaja, lasten sairaanhoito', 'Sjukskötare, barnsjukvård');
 
 

insert into koulutustoimija (ytunnus, nimi_fi, nimi_sv)
  values ('2325448-4', 'Hikimäen urheiluopisto', 'Hikibacka iddrotsklubben');
  
insert into oppilaitos (oppilaitoskoodi, koulutustoimija, nimi_fi, nimi_sv)
 values ('46572', '2325448-4', 'Hikipajan koulu', 'Hikiklubben skola');

-- TODO: kysely - kysymys -liitostaulua ei populoida


insert into kysely (voimassa_alkupvm, nimi_fi, nimi_sv, selite_fi, koulutustoimija, tila)
  values (to_date('2016-01-04', 'YYYY-MM-DD'), 'testikysely', 'test frågande', '-', '2345678-0', 'julkaistu');

-- kyselyid sekvenssi alkaa numerosta 1 kun kanta luodaan.  
insert into kyselykerta (kyselyid, nimi, voimassa_alkupvm, lukittu)
  values (1, 'testikerta', to_date('2016-01-04', 'YYYY-MM-DD'), false);

insert into kysely_kysymysryhma (kyselyid, kysymysryhmaid, jarjestys)
  values (1, 3341884, 0),
   	     (1, 3341886, 1);

insert into kysely_kysymys (kyselyid, kysymysid)
  values (1, 7312034),
         (1, 7312035),
         (1, 7312036),
         (1, 7312037),
         (1, 7312038),
         (1, 7312039),
         (1, 7312040),
         (1, 7312017),
         (1, 7312018),
         (1, 7312019),
         (1, 7312020),
         (1, 7312021),
         (1, 7312022),
         (1, 7312023),
         (1, 7312024),
         (1, 7312025),
         (1, 7312026);
                  

-- kyselykertaid sekvenssi alkaa numerosta 1 kun kanta luodaan.
insert into vastaajatunnus (kyselykertaid, rahoitusmuotoid, tutkintotunnus, tunnus, vastaajien_lkm, lukittu, voimassa_alkupvm, suorituskieli)
  values (1, 1, 'X00001', '3CXHJF', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (1, 1, 'X00001', 'JMHYJE', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (1, 1, null, 'JYTA7A', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
	     (1, 1, null, 'CCEMTC', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (1, 1, null, 'RFMC7H', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'sv');
   	     
insert into vastaaja (vastaajaid, kyselykertaid, vastaajatunnusid, vastannut)
  values (-1, 1, 1, true),
   	     (-2, 1, 2, true),
         (-3, 1, 3, true),
         (-4, 1, 4, true),
         (-5, 1, 5, true);

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
	(7312034,-2,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312035,-2,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312036,-2,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312037,-2,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312038,-2,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312039,-2,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312040,-2,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312017,-2,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312018,-2,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312019,-2,to_date('2016-02-04', 'YYYY-MM-DD'),5, null),
	(7312020,-2,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),		
	(7312021,-2,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312022,-2,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312023,-2,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312024,-2,to_date('2016-02-04', 'YYYY-MM-DD'),null,'ei'),		
	(7312025,-2,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312026,-2,to_date('2016-02-04', 'YYYY-MM-DD'),5, null);

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312034,-3,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312035,-3,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312036,-3,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312037,-3,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312038,-3,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312039,-3,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312040,-3,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312017,-3,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312018,-3,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312019,-3,to_date('2016-02-04', 'YYYY-MM-DD'),5, null),
	(7312020,-3,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),		
	(7312021,-3,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312022,-3,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312023,-3,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312024,-3,to_date('2016-02-04', 'YYYY-MM-DD'),null,'ei'),		
	(7312025,-3,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312026,-3,to_date('2016-02-04', 'YYYY-MM-DD'),5, null);
	

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

insert into kysely (voimassa_alkupvm, nimi_fi, nimi_sv, selite_fi, koulutustoimija, tila)
  values (to_date('2016-01-04', 'YYYY-MM-DD'), 'testikysely', 'test frågande', '-', '2325448-4', 'julkaistu');

insert into kysely_kysymys (kyselyid, kysymysid)
  values (2, 7312034),
         (2, 7312035),
         (2, 7312036),
         (2, 7312037),
         (2, 7312038),
         (2, 7312039),
         (2, 7312040),
         (2, 7312017),
         (2, 7312018),
         (2, 7312019),
         (2, 7312020),
         (2, 7312021),
         (2, 7312022),
         (2, 7312023),
         (2, 7312024),
         (2, 7312025),
         (2, 7312026);
                  

insert into kysely_kysymysryhma (kyselyid, kysymysryhmaid, jarjestys)
  values (2, 3341884, 0),
   	     (2, 3341886, 1);

insert into kyselykerta (kyselyid, nimi, voimassa_alkupvm, lukittu)
  values (2, 'testikerta', to_date('2016-01-04', 'YYYY-MM-DD'), false);

-- kyselykertaid sekvenssi alkaa numerosta 1 kun kanta luodaan.
insert into vastaajatunnus (kyselykertaid, rahoitusmuotoid, tutkintotunnus, tunnus, vastaajien_lkm, lukittu, voimassa_alkupvm, suorituskieli)
  values (2, 1, 'X00001', '3XXHJF', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (2, 1, 'X00001', 'JXHYJE', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (2, 1, 'X00001', 'JXTA7A', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
	     (2, 1, null, 'CXEMTC', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'fi'),
   	     (2, 1, 'X00001', 'RXMC7H', 1, false, to_date('2016-01-04', 'YYYY-MM-DD'), 'sv');
   	     
insert into vastaaja (vastaajaid, kyselykertaid, vastaajatunnusid, vastannut)
  values (-10, 2, 6, true),
   	     (-20, 2, 7, true),
         (-30, 2, 8, true),
         (-40, 2, 9, true),
         (-50, 2, 10, true);


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
	(7312034,-20,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312035,-20,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312036,-20,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312037,-20,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312038,-20,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312039,-20,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312040,-20,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312017,-20,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312018,-20,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312019,-20,to_date('2016-02-04', 'YYYY-MM-DD'),5, null),
	(7312020,-20,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),		
	(7312021,-20,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312022,-20,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312023,-20,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312024,-20,to_date('2016-02-04', 'YYYY-MM-DD'),null,'ei'),		
	(7312025,-20,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312026,-20,to_date('2016-02-04', 'YYYY-MM-DD'),2, null);

insert into vastaus (kysymysid, vastaajaid, vastausaika, numerovalinta, vaihtoehto)
 values 
	(7312034,-30,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312035,-30,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),
	(7312036,-30,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),		
	(7312037,-30,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312038,-30,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312039,-30,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312040,-30,to_date('2016-02-04', 'YYYY-MM-DD'),0, null),		
	(7312017,-30,to_date('2016-02-04', 'YYYY-MM-DD'),2, null),
	(7312018,-30,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312019,-30,to_date('2016-02-04', 'YYYY-MM-DD'),5, null),
	(7312020,-30,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),		
	(7312021,-30,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'ei'),
	(7312022,-30,to_date('2016-02-04', 'YYYY-MM-DD'),null, 'kylla'),
	(7312023,-30,to_date('2016-02-04', 'YYYY-MM-DD'),1, null),
	(7312024,-30,to_date('2016-02-04', 'YYYY-MM-DD'),null,'ei'),		
	(7312025,-30,to_date('2016-02-04', 'YYYY-MM-DD'),4, null),
	(7312026,-30,to_date('2016-02-04', 'YYYY-MM-DD'),1, null);
	

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
	
	
REFRESH MATERIALIZED VIEW CONCURRENTLY vastaus_jatkovastaus_valtakunnallinen_view;
REFRESH MATERIALIZED VIEW vastaaja_taustakysymysryhma_view;
REFRESH MATERIALIZED VIEW kysymysryhma_taustakysymysryhma_view;
