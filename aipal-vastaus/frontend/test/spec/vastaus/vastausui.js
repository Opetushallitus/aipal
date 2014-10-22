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
  }));

  function alustaController(tunnus) {
    if (tunnus === undefined) { tunnus = 'abc'; }
    $controller('VastausController', {$scope: $scope, $routeParams: {'tunnus': tunnus}});
  }

  it('pitäisi hakea kyselykerta alustuksessa scopeen', function() {
    $httpBackend.whenGET(/api\/kyselykerta\/.*/).respond({data:'data'});
    alustaController();
    $httpBackend.flush();

    expect($scope.data).toEqual({data:'data'});
  });
});
