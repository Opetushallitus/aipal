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

angular.module('yhteiset.direktiivit.kysymysryhmalista', ['yhteiset.palvelut.i18n', 'yhteiset.palvelut.ilmoitus', 'yhteiset.palvelut.lokalisointi'])

  .directive('kysymysryhmalista', [function() {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        hakuehto: '=',
        kysymysryhmat: '=',
        tila: '@'
      },
      templateUrl: 'template/yhteiset/direktiivit/kysymysryhmalista.html',
      link: function(scope, element, attrs) {
        attrs.$observe('valtakunnalliset', function() {
          scope.valtakunnalliset = scope.$eval(attrs.valtakunnalliset);
        });
      },
      controller: ['$filter', '$modal', '$scope', 'Kysymysryhma', 'i18n', 'ilmoitus', 'varmistus', function($filter, $modal, $scope, Kysymysryhma, i18n, ilmoitus, varmistus) {
        $scope.i18n = i18n;
        $scope.rajoitin = {
          tila: $scope.tila
        };
        $scope.$watch('valtakunnalliset', function(valtakunnalliset) {
          if (valtakunnalliset !== undefined) {
            $scope.rajoitin.valtakunnallinen = valtakunnalliset;
          } else {
            delete $scope.rajoitin.valtakunnallinen;
          }
        });

        $scope.julkaiseKysymysryhmaModal = function(kysymysryhma) {
          var modalInstance = $modal.open({
            templateUrl: 'template/yhteiset/direktiivit/julkaise-kysymysryhma.html',
            controller: 'JulkaiseKysymysryhmaModalController',
            resolve: {
              kysymysryhma: function() {
                return Kysymysryhma.hae(kysymysryhma.kysymysryhmaid).then(function(response) { return response.data; });
              }
            }
          });
          modalInstance.result.then(function () {
            Kysymysryhma.julkaise(kysymysryhma)
              .success(function(uusiKysymysryhma) {
                _.assign(kysymysryhma, uusiKysymysryhma);
                ilmoitus.onnistuminen(i18n.hae('kysymysryhma.julkaisu_onnistui'));
              })
              .error(function() {
                ilmoitus.virhe(i18n.hae('kysymysryhma.julkaisu_epaonnistui'));
              });
          });
        };

        $scope.suljeKysymysryhma = function(kysymysryhma) {
          varmistus.varmista(i18n.hae('kysymysryhma.sulje'), $filter('lokalisoiKentta')(kysymysryhma, 'nimi'), i18n.hae('kysymysryhma.sulje_teksti'), i18n.hae('kysymysryhma.sulje')).then(function() {
            Kysymysryhma.sulje(kysymysryhma)
              .success(function(uusiKysymysryhma) {
                _.assign(kysymysryhma, uusiKysymysryhma);
                ilmoitus.onnistuminen(i18n.hae('kysymysryhma.sulkeminen_onnistui'));
              })
              .error(function() {
                ilmoitus.virhe(i18n.hae('kysymysryhma.sulkeminen_epaonnistui'));
              });
          });
        };

        $scope.poistaKysymysryhma = function(kysymysryhmalista, kysymysryhma) {
          varmistus.varmista(i18n.hae('kysymysryhma.poista'), $filter('lokalisoiKentta')(kysymysryhma, 'nimi'), i18n.hae('kysymysryhma.poista_teksti'), i18n.hae('kysymysryhma.poista')).then(function() {
            var kysymysryhmaid = kysymysryhma.kysymysryhmaid;
            var kysymysryhmaindex = _.findIndex(kysymysryhmalista, {kysymysryhmaid: kysymysryhmaid});
            Kysymysryhma.poista(kysymysryhmaid)
              .success(function() {
                kysymysryhmalista.splice(kysymysryhmaindex, 1);
                ilmoitus.onnistuminen(i18n.hae('kysymysryhma.poistaminen_onnistui'));
              })
              .error(function() {
                ilmoitus.virhe(i18n.hae('kysymysryhma.poistaminen_epaonnistui'));
              });
          });
        };

        $scope.palautaKysymysryhmaJulkaistuksi = function(kysymysryhma) {
          varmistus.varmista(i18n.hae('kysymysryhma.palauta_julkaistuksi'), $filter('lokalisoiKentta')(kysymysryhma, 'nimi'), i18n.hae('kysymysryhma.palauta_julkaistuksi_teksti'), i18n.hae('kysymysryhma.palauta_julkaistuksi')).then(function() {
            Kysymysryhma.julkaise(kysymysryhma)
              .success(function(uusiKysymysryhma) {
                _.assign(kysymysryhma, uusiKysymysryhma);
                ilmoitus.onnistuminen(i18n.hae('kysymysryhma.palautus_julkaistuksi_onnistui'));
              })
              .error(function() {
                ilmoitus.virhe(i18n.hae('kysymysryhma.palautus_julkaistuksi_epaonnistui'));
              });
          });
        };

        $scope.palautaKysymysryhmaLuonnokseksi = function(kysymysryhma) {
          varmistus.varmista(i18n.hae('kysymysryhma.palauta_luonnokseksi'), $filter('lokalisoiKentta')(kysymysryhma, 'nimi'), i18n.hae('kysymysryhma.palauta_luonnokseksi_teksti'), i18n.hae('kysymysryhma.palauta_luonnokseksi')).then(function() {
            Kysymysryhma.palautaLuonnokseksi(kysymysryhma)
              .success(function(uusiKysymysryhma) {
                _.assign(kysymysryhma, uusiKysymysryhma);
                ilmoitus.onnistuminen(i18n.hae('kysymysryhma.palautus_luonnokseksi_onnistui'));
              })
              .error(function() {
                ilmoitus.virhe(i18n.hae('kysymysryhma.palautus_luonnokseksi_epaonnistui'));
              });
          });
        };
      }]
    };
  }])

  .controller('JulkaiseKysymysryhmaModalController', ['$modalInstance', '$scope', 'kysymysryhma', function ($modalInstance, $scope, kysymysryhma) {
    $scope.kysymysryhma = kysymysryhma;

    $scope.julkaise = $modalInstance.close;
    $scope.cancel = $modalInstance.dismiss;
  }])
;