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

describe('Palvelu: kaavioApurit', function () {
  beforeEach(module('raportti.kyselykerta.kaavioapurit'));

  var kaavioApurit;
  beforeEach(inject(function(_kaavioApurit_) {
    kaavioApurit = _kaavioApurit_;
  }));

  describe('jaaTeksti:', function() {
    var jaaTeksti = function (teksti, odotettuTulos, kuvaus) {
      it('pitäisi jakaa ' + kuvaus, function () {
        var rivit = kaavioApurit.jaaTeksti(teksti);
        expect(rivit).toEqual(odotettuTulos);
      });
    };

    var testitapaukset = [
      // teksti, odotettu tulos, kuvaus
      [ 'abc', [ 'abc' ], 'yksi sana' ],
      [ 'abcdefghi', [ 'abcdefghi' ], 'pitkä sana' ],
      [ 'abc def', [ 'abc', 'def' ], 'kaksi sanaa' ],
      [ 'abc def ghi', [ 'abc def', 'ghi' ], 'kolme sanaa' ],
      [ 'abc def ghi jkl', [ 'abc def', 'ghi jkl' ], 'neljä sanaa' ],
      [ 'abcde fgh', [ 'abcde', 'fgh' ], 'eripituiset sanat' ],
      [ 'abcdefghijkl mno pqr', [ 'abcdefghijkl mno', 'pqr' ], 'pitkä ja kaksi lyhyttä' ],
      [ 'abc ', [ 'abc', '' ], 'välilyönti lopussa' ],
      [ ' abc', [ '', 'abc' ], 'välilyönti alussa' ],
      [ 'abc\tdef', [ 'abc', 'def' ], 'sarkain' ]
    ];

    _.forEach(testitapaukset, function(tapaus) {jaaTeksti.apply(null, tapaus);});
  });

  describe('maksimi:', function() {
    it('pitäisi laskea jakauman lukumäärien maksimi', function () {
      var maksimi = kaavioApurit.maksimi([{lukumaara: 1}, {lukumaara: 2}]);
      expect(maksimi).toBe(2);
    });
  });

  describe('otsikoilleTilaa:', function() {
    var asetukset = {
      maksimitilaOtsikolle: 100,
      otsikoidenSisennys: 5,
      tekstinMaksimiPituus: 10
    };
    var otsikoilleTilaa = function (kuvaus, jakauma, odotettuTulos) {
      it('pitäisi antaa ' + kuvaus, function () {
        var tilaa = kaavioApurit.otsikoilleTilaa(asetukset, jakauma);
        expect(tilaa).toBe(odotettuTulos);
      });
    };

    var testitapaukset = [
      ['vähän tilaa tyhjälle vaihtoehdolle', [{vaihtoehto: ''}], 5],
      ['hieman enemmän tilaa keskipituiselle vaihtoehdolle', [{
        vaihtoehto: '12345'
      }], 55],
      ['paljon tilaa pitkälle vaihtoehdolle', [{
        vaihtoehto: '1234567890'
      }], 105],
      ['tilaa pisimmäin vaihtoehdon mukaan', [
        {vaihtoehto: ''},
        {vaihtoehto: '1234567890'}
      ], 105]
    ];

    _.forEach(testitapaukset, function(tapaus) {otsikoilleTilaa.apply(null, tapaus);});

  });

  describe('lukumäärät yhteensä:', function() {
    it('pitäisi laskea jakauman lukumäärien summa', function () {
      var summa = kaavioApurit.lukumaaratYhteensa([{lukumaara: 1}, {lukumaara: 2}]);
      expect(summa).toBe(3);
    });
  });

  describe('palkinPituus:', function() {
    var asetukset = {
      palkinMaksimiPituus: 480,
    };
    var palkinPituudeksi = function (kuvaus, lukumaara, jakauma, odotettuTulos) {
      it('pitäisi antaa ' + kuvaus, function () {
        var pituus = kaavioApurit.palkinPituus(asetukset, lukumaara, jakauma);
        expect(pituus).toBe(odotettuTulos);
      });
    };

    var jakauma = [{lukumaara: 10}];
    var maksimiPituus = asetukset.palkinMaksimiPituus;
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
    expect(kaavioApurit.palkinVari).toBeDefined();
  });
});
