// Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
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

angular.module('rest.oppilaitos', [])
  .factory('Oppilaitos', ['$http', function($http) {
    return {
      haeKoulutustoimijanOppilaitokset: function(koulutustoimija) {
        return $http.get('api/oppilaitos', {params: {nocache: Date.now(),
                                                     koulutustoimija: koulutustoimija}});
      },
      haeOppilaitoksenToimipaikat: function(oppilaitos) {
        return $http.get('api/toimipiste', {params: {nocache: Date.now(),
          oppilaitos: oppilaitos}});
      }
    };
  }]);
