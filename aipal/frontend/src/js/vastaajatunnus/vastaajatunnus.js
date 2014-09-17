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

angular.module('vastaajatunnus.vastaajatunnusui', ['yhteiset.palvelut.i18n', 'ngRoute', 'rest.rahoitusmuoto', 'rest.vastaajatunnus', 'rest.kyselykerta'])
  
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/vastaajatunnus/:kyselykertaid', {
        controller: 'VastaajatunnusController',
        templateUrl: 'template/vastaajatunnus/vastaajatunnus.html'
      });
  }])

  .controller('VastaajatunnusController', ['Rahoitusmuoto', 'Vastaajatunnus', '$modal', '$routeParams', '$scope',
    function(Rahoitusmuoto, Vastaajatunnus, $modal, $routeParams, $scope) {
      $scope.luoTunnuksiaDialogi = function() {
        var kyselykertaId = $routeParams.kyselykertaid;

        var modalInstance = $modal.open({
          templateUrl: 'template/kysely/tunnusten-luonti.html',
          controller: 'LuoTunnuksiaModalController',
          resolve: {
            rahoitusmuodot: function() {
              return $scope.rahoitusmuodot;
            }
          }
        });

        modalInstance.result.then(function(vastaajatunnus) {
          Vastaajatunnus.luoUusia(kyselykertaId, vastaajatunnus, function(uudetTunnukset) {
            _.forEach(uudetTunnukset, function(tunnus) {
              tunnus.new = true;
              $scope.tulos.unshift(tunnus);
            });
          });
        });
      };

      $scope.kyselykertaid = $routeParams.kyselykertaid;
      $scope.rahoitusmuodot = Rahoitusmuoto.haeKaikki(function(data) {
        $scope.rahoitusmuodotmap = _.indexBy(data, 'rahoitusmuotoid');
      });

      $scope.tulos = Vastaajatunnus.hae($routeParams.kyselykertaid);
    }]
  )

  .controller('LuoTunnuksiaModalController', ['$modalInstance', '$scope', 'rahoitusmuodot', function($modalInstance, $scope, rahoitusmuodot) {
    $scope.vastaajatunnus = {
      vastaajien_lkm: 1
    };
    $scope.rahoitusmuodot = rahoitusmuodot;

    $scope.luoTunnuksia = function(vastaajatunnus) {
      $modalInstance.close(vastaajatunnus);
    };
  }])
;
