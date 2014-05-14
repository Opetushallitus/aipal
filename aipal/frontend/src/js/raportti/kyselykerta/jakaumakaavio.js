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

angular.module('raportti.kyselykerta.jakaumakaavio', [])
  .directive('jakaumaKaavio', ['jakaumaKaavioApurit', function(jakaumaKaavioApurit) {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        jakauma: '='
      },
      templateUrl: 'template/raportti/jakaumaKaavio.html',
      link: function(scope) {
        _.assign(scope, jakaumaKaavioApurit);
      }
    };
  }])

  .factory('jakaumaKaavioApurit', [function() {
    var varit = ['#43b1d5', '#ffad33', '#d633ad', '#6cc555'];

    var lukumaaratYhteensa = function (jakauma) {
      var lukumaarat = _.pluck(jakauma, 'lukumaara');
      return _.reduce(lukumaarat, function (sum, n) {return sum + n;});
    };

    return {
      maksimi: function (jakauma) {
        var lukumaarat = _.pluck(jakauma, 'lukumaara');
        return _.max(lukumaarat);
      },

      otsikoilleTilaa: function (jakauma) {
        var vaihtoehdot = _.pluck(jakauma, 'vaihtoehto');
        var pituudet = _.map(vaihtoehdot, function (v) {return v.length;});
        return 300 * _.max(pituudet) / 40 + 10;
      },

      lukumaaratYhteensa: lukumaaratYhteensa,

      palkinPituus: function (lukumaara, jakauma) {
        var yhteensa = lukumaaratYhteensa(jakauma);
        if (Math.abs(yhteensa) > 0.01) {
          return 480 * (lukumaara / yhteensa);
        } else {
          return 0;
        }
      },

      palkinVari: function (i) {
        return varit[i % varit.length];
      },

      otsikot: [{x: 0, teksti: ''}, {x: 0.25, teksti: '25%'}, {x: 0.5, teksti: '50%'}, {x: 0.75, teksti: '75%'}, {x: 1.0, teksti: '100%'}]
    };
  }]);
