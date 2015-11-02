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

angular.module('aipalvastaus', [
    'lomake.lomakeui',
    'vastaus.vastausui',
    'yhteiset.direktiivit.copyright',
    'yhteiset.palvelut.i18n',
    'yhteiset.palvelut.lokalisointi',
    'ngRoute',
    'ngPostMessage'
  ])

  .controller('AipalvastausController', ['$scope', '$window', 'i18n', 'kieli', function($scope, $window, i18n, kieli){
    $scope.i18n = i18n;
    $scope.baseUrl = _.has($window, 'hakuBaseUrl') ?  $window.hakuBaseUrl : '';

    $scope.vastauksetToisellaKielella = function(kysymys) {
      if (kieli === 'sv') {
        return kysymys.kysymys_sv === '';
      }
      else {
        return kysymys.kysymys_fi === '';
      }
    };
  }])

  .constant('asetukset', {
    requestTimeout : 120000 //2min timeout kaikille pyynnöille
  })

  .directive('kielenVaihto', ['kieli', function(kieli){
    return {
      restrict: 'E',
      templateUrl : 'template/kielen_vaihto.html',
      replace: true,
      link: function(scope) {
        scope.vaihtoehdot = [{tunnus: 'fi', teksti: 'suomeksi'}, {tunnus: 'sv', teksti: 'ruotsiksi'}];
        scope.kieli = kieli;
        scope.asetaKieli = function(kieli) {
          localStorage.setItem('kieli', kieli);
          document.location.reload();
        };
      }
    };
  }]);
