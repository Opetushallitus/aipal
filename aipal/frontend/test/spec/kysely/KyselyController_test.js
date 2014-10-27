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

describe('kysely.kyselyui.KyselyController', function(){

  var $scope;
  var $httpBackend;
  var $controller;
  var $routeParams;
  var $location;

  beforeEach(module('ui.bootstrap','kysely.kyselyui','yhteiset.palvelut.seuranta'));

  beforeEach(module(function($provide){
    $routeParams = {};
    $provide.value('$routeParams', $routeParams);
    $location = {path: jasmine.createSpy('path')};
    $provide.value('$location', $location);
    $provide.value('i18n', {hae: function(){return '';}});
  }));

  beforeEach(inject(function($rootScope, _$httpBackend_, _$controller_){
    $scope = $rootScope.$new();
    $httpBackend = _$httpBackend_;
    $controller = _$controller_;
  }));

  function alustaController() {
    $scope.kyselyForm = { $setPristine: function() {} };
    $controller('KyselyController', {$scope: $scope});
  }

  it('pitäisi ilman id:tä tultaessa jättää kysely tyhjäksi', function(){
    alustaController();
    expect($scope.kysely).toEqual({});
  });

  it('pitäisi id:n kanssa tultaessa hakea kysely palvelimelta', function(){
    $httpBackend.whenGET(/api\/kysely\/1\?nocache=.*/).respond({nimi_fi:'kysely'});
    $routeParams.kyselyid = 1;
    alustaController();
    $httpBackend.flush();
    expect($scope.kysely).toEqual({nimi_fi:'kysely'});
  });

  it('pitäisi kutsua uuden kyselyn luontia jos tallennetaan kysely kun on tultu ilman id:tä', function(){
    alustaController();

    $httpBackend.expectPOST('api/kysely').respond(200);
    $scope.tallenna();
    $httpBackend.flush();
  });

  it('pitäisi kutsua kyselyn muokkausta jos tallennetaan kysely kun on tultu kyselyn id:llä', function(){
    $httpBackend.whenGET(/api\/kysely\/1\?nocache=.*/).respond({kyselyid:1, nimi_fi:'kysely'});
    $routeParams.kyselyid = 1;
    alustaController();
    $httpBackend.flush();

    $httpBackend.expectPOST('api/kysely\/1').respond(200);
    $scope.tallenna();
    $httpBackend.flush();
  });
});
