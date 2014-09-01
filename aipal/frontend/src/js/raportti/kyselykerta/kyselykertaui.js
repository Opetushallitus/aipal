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
                                                      'toimiala.kyselykerta',
                                                      'yhteiset.suodattimet.voimassaoloaika',
                                                      'yhteiset.direktiivit.auth-toiminto',
                                                      'ngRoute',
                                                      'ngResource'])
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/raportti/kyselykerrat', {
        controller: 'KyselykerratController',
        templateUrl: 'template/raportti/kyselykerrat.html'
      })
      .when('/raportti/kyselykerta/:kyselykertaid', {
        controller: 'KyselykertaRaporttiController',
        templateUrl: 'template/raportti/kyselykerta.html'
      });
  }])

  .controller('KyselykerratController', [
    'Kyselykerta', '$scope',
    function(Kyselykerta, $scope) {
      $scope.kyselykerrat = Kyselykerta.hae();
    }
  ])

  .controller('KyselykertaRaporttiController', [
    'kaavioApurit', 'KyselykertaRaportti', '$routeParams', '$scope',
    function(kaavioApurit, KyselykertaRaportti, $routeParams, $scope) {
      $scope.tulos = KyselykertaRaportti.hae($routeParams.kyselykertaid);

      $scope.lukumaaratYhteensa = kaavioApurit.lukumaaratYhteensa;
    }
  ]);

