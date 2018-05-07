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

angular.module('rest.kysely', ['yhteiset.palvelut.pvm'])
  .factory('Kysely', ['$http', function($http) {
    return {
      hae: function() {
        return $http.get('api/kysely', {params: {nocache: Date.now()}});
      },
      haeId: function(id) {
        return $http.get('api/kysely/' + id, {params: {nocache: Date.now()}});
      },
      haeVastaustunnustiedot: function(id) {
        return $http.get('api/kysely/' + id + '/vastaustunnustiedot', {params: {nocache: Date.now()}});
      },
      luoUusi: function(kysely) {
        return $http.post('api/kysely', kysely);
      },
      tallenna: function(kysely) {
        return $http.post('api/kysely/' + kysely.kyselyid, kysely);
      },
      julkaise: function(id) {
        return $http.put('api/kysely/julkaise/' + id);
      },
      sulje: function(id) {
        return $http.put('api/kysely/sulje/' + id);
      },
      palauta: function(id) {
        return $http.put('api/kysely/palauta/' + id);
      },
      palautaLuonnokseksi: function(id) {
        return $http.put('api/kysely/palauta-luonnokseksi/' + id);
      },
      poista: function(id) {
        return $http.delete('api/kysely/' + id);
      },
      kyselytyypit: function() {
        return $http.get('api/kysely/kyselytyypit')
      }
    };
  }]);

