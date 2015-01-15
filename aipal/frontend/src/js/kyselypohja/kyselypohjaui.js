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

angular.module('kyselypohja.kyselypohjaui', ['ngRoute'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/kyselypohjat', {
        controller: 'KyselypohjatController',
        templateUrl: 'template/kyselypohja/kyselypohjat.html',
        label: 'i18n.kyselypohja.breadcrumb_kyselypohja'
      })
      .when('/kyselypohjat/kyselypohja/uusi', {
        controller: 'KyselypohjaController',
        templateUrl: 'template/kyselypohja/kyselypohja.html',
        label: 'i18n.kyselypohja.breadcrumb_uusi_kyselypohja'
      })
      .when('/kyselypohjat/kyselypohja/:kyselypohjaid', {
        controller: 'KyselypohjaController',
        templateUrl: 'template/kyselypohja/kyselypohja.html',
        label: 'i18n.kyselypohja.breadcrumb_muokkaa_kyselypohjaa'
      })
    ;
  }])

  .controller('KyselypohjatController', ['$filter', '$location', '$scope', 'Kyselypohja', 'i18n', 'ilmoitus', 'varmistus', function($filter, $location, $scope, Kyselypohja, i18n, ilmoitus, varmistus) {
    $scope.luoUusiKyselypohja = function() {
      $location.url('/kyselypohjat/kyselypohja/uusi');
    };

    $scope.julkaiseKyselypohja = function(kyselypohja) {
      varmistus.varmista(i18n.hae('kyselypohja.julkaise'), $filter('lokalisoiKentta')(kyselypohja, 'nimi'), i18n.hae('kyselypohja.julkaise_teksti'), i18n.hae('kyselypohja.julkaise')).then(function() {
        Kyselypohja.julkaise(kyselypohja).success(function(uusiKyselypohja) {
          ilmoitus.onnistuminen(i18n.hae('kyselypohja.julkaistu'));
          _.assign(kyselypohja, uusiKyselypohja);
        }).error(function() {
          ilmoitus.virhe(i18n.hae('kyselypohja.julkaisu_epaonnistui'));
        });
      });
    };

    $scope.palautaKyselypohjaJulkaistuksi = function(kyselypohja) {
      varmistus.varmista(i18n.hae('kyselypohja.palauta_julkaistuksi'), $filter('lokalisoiKentta')(kyselypohja, 'nimi'), i18n.hae('kyselypohja.palauta_julkaistuksi_teksti'), i18n.hae('kyselypohja.palauta_julkaistuksi')).then(function() {
        Kyselypohja.julkaise(kyselypohja).success(function(uusiKyselypohja) {
          ilmoitus.onnistuminen(i18n.hae('kyselypohja.palautus_julkaistuksi_onnistui'));
          _.assign(kyselypohja, uusiKyselypohja);
        }).error(function() {
          ilmoitus.virhe(i18n.hae('kyselypohja.palautus_julkaistuksi_epaonnistui'));
        });
      });
    };

    $scope.palautaKyselypohjaLuonnokseksi= function(kyselypohja) {
      varmistus.varmista(i18n.hae('kyselypohja.palauta_luonnokseksi'), $filter('lokalisoiKentta')(kyselypohja, 'nimi'), i18n.hae('kyselypohja.palauta_luonnokseksi_teksti'), i18n.hae('kyselypohja.palauta_luonnokseksi')).then(function() {
        Kyselypohja.palautaLuonnokseksi(kyselypohja).success(function(uusiKyselypohja) {
          ilmoitus.onnistuminen(i18n.hae('kyselypohja.palautus_luonnokseksi_onnistui'));
          _.assign(kyselypohja, uusiKyselypohja);
        }).error(function() {
          ilmoitus.virhe(i18n.hae('kyselypohja.palautus_luonnokseksi_epaonnistui'));
        });
      });
    };

    $scope.suljeKyselypohja = function(kyselypohja) {
      varmistus.varmista(i18n.hae('kyselypohja.sulje'), $filter('lokalisoiKentta')(kyselypohja, 'nimi'), i18n.hae('kyselypohja.sulje_teksti'), i18n.hae('kyselypohja.sulje')).then(function() {
        Kyselypohja.sulje(kyselypohja).success(function(uusiKyselypohja) {
          ilmoitus.onnistuminen(i18n.hae('kyselypohja.suljettu'));
          _.assign(kyselypohja, uusiKyselypohja);
        }).error(function() {
          ilmoitus.onnistuminen(i18n.hae('kyselypohja.sulkeminen_epaonnistui'));
        });
      });
    };

    $scope.poistaKyselypohja = function(kyselypohjalista, kyselypohja) {
      varmistus.varmista(i18n.hae('kyselypohja.poista'), $filter('lokalisoiKentta')(kyselypohja, 'nimi'), i18n.hae('kyselypohja.poista_teksti'), i18n.hae('kyselypohja.poista')).then(function() {
        var kyselypohjaid = kyselypohja.kyselypohjaid;
        var kyselypohjaindex = _.findIndex(kyselypohjalista, {kyselypohjaid: kyselypohjaid});
        Kyselypohja.poista(kyselypohjaid).success(function() {
          kyselypohjalista.splice(kyselypohjaindex, 1);
          ilmoitus.onnistuminen(i18n.hae('kyselypohja.poistaminen_onnistui'));
        }).error(function() {
          ilmoitus.onnistuminen(i18n.hae('kyselypohja.poistaminen_epaonnistui'));
        });
      });
    };

    Kyselypohja.haeKaikki().success(function(kyselypohjat) {
      $scope.kyselypohjat = kyselypohjat;
    });
  }])

  .controller('KyselypohjaController', ['$location', '$modal', '$routeParams', '$scope', 'Kyselypohja', 'Kysymysryhma', 'i18n', 'ilmoitus', 'tallennusMuistutus', function($location, $modal, $routeParams, $scope, Kyselypohja, Kysymysryhma, i18n, ilmoitus, tallennusMuistutus) {
    $scope.lisaaKysymysryhmaModal = function() {
      var modalInstance = $modal.open({
        templateUrl: 'template/kysely/lisaa-kysymysryhma.html',
        controller: 'LisaaKysymysryhmaModalController'
      });
      modalInstance.result.then(function (kysymysryhmaid) {
        Kysymysryhma.hae(kysymysryhmaid)
          .success(function(kysymysryhma) {
            _.assign($scope.kyselypohja, { kysymysryhmat: _($scope.kyselypohja.kysymysryhmat.concat(kysymysryhma)).uniq('kysymysryhmaid').value() });
            $scope.kyselypohja.kysymysryhmat = _.sortBy($scope.kyselypohja.kysymysryhmat, function(kysymysryhma, index) {
              return (kysymysryhma.taustakysymykset ? 0 : 100) + (kysymysryhma.valtakunnallinen ? 0 : 1000) + index;
            });

            $scope.kyselypohjaForm.$setDirty();
          });
      });
    };

    $scope.poistaTaiPalautaKysymysryhma = function(kysymysryhma) {
      kysymysryhma.poistetaan_kyselysta = !kysymysryhma.poistetaan_kyselysta;
    };

    function poistaKysymysryhmat() {
      $scope.kyselypohja.kysymysryhmat = _.reject($scope.kyselypohja.kysymysryhmat, 'poistetaan_kyselysta');
    }

    $scope.tallenna = function() {
      poistaKysymysryhmat();
      if ($routeParams.kyselypohjaid) {
        Kyselypohja.muokkaa($scope.kyselypohja).success(function() {
          $scope.kyselypohjaForm.$setPristine();
          ilmoitus.onnistuminen(i18n.hae('kyselypohja.tallennus_onnistui'));
          $location.url('/kyselypohjat');
        }).error(function() {
          ilmoitus.virhe(i18n.hae('kyselypohja.tallennus_epaonnistui'));
        });
      } else {
        Kyselypohja.luoUusi($scope.kyselypohja).success(function() {
          $scope.kyselypohjaForm.$setPristine();
          ilmoitus.onnistuminen(i18n.hae('kyselypohja.tallennus_onnistui'));
          $location.url('/kyselypohjat');
        }).error(function() {
          ilmoitus.virhe(i18n.hae('kyselypohja.tallennus_epaonnistui'));
        });
      }
    };
    $scope.peruuta = function() {
      $location.url('/kyselypohjat');
    };

    $scope.$watch('kyselypohjaForm', function(form) {
      // watch tarvitaan koska form asetetaan vasta controllerin jälkeen
      tallennusMuistutus.muistutaTallennuksestaPoistuttaessaFormilta(form);
    });

    if ($routeParams.kyselypohjaid) {
      Kyselypohja.hae($routeParams.kyselypohjaid).success(function(kyselypohja) {
        $scope.kyselypohja = kyselypohja;
      }).error(function(data, status) {
        if (status !== 500) {
          $location.url('/kyselypohjat');
        }
      });
    } else {
      $scope.kyselypohja = {
        kysymysryhmat: [],
        voimassa_alkupvm: new Date().toISOString().slice(0, 10)
      };
    }
  }])
;
