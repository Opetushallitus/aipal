'use strict';

describe('yhteiset.direktiivit.tiedote', function(){
  var $compile;
  var $scope;
  var $httpBackend;

  beforeEach(module('yhteiset.direktiivit.tiedote'));
  beforeEach(module('template/yhteiset/direktiivit/tiedote.html'));

  afterEach(function(){
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

  function alustaInjector(kieli) {
    module(function($provide){
      $provide.value('kieli', kieli);
      $provide.value('i18nFilter', function(){return '';});
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
    $compile('<div data-tiedote></div>')($scope);
    $scope.$digest();
    $httpBackend.flush();
    expect($scope.naytettavaTiedote).toEqual('suomenkielinen tiedote');
  });

  it('näyttää ruotsinkielisen tiedotteen, jos kieli on ruotsi', function(){
    alustaInjector('sv');
    $httpBackend
    .whenGET(/api\/tiedote.*/)
    .respond(200, {fi: 'suomenkielinen tiedote',
                   sv: 'ruotsinkielinen tiedote'});
    $compile('<div data-tiedote></div>')($scope);
    $scope.$digest();
    $httpBackend.flush();
    expect($scope.naytettavaTiedote).toEqual('ruotsinkielinen tiedote');
  });

  it('tallentaa muutokset tiedotteesen palvelimelle', function(){
    alustaInjector('fi');
    $httpBackend
    .whenGET(/api\/tiedote.*/)
    .respond(200, {fi: 'suomenkielinen tiedote',
                   sv: 'ruotsinkielinen tiedote'});
    $compile('<div data-tiedote></div>')($scope);
    $scope.$digest();
    $httpBackend.flush();
    $scope.muokkaa();
    $scope.$digest();
    $scope.tiedoteFi = 'päivitetty suomenkielinen';
    $scope.tiedoteSv = 'päivitetty ruotsinkielinen';
    $httpBackend
    .expectPOST('api/tiedote', {fi: 'päivitetty suomenkielinen',
                                sv: 'päivitetty ruotsinkielinen'})
    .respond(200);
    $scope.tallenna();
    $httpBackend.flush();
  });

  it('päivittää näytettävän tiedotteen tallennuksen jälkeen', function(){
    alustaInjector('fi');
    $httpBackend
    .whenGET(/api\/tiedote.*/)
    .respond(200, {fi: 'suomenkielinen tiedote',
                   sv: 'ruotsinkielinen tiedote'});
    $compile('<div data-tiedote></div>')($scope);
    $scope.$digest();
    $httpBackend.flush();
    $scope.muokkaa();
    $scope.$digest();
    $scope.tiedoteFi = 'päivitetty suomenkielinen';
    $scope.tiedoteSv = 'päivitetty ruotsinkielinen';
    $httpBackend.whenPOST('api/tiedote').respond(200);
    $scope.tallenna();
    $httpBackend.flush();
    expect($scope.naytettavaTiedote).toEqual('päivitetty suomenkielinen');
  });

});
