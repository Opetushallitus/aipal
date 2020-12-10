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
  'raportti.raporttiui',
  'raportti.kyselyui',
  'yhteiset.palvelut.i18n',
  'yhteiset.palvelut.apicallinterceptor',
  'yhteiset.palvelut.lokalisointi',
  'yhteiset.palvelut.tallennusMuistutus',
  'yhteiset.palvelut.virheLogitus',
  'yhteiset.palvelut.seuranta',
  'yhteiset.palvelut.kayttooikeudet',
  'yhteiset.palvelut.palvelinvirhe',
  'yhteiset.palvelut.varmistus',
  'yhteiset.direktiivit.auth',
  'yhteiset.direktiivit.tiedote',
  'yhteiset.direktiivit.copyright',
  'yhteiset.direktiivit.navigaatio',
  'yhteiset.direktiivit.popup-ikkuna',
  'yhteiset.direktiivit.pvm-valitsin',
  'yhteiset.direktiivit.kyselypohjalista',
  'yhteiset.direktiivit.kysymys-tekstit',
  'yhteiset.direktiivit.kysymysryhma-accordion',
  'yhteiset.direktiivit.kysymysryhmalista',
  'yhteiset.direktiivit.latausindikaattori',
  'yhteiset.direktiivit.lokalisoitufilter',
  'yhteiset.direktiivit.ohjeet',
  'yhteiset.direktiivit.pakollisia-kenttia',
  'yhteiset.direktiivit.tallenna',
  'yhteiset.direktiivit.hakuvalitsin',
  'yhteiset.direktiivit.kyselylista',
  'yhteiset.direktiivit.rullaus',
  'yhteiset.direktiivit.jakauma',
  'yhteiset.suodattimet.enumarvo',
  'yhteiset.suodattimet.i18n',
  'ui.bootstrap',
  'ngCookies',
  'ngRoute',
  'ui.select2',
  'ng-breadcrumbs',
  'taiPlaceholder',
  'ngPostMessage',
  'ui.select',
  'ngSanitize',
  'ui.sortable',
  'tableSort'
])

  .config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push(
      function (apiCallInterceptor, $q) {
        return {
          request: function (pyynto) {
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
    $httpProvider.interceptors.push(
      function($q, $location) {
        return {
          responseError: function (vastaus) {
            if (vastaus.status === 403 && !vastaus.headers('X-Kayttooikeudet-Forbidden') && !vastaus.headers('X-Aipal-Error')) {
              $location.url('/istunto-vanhentunut');
            }
            return $q.reject(vastaus);
          }
        };
      }
    );
    $httpProvider.defaults.headers.common['angular-ajax-request'] = true;
  }])

  .config(['$locationProvider', function ($locationProvider) {
    $locationProvider.hashPrefix('');
  }])

  .controller('AipalController', ['$location', '$uibModal', '$scope', '$window', 'i18n', 'impersonaatioResource', 'rooliResource', 'kayttooikeudet', 'breadcrumbs', '$filter',
              function ($location, $uibModal, $scope, $window, i18n, impersonaatioResource, rooliResource, kayttooikeudet, breadcrumbs, $filter) {
    $scope.aipalOminaisuus = _.has($window, 'aipalOminaisuus') ? $window.aipalOminaisuus : {};
    $scope.i18n = i18n;
    $scope.breadcrumbs = breadcrumbs;
    $scope.baseUrl = _.has($window, 'ophBaseUrl') ? $window.ophBaseUrl : '';
    $scope.vastausBaseUrl = _.has($window, 'vastausBaseUrl') ? $window.vastausBaseUrl : 'http://192.168.50.1:8083';
    $scope.valikkoAvattu = false;
    $scope.varmistaLogout = function () {
      if (!_.isEmpty($window.aipalLogoutUrl) && $window.confirm(i18n.yleiset.haluatko_kirjautua_ulos)) {
        $window.location = $window.aipalLogoutUrl;
      }
    };
    $scope.timestamp = Date.now();

    $scope.valitse = function () {
      var modalInstance = $uibModal.open({
        templateUrl: 'template/impersonointi.html',
        controller: 'ImpersonointiModalController'
      });
      modalInstance.result.then(function(impersonoitava) {
        impersonaatioResource.impersonoi({oid: impersonoitava.oid}, function () {
          $window.location = $scope.baseUrl + '/';
        });
      }).catch(function (e) {
        console.error(e);
      });
    };

    $scope.valitseOrganisaatio = function () {
      var modalInstance = $uibModal.open({
        templateUrl: 'template/organisaation_vaihto.html',
        controller: 'ImpersonointiModalController'
      });
      modalInstance.result.then(function(impersonoitava) {
        impersonaatioResource.vaihdaOrganisaatio({oid: impersonoitava.ytunnus}, function () {
          $window.location = $scope.baseUrl + '/';
        });
      }).catch(function (e) {
        console.error(e);
      });
    };

    $scope.lopetaImpersonointi = function () {
      impersonaatioResource.lopeta(null, function () {
        $window.location = $scope.baseUrl + '/';
      });
    };
    $scope.vaihdaRoolia = function () {
      var modalInstance = $uibModal.open({
        templateUrl: 'template/roolit.html',
        controller: 'RoolitModalController',
        resolve: {
          roolit: function() { return $scope.kayttooikeudet.roolit; },
          aktiivinenRooli: function() { return $scope.kayttooikeudet.aktiivinen_rooli.rooli_organisaatio_id; }
        }
      });
      modalInstance.result.then(function(rooli_organisaatio_id) {
        rooliResource.valitse({rooli_organisaatio_id: rooli_organisaatio_id}, function () {
          $window.location = $scope.baseUrl + '/';
        });
      }).catch(function (e) {
        console.error(e);
      });
    };

    // NOTE: .hae() is not http call an returns prefetched data so no resp.data.
    kayttooikeudet.hae().then(function (data) {
      $scope.kayttooikeudet = data;

      $scope.yllapitaja = kayttooikeudet.isYllapitaja();
      $scope.vastuuKayttaja = kayttooikeudet.isVastuuKayttaja();
      $scope.impersonoitu = kayttooikeudet.isImpersonoitu();

      $scope.currentuser = $scope.kayttooikeudet.impersonoitu_kayttaja ||
                           $scope.kayttooikeudet.etunimi + ' ' + $scope.kayttooikeudet.sukunimi;
      var koulutustoimija = $filter('lokalisoiKentta')($scope.kayttooikeudet.aktiivinen_rooli, 'koulutustoimija');
      if ($scope.kayttooikeudet.roolit.length === 1) {
        $scope.rooli_koulutustoimija = koulutustoimija;
      } else {
        var rooli = $scope.kayttooikeudet.aktiivinen_rooli.rooli;
        i18n.$promise.then(function(){
          $scope.rooli_koulutustoimija = i18n.hae('roolit.rooli.' + rooli) + ' / ' + koulutustoimija;
        }).catch(function (e) {
          console.error(e);
        });
      }
    }).catch(function (e) {
      console.error(e);
    });

    $scope.unohdaAvoimetKyselyt = function() {
      sessionStorage.removeItem('avoimetKyselyt');
    };

    /* UI Sorting defaults */
    $scope.sortableOptions = {
      forceHelperSize: true,
      forcePlaceholderSize: true,
      helper: function(e, tr){
        var $originals = tr.children();
        var $helper = tr.clone();
        $helper.children().each(function(index){
          $(this).width($originals.eq(index).width());
          $originals.eq(index).width($(this).width());
        });
        return $helper;
      }
    };

  }])

  .controller('ImpersonointiModalController', ['$uibModalInstance', '$scope', 'i18n', function($uibModalInstance, $scope, i18n) {
    $scope.i18n = i18n;
    $scope.impersonointi = {
      impersonoitava: {},
      organisaatio: {}
    };
    $scope.impersonoi = function() {
      $uibModalInstance.close($scope.impersonointi.impersonoitava);
    };
    $scope.vaihdaOrganisaatio = function() {
      console.log("IMPERSONOINTI " + JSON.stringify($scope.impersonointi))
      $uibModalInstance.close($scope.impersonointi.organisaatio);
    };
    $scope.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };
  }])

  .controller('RoolitModalController', ['$uibModalInstance', '$scope', 'i18n', 'roolit', 'aktiivinenRooli', function($uibModalInstance, $scope, i18n, roolit, aktiivinenRooli) {
    $scope.i18n = i18n;
    $scope.roolit = roolit;
    $scope.rooli = aktiivinenRooli;
    $scope.valitseRooli = function() {
      $uibModalInstance.close($scope.rooli);
    };
    $scope.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };
  }])

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
    $.fn.select2.ajaxDefaults.params.headers = {'x-xsrf-token' : $cookies.get('XSRF-TOKEN')};
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
      vaihdaOrganisaatio: {
        method: 'POST',
        url: 'api/kayttaja/vaihda-organisaatio',
        id: 'vaihda-organisaatio'
      },
      lopeta: {
        method: 'POST',
        url: 'api/kayttaja/lopeta-impersonointi',
        id: 'impersonoi-lopetus'
      }
    });
  }])

  .factory('rooliResource', ['$resource', function ($resource) {
    return $resource(null, null, {
      valitse: {
        method: 'POST',
        url: 'api/kayttaja/rooli',
        id: 'valitse-rooli'
      }
    });
  }])

  .factory('$exceptionHandler', ['virheLogitus', function (virheLogitus) {
    return virheLogitus;
  }]);
