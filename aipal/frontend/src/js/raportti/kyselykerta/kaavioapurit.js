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

angular.module('raportti.kyselykerta.kaavioapurit', ['yhteiset.palvelut.i18n', 'yhteiset.palvelut.lokalisointi'])
  .factory('kaavioApurit', ['$filter', 'i18n', function($filter, i18n) {
    var varit = ['#43b1d5', '#ffad33', '#d633ad', '#6cc555'];

    var lukumaaratYhteensa = function (jakauma, i) {
      var lukumaarat = _.map(_.pluck(jakauma, 'lukumaara'), function(taulukko) {return taulukko[i];});
      return _.reduce(lukumaarat, function (sum, n) {return sum + n;});
    };

    var jaaTeksti = function(teksti) {
      var sanat = teksti.split(/\s/);
      var sanojaPerRivi = Math.ceil(sanat.length / 2);

      var rivit = [];
      while (sanat.length > 0) {
        rivit.push(sanat.splice(0, sanojaPerRivi).join(' '));
      }
      return rivit;
    };

    return {
      jaaTeksti: jaaTeksti,

      jaaLokalisoituTeksti: function (avain, data) {
        var teksti = $filter('lokalisoiKentta')(data, avain);

        return jaaTeksti(teksti);
      },

      jaaLokalisointiavain: function (lokalisaatioAvain, dataAvain, data) {
        var teksti = i18n.hae(lokalisaatioAvain + '.' + data[dataAvain]);

        return jaaTeksti(teksti);
      },

      maksimi: function (jakauma) {
        var lukumaarat = _.pluck(jakauma, 'lukumaara');
        return _.max(lukumaarat);
      },

      lukumaaratYhteensa: lukumaaratYhteensa,

      prosenttiosuus: function prosenttiosuus(a, b) {
        return (b >= 1) ? Math.round(100 * a / b) : 0;
      },

      erotaJakauma: function (jakauma, kentta, i) {
        return _.map(jakauma, function(alkio) {return alkio[kentta][i];});
      },

      palkinPituus: function (asetukset, osuus) {
        return asetukset.palkinMaksimiPituus * osuus / 100;
      },

      palkinVari: function (i) {
        return varit[i % varit.length];
      },

      raporttiIndeksit: function(taulukko) {
        return _.range(_.size(_.filter(taulukko, function(alkio) {return alkio !== null;})));
      }

    };
  }]);
