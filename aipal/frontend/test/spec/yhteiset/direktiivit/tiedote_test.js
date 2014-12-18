'use strict';

describe('yhteiset.direktiivit.tiedote', function(){
  var $compile;
  var $scope;
  var $httpBackend;

  beforeEach(module('yhteiset.direktiivit.tiedote'));
  beforeEach(module('template/yhteiset/direktiivit/tiedote.html'));

  function alustaInjector(kieli) {
    module(function($provide){
      $provide.value('kieli', kieli);
    });
    inject(function(_$compile_, $rootScope, _$httpBackend_){
      $compile = _$compile_;
      $scope = $rootScope.$new();
      $httpBackend = _$httpBackend_;
    });
  }

  it('näyttää suomenkielisen tiedotteen, jos kieli on suomi', function(){
    alustaInjector('fi');
    $httpBackend
    .whenGET(/api\/tiedote.*/)
    .respond(200, {fi: 'suomenkielinen tiedote',
                   sv: 'ruotsinkielinen tiedote'});
    var e = $compile('<div data-tiedote></div>')($scope);
    $scope.$digest();
    $httpBackend.flush();
    expect(e.html()).toMatch(/suomenkielinen tiedote/);
  });

  it('näyttää ruotsinkielisen tiedotteen, jos kieli on ruotsi', function(){
    alustaInjector('sv');
    $httpBackend
    .whenGET(/api\/tiedote.*/)
    .respond(200, {fi: 'suomenkielinen tiedote',
                   sv: 'ruotsinkielinen tiedote'});
    var e = $compile('<div data-tiedote></div>')($scope);
    $scope.$digest();
    $httpBackend.flush();
    expect(e.html()).toMatch(/ruotsinkielinen tiedote/);
  });
});
