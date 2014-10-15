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

angular.module('kysely.kyselyui', ['rest.kysely', 'rest.kyselypohja',
                                   'rest.vastaajatunnus', 'yhteiset.palvelut.i18n',
                                   'ngAnimate', 'ngRoute', 'yhteiset.palvelut.ilmoitus',
                                   'rest.kysymysryhma'])

  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
      .when('/kyselyt', {
        controller: 'KyselytController',
        templateUrl: 'template/kysely/kyselyt.html',
        label: 'i18n.kysely.breadcrumb_kyselyt'
      })
      .when('/kyselyt/kysely/:kyselyid', {
        controller: 'KyselyController',
        templateUrl: 'template/kysely/kysely.html',
        label: 'i18n.kysely.breadcrumb_muokkaa_kyselya'
      });
  }])

  .controller('KyselytController', [
    '$location', '$modal', '$scope', 'ilmoitus', 'Kysely', 'Kyselykerta', 'i18n', 'seuranta',
    function ($location, $modal, $scope, ilmoitus, Kysely, Kyselykerta, i18n, seuranta) {
      $scope.naytaLuonti = false;

      $scope.status = {};

      $scope.luoUusiKysely = function () {
        Kysely.luoUusi()
        .success(function (data) {
          $location.url('/kyselyt/kysely/' + data.kyselyid);
        })
        .error(function () {
          ilmoitus.virhe(i18n.hae('kysely.uuden_luonti_epaonnistui'));
        });
      };

      $scope.haeKyselyt = function () {
        seuranta.asetaLatausIndikaattori(Kysely.hae(), 'kyselylistaus')
        .success(function (data) {
          $scope.kyselyt = data;
        })
        .error(function() {
          ilmoitus.virhe(i18n.hae('yleiset.lataus_epaonnistui'));
        });
      };
      $scope.haeKyselyt();

      $scope.uusiKyselykerta = function (kysely) {
        var modalInstance = $modal.open({
          templateUrl: 'template/kysely/kyselykerta-luonti.html',
          controller: 'UusiKyselykertaModalController',
          resolve: {
            kysely: function () {
              return kysely;
            }
          }
        });
        modalInstance.result.then(function (kyselykerta) {
          Kyselykerta.luoUusi(kysely.kyselyid, kyselykerta, function () {
            $scope.haeKyselyt();
            ilmoitus.onnistuminen(i18n.hae('kyselykerta.tallennus_onnistui'));
          }, function () {
            ilmoitus.virhe(i18n.hae('kyselykerta.tallennus_epaonnistui'));
          });
        });
      };
    }
  ])

  .factory('kyselyApurit', [function() {
    return {
      lisaaUniikitKysymysryhmatKyselyyn: function(kysely, uudet) {
        _.assign(kysely, { kysymysryhmat: _(kysely.kysymysryhmat.concat(uudet)).uniq('kysymysryhmaid').value() });
      }
    };
  }])

  .controller('KyselyController', [
    'Kysely', 'Kyselypohja', 'Kysymysryhma', 'kyselyApurit', 'i18n', '$routeParams', '$route', '$scope', 'ilmoitus', '$location', '$modal',
    function (Kysely, Kyselypohja, Kysymysryhma, apu, i18n, $routeParams, $route, $scope, ilmoitus, $location, $modal) {
      Kysely.haeId($routeParams.kyselyid)
        .success(function(kysely) {
          $scope.kysely = kysely;
        })
        .error(function() {
          ilmoitus.virhe(i18n.hae('yleiset.lataus_epaonnistui'));
        });

      $scope.tallenna = function (kysely) {
        Kysely.tallenna(kysely)
        .success(function () {
          $location.path('/kyselyt');
          ilmoitus.onnistuminen(i18n.hae('kysely.tallennus_onnistui'));
        })
        .error(function () {
          ilmoitus.virhe(i18n.hae('kysely.tallennus_epaonnistui'));
        });
      };

      $scope.lisaaKyselypohjaModal = function () {
        var modalInstance = $modal.open({
          templateUrl: 'template/kysely/lisaa-kyselypohja.html',
          controller: 'LisaaKyselypohjaModalController'
        });
        modalInstance.result.then(function (kyselypohjaId) {
          Kyselypohja.hae(kyselypohjaId)
          .success(function(kysymysryhmat) {
            apu.lisaaUniikitKysymysryhmatKyselyyn($scope.kysely, kysymysryhmat);
          })
          .error(function(){
            ilmoitus.virhe(i18n.hae('kysely.pohjan_haku_epaonnistui'));
          });
        });
      };

      $scope.lisaaKysymysryhmaModal = function() {
        var modalInstance = $modal.open({
          templateUrl: 'template/kysely/lisaa-kysymysryhma.html',
          controller: 'LisaaKysymysryhmaModalController'
        });
        modalInstance.result.then(function (kysymysryhmaid) {
          Kysymysryhma.hae(kysymysryhmaid)
          .success(function(kysymysryhma) {
            apu.lisaaUniikitKysymysryhmatKyselyyn($scope.kysely, kysymysryhma);
          })
          .error(function() {
            ilmoitus.virhe(i18n.hae('kysely.ryhman_haku_epaonnistui'));
          });
        });
      };

      $scope.poistaKysymys = function (kysymys) {
        kysymys.poistettu = true;
      };
      $scope.palautaKysymys = function (kysymys) {
        kysymys.poistettu = false;
      };
    }
  ])

  .controller('UusiKyselykertaModalController', ['$modalInstance', '$scope', 'kysely', function ($modalInstance, $scope, kysely) {
    $scope.kysely = kysely;

    $scope.kyselykerta = {};
    $scope.tallenna = function () {
      $modalInstance.close($scope.kyselykerta);
    };
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }])

  .controller('LisaaKyselypohjaModalController', ['$modalInstance', '$scope', 'Kyselypohja', function ($modalInstance, $scope, Kyselypohja) {
    Kyselypohja.haeVoimassaolevat()
    .success(function (data) {
      $scope.kyselypohjat = data;
    });
    $scope.tallenna = function (kyselypohjaId) {
      $modalInstance.close(kyselypohjaId);
    };
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }])

  .controller('LisaaKysymysryhmaModalController', ['$modalInstance', '$scope', 'Kysymysryhma', function ($modalInstance, $scope, Kysymysryhma) {
    Kysymysryhma.haeVoimassaolevat().success(function(kysymysryhmat){
      $scope.kysymysryhmat = kysymysryhmat;
    });
    $scope.tallenna = function (kysymysryhmaid) {
      $modalInstance.close(kysymysryhmaid);
    };
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }])
;
