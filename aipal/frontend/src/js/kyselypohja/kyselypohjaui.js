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

  .controller('KyselypohjatController', ['$filter', '$location', '$scope', 'Kyselypohja', '$window', function($filter, $location, $scope, Kyselypohja, $window) {
    $scope.luoUusiKyselypohja = function() {
      $location.url('/kyselypohjat/kyselypohja/uusi');
    };

    $scope.uploadFile = function(file){
      var reader = new FileReader();

      reader.onload = function(e) {
        var data = e.target.result
        Kyselypohja.lisaaTiedostosta(data).success(function(kyselypohja){
          $window.location.reload();
        })
      }
      reader.readAsText(file, 'UTF-8')
    }

    $scope.fileChanged = function(ele){
      var files = ele.files;
      if(files.length > 0 ){
        $scope.uploadFile(files[0]);
      }
    }

    Kyselypohja.haeKaikki().success(function(kyselypohjat) {
      // angular-tablesort haluaa lajitella rivioliosta löytyvän (filtteröidyn)
      // attribuutin perusteella, mutta lokalisoitujen kenttien kanssa täytyy
      // antaa filtterille koko rivi. Lisätään riviolioon viittaus itseensä,
      // jolloin voidaan kertoa angular-tablesortille attribuutti, josta koko
      // rivi löytyy.
      $scope.kyselypohjat = _.map(kyselypohjat, function(k){
        return _.assign(k, {self: k});
      });
    });
  }])

  .controller('KyselypohjaController', ['$location', '$uibModal', '$routeParams', '$scope', 'Kyselypohja', 'Kysymysryhma', 'i18n', 'ilmoitus', 'tallennusMuistutus', 'pvm',
    function($location, $uibModal, $routeParams, $scope, Kyselypohja, Kysymysryhma, i18n, ilmoitus, tallennusMuistutus, pvm) {
    $scope.lisaaKysymysryhmaModal = function() {
      var modalInstance = $uibModal.open({
        templateUrl: 'template/kysely/lisaa-kysymysryhma.html',
        controller: 'LisaaKysymysryhmaModalController',
        resolve: {
          isJulkaistu: function() { return $scope.kyselypohja.tila === 'julkaistu';},
          isPohja: function() {return true;}
        }
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

    $scope.naytaRakenneModal = function() {
      $uibModal.open({
        templateUrl: 'template/kysymysryhma/rakenne.html',
        controller: 'KyselypohjaModalController',
        resolve: {
          kyselypohja: function() { return $scope.kyselypohja; }
        }
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
        $scope.kyselypohja = pvm.parsePvm(kyselypohja);
      }).error(function(data, status) {
        if (status !== 500) {
          $location.url('/kyselypohjat');
        }
      });
    } else {
      $scope.kyselypohja = {
        kysymysryhmat: [],
        voimassa_alkupvm: new Date()
      };
    }
  }])
  .controller('KyselypohjaModalController', ['$uibModalInstance', '$scope', 'kyselypohja', 'kayttooikeudet', 'Kyselypohja', function($uibModalInstance, $scope, kyselypohja, kayttooikeudet, Kyselypohja) {

    /* Luo Kyselypohjista kysymykset -arrayn jota rakenne.html -template ymmärtää */
    var kysymykset = [];

    // Otsikon kielimuuttujaa varten
    $scope.view = 'kyselypohjat';

    $scope.yllapitaja = function() {
      return kayttooikeudet.isYllapitaja();
    };

    var setKysymykset = function(kyselypohja) {
      _.each(kyselypohja.kysymysryhmat, function(x){
        if(!x.poistetaan_kyselysta) {
          _.each(x.kysymykset, function(y){
            kysymykset.push(y);
          });
        }
      });
      $scope.kysymysryhma = {kysymykset: kysymykset};
    };

    if(!kyselypohja.kysymysryhmat) {
      Kyselypohja.hae(kyselypohja.kyselypohjaid).success(function(kyselypohja) {
        setKysymykset(kyselypohja);
      });
    }else{
      setKysymykset(kyselypohja);
    }

    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

  }])
;
