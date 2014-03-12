'use strict';

angular.module('yhteiset.palvelut.i18n', ['ngResource'])

  .factory('kieli', ['$location', function($location) {
    var url = $location.absUrl();
    var kieli = url.match(/\/sv\//) ? 'sv' : 'fi';
    return kieli;
  }])

  .factory('i18n', ['$resource', 'kieli', function($resource, kieli) {

    var i18nResource = $resource('api/i18n/:kieli');

    return i18nResource.get({kieli : kieli});
  }]);
