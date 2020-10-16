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

angular.module('yhteiset.direktiivit.kysymysryhmalista', ['yhteiset.palvelut.i18n', 'yhteiset.palvelut.ilmoitus', 'yhteiset.palvelut.kayttooikeudet', 'yhteiset.palvelut.lokalisointi'])

  .directive('kysymysryhmalista', ['kayttooikeudet', function(kayttooikeudet) {
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
      controller: ['$location', '$filter', '$uibModal', '$scope', 'Kysymysryhma', 'i18n', 'ilmoitus', 'varmistus', function($location, $filter, $uibModal, $scope, Kysymysryhma, i18n, ilmoitus, varmistus) {
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

        $scope.yllapitaja = function() {
          return kayttooikeudet.isYllapitaja();
        };
        $scope.julkaiseKysymysryhmaModal = function(kysymysryhma) {
          var modalInstance = $uibModal.open({
            templateUrl: 'template/yhteiset/direktiivit/julkaise-kysymysryhma.html',
            controller: 'JulkaiseKysymysryhmaModalController',
            resolve: {
              kysymysryhma: function() {
                return Kysymysryhma.hae(kysymysryhma.kysymysryhmaid).then(function(resp) {
                  if (!resp.data) {
                    console.error('resp.data missing');
                  }
                  return resp.data;
                }).catch(function (e) {
                  console.error(e);
                });
              }
            }
          });
          modalInstance.result.then(function () {
            Kysymysryhma.julkaise(kysymysryhma)
              .then(function(resp) {
                if (!resp.data) {
                  console.error('resp.data missing');
                }
                const uusiKysymysryhma = resp.data;
                _.assign(kysymysryhma, uusiKysymysryhma);
                ilmoitus.onnistuminen(i18n.hae('kysymysryhma.julkaisu_onnistui'));
                $location.url('/kysymysryhmat/');
              })
              .catch(function() {
                ilmoitus.virhe(i18n.hae('kysymysryhma.julkaisu_epaonnistui'));
              });
          }).catch(function (e) {
            console.error(e);
          });
        };

        $scope.naytaRakenneModal = function(kysymysryhma) {
            $uibModal.open({
              templateUrl: 'template/kysymysryhma/rakenne.html',
              controller: 'KysymysryhmaRakenneModalController',
              resolve: {
                kysymysryhma: function() {
                  return Kysymysryhma.hae(kysymysryhma.kysymysryhmaid).then(function(resp) {
                    if (!resp.data) {
                      console.error('resp.data missing');
                    }
                    return resp.data;
                  }).catch(function (e) {
                    console.error(e);
                  });
                }
              }
            }).result.then(function () { }).catch(function (e) {
              console.error(e);
            });
          };

        $scope.suljeKysymysryhma = function(kysymysryhma) {
          varmistus.varmista(i18n.hae('kysymysryhma.sulje'), $filter('lokalisoiKentta')(kysymysryhma, 'nimi'), i18n.hae('kysymysryhma.sulje_teksti'), i18n.hae('kysymysryhma.sulje')).then(function() {
            Kysymysryhma.sulje(kysymysryhma)
              .then(function(resp) {
                if (!resp.data) {
                  console.error('resp.data missing');
                }
                const uusiKysymysryhma = resp.data;
                _.assign(kysymysryhma, uusiKysymysryhma);
                ilmoitus.onnistuminen(i18n.hae('kysymysryhma.sulkeminen_onnistui'));
                $location.url('/kysymysryhmat/');
              })
              .catch(function() {
                ilmoitus.virhe(i18n.hae('kysymysryhma.sulkeminen_epaonnistui'));
              });
          }).catch(function (e) {
            console.error(e);
          });
        };

        $scope.poistaKysymysryhma = function(kysymysryhmalista, kysymysryhma) {
          varmistus.varmista(i18n.hae('kysymysryhma.poista'), $filter('lokalisoiKentta')(kysymysryhma, 'nimi'), i18n.hae('kysymysryhma.poista_teksti'), i18n.hae('kysymysryhma.poista')).then(function() {
            var kysymysryhmaid = kysymysryhma.kysymysryhmaid;
            var kysymysryhmaindex = _.findIndex(kysymysryhmalista, {kysymysryhmaid: kysymysryhmaid});
            Kysymysryhma.poista(kysymysryhmaid)
              .then(function() {
                kysymysryhmalista.splice(kysymysryhmaindex, 1);
                ilmoitus.onnistuminen(i18n.hae('kysymysryhma.poistaminen_onnistui'));
                $location.url('/kysymysryhmat/');
              })
              .catch(function() {
                ilmoitus.virhe(i18n.hae('kysymysryhma.poistaminen_epaonnistui'));
              });
          }).catch(function (e) {
            console.error(e);
          });
        };

        $scope.palautaKysymysryhmaJulkaistuksi = function(kysymysryhma) {
          varmistus.varmista(i18n.hae('kysymysryhma.palauta_julkaistuksi'), $filter('lokalisoiKentta')(kysymysryhma, 'nimi'), i18n.hae('kysymysryhma.palauta_julkaistuksi_teksti'), i18n.hae('kysymysryhma.palauta_julkaistuksi')).then(function() {
            Kysymysryhma.julkaise(kysymysryhma)
              .then(function(resp) {
                if (!resp.data) {
                  console.error('resp.data missing');
                }
                const uusiKysymysryhma = resp.data;
                _.assign(kysymysryhma, uusiKysymysryhma);
                ilmoitus.onnistuminen(i18n.hae('kysymysryhma.palautus_julkaistuksi_onnistui'));
                $location.url('/kysymysryhmat/');
              })
              .catch(function() {
                ilmoitus.virhe(i18n.hae('kysymysryhma.palautus_julkaistuksi_epaonnistui'));
              });
          }).catch(function (e) {
            console.error(e);
          });
        };

        $scope.palautaKysymysryhmaLuonnokseksi = function(kysymysryhma) {
          varmistus.varmista(i18n.hae('kysymysryhma.palauta_luonnokseksi'), $filter('lokalisoiKentta')(kysymysryhma, 'nimi'), i18n.hae('kysymysryhma.palauta_luonnokseksi_teksti'), i18n.hae('kysymysryhma.palauta_luonnokseksi')).then(function() {
            Kysymysryhma.palautaLuonnokseksi(kysymysryhma)
              .then(function(resp) {
                if (!resp.data) {
                  console.error('resp.data missing');
                }
                const uusiKysymysryhma = resp.data;
                _.assign(kysymysryhma, uusiKysymysryhma);
                ilmoitus.onnistuminen(i18n.hae('kysymysryhma.palautus_luonnokseksi_onnistui'));
                $location.url('/kysymysryhmat/');
              })
              .catch(function() {
                ilmoitus.virhe(i18n.hae('kysymysryhma.palautus_luonnokseksi_epaonnistui'));
              });
          }).catch(function (e) {
            console.error(e);
          });
        };
      }]
    };
  }])

  .controller('JulkaiseKysymysryhmaModalController', ['$uibModalInstance', '$scope', 'kysymysryhma', function ($uibModalInstance, $scope, kysymysryhma) {
    $scope.kysymysryhma = kysymysryhma;

    $scope.julkaise = $uibModalInstance.close;
    $scope.cancel = $uibModalInstance.dismiss;
  }])
;
