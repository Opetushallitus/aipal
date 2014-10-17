'use strict';

angular.module('vastaus.vastausui', ['ngRoute', 'toimiala.vastaus'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/vastaus/:tunnus', {
        controller: 'VastausController',
        templateUrl: 'template/vastaus/vastaus.html'
      })
      .when('/vastausaika-loppunut', {
        templateUrl: 'template/vastaus/lukittu.html'
      })
      .when('/kiitos', {
        templateUrl: 'template/vastaus/kiitos.html'
      })
    ;
  }])

  .factory('VastausControllerFunktiot', ['Vastaus', '$location', function(Vastaus, $location) {
    function keraaVastausdata(data) {
      var vastaukset = [];

      for (var ryhma in data.kysymysryhmat) {
        for (var kysymys in data.kysymysryhmat[ryhma].kysymykset) {
          var kysymysdata = data.kysymysryhmat[ryhma].kysymykset[kysymys];
          var vastaus = {
            kysymysid: kysymysdata.kysymysid
          };
          vastaus.vastaus = [];
          if (kysymysdata.vastaustyyppi === 'monivalinta' && kysymysdata.monivalinta_max > 1) {
            for (var vaihtoehto in kysymysdata.monivalintavaihtoehdot) {
              if (kysymysdata.monivalintavaihtoehdot[vaihtoehto].valittu) {
                vastaus.vastaus.push(kysymysdata.monivalintavaihtoehdot[vaihtoehto].jarjestys);
              }
            }
          }
          else if (!_.isUndefined(kysymysdata.vastaus)) {
            vastaus.vastaus.push(kysymysdata.vastaus);
          }
          if (!_.isEmpty(vastaus.vastaus)) {
            vastaukset.push(vastaus);
          }
        }
      }
      return {vastaukset: vastaukset};
    }

    function tallenna($scope) {
      Vastaus.tallenna($scope.tunnus, keraaVastausdata($scope.data), function() {
        $location.url('/kiitos');
      });
    }

    return {
      keraaVastausdata: keraaVastausdata,
      tallenna: tallenna
    };
  }])

  .controller('VastausController', [
    '$http', '$routeParams', '$scope', '$location', 'Vastaus', 'VastausControllerFunktiot',
    function($http, $routeParams, $scope, $location, Vastaus, f) {

      $scope.tunnus = $routeParams.tunnus;
      $scope.monivalinta = {};

      $scope.tallenna = function() {
        f.tallenna($scope);
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

      $http.get('api/kyselykerta/' + $routeParams.tunnus)
      .success(function(data) {
        $scope.data = data;
      })
      .error(function() {
        $location.path('/vastausaika-loppunut');
      });
    }
  ]);
