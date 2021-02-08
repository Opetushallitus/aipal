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

angular.module('kysymysryhma.kysymysryhmaui', ['ngRoute',
                                               'ui.bootstrap',
                                               'rest.kysymysryhma',
                                               'yhteiset.palvelut.i18n',
                                               'yhteiset.palvelut.ilmoitus',
                                               'yhteiset.palvelut.kayttooikeudet',
                                               'yhteiset.palvelut.tallennusMuistutus',
                                               'yhteiset.palvelut.varmistus',
                                               'yhteiset.suodattimet.numerot',
                                               'yhteiset.suodattimet.tutkinto'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/kysymysryhmat', {
        controller: 'KysymysryhmatController',
        templateUrl: 'template/kysymysryhma/kysymysryhmat.html',
        label: 'i18n.kysymysryhma.kysymysryhmat'
      })
      .when('/kysymysryhmat/kysymysryhma/uusi', {
        controller: 'KysymysryhmaController',
        templateUrl: 'template/kysymysryhma/kysymysryhma.html',
        label: 'i18n.kysymysryhma.luo_uusi',
        resolve: {
          uusi: function() { return true; },
          kopioi: function() { return false; }
        }
      })
      .when('/kysymysryhmat/kysymysryhma/:kysymysryhmaid/kopioi', {
        controller: 'KysymysryhmaController',
        templateUrl: 'template/kysymysryhma/kysymysryhma.html',
        label: 'i18n.kysymysryhma.luo_uusi',
        resolve: {
          uusi: function() { return true; },
          kopioi: function() { return true; }
        }
      })
      .when('/kysymysryhmat/kysymysryhma/:kysymysryhmaid', {
        controller: 'KysymysryhmaController',
        templateUrl: 'template/kysymysryhma/kysymysryhma.html',
        label: 'i18n.kysymysryhma.muokkaa',
        resolve: {
          uusi: function() { return false; },
          kopioi: function() { return false; }
        }
      });
  }])

  .controller('KysymysryhmatController', ['$filter', '$scope', '$uibModal', 'Kysymysryhma',
                                          function($filter, $scope, $uibModal, Kysymysryhma) {
    $scope.latausValmis = false;
    Kysymysryhma.haeKaikki().then(function(resp){
      if (!resp.data) {
        console.error('resp.data missing');
      }
      // angular-tablesort haluaa lajitella rivioliosta löytyvän (filtteröidyn)
      // attribuutin perusteella, mutta lokalisoitujen kenttien kanssa täytyy
      // antaa filtterille koko rivi. Lisätään riviolioon viittaus itseensä,
      // jolloin voidaan kertoa angular-tablesortille attribuutti, josta koko
      // rivi löytyy.
      $scope.kysymysryhmat = _.map(resp.data, function(k){
        return _.assign(k, {self: k});
      });
      $scope.latausValmis = true;
    }).catch(function() {
      $scope.latausValmis = true;
    });
  }])

  .factory('kysymysApurit', [function() {
    var uusiVaihtoehto = function() {
      return {
        teksti_fi: '',
        teksti_sv: ''
      };
    };
    return {
      uusiKysymys: function() {
        return {
          uusi: true, // Jos on uusi, eikä muokkaus
          kysymys_fi: '',
          kysymys_sv: '',
          pakollinen: true,
          poistettava: false,
          poistetaan_kysymysryhmasta: false,
          vastaustyyppi: 'likert_asteikko',
          muokattava: true,
          jatkokysymykset: {},
          max_vastaus: 500,
          monivalinta_max: 1,
          monivalintavaihtoehdot: [uusiVaihtoehto(), uusiVaihtoehto()]
        };
      },

      poistaYlimaaraisetKentat: function(kysymys) {

        if (kysymys.vastaustyyppi !== 'kylla_ei_valinta') {
          kysymys.jatkokysymykset = {};
        }

        if (kysymys.vastaustyyppi !== 'vapaateksti') {
          delete kysymys.max_vastaus;
        }
        if (kysymys.vastaustyyppi !== 'monivalinta') {
          delete kysymys.monivalintavaihtoehdot;
          delete kysymys.monivalinta_max;
        }
      },
      uusiVaihtoehto: uusiVaihtoehto,
      poistaVaihtoehto: function(kysymys, index) {
        kysymys.monivalintavaihtoehdot.splice(index,1);
        if (kysymys.monivalinta_max > kysymys.monivalintavaihtoehdot.length) {
          kysymys.monivalinta_max = kysymys.monivalintavaihtoehdot.length;
        }
      }
    };
  }])

  .controller('KysymysryhmaController', ['$uibModal', '$routeParams', '$scope', '$location', 'Kysymysryhma', 'i18n', 'ilmoitus', 'kysymysApurit', 'tallennusMuistutus', 'uusi', 'kopioi', 'kayttooikeudet', function($uibModal, $routeParams, $scope, $location, Kysymysryhma, i18n, ilmoitus, apu, tallennusMuistutus, uusi, kopioi, kayttooikeudet) {
    $scope.$watch('form', function(form) {
      tallennusMuistutus.muistutaTallennuksestaPoistuttaessaFormilta(form);
    });

    $scope.uusi = uusi;

    $scope.selitteet = false;

    $scope.toggleSelitteet = function () {
      $scope.selitteet = !$scope.selitteet;
    };

    $scope.kysymysryhma = {
      kysymykset: []
    };

    if (kopioi || !uusi) {
      Kysymysryhma.hae($routeParams.kysymysryhmaid)
        .then(function(resp) {
          if (!resp.data) {
            console.error('resp.data missing');
          }
          const kysymysryhma = resp.data;
          if (kopioi) {
            delete kysymysryhma.kysymysryhmaid;
          }
          $scope.kysymysryhma = kysymysryhma;
        })
        .catch(function(data, status) {
          if (status !== 500) {
            $location.url('/kysymysryhmat');
          }
        });
    }

    $scope.muokkaustila = false;
    $scope.vastaustyypit = [
      'arvosana',
      'arvosana6',
      'arvosana7',
      'nps',
      'kylla_ei_valinta',
      'likert_asteikko',
      'monivalinta',
      'vapaateksti',
      'arvosana4_ja_eos',
      'asteikko5_1',
      'arvosana6_ja_eos',
      'valiotsikko',
      'alasvetovalikko'
    ];

    $scope.kysymystekstinTyyppi = function() {
      return $scope.aktiivinenKysymys.vastaustyyppi === 'valiotsikko' ? 'valiotsikko' : 'kysymys';
    };

    $scope.vapaateksti_maksimit = [10, 100, 500,1000,1500,2000,2500,3000];

    $scope.vapaateksti_rajoitteet = ['ei_rajoitetta', 'numero'];

    $scope.lisaaKysymys = function() {
      $scope.kysymysryhma.kysymykset.push(apu.uusiKysymys());
      $scope.aktiivinenKysymys = $scope.kysymysryhma.kysymykset[$scope.kysymysryhma.kysymykset.length-1];
      $scope.muokkaustila = true;
      $scope.selitteet = false;
    };

    $scope.lisaaVaihtoehto = function() {
      if($scope.aktiivinenKysymys.monivalintavaihtoehdot === undefined){
        $scope.aktiivinenKysymys.monivalintavaihtoehdot = [];
      }
      $scope.aktiivinenKysymys.monivalintavaihtoehdot.push(apu.uusiVaihtoehto());
    };

    $scope.poistaVaihtoehto = apu.poistaVaihtoehto;

    $scope.tallenna = function() {
      apu.poistaYlimaaraisetKentat($scope.aktiivinenKysymys);
      // Metatietojenpitää olla jsonia
      // $scope.aktiivinenKysymys.metatiedot = JSON.stringify($scope.aktiivinenKysymys.metatiedot);
      $scope.aktiivinenKysymys.muokattava = false;
      $scope.aktiivinenKysymys.uusi = false;
      $scope.muokkaustila = false;
    };

    $scope.peruutaKysymysTallennus = function() {
      $scope.aktiivinenKysymys.muokattava = false;
      $scope.muokkaustila = false;

      if (!$scope.aktiivinenKysymys.uusi ) {
        $scope.kysymysryhma.kysymykset = originals;
      }
      // Uudet "tyhjät" pois jos painetaan peruuta
      $scope.kysymysryhma.kysymykset = _.filter(
        $scope.kysymysryhma.kysymykset,
        function(kysymys) {return !kysymys.uusi;}
      );
    };

    $scope.asteikot = [];
    $scope.tallenusNakyvissa = false;
    $scope.latausNakyvissa = false;


    $scope.asteikkoValidi = function(){
      return _.every($scope.aktiivinenKysymys.monivalintavaihtoehdot, 'teksti_fi')
    };

    Kysymysryhma.haeAsteikot()
      .then(function(resp){
        if (!resp.data) {
          console.error('resp.data missing');
        }
        const data = resp.data;
        $scope.asteikot = data;
        if(data.length > 0){
          $scope.valittuAsteikko = data[0];
        }
      }).catch(function (e) {
      console.error(e);
    });

    $scope.lataaAsteikko = function(asteikko){
      if (asteikko === null) {
        return;
      }
      $scope.aktiivinenKysymys.monivalintavaihtoehdot = [];
      var vaihtoehdot = asteikko.asteikko.vaihtoehdot;
      _.forEach(vaihtoehdot, function(vaihtoehto){
        $scope.aktiivinenKysymys.monivalintavaihtoehdot.push(vaihtoehto);
      });
      $scope.latausNakyvissa = false;
    };

    $scope.nollaa = function () {
      if ($scope.aktiivinenKysymys) {
        console.log('Nollataan kysymys');
        $scope.aktiivinenKysymys.kysymys_fi = '';
        $scope.aktiivinenKysymys.kysymys_sv = '';
        $scope.aktiivinenKysymys.kysymys_en = '';
        $scope.aktiivinenKysymys.pakollinen = true;
        $scope.aktiivinenKysymys.poistettava = false;
        $scope.aktiivinenKysymys.poistetaan_kysymysryhmasta = false;
        $scope.aktiivinenKysymys.muokattava = true;
        $scope.aktiivinenKysymys.jatkokysymykset = {};
        $scope.aktiivinenKysymys.max_vastaus = 500;
        $scope.aktiivinenKysymys.monivalinta_max = 1;
        $scope.aktiivinenKysymys.monivalintavaihtoehdot = [apu.uusiVaihtoehto(), apu.uusiVaihtoehto()];
        $scope.aktiivinenKysymys.selite_fi = '';
        $scope.aktiivinenKysymys.selite_sv = '';
        $scope.aktiivinenKysymys.selite_en = '';
        $scope.aktiivinenKysymys.metatiedot = {};
      }
    };

    $scope.tallennaAsteikko = function(nimi){
      var asteikko = $scope.aktiivinenKysymys.monivalintavaihtoehdot;
      Kysymysryhma.tallennaAsteikko(nimi, asteikko)
        .then(function(resp){
          if (!resp.data) {
            console.error('resp.data missing');
          }
          const tallennettu = resp.data;
          $scope.asteikot.push(tallennettu);
        }).catch(function (e) {
        console.error(e);
      });
      $scope.tallennusNakyvissa = false;
    };

    $scope.naytaTallennus = function (nakyvissa) {
      $scope.tallennusNakyvissa = nakyvissa;
    };

    $scope.naytaLataus = function (nakyvissa) {
      $scope.latausNakyvissa = nakyvissa;
    };

    $scope.peruuta = function(){
      $location.path('/kysymysryhmat');
    };

    function luoUusiKysymysryhma(){
      Kysymysryhma.luoUusi($scope.kysymysryhma)
      .then(function(){
        $scope.form.$setPristine();
        $location.path('/kysymysryhmat');
        ilmoitus.onnistuminen(i18n.hae('kysymysryhma.luonti_onnistui'));
      })
      .catch(function(){
        ilmoitus.virhe(i18n.hae('kysymysryhma.luonti_epaonnistui'));
      });
    }

    function tallennaKysymysryhma() {
      Kysymysryhma.tallenna($scope.kysymysryhma)
      .then(function(){
        $scope.form.$setPristine();
        $location.path('/kysymysryhmat');
        ilmoitus.onnistuminen(i18n.hae('kysymysryhma.tallennus_onnistui'));
      })
      .catch(function(){
        ilmoitus.virhe(i18n.hae('kysymysryhma.tallennus_epaonnistui'));
      });
    }

    $scope.tallennaKysymysryhma = function() {
      $scope.poistaKysymykset();
      if (uusi) {
        luoUusiKysymysryhma();
      } else {
        tallennaKysymysryhma();
      }
    };

    $scope.naytaRakenneModal = function() {
      $uibModal.open({
        templateUrl: 'template/kysymysryhma/rakenne.html',
        controller: 'KysymysryhmaRakenneModalController',
        resolve: {
          kysymysryhma: function() { return $scope.kysymysryhma; }
        }
      }).result.then(function () { }).catch(function (e) {
        console.error(e);
      });
    };

    var originals = {};
    $scope.muokkaa = function(kysymys) {
      originals = angular.copy($scope.kysymysryhma.kysymykset);
      kysymys.muokattava = true;

      $scope.aktiivinenKysymys = kysymys;
      $scope.muokkaustila = true;
    };

    $scope.poistaTahiPalautaKysymys = function(kysymys) {
      kysymys.poistetaan_kysymysryhmasta = !kysymys.poistetaan_kysymysryhmasta;
    };

    $scope.poistaKysymykset = function(){
      $scope.kysymysryhma.kysymykset = _.reject($scope.kysymysryhma.kysymykset, 'poistetaan_kysymysryhmasta');
    };

    $scope.tallennusSallittu = function() {
      return $scope.form.$valid &&
        !$scope.muokkaustila;
    };
  }])

  .controller('KysymysryhmaRakenneModalController', ['$uibModalInstance', '$scope', 'kysymysryhma', function($uibModalInstance, $scope, kysymysryhma) {
    $scope.kysymysryhma = kysymysryhma;
    $scope.view = 'kysymysryhma';

    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };
  }]);
