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

angular.module('yhteiset.palvelut.seuranta', [])

  .factory('seuranta',[function() {
    var latausIndikaattorit = [],
        seuraavaAikaleima = 0;

    // aikaleimaa käytetään jotta tilan muuttumista pystytään seuraamaan siitä $watch metodilla
    function aikaleima() {
      return seuraavaAikaleima++;
    }

    return {
      asetaLatausIndikaattori : function(promise, id) {
        latausIndikaattorit[id] = { valmis: false, ok: false, paivitetty: aikaleima() };

        promise.then(function() {
          latausIndikaattorit[id].valmis = true;
          latausIndikaattorit[id].ok = true;
          latausIndikaattorit[id].paivitetty = aikaleima();
        },
        function() {
          latausIndikaattorit[id].valmis = true;
          latausIndikaattorit[id].ok = false;
          latausIndikaattorit[id].paivitetty = aikaleima();
        }).catch(function (e) {
          console.error(e);
        });

        return promise;
      },
      kuittaaVirhe: function(id) {
        latausIndikaattorit[id].ok = true;
      },
      haeTila: function(id) {
        return latausIndikaattorit[id] ? latausIndikaattorit[id] : { valmis: true, ok: true, paivitetty: -1 };
      }
    };
  }]);
