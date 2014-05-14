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

describe('Palvelu: jakaumaKaavioApurit', function () {
  beforeEach(module('raportti.kyselykerta.jakaumakaavio'));

  var jakaumaKaavioApurit;
  beforeEach(inject(function(_jakaumaKaavioApurit_) {
    jakaumaKaavioApurit = _jakaumaKaavioApurit_;
  }));

  describe('maksimi:', function() {
    it('pitäisi laskea jakauman lukumäärien maksimi', function () {
      var maksimi = jakaumaKaavioApurit.maksimi([{lukumaara: 1}, {lukumaara: 2}]);
      expect(maksimi).toBe(2);
    });
  });

  describe('otsikoilleTilaa:', function() {
    var otsikoilleTilaa = function (kuvaus, jakauma, odotettuTulos) {
      it('pitäisi antaa ' + kuvaus, function () {
        var tilaa = jakaumaKaavioApurit.otsikoilleTilaa(jakauma);
        expect(tilaa).toBe(odotettuTulos);
      });
    };

    var testitapaukset = [
      ['vähän tilaa tyhjälle vaihtoehdolle', [{vaihtoehto: ''}], 10],
      ['hieman enemmän tilaa keskipituiselle vaihtoehdolle', [{
        vaihtoehto: '12345678901234567890'
      }], 160],
      ['paljon tilaa pitkälle vaihtoehdolle', [{
        vaihtoehto: '1234567890123456789012345678901234567890'
      }], 310],
      ['tilaa pisimmäin vaihtoehdon mukaan', [
        {vaihtoehto: ''},
        {vaihtoehto: '12345678901234567890'}
      ], 160]
    ];

    _.forEach(testitapaukset, function(tapaus) {otsikoilleTilaa.apply(null, tapaus);});

  });

  describe('lukumäärät yhteensä:', function() {
    it('pitäisi laskea jakauman lukumäärien summa', function () {
      var summa = jakaumaKaavioApurit.lukumaaratYhteensa([{lukumaara: 1}, {lukumaara: 2}]);
      expect(summa).toBe(3);
    });
  });

  describe('palkinPituus:', function() {
    var palkinPituudeksi = function (kuvaus, lukumaara, jakauma, odotettuTulos) {
      it('pitäisi antaa ' + kuvaus, function () {
        var pituus = jakaumaKaavioApurit.palkinPituus(lukumaara, jakauma);
        expect(pituus).toBe(odotettuTulos);
      });
    };

    var jakauma = [{lukumaara: 10}];
    var maksimiPituus = 480;
    var testitapaukset = [
      ['tyhjä palkki lukumäärälle nolla', 0, jakauma, 0],
      ['puolikas palkki lukumäärien maksimin puolikkaalle', 5, jakauma, maksimiPituus / 2],
      ['koko palkki lukumäärien maksimiarvolle', 10, jakauma, maksimiPituus],

      ['tyhjä palkki tyhjälle jakaumalle', 0, [], 0],
      ['palkin pituus jakauman yhteismäärän mukaan', 5, [{lukumaara: 5}, {lukumaara: 5}], maksimiPituus / 2],
    ];

    _.forEach(testitapaukset, function(tapaus) {palkinPituudeksi.apply(null, tapaus);});

  });

  it('pitäisi sisältää funktio palkin värille', function () {
    expect(jakaumaKaavioApurit.palkinVari).toBeDefined();
  });

  it('pitäisi asettaa otsikot', function() {
    expect(jakaumaKaavioApurit.otsikot).toBeDefined();
  });
});
