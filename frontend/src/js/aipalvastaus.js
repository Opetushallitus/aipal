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
    'yhteiset.palvelut.i18n',
    'yhteiset.direktiivit.copyright',
    'vastaus.vastausui',
    'ngRoute'
  ])

  .controller('AipalvastausController', ['$scope', '$window', 'i18n', function($scope, $window, i18n){
    $scope.i18n = i18n;
    $scope.baseUrl = _.has($window, 'hakuBaseUrl') ?  $window.hakuBaseUrl : '';
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
        scope.kieli = kieli;
      }
    };
  }])

  .directive('piilotaTekstienLatauksenAjaksi', ['i18n', function(i18n) {
    return {
      restrict: 'A',
      link : function(scope, el) {
        var element = $(el);
        element.hide();
        i18n.$promise.then(function(){
          element.show();
        });
      }
    };
  }]);
