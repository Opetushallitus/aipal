'use strict';

angular.module('yhteiset.palvelut.kielet', [])
  .factory('Kielet', ['$http', function($http) {
    return {
      hae: function(kieli) {
        return $http.get('api/i18n/' + kieli, {cache: true}).then(function(result) {
          return result.data;
        });
      }
    };
  }])
;
