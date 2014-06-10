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

angular.module('kysely.kyselyui', ['toimiala.kysely', 'ngRoute'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/kyselyt', {
        controller: 'KyselytController',
        templateUrl: 'template/kysely/kyselyt.html'
      })
      .when('/kysely/:kyselyid', {
        controller: 'KyselyController',
        templateUrl: 'template/kysely/kysely.html'
      });
  }])

  .controller('KyselytController', [
    'Kysely', '$scope',
    function(Kysely, $scope) {
      $scope.piilotaLuonti = true;
      $scope.kyselyt = Kysely.hae();

      $scope.uusiKyselykerta = function(kyselyid) {
        $scope.valittuKyselyid = kyselyid;
        $scope.piilotaLuonti = false;
      };
    }
  ])

  .controller('KyselyController', [
    'Kysely', '$routeParams', '$scope',
    function(Kysely, $routeParams, $scope) {
      $scope.kysely = Kysely.haeId($routeParams.kyselyid);
    }
  ])

  .directive('kyselykertaLuonti', ['Kysely', function(Kysely) {
    return {
      restrict: 'E',
      scope: {
        kyselyid : '='
      },
      templateUrl: 'template/kysely/kyselykerta-luonti.html',
      controller: function($scope) {
        $scope.kysely = {};
        $scope.kyselykerta = {};

        $scope.$watch('kyselyid', function(kyselyid) {
          if(_.isNumber(kyselyid)) {
            Kysely.haeId(kyselyid, function(kysely) {
              _.assign($scope.kysely, kysely);
            });
          }
        });
        $scope.luo = function() {
          console.log('kyselykerta luotu');
        };
      }
    };
  }]);
