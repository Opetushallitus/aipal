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
    var parsePvm = function(data) {
      if(data.voimassa_alkupvm) {
        data.voimassa_alkupvm = new Date(data.voimassa_alkupvm);
      }
      if(data.voimassa_loppupvm) {
        data.voimassa_loppupvm = new Date(data.voimassa_loppupvm);
      }
      return data;
    };
    var dateToPvm = function(date) {
      return $filter('date')(date, 'yyyy-MM-dd');
    };
    var formatPvm = function(d) {
      var data = _.cloneDeep(d);
      if(data.voimassa_alkupvm) {
        data.voimassa_alkupvm = dateToPvm(data.voimassa_alkupvm);
      }
      if(data.voimassa_loppupvm) {
        data.voimassa_loppupvm = dateToPvm(data.voimassa_loppupvm);
      }
      return data;
    };
    return {
      parsePvm : parsePvm,
      formatPvm: formatPvm,
      dateToPvm : dateToPvm
    };
  }]);