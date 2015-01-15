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

describe('kysymysryhma.kysymysryhmaui.KysymysryhmatController', function(){

  var $scope;
  var $httpBackend;
  var $controller;

  beforeEach(module('ui.bootstrap','kysymysryhma.kysymysryhmaui','mock.yhteiset.palvelut.i18n'));
  beforeEach(inject(function($rootScope, _$httpBackend_, _$controller_){
    $scope = $rootScope.$new();
    $httpBackend = _$httpBackend_;
    $controller = _$controller_;
  }));

  function alustaController() {
    $controller('KysymysryhmatController', {$scope: $scope});
  }

  it('hakee kysymysryhmät REST APIa käyttäen', function(){
    $httpBackend.whenGET(/api\/kysymysryhma\?nocache=.*/).respond([{nimi_fi: 'foo'}]);
    alustaController();
    $httpBackend.flush();
    expect(_.map($scope.kysymysryhmat, 'nimi_fi')).toEqual(['foo']);
  });

  it('kertoo templatelle kun lataus on kesken', function(){
    alustaController();
    expect($scope.latausValmis).toEqual(false);
  });

  it('kertoo templatelle kun lataus on valmis', function(){
    $httpBackend.whenGET(/api\/kysymysryhma\?nocache=.*/).respond([]);
    alustaController();
    $httpBackend.flush();
    expect($scope.latausValmis).toEqual(true);
  });

});
