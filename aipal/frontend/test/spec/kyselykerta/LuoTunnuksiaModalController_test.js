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

describe('kysely.kyselyui.LuoTunnuksiaModalController', function(){

  var $scope;
  var $httpBackend;
  var $controller;
  var kyselykerta;

  beforeEach(module('ui.bootstrap','kyselykerta.kyselykertaui'));

  beforeEach(module('ui.bootstrap.modal'));
  beforeEach(module(function($provide){
    $provide.value('$uibModalInstance', {});
    $provide.value('kielet', {});
    $provide.value('tutkinnot', {});
    $provide.value('koulutustoimijat', {});
    $provide.value('aktiivinenKoulutustoimija', {});
    $provide.value('viimeksiValittuTutkinto', {});
    kyselykerta = {};
    $provide.value('kyselykerta', kyselykerta);
  }));

  beforeEach(function() {
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  function alustaController() {
    $scope.kyselykertaForm = { $setDirty: function() {}, $setPristine: function() {} };
    $controller('LuoTunnuksiaModalController', {$scope: $scope});
  }

  describe('nykyhetki = 15.1.2015', function() {
    var today = new Date('2015-01-15');

    beforeEach(module(function(){
      jasmine.clock().mockDate(today);
    }));

    beforeEach(inject(function($rootScope, _$httpBackend_, _$controller_){
      $scope = $rootScope.$new();
      $httpBackend = _$httpBackend_;
      $controller = _$controller_;
    }));

    it('pitäisi antaa oletusalkupäiväksi nykyhetki, kun kyselykerta voimassa', function() {
      kyselykerta.voimassa_alkupvm = '2015-01-01';
      kyselykerta.voimassa_loppupvm = '2015-02-01';
      alustaController();
      expect($scope.menneisyydessa).toEqual(false);
      expect(new Date($scope.oletusalkupvm)).toEqual(today);
    });

    it('pitäisi antaa oletusalkupäiväksi nykyhetki, kun kyselykerta viimeistä päivää voimassa', function() {
      kyselykerta.voimassa_alkupvm = '2015-01-01';
      kyselykerta.voimassa_loppupvm = '2015-01-15';
      alustaController();
      expect($scope.menneisyydessa).toEqual(false);
      expect(new Date($scope.oletusalkupvm)).toEqual(today);
    });

    it('pitäisi antaa oletusalkupäiväksi kyselykerran loppupäivä, kun kyselykerta päättynyt', function() {
      kyselykerta.voimassa_alkupvm = '2015-01-01';
      kyselykerta.voimassa_loppupvm = '2015-01-14';
      var loppupvm = new Date(kyselykerta.voimassa_loppupvm);
      alustaController();
      expect($scope.menneisyydessa).toEqual(true);
      expect(new Date($scope.oletusalkupvm)).toEqual(loppupvm);
    });

    it('pitäisi antaa oletusalkupäiväksi nykyhetki, kun kyselykerta voimassa toistaiseksi', function() {
      kyselykerta.voimassa_alkupvm = '2015-01-01';
      kyselykerta.voimassa_loppupvm = null;
      alustaController();
      expect($scope.menneisyydessa).toEqual(false);
      expect(new Date($scope.oletusalkupvm)).toEqual(today);
    });

    it('pitäisi antaa oletusalkupäiväksi kyselykerran alkupäivä, kun kyselykerta tulevaisuudessa', function() {
      kyselykerta.voimassa_alkupvm = '2015-02-01';
      kyselykerta.voimassa_loppupvm = '2015-02-15';
      var alkupvm = new Date(kyselykerta.voimassa_alkupvm);
      alustaController();
      expect($scope.menneisyydessa).toEqual(false);
      expect(new Date($scope.oletusalkupvm)).toEqual(alkupvm);
    });

    it('pitäisi antaa oletusalkupäiväksi kyselykerran alkupäivä, kun kyselykerta tulevaisuudessa ja avoimella loppupäivällä', function() {
      kyselykerta.voimassa_alkupvm = '2015-02-01';
      kyselykerta.voimassa_loppupvm = null;
      var alkupvm = new Date(kyselykerta.voimassa_alkupvm);
      alustaController();
      expect($scope.menneisyydessa).toEqual(false);
      expect(new Date($scope.oletusalkupvm)).toEqual(alkupvm);
    });

    it('pitäisi antaa oletusalkupäiväksi nykyhetki, kun kyselykerta tulee voimaan samana päivänä', function() {
      kyselykerta.voimassa_alkupvm = today;
      kyselykerta.voimassa_loppupvm = '2015-01-31';
      alustaController();
      expect($scope.menneisyydessa).toEqual(false);
      expect(new Date($scope.oletusalkupvm)).toEqual(today);
    });

    it('pitäisi antaa oletusalkupäiväksi nykyhetki, kun kyselykerta tulee voimaan samana päivänä ja on avoimella loppupäivällä', function() {
      kyselykerta.voimassa_alkupvm = today;
      kyselykerta.voimassa_loppupvm = null;
      alustaController();
      expect($scope.menneisyydessa).toEqual(false);
      expect(new Date($scope.oletusalkupvm)).toEqual(today);
    });
  });
});
