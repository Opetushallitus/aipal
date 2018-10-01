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

describe('kysely.kyselyui.KyselykertaController', function(){

  var $scope;
  var $httpBackend;
  var $controller;
  var $routeParams;
  var $location;
  var $uibModal;
  var $q;

  beforeEach(module('ui.bootstrap','kyselykerta.kyselykertaui','mock.yhteiset.palvelut.i18n'));

  beforeEach(module(function($provide){
    $routeParams = {};
    $provide.value('$routeParams', $routeParams);
    $location = {path: jasmine.createSpy('path')};
    $provide.value('$location', $location);
    $provide.value('kopioi', false);
  }));

  function alustaController() {
    $scope.kyselykertaForm = { $setDirty: function() {}, $setPristine: function() {} };
    $httpBackend.whenGET(/api\/vastaajatunnus\/.*/).respond([]);
    $httpBackend.whenGET(/api\/tutkinto\/voimassaolevat.*/).respond([]);
    $httpBackend.whenGET(/api\/koulutustoimija.*/).respond([]);
    $httpBackend.whenGET(/api\/kieli.*/).respond([{kieli: 'fi'}]);
    $controller('KyselykertaController', {$scope: $scope});
  }

  describe('uusi = false', function() {
    beforeEach(module(function($provide){
      $provide.value('uusi', false);
    }));

    beforeEach(inject(function($rootScope, _$httpBackend_, _$controller_, _$uibModal_, _$q_){
      $scope = $rootScope.$new();
      $httpBackend = _$httpBackend_;
      $controller = _$controller_;
      $uibModal = _$uibModal_;
      $q = _$q_;
    }));

    it('pitäisi alustaa kysely ja kyselykerta', function() {
      $httpBackend.whenGET(/api\/kysely\/1\?nocache=.*/).respond({nimi_fi:'kysely'});
      $httpBackend.whenGET(/api\/kyselykerta\/2\?nocache=.*/).respond({nimi:'kyselykerta'});
      $routeParams.kyselyid = 1;
      $routeParams.kyselykertaid = 2;
      alustaController();
      $httpBackend.flush();
      expect($scope.kysely).toEqual({nimi_fi:'kysely'});
      expect($scope.kyselykerta).toEqual({nimi:'kyselykerta'});
    });

    it('pitäisi olla muokkaustilassa jos kysely on käytettävissä ja kyselykerta ei ole lukittu', function() {
      $httpBackend.whenGET(/api\/kysely\/1\?nocache=.*/).respond({kaytettavissa: true});
      $httpBackend.whenGET(/api\/kyselykerta\/2\?nocache=.*/).respond({lukittu: false});
      $routeParams.kyselyid = 1;
      $routeParams.kyselykertaid = 2;
      alustaController();
      $httpBackend.flush();
      expect($scope.muokkaustila).toEqual(true);
    });

    it('ei pitäisi olla muokkaustilassa jos kysely on käytettävissä mutta kyselykerta on lukittu', function() {
      $httpBackend.whenGET(/api\/kysely\/1\?nocache=.*/).respond({kaytettavissa: true});
      $httpBackend.whenGET(/api\/kyselykerta\/2\?nocache=.*/).respond({lukittu: true});
      $routeParams.kyselyid = 1;
      $routeParams.kyselykertaid = 2;
      alustaController();
      $httpBackend.flush();
      expect($scope.muokkaustila).toEqual(false);
    });

    it('ei pitäisi olla muokkaustilassa jos kysely ei ole käytettävissä mutta kyselykerta ei ole lukittu', function() {
      $httpBackend.whenGET(/api\/kysely\/1\?nocache=.*/).respond({kaytettavissa: false});
      $httpBackend.whenGET(/api\/kyselykerta\/2\?nocache=.*/).respond({lukittu: false});
      $routeParams.kyselyid = 1;
      $routeParams.kyselykertaid = 2;
      alustaController();
      $httpBackend.flush();
      expect($scope.muokkaustila).toEqual(false);
    });

    it('ei pitäisi olla muokkaustilassa jos kysely ei ole käytettävissä ja kyselykerta on lukittu', function() {
      $httpBackend.whenGET(/api\/kysely\/1\?nocache=.*/).respond({kaytettavissa: false});
      $httpBackend.whenGET(/api\/kyselykerta\/2\?nocache=.*/).respond({lukittu: true});
      $routeParams.kyselyid = 1;
      $routeParams.kyselykertaid = 2;
      alustaController();
      $httpBackend.flush();
      expect($scope.muokkaustila).toEqual(false);
    });
  });

  describe('uusi = false', function() {
    beforeEach(module(function($provide){
      $provide.value('uusi', true);
    }));

    beforeEach(inject(function($rootScope, _$httpBackend_, _$controller_, _$uibModal_, _$q_){
      $scope = $rootScope.$new();
      $httpBackend = _$httpBackend_;
      $controller = _$controller_;
      $uibModal = _$uibModal_;
      $q = _$q_;
    }));

    it('pitäisi alustaa kysely ja jättää kyselykerta tyhjäksi', function() {
      $httpBackend.whenGET(/api\/kysely\/1\?nocache=.*/).respond({nimi_fi:'kysely'});
      $routeParams.kyselyid = 1;
      alustaController();
      $httpBackend.flush();
      expect($scope.kysely).toEqual({nimi_fi:'kysely'});
      expect($scope.kyselykerta).toEqual({});
    });

    it('pitäisi olla muokkaustilassa kun kysely on kaytettavissa', function() {
      $httpBackend.whenGET(/api\/kysely\/1\?nocache=.*/).respond({kaytettavissa: true});
      $routeParams.kyselyid = 1;
      alustaController();
      $httpBackend.flush();
      expect($scope.muokkaustila).toEqual(true);
    });

    it('ei pitäisi olla muokkaustilassa kun kysely ei ole kaytettavissa', function() {
      $httpBackend.whenGET(/api\/kysely\/1\?nocache=.*/).respond({kaytettavissa: false});
      $routeParams.kyselyid = 1;
      alustaController();
      $httpBackend.flush();
      expect($scope.muokkaustila).toEqual(false);
    });
  });

});
