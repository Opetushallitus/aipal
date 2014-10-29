'use strict';

angular.module('vastaus.vastausui', ['ngRoute', 'toimiala.vastaus', 'yhteiset.palvelut.ilmoitus', 'yhteiset.palvelut.i18n'])

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

  .factory('VastausControllerFunktiot', ['Vastaus', '$location', 'ilmoitus', 'i18n', function(Vastaus, $location, ilmoitus, i18n) {
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
          else if (kysymysdata.vastaustyyppi === 'kylla_ei_valinta' && kysymysdata.vastaus) {
            if (kysymysdata.jatkokysymysid) {
              if (kysymysdata.vastaus === 'kylla' && kysymysdata.jatkovastaus_kylla) {
                vastaus.jatkokysymysid = kysymysdata.jatkokysymysid;
                vastaus.jatkovastaus_kylla = kysymysdata.jatkovastaus_kylla;
              }
              else if (kysymysdata.jatkovastaus_ei) {
                vastaus.jatkokysymysid = kysymysdata.jatkokysymysid;
                vastaus.jatkovastaus_ei = kysymysdata.jatkovastaus_ei;
              }
            }
            vastaus.vastaus.push(kysymysdata.vastaus);
          }
          else if (kysymysdata.vastaus) {
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
      $scope.tallennaNappiDisabloitu = true;
      Vastaus.tallenna($scope.tunnus, keraaVastausdata($scope.data), function() {
        $location.url('/kiitos');
      }, function(){
        $scope.tallennaNappiDisabloitu = false;
        ilmoitus.virhe(i18n.hae('palvelinvirhe.teksti'));
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
      $scope.tallennaNappiDisabloitu = false;

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
