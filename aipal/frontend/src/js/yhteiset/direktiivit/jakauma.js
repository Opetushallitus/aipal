// Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
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

angular.module('yhteiset.direktiivit.jakauma', ['yhteiset.palvelut.i18n', 'raportti.kyselykerta.kaavioapurit'])

  .directive('jakauma', [function() {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        kysymys: '=',
        kysymysryhma: '=',
        nimet: '='
      },
      templateUrl: 'template/yhteiset/direktiivit/jakauma.html',
      controller: ['$scope', 'i18n', 'kaavioApurit', function($scope, i18n, kaavioApurit) {
        $scope.i18n = i18n;
        $scope.lukumaaratYhteensa = kaavioApurit.lukumaaratYhteensa;
        $scope.prosenttiosuus = kaavioApurit.prosenttiosuus;
        $scope.raporttiIndeksit = kaavioApurit.raporttiIndeksit;
        $scope.anyNotNull = function (array) {
          return !!array && array.some(function (item) {
            return item !== null && item !== undefined;
          });
        };
      }]
    };
  }]);
