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

angular.module('yhteiset.direktiivit.pvm-valitsin', ['yhteiset.palvelut.pvm'])

 .directive('pvmValitsin', [function() {
    return {
      restrict: 'E',
      replace: true,
      scope : {
        valittuPvm : '=',
        oletusPvm : '=',
        minPvm : '=',
        maxPvm : '=',
        otsikko : '@',
        pakollinen : '='
      },
      templateUrl : 'template/yhteiset/direktiivit/pvm-valitsin.html',
      link : function(scope) {
        scope.$watch('oletusPvm', function(value){
          if(value && !scope.valittuPvm) {
            scope.valittuPvm = value;
          }
        });

        scope.open = function($event) {
          $event.preventDefault();
          $event.stopPropagation();
          scope.opened = true;
        };
      }
    };
  }])

  .directive('formatteddate', ['$filter', 'pvm', function ($filter, pvm) {
    function parseDate(viewValue) {
      if(typeof viewValue === 'string' && viewValue !== '') {
        var parsittu = pvm.parsiPvm(viewValue);
        if(parsittu) {
          return parsittu;
        } else {
          return 'Invalid date';
        }
      }
      return viewValue;
    }

    return {
      link: function (scope, element, attrs, ctrl) {
        ctrl.$parsers.unshift(parseDate);
      },
      priority: 1, //<-- Formatteddate- direktiivin link funktio suoritetaan datepickerin link funktion jälkeen.
                   //    Näin saadaan custom parsefuktio parseriketjun ensimmäiseksi
      restrict: 'A',
      require: 'ngModel'
    };
  }]);
