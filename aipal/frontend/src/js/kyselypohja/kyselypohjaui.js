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

  .controller('KyselypohjatController', ['$location', '$scope', 'Kyselypohja', 'i18n', 'ilmoitus', function($location, $scope, Kyselypohja, i18n, ilmoitus) {
    $scope.luoUusiKyselypohja = function() {
      $location.url('/kyselypohjat/kyselypohja/uusi');
    };

    $scope.julkaiseKyselypohja = function(kyselypohja) {
      Kyselypohja.julkaise(kyselypohja).success(function(uusiKyselypohja) {
        ilmoitus.onnistuminen(i18n.hae('kyselypohja.julkaistu'));
        _.assign(kyselypohja, uusiKyselypohja);
      }).error(function() {
        ilmoitus.virhe(i18n.hae('kyselypohja.julkaisu_epaonnistui'));
      });
    };

    $scope.suljeKyselypohja = function(kyselypohja) {
      Kyselypohja.sulje(kyselypohja).success(function(uusiKyselypohja) {
        ilmoitus.onnistuminen(i18n.hae('kyselypohja.suljettu'));
        _.assign(kyselypohja, uusiKyselypohja);
      }).error(function() {
        ilmoitus.onnistuminen(i18n.hae('kyselypohja.sulkeminen_epaonnistui'));
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
      });
    } else {
      $scope.kyselypohja = {
        kysymysryhmat: [],
        voimassa_alkupvm: new Date().toISOString().slice(0, 10)
      };
    }
  }])
;
