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
                                   'yhteiset.palvelut.tallennusMuistutus',
                                   'rest.kysymysryhma'])

  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
      .when('/kyselyt', {
        controller: 'KyselytController',
        templateUrl: 'template/kysely/kyselyt.html',
        label: 'i18n.kysely.breadcrumb_kyselyt'
      })
      .when('/kyselyt/kysely/uusi', {
        controller: 'KyselyController',
        templateUrl: 'template/kysely/kysely.html',
        label: 'i18n.kysely.breadcrumb_uusi_kysely',
        resolve: {
          kopioi: function() { return false; }
        }
      })
      .when('/kyselyt/kysely/:kyselyid', {
        controller: 'KyselyController',
        templateUrl: 'template/kysely/kysely.html',
        label: 'i18n.kysely.breadcrumb_muokkaa_kyselya',
        resolve: {
          kopioi: function() { return false; }
        }
      })
      .when('/kyselyt/kysely/:kyselyid/kopioi', {
        controller: 'KyselyController',
        templateUrl: 'template/kysely/kysely.html',
        label: 'i18n.kysely.breadcrumb_kopioi_kysely',
        resolve: {
          kopioi: function() { return true; }
        }
      });
  }])

  .controller('KyselytController', [
    '$location', '$scope', 'ilmoitus', 'Kysely', 'i18n', 'seuranta',
    function ($location, $scope, ilmoitus, Kysely, i18n, seuranta) {
      $scope.naytaLuonti = false;

      $scope.status = {};

      $scope.luoUusiKysely = function () {
        $location.url('/kyselyt/kysely/uusi');
      };

      var avaaMuistetutKyselyt = function() {
        var avoimetKyselyIdt = JSON.parse(sessionStorage.getItem('avoimetKyselyt'));
        _.forEach(avoimetKyselyIdt, function(kyselyId) {
          var kysely = _.find($scope.kyselyt, {kyselyid: kyselyId});
          if (kysely !== undefined) {
            kysely.open = true;
          }
        });
      };

      var muistaAvattavatKyselyt = function() {
        $scope.$watch('kyselyt', function(kyselyt) {
          var avoimetKyselyIdt = _(kyselyt).filter('open').map('kyselyid').value();
          sessionStorage.setItem('avoimetKyselyt', JSON.stringify(avoimetKyselyIdt));
        }, true);
      };

      $scope.haeKyselyt = function () {
        seuranta.asetaLatausIndikaattori(Kysely.hae(), 'kyselylistaus')
        .success(function (data) {
          $scope.kyselyt = data;
          avaaMuistetutKyselyt();
          muistaAvattavatKyselyt();
        });
      };
      $scope.haeKyselyt();
    }
  ])

  .factory('kyselyApurit', [function() {
    return {
      lisaaUniikitKysymysryhmatKyselyyn: function(kysely, uudet) {
        _.assign(kysely, { kysymysryhmat: _(kysely.kysymysryhmat.concat(uudet)).uniq('kysymysryhmaid').value() });
      },
      laskeLisakysymykset: function(kysely) {
        return _(kysely.kysymysryhmat).reject('valtakunnallinen').pluck('kysymykset').flatten().reject('poistettu').reduce(function(sum) { return sum + 1; }, 0);
      }
    };
  }])

  .controller('KyselyController', [
    'Kysely', 'Kyselypohja', 'Kysymysryhma', 'kyselyApurit', 'i18n', 'tallennusMuistutus', '$routeParams', '$route', '$scope', 'ilmoitus', '$location', '$modal', 'seuranta', '$timeout', 'kopioi',
    function (Kysely, Kyselypohja, Kysymysryhma, apu, i18n, tallennusMuistutus, $routeParams, $route, $scope, ilmoitus, $location, $modal, seuranta, $timeout, kopioi) {
      var tallennusFn = $routeParams.kyselyid ? Kysely.tallenna : Kysely.luoUusi;
      $scope.$watch('kyselyForm', function(form) {
        // watch tarvitaan koska form asetetaan vasta controllerin jälkeen
        tallennusMuistutus.muistutaTallennuksestaPoistuttaessaFormilta(form);
      });

      if ($routeParams.kyselyid) {
        Kysely.haeId($routeParams.kyselyid)
          .success(function(kysely) {
            $scope.kysely = kysely;
            if(kopioi) {
              tallennusFn = Kysely.luoUusi;
              $scope.kysely.tila = 'luonnos';
              delete $scope.kysely.kyselyid;

              var alkuperaisetKysymysryhmat = _.clone($scope.kysely.kysymysryhmat);
              var suodatetutKysymysryhmat = _.filter($scope.kysely.kysymysryhmat, function(k){
                return k.tila !== 'suljettu';
              });
              if (!_.isEqual(alkuperaisetKysymysryhmat, suodatetutKysymysryhmat)) {
                ilmoitus.varoitus(i18n.hae('kysely.suljetun_kysymysryhman_kopiointi'));
              }
              $scope.kysely.kysymysryhmat = suodatetutKysymysryhmat;
            } else {
              tallennusFn = Kysely.tallenna;
            }
          })
          .error(function() {
            ilmoitus.virhe(i18n.hae('yleiset.lataus_epaonnistui'));
          });
      }
      else {
        $scope.kysely = {
          kysymysryhmat: [],
          voimassa_alkupvm: new Date().toISOString().slice(0, 10)
        };
        tallennusFn = Kysely.luoUusi;
      }

      function poistaKysymysryhmat() {
        $scope.kysely.kysymysryhmat = _.reject($scope.kysely.kysymysryhmat, 'poistetaan_kyselysta');
      }

      $scope.tallenna = function () {
        poistaKysymysryhmat();
        if (apu.laskeLisakysymykset($scope.kysely) > 10) {
          ilmoitus.virhe(i18n.hae('kysely.liian_monta_lisakysymysta'));
        }
        else {
          seuranta.asetaLatausIndikaattori(tallennusFn($scope.kysely), 'kyselynTallennus')
          .success(function () {
            $scope.kyselyForm.$setPristine();
            $location.path('/kyselyt');
            ilmoitus.onnistuminen(i18n.hae('kysely.tallennus_onnistui'));
          })
          .error(function () {
            ilmoitus.virhe(i18n.hae('kysely.tallennus_epaonnistui'));
          });
        }
      };

      $scope.validoiKysymysryhmat = function() {
        if ($scope.kysely === undefined) {
          return true;
        }
        var kysymysryhmat = _.reject($scope.kysely.kysymysryhmat, 'poistetaan_kyselysta');
        var taustakysymysryhma = _.find(kysymysryhmat, 'taustakysymykset') !== undefined;
        var valtakunnallisia = _.find(kysymysryhmat, 'valtakunnallinen') !== undefined;
        return !valtakunnallisia || taustakysymysryhma;
      };

      $scope.peruuta = function() {
        $location.path('/kyselyt');
      };

      $scope.lisaaKyselypohjaModal = function () {
        var modalInstance = $modal.open({
          templateUrl: 'template/kysely/lisaa-kyselypohja.html',
          controller: 'LisaaKyselypohjaModalController'
        });
        modalInstance.result.then(function (kyselypohjaId) {
          Kyselypohja.haeKysymysryhmat(kyselypohjaId)
          .success(function(kysymysryhmat) {
            apu.lisaaUniikitKysymysryhmatKyselyyn($scope.kysely, kysymysryhmat);
            $scope.kyselyForm.$setDirty();
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
          Kysymysryhma.haeEsikatselulle(kysymysryhmaid)
          .success(function(kysymysryhma) {
            apu.lisaaUniikitKysymysryhmatKyselyyn($scope.kysely, kysymysryhma);
            $scope.kyselyForm.$setDirty();
          })
          .error(function() {
            ilmoitus.virhe(i18n.hae('kysely.ryhman_haku_epaonnistui'));
          });
        });
      };

      $scope.esikatseleModal = function() {

        /* Send 'kysely' to Aipalvastaus via PostMessage */
        window.doneLoading = function(){
          $timeout(function () {
            var iframe = document.getElementById('previewiframe').contentWindow;
            iframe.postMessage('connect', '*');

            // Esikatselussa ei näytetä poistettuja kysymysryhmiä ja kysymyksiä
            var kysely = angular.copy($scope.kysely);
            kysely.kysymysryhmat = _.reject(kysely.kysymysryhmat, 'poistetaan_kyselysta');
            _.forEach(kysely.kysymysryhmat, function(kysymysryhma) {
              kysymysryhma.kysymykset = _.reject(kysymysryhma.kysymykset, 'poistettu');
            });

            var message = JSON.stringify({message: kysely});
            iframe.postMessage(message, '*');
          }, 1000);
        };

        $modal.open({
          templateUrl: 'template/kysely/esikatsele.html',
          controller: 'AvaaEsikatseluModalController',
          windowClass: 'preview-modal-window',
          resolve: {vastausBaseUrl: function(){
            return $scope.vastausBaseUrl;
          }}
        });
      };
    }
  ])

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

  .controller('AvaaEsikatseluModalController', ['$modalInstance', '$scope', 'vastausBaseUrl', '$sce', function ($modalInstance, $scope, vastausBaseUrl, $sce) {
    $scope.getVastausBaseUrl = function(){
      return $sce.trustAsResourceUrl(vastausBaseUrl+'/#/preview/');
    };
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }])
;
