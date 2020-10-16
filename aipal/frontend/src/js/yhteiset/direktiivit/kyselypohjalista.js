// Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
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

angular.module('yhteiset.direktiivit.kyselypohjalista', ['yhteiset.palvelut.i18n', 'yhteiset.palvelut.ilmoitus', 'yhteiset.palvelut.kayttooikeudet', 'yhteiset.palvelut.lokalisointi'])

  .directive('kyselypohjalista', [function() {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        hakuehto: '=',
        kyselypohjat: '=',
        tila: '@'
      },
      templateUrl: 'template/yhteiset/direktiivit/kyselypohjalista.html',
      link: function(scope, element, attrs) {
        attrs.$observe('valtakunnalliset', function() {
          scope.valtakunnalliset = scope.$eval(attrs.valtakunnalliset);
        });
      },
      controller: ['$uibModal', '$location', '$filter', '$scope', 'kayttooikeudet', 'Kyselypohja', 'i18n', 'ilmoitus', 'varmistus', function($uibModal, $location, $filter, $scope, kayttooikeudet, Kyselypohja, i18n, ilmoitus, varmistus) {
        $scope.i18n = i18n;
        $scope.rajoitin = {
          tila: $scope.tila
        };

        $scope.naytaRakenneModal = function(kyselypohja) {
          $uibModal.open({
            templateUrl: 'template/kysymysryhma/rakenne.html',
            controller: 'KyselypohjaModalController',
            resolve: {
              kyselypohja: function() { return kyselypohja; }
            }
          }).result.then(function () { }).catch(function (e) {
            console.error(e);
          });
        };

        $scope.$watch('valtakunnalliset', function(valtakunnalliset) {
          if (valtakunnalliset !== undefined) {
            $scope.rajoitin.valtakunnallinen = valtakunnalliset;
          } else {
            delete $scope.rajoitin.valtakunnallinen;
          }
        });

        $scope.yllapitaja = function() {
          return kayttooikeudet.isYllapitaja();
        };

        $scope.julkaiseKyselypohja = function(kyselypohja) {
          varmistus.varmista(i18n.hae('kyselypohja.julkaise'), $filter('lokalisoiKentta')(kyselypohja, 'nimi'), i18n.hae('kyselypohja.julkaise_teksti'), i18n.hae('kyselypohja.julkaise')).then(function() {
            Kyselypohja.julkaise(kyselypohja).then(function(resp) {
              if (!resp.data) {
                console.error('resp.data missing');
              }
              const uusiKyselypohja = resp.data;
              ilmoitus.onnistuminen(i18n.hae('kyselypohja.julkaistu'));
              _.assign(kyselypohja, uusiKyselypohja);
              $location.url('/kyselypohjat/');
            }).catch(function() {
              ilmoitus.virhe(i18n.hae('kyselypohja.julkaisu_epaonnistui'));
            });
          }).catch(function (e) {
            console.error(e);
          });
        };

        $scope.poistaKyselypohja = function(kyselypohjalista, kyselypohja) {
          varmistus.varmista(i18n.hae('kyselypohja.poista'), $filter('lokalisoiKentta')(kyselypohja, 'nimi'), i18n.hae('kyselypohja.poista_teksti'), i18n.hae('kyselypohja.poista')).then(function() {
            var kyselypohjaid = kyselypohja.kyselypohjaid;
            var kyselypohjaindex = _.findIndex(kyselypohjalista, {kyselypohjaid: kyselypohjaid});
            Kyselypohja.poista(kyselypohjaid).then(function() {
              kyselypohjalista.splice(kyselypohjaindex, 1);
              ilmoitus.onnistuminen(i18n.hae('kyselypohja.poistaminen_onnistui'));
              $location.url('/kyselypohjat/');
            }).catch(function() {
              ilmoitus.onnistuminen(i18n.hae('kyselypohja.poistaminen_epaonnistui'));
            });
          }).catch(function (e) {
            console.error(e);
          });
        };

        $scope.palautaKyselypohjaLuonnokseksi = function(kyselypohja) {
          varmistus.varmista(i18n.hae('kyselypohja.palauta_luonnokseksi'), $filter('lokalisoiKentta')(kyselypohja, 'nimi'), i18n.hae('kyselypohja.palauta_luonnokseksi_teksti'), i18n.hae('kyselypohja.palauta_luonnokseksi')).then(function() {
            Kyselypohja.palautaLuonnokseksi(kyselypohja).then(function(resp) {
              if (!resp.data) {
                console.error('resp.data missing');
              }
              const uusiKyselypohja = resp.data;
              ilmoitus.onnistuminen(i18n.hae('kyselypohja.palautus_luonnokseksi_onnistui'));

              _.assign(kyselypohja, uusiKyselypohja);
              $location.url('/kyselypohjat/');
            }).catch(function() {
              ilmoitus.virhe(i18n.hae('kyselypohja.palautus_luonnokseksi_epaonnistui'));
            });
          }).catch(function (e) {
            console.error(e);
          });
        };

        $scope.suljeKyselypohja = function(kyselypohja) {
          varmistus.varmista(i18n.hae('kyselypohja.sulje'), $filter('lokalisoiKentta')(kyselypohja, 'nimi'), i18n.hae('kyselypohja.sulje_teksti'), i18n.hae('kyselypohja.sulje')).then(function() {
            Kyselypohja.sulje(kyselypohja).then(function(resp) {
              if (!resp.data) {
                console.error('resp.data missing');
              }
              const uusiKyselypohja = resp.data;
              ilmoitus.onnistuminen(i18n.hae('kyselypohja.suljettu'));
              _.assign(kyselypohja, uusiKyselypohja);
              $location.url('/kyselypohjat/');
            }).catch(function() {
              ilmoitus.onnistuminen(i18n.hae('kyselypohja.sulkeminen_epaonnistui'));
            });
          }).catch(function (e) {
            console.error(e);
          });
        };

        $scope.palautaKyselypohjaJulkaistuksi = function(kyselypohja) {
          varmistus.varmista(i18n.hae('kyselypohja.palauta_julkaistuksi'), $filter('lokalisoiKentta')(kyselypohja, 'nimi'), i18n.hae('kyselypohja.palauta_julkaistuksi_teksti'), i18n.hae('kyselypohja.palauta_julkaistuksi')).then(function() {
            Kyselypohja.julkaise(kyselypohja).then(function(resp) {
              if (!resp.data) {
                console.error('resp.data missing');
              }
              const uusiKyselypohja = resp.data;
              ilmoitus.onnistuminen(i18n.hae('kyselypohja.palautus_julkaistuksi_onnistui'));
              _.assign(kyselypohja, uusiKyselypohja);
              $location.url('/kyselypohjat/');
            }).catch(function() {
              ilmoitus.virhe(i18n.hae('kyselypohja.palautus_julkaistuksi_epaonnistui'));
            });
          }).catch(function (e) {
            console.error(e);
          });
        };

      }]
    };
  }])
;
