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

angular.module('yhteiset.direktiivit.kysymysryhma-accordion', [])
  .directive('kysymysryhmaAccordion', [function() {
    return {
      restrict: 'E',
      scope: {
        kysymysryhmat: '=',
        kysymystenPoisto: '=',
        isJulkaistu: '=',
        isPoistettavissa: '='
      },
      templateUrl: 'template/yhteiset/direktiivit/kysymysryhma-accordion.html',
      controller: ['$scope', 'i18n', function($scope, i18n) {
        $scope.i18n = i18n;

        $scope.poistaTaiPalautaKysymysryhma = function(kysymysryhma) {
          kysymysryhma.poistetaan_kyselysta = !kysymysryhma.poistetaan_kyselysta;
        };

        $scope.isMuokattavissa = function (kysymysryhma) {

          const kategoria_ok = _.get(kysymysryhma, ['kategoria', 'lisattavissa_kyselyyn'], null) !== null;
          const tyyppi_ok = (!kysymysryhma.valtakunnallinen && !kysymysryhma.taustakysymykset)

          return $scope.isPoistettavissa && (tyyppi_ok || kategoria_ok);
        };

        var jarjesta = function() {
          if ($scope.kysymysryhmat !== undefined) {
            $scope.kysymysryhmat = _.sortBy($scope.kysymysryhmat, function(kysymysryhma, index) {
              return (kysymysryhma.taustakysymykset ? 0 : 100) + (kysymysryhma.valtakunnallinen ? 0 : 1000) + index;
            });
          }
        };
        jarjesta();

        $scope.sortableOptions = {
          stop: function() {
            jarjesta();
          }
        };
      }]
    };
  }])
;
