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
    ;
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

    return {
      keraaVastausdata: keraaVastausdata
    };
  }])

  .controller('VastausController', [
    '$http', '$routeParams', '$scope', '$location', 'Vastaus', 'VastausControllerFunktiot',
    function($http, $routeParams, $scope, $location, Vastaus, f) {

      $scope.tunnus = $routeParams.tunnus;
      $scope.monivalinta = {};

      // Jos käyttäjä vaihtaa vastaajatunnusta vastaajaid:n tallentamisen jälkeen (kahden eri kyselyn avaus peräkkäin)
      // niin vastaajaid pitää luoda uudestaan
      if (sessionStorage.getItem('vastaajaid') === null || sessionStorage.getItem('tunnus') !== $routeParams.tunnus) {
        Vastaus.luoVastaaja($scope.tunnus, function(data) {
          sessionStorage.setItem('vastaajaid', data.vastaajaid);
          sessionStorage.setItem('tunnus', $routeParams.tunnus);
          $scope.vastaajaid = data.vastaajaid;
        }, function(error) {
          if (error.status === 403) {
            $location.path('/vastausaika-loppunut');
          }
        });
      } else {
        $scope.vastaajaid = parseInt(sessionStorage.getItem('vastaajaid'), 10);
      }

      $scope.tallenna = function() {
        Vastaus.tallenna($scope.tunnus, $scope.vastaajaid, f.keraaVastausdata($scope.data), function() {
          // TODO: siirtyminen "kiitos vastauksesta" -sivulle
          sessionStorage.removeItem('tunnus');
          sessionStorage.removeItem('vastaajaid');
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

      $http.get('api/kyselykerta/' + $routeParams.tunnus).success(function(data) {
        $scope.data = data;
      });
    }
  ]);
