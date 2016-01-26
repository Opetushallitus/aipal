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
  var $uibModal;
  var $q;
  var ilmoitus;

  beforeEach(module('kysely.kyselyui'));

  beforeEach(module(function($provide){
    $routeParams = {};
    $provide.value('$routeParams', $routeParams);
    $location = {path: jasmine.createSpy('path')};
    $provide.value('$location', $location);
    $provide.value('i18n', {hae: function(){return '';}});
    $provide.value('kopioi', false);
    ilmoitus = {onnistuminen: jasmine.createSpy('onnistuminen'),
                varoitus: jasmine.createSpy('varoitus'),
                virhe: jasmine.createSpy('virhe')};
    $provide.value('ilmoitus', ilmoitus);
  }));

  beforeEach(inject(function($rootScope, _$httpBackend_, _$controller_, _$uibModal_, _$q_){
    $scope = $rootScope.$new();
    $httpBackend = _$httpBackend_;
    $controller = _$controller_;
    $uibModal = _$uibModal_;
    $q = _$q_;
  }));

  function alustaController(injektiot) {
    $scope.kyselyForm = { $setDirty: function() {}, $setPristine: function() {} };
    $controller('KyselyController', _.assign({$scope: $scope}, injektiot));
  }

  function alustaControllerKopioimaan(kyselyid) {
    $routeParams.kyselyid = kyselyid;
    alustaController({kopioi: true});
  }

  it('pitäisi ilman id:tä tultaessa jättää kysely tyhjäksi', function(){
    alustaController();
    expect($scope.kysely.kysymysryhmat).toEqual([]);
    expect($scope.kysely.nimi_fi).toEqual(undefined);
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

  it('voi lisätä kysymysryhmän uuteen kyselyyn', function(){
    $httpBackend.whenGET(/api\/kysymysryhma\/123\??.*/).respond({kysymysryhmaid: 123});

    alustaController();
    spyOn($uibModal, 'open').and.returnValue({result: $q.when(123)});
    $scope.lisaaKysymysryhmaModal();
    $httpBackend.flush();

    expect($scope.kysely.kysymysryhmat).toContain({kysymysryhmaid: 123});
  });

  it('hakee kopioitavan kyselyn tiedot palvelimelta', function(){
    $httpBackend.whenGET(/api\/kysely\/1234.*/)
                .respond(200, {kysymysryhmat: [{nimi_fi: 'kr1'},
                                               {nimi_fi: 'kr2'}]});
    alustaControllerKopioimaan(1234);
    $httpBackend.flush();
    expect($scope.kysely).toEqual({kysymysryhmat: [{nimi_fi: 'kr1'},
                                                   {nimi_fi: 'kr2'}],
                                   tila: 'luonnos'});
  });

  it('ei kopioi suljettuja kysymysryhmiä', function(){
    $httpBackend.whenGET(/api\/kysely\/1234.*/)
                .respond(200, {kysymysryhmat: [{nimi_fi: 'kr1'},
                                               {nimi_fi: 'kr2',
                                                tila: 'suljettu'},
                                               {nimi_fi: 'kr3'}]});
    alustaControllerKopioimaan(1234);
    $httpBackend.flush();
    expect($scope.kysely.kysymysryhmat).toEqual([{nimi_fi: 'kr1'},
                                                 {nimi_fi: 'kr3'}]);
  });

  it('ei näytä varoitusta, jos kaikki kysymysryhmät ovat kopioitavissa', function(){
    $httpBackend.whenGET(/api\/kysely\/1234.*/)
                .respond(200, {kysymysryhmat: [{nimi_fi: 'kr1'},
                                               {nimi_fi: 'kr2'},
                                               {nimi_fi: 'kr3'}]});
    alustaControllerKopioimaan(1234);
    $httpBackend.flush();
    expect(ilmoitus.varoitus).not.toHaveBeenCalled();
  });

  it('näyttää varoituksen, jos jokin kysymysryhmä ei ole kopioitavissa', function(){
    $httpBackend.whenGET(/api\/kysely\/1234.*/)
                .respond(200, {kysymysryhmat: [{nimi_fi: 'kr1'},
                                               {nimi_fi: 'kr2',
                                                tila: 'suljettu'},
                                               {nimi_fi: 'kr3'}]});
    alustaControllerKopioimaan(1234);
    $httpBackend.flush();
    expect(ilmoitus.varoitus).toHaveBeenCalled();
  });

});
