'use strict';

angular.module('vastaus.vastausui', ['ngRoute'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/vastaus/:kyselyid', {
        controller: 'VastausController',
        templateUrl: 'template/vastaus/vastaus.html'
      });
  }])

  .controller('VastausController', ['$http', '$routeParams', '$scope', function($http, $routeParams, $scope) {
    $scope.kyselyid = $routeParams.kyselyid;
    $scope.answers = {};

    $http.get('/api/kyselykerta/' + $routeParams.kyselyid).success(function(data) {
      $scope.data = data;
    });
  }]);
