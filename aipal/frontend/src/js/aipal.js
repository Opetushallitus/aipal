// Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
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

angular.module('aipal', [
  'angular-loading-bar',
  'etusivu.etusivuui',
  'kysely.kyselyui',
  'kyselypohja.kyselypohjaui',
  'kysymysryhma.kysymysryhmaui',
  'vastaajatunnus.vastaajatunnusui',
  'raportti.kyselykerta.kyselykertaui',
  'yhteiset.palvelut.i18n',
  'yhteiset.palvelut.apicallinterceptor',
  'yhteiset.palvelut.lokalisointi',
  'yhteiset.palvelut.virheLogitus',
  'yhteiset.direktiivit.copyright',
  'yhteiset.direktiivit.navigaatio',
  'yhteiset.direktiivit.popup-ikkuna',
  'yhteiset.direktiivit.input',
  'yhteiset.direktiivit.pvm-valitsin',
  'yhteiset.direktiivit.latausindikaattori',
  'yhteiset.direktiivit.pakollisia-kenttia',
  'yhteiset.direktiivit.tallenna',
  'yhteiset.direktiivit.hakuvalitsin',
  'ui.bootstrap',
  'ngRoute',
  'kayttooikeudet',
  'ui.select2'
])

  .config(['$httpProvider', 'asetukset', function ($httpProvider, asetukset) {
    $httpProvider.interceptors.push(
      function (apiCallInterceptor, $q) {
        return {
          request: function (pyynto) {
            pyynto.timeout = asetukset.requestTimeout;
            apiCallInterceptor.apiPyynto(pyynto);
            return pyynto;
          },
          response: function (vastaus) {
            apiCallInterceptor.apiVastaus(vastaus, false);
            return vastaus;
          },
          responseError: function (vastaus) {
            apiCallInterceptor.apiVastaus(vastaus, true);
            return $q.reject(vastaus);
          }
        };
      }
    );
  }])

  .controller('AipalController', ['$scope', '$window', '$location', 'i18n', 'impersonaatioResource', 'kayttooikeudet', function ($scope, $window, $location, i18n, impersonaatioResource, kayttooikeudet) {
    $scope.i18n = i18n;
    $scope.baseUrl = _.has($window, 'ophBaseUrl') ? $window.ophBaseUrl : '';
    $scope.impersonoitava = {};
    $scope.varmistaLogout = function () {
      if (!_.isEmpty($window.aipalLogoutUrl) && $window.confirm(i18n.yleiset.haluatko_kirjautua_ulos)) {
        $window.location = $window.aipalLogoutUrl;
      }
    };
    $scope.valitse = function () {
      $scope.valitseHenkilo = true;
    };
    $scope.piilota = function () {
      $scope.valitseHenkilo = false;
    };
    $scope.impersonoi = function () {
      impersonaatioResource.impersonoi({oid: $scope.impersonoitava.oid}, function () {
        $window.location = $scope.baseUrl;
      });
    };
    $scope.lopetaImpersonointi = function () {
      impersonaatioResource.lopeta(null, function () {
        $window.location = $scope.baseUrl;
      });
    };

    // Set current user and if yllapitaja, impersonoitu
    kayttooikeudet.hae().then(function (data) {
      $scope.kayttooikeudet = data;

      $scope.yllapitaja = kayttooikeudet.isYllapitaja();
      $scope.impersonoitu = kayttooikeudet.isImpersonoitu();
      $scope.currentuser = $scope.kayttooikeudet.etunimi + ' ' + $scope.kayttooikeudet.sukunimi;

      if ($scope.impersonoitu) {
        $scope.currentuser = $scope.kayttooikeudet.impersonoitu_kayttaja;
      }
    });
  }])

  .constant('asetukset', {
    requestTimeout: 120000 //2min timeout kaikille pyynnöille
  })

  .constant('datepickerConfig', {
    dayFormat: 'd',
    monthFormat: 'MMMM',
    yearFormat: 'yyyy',
    dayHeaderFormat: 'EEE',
    dayTitleFormat: 'MMMM yyyy',
    monthTitleFormat: 'yyyy',
    showWeeks: false,
    startingDay: 1,
    yearRange: 20,
    minDate: null,
    maxDate: null
  })

  .config(['cfpLoadingBarProvider', function (cfpLoadingBarProvider) {
    cfpLoadingBarProvider.latencyThreshold = 100;
    cfpLoadingBarProvider.includeSpinner = false;
  }])

  .directive('kielenVaihto', ['kieli', function (kieli) {
    return {
      restrict: 'E',
      templateUrl: 'template/kielen_vaihto.html',
      replace: true,
      link: function (scope) {
        scope.kieli = kieli;
        scope.asetaKieli = function (kieli) {
          localStorage.setItem('kieli', kieli);
          document.location.reload();
        };
      }
    };
  }])

  .factory('impersonaatioResource', ['$resource', function ($resource) {
    return $resource(null, null, {
      impersonoi: {
        method: 'POST',
        url: 'api/kayttaja/impersonoi',
        id: 'impersonoi'
      },
      lopeta: {
        method: 'POST',
        url: 'api/kayttaja/lopeta-impersonointi',
        id: 'impersonoi-lopetus'
      }
    });
  }])

  .factory('$exceptionHandler', ['virheLogitus', function (virheLogitus) {
    return virheLogitus;
  }]);
