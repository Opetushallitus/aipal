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

angular.module('yhteiset.palvelut.apicallinterceptor', [])
  .factory('apiCallInterceptor', [function() {

    var pyynnot = {};
    var vastaukset = {
      paivitetty : null,
      lista : []
    };
    var vastausCallbackit = {};

    function asetaVastausCallback(metodiId, callback) {
      if(!vastausCallbackit[metodiId]) {
        vastausCallbackit[metodiId] = [];
      }

      vastausCallbackit[metodiId].push(callback);
    }

    // Käytetään suhteellista aikaleimaa, koska saman millisekunnin aikana voi
    // tulla käsittelyyn useita pyyntöjä ja vastauksia. Ennen käytettiin
    // aikaleimana new Date().getTime(), mikä aiheutti bugin OPH-330.
    var seuraavaAikaleima = 0;
    function aikaleima() {
      return seuraavaAikaleima++;
    }

    function seuraaPyyntoa(pyynto) {
      return pyynto.id; //Seuraa pyyntöjä joilla id asetettu. Muista pyynnöistä talletetaan vain virhevastaukset.
    }

    function apiPyynto(pyynto) {
      if(seuraaPyyntoa(pyynto)) {
        if(pyynnot[pyynto.id]) {
          pyynnot[pyynto.id].pyyntojaKaynnissa++;
          pyynnot[pyynto.id].paivitetty = aikaleima();
        } else {
          pyynnot[pyynto.id] = {url : pyynto.url, pyyntojaKaynnissa: 1, viimeinenPyyntoOnnistui : true, paivitetty : aikaleima(), pyyntoObj : pyynto};
        }
      }
    }

    function apiVastaus(vastaus, virhe) {
      var seuraa = seuraaPyyntoa(vastaus.config);

      if(seuraa) {
        var id = vastaus.config.id;
        var pyynto = pyynnot[id];
        pyynto.pyyntojaKaynnissa--;
        pyynto.viimeinenPyyntoOnnistui = !virhe;
        pyynto.paivitetty = aikaleima();

        if(vastausCallbackit[id]) {
          _.each(vastausCallbackit[id], function(callback) {callback();});
        }
      }

      if(seuraa || virhe) {
        vastaukset.paivitetty = aikaleima();
        vastaukset.lista.push(vastaus);
      }
    }

    return {
      pyynnot : pyynnot,
      vastaukset : vastaukset,
      apiPyynto : apiPyynto,
      apiVastaus : apiVastaus,
      asetaVastausCallback : asetaVastausCallback
    };

  }]);
