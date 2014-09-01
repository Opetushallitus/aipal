angular.module('kayttooikeudet', ['ngResource'])
  .factory('kayttooikeudet', ['$resource', function ($resource) {
    var resource = $resource('api/kayttaja', null, {
      get: {
        method: 'GET',
        params: { nocache: function () {
          return Date.now();
        }},
        id: "henkilon-tiedot"
      }
    });

    var oikeudet,
        yllapitaja;

    function paivitaOikeudet() {
      oikeudet = resource.get().$promise;

      // Is the user yllapitaja?
      oikeudet.then(function(data){
        yllapitaja = false;
        if(_.where(data.roolit, {rooli: "YLLAPITAJA"}).length > 0){
          yllapitaja = true;
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
      paivita: paivitaOikeudet
    }
  }])

  .run(['kayttooikeudet', function (kayttooikeudet) {
  }])