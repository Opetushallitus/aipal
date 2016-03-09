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

angular.module('rest.kyselykerta', ['yhteiset.palvelut.pvm'])
  .factory('Kyselykerta', ['$http', function($http) {
    return {
      hae: function() {
        return $http.get('api/kyselykerta', {params: {nocache: Date.now()}});
      },
      haeYksi: function(id) {
        return $http.get('api/kyselykerta/' + id, {params: {nocache: Date.now()}});
      },
      haeVastaustunnustiedot: function(id) {
        return $http.get('api/kyselykerta/' + id + '/vastaustunnustiedot', {params: {nocache: Date.now()}});
      },
      luoUusi: function(kyselyId, kyselykerta) {
        return $http.post('api/kyselykerta', {kyselyid: kyselyId, kyselykerta: kyselykerta});
      },
      tallenna: function(kyselykertaId, kyselykerta) {
        return $http.post('api/kyselykerta/' + kyselykertaId, kyselykerta);
      },
      lukitse: function(kyselykertaId, lukitse) {
        return $http.put('api/kyselykerta/' + kyselykertaId + '/lukitse', {lukitse: lukitse});
      },
      poista: function(kyselykertaId) {
        return $http.delete('api/kyselykerta/' + kyselykertaId);
      }
    };
  }]);

