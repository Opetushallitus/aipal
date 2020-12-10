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
        vastuukayttaja,
        laajennettu;

    function paivitaOikeudet() {
      oikeudet = resource.get().$promise;

      // Is the user yllapitaja,impersonoitu?
      oikeudet.then(function(data){
        yllapitaja = false;
        impersonoitu = false;
        vastuukayttaja = data.aktiivinen_rooli.rooli === 'OPL-VASTUUKAYTTAJA';

        laajennettu = data.laajennettu;

        if(_.where(data.roolit, {rooli: 'YLLAPITAJA'}).length > 0){
          yllapitaja = true;
        }

        if(data.impersonoitu_kayttaja.trim().length > 0 || data.vaihdettu_organisaatio.trim().length > 0){
          impersonoitu = true;
        }
      }).catch(function (e) {
        console.error(e);
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
      isVastuuKayttaja: function(){
        return vastuukayttaja || yllapitaja;
      },
      laajennettuOppisopimuskoulutus: function() {
        return laajennettu;
      },
      paivita: paivitaOikeudet
    };
  }]);
