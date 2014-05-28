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

angular.module('yhteiset.suodattimet.voimassaoloaika', [])
  .filter('voimassaoloAika', ['i18n', '$filter', function(i18n, $filter) {

    var dateFormat = 'dd.MM.yyyy';

    return function(alkupvm, loppupvm) {
      if(alkupvm && loppupvm) {
        return $filter('date')(alkupvm, dateFormat) + ' - ' + $filter('date')(loppupvm, dateFormat);
      } else if (alkupvm && !loppupvm) {
        return $filter('date')(alkupvm, dateFormat) + ' ' + i18n.yleiset.alkaen;
      } else if (loppupvm && !alkupvm) {
        return $filter('date')(loppupvm, dateFormat) + ' ' + i18n.yleiset.asti;
      } else {
        return '-';
      }
    };
  }]);
