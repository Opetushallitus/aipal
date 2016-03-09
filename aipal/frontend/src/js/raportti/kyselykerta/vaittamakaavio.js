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

angular.module('raportti.kyselykerta.vaittamakaavio', ['raportti.kyselykerta.kaavioapurit'])
  .directive('vaittamaKaavio', ['kaavioApurit', function(kaavioApurit) {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        jakauma: '=',
        nimet: '=',
        vastaustyyppi: '=',
        eos: '='
      },
      templateUrl: 'template/raportti/vaittamakaavio.html',
      link: function(scope) {
        var raportoitavia = scope.jakauma[0].osuus.length,
          viivastonLeveys;
        if(scope.eos) {
          viivastonLeveys = (scope.jakauma.length + 1)*(raportoitavia + 2)*35;
        } else {
          viivastonLeveys = (scope.jakauma.length)*(raportoitavia + 2)*35;
        }
        var asetukset = {
          palkinLeveys: 35,
          palkinMaksimiPituus: 300,
          viivastonLeveys: viivastonLeveys
        };

        scope.asetukset = asetukset;
        _.assign(scope, _.pick(kaavioApurit, ['erotaJakauma', 'maksimi', 'palkinVari']));
        scope.palkinPituus = _.partial(kaavioApurit.palkinPituus, asetukset);
        scope.jaaTeksti = _.partial(kaavioApurit.jaaLokalisointiavain, 'kysymys.' + scope.vastaustyyppi, 'vaihtoehto-avain');
        scope.raporttiIndeksit = function(taulukko) {
          return _.range(taulukko.length);
        };
        scope.paikkaPalkistonSuhteen = function(palkisto, palkki, siirtyma) {
          return asetukset.palkinLeveys * ((raportoitavia + 2)*palkisto + palkki + siirtyma);
        };
        scope.otsikot = [
          {x: 0, teksti: ''},
          {x: 0.2, teksti: '20%'},
          {x: 0.4, teksti: '40%'},
          {x: 0.6, teksti: '60%'},
          {x: 0.8, teksti: '80%'},
          {x: 1.0, teksti: '100%'}
        ];
      }
    };
  }]);
