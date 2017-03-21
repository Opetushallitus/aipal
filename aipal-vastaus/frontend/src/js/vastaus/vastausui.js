'use strict';

function asetaKieli(lang) {
  var old = localStorage.getItem('kieli');
  if(old !== lang){
    localStorage.setItem('kieli', lang);
    document.location.reload(true)
  }
}

angular.module('vastaus.vastausui', ['ngRoute', 'toimiala.vastaus', 'yhteiset.palvelut.ilmoitus', 'yhteiset.palvelut.i18n', 'yhteiset.palvelut.lokalisointi', 'yhteiset.palvelut.tallennusMuistutus'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/vastaus/:tunnus', {
        controller: 'VastausController',
        templateUrl: 'template/vastaus/vastaus.html'
      })
      .when('/vastaus/:tunnus/:lang', {
        redirectTo: function (pathParams, path) {
          if(['fi', 'sv', 'en'].indexOf(pathParams.lang) !== -1){
            asetaKieli(pathParams.lang);
          }
          return '/vastaus/' + pathParams.tunnus;
        }
      })
      .when('/vastausaika-loppunut', {
        templateUrl: 'template/vastaus/lukittu.html'
      })
      .when('/kiitos', {
        controller: 'KiitosController',
        templateUrl: 'template/vastaus/kiitos.html'
      })
      .when('/preview', {
        controller: 'PreviewController',
        templateUrl: 'template/vastaus/vastaus.html'
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
            if(kysymysdata.vastaus === 'EOS') {
              vastaus.vastaus.push('EOS');
            } else {
              for (var vaihtoehto in kysymysdata.monivalintavaihtoehdot) {
                if (kysymysdata.monivalintavaihtoehdot[vaihtoehto].valittu) {
                  vastaus.vastaus.push(kysymysdata.monivalintavaihtoehdot[vaihtoehto].jarjestys);
                }
              }
            }
          }
          else if (kysymysdata.vastaustyyppi === 'kylla_ei_valinta' && kysymysdata.vastaus) {
            if (kysymysdata.jatkokysymysid) {
              if (kysymysdata.vastaus === 'kylla' && kysymysdata.jatkovastaus_kylla) {
                vastaus.jatkokysymysid = kysymysdata.jatkokysymysid;
                vastaus.jatkovastaus_kylla = kysymysdata.jatkovastaus_kylla;
              }
              else if (kysymysdata.vastaus === 'ei' && kysymysdata.jatkovastaus_ei) {
                vastaus.jatkokysymysid = kysymysdata.jatkokysymysid;
                vastaus.jatkovastaus_ei = kysymysdata.jatkovastaus_ei;
              }
            }
            vastaus.vastaus.push(kysymysdata.vastaus);
          }
          else if (kysymysdata.vastaus !== undefined) {
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
        $location.url('/kiitos' + (($scope.data.uudelleenohjaus_url) ? '?redirect=' + encodeURIComponent($scope.data.uudelleenohjaus_url) : ''));
        $scope.vastausForm.$setPristine();
        scrollTo(0,0);
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
    '$filter', '$http', '$rootScope', '$routeParams', '$scope', '$location', 'Vastaus', 'VastausControllerFunktiot', 'tallennusMuistutus', '$anchorScroll', '$timeout', '$window',
    function($filter, $http, $rootScope, $routeParams, $scope, $location, Vastaus, f, tallennusMuistutus, $anchorScroll, $timeout, $window) {
      $scope.preview = false;
      $scope.$watch('vastausForm', function(vastausForm) {
        tallennusMuistutus.muistutaTallennuksestaPoistuttaessaFormilta(vastausForm);
      });

      $scope.findPage = function(kysymysId) {
        for(var i = 0; i < $scope.data.kysymysryhmat.length; i++){
          if(_.find($scope.data.kysymysryhmat[i].kysymykset, {'kysymysid': kysymysId}) != undefined){
            return i;
          }
        }
      };

      $scope.gotoQuestion = function(kysymysid) {

        if($scope.data.sivutettu){
          $scope.sivu = $scope.findPage(kysymysid);
          $scope.vaihdaSivu()
        }

        var old = $location.hash();
        $location.hash('k' + kysymysid);
        $anchorScroll();
        $location.hash(old);

        var required = angular.element(document.getElementsByClassName('kysymysForm ng-invalid-required'));

        _.forEach(required,function(e){
          angular.element(e).parent().addClass('highlight');
        });

        $timeout(function(){
          // Remove all highlights
          _.forEach(required,function(e){
            angular.element(e).parent().removeClass('highlight');
          });
        },7000);

      };

      $scope.tunnus = $routeParams.tunnus;
      $scope.monivalinta = {};
      $scope.tallennaNappiDisabloitu = false;
      $scope.seuraavaNappiDisabloitu = false;
      $scope.edellinenNappiDisabloitu = false;
      $scope.sivu = 0;

      $scope.tallenna = function() {
        f.tallenna($scope);
      };

      $scope.vastausTemplate = $scope.valittuTemplate = "template/vastaus/kysymysryhmat.html";
      $scope.valittuKysymysryhma = null;

      $scope.vaihdaSivu = function() {

        $scope.valittuKysymysryhma = $scope.data.kysymysryhmat[$scope.sivu];
        $scope.edellinenNappiDisabloitu = $scope.sivu === 0;

        $scope.seuraavaNappiDisabloitu = $scope.sivu == ($scope.data.kysymysryhmat.length -1);

        $window.scrollTo(0,0);
      };


      $scope.seuraavaSivu = function() {
        $scope.sivu = Math.min($scope.sivu +1, $scope.data.kysymysryhmat.length -1);
        $scope.vaihdaSivu()
      };

      $scope.edellinenSivu = function() {
        $scope.sivu = Math.max($scope.sivu -1, 0);
        $scope.vaihdaSivu()
      };

      $scope.vaihdaMonivalinta = function(vaihtoehto, kysymys) {
        var kysymysid = kysymys.kysymysid;

        if (_.isUndefined($scope.monivalinta[kysymysid])) {
          $scope.monivalinta[kysymysid] = 0;
        }
        if(vaihtoehto.valittu) {
          $scope.monivalinta[kysymysid]++;
        }
        else {
          $scope.monivalinta[kysymysid]--;
        }

        if(_.isUndefined($scope.monivalinta[kysymysid]) || $scope.monivalinta[kysymysid] === 0){
          kysymys.vastaus = false;
        }else{
          kysymys.vastaus = true;
        }
      };

      $http.get('api/kyselykerta/' + $routeParams.tunnus)
      .success(function(data) {
        $scope.data = data;
        $scope.valittuKysymysryhma = data.kysymysryhmat[0];
        $scope.vaihdaSivu();
        if(data.sivutettu){
          $scope.valittuTemplate = "template/vastaus/kysymysryhmat-sivutettu.html"
        }
        $rootScope.title = $filter('lokalisoiKentta')(data, 'nimi');
      })
      .error(function() {
        $location.path('/vastausaika-loppunut');
      });
    }
  ])
  .controller('PreviewController', [
    '$http', '$routeParams', '$scope', '$location',
    function($http, $routeParams, $scope) {
      $scope.preview = true;
      $scope.messages = [];
      $scope.$on('$messageIncoming', function (event, data){
        $scope.data = angular.fromJson(data.message);
      });
    }
  ])
  .controller('KiitosController', [
    '$location', '$timeout', '$window',
    function($location, $timeout, $window) {
      var redirectUrl = $location.search().redirect;
      if (redirectUrl) {
        $timeout(function() {
          $window.location.href = redirectUrl;
        }, 2000);
      }
    }
  ]);
