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

angular.module('yhteiset.direktiivit.auth-toiminto', [])

  .directive('authToiminto', ['kayttooikeudet', function (kayttooikeudetService) {

    function resolveTemplate(el, attrs) {
      if (attrs.authLinkki !== undefined) {
        return 'template/yhteiset/direktiivit/auth-linkki.htm';
      } else if (attrs.authNappi !== undefined) {
        return 'template/yhteiset/direktiivit/auth-nappi.htm';
      }
    }

    return {
      restrict: 'E',
      replace: true,
      transclude: true,
      templateUrl: resolveTemplate,
      scope: true,
      link: function (scope, element, attrs) {

        var kayttooikeudet, entityId;

        var vaadittuOikeus = attrs.oikeus;
        var konteksti = attrs.konteksti;
        var sallitutRoolit = attrs.sallitutRoolit ? scope.$eval(attrs.sallitutRoolit) : [];
        sallitutRoolit.push('YLLAPITAJA');

        scope.sallittu = false;
        scope.href = attrs.href;

        function paivitaOikeus() {
          scope.sallittu = onkoSalittu();
        }

        function onkoSalittu() {
          if (kayttooikeudet && kayttooikeudet.$resolved) {
            try {
              return _.contains(sallitutRoolit, kayttooikeudet.roolitunnus) ||
                _(kayttooikeudet[konteksti]).filter(function (value) {
                  return value.tunniste === entityId;
                }).pluck('oikeudet').flatten().contains(vaadittuOikeus);
            } catch (e) {
            }
          }
          return false;
        }

        kayttooikeudetService.hae().then(function (oikeudet) {
          kayttooikeudet = oikeudet;
          paivitaOikeus();
        });

        attrs.$observe('entityId', function (value) {
          entityId = value;
          paivitaOikeus();
        });
      }
    };
  }]);