'use strict';

describe('yhteiset.direktiivit.tiedote', function(){
  var $compile;
  var $scope;
  var $httpBackend;
  var haku;
  var ilmoitus;
  var tallennus;

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
      ilmoitus = {onnistuminen: jasmine.createSpy('onnistuminen'),
                  varoitus: jasmine.createSpy('varoitus'),
                  virhe: jasmine.createSpy('virhe')};
      $provide.value('ilmoitus', ilmoitus);
      $provide.value('i18n', {hae: function(){return '';}});
    });
    inject(function(_$compile_, $rootScope, _$httpBackend_){
      $compile = _$compile_;
      $scope = $rootScope.$new();
      $httpBackend = _$httpBackend_;

      haku = $httpBackend.whenGET(/api\/tiedote.*/);
      haku.respond(200, {});

      tallennus = $httpBackend.whenPOST('api/tiedote');
      tallennus.respond(200);
    });
  }

  function alustaDirektiivi() {
    $compile('<div data-tiedote></div>')($scope);
    $scope.$digest();
    $httpBackend.flush();
  }

  function tallenna() {
    $scope.tallenna();
    $httpBackend.flush();
  }

  it('näyttää suomenkielisen tiedotteen, jos kieli on suomi', function(){
    alustaInjector('fi');
    haku.respond(200, {fi: 'suomenkielinen tiedote',
                       sv: 'ruotsinkielinen tiedote'});
    alustaDirektiivi();
    expect($scope.naytettavaTiedote).toEqual('suomenkielinen tiedote');
  });

  it('näyttää ruotsinkielisen tiedotteen, jos kieli on ruotsi', function(){
    alustaInjector('sv');
    haku.respond(200, {fi: 'suomenkielinen tiedote',
                       sv: 'ruotsinkielinen tiedote'});
    alustaDirektiivi();
    expect($scope.naytettavaTiedote).toEqual('ruotsinkielinen tiedote');
  });

  it('tallentaa muutokset tiedotteesen palvelimelle', function(){
    alustaInjector('fi');
    alustaDirektiivi();
    $scope.muokkaa();
    $scope.tiedoteFi = 'päivitetty suomenkielinen';
    $scope.tiedoteSv = 'päivitetty ruotsinkielinen';
    $httpBackend
    .expectPOST('api/tiedote', {fi: 'päivitetty suomenkielinen',
                                sv: 'päivitetty ruotsinkielinen'})
    .respond(200);
    tallenna();
  });

  it('päivittää näytettävän tiedotteen, jos tallennus onnistuu', function(){
    alustaInjector('fi');
    haku.respond(200, {fi: 'suomenkielinen tiedote',
                       sv: 'ruotsinkielinen tiedote'});
    alustaDirektiivi();
    $scope.muokkaa();
    $scope.tiedoteFi = 'päivitetty suomenkielinen';
    $scope.tiedoteSv = 'päivitetty ruotsinkielinen';
    tallenna();
    expect($scope.naytettavaTiedote).toEqual('päivitetty suomenkielinen');
  });

  it('ei päivitä näytettävää tiedotetta, jos tallennus epäonnistuu', function(){
    alustaInjector('fi');
    haku.respond(200, {fi: 'suomenkielinen tiedote',
                       sv: 'ruotsinkielinen tiedote'});
    alustaDirektiivi();
    $scope.muokkaa();
    $scope.tiedoteFi = 'päivitetty suomenkielinen';
    $scope.tiedoteSv = 'päivitetty ruotsinkielinen';
    tallennus.respond(500);
    tallenna();
    expect($scope.naytettavaTiedote).toEqual('suomenkielinen tiedote');
  });

  it('näyttää onnistumisilmoituksen, jos tallennus onnistuu', function(){
    alustaInjector('fi');
    alustaDirektiivi();
    $scope.muokkaa();
    tallenna();
    expect(ilmoitus.onnistuminen).toHaveBeenCalled();
  });

  it('ei näytä onnistumisilmoitusta, jos tallennus epäonnistuu', function(){
    alustaInjector('fi');
    alustaDirektiivi();
    $scope.muokkaa();
    tallennus.respond(500);
    tallenna();
    expect(ilmoitus.onnistuminen).not.toHaveBeenCalled();
  });

  it('ei näytä virheilmoitusta, jos tallennus onnistuu', function(){
    alustaInjector('fi');
    alustaDirektiivi();
    $scope.muokkaa();
    tallenna();
    expect(ilmoitus.virhe).not.toHaveBeenCalled();
  });

  it('näyttää virheilmoituksen, jos tallennus epäonnistuu', function(){
    alustaInjector('fi');
    alustaDirektiivi();
    $scope.muokkaa();
    tallennus.respond(500);
    tallenna();
    expect(ilmoitus.virhe).toHaveBeenCalled();
  });

});
