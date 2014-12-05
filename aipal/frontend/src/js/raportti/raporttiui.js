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

angular.module('raportti.raporttiui', ['rest.raportti', 'raportti.kyselykerta.kaavioapurit'])
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/raportit', {
        controller: 'RaportitController',
        templateUrl: 'template/raportti/raportit.html',
        label: 'i18n.raportit.breadcrumb_raportit'
      });
  }])

  .controller('RaportitController', ['$scope', 'Kysymysryhma', 'Raportti', 'Tutkinto', 'kaavioApurit', 'i18n', 'ilmoitus', 'seuranta', function($scope, Kysymysryhma, Raportti, Tutkinto, kaavioApurit, i18n, ilmoitus, seuranta) {
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

    Tutkinto.haeTutkinnot().success(function(tutkinnot) {
      $scope.tutkinnot = tutkinnot;

      var tutkinnotOpintoaloittain = _.groupBy(tutkinnot, 'opintoalatunnus');

      var opintoalatKoulutusaloittain = _(tutkinnot).map(function(tutkinto) {
        return _.assign(_.pick(tutkinto, ['opintoalatunnus', 'opintoala_nimi_fi', 'opintoala_nimi_sv', 'koulutusalatunnus']), {tutkinnot: tutkinnotOpintoaloittain[tutkinto.opintoalatunnus]});
      }).sortBy('opintoalatunnus').uniq(true, 'opintoalatunnus').groupBy('koulutusalatunnus').value();

      $scope.koulutusalat = _(tutkinnot).map(function(tutkinto) {
        return _.assign(_.pick(tutkinto, ['koulutusalatunnus', 'koulutusala_nimi_fi', 'koulutusala_nimi_sv']), {opintoalat: opintoalatKoulutusaloittain[tutkinto.koulutusalatunnus]});
      }).sortBy('koulutusalatunnus').uniq(true, 'koulutusalatunnus').value();

    });

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