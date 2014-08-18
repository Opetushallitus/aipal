'use strict';

angular.module('vastaus.vastausui', ['ngRoute', 'toimiala.vastaus'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/vastaus/:tunnus', {
        controller: 'VastausController',
        templateUrl: 'template/vastaus/vastaus.html'
      });
  }])

  .factory('VastausControllerFunktiot', [function() {
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
                vastaus.vastaus.push(kysymysdata.monivalintavaihtoehdot[vaihtoehto].jarjestys);
              }
            }
          }
          else {
            vastaus.vastaus = kysymysdata.vastaus;
          }
          if (!_.isUndefined(vastaus.vastaus) && (_.isNumber(vastaus.vastaus) || !_.isEmpty(vastaus.vastaus))) {
            vastaukset.push(vastaus);
          }
        }
      }
      return {vastaukset: vastaukset};
    }

    return {
      keraaVastausdata: keraaVastausdata
    };
  }])

  .controller('VastausController', [
    '$http', '$routeParams', '$scope', '$location', 'Vastaus', 'VastausControllerFunktiot',
    function($http, $routeParams, $scope, $location, Vastaus, f) {

      $scope.tunnus = $routeParams.tunnus;
      $scope.monivalinta = {};

      $scope.tallenna = function() {
        Vastaus.tallenna($scope.tunnus, f.keraaVastausdata($scope.data), function() {
          // TODO: siirtyminen "kiitos vastauksesta" -sivulle
          $location.url('/');
        });
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
    }
  ]);
