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

angular.module('raportti.kyselykerta.jakaumakaavio', ['raportti.kyselykerta.kaavioapurit'])
  .directive('jakaumaKaavio', ['kaavioApurit', 'i18n', function(kaavioApurit, i18n) {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        jakauma: '=',
        nimet: '=',
        vastaustyyppi: '='
      },
      templateUrl: 'template/raportti/jakaumaKaavio.html',
      link: function(scope) {
        var asetukset = {
          palkinLeveys: 25,
          palkinMaksimiPituus: 300,
          tekstinPituus: 180
        };
        var raportoitavia = scope.jakauma[0].osuus.length;

        scope.asetukset = asetukset;
        _.assign(scope, _.pick(kaavioApurit, ['erotaJakauma', 'maksimi', 'palkinVari']));
        scope.palkinPituus = _.partial(kaavioApurit.palkinPituus, asetukset);
        if (scope.vastaustyyppi === 'kylla_ei_valinta') {
          scope.jaaTeksti = _.partial(kaavioApurit.jaaLokalisointiavain, 'kysymys.kylla_ei_valinta', 'vaihtoehto-avain');
        }
        else {
          scope.jaaTeksti = function(data) {
            if(data.jarjestys === 'eos') {
              return kaavioApurit.jaaTeksti(i18n.hae('kysymys.monivalinta.eos'));
            } else {
              return kaavioApurit.jaaLokalisoituTeksti('vaihtoehto', data);
            }
          };
        }
        scope.raporttiIndeksit = function(taulukko) {
          return _.range(taulukko.length);
        };
        scope.viivastonKorkeus = function() {
          return (raportoitavia + 0.5) * asetukset.palkinLeveys * scope.jakauma.length;
        };
        scope.paikkaPalkistonSuhteen = function(palkisto, palkki, siirtyma) {
          return asetukset.palkinLeveys * ((raportoitavia + 0.5)*palkisto + palkki + siirtyma);
        };
        scope.otsikot = [
          {x: 0, teksti: ''},
          {x: 0.25, teksti: '25%'},
          {x: 0.5, teksti: '50%'},
          {x: 0.75, teksti: '75%'},
          {x: 1.0, teksti: '100%'}
        ];
      }
    };
  }]);
