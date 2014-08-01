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
    'raportti.kyselykerta.kyselykertaui',
    'yhteiset.palvelut.i18n',
    'yhteiset.palvelut.apicallinterceptor',
    'yhteiset.palvelut.virheLogitus',
    'yhteiset.direktiivit.copyright',
    'yhteiset.direktiivit.navigaatio',
    'yhteiset.direktiivit.popup-ikkuna',
    'yhteiset.direktiivit.input',
    'yhteiset.direktiivit.pvm-valitsin',
    'yhteiset.direktiivit.latausindikaattori',
    'yhteiset.direktiivit.pakollisia-kenttia',
    'yhteiset.direktiivit.tallenna',
    'ui.bootstrap',
    'ngRoute'
  ])

  .config(['$httpProvider', 'asetukset', function($httpProvider, asetukset) {
    $httpProvider.interceptors.push(
      function(apiCallInterceptor, $q){
        return {
          request : function(pyynto){
            pyynto.timeout = asetukset.requestTimeout;
            apiCallInterceptor.apiPyynto(pyynto);
            return pyynto;
          },
          response : function(vastaus){
            apiCallInterceptor.apiVastaus(vastaus, false);
            return vastaus;
          },
          responseError : function(vastaus){
            apiCallInterceptor.apiVastaus(vastaus, true);
            return $q.reject(vastaus);
          }
        };
      }
    );
  }])

  .controller('AipalController', ['$scope', '$window', 'i18n', function($scope, $window, i18n){
    $scope.i18n = i18n;
    $scope.baseUrl = _.has($window, 'hakuBaseUrl') ?  $window.hakuBaseUrl : '';
    $scope.varmistaLogout = function() {
      if(!_.isEmpty($window.aipalLogoutUrl) && $window.confirm(i18n.yleiset.haluatko_kirjautua_ulos)) {
        $window.location = $window.aipalLogoutUrl;
      }
    };
  }])

  .constant('asetukset', {
    requestTimeout : 120000 //2min timeout kaikille pyynnöille
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

  .config(['cfpLoadingBarProvider', function(cfpLoadingBarProvider) {
    cfpLoadingBarProvider.latencyThreshold = 100;
    cfpLoadingBarProvider.includeSpinner = false;
  }])

  .directive('kielenVaihto', ['kieli', function(kieli){
    return {
      restrict: 'E',
      templateUrl : 'template/kielen_vaihto.html',
      replace: true,
      link: function(scope) {
        scope.kieli = kieli;
        scope.asetaKieli = function(kieli) {
          localStorage.setItem('kieli', kieli);
          document.location.reload();
        };
      }
    };
  }])

  .factory('$exceptionHandler', ['virheLogitus', function(virheLogitus) {
    return virheLogitus;
  }]);
