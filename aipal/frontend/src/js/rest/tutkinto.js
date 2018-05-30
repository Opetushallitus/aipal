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

angular.module('rest.tutkinto', [])
  .factory('Tutkinto', ['$http', function($http) {
    return {
      haeVoimassaolevatTutkinnotListassa: function() {
        return $http.get('api/tutkinto/voimassaolevat-listana', {params: {nocache: Date.now()}});
      },
      haeVoimassaolevatTutkinnotHierarkiassa: function() {
        return $http.get('api/tutkinto/voimassaolevat', {params: {nocache: Date.now()}});
      },
      haeVanhentuneetTutkinnotHierarkiassa: function() {
        return $http.get('api/tutkinto/vanhentuneet', {params: {nocache: Date.now()}});
      },
      koulutustoimijanTutkinnot: function() {
        return $http.get('api/tutkinto/koulutustoimija', {params: {nocache: Date.now()}});
      },
      haeTutkinnonJarjestajat: function(tutkintotunnus) {
        return $http.get('api/tutkinto/jarjestajat/'+tutkintotunnus, {params: {nocache: Date.now()}})
      },
      haeKoulutustoimijanTutkinnot: function(ytunnus) {
        return $http.get('api/tutkinto/koulutustoimija/'+ytunnus, {params: {nocache: Date.now()}});
      }
    };
  }]);
