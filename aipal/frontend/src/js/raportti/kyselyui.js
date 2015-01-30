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

angular.module('raportti.kyselyui', ['raportti.kyselykerta.jakaumakaavio',
                                     'raportti.kyselykerta.kaavioapurit',
                                     'raportti.kyselykerta.vaittamakaavio',
                                     'rest.raportti',
                                     'yhteiset.suodattimet.voimassaoloaika',
                                     'ngRoute'])
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/raportit/kysely/:kyselyid', {
        controller: 'KyselyraporttiController',
        templateUrl: 'template/raportti/raportti.html',
        label: 'i18n.raportit.breadcrumb_raportti'
      });
  }])

  .controller('KyselyraporttiController', [
    'kaavioApurit', 'Raportti', '$location', '$routeParams', '$scope',
    function(kaavioApurit, Raportti, $location, $routeParams, $scope) {
      Raportti.muodostaKyselyraportti($routeParams.kyselyid)
        .success(function(tulokset) {
          $scope.tulokset = tulokset;
          $scope.tulos = tulokset[0];
        })
        .error(function(value) {
          if (value.status !== 500) {
            $location.url('/');
          }
        });

      $scope.lukumaaratYhteensa = kaavioApurit.lukumaaratYhteensa;
      $scope.prosenttiosuus = kaavioApurit.prosenttiosuus;
    }
  ]);
