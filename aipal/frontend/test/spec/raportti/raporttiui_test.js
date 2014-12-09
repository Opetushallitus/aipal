// Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
//
// This program is free software:  Licensed under the EUPL, Version 1.1 or - as
// soon as they will be approved by the European Commission - subsequent versions
// of the EUPL (the "Licence");
//
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// European Union Public Licence for more details.

'use strict';

describe('raporttiui', function() {
  beforeEach(module('raportti.raporttiui'));

  var RaporttiFunktiot;
  beforeEach(inject(function(_RaporttiFunktiot_) {
    RaporttiFunktiot = _RaporttiFunktiot_;
  }));

  it('tutkinnotHierarkiaksi', function() {
    var tutkinnot = [
      {'voimassa_alkupvm':null,'nimi_sv':'Ingenjör, elkraftsteknik','opintoala_nimi_fi':'Sähkö- ja automaatiotekniikka','voimassa_loppupvm':null,'nimi_fi':'Insinööri, sähkövoimatekniikka','opintoala_nimi_sv':'El- och automationsteknik','koulutusala_nimi_sv':'Teknik och kommunikation','opintoalatunnus':'503','koulutusala_nimi_fi':'Tekniikan ja liikenteen ala','tutkintotunnus':'064151','koulutusalatunnus':'5'},
      {'voimassa_alkupvm':null,'nimi_sv':'Ingenjör, transportteknik','opintoala_nimi_fi':'Ajoneuvo- ja kuljetustekniikka','voimassa_loppupvm':null,'nimi_fi':'Insinööri, kuljetustekniikka','opintoala_nimi_sv':'Fordons- och transportteknik','koulutusala_nimi_sv':'Teknik och kommunikation','opintoalatunnus':'509','koulutusala_nimi_fi':'Tekniikan ja liikenteen ala','tutkintotunnus':'064122','koulutusalatunnus':'5'}
    ];

    var koulutusalat = RaporttiFunktiot.tutkinnotHierarkiaksi(tutkinnot);

    expect(koulutusalat.length).toEqual(1);
    expect(koulutusalat[0].opintoalat.length).toEqual(2);
    expect(koulutusalat[0].opintoalat[0].tutkinnot[0].tutkintotunnus).toEqual('064151');
  });
});