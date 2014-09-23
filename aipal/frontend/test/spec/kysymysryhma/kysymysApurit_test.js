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

describe('kysymysryhma.kysymysryhmaui.kysymysApurit', function(){

  var apu;

  beforeEach(module('kysymysryhma.kysymysryhmaui'));

  beforeEach(inject(function(kysymysApurit){
    apu = kysymysApurit;
  }));

  it('pitäisi asettaa kysymyksen monivalintojen maksimimäärän vaihtoehtojen määrään jos maksimimäärä on suurempi kuin vaihtoehtojen määrä poiston jälkeen', function(){
    var kysymys = {
      monivalinta_max: 3,
      monivalintavaihtoehdot: [1, 2, 3]
    };
    apu.poistaVaihtoehto(kysymys, 1);
    expect(kysymys.monivalinta_max).toEqual(2);
  });
  it('pitäisi jättää monivalintojen maksimimäärän ennalleen määrään jos on suurempi poiston jälkeen', function(){
    var kysymys = {
      monivalinta_max: 1,
      monivalintavaihtoehdot: [1, 2, 3]
    };
    apu.poistaVaihtoehto(kysymys, 1);
    expect(kysymys.monivalinta_max).toEqual(1);
  });
  it('pitäisi poistaa taulukon indeksin mukainen vaihtoehto monivalintavaihtoehdoista', function(){
    var kysymys = {
      monivalintavaihtoehdot: [1, 2, 3]
    };
    apu.poistaVaihtoehto(kysymys, 1);
    expect(kysymys.monivalintavaihtoehdot).toEqual([1,3]);
  });
});
