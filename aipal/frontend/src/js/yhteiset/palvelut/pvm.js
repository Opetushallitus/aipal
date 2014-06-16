// Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
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

angular.module('yhteiset.palvelut.pvm', ['ngResource'])

  .factory('pvm',['$filter', function($filter) {
    return {
      parsiPvm : function(pvm) {
        if(pvm) {
          try {
            var parts = pvm.split('.');
            var parsittu = new Date(parts[2], parts[1] - 1, parts[0]);
            if(parsittu.getDate().toString() === parts[0] && parsittu.getMonth() === parts[1] -1 && parsittu.getFullYear().toString() === parts[2]) {
              return parsittu;
            }
          } catch(e) {}
        }
        return null;
      },
      dateToPvm : function(date) {
        return $filter('date')(date, 'dd.MM.yyyy');
      }
    };
  }]);