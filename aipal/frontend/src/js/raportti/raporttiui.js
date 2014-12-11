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

angular.module('raportti.raporttiui', ['ngRoute', 'rest.raportti', 'raportti.kyselykerta.kaavioapurit'])
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/raportit', {
        controller: 'RaportitController',
        templateUrl: 'template/raportti/raportit.html',
        label: 'i18n.raportit.breadcrumb_raportit'
      });
  }])

  .controller('RaportitController', ['$scope', 'Koulutustoimija', 'Kysymysryhma', 'Raportti', 'Tutkinto', 'kaavioApurit', 'i18n', 'ilmoitus', 'seuranta', function($scope, Koulutustoimija, Kysymysryhma, Raportti, Tutkinto, kaavioApurit, i18n, ilmoitus, seuranta) {
    $scope.raportti = {};
    $scope.raportti.tyyppi = 'vertailu';
    $scope.raportti.vertailutyyppi = 'tutkinto';

    var poistaKoulutusalaValinnat = function() {
      _.forEach($scope.koulutusalat, function(koulutusala) {
        delete koulutusala.valittu;
      });
    };
    var poistaOpintoalaValinnat = function() {
      _.forEach($scope.koulutusalat, function(koulutusala) {
        _.forEach(koulutusala.opintoalat, function(opintoala) {
          delete opintoala.valittu;
        });
      });
    };
    var poistaTutkintoValinnat = function() {
      _.forEach($scope.koulutusalat, function(koulutusala) {
        _.forEach(koulutusala.opintoalat, function(opintoala) {
          _.forEach(opintoala.tutkinnot, function(tutkinto) {
            delete tutkinto.valittu;
          });
        });
      });
    };

    $scope.vaihdaTyyppi = function(tyyppi) {
      $scope.raportti.tyyppi = tyyppi;

      // Vain vertailuraportilla voi valita useamman tutkinnon/alan, joten tyhjennä valinnat raportin tyypin vaihtuessa
      poistaKoulutusalaValinnat();
      poistaOpintoalaValinnat();
      poistaTutkintoValinnat();
    };

    var haeTaustakysymykset = function(kysymysryhmaid) {
      Kysymysryhma.hae(kysymysryhmaid).success(function(kysymysryhma) {
        $scope.kysymysryhma = kysymysryhma;

        $scope.raportti.kysymykset = {};
        _.forEach(kysymysryhma.kysymykset, function(kysymys) {
          $scope.raportti.kysymykset[kysymys.kysymysid] = { monivalinnat: {} };
        });
      });
    };

    Kysymysryhma.haeVoimassaolevat().success(function(kysymysryhmat) {
      $scope.taustakysymysryhmat = _.filter(kysymysryhmat, 'taustakysymykset');

      $scope.$watch('raportti.taustakysymysryhmaid', function(kysymysryhmaid) {
        haeTaustakysymykset(kysymysryhmaid);
      });

      $scope.raportti.taustakysymysryhmaid = $scope.taustakysymysryhmat[0].kysymysryhmaid;
    });

    Koulutustoimija.haeKaikki().success(function(koulutustoimijat) {
      $scope.koulutustoimijat = koulutustoimijat;
    });

    Tutkinto.haeTutkinnot().success(function(koulutusalat) {
      $scope.koulutusalat = koulutusalat;
    });

    $scope.raportti.koulutusalat = [];
    $scope.valitseKoulutusala = function(koulutusala) {
      if ($scope.raportti.vertailutyyppi === 'koulutusala') {
        // Vain vertailuraportilla voi valita useamman
        if ($scope.raportti.tyyppi !== 'vertailu-fixme' && !koulutusala.valittu) {
          poistaKoulutusalaValinnat();
        }
        koulutusala.valittu = !koulutusala.valittu;
        $scope.raportti.koulutusalat = _.xor($scope.raportti.koulutusalat, [koulutusala.koulutusalatunnus]);
      }
    };
    $scope.raportti.opintoalat = [];
    $scope.valitseOpintoala = function(opintoala) {
      if ($scope.raportti.vertailutyyppi === 'opintoala') {
        if ($scope.raportti.tyyppi !== 'vertailu-fixme' && !opintoala.valittu) {
          poistaOpintoalaValinnat();
        }
        opintoala.valittu = !opintoala.valittu;
        $scope.raportti.opintoalat = _.xor($scope.raportti.opintoalat, [opintoala.opintoalatunnus]);
      }
    };
    $scope.raportti.tutkinnot = [];
    $scope.valitseTutkinto = function(tutkinto) {
      if ($scope.raportti.vertailutyyppi === 'tutkinto') {
        if ($scope.raportti.tyyppi !== 'vertailu-fixme' && !tutkinto.valittu) {
          poistaTutkintoValinnat();
        }
        tutkinto.valittu = !tutkinto.valittu;
        $scope.raportti.tutkinnot = _.xor($scope.raportti.tutkinnot, [tutkinto.tutkintotunnus]);
      }
    };

    $scope.raportti.koulutustoimijat = [];
    $scope.valitseTaiPoistaKoulutustoimija = function(koulutustoimija) {
      if (_.remove($scope.raportti.koulutustoimijat, function(ytunnus) { return ytunnus === koulutustoimija.ytunnus; }).length === 0) {
        koulutustoimija.valittu = true;
        $scope.raportti.koulutustoimijat.push(koulutustoimija.ytunnus);
      } else {
        delete koulutustoimija.valittu;
      }
    };

    $scope.muodostaRaportti = function() {
      seuranta.asetaLatausIndikaattori(Raportti.muodosta($scope.raportti), 'raportinMuodostus').success(function(tulos) {
        $scope.tulos = tulos;
      }).error(function(data, status) {
        if (status !== 500) {
          ilmoitus.virhe(i18n.hae('raportti.muodostus_epaonnistui'));
        }
      });
    };

    $scope.lukumaaratYhteensa = kaavioApurit.lukumaaratYhteensa;
  }])
;