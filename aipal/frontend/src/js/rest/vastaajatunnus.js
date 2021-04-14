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

angular.module('rest.vastaajatunnus', ['ngResource'])
  .factory('Vastaajatunnus', ['$http', function($http) {
    return {
      hae: function(kyselykertaid, omat) {
        return $http.get('api/vastaajatunnus/' + kyselykertaid, {params: {nocache: Date.now(), omat: omat}});
      },
      haeNiput: function(kyselykertaid) {
        return $http.get('api/vastaajatunnus/' + kyselykertaid + '/niput', {params: {nocache: Date.now()}});
      },
      luoUusia: function(kyselykertaid, vastaajatunnus) {
        return $http.post('api/vastaajatunnus/' + kyselykertaid, vastaajatunnus);
      },
      lukitse: function(kyselykertaid, vastaajatunnusid, lukitse) {
        return $http.post('api/vastaajatunnus/' + kyselykertaid + '/tunnus/' + vastaajatunnusid + '/lukitse', {lukitse: lukitse});
      },
      haeViimeisinTutkinto: function(kyselykertaid) {
        return $http.get('api/vastaajatunnus/' + kyselykertaid + '/tutkinto', {params: {nocache: Date.now()}});
      },
      muokkaaVastaajienLukumaaraa: function(kyselykertaid, vastaajatunnusid, lukumaara) {
        return $http.post('api/vastaajatunnus/' + kyselykertaid + '/tunnus/' + vastaajatunnusid + '/muokkaa-lukumaaraa', {lukumaara: lukumaara});
      },
      poista: function(kyselykertaid, vastaajatunnusid) {
        return $http.delete('api/vastaajatunnus/' + kyselykertaid + '/tunnus/' + vastaajatunnusid);
      }
    };
  }]);
