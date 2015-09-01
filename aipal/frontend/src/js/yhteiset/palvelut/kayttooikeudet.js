'use strict';

angular.module('yhteiset.palvelut.kayttooikeudet', ['ngResource'])
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
        impersonoitu,
        ntmVastuuKayttaja;

    function paivitaOikeudet() {
      oikeudet = resource.get().$promise;

      // Is the user yllapitaja,impersonoitu?
      oikeudet.then(function(data){
        yllapitaja = false;
        impersonoitu = false;
        ntmVastuuKayttaja = false;

        if(_.where(data.roolit, {rooli: 'YLLAPITAJA'}).length > 0){
          yllapitaja = true;
        }

        if(data.aktiivinen_rooli.rooli === 'OPL-NTMVASTUUKAYTTAJA'){
          ntmVastuuKayttaja = true;
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
      isNtmVastuuKayttaja: function() {
        return ntmVastuuKayttaja;
      },
      paivita: paivitaOikeudet
    };
  }]);
