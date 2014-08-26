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

describe('vastaus.vastausui.VastausController', function() {
  var $controller;
  var $httpBackend;
  var $scope;

  beforeEach(module('vastaus.vastausui'));

  beforeEach(inject(function(_$controller_, _$httpBackend_, _$rootScope_) {
    $controller = _$controller_;
    $httpBackend = _$httpBackend_;
    $scope = _$rootScope_.$new();

    sessionStorage.clear();

    $httpBackend.whenGET(/api\/kyselykerta\/.*/).respond({});
  }));

  function alustaController(tunnus) {
    if (tunnus === undefined) { tunnus = 'abc'; }
    $controller('VastausController', {$scope: $scope, $routeParams: {'tunnus': tunnus}});
  }

  it('Uusi vastaajaid haetaan $scopeen', function() {
    $httpBackend.expectPOST('api/vastaaja/luo').respond({ 'vastaajaid': 123456 });
    alustaController();
    $httpBackend.flush();

    expect($scope.vastaajaid).toEqual(123456);
  });

  it('Uusi vastaajaid haetaan jos tunnus vaihtuu', function() {
    $httpBackend.expectPOST('api/vastaaja/luo').respond({ 'vastaajaid': 123456 });
    alustaController('abc');
    $httpBackend.flush();

    $httpBackend.expectPOST('api/vastaaja/luo').respond({ 'vastaajaid': 23456 });
    alustaController('def');
    $httpBackend.flush();

    expect($scope.vastaajaid).toEqual(23456);
  });

  it('Uusi vastaajaid haetaan vain kerran', function() {
    $httpBackend.expectPOST('api/vastaaja/luo').respond({ 'vastaajaid': 123456 });
    alustaController();
    $httpBackend.flush();

    alustaController();
    $httpBackend.flush(); // Hajoaa jos POST api/vastaaja/luo tehdään toisen kerran
  });

  it('Uusi vastaajaid haetaan tallennuksen jälkeisellä sivulatauksella', function() {
    $httpBackend.expectPOST('api/vastaaja/luo').respond({ 'vastaajaid': 123456 });
    alustaController();
    $httpBackend.flush();

    $scope.data = {};
    $httpBackend.whenPOST(/api\/vastaus\/.*/).respond({});
    $scope.tallenna();
    $httpBackend.flush();

    $httpBackend.expectPOST('api/vastaaja/luo').respond({ 'vastaajaid': 23456 });
    alustaController();
    $httpBackend.flush();
    expect($scope.vastaajaid).toEqual(23456);
  });
});
