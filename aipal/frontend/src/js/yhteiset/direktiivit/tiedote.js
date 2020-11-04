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

angular.module('yhteiset.direktiivit.tiedote',
  ['rest.tiedote',
   'yhteiset.palvelut.i18n'])
  .directive('tiedote', ['Tiedote', 'kieli', 'ilmoitus', 'i18n', function(Tiedote, kieli, ilmoitus, i18n){
    return {
      restrict: 'A',
      templateUrl: 'template/yhteiset/direktiivit/tiedote.html',
      link: function($scope){
        $scope.tila = 'nayta';
        $scope.muokkaa = function(){
          $scope.tila = 'muokkaa';
        };
        $scope.tallenna = function(){
          var tiedote = {fi: $scope.tiedoteFi,
                         sv: $scope.tiedoteSv,
                         en: $scope.tiedoteEn};
          Tiedote.tallenna(tiedote).then(function(){
            ilmoitus.onnistuminen(i18n.hae('tiedote.tallennus_onnistui'));
            $scope.naytettavaTiedote = tiedote[kieli];
          }).catch(function(){
            ilmoitus.virhe(i18n.hae('yleiset.tallennus_epaonnistui'));
          });
          $scope.tila = 'nayta';
        };
        Tiedote.hae().then(function(tiedote){
          $scope.naytettavaTiedote = tiedote[kieli];
          $scope.tiedoteFi = tiedote.fi;
          $scope.tiedoteSv = tiedote.sv;
          $scope.tiedoteEn = tiedote.en;
        }).catch(function (e) {
          console.error(e);
        });
      }
    };
  }]);
