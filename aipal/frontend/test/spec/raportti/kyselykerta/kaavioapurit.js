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

  describe('jaaLokalisoituTeksti:', function() {
    var jaaLokalisoituTeksti = function (data, odotettuTulos, kuvaus) {
      it('pitäisi jakaa ' + kuvaus, function () {
        var rivit = kaavioApurit.jaaLokalisoituTeksti('a', data);
        expect(rivit).toEqual(odotettuTulos);
      });
    };

    var testitapaukset = [
      // data, odotettu tulos, kuvaus
      [ {a_fi:'abc'}, [ 'abc' ], 'yksi sana' ],
      [ {a_fi:'abcdefghi'}, [ 'abcdefghi' ], 'pitkä sana' ],
      [ {a_fi:'abc def'}, [ 'abc', 'def' ], 'kaksi sanaa' ],
      [ {a_fi:'abc def ghi'}, [ 'abc def', 'ghi' ], 'kolme sanaa' ],
      [ {a_fi:'abc def ghi jkl'}, [ 'abc def', 'ghi jkl' ], 'neljä sanaa' ],
      [ {a_fi:'abcde fgh'}, [ 'abcde', 'fgh' ], 'eripituiset sanat' ],
      [ {a_fi:'abcdefghijkl mno pqr'}, [ 'abcdefghijkl mno', 'pqr' ], 'pitkä ja kaksi lyhyttä' ],
      [ {a_fi:'abc '}, [ 'abc', '' ], 'välilyönti lopussa' ],
      [ {a_fi:' abc'}, [ '', 'abc' ], 'välilyönti alussa' ],
      [ {a_fi:'abc\tdef'}, [ 'abc', 'def' ], 'sarkain' ]
    ];

    _.forEach(testitapaukset, function(tapaus) {jaaLokalisoituTeksti.apply(null, tapaus);});
  });

  describe('maksimi:', function() {
    it('pitäisi laskea jakauman lukumäärien maksimi', function () {
      var maksimi = kaavioApurit.maksimi([{lukumaara: 1}, {lukumaara: 2}]);
      expect(maksimi).toBe(2);
    });
  });

  describe('lukumäärät yhteensä:', function() {
    it('pitäisi laskea jakauman lukumäärien summa', function () {
      var summa = kaavioApurit.lukumaaratYhteensa([{lukumaara: [1]}, {lukumaara: [2]}], 0);
      expect(summa).toBe(3);
    });
  });

  describe('palkinPituus:', function() {
    var asetukset = {
      palkinMaksimiPituus: 480,
    };
    var palkinPituudeksi = function (kuvaus, osuus, odotettuTulos) {
      it('pitäisi antaa ' + kuvaus, function () {
        var pituus = kaavioApurit.palkinPituus(asetukset, osuus);
        expect(pituus).toBe(odotettuTulos);
      });
    };

    var maksimiPituus = asetukset.palkinMaksimiPituus;
    var testitapaukset = [
      ['tyhjä palkki 0% osuudelle', 0, 0],
      ['puolikas palkki 50% osuudelle', 50, maksimiPituus / 2],
      ['koko palkki 100% osuudelle', 100, maksimiPituus],

      ['tyhjä palkki puuttuvalle osuudelle', null, 0],
    ];

    _.forEach(testitapaukset, function(tapaus) {palkinPituudeksi.apply(null, tapaus);});

  });

  it('pitäisi sisältää funktio palkin värille', function () {
    expect(kaavioApurit.palkinVari).toBeDefined();
  });

  describe('raporttiIndeksit:', function() {
    it('pitäisi tuottaa taulukon indeksit', function () {
      expect(kaavioApurit.raporttiIndeksit([2, 3, 4])).toEqual([0, 1, 2]);
    });

    it('pitäisi jättää lopusta null-arvot huomiotta', function () {
      expect(kaavioApurit.raporttiIndeksit([2, 3, null])).toEqual([0, 1]);
    });

    it('pitäisi toimia tyhjälle taulukolle', function () {
      expect(kaavioApurit.raporttiIndeksit([])).toEqual([]);
    });
  });
});
