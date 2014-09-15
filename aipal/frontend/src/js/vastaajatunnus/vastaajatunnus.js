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

angular.module('vastaajatunnus.vastaajatunnusui', ['yhteiset.palvelut.i18n', 'ngRoute', 'toimiala.rahoitusmuoto', 'toimiala.vastaajatunnus', 'toimiala.kyselykerta'])
  
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/vastaajatunnus/:kyselykertaid', {
        controller: 'VastaajatunnusController',
        templateUrl: 'template/vastaajatunnus/vastaajatunnus.html'
      });
  }])

  .controller('VastaajatunnusController', ['Rahoitusmuoto', 'Vastaajatunnus', '$routeParams', '$scope',
    function(Rahoitusmuoto, Vastaajatunnus, $routeParams, $scope) {
      $scope.luoTunnuksiaDialogi = function(kyselykertaId) {
        $scope.naytaLuoTunnuksia = true;
        $scope.valittuKyselykertaId = kyselykertaId;
      };
      $scope.luoTunnuksiaCallback = function(uudetTunnukset) {
        $scope.naytaLuoTunnuksia = false;
        _.forEach(uudetTunnukset, function(tunnus) {
          tunnus.new = true;
          $scope.tulos.unshift(tunnus);
        });
      };
      $scope.kyselykertaid = $routeParams.kyselykertaid;
      $scope.rahoitusmuodot = Rahoitusmuoto.haeKaikki(function(data) {
        $scope.rahoitusmuodotmap = _.indexBy(data, 'rahoitusmuotoid');
      });

      $scope.tulos = Vastaajatunnus.hae($routeParams.kyselykertaid);
    }]
  )

  .directive('tunnustenLuonti', ['Rahoitusmuoto', 'Vastaajatunnus', function(Rahoitusmuoto, Vastaajatunnus) {
    return {
      restrict: 'E',
      scope: {
        kyselykertaid: '=',
        ilmoitaLuonti: '&',
        rahoitusmuodot: '='
      },
      templateUrl: 'template/kysely/tunnusten-luonti.html',
      link: function(scope) {
        scope.vastaajatunnus = {
          vastaajien_lkm: 1
        };
        scope.luoTunnuksia = function(vastaajatunnus) {
          Vastaajatunnus.luoUusia(scope.kyselykertaid, vastaajatunnus, function(uusiTunnus) {
            scope.ilmoitaLuonti({uusiTunnus: uusiTunnus});
          });
        };
      }
    };
  }])

;
