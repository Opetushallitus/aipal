// Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
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

describe('vastaus.vastausui.VastausControllerFunktiot', function() {
  var f;

  beforeEach(module('vastaus.vastausui'));

  beforeEach(inject(function(VastausControllerFunktiot){
    f = VastausControllerFunktiot;
  }));

  describe('kerätään kaikki vastaukset', function() {
    it('Vastatusta kysymyksestä saadaan vastaus', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaus: 1
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:[1]}]});
    });
    it('Kahdesta kysymyksestä, joista vain toiseen vastattu saadaan yksi vastaus', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaus: 1
              },
              {
                kysymysid: 3
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:[1]}]});
    });
    it('Kahdesta kysymyksestä, joista molempiin vastattu saadaan molemmat vastaukset', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaus: 1
              },
              {
                kysymysid: 3,
                vastaus: 'vapaateksti'
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:[1]},{kysymysid:3,vastaus:['vapaateksti']}]});
    });
    it('Monivalintakysymys, jossa maksimi vastausten lukumäärä 1. Tuloksena valittu vastaus', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaustyyppi: 'monivalinta',
                monivalinta_max: 1,
                vastaus: 2,
                monivalintavaihtoehdot: [
                  { jarjestys: 1 },
                  { jarjestys: 2 }
                ]
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:[2]}]});
    });
    it('Monivalintakysymys, jossa molemmat vaihtoehdot valittu. Tuloksena vastaus, jossa valitut vaihtoehdot', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaustyyppi: 'monivalinta',
                monivalinta_max: 3,
                monivalintavaihtoehdot: [
                  { jarjestys: 1, valittu: true },
                  { jarjestys: 2, valittu: true }
                ]
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:[1,2]}]});
    });
    it('Monivalintakysymys, jossa valittu yksi vaihtoehto. Tuloksena vastaus, jossa valittu vaihtoehto', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaustyyppi: 'monivalinta',
                monivalinta_max: 3,
                monivalintavaihtoehdot: [
                  { jarjestys: 1, valittu: true },
                  { jarjestys: 2, valittu: false }
                ]
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:[1]}]});
    });
    it('Eri kysymysryhmien vastatuista kysymyksistä saadaan vastaukset', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaus: 1
              }
            ]
          },
          {
            kysymykset: [
              {
                kysymysid: 3,
                vastaus: 'kylla'
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:[1]},{kysymysid:3,vastaus:['kylla']}]});
    });
  });
});
