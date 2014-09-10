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

angular.module('kysely.kyselyui', ['toimiala.kysely', 'toimiala.kyselypohja', 'toimiala.vastaajatunnus', 'yhteiset.palvelut.i18n', 'ngAnimate', 'ngRoute', 'yhteiset.palvelut.ilmoitus'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/kyselyt', {
        controller: 'KyselytController',
        templateUrl: 'template/kysely/kyselyt.html'
      })
      .when('/kysely/:kyselyid', {
        controller: 'KyselyController',
        templateUrl: 'template/kysely/kysely.html'
      });
  }])

  .controller('KyselytController', [
    '$location', '$scope', 'ilmoitus', 'Kysely', 'i18n',
    function($location, $scope, ilmoitus, Kysely, i18n) {
      $scope.naytaLuonti = false;

      $scope.luoUusiKysely = function() {
        Kysely.luoUusi(function(data) {
          $location.url('/kysely/' + data.kyselyid);
        }, function() {
          ilmoitus.virhe(i18n.kysely.uuden_luonti_epaonnistui);
        });
      };

      $scope.haeKyselyt = function() {
        $scope.kyselyt = Kysely.hae();
      };
      $scope.haeKyselyt();

      $scope.uusiKyselykerta = function(kyselyid) {
        $scope.valittuKyselyid = kyselyid;
        $scope.naytaLuonti = true;
      };
      $scope.suljePopup = function() {
        $scope.naytaLuonti = false;
        $scope.haeKyselyt();
      };

      $scope.luoTunnuksiaDialogi = function(kyselykertaId) {
        $scope.naytaLuoTunnuksia = true;
        $scope.valittuKyselykertaId = kyselykertaId;
      };
    }
  ])

  .controller('KyselyController', [
    'Kysely', 'Kyselypohja', 'i18n', '$routeParams', '$route', '$scope', 'ilmoitus',
    function(Kysely, Kyselypohja, i18n, $routeParams, $route, $scope, ilmoitus) {
      $scope.kysely = Kysely.haeId($routeParams.kyselyid);

      $scope.tallenna = function(kysely) {
        Kysely.tallenna(kysely, function() {
          ilmoitus.onnistuminen(i18n.kysely.tallennus_onnistui);
        }, function() {
          ilmoitus.virhe(i18n.kysely.tallennus_epaonnistui);
        });
      };

      Kyselypohja.hae(function(data) {
        $scope.kyselypohjat = data;
      });

      $scope.naytaLisaaKyselyPohjaPopup = false;
      $scope.lisaaKyselyPohjaDialog = function() {
        $scope.naytaLisaaKyselyPohjaPopup = true;
      };

      $scope.lisaaKyselypohja = function(kyselypohjaId) {
        $scope.naytaLisaaKyselyPohjaPopup = false;
        Kysely.lisaaKyselypohja($scope.kysely.kyselyid, kyselypohjaId, function() {
          $route.reload();
        });
      };

      $scope.poistaKysymys = function(kysymys) {
        Kysely.poistaKysymys($scope.kysely.kyselyid, kysymys.kysymysid, function() {
          kysymys.poistettu = true;
        });
      };
      $scope.palautaKysymys = function(kysymys) {
        Kysely.palautaKysymys($scope.kysely.kyselyid, kysymys.kysymysid, function() {
          kysymys.poistettu = false;
        });
      };
    }
  ])

  .directive('kyselykertaLuonti', ['Kysely', 'Kyselykerta', 'i18n', function(Kysely, Kyselykerta, i18n) {
    return {
      restrict: 'E',
      scope: {
        kyselyid : '=',
        ilmoitaTallennus: '&'
      },
      templateUrl: 'template/kysely/kyselykerta-luonti.html',
      link: function(scope) {
        scope.i18n = i18n;
        scope.kysely = {};
        scope.kyselykerta = {};

        scope.$watch('kyselyid', function(kyselyid) {
          if(_.isNumber(kyselyid)) {
            Kysely.haeId(kyselyid, function(kysely) {
              _.assign(scope.kysely, kysely);
            });
          }
        });
        scope.tallenna = function() {
          Kyselykerta.tallenna(scope.kyselyid, scope.kyselykerta, function() {
            scope.ilmoitaTallennus();
          });
        };
      }
    };
  }])

  .directive('tunnustenLuonti', ['Vastaajatunnus', function(Vastaajatunnus) {
    return {
      restrict: 'E',
      scope: {
        kyselykertaid: '='
      },
      templateUrl: 'template/kysely/tunnusten-luonti.html',
      link: function(scope) {
        scope.vastaajatunnus = {
          vastaajien_lkm: 1
        };
        scope.luoTunnuksia = function(vastaajatunnus) {
          scope.naytaLuoTunnuksia = false;
          Vastaajatunnus.luoUusi(scope.kyselykertaid, vastaajatunnus);
        };
      }
    };
  }])

;
