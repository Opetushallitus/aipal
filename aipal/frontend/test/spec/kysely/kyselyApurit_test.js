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

describe('kysely.kyselyui.kyselyApurit', function(){

  var apu;

  beforeEach(module('kysely.kyselyui'));

  beforeEach(inject(function(kyselyApurit){
    apu = kyselyApurit;
  }));

  describe('lisaaUniikitKysymysryhmatKyselyyn:', function() {
    it('pitäisi lisätä kysymysryhmä kysymysryhmat taulukkoon', function(){
      var kysely = {
        kysymysryhmat: []
      };
      apu.lisaaUniikitKysymysryhmatKyselyyn(kysely, { kysymysryhmaid: 1 });
      expect(kysely.kysymysryhmat).toEqual([{ kysymysryhmaid: 1 }]);
    });
    it('pitäisi jättää kysymysryhmat ennalleen jos uusi kysymysryhmä on jo taulukossa', function(){
      var kysely = {
        kysymysryhmat: [{ kysymysryhmaid: 1 }]
      };
      apu.lisaaUniikitKysymysryhmatKyselyyn(kysely, { kysymysryhmaid: 1 });
      expect(kysely.kysymysryhmat).toEqual([{ kysymysryhmaid: 1 }]);
    });
    it('pitäisi lisätä uudet kysymysryhmat joita ei ole vielä taulukossa', function(){
      var kysely = {
        kysymysryhmat: [{ kysymysryhmaid: 1 }, { kysymysryhmaid: 2 }]
      };
      apu.lisaaUniikitKysymysryhmatKyselyyn(kysely, [{ kysymysryhmaid: 2 }, { kysymysryhmaid: 3 }]);
      expect(kysely.kysymysryhmat).toEqual([{ kysymysryhmaid: 1 }, { kysymysryhmaid: 2 }, { kysymysryhmaid: 3 }]);
    });
  });

  describe('laskeLisakysymykset:', function() {
    it('pitäisi laskea vain lisäkysymyksiä', function() {
      var kysely = {
        kysymysryhmat: [
          { valtakunnallinen: true, kysymykset: [{kysymysid: 1}, {kysymysid: 2}] },
          { valtakunnallinen: false, kysymykset: [{kysymysid: 3}]}
        ]
      };
      expect(apu.laskeLisakysymykset(kysely)).toEqual(1);
    });
    it('ei pitäisi laskea poistettuja', function() {
      var kysely = {
        kysymysryhmat: [
          { valtakunnallinen: false, kysymykset: [{kysymysid: 1}, {kysymysid: 2, poistettu: true}]}
        ]
      };
      expect(apu.laskeLisakysymykset(kysely)).toEqual(1);
    });
    it('pitäisi laskea kaikki kysymykset kaikista lisäkysymysryhmistä', function() {
      var kysely = {
        kysymysryhmat: [
          { valtakunnallinen: false, kysymykset: [{kysymysid: 1}] },
          { valtakunnallinen: false, kysymykset: [{kysymysid: 2}, {kysymysid: 3}]}
        ]
      };
      expect(apu.laskeLisakysymykset(kysely)).toEqual(3);
    });
  });
});
