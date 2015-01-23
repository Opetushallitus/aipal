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

angular.module('yhteiset.direktiivit.kyselylista',
    ['yhteiset.palvelut.i18n',
     'yhteiset.palvelut.ilmoitus',
     'rest.kysely',
     'rest.kyselykerta'])

  .controller('KyselylistaController', ['$filter', '$scope', '$location', 'Kysely', 'Kyselykerta', 'ilmoitus', 'i18n', 'varmistus', function($filter, $scope, $location, Kysely, Kyselykerta, ilmoitus, i18n, varmistus) {
    $scope.julkaiseKyselyModal = function(kysely) {
      varmistus.varmista(i18n.hae('kysely.julkaise'), $filter('lokalisoiKentta')(kysely, 'nimi'), i18n.hae('kysely.julkaise_ohjeistus'), i18n.hae('kysely.julkaise')).then(function() {
        Kysely.julkaise(kysely.kyselyid)
          .success(function(response) {
            _.assign(kysely, response);
            ilmoitus.onnistuminen(i18n.hae('kysely.julkaisu_onnistui'));
          })
          .error(function() {
            ilmoitus.virhe(i18n.hae('kysely.julkaisu_epaonnistui'));
          });
      });
    };

    $scope.poistaKyselyModal = function(kyselylista, kysely) {
      varmistus.varmista(i18n.hae('kysely.poista'), $filter('lokalisoiKentta')(kysely, 'nimi'), i18n.hae('kysely.poista_ohjeistus'), i18n.hae('kysely.poista')).then(function() {
        var kyselyid = kysely.kyselyid;
        var kyselyindex = _.findIndex(kyselylista, {kyselyid: kyselyid});
        Kysely.poista(kyselyid)
          .success(function() {
            kyselylista.splice(kyselyindex, 1);
            ilmoitus.onnistuminen(i18n.hae('kysely.poisto_onnistui'));
          })
          .error(function() {
            ilmoitus.virhe(i18n.hae('kysely.poisto_epaonnistui'));
          });
      });
    };

    $scope.uusiKyselykerta = function (kysely) {
      $location.url('/kyselyt/' + kysely.kyselyid + '/kyselykerta/uusi');
    };

    $scope.poistaKyselykerta = function(kyselykerta) {
      var id = kyselykerta.kyselykertaid;
      Kyselykerta.poista(id)
      .success(function(){
        _.map($scope.kyselyt, function(kysely){
          _.remove(kysely.kyselykerrat, function(kyselykerta){
            return kyselykerta.kyselykertaid === id;
          });
        });
        ilmoitus.onnistuminen(i18n.hae('kyselykerta.poistaminen_onnistui'));
      })
      .error(function(){
        ilmoitus.virhe(i18n.hae('kyselykerta.poistaminen_epaonnistui'));
      });
    };

    $scope.suljeKyselyModal = function(kysely) {
      varmistus.varmista(i18n.hae('kysely.sulje'), $filter('lokalisoiKentta')(kysely, 'nimi'), i18n.hae('kysely.sulje_ohjeistus'), i18n.hae('kysely.sulje')).then(function() {
        Kysely.sulje(kysely.kyselyid).success(function(uusiKysely) {
          _.assign(kysely, uusiKysely);
          ilmoitus.onnistuminen(i18n.hae('kysely.sulkeminen_onnistui'));
        });
      });
    };

    $scope.palautaKysely = function(kysely) {
      Kysely.palauta(kysely.kyselyid).success(function(uusiKysely) {
        _.assign(kysely, uusiKysely);
        ilmoitus.onnistuminen(i18n.hae('kysely.palautus_onnistui'));
      });
    };
    $scope.palautaLuonnokseksi = function(kysely) {
      Kysely.palautaLuonnokseksi(kysely.kyselyid).success(function(uusiKysely) {
        _.assign(kysely, uusiKysely);
        ilmoitus.onnistuminen(i18n.hae('kysely.palautus_onnistui'));
      });
    };
    $scope.lukitseKyselykerta = function(kyselykerta) {
      varmistus.varmista(i18n.hae('kyselykerta.lukitse'), kyselykerta.nimi, i18n.hae('kyselykerta.lukitse_teksti'), i18n.hae('kyselykerta.lukitse')).then(function() {
        Kyselykerta.lukitse(kyselykerta.kyselykertaid)
          .success(function(uusiKyselykerta) {
            _.assign(kyselykerta, uusiKyselykerta);
            ilmoitus.onnistuminen(i18n.hae('kyselykerta.lukitseminen_onnistui'));
          });
      });
    };
    $scope.avaaKyselykerta = function(kyselykerta) {
      Kyselykerta.avaa(kyselykerta.kyselykertaid)
        .success(function(uusiKyselykerta) {
          _.assign(kyselykerta, uusiKyselykerta);
        });
    };

    $scope.kyselykertojen_lajitteluperuste = 'luotuaika';
  }])

  .directive('kyselylista', [function() {
    return {
      restrict: 'E',
      replace: true,
      scope : {
        kyselyt: '=',
        suodatus: '=',
        haku: '='
      },
      templateUrl : 'template/yhteiset/direktiivit/kyselylista.html',
      controller: 'KyselylistaController'
    };
  }])
;
