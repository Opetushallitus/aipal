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

angular.module('rest.kysymysryhma', ['ngResource'])
  .factory('Kysymysryhma', ['$http', function($http){
    return {
      haeKaikki: function(){
        return $http.get('api/kysymysryhma',
                         {params: {nocache: Date.now()}});
      },
      haeVoimassaolevat: function() {
        return $http.get('api/kysymysryhma',
                         {params: {nocache: Date.now(),
                                   voimassa: 'true'}});
      },
      haeTaustakysymysryhmat: function() {
        return $http.get('api/kysymysryhma',
                         {params: {nocache: Date.now(),
                                   taustakysymysryhmat: 'true'}});
      },
      haeTaustakysymysryhma: function(kysymysryhmaid) {
        return $http.get('api/kysymysryhma/taustakysymysryhma/' + kysymysryhmaid, {params: {nocache: Date.now()}});
      },
      hae: function(kysymysryhmaid) {
        return $http.get('api/kysymysryhma/' + kysymysryhmaid, {params: {nocache: Date.now()}});
      },
      haeEsikatselulle: function(kysymysryhmaid) {
        return $http.get('api/kysymysryhma/' + kysymysryhmaid + '/esikatselu', {params: {nocache: Date.now()}});
      },
      luoUusi: function(kysymysryhma) {
        return $http.post('api/kysymysryhma', kysymysryhma);
      },
      tallenna: function(kysymysryhma) {
        return $http.put('api/kysymysryhma/' + kysymysryhma.kysymysryhmaid, kysymysryhma);
      },
      julkaise: function(kysymysryhma) {
        return $http.put('api/kysymysryhma/' + kysymysryhma.kysymysryhmaid + '/julkaise');
      },
      sulje: function(kysymysryhma) {
        return $http.put('api/kysymysryhma/' + kysymysryhma.kysymysryhmaid + '/sulje');
      },
      palautaLuonnokseksi: function(kysymysryhma) {
        return $http.put('api/kysymysryhma/' + kysymysryhma.kysymysryhmaid + '/palauta');
      },
      poista: function(kysymysryhmaid) {
        return $http.delete('api/kysymysryhma/' + kysymysryhmaid);
      },
      haeAsteikot: function(){
        return $http.get('api/kysymysryhma/asteikot', {params: {nocache: Date.now()}});
      },
      tallennaAsteikko: function(nimi, asteikko){
        return $http.post('api/kysymysryhma/asteikot', {nimi: nimi, asteikko: {vaihtoehdot: asteikko}});
      }
    };
  }]);
