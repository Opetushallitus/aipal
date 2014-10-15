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

angular.module('kyselykerta.kyselykertaui', ['yhteiset.palvelut.i18n', 'ngRoute', 'rest.rahoitusmuoto', 'rest.vastaajatunnus', 'rest.kyselykerta', 'yhteiset.palvelut.ilmoitus'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/kyselyt/:kyselyid/kyselykerta/:kyselykertaid', {
        controller: 'KyselykertaController',
        templateUrl: 'template/kyselykerta/kyselykerta.html',
        label: 'i18n.kysely.breadcrumb_muokkaa_kyselykertaa'
      });
  }])

  .controller('KyselykertaController', ['Kyselykerta', 'Rahoitusmuoto', 'Vastaajatunnus', '$modal', '$routeParams', '$scope', 'ilmoitus', 'i18n',
    function(Kyselykerta, Rahoitusmuoto, Vastaajatunnus, $modal, $routeParams, $scope, ilmoitus, i18n) {
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
              $scope.tunnukset.unshift(tunnus);
            });
            ilmoitus.onnistuminen(i18n.hae('vastaajatunnus.tallennus_onnistui'));
          }, function() {
            ilmoitus.virhe(i18n.hae('vastaajatunnus.tallennus_epaonnistui'));
          });
        });
      };

      $scope.kyselykertaid = $routeParams.kyselykertaid;
      $scope.rahoitusmuodot = Rahoitusmuoto.haeKaikki(function(data) {
        $scope.rahoitusmuodotmap = _.indexBy(data, 'rahoitusmuotoid');
      });

      $scope.tunnukset = Vastaajatunnus.hae($routeParams.kyselykertaid);

      $scope.kyselykerta = {};
      Kyselykerta.haeYksi($scope.kyselykertaid, function(kyselykerta) {
        $scope.kyselykerta = kyselykerta;
      });

      $scope.tallennaKyselykerta = function() {
        Kyselykerta.tallenna($scope.kyselykertaid, $scope.kyselykerta, function() {
          ilmoitus.onnistuminen(i18n.hae('kyselykerta.tallennus_onnistui'));
        }, function() {
          ilmoitus.virhe(i18n.hae('kyselykerta.tallennus_epaonnistui'));
        });
      };

      $scope.lukitseTunnus = function(tunnus, lukitse) {
        Vastaajatunnus.lukitse($routeParams.kyselykertaid, tunnus.vastaajatunnusid, lukitse, function(uusiTunnus) {
          _.assign(tunnus, uusiTunnus);
        });
      };
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
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }])
;
