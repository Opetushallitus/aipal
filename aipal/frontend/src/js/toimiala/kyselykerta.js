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

angular.module('toimiala.kyselykerta', ['ngResource'])
  .factory('Kyselykerta', ['$resource', function($resource) {
    var resource = $resource(null, null, {
      haku: {
        method: 'GET',
        isArray: true,
        url: 'api/kyselykerta/:id'
      },
      tallennus: {
        method: 'POST',
        url: 'api/kyselykerta/:id',
        id: 'tallenna-kyselykerta'
      }
    });

    return {
      hae: function(successCallback, errorCallback) {
        return resource.haku({}, successCallback, errorCallback);
      },
      haeYksi: function(id, successCallback, errorCallback) {
        return 42;
      },
      tallenna: function(id, kyselykerta, successCallback, errorCallback) {
        return resource.tallennus({id: id}, {kyselykerta: kyselykerta}, successCallback, errorCallback);
      }
    };
  }]);

