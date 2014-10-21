// Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
//
// This program is free software:  Licensed under the EUPL, Version 1.1 or - as
// soon as they will be approved by the European Commission - subsequent versions
// of the EUPL (the "Licence");
//
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// European Union Public Licence for more details.

'use strict';

angular.module('rest.kyselykerta', ['ngResource'])
  .factory('Kyselykerta', ['$resource', function($resource) {
    var resource = $resource(null, null, {
      haku: {
        method: 'GET',
        params: {
          nocache: function() {return Date.now();},
        },
        isArray: true,
        url: 'api/kyselykerta/'
      },
      haeYksi: {
        method: 'GET',
        params: {
          nocache: function() {return Date.now();},
        },
        url: 'api/kyselykerta/:id'
      },
      luoUusi: {
        method: 'POST',
        url: 'api/kyselykerta/',
        id: 'tallenna-kyselykerta'
      },
      tallenna: {
        method: 'POST',
        url: 'api/kyselykerta/:id'
      }
    });

    return {
      hae: function(successCallback, errorCallback) {
        return resource.haku({}, successCallback, errorCallback);
      },
      haeYksi: function(id, successCallback, errorCallback) {
        return resource.haeYksi({id: id}, successCallback, errorCallback);
      },
      luoUusi: function(kyselyId, kyselykerta, successCallback, errorCallback) {
        return resource.luoUusi({}, {kyselyid: kyselyId, kyselykerta: kyselykerta}, successCallback, errorCallback);
      },
      tallenna: function(kyselykertaId, kyselykerta, successCallback, errorCallback) {
        return resource.tallenna({id: kyselykertaId}, kyselykerta, successCallback, errorCallback);
      }
    };
  }]);

