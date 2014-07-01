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
    $scope.answers = {};

    $http.get('/api/kyselykerta/' + $routeParams.tunnus).success(function(data) {
      $scope.data = data;
    });
  }]);
