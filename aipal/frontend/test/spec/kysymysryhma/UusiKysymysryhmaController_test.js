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
  var $window;
  var toaster;

  beforeEach(module('kysymysryhma.kysymysryhmaui'));

  beforeEach(module(function($provide){
    $window = {location: {}};
    $provide.value('$window', $window);

    $provide.value('i18n', {hae: function(){return '';}});

    toaster = {pop: jasmine.createSpy('pop')};
    $provide.value('toaster', toaster);
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
                                                 selite_sv: 'bår'}).respond(201);
    $scope.kysely.nimi_fi = 'foo';
    $scope.kysely.nimi_sv = 'fåå';
    $scope.kysely.selite_fi = 'bar';
    $scope.kysely.selite_sv = 'bår';
    $scope.luoUusi();
    $httpBackend.verifyNoOutstandingExpectation();
  });

  it('siirtää käyttäjän kysymysryhmien listausnäytölle luonnin jälkeen, jos luonti onnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(201);
    $scope.luoUusi();
    $httpBackend.flush();
    expect($window.location.hash).toEqual('/kysymysryhmat');
  });

  it('ei näytä virheilmoitusta, jos luonti onnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(201);
    $scope.luoUusi();
    $httpBackend.flush();
    expect(toaster.pop).not.toHaveBeenCalled();
  });

  it('ei siirrä käyttäjää, jos luonti epäonnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(500);
    $scope.luoUusi();
    $httpBackend.flush();
    expect($window.location.hash).toBe(undefined);
  });

  it('näyttää virheilmoituksen, jos luonti epäonnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(500);
    $scope.luoUusi();
    $httpBackend.flush();
    expect(toaster.pop).toHaveBeenCalledWith('error', null, jasmine.any(String));
  });

});
