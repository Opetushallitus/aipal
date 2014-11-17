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

angular.module('kyselypohja.kyselypohjaui', ['ngRoute'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/kyselypohjat', {
        controller: 'KyselypohjatController',
        templateUrl: 'template/kyselypohja/kyselypohjat.html',
        label: 'i18n.kyselypohja.breadcrumb_kyselypohja'
      })
      .when('/kyselypohjat/kyselypohja/uusi', {
        controller: 'KyselypohjaController',
        templateUrl: 'template/kyselypohja/kyselypohja.html',
        label: 'i18n.kyselypohja.breadcrumb_kyselypohja'
      })
      .when('/kyselypohjat/kyselypohja/:kyselypohjaid', {
        controller: 'KyselypohjaController',
        templateUrl: 'template/kyselypohja/kyselypohja.html',
        label: 'i18n.kyselypohja.breadcrumb_kyselypohja'
      })
    ;
  }])

  .controller('KyselypohjatController', ['$location', '$scope', 'Kyselypohja', function($location, $scope, Kyselypohja) {
    $scope.luoUusiKyselypohja = function() {
      $location.url('/kyselypohjat/kyselypohja/uusi');
    };

    Kyselypohja.haeKaikki().success(function(kyselypohjat) {
      $scope.kyselypohjat = kyselypohjat;
    });
  }])

  .controller('KyselypohjaController', ['$routeParams', '$scope', 'Kyselypohja', function($routeParams, $scope, Kyselypohja) {
    if ($routeParams.kyselypohjaid) {
      Kyselypohja.hae($routeParams.kyselypohjaid).success(function(kyselypohja) {
        $scope.kyselypohja = kyselypohja;
      });
    }
  }])
;
