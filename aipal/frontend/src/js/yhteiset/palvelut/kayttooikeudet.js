'use strict';

angular.module('kayttooikeudet', ['ngResource'])
  .factory('kayttooikeudet', ['$resource', function ($resource) {
    var resource = $resource('api/kayttaja', null, {
      get: {
        method: 'GET',
        params: { nocache: function () {
          return Date.now();
        }},
        id: 'henkilon-tiedot'
      }
    });

    var oikeudet,
        yllapitaja,
        impersonoitu;

    function paivitaOikeudet() {
      oikeudet = resource.get().$promise;

      // Is the user yllapitaja,impersonoitu?
      oikeudet.then(function(data){
        yllapitaja = false;
        impersonoitu = false;

        if(_.where(data.roolit, {rooli: 'YLLAPITAJA'}).length > 0){
          yllapitaja = true;
        }

        if(data.impersonoitu_kayttaja.trim().length > 0){
          impersonoitu = true;
        }
      });

    }
    paivitaOikeudet();

    return {
      hae: function () {
        return oikeudet;
      },
      isYllapitaja: function (){
        return yllapitaja;
      },
      isImpersonoitu: function(){
        return impersonoitu;
      },
      paivita: paivitaOikeudet
    };
  }]);