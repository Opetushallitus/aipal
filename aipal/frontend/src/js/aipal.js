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
  'kyselykerta.kyselykertaui',
  'raportti.kyselykerta.kyselykertaui',
  'yhteiset.palvelut.i18n',
  'yhteiset.palvelut.apicallinterceptor',
  'yhteiset.palvelut.lokalisointi',
  'yhteiset.palvelut.tallennusMuistutus',
  'yhteiset.palvelut.virheLogitus',
  'yhteiset.palvelut.seuranta',
  'yhteiset.palvelut.kayttooikeudet',
  'yhteiset.direktiivit.copyright',
  'yhteiset.direktiivit.navigaatio',
  'yhteiset.direktiivit.popup-ikkuna',
  'yhteiset.direktiivit.pvm-valitsin',
  'yhteiset.direktiivit.latausindikaattori',
  'yhteiset.direktiivit.pakollisia-kenttia',
  'yhteiset.direktiivit.tallenna',
  'yhteiset.direktiivit.hakuvalitsin',
  'yhteiset.suodattimet.enumarvo',
  'yhteiset.suodattimet.i18n',
  'ui.bootstrap',
  'ngRoute',
  'ui.select2',
  'ng-breadcrumbs',
  'placeholderShim'
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

  .controller('AipalController', ['$location', '$modal', '$scope', '$window', 'i18n', 'impersonaatioResource', 'kayttooikeudet', 'breadcrumbs', function ($location, $modal, $scope, $window, i18n, impersonaatioResource, kayttooikeudet, breadcrumbs) {
    $scope.i18n = i18n;
    $scope.breadcrumbs = breadcrumbs;
    $scope.baseUrl = _.has($window, 'ophBaseUrl') ? $window.ophBaseUrl : '';
    $scope.vastausBaseUrl = _.has($window, 'vastausBaseUrl') ? $window.vastausBaseUrl : 'http://192.168.50.1:8083';
    $scope.varmistaLogout = function () {
      if (!_.isEmpty($window.aipalLogoutUrl) && $window.confirm(i18n.yleiset.haluatko_kirjautua_ulos)) {
        $window.location = $window.aipalLogoutUrl;
      }
    };
    $scope.valitse = function () {
      var modalInstance = $modal.open({
        templateUrl: 'template/impersonointi.html',
        controller: 'ImpersonointiModalController',
      });
      modalInstance.result.then(function(impersonoitava) {
        impersonaatioResource.impersonoi({oid: impersonoitava.oid}, function () {
          $window.location = $scope.baseUrl;
        });
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

  .controller('ImpersonointiModalController', ['$modalInstance', '$scope', 'i18n', function($modalInstance, $scope, i18n) {
    $scope.i18n = i18n;
    $scope.impersonoitava = {};
    $scope.impersonoi = function() {
      $modalInstance.close($scope.impersonoitava);
    };
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }])

  .constant('asetukset', {
    requestTimeout: 120000 //2min timeout kaikille pyynnöille
  })

  .constant('datepickerConfig', {
    formatDay: 'd',
    formatMonth: 'MMMM',
    formatYear: 'yyyy',
    formatDayHeader: 'EEE',
    formatDayTitle: 'MMMM yyyy',
    formatMonthTitle: 'yyyy',
    datepickerMode: 'day',
    minMode: 'day',
    maxMode: 'year',
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

  .run(['$cookies', function($cookies) {
    $.fn.select2.ajaxDefaults.params.headers = {'x-xsrf-token' : $cookies['XSRF-TOKEN']};
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
