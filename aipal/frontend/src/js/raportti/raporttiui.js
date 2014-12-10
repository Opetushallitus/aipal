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

  .factory('RaporttiFunktiot', [function() {
    return {
      tutkinnotHierarkiaksi: function(tutkinnot) {
        var tutkinnotOpintoaloittain = _.groupBy(tutkinnot, 'opintoalatunnus');

        var opintoalatKoulutusaloittain = _(tutkinnot).map(function(tutkinto) {
          return _.assign(_.pick(tutkinto, ['opintoalatunnus', 'opintoala_nimi_fi', 'opintoala_nimi_sv', 'koulutusalatunnus']), {tutkinnot: tutkinnotOpintoaloittain[tutkinto.opintoalatunnus]});
        }).sortBy('opintoalatunnus').uniq(true, 'opintoalatunnus').groupBy('koulutusalatunnus').value();

        return _(tutkinnot).map(function(tutkinto) {
          return _.assign(_.pick(tutkinto, ['koulutusalatunnus', 'koulutusala_nimi_fi', 'koulutusala_nimi_sv']), {opintoalat: opintoalatKoulutusaloittain[tutkinto.koulutusalatunnus]});
        }).sortBy('koulutusalatunnus').uniq(true, 'koulutusalatunnus').value();
      }
    };
  }])

  .controller('RaportitController', ['$scope', 'Koulutustoimija', 'Kysymysryhma', 'RaporttiFunktiot', 'Raportti', 'Tutkinto', 'kaavioApurit', 'i18n', 'ilmoitus', 'seuranta', function($scope, Koulutustoimija, Kysymysryhma, RaporttiFunktiot, Raportti, Tutkinto, kaavioApurit, i18n, ilmoitus, seuranta) {
    $scope.raportti = {};
    $scope.raportti.vertailutyyppi = 'tutkinto';

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

    Tutkinto.haeTutkinnot().success(function(tutkinnot) {
      $scope.tutkinnot = tutkinnot;

      $scope.koulutusalat = RaporttiFunktiot.tutkinnotHierarkiaksi(tutkinnot);

    });

    $scope.valitseKoulutusala = function(koulutusala) {
      if ($scope.raportti.vertailutyyppi === 'koulutusala') {
        $scope.raportti.koulutusalatunnus = koulutusala.koulutusalatunnus;
      }
    };
    $scope.valitseOpintoala = function(opintoala) {
      if ($scope.raportti.vertailutyyppi === 'opintoala') {
        $scope.raportti.opintoalatunnus = opintoala.opintoalatunnus;
      }
    };
    $scope.valitseTutkinto = function(tutkinto) {
      if ($scope.raportti.vertailutyyppi === 'tutkinto') {
        $scope.raportti.tutkintotunnus = tutkinto.tutkintotunnus;
      }
    };

    $scope.raportti.koulutustoimijat = [];
    $scope.valitseKoulutustoimija = function(koulutustoimija) {
      if (_.remove($scope.raportti.koulutustoimijat, function(ytunnus) { return ytunnus === koulutustoimija.ytunnus; }).length == 0) {
        $scope.raportti.koulutustoimijat.push(koulutustoimija.ytunnus);
      }
    };
    $scope.onkoKoulutustoimijaValittu = function(koulutustoimija) {
      return $scope.raportti.koulutustoimijat.indexOf(koulutustoimija.ytunnus) != -1;
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