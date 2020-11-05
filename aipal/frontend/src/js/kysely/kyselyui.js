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
                                   'rest.kysymysryhma', 'yhteiset.palvelut.seuranta',
                                   'ui.bootstrap'])

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
        kysely.kysymysryhmat = _.sortBy(kysely.kysymysryhmat, function(kysymysryhma, index) {
          return (kysymysryhma.taustakysymykset ? 0 : 100) + (kysymysryhma.valtakunnallinen ? 0 : 1000) + index;
        });
      },
      laskeLisakysymykset: function(kysely) {
        return _(kysely.kysymysryhmat).reject('valtakunnallinen').pluck('kysymykset').flatten().reject('poistettu').reduce(function(sum) { return sum + 1; }, 0);
      },
      valtakunnallisiaRyhmia: function(kysely) {
        return _.find(kysely.kysymysryhmat, 'valtakunnallinen');
      }
    };
  }])

  .controller('KyselyController', [
    'Kysely', 'Kyselypohja', 'Kysymysryhma', 'kyselyApurit', 'i18n', 'tallennusMuistutus', '$routeParams', '$route', '$scope', 'ilmoitus', '$location', '$uibModal', 'seuranta', '$timeout', 'kopioi', 'pvm',
    function (Kysely, Kyselypohja, Kysymysryhma, apu, i18n, tallennusMuistutus, $routeParams, $route, $scope, ilmoitus, $location, $uibModal, seuranta, $timeout, kopioi, pvm) {
      var tallennusFn = $routeParams.kyselyid ? Kysely.tallenna : Kysely.luoUusi;
      $scope.$watch('kyselyForm', function(form) {
        // watch tarvitaan koska form asetetaan vasta controllerin jälkeen
        tallennusMuistutus.muistutaTallennuksestaPoistuttaessaFormilta(form);
      });

      $scope.isJulkaistu = function() {
        return $scope.kysely && $scope.kysely.tila === 'julkaistu';
      };

      function haeKyselypohja(kyselypohjaid){
        Kyselypohja.hae(kyselypohjaid).success(function(kyselypohja){
          Kyselypohja.haeKysymysryhmat(kyselypohja.kyselypohjaid).success(
            function (kysymysryhmat){
              const pohja = {id: kyselypohja.kyselypohjaid,
                nimi_fi: kyselypohja.nimi_fi,
                nimi_sv: kyselypohja.nimi_sv,
                nimi_en: kyselypohja.nimi_en,
                valtakunnallinen: kyselypohja.valtakunnallinen,
                kysymysryhmat: _.map(kysymysryhmat, 'kysymysryhmaid')};
              _.map(kysymysryhmat, function(kr){_.set(kr, 'kyselypohjaid', pohja.id)});
              apu.lisaaUniikitKysymysryhmatKyselyyn($scope.kysely, kysymysryhmat);
              if(pohja.valtakunnallinen){
                $scope.kysely.kyselypohjaid = pohja.id;
                $scope.kyselypohja = pohja;
              }
              $scope.kyselyForm.$setDirty();
            }
          )
        })
      }

      $scope.kyselytyypit = [];

      $scope.kyselypohjat = [];

      Kysely.kyselytyypit()
        .success(function (tyypit){
          $scope.kyselytyypit = tyypit;
        });

      if ($routeParams.kyselyid) {
        Kysely.haeId($routeParams.kyselyid)
          .success(function(kysely) {
            $scope.kysely = pvm.parsePvm(kysely);

            $scope.kysely.tyyppi = _.find($scope.kyselytyypit, function(kt) {return kt.id === kysely.tyyppi});

            if(kysely.kyselypohjaid) {

              $scope.kyselypohjat.push(kysely.kyselypohjaid)

              haeKyselypohja(kysely.kyselypohjaid)
            }

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
          kysymysryhmat: []
        };
        $scope.kysely.tyyppi = $scope.kyselytyypit[0];
        tallennusFn = Kysely.luoUusi;
      }

      function poistaKysymysryhmat() {
        $scope.kysely.kysymysryhmat = _.reject($scope.kysely.kysymysryhmat, 'poistetaan_kyselysta');
      }

      $scope.tallenna = function () {
        poistaKysymysryhmat();
        var maxKysymyksia = 140;

        if (apu.laskeLisakysymykset($scope.kysely) > maxKysymyksia) {
          ilmoitus.virhe(i18n.hae('kysely.liian_monta_lisakysymysta'));
        }
        else {
          seuranta.asetaLatausIndikaattori(tallennusFn($scope.kysely), 'kyselynTallennus')
          .success(function () {
            $scope.kyselyForm.$setPristine();
            $location.path('/kyselyt');
            ilmoitus.onnistuminen(i18n.hae('kysely.tallennus_onnistui'));
          })
          .error(function (virhe) {
            ilmoitus.virhe(i18n.hae(virhe), i18n.hae('kysely.tallennus_epaonnistui'));
          });
        }
      };

      $scope.validoiKysymysryhmat = function() {
        if ($scope.kysely === undefined) {
          return true;
        }
        var kysymysryhmat = _.reject($scope.kysely.kysymysryhmat, 'poistetaan_kyselysta');
        var vainNtmKysymyksia = _.every(kysymysryhmat, 'ntm_kysymykset');
        var ntmKysymyksia = _.find(kysymysryhmat, 'ntm_kysymykset') !== undefined;
        var taustakysymysryhma = _.size(_.filter(kysymysryhmat, 'taustakysymykset')) === 1;
        var valtakunnallisia = _.find(kysymysryhmat, 'valtakunnallinen') !== undefined;
        return !valtakunnallisia || (!ntmKysymyksia && taustakysymysryhma) || (vainNtmKysymyksia);
      };

      $scope.peruuta = function() {
        $location.path('/kyselyt');
      };

      $scope.poistaKyselypohja = function(){
        $scope.kysely.kysymysryhmat = _.reject($scope.kysely.kysymysryhmat,
          function(kr){
          return !(_.includes(_.map($scope.kyselypohja.kysymysryhmat, 'kysymysryhmaid'), kr.kysymysryhmaid))});
        $scope.kysely.kyselypohjaid = null;
        $scope.kyselypohja = null;
      };

      $scope.lisaaKyselypohjaModal = function () {
        var modalInstance = $uibModal.open({
          templateUrl: 'template/kysely/lisaa-kyselypohja.html',
          controller: 'LisaaKyselypohjaModalController',
          resolve: {valittuPohja: function () {
              return $scope.kyselypohja
            }}
        });
        modalInstance.result.then(function (kyselypohja) {
          haeKyselypohja(kyselypohja.kyselypohjaid)
        });
      };

      $scope.lisaaKysymysryhmaModal = function() {
        var modalInstance = $uibModal.open({
          templateUrl: 'template/kysely/lisaa-kysymysryhma.html',
          controller: 'LisaaKysymysryhmaModalController',
          resolve: {
            isJulkaistu: function() {return $scope.isJulkaistu();},
            isPohja: function() {return false;}
          }
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
              _.forEach(kysymysryhma.kysymykset, function(kysymys){
                if(kysymys.jatkokysymys){
                  var kys = _.find(kysymysryhma.kysymykset, {'kysymysid': kysymys.jatkokysymys_kysymysid})
                  if(kys && kys.jarjestys) {
                    kysymys.jarjestys = kys.jarjestys + 0.1;
                  }
                }
              })
              kysymysryhma.kysymykset.sort(function(a, b){ return a.jarjestys > b.jarjestys})
            });

            var message = JSON.stringify({message: kysely});
            iframe.postMessage(message, '*');
          }, 1000);
        };

        $uibModal.open({
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

  .controller('LisaaKyselypohjaModalController', ['$uibModalInstance', '$scope', 'Kyselypohja', 'valittuPohja', function ($uibModalInstance, $scope, Kyselypohja, valittuPohja) {
    Kyselypohja.haeVoimassaolevat()
    .success(function (data) {
      $scope.kyselypohjat = _.filter(data, function(kp){return !kp.valtakunnallinen || (valittuPohja === null || valittuPohja === undefined)})

    });
    $scope.tallenna = function (valittuPohja) {
      $uibModalInstance.close(valittuPohja);
    };
    $scope.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };
  }])

  .controller('LisaaKysymysryhmaModalController', ['$uibModalInstance', '$scope', 'Kysymysryhma', 'isJulkaistu', 'isPohja', function ($uibModalInstance, $scope, Kysymysryhma, isJulkaistu, isPohja) {
    $scope.outerscope = {};
    $scope.isJulkaistu = isJulkaistu;
    $scope.isPohja = isPohja

    Kysymysryhma.haeVoimassaolevat().success(function(kysymysryhmat){
      console.log("julkaistu: " +  isJulkaistu + " pohja: " + isPohja)
      $scope.kysymysryhmat = _.filter(kysymysryhmat, function(kr) {
        return isPohja ? kr.valtakunnallinen : !kr.valtakunnallinen
      });
    });
    $scope.tallenna = function (kysymysryhmaid) {
      $uibModalInstance.close(kysymysryhmaid);
    };
    $scope.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };
  }])

  .controller('AvaaEsikatseluModalController', ['$uibModalInstance', '$scope', 'vastausBaseUrl', '$sce', function ($uibModalInstance, $scope, vastausBaseUrl, $sce) {
    $scope.getVastausBaseUrl = function(){
      return $sce.trustAsResourceUrl(vastausBaseUrl+'/#/preview/');
    };
    $scope.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };
  }])
;
