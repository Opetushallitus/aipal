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

  .controller('KyselylistaController', ['$filter', '$scope', '$location', '$sce', 'Kysely', 'Kyselykerta', 'ilmoitus', 'i18n', 'varmistus', 'kayttooikeudet', function($filter, $scope, $location, $sce, Kysely, Kyselykerta, ilmoitus, i18n, varmistus, kayttooikeudet) {

    $scope.getVastausBaseUrl = function(){
      return $sce.trustAsResourceUrl(vastausBaseUrl);
    };

    $scope.julkaiseKyselyModal = function(kysely) {
      varmistus.varmista(i18n.hae('kysely.julkaise'), $filter('lokalisoiKentta')(kysely, 'nimi'), i18n.hae('kysely.julkaise_ohjeistus'), i18n.hae('kysely.julkaise')).then(function() {
        Kysely.julkaise(kysely.kyselyid)
          .then(function(resp) {
            if (!resp.data) {
              console.error('resp.data missing');
            }
            _.assign(kysely, resp.data);
            ilmoitus.onnistuminen(i18n.hae('kysely.julkaisu_onnistui'));
          })
          .catch(function() {
            ilmoitus.virhe(i18n.hae('kysely.julkaisu_epaonnistui'));
          });
      }).catch(function (e) {
        console.error(e);
      });
    };

    $scope.poistaKyselyModal = function(kyselylista, kysely) {
      varmistus.varmista(i18n.hae('kysely.poista'), $filter('lokalisoiKentta')(kysely, 'nimi'), i18n.hae('kysely.poista_ohjeistus'), i18n.hae('kysely.poista')).then(function() {
        var kyselyid = kysely.kyselyid;
        var kyselyindex = _.findIndex(kyselylista, {kyselyid: kyselyid});
        Kysely.poista(kyselyid)
          .then(function() {
            kyselylista.splice(kyselyindex, 1);
            ilmoitus.onnistuminen(i18n.hae('kysely.poisto_onnistui'));
          })
          .catch(function() {
            ilmoitus.virhe(i18n.hae('kysely.poisto_epaonnistui'));
          });
      }).catch(function (e) {
        console.error(e);
      });
    };

    $scope.uusiKyselykerta = function (kysely) {
      $location.url('/kyselyt/' + kysely.kyselyid + '/kyselykerta/uusi');
    };

    $scope.poistaKyselykerta = function(kyselykerta) {
      varmistus.varmista(i18n.hae('kyselykerta.poista'), kyselykerta.nimi, i18n.hae('kyselykerta.poista_ohjeistus'), i18n.hae('kyselykerta.poista')).then(function() {
        var id = kyselykerta.kyselykertaid;
        Kyselykerta.poista(id)
        .then(function(){
          _.forEach($scope.kyselyt, function(kysely){
            _.remove(kysely.kyselykerrat, {kyselykertaid: id});
          });
          ilmoitus.onnistuminen(i18n.hae('kyselykerta.poistaminen_onnistui'));
        })
        .catch(function(){
          ilmoitus.virhe(i18n.hae('kyselykerta.poistaminen_epaonnistui'));
        });
      }).catch(function (e) {
        console.error(e);
      });
    };

    $scope.suljeKyselyModal = function(kysely) {
      varmistus.varmista(i18n.hae('kysely.sulje'), $filter('lokalisoiKentta')(kysely, 'nimi'), i18n.hae('kysely.sulje_ohjeistus'), i18n.hae('kysely.sulje')).then(function() {
        Kysely.sulje(kysely.kyselyid).then(function(resp) {
          if (!resp.data) {
            console.error('resp.data missing');
          }
          const uusiKysely = resp.data;
          _.assign(kysely, uusiKysely);
          ilmoitus.onnistuminen(i18n.hae('kysely.sulkeminen_onnistui'));
        }).catch(function (e) {
          console.error(e);
        });
      }).catch(function (e) {
        console.error(e);
      });
    };

    $scope.palautaKysely = function(kysely) {
      Kysely.palauta(kysely.kyselyid).then(function(resp) {
        if (!resp.data) {
          console.error('resp.data missing');
        }
        const uusiKysely = resp.data;
        _.assign(kysely, uusiKysely);
        ilmoitus.onnistuminen(i18n.hae('kysely.palautus_onnistui'));
      }).catch(function (e) {
        console.error(e);
      });
    };
    $scope.palautaLuonnokseksi = function(kysely) {
      Kysely.palautaLuonnokseksi(kysely.kyselyid).then(function(resp) {
        if (!resp.data) {
          console.error('resp.data missing');
        }
        const uusiKysely = resp.data;
        _.assign(kysely, uusiKysely);
        ilmoitus.onnistuminen(i18n.hae('kysely.palautus_onnistui'));
      }).catch(function (e) {
        console.error(e);
      });
    };
    $scope.lukitseKyselykerta = function(kyselykerta) {
      varmistus.varmista(i18n.hae('kyselykerta.lukitse'), kyselykerta.nimi, i18n.hae('kyselykerta.lukitse_teksti'), i18n.hae('kyselykerta.lukitse')).then(function() {
        Kyselykerta.lukitse(kyselykerta.kyselykertaid, true)
          .then(function(resp) {
            if (!resp.data) {
              console.error('resp.data missing');
            }
            const uusiKyselykerta = resp.data;
            _.assign(kyselykerta, uusiKyselykerta);
            ilmoitus.onnistuminen(i18n.hae('kyselykerta.lukitseminen_onnistui'));
          }).catch(function (e) {
          console.error(e);
        });
      }).catch(function (e) {
        console.error(e);
      });
    };
    $scope.avaaKyselykerta = function(kyselykerta) {
      Kyselykerta.lukitse(kyselykerta.kyselykertaid, false)
        .then(function(resp) {
          if (!resp.data) {
            console.error('resp.data missing');
          }
          const uusiKyselykerta = resp.data;
          _.assign(kyselykerta, uusiKyselykerta);
        }).catch(function (e) {
        console.error(e);
      });
    };

    $scope.vastuuKayttaja = kayttooikeudet.isVastuuKayttaja();

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
