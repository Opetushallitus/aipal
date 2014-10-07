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
    '$location', '$modal', '$scope', 'ilmoitus', 'Kysely', 'Kyselykerta', 'i18n',
    function ($location, $modal, $scope, ilmoitus, Kysely, Kyselykerta, i18n) {
      $scope.naytaLuonti = false;

      $scope.status = {};

      $scope.setActiveGroup = function (id) {
        $location.hash(id);
      };

      $scope.luoUusiKysely = function () {
        Kysely.luoUusi(function (data) {
          $location.url('/kyselyt/kysely/' + data.kyselyid);
        }, function () {
          ilmoitus.virhe(i18n.hae('kysely.uuden_luonti_epaonnistui'));
        });
      };

      $scope.haeKyselyt = function () {
        Kysely.hae(function (data) {
          $scope.kyselyt = data;
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
          Kyselykerta.tallenna(kysely.kyselyid, kyselykerta, function () {
            $scope.haeKyselyt();
            ilmoitus.onnistuminen(i18n.hae('kyselykerta.tallennus_onnistui'));
          }, function () {
            ilmoitus.virhe(i18n.hae('kyselykerta.tallennus_epaonnistui'));
          });
        });
      };
    }
  ])

  .controller('KyselyController', [
    'Kysely', 'Kyselypohja', 'Kysymysryhma', 'i18n', '$routeParams', '$route', '$scope', 'ilmoitus', '$window', '$modal',
    function (Kysely, Kyselypohja, Kysymysryhma, i18n, $routeParams, $route, $scope, ilmoitus, $window, $modal) {
      $scope.kysely = Kysely.haeId($routeParams.kyselyid);

      $scope.tallenna = function (kysely) {
        Kysely.tallenna(kysely, function () {
          $window.location.hash = '/kyselyt';
          ilmoitus.onnistuminen(i18n.hae('kysely.tallennus_onnistui'));
        }, function () {
          ilmoitus.virhe(i18n.hae('kysely.tallennus_epaonnistui'));
        });
      };

      $scope.haeKyselypohjat = function () {
        Kyselypohja.haeKaikki(function (data) {
          $scope.kyselypohjat = data;
        });
      };
      $scope.haeKyselypohjat();

      $scope.lisaaKyselypohjaModal = function (kysely) {
        var modalInstance = $modal.open({
          templateUrl: 'template/kysely/lisaa-kyselypohja.html',
          controller: 'LisaaKyselypohjaModalController',
          resolve: {
            kysely: function () {
              return kysely;
            },
            kyselypohjat: function() {
              return $scope.kyselypohjat;
            }
          }
        });
        modalInstance.result.then(function (kyselypohjaId) {
          Kyselypohja.hae(kyselypohjaId,
            function(kysymysryhmat) {
              _.assign($scope.kysely, { kysymysryhmat: _($scope.kysely.kysymysryhmat.concat(kysymysryhmat)).uniq('kysymysryhmaid').value() });
              ilmoitus.onnistuminen(i18n.hae('kyselykerta.pohjan_lisays_onnistui'));
            },
            function(){
              ilmoitus.virhe(i18n.hae('kyselykerta.pohjan_lisays_epaonnistui'));
            });
        });
      };

      $scope.lisaaKysymysryhmaModal = function(kysely) {
        var modalInstance = $modal.open({
          templateUrl: 'template/kysely/lisaa-kysymysryhma.html',
          controller: 'LisaaKysymysryhmaModalController',
          resolve: {
            kysely: function () {
              return kysely;
            }
          }
        });
        modalInstance.result.then(function (kysymysryhmaid) {
          Kysymysryhma.hae(kysymysryhmaid)
          .success(function(kysymysryhma) {
            $scope.kysely.kysymysryhmat.push(kysymysryhma);
            _.assign($scope.kysely, { kysymysryhmat: _($scope.kysely.kysymysryhmat).uniq('kysymysryhmaid').value() });
            ilmoitus.onnistuminen(i18n.hae('kyselykerta.ryhman_haku_onnistui'));
          })
          .error(function() {
            ilmoitus.virhe(i18n.hae('kyselykerta.ryhman_haku_epaonnistui'));
          });
        });
      };

      $scope.poistaKysymys = function (kysymys) {
        Kysely.poistaKysymys($scope.kysely.kyselyid, kysymys.kysymysid, function () {
          kysymys.poistettu = true;
        });
      };
      $scope.palautaKysymys = function (kysymys) {
        Kysely.palautaKysymys($scope.kysely.kyselyid, kysymys.kysymysid, function () {
          kysymys.poistettu = false;
        });
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

  .controller('LisaaKyselypohjaModalController', ['$modalInstance', '$scope', 'kysely', 'kyselypohjat', function ($modalInstance, $scope, kysely, kyselypohjat) {
    $scope.kysely = kysely;
    $scope.kyselypohja = {};
    $scope.kyselypohjat = kyselypohjat;
    $scope.tallenna = function (kyselypohjaId) {
      $modalInstance.close(kyselypohjaId);
    };
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }])

  .controller('LisaaKysymysryhmaModalController', ['$modalInstance', '$scope', 'kysely', 'Kysymysryhma', function ($modalInstance, $scope, kysely, Kysymysryhma) {
    $scope.kysely = kysely;
    Kysymysryhma.haeKaikki().success(function(kysymysryhmat){
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
