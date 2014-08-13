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

    function keraaVastausdata(data) {
      var vastaukset = [];

      for (var ryhma in data.kysymysryhmat) {
        for (var kysymys in data.kysymysryhmat[ryhma].kysymykset) {
          var kysymysdata = data.kysymysryhmat[ryhma].kysymykset[kysymys];
          var vastaus = {
            kysymysid: kysymysdata.kysymysid
          };
          if (kysymysdata.vastaustyyppi === 'monivalinta' && kysymysdata.monivalinta_max > 1) {
            vastaus.vastaus = [];
            for (var vaihtoehto in kysymysdata.monivalintavaihtoehdot) {
              if (kysymysdata.monivalintavaihtoehdot[vaihtoehto].valittu) {
                vastaus.vastaus.push(kysymysdata.monivalintavaihtoehdot[vaihtoehto].monivalintavaihtoehtoid);
              }
            }
          }
          else {
            vastaus.vastaus = kysymysdata.vastaus;
          }
          if (!_.isUndefined(vastaus.vastaus) && !_.isEmpty(vastaus.vastaus)) {
            vastaukset.push(vastaus);
          }
        }
      }
    }

    $scope.tallenna = function() {
      keraaVastausdata($scope.data);
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
