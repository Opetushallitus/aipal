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
  var $httpBackend;
  var $location;
  var ilmoitus;

  beforeEach(module('vastaus.vastausui'));

  beforeEach(module(function($provide){
    $location = {url: jasmine.createSpy('url')};
    $provide.value('$location', $location);

    ilmoitus = {virhe: jasmine.createSpy('virhe')};
    $provide.value('ilmoitus', ilmoitus);

    $provide.value('i18n', {hae: function(){return '';}});
  }));

  beforeEach(inject(function(VastausControllerFunktiot, _$httpBackend_){
    f = VastausControllerFunktiot;
    $httpBackend = _$httpBackend_;
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
                vastaus: 0,
                monivalintavaihtoehdot: [
                  { jarjestys: 0 },
                  { jarjestys: 1 }
                ]
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:[0]}]});
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
                  { jarjestys: 0, valittu: true },
                  { jarjestys: 1, valittu: true }
                ]
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:[0,1]}]});
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
                  { jarjestys: 0, valittu: true },
                  { jarjestys: 1, valittu: false }
                ]
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:[0]}]});
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
    it('Kyllä vastaukselta saadaan vastaus jatkokysymykseen', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaustyyppi: 'kylla_ei_valinta',
                jatkokysymysid: 3,
                vastaus: 'kylla',
                jatkovastaus_kylla: 1
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:['kylla'], jatkokysymysid:3, jatkovastaus_kylla:1}]});
    });
    it('Kyllä vastaukselta saadaan vastaus jatkokysymykseen vaikka ei-vastauksellekin olisi tallennettu jotain', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaustyyppi: 'kylla_ei_valinta',
                jatkokysymysid: 3,
                vastaus: 'kylla',
                jatkovastaus_kylla: 1,
                jatkovastaus_ei: 'vastaus'
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:['kylla'], jatkokysymysid:3, jatkovastaus_kylla:1}]});
    });
    it('Ei vastaukselta saadaan vastaus jatkokysymykseen', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaustyyppi: 'kylla_ei_valinta',
                jatkokysymysid: 3,
                vastaus: 'ei',
                jatkovastaus_ei: 'vastaus'
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:['ei'], jatkokysymysid:3, jatkovastaus_ei:'vastaus'}]});
    });
    it('Ei vastaukselta saadaan vastaus jatkokysymykseen vaikka kyllä-vastauksellekin olisi tallennettu jotain', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaustyyppi: 'kylla_ei_valinta',
                jatkokysymysid: 3,
                vastaus: 'ei',
                jatkovastaus_kylla: 1,
                jatkovastaus_ei: 'vastaus'
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:['ei'], jatkokysymysid:3, jatkovastaus_ei:'vastaus'}]});
    });
    it('kylla vastaukselta saadaan vastaus ilman jatkokysymystä', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaustyyppi: 'kylla_ei_valinta',
                jatkokysymysid: null,
                vastaus: 'kylla'
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:['kylla']}]});
    });
    it('ei vastaukselta saadaan vastaus ilman jatkokysymystä', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaustyyppi: 'kylla_ei_valinta',
                jatkokysymysid: null,
                vastaus: 'ei'
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:['ei']}]});
    });
    it('kylla vastaukselta saadaan vastaus ilman jatkokysymystä, jos jatkokysymykseen ei ole vastattu', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaustyyppi: 'kylla_ei_valinta',
                jatkokysymysid: 3,
                vastaus: 'kylla'
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:['kylla']}]});
    });
    it('ei vastaukselta saadaan vastaus ilman jatkokysymystä, jos jatkokysymykseen ei ole vastattu', function() {
      var vastausdata = {
        kysymysryhmat: [
          {
            kysymykset: [
              {
                kysymysid: 2,
                vastaustyyppi: 'kylla_ei_valinta',
                jatkokysymysid: 3,
                vastaus: 'ei'
              }
            ]
          }
        ]
      };
      expect(f.keraaVastausdata(vastausdata)).toEqual({vastaukset: [{kysymysid:2,vastaus:['ei']}]});
    });
  });

  it('Käyttäjä siirretään kiitos-sivulle, jos vastausten tallennus onnistuu', function(){
    $httpBackend.whenPOST('api/vastaus').respond(200);
    f.tallenna({data: {}, vastausForm: { $setPristine: function() {} }});
    $httpBackend.flush();
    expect($location.url).toHaveBeenCalledWith('/kiitos');
  });

  it('Käyttäjää ei siirretä, jos vastausten tallennus epäonnistuu', function(){
    $httpBackend.whenPOST('api/vastaus').respond(500);
    f.tallenna({data: {}});
    $httpBackend.flush();
    expect($location.url).not.toHaveBeenCalled();
  });

  it('Tallenna-nappi disabloidaan vastausten lähetyksen ajaksi', function(){
    var $scope = {data: {}, tallennaNappiDisabloitu: false};
    f.tallenna($scope);
    expect($scope.tallennaNappiDisabloitu).toBe(true);
  });

  it('Tallenna-nappi enabloidaan palvelinvirheen jälkeen', function(){
    $httpBackend.whenPOST('api/vastaus').respond(500);
    var $scope = {data: {}};
    f.tallenna($scope);
    $httpBackend.flush();
    expect($scope.tallennaNappiDisabloitu).toBe(false);
  });

  it('Ei näytetä virheilmoitusta, jos vastausten tallennus onnistuu', function(){
    $httpBackend.whenPOST('api/vastaus').respond(200);
    f.tallenna({data: {}, vastausForm: { $setPristine: function() {} }});
    $httpBackend.flush();
    expect(ilmoitus.virhe).not.toHaveBeenCalled();
  });

  it('Näytetään virheilmoitus, jos vastausten tallennus epäonnistuu', function(){
    $httpBackend.whenPOST('api/vastaus').respond(500);
    f.tallenna({data: {}});
    $httpBackend.flush();
    expect(ilmoitus.virhe).toHaveBeenCalled();
  });
});
