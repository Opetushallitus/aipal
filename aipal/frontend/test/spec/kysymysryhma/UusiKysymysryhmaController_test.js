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

describe('kysymysryhma.kysymysryhmaui.UusiKysymysryhmaController', function(){

  var $scope;
  var $httpBackend;
  var $controller;
  var $location;
  var ilmoitus;

  beforeEach(module('kysymysryhma.kysymysryhmaui'));

  beforeEach(module(function($provide){
    $location = {path: jasmine.createSpy('path')};
    $provide.value('$location', $location);

    $provide.value('i18n', {hae: function(){return '';}});

    ilmoitus = {onnistuminen: jasmine.createSpy('onnistuminen'),
                virhe: jasmine.createSpy('virhe')};
    $provide.value('ilmoitus', ilmoitus);
  }));

  beforeEach(inject(function($rootScope, _$httpBackend_, _$controller_){
    $scope = $rootScope.$new();
    $httpBackend = _$httpBackend_;
    $controller = _$controller_;
  }));

  function alustaController() {
    $controller('UusiKysymysryhmaController', {$scope: $scope});
  }

  it('lähettää kysymysryhmän tiedot backendiin, kun luontinappia painetaan', function(){
    alustaController();
    $httpBackend.expectPOST('api/kysymysryhma', {nimi_fi: 'foo',
                                                 nimi_sv: 'fåå',
                                                 selite_fi: 'bar',
                                                 selite_sv: 'bår'}).respond(200);
    $scope.kysymysryhma = {};
    $scope.kysymysryhma.nimi_fi = 'foo';
    $scope.kysymysryhma.nimi_sv = 'fåå';
    $scope.kysymysryhma.selite_fi = 'bar';
    $scope.kysymysryhma.selite_sv = 'bår';
    $scope.luoUusi();
    $httpBackend.verifyNoOutstandingExpectation();
  });

  it('siirtää käyttäjän kysymysryhmien listausnäytölle luonnin jälkeen, jos luonti onnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(200);
    $scope.luoUusi();
    $httpBackend.flush();
    expect($location.path).toHaveBeenCalledWith('/kysymysryhmat');
  });

  it('ei näytä virheilmoitusta, jos luonti onnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(200);
    $scope.luoUusi();
    $httpBackend.flush();
    expect(ilmoitus.virhe).not.toHaveBeenCalled();
  });

  it('näyttää ilmoituksen, jos luonti onnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(200);
    $scope.luoUusi();
    $httpBackend.flush();
    expect(ilmoitus.onnistuminen).toHaveBeenCalled();
  });

  it('ei siirrä käyttäjää, jos luonti epäonnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(500);
    $scope.luoUusi();
    $httpBackend.flush();
    expect($location.path).not.toHaveBeenCalled();
  });

  it('näyttää virheilmoituksen, jos luonti epäonnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(500);
    $scope.luoUusi();
    $httpBackend.flush();
    expect(ilmoitus.virhe).toHaveBeenCalled();
  });

});
