'use strict';

angular.module('vastaus.vastausui', ['ngRoute'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/vastaus/:tunnus', {
        controller: 'VastausController',
        templateUrl: 'template/vastaus/vastaus.html'
      });
  }])

  .controller('VastausController', ['$http', '$routeParams', '$scope', function($http, $routeParams, $scope) {
    $scope.tunnus = $routeParams.tunnus;
    $scope.monivalinta = {};

    $scope.tallenna = function() {
      // TODO: vastausdatan käsittely ja lähetys
    };

    $scope.vaihdaMonivalinta = function(vaihtoehto, kysymysid) {
      if (_.isUndefined($scope.monivalinta[kysymysid])) {
        $scope.monivalinta[kysymysid] = 0;
      }
      if(vaihtoehto.valittu) {
        $scope.monivalinta[kysymysid]++;
      }
      else {
        $scope.monivalinta[kysymysid]--;
      }
    };

    $http.get('/api/kyselykerta/' + $routeParams.tunnus).success(function(data) {
      $scope.data = data;
    });
  }]);
