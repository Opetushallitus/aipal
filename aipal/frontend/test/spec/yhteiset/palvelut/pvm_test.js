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

describe('Palvelut: pvm', function () {

  var pvmService;

  beforeEach(module('yhteiset.palvelut.pvm'));

  beforeEach(inject(function(pvm){
    pvmService = pvm;
  }));

  it('Muuttaa voimassa_alkupvm ja voimassa_loppupvm dateksi', function() {
    var tulos = pvmService.parsePvm({'voimassa_alkupvm': '2014-01-28', 'voimassa_loppupvm': '2014-01-29'});
    expect(tulos).toEqual({'voimassa_alkupvm': new Date('2014-01-28'), 'voimassa_loppupvm': new Date('2014-01-29')});
  });
});