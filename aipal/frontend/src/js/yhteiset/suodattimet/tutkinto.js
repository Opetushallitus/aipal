angular.module('yhteiset.suodattimet.tutkinto', [])
  .filter('lokalisoiTutkinto', ['kieli', function(kieli) {

    return function (obj) {

      if (!obj) {
        return '';
      }

      const prioriteettiJarjestys = [kieli, 'fi', 'sv', 'en'];

      let res = '';

      _.forEach(prioriteettiJarjestys, function (k) {
        const kaannos = obj['nimi_' + k];
        if (kaannos){
           res = obj.tutkintotunnus + ' - ' + kaannos;
           return false;
        }
      })
      return res;
    }
  }]);
