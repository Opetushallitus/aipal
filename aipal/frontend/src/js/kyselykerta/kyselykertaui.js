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

angular.module('kyselykerta.kyselykertaui', ['yhteiset.palvelut.i18n', 'ngRoute', 'rest.tutkinto', 'rest.koulutustoimija', 'rest.kieli',
                                             'rest.rahoitusmuoto', 'rest.vastaajatunnus', 'rest.kyselykerta', 'rest.kysely',
                                             'rest.oppilaitos', 'yhteiset.palvelut.tallennusMuistutus', 'yhteiset.palvelut.ilmoitus',
                                             'yhteiset.palvelut.varmistus'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/kyselyt/:kyselyid/kyselykerta/uusi', {
        controller: 'KyselykertaController',
        templateUrl: 'template/kyselykerta/kyselykerta.html',
        label: 'i18n.kysely.breadcrumb_uusi_kyselykerta',
        resolve: {
          uusi: function() { return true; }
        }
      })
      .when('/kyselyt/:kyselyid/kyselykerta/:kyselykertaid', {
        controller: 'KyselykertaController',
        templateUrl: 'template/kyselykerta/kyselykerta.html',
        label: 'i18n.kysely.breadcrumb_kyselykerta',
        resolve: {
          uusi: function() { return false; }
        }
      });
  }])

  .controller('KyselykertaController', ['Kyselykerta', 'Kysely', 'Kieli', 'Rahoitusmuoto', 'Tutkinto', 'Vastaajatunnus', 'Koulutustoimija', 'tallennusMuistutus', '$location', '$uibModal', '$routeParams', '$scope', 'ilmoitus', 'i18n', 'uusi', 'varmistus', 'pvm',
    function(Kyselykerta, Kysely, Kieli, Rahoitusmuoto, Tutkinto, Vastaajatunnus, Koulutustoimija, tallennusMuistutus, $location, $uibModal, $routeParams, $scope, ilmoitus, i18n, uusi, varmistus, pvm) {
      $scope.muokkaustila = true;
      $scope.$watch('kyselykertaForm', function(form) {
        // watch tarvitaan koska form asetetaan vasta controllerin jälkeen
        tallennusMuistutus.muistutaTallennuksestaPoistuttaessaFormilta(form);
      });
      $scope.luoTunnuksiaDialogi = function() {
        var kyselykertaId = $routeParams.kyselykertaid;

        var modalInstance = $uibModal.open({
          templateUrl: 'template/kysely/tunnusten-luonti.html',
          controller: 'LuoTunnuksiaModalController',
          resolve: {
            kielet: function() {
              return $scope.kielet;
            },
            rahoitusmuodot: function() {
              return $scope.rahoitusmuodot;
            },
            tutkinnot: function() {
              return $scope.tutkinnot;
            },
            koulutustoimijat: function() {
              return $scope.koulutustoimijat;
            },
            aktiivinenKoulutustoimija: function() {
              return $scope.aktiivinenKoulutustoimija;
            },
            viimeksiValittuTutkinto: function() {
              return Vastaajatunnus.haeViimeisinTutkinto($routeParams.kyselykertaid).then(function(response) { return response.data; });
            },
            kyselykerta: function () {
              return $scope.kyselykerta;
            }
          }
        });

        modalInstance.result.then(function(vastaajatunnus) {
          Vastaajatunnus.luoUusia(kyselykertaId, vastaajatunnus).success(function(uudetTunnukset) {
            _.forEach(uudetTunnukset, function(tunnus) {
              tunnus.new = true;
              $scope.tunnukset.unshift(tunnus);
            });
            ilmoitus.onnistuminen(i18n.hae('vastaajatunnus.tallennus_onnistui'));
          }).error(function() {
            ilmoitus.virhe(i18n.hae('vastaajatunnus.tallennus_epaonnistui'));
          });
        });
      };

      $scope.uusi = uusi;
      $scope.kyselykertaid = $routeParams.kyselykertaid;
      $scope.rahoitusmuodot = Rahoitusmuoto.haeKaikki(function(data) {
        $scope.rahoitusmuodotmap = _.indexBy(data, 'rahoitusmuotoid');
      });

      Kieli.haeKaikki().success(function(kielet) {
        $scope.kielet = _.pluck(kielet, 'kieli');
      });

      Tutkinto.koulutustoimijanTutkinnot().success(function(tutkinnot) {
        $scope.tutkinnot = tutkinnot;
      });

      Koulutustoimija.haeKaikki().success(function(koulutustoimijat) {
        $scope.koulutustoimijat = koulutustoimijat;
      });

      Koulutustoimija.haeAktiivinen().success(function(koulutustoimija) {
        $scope.aktiivinenKoulutustoimija = koulutustoimija;
      });

      Kysely.haeId($routeParams.kyselyid).success(function(kysely) {
        $scope.kysely = pvm.parsePvm(kysely);
        if(!kysely.kaytettavissa) { $scope.muokkaustila = false; }
      }).error(function() {
        $location.url('/');
      });

      if (!$scope.uusi) {
        Vastaajatunnus.hae($routeParams.kyselykertaid)
          .success(function(tunnukset) {
            $scope.tunnukset = tunnukset;
          });
        Kyselykerta.haeYksi($scope.kyselykertaid)
          .success(function(kyselykerta) {
            $scope.kyselykerta = pvm.parsePvm(kyselykerta);
            if (kyselykerta.lukittu) { $scope.muokkaustila = false; }
          })
          .error(function() {
            $location.url('/');
          });
      }
      else {
        $scope.tunnukset = [];
        $scope.kyselykerta = {};
      }

      $scope.getVastaustenLkm = function(rahoitusmuotoid){
        if(!rahoitusmuotoid) {
          return _($scope.tunnukset).map('vastausten_lkm').reduce(function (sum, num) {return sum + num;});
        }
        return _($scope.tunnukset).filter({ 'rahoitusmuotoid': rahoitusmuotoid }).map('vastausten_lkm').reduce(function(sum, num) {return sum + num;});
      };
      $scope.getVastaajienLkm = function(rahoitusmuotoid){
        if(!rahoitusmuotoid) {
          return _($scope.tunnukset).map('vastaajien_lkm').reduce(function (sum, num) {return sum + num;});
        }
        return _($scope.tunnukset).filter({ 'rahoitusmuotoid': rahoitusmuotoid }).map('vastaajien_lkm').reduce(function(sum, num) {return sum + num;});
      };
      $scope.getVastausProsentti = function(rahoitusmuotoid){
        var prosentti;
        if(!rahoitusmuotoid) {
          prosentti = ($scope.getVastaustenLkm() / $scope.getVastaajienLkm()) * 100;
        }
        prosentti = ($scope.getVastaustenLkm(rahoitusmuotoid) / $scope.getVastaajienLkm(rahoitusmuotoid))*100;
        return Math.min(100, prosentti);
      };

      $scope.tallenna = function() {
        if ($scope.uusi) {
          Kyselykerta.luoUusi(parseInt($routeParams.kyselyid, 10), $scope.kyselykerta)
            .success(function(kyselykerta) {
              $scope.kyselykertaForm.$setPristine();
              $location.url('/kyselyt/' + $routeParams.kyselyid + '/kyselykerta/' + kyselykerta.kyselykertaid);
            })
            .error(function(virhe) {
              ilmoitus.virhe(i18n.hae(virhe), i18n.hae('kyselykerta.tallennus_epaonnistui'));
            });
        } else {
          Kyselykerta.tallenna($scope.kyselykertaid, $scope.kyselykerta)
            .success(function() {
              $scope.kyselykertaForm.$setPristine();
              ilmoitus.onnistuminen(i18n.hae('kyselykerta.tallennus_onnistui'));
            })
            .error(function(virhe) {
              ilmoitus.virhe(i18n.hae(virhe), i18n.hae('kyselykerta.tallennus_epaonnistui'));
            });
        }
      };

      $scope.muokkaaVastaajienMaaraa = function(tunnus) {
        var modalInstance = $uibModal.open({
          templateUrl: 'template/kyselykerta/muokkaa-vastaajia.html',
          controller: 'MuokkaaVastaajiaModalController',
          resolve: {
            tunnus: function() { return tunnus; }
          }
        });

        modalInstance.result.then(function(vastaajien_lkm) {
          Vastaajatunnus.muokkaaVastaajienLukumaaraa($scope.kyselykertaid, tunnus.vastaajatunnusid, vastaajien_lkm).success(function() {
            tunnus.vastaajien_lkm = vastaajien_lkm;
          });
        });
      };

      $scope.poistaTunnus = function(tunnus) {
        varmistus.varmista(i18n.hae('vastaajatunnus.poista_otsikko'), null, i18n.hae('vastaajatunnus.poista_teksti'), i18n.hae('yleiset.poista')).then(function() {
          Vastaajatunnus.poista($scope.kyselykertaid, tunnus.vastaajatunnusid).success(function() {
            $scope.tunnukset = _.reject($scope.tunnukset, function(t) { return t.vastaajatunnusid === tunnus.vastaajatunnusid; });
          });
        });
      };

      $scope.lukitseTunnus = function(tunnus, lukitse) {
        Vastaajatunnus.lukitse($routeParams.kyselykertaid, tunnus.vastaajatunnusid, lukitse).success(function(uusiTunnus) {
          _.assign(tunnus, uusiTunnus);
        });
      };
    }]
  )

  .controller('LuoTunnuksiaModalController', ['$uibModalInstance', '$scope', '$filter', 'Oppilaitos', 'kielet', 'rahoitusmuodot', 'tutkinnot', 'koulutustoimijat', 'kyselykerta', 'aktiivinenKoulutustoimija', 'viimeksiValittuTutkinto',
                                              function($uibModalInstance, $scope, $filter, Oppilaitos, kielet, rahoitusmuodot, tutkinnot, koulutustoimijat, kyselykerta, aktiivinenKoulutustoimija, viimeksiValittuTutkinto) {
    $scope.vastaajatunnus = {
      henkilokohtainen: true,
      koulutuksen_jarjestaja: aktiivinenKoulutustoimija,
      rahoitusmuotoid: 5,
      suorituskieli: 'fi',
      tutkinto: viimeksiValittuTutkinto,
      vastaajien_lkm: 1
    };
    $scope.kielet = kielet;
    $scope.rahoitusmuodot = rahoitusmuodot;
    $scope.rahoitusmuodotmap = _.indexBy(rahoitusmuodot, 'rahoitusmuotoid');
    $scope.kyselykerta = kyselykerta;
    var tanaan = new Date();
    tanaan.setUTCHours(0,0,0,0);
    var alkupvm = new Date(kyselykerta.voimassa_alkupvm),
      loppupvm = kyselykerta.voimassa_loppupvm ? new Date(kyselykerta.voimassa_loppupvm) : alkupvm;
    $scope.menneisyydessa =  !_.isNull(kyselykerta.voimassa_loppupvm) && loppupvm < tanaan;
    var oletusalkupvm = alkupvm > tanaan ? alkupvm : ($scope.menneisyydessa ? loppupvm : tanaan);
    $scope.oletusalkupvm = oletusalkupvm;

    $scope.tutkinnot = tutkinnot;

    $scope.koulutustoimijat = koulutustoimijat;
    $scope.naytaLisaa = function() {
      $scope.rullausrajoite += 5;
    };
    $scope.nollaaRajoite = function() {
      $scope.rullausrajoite = 20;
    };
    $scope.nollaaRajoite();

    function haeOppilaitokset(koulutustoimija) {
      Oppilaitos.haeKoulutustoimijanOppilaitokset(koulutustoimija).success(function(oppilaitokset) {
        $scope.oppilaitokset = oppilaitokset;
        $scope.vastaajatunnus.koulutuksen_jarjestaja_oppilaitos = null;
      });
    }
    haeOppilaitokset(aktiivinenKoulutustoimija.ytunnus);
    $scope.$watch('vastaajatunnus.koulutuksen_jarjestaja', function(koulutustoimija) {
      haeOppilaitokset(koulutustoimija.ytunnus);
    });

    $scope.luoTunnuksia = function(vastaajatunnus) {
      $uibModalInstance.close(vastaajatunnus);
    };
    $scope.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };
    $scope.lokalisoiNimi = function(tutkinto) {
      if(typeof tutkinto === 'object') {
        return $filter('lokalisoiKentta')(tutkinto, 'nimi');
      } else {
        return tutkinto;
      }
    };

    $scope.tutkintoPakollinen = function() {
      var rahoitusmuotoid = $scope.vastaajatunnus.rahoitusmuotoid;
      return $scope.tutkinnot.length > 0 && (rahoitusmuotoid === undefined || $scope.rahoitusmuodotmap[rahoitusmuotoid].rahoitusmuoto !== 'ei_rahoitusmuotoa');
    };
  }])

  .controller('MuokkaaVastaajiaModalController', ['$uibModalInstance', '$scope', 'i18n', 'tunnus', function($uibModalInstance, $scope, i18n, tunnus) {
    $scope.i18n = i18n;

    $scope.minimi = Math.max(1, tunnus.vastausten_lkm);
    $scope.vastausten_lkm = tunnus.vastausten_lkm;
    $scope.vastaajien_lkm = tunnus.vastaajien_lkm;

    $scope.save = function() {
      $uibModalInstance.close(parseInt($scope.vastaajien_lkm));
    };

    $scope.cancel = $uibModalInstance.dismiss;
  }])
;
