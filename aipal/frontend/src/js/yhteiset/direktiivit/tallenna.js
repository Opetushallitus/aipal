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

angular.module('yhteiset.direktiivit.tallenna', ['yhteiset.palvelut.apicallinterceptor'])
  .directive('tallenna', ['apiCallInterceptor', function(apiCallInterceptor){

    function onkoPyyntojaKaynnissa(metodiIdt) {
      return !_(apiCallInterceptor.pyynnot).pick(metodiIdt).every({pyyntojaKaynnissa : 0});
    }

    return {
      restrict: 'E',
      scope: {
        disabloiPyyntojenAjaksi: '@',
        formiValidi : '=',
        teksti : '@'
      },
      template : '<button class="btn btn-primary e2e-direktiivit-tallenna" ng-disabled="tallennusDisabloitu">{{teksti}}&nbsp;&nbsp;<span ng-if="icon" ng-class="icon"></span></button>',
      replace : true,
      link : function(scope, element, attrs) {
        var idt = scope.$eval(scope.disabloiPyyntojenAjaksi);
        var pyyntojaKaynnissa = false;
        var tarkistaOnkoPyyntoja = _.partial(onkoPyyntojaKaynnissa, idt);

        scope.tallennusDisabloitu = false;
        scope.icon = attrs.icon;

        function paivitaTila() {
          scope.tallennusDisabloitu = scope.formiValidi === false || pyyntojaKaynnissa;
        }

        scope.$watch(tarkistaOnkoPyyntoja, function(value){
          pyyntojaKaynnissa = value ? true : false;
          paivitaTila();
        });

        scope.$watch('formiValidi', paivitaTila );
      }
    };
  }]);
