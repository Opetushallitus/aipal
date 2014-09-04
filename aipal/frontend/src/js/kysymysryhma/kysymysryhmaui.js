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

angular.module('kysymysryhma.kysymysryhmaui', ['ngRoute', 'toimiala.kysymysryhma'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/kysymysryhmat', {
        controller: 'KysymysryhmatController',
        templateUrl: 'template/kysymysryhma/kysymysryhmat.html'
      })
      .when('/kysymysryhma/uusi', {
        controller: 'UusiKysymysryhmaController',
        templateUrl: 'template/kysymysryhma/uusi.html'
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

  .controller('UusiKysymysryhmaController', ['$scope', '$window', 'Kysymysryhma',
                                             function($scope, $window, Kysymysryhma){
    $scope.kysely = {};
    $scope.peruuta = function(){
      $window.location.hash = '/kysymysryhmat';
    };
    $scope.luoUusi = function(){
      Kysymysryhma.luoUusi($scope.kysely);
      $window.location.hash = '/kysymysryhmat';
    };
  }]);
