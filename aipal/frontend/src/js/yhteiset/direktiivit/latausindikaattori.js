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

angular.module('yhteiset.direktiivit.latausindikaattori', ['yhteiset.palvelut.apicallinterceptor', 'yhteiset.palvelut.i18n'])

  .directive('latausIndikaattori', ['apiCallInterceptor', 'i18n', function(apiCallInterceptor, i18n){

    function tarkastaPyynnonTila(metodiIdt, tarkastusFunktio) {
      var val = _.all( metodiIdt,
        function(id) {
          var pyynto = _.pick(apiCallInterceptor.pyynnot, id);
          return !_.isEmpty(pyynto) ? _.all(_.values(pyynto), tarkastusFunktio) : true;
        });
      return val;
    }

    function paivitaStatus(metodiIdt, scope) {
      var ok = tarkastaPyynnonTila(metodiIdt, function(pyynto) {return pyynto.viimeinenPyyntoOnnistui;});
      var valmis = tarkastaPyynnonTila(metodiIdt, function(pyynto) {return pyynto.pyyntojaKaynnissa === 0;});
      scope.latausKaynnissa = !valmis;
      scope.virhe = !ok;
    }

    function statusPaivitettyViimeksi(metodiIdt) {
      return _(apiCallInterceptor.pyynnot).pick(metodiIdt).map(function(pyynto){return pyynto.paivitetty;}).max().value();
    }

    return {
      scope : {
        viesti: '@',
        metodiIdt : '@',
        virheviesti: '@',
        yritaUudelleen : '&'
      },
      transclude: true,
      templateUrl : 'template/yhteiset/direktiivit/latausindikaattori.html',
      restrict: 'A',
      link: function(scope, element, attrs) {
        var idt = scope.$eval(attrs.metodiIdt);
        paivitaStatus(idt, scope);
        scope.i18n = i18n;

        scope.$watch(function() {
          return statusPaivitettyViimeksi(idt);
        }, function(paivitetty){
          if(paivitetty) {
            paivitaStatus(idt, scope);
          }
        });
      }
    };
  }]);
