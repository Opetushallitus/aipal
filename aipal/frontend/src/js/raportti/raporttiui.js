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

angular.module('raportti.raporttiui', ['rest.raportti'])
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/raportit', {
        controller: 'RaportitController',
        templateUrl: 'template/raportti/raportit.html',
        label: 'i18n.raportit.breadcrumb_raportit'
      });
  }])

  .controller('RaportitController', ['$scope', 'Kysymysryhma', 'Raportti', function($scope, Kysymysryhma, Raportti) {
    $scope.raportti = {};

    var haeTaustakysymykset = function(kysymysryhmaid) {
      Kysymysryhma.hae(kysymysryhmaid).success(function(kysymysryhma) {
        $scope.kysymysryhma = kysymysryhma;
      });
    };

    Kysymysryhma.haeVoimassaolevat().success(function(kysymysryhmat) {
      $scope.taustakysymysryhmat = _.filter(kysymysryhmat, 'taustakysymykset');

      $scope.$watch('raportti.taustakysymysryhmaid', function(kysymysryhmaid) {
        haeTaustakysymykset(kysymysryhmaid);
      });

      $scope.raportti.taustakysymysryhmaid = $scope.taustakysymysryhmat[0].kysymysryhmaid;
    });

    $scope.muodostaRaportti = function() {
      Raportti.muodosta($scope.raportti).success(function(tulos) {
        $scope.tulos = tulos;
      });
    };
  }])
;