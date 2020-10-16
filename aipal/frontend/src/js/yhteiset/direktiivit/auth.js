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

angular.module('yhteiset.direktiivit.auth', [])

  .directive('auth', ['kayttooikeudet', function (kayttooikeudet) {

    return {
      restrict: 'E',
      replace: true,
      transclude: true,
      templateUrl: 'template/yhteiset/direktiivit/auth.html',
      scope: true,
      link: function (scope, element, attrs) {

        var roolit = attrs.roolit ? scope.$eval(attrs.roolit) : [];
        roolit.push('YLLAPITAJA');

        scope.sallittu = false;

        // NOTE: .hae() is not http call an returns prefetched data so no resp.data.
        kayttooikeudet.hae().then(function (oikeudet) {
          scope.sallittu = _.contains(roolit, oikeudet.aktiivinen_rooli.rooli);
        }).catch(function (e) {
          console.error(e);
        });

      }
    };
  }]);
