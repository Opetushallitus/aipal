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

angular.module('kysymysryhma.kysymysryhmaui', ['ngRoute', 'rest.kysymysryhma',
                                               'yhteiset.palvelut.i18n',
                                               'yhteiset.palvelut.ilmoitus',
                                               'yhteiset.palvelut.tallennusMuistutus',
                                               'yhteiset.suodattimet.numerot'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/kysymysryhmat', {
        controller: 'KysymysryhmatController',
        templateUrl: 'template/kysymysryhma/kysymysryhmat.html',
        label: 'i18n.kysymysryhma.kysymysryhmat'
      })
      .when('/kysymysryhmat/kysymysryhma/uusi', {
        controller: 'UusiKysymysryhmaController',
        templateUrl: 'template/kysymysryhma/uusi.html',
        label: 'i18n.kysymysryhma.luo_uusi',
        resolve: {
          uusi: function() { return true; }
        }
      })
      .when('/kysymysryhmat/kysymysryhma/:kysymysryhmaid', {
        controller: 'UusiKysymysryhmaController',
        templateUrl: 'template/kysymysryhma/uusi.html',
        label: 'i18n.kysymysryhma.muokkaa',
        resolve: {
          uusi: function() { return false; }
        }
      });
  }])

  .controller('KysymysryhmatController', ['$scope', 'Kysymysryhma',
                                          function($scope, Kysymysryhma) {
    $scope.latausValmis = false;
    Kysymysryhma.haeKaikki().success(function(kysymysryhmat){
      $scope.kysymysryhmat = kysymysryhmat;
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
          vastaustyyppi: 'asteikko',
          muokattava: true,
          jatkokysymys: {max_vastaus: 500},
          monivalinta_max: 1,
          monivalintavaihtoehdot: [uusiVaihtoehto(), uusiVaihtoehto()]
        };
      },
      poistaYlimaaraisetKentat: function(kysymys) {
        if (kysymys.jatkokysymys !== undefined) {
          if (!kysymys.jatkokysymys.kylla_jatkokysymys && !kysymys.jatkokysymys.ei_jatkokysymys) {
            delete kysymys.jatkokysymys;
          }
          else {
            if (!kysymys.jatkokysymys.kylla_jatkokysymys) {
              delete kysymys.jatkokysymys.kylla_teksti_fi;
              delete kysymys.jatkokysymys.kylla_teksti_sv;
              delete kysymys.jatkokysymys.kylla_jatkokysymys;
            }
            if (!kysymys.jatkokysymys.ei_jatkokysymys) {
              delete kysymys.jatkokysymys.ei_teksti_fi;
              delete kysymys.jatkokysymys.ei_teksti_sv;
              delete kysymys.jatkokysymys.max_vastaus;
              delete kysymys.jatkokysymys.ei_jatkokysymys;
            }
          }
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

  .controller('UusiKysymysryhmaController', ['$routeParams', '$scope', '$location', 'Kysymysryhma', 'i18n', 'ilmoitus', 'kysymysApurit', 'tallennusMuistutus', 'uusi',
                                             function($routeParams, $scope, $location, Kysymysryhma, i18n, ilmoitus, apu, tallennusMuistutus, uusi) {
    $scope.$watch('form', function(form) {
      tallennusMuistutus.muistutaTallennuksestaPoistuttaessaFormilta(form);
    });

    $scope.uusi = uusi;

    $scope.kysymysryhma = {
      kysymykset: []
    };
    if (!uusi) {
      Kysymysryhma.hae($routeParams.kysymysryhmaid)
      .success(function(kysymysryhma) {
        $scope.kysymysryhma = kysymysryhma;
      });
    }

    $scope.muokkaustila = false;
    $scope.vastaustyypit = [
      'asteikko',
      'kylla_ei_valinta',
      'monivalinta',
      'vapaateksti'
    ];
    $scope.vapaateksti_maksimit = [500,1000,1500,2000,2500,3000];
    $scope.aktiivinenKysymys = {vastaustyyppi: 'asteikko'};

    $scope.lisaaKysymys = function() {
      $scope.kysymysryhma.kysymykset.push(apu.uusiKysymys());
      $scope.aktiivinenKysymys = $scope.kysymysryhma.kysymykset[$scope.kysymysryhma.kysymykset.length-1];
      $scope.muokkaustila = true;
    };

    $scope.lisaaVaihtoehto = function() {
      $scope.aktiivinenKysymys.monivalintavaihtoehdot.push(apu.uusiVaihtoehto());
    };
    $scope.poistaVaihtoehto = apu.poistaVaihtoehto;
    $scope.tallenna = function() {
      apu.poistaYlimaaraisetKentat($scope.aktiivinenKysymys);
      $scope.aktiivinenKysymys.muokattava = false;
      $scope.aktiivinenKysymys.uusi = false;
      $scope.muokkaustila = false;
    };

    $scope.peruutaKysymysTallennus = function(){
      $scope.aktiivinenKysymys.muokattava = false;
      $scope.muokkaustila = false;

      if(!$scope.aktiivinenKysymys.uusi ){
        $scope.kysymysryhma.kysymykset = originals;
      }
      // Uudet "tyhjät" pois jos painetaan peruuta
      $scope.kysymysryhma.kysymykset = _.filter(
        $scope.kysymysryhma.kysymykset,
        function(kysymys) {return !kysymys.uusi;}
      );
    };

    $scope.peruuta = function(){
      $location.path('/kysymysryhmat');
    };

    function luoUusiKysymysryhma(){
      Kysymysryhma.luoUusi($scope.kysymysryhma)
      .success(function(){
        $scope.form.$setPristine();
        $location.path('/kysymysryhmat');
        ilmoitus.onnistuminen(i18n.hae('kysymysryhma.luonti_onnistui'));
      })
      .error(function(){
        ilmoitus.virhe(i18n.hae('kysymysryhma.luonti_epaonnistui'));
      });
    }

    function tallennaKysymysryhma() {
      Kysymysryhma.tallenna($scope.kysymysryhma)
      .success(function(){
        $scope.form.$setPristine();
        ilmoitus.onnistuminen(i18n.hae('kysymysryhma.tallennus_onnistui'));
      })
      .error(function(){
        ilmoitus.virhe(i18n.hae('kysymysryhma.tallennus_epaonnistui'));
      });
    }

    $scope.tallennaKysymysryhma = function() {
      if (uusi) {
        luoUusiKysymysryhma();
      } else {
        tallennaKysymysryhma();
      }
    };

    var originals = {};
    $scope.muokkaa = function(kysymys) {
      originals = angular.copy($scope.kysymysryhma.kysymykset);
      kysymys.muokattava = true;
      $scope.aktiivinenKysymys = kysymys;
      $scope.muokkaustila = true;
    };
  }]);
