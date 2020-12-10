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

describe('kysymysryhma.kysymysryhmaui.KysymysryhmaController', function(){

  var $scope;
  var $httpBackend;
  var $controller;
  var $location;
  var ilmoitus;
  var $routeParams;

  beforeEach(module('kysymysryhma.kysymysryhmaui'));

  beforeEach(module(function($provide){
    $location = {path: jasmine.createSpy('path')};
    $provide.value('$location', $location);

    $routeParams = {};
    $provide.value('$routeParams', $routeParams);

    $provide.value('i18n', {hae: function(){return '';}});

    ilmoitus = {onnistuminen: jasmine.createSpy('onnistuminen'),
                varoitus: jasmine.createSpy('varoitus'),
                virhe: jasmine.createSpy('virhe')};
    $provide.value('ilmoitus', ilmoitus);
    $provide.value('uusi', true);
    $provide.value('kopioi', false);
  }));

  beforeEach(inject(function($rootScope, _$httpBackend_, _$controller_){
    $scope = $rootScope.$new();
    $httpBackend = _$httpBackend_;
    $controller = _$controller_;
  }));

  function alustaController(injektiot) {
    $controller('KysymysryhmaController', _.assign({$scope: $scope}, injektiot));
    $scope.form = {
      $setPristine: function() {},
      $valid: true
    };
    $httpBackend.expectGET(/api\/kayttaja/).respond({
      impersonoitu_kayttaja: '',
      sukunimi: 'Pääkäyttäjä',
      etunimi: 'Pekka',
      aktiivinen_rooli: {
        koulutustoimija_sv: null,
        koulutustoimija_fi: 'Testi-Opetushallitus',
        rooli_organisaatio_id: 1767,
        organisaatio: '9876543-2',
        rooli: 'YLLAPITAJA'
      },
      uid: 'T-1001',
      voimassa: true,
      roolit: [{
        koulutustoimija_sv: null,
        koulutustoimija_fi: 'Testi-Opetushallitus',
        rooli_organisaatio_id: 1767,
        organisaatio: '9876543-2',
        rooli: 'YLLAPITAJA'
      }],
      oid: 'OID.T-1001'});
  }

  function alustaControllerKopioimaan(kysymysryhmaid) {
    $routeParams.kysymysryhmaid = kysymysryhmaid;
    alustaController({kopioi: true});
  }

  it('lähettää kysymysryhmän tiedot backendiin, kun luontinappia painetaan', function(){
    alustaController();
    $httpBackend.expectPOST('api/kysymysryhma', {nimi_fi: 'foo',
                                                 nimi_sv: 'fåå',
                                                 selite_fi: 'bar',
                                                 selite_sv: 'bår',
                                                 kysymykset: []}).respond(200);
    $scope.kysymysryhma = {};
    $scope.kysymysryhma.nimi_fi = 'foo';
    $scope.kysymysryhma.nimi_sv = 'fåå';
    $scope.kysymysryhma.selite_fi = 'bar';
    $scope.kysymysryhma.selite_sv = 'bår';
    $scope.tallennaKysymysryhma();
    $httpBackend.verifyNoOutstandingExpectation();
  });

  it('siirtää käyttäjän kysymysryhmien listausnäytölle luonnin jälkeen, jos luonti onnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(200);
    $scope.tallennaKysymysryhma();
    $httpBackend.flush();
    expect($location.path).toHaveBeenCalledWith('/kysymysryhmat');
  });

  it('ei näytä virheilmoitusta, jos luonti onnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(200);
    $scope.tallennaKysymysryhma();
    $httpBackend.flush();
    expect(ilmoitus.virhe).not.toHaveBeenCalled();
  });

  it('näyttää ilmoituksen, jos luonti onnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(200);
    $scope.tallennaKysymysryhma();
    $httpBackend.flush();
    expect(ilmoitus.onnistuminen).toHaveBeenCalled();
  });

  it('ei siirrä käyttäjää, jos luonti epäonnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(500);
    $scope.tallennaKysymysryhma();
    $httpBackend.flush();
    expect($location.path).not.toHaveBeenCalled();
  });

  it('näyttää virheilmoituksen, jos luonti epäonnistuu', function(){
    alustaController();
    $httpBackend.whenPOST('api/kysymysryhma').respond(500);
    $scope.tallennaKysymysryhma();
    $httpBackend.flush();
    expect(ilmoitus.virhe).toHaveBeenCalled();
  });

  it('ainoan kysymyksen poisto', function() {
    alustaController();
    var kysymys =  {foo: 'bar'};
    $scope.kysymysryhma.kysymykset = [kysymys];
    $scope.poistaTahiPalautaKysymys(kysymys);
    $scope.poistaKysymykset();
    expect($scope.kysymysryhma.kysymykset).toEqual([]);
  });

  it('kysymyksen poisto', function() {
    alustaController();
    var kysymys1 = {foo: 'bar'},
        kysymys2 = {bar: 'baz'};

    var kysymykset =  [kysymys1,kysymys2];

    $scope.kysymysryhma.kysymykset = kysymykset;
    $scope.poistaTahiPalautaKysymys(kysymys2);
    $scope.poistaKysymykset();
    expect($scope.kysymysryhma.kysymykset).toEqual([{foo: 'bar'}]);
  });

  it('kysymyksen palautus', function() {
    alustaController();
    var kysymys =  {foo: 'bar'};
    $scope.kysymysryhma.kysymykset = [kysymys];
    $scope.poistaTahiPalautaKysymys(kysymys);
    $scope.poistaTahiPalautaKysymys(kysymys);
    $scope.poistaKysymykset();
    expect($scope.kysymysryhma.kysymykset).toEqual([{foo: 'bar', poistetaan_kysymysryhmasta: false}]);
  });

  it('antaa tallentaa, jos kaikki tiedot ovat kunnossa', function(){
    alustaController();
    expect($scope.tallennusSallittu()).toBe(true);
  });

  it('ei anna tallentaa, jos lomakkeen tiedoissa on virheitä', function(){
    alustaController();
    $scope.form.$valid = false;
    expect($scope.tallennusSallittu()).toBe(false);
  });

  it('ei anna tallentaa muokkaustilassa', function(){
    alustaController();
    $scope.lisaaKysymys();
    expect($scope.tallennusSallittu()).toBe(false);
  });

  it('hakee kopioitavan ryhmän tiedot palvelimelta', function(){
    $httpBackend.whenGET(/api\/kysymysryhma\/1234.*/)
                .respond(200, {kysymysryhmaid: 1234,
                               kysymykset: [{vastaustyyppi: 'kylla_ei_valinta'},
                                            {vastaustyyppi: 'likert_asteikko'},
                                            {vastaustyyppi: 'monivalinta'}]});
    alustaControllerKopioimaan(1234);
    $httpBackend.flush();
    expect($scope.kysymysryhma)
    .toEqual({kysymykset: [{vastaustyyppi: 'kylla_ei_valinta'},
                           {vastaustyyppi: 'likert_asteikko'},
                           {vastaustyyppi: 'monivalinta'}]});
  });

  it('ei kopioi asteikko-tyyppisiä kysymyksiä', function(){
    $httpBackend.whenGET(/api\/kysymysryhma\/1234.*/)
                .respond(200, {kysymysryhmaid: 1234,
                               kysymykset: [{vastaustyyppi: 'kylla_ei_valinta'},
                                            {vastaustyyppi: 'monivalinta'}]});
    alustaControllerKopioimaan(1234);
    $httpBackend.flush();
    expect($scope.kysymysryhma)
    .toEqual({kysymykset: [{vastaustyyppi: 'kylla_ei_valinta'},
                           {vastaustyyppi: 'monivalinta'}]});
  });

  it('ei näytä varoitusta, jos kaikki kysymykset ovat kopioitavissa', function(){
    $httpBackend.whenGET(/api\/kysymysryhma\/1234.*/)
                .respond(200, {kysymysryhmaid: 1234,
                               kysymykset: [{vastaustyyppi: 'kylla_ei_valinta'},
                                            {vastaustyyppi: 'likert_asteikko'},
                                            {vastaustyyppi: 'monivalinta'}]});
    alustaControllerKopioimaan(1234);
    $httpBackend.flush();
    expect(ilmoitus.varoitus).not.toHaveBeenCalled();
  });

  it('näyttää varoituksen, jos jokin kysymys ei ole kopioitavissa', function(){
    $httpBackend.whenGET(/api\/kysymysryhma\/1234.*/)
                .respond(200, {kysymysryhmaid: 1234,
                               kysymykset: [{vastaustyyppi: 'kylla_ei_valinta'},
                                            {vastaustyyppi: 'monivalinta'}]});
    alustaControllerKopioimaan(1234);
    $httpBackend.flush();
    expect(ilmoitus.varoitus).toHaveBeenCalled();
  });

  it('ei näytä varoitusta, jos kaikki jatkokysymykset ovat kopioitavissa', function(){
    $httpBackend
    .whenGET(/api\/kysymysryhma\/1234.*/)
    .respond(200, {kysymysryhmaid: 1234,
                   kysymykset: [{vastaustyyppi: 'kylla_ei_valinta',
                                 jatkokysymys:
                                 {kylla_kysymys_fi: 'k1',
                                  kylla_vastaustyyppi: 'likert_asteikko'}},
                                {vastaustyyppi: 'kylla_ei_valinta',
                                 jatkokysymys:
                                 {ei_kysymys_fi: 'k4'}}]});
    alustaControllerKopioimaan(1234);
    $httpBackend.flush();
    expect(ilmoitus.varoitus).not.toHaveBeenCalled();
  });
});
