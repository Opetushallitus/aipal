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

angular.module('raportti.kyselykerta.kyselykertaui', ['raportti.kyselykerta.jakaumakaavio',
                                                      'raportti.kyselykerta.kaavioapurit',
                                                      'raportti.kyselykerta.kyselykertaraportti',
                                                      'raportti.kyselykerta.vaittamakaavio',
                                                      'rest.kyselykerta',
                                                      'yhteiset.suodattimet.voimassaoloaika',
                                                      'ngRoute',
                                                      'ngResource'])
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/raportit/kyselykerrat', {
        controller: 'KyselykerratController',
        templateUrl: 'template/raportti/kyselykerrat.html',
        label: 'i18n.raportit.breadcrumb_raportit'
      })
      .when('/raportit/kyselykerrat/kyselykerta/:kyselykertaid', {
        controller: 'KyselykertaRaporttiController',
        templateUrl: 'template/raportti/raportti.html',
        label: 'i18n.raportit.breadcrumb_raportti'
      });
  }])

  .controller('KyselykerratController', [
    'Kyselykerta', '$scope',
    function(Kyselykerta, $scope) {
      Kyselykerta.hae()
        .success(function(kyselykerrat) {
          $scope.kyselykerrat = kyselykerrat;
        });
    }
  ])

  .controller('KyselykertaRaporttiController', [
    'kaavioApurit', 'KyselykertaRaportti', '$location', '$routeParams', '$scope',
    function(kaavioApurit, KyselykertaRaportti, $location, $routeParams, $scope) {
      KyselykertaRaportti.hae($routeParams.kyselykertaid, function(tulos) {
        $scope.tulos = tulos;
      }, function(value) {
        if (value.status !== 500) {
          $location.url('/');
        }
      });

      $scope.lukumaaratYhteensa = kaavioApurit.lukumaaratYhteensa;
    }
  ]);

