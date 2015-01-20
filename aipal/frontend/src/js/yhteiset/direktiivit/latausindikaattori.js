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

angular.module('yhteiset.direktiivit.latausindikaattori', ['yhteiset.palvelut.seuranta', 'yhteiset.palvelut.i18n'])

  .directive('latausIndikaattori', ['seuranta', 'i18n', function(seuranta, i18n){

    return {
      scope : {
        viesti: '@',
        metodiId : '@',
        virheviesti: '@',
        yritaUudelleen : '&'
      },
      transclude: true,
      templateUrl : 'template/yhteiset/direktiivit/latausindikaattori.html',
      restrict: 'A',
      link: function(scope) {
        var id = scope.metodiId;
        scope.i18n = i18n;

        scope.$watch(function() {
          return seuranta.haeTila(id).paivitetty;
        }, function(){
          var tila = seuranta.haeTila(id);
          scope.latausKaynnissa = !tila.valmis;
          if (tila.valmis && !tila.ok) {
            seuranta.kuittaaVirhe(id);
            scope.virhe = true;
          }
        });
      }
    };
  }]);
