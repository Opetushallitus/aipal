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

describe('kysely.kyselyui.KyselylistaController', function(){

  var $scope;
  var $httpBackend;
  var $controller;
  var ilmoitus;
  var varmistus;
  var varmistusHyvaksytty;
  var varmistusPeruutettu;

  beforeEach(module('yhteiset.direktiivit.kyselylista'));

  beforeEach(module(function($provide){
    $provide.value('i18n', {hae: function(){return '';}});
    ilmoitus = {onnistuminen: jasmine.createSpy('onnistuminen'),
                varoitus: jasmine.createSpy('varoitus'),
                virhe: jasmine.createSpy('virhe')};
    $provide.value('ilmoitus', ilmoitus);
    varmistusHyvaksytty = {then: function(f){f();}};
    varmistusPeruutettu = {then: function(){}};
    varmistus = {varmista: jasmine.createSpy('varmista')
                           .and.returnValue(varmistusHyvaksytty)};
    $provide.value('varmistus', varmistus);
  }));

  beforeEach(inject(function($rootScope, _$httpBackend_, _$controller_){
    $scope = $rootScope.$new();
    $httpBackend = _$httpBackend_;
    $controller = _$controller_;
  }));

  afterEach(function(){
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

  function alustaController() {
    $controller('KyselylistaController', {$scope: $scope});
  }

  it('Ei poista kyselykertaa, jos käyttäjä peruuttaa varmistuksen', function(){
    alustaController();
    varmistus.varmista.and.returnValue(varmistusPeruutettu);
    $scope.poistaKyselykerta({kyselykertaid: 123});
    $httpBackend.verifyNoOutstandingRequest();
  });

  it('Ei näytä onnistumisilmoitusta, jos kyselykerran poistaminen epäonnistuu', function(){
    alustaController();

    $httpBackend.whenDELETE('api/kyselykerta/123').respond(500);
    $scope.poistaKyselykerta({kyselykertaid: 123});
    $httpBackend.flush();

    expect(ilmoitus.onnistuminen).not.toHaveBeenCalled();
  });

  it('Näyttää virheilmoituksen, jos kyselykerran poistaminen epäonnistuu', function(){
    alustaController();

    $httpBackend.whenDELETE('api/kyselykerta/123').respond(500);
    $scope.poistaKyselykerta({kyselykertaid: 123});
    $httpBackend.flush();

    expect(ilmoitus.virhe).toHaveBeenCalled();
  });

  it('Näyttää onnistumisilmoituksen, jos kyselykerran poistaminen onnistuu', function(){
    alustaController();
    $scope.kyselyt = [{kyselykerrat: [{kyselykertaid: 123}]}];

    $httpBackend.whenDELETE('api/kyselykerta/123').respond(204);
    $scope.poistaKyselykerta({kyselykertaid: 123});
    $httpBackend.flush();

    expect(ilmoitus.onnistuminen).toHaveBeenCalled();
  });

  it('Ei näytä virheilmoitusta, jos kyselykerran poistaminen onnistuu', function(){
    alustaController();
    $scope.kyselyt = [{kyselykerrat: [{kyselykertaid: 123}]}];

    $httpBackend.whenDELETE('api/kyselykerta/123').respond(204);
    $scope.poistaKyselykerta({kyselykertaid: 123});
    $httpBackend.flush();

    expect(ilmoitus.virhe).not.toHaveBeenCalled();
  });

  it('Poistaa poistetun kyselykerran käyttöliittymästä', function(){
    alustaController();
    $scope.kyselyt = [{kyselykerrat: [{kyselykertaid: 123},
                                      {kyselykertaid: 456},
                                      {kyselykertaid: 789}]}];

    $httpBackend.whenDELETE('api/kyselykerta/456').respond(204);
    $scope.poistaKyselykerta({kyselykertaid: 456});
    $httpBackend.flush();

    expect($scope.kyselyt[0].kyselykerrat).toEqual([{kyselykertaid: 123},
                                                    {kyselykertaid: 789}]);
  });

});
