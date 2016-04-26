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

insert into kysymysryhma (kysymysryhmaid, valtakunnallinen, taustakysymykset, nimi_fi, nimi_sv)
  values (3341884, true, true, 'Näyttötutkintojen taustakysymykset hakeutumisvaiheessa', 'Bakgrundsfrågor gällande fristående examina i ansökningsskedet');

select 'insert into kysymys (kysymysid, pakollinen, vastaustyyppi, kysymys_fi, kysymys_sv, jarjestys, monivalinta_max)' ||
  ' values (' || kysymysid || ' , false, ''monivalinta'', ''' || kysymys_fi || ''', ''' || kysymys_sv || ''', ' || jarjestys
  from kysymys where kysymysryhmaid = 3341884;
  
insert into kysymys (kysymysid, pakollinen, vastaustyyppi, kysymys_fi, kysymys_sv, jarjestys, monivalinta_max) values (7312034 , false, 'monivalinta', 'Sukupuoli', 'Kön', 0, 1);
insert into kysymys (kysymysid, pakollinen, vastaustyyppi, kysymys_fi, kysymys_sv, jarjestys, monivalinta_max) values (7312035 , false, 'monivalinta', 'Äidinkieli', 'Modersmål', 1, 1);
insert into kysymys (kysymysid, pakollinen, vastaustyyppi, kysymys_fi, kysymys_sv, jarjestys, monivalinta_max) values (7312036 , false, 'monivalinta', 'Ikä', 'Ålder', 2, 1);
insert into kysymys (kysymysid, pakollinen, vastaustyyppi, kysymys_fi, kysymys_sv, jarjestys, monivalinta_max) values (7312037 , false, 'monivalinta', 'Korkein koulutus tai tutkinto, jonka olet suorittanut ennen nyt suoritettavaa tutkintoa', 'Högsta utbildning eller examen som du har avlagt före den examen som nu ska avläggas', 3, 1);
insert into kysymys (kysymysid, pakollinen, vastaustyyppi, kysymys_fi, kysymys_sv, jarjestys, monivalinta_max) values (7312038 , false, 'monivalinta', 'Tärkein syy, miksi hakeuduit suorittamaan näyttötutkintoa', 'Viktigaste orsak till att jag sökte till utbildningen', 4, 1);
insert into kysymys (kysymysid, pakollinen, vastaustyyppi, kysymys_fi, kysymys_sv, jarjestys, monivalinta_max) values (7312039 , false, 'monivalinta', 'Mikä oli tilanteesi ennen kuin hakeuduit suorittamaan näyttötutkintoa', 'Vilken var din situation innan du ansökte om att avlägga examen', 5, 1);
insert into kysymys (kysymysid, pakollinen, vastaustyyppi, kysymys_fi, kysymys_sv, jarjestys, monivalinta_max) values (7312040 , false, 'monivalinta', 'Tavoitteeni on', 'Mitt mål är att', 6, 1);


; 3341884;"";"";t;t;"Näyttötutkintojen taustakysymykset hakeutumisvaiheessa";"Bakgrundsfrågor gällande fristående examina i ansökningsskedet"
