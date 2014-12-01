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
        jakauma: '='
      },
      templateUrl: 'template/raportti/vaittamakaavio.html',
      link: function(scope) {
        var asetukset = {
          maksimitilaOtsikolle: 300,
          palkinMaksimiPituus: 300,
          otsikoidenSisennys: 10,
          tekstinMaksimiPituus: 40
        };

        _.assign(scope, _.pick(kaavioApurit, ['maksimi', 'lukumaaratYhteensa', 'palkinVari']));
        scope.palkinPituus = _.partial(kaavioApurit.palkinPituus, asetukset);
        scope.jaaTeksti = _.partial(kaavioApurit.jaaLokalisointiavain, 'kysymys.asteikko', 'vaihtoehto-avain');
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
