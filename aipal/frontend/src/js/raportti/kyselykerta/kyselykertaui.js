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
                                                      'raportti.kyselykerta.vaittamakaavio',
                                                      'rest.kyselykerta',
                                                      'rest.raportti',
                                                      'yhteiset.suodattimet.voimassaoloaika',
                                                      'ngRoute',
                                                      'ngResource'])
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/raportit/kyselykerrat/kyselykerta/:kyselykertaid', {
        controller: 'KyselykertaRaporttiController',
        templateUrl: 'template/raportti/raportti.html',
        label: 'i18n.raportit.breadcrumb_raportti'
      });
  }])

  .controller('KyselykertaRaporttiController', [
    'kaavioApurit', 'Raportti', '$location', '$routeParams', '$scope',
    function(kaavioApurit, Raportti, $location, $routeParams, $scope) {
      Raportti.muodostaKyselykertaraportti($routeParams.kyselykertaid, {})
        .then(function onSuccess(resp) {
          if (!resp.data) {
            console.error('resp.data missing');
          }
          $scope.tulos = resp.data;
          $scope.$parent.timestamp = new Date();
        })
        .catch(function onError(value) {
          if (value.status !== 500) {
            $location.url('/');
          }
        });

      $scope.lukumaaratYhteensa = kaavioApurit.lukumaaratYhteensa;
      $scope.prosenttiosuus = kaavioApurit.prosenttiosuus;
      $scope.raporttiIndeksit = kaavioApurit.raporttiIndeksit;
    }
  ]);

