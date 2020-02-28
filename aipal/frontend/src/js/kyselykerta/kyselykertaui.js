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

angular.module('kyselykerta.kyselykertaui', ['yhteiset.palvelut.i18n', 'ui.bootstrap','ngRoute', 'rest.tutkinto', 'rest.koulutustoimija', 'rest.kieli',
                                             'rest.vastaajatunnus', 'rest.kyselykerta', 'rest.kysely',
                                             'rest.oppilaitos', 'yhteiset.palvelut.tallennusMuistutus', 'yhteiset.palvelut.ilmoitus',
                                             'yhteiset.palvelut.kayttooikeudet', 'yhteiset.palvelut.varmistus', 'yhteiset.suodattimet.tutkinto'])

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

  .controller('KyselykertaController', ['Kyselykerta', 'Kysely', 'Kieli', 'Tutkinto', 'Vastaajatunnus', 'Koulutustoimija', 'tallennusMuistutus', '$location', '$uibModal', '$routeParams', '$scope', 'ilmoitus', 'i18n', 'uusi', 'varmistus', 'pvm','kayttooikeudet', 'kieli',
    function (Kyselykerta, Kysely, Kieli, Tutkinto, Vastaajatunnus, Koulutustoimija, tallennusMuistutus, $location, $uibModal, $routeParams, $scope, ilmoitus, i18n, uusi, varmistus, pvm, kayttooikeudet, kieli) {
      $scope.muokkaustila = true;

      function setMuokkaustila(muokkaustila){
        $scope.muokkaustila = kayttooikeudet.isYllapitaja() || muokkaustila;
      }

      $scope.$watch('kyselykertaForm', function (form) {
        // watch tarvitaan koska form asetetaan vasta controllerin jälkeen
        tallennusMuistutus.muistutaTallennuksestaPoistuttaessaFormilta(form);
      });

      $scope.kieli = kieli
      $scope.laajennettuOppisopimuskoulutus = kayttooikeudet.laajennettuOppisopimuskoulutus();

      const templateMap = {
        'avop': 'template/kysely/palautekysely-tunnukset.html',
        'rekrykysely': 'template/kysely/rekrykysely-tunnukset.html',
        'yo-uraseuranta': 'template/kysely/uraseuranta-tunnukset.html',
        'itsearviointi': 'template/kysely/digikyvykkyys-tunnukset.html',
        'amispalaute': 'template/kysely/amis-tunnukset.html',
        'kandipalaute': 'template/kysely/palautekysely-tunnukset.html',
        'amk-uraseuranta': 'template/kysely/uraseuranta-tunnukset.html'
      }

      $scope.luoTunnuksiaDialogi = function (laajennettu) {
        var kyselykertaId = $routeParams.kyselykertaid;

        var templateUrl = laajennettu ? 'template/kysely/amis-laajennettu.html' : templateMap[$scope.kysely.tyyppi]
        var template = {templateUrl: templateUrl, controller: 'LuoTunnuksiaModalController'}

        var modalInstance = $uibModal.open({
          templateUrl: template.templateUrl,
          controller: template.controller,
          resolve: {
            kielet: function () {
              return $scope.kielet;
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
            },
            kyselytyyppi: function () {
              return $scope.kysely.tyyppi;
            },
            laajennettu: function () {
              return laajennettu;
            }
          }
        });

        modalInstance.result.then(function(vastaajatunnus) {
          Vastaajatunnus.luoUusia(kyselykertaId, vastaajatunnus).success(function(uudetTunnukset) {
            _.forEach(uudetTunnukset, function(tunnus) {
              tunnus.new = true;
              tunnus.vastauksia = 0;
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

      Kieli.haeKaikki().success(function(kielet) {
        $scope.kielet = _.pluck(kielet, 'kieli');
      });

      function haeTutkinnot(kysely){
        Tutkinto.koulutustoimijanTutkinnot(kysely.tyyppi, false).success(function(tutkinnot) {
          $scope.tutkinnot = tutkinnot;
        });
      }

      $scope.vain_omat = false;

      function haeTunnukset (){
        Vastaajatunnus.hae($routeParams.kyselykertaid, $scope.vain_omat)
          .success(function(tunnukset) {
            $scope.tunnukset = tunnukset;
          });
      }

      $scope.toggleOmat = function(){
        $scope.vain_omat = !$scope.vain_omat;
        haeTunnukset();
      }

      Koulutustoimija.haeKaikki().success(function(koulutustoimijat) {
        $scope.koulutustoimijat = koulutustoimijat;
      });

      Koulutustoimija.haeAktiivinen().success(function(koulutustoimija) {
        $scope.aktiivinenKoulutustoimija = koulutustoimija;
      });

      Kysely.haeId($routeParams.kyselyid).success(function(kysely) {
        $scope.kysely = pvm.parsePvm(kysely);

        if(!kysely.kaytettavissa || kysely.automatisoitu) { setMuokkaustila(false); }
        haeTutkinnot(kysely)
        $scope.vain_omat = kysely.tyyppi === 'amispalaute'
        if(!$scope.uusi){
          haeTunnukset();
        }
      }).error(function() {
        $location.url('/');
      });

      if (!$scope.uusi) {
        Kyselykerta.haeYksi($scope.kyselykertaid)
          .success(function(kyselykerta) {
            $scope.kyselykerta = pvm.parsePvm(kyselykerta);
            if (kyselykerta.lukittu) { setMuokkaustila(false); }
          })
          .error(function() {
            $location.url('/');
          });
      }
      else {
        $scope.tunnukset = [];
        $scope.kyselykerta = {};
      }

      $scope.getVastaustenLkm = function(){
        return _($scope.tunnukset).map('vastausten_lkm').reduce(function (sum, num) {return sum + num;});
      };

      $scope.getVastaajienLkm = function(){
        return _($scope.tunnukset).map('kohteiden_lkm').reduce(function (sum, num) {return sum + num;});
      };

      $scope.getVastausProsentti = function(){
        return Math.min(100, ($scope.getVastaustenLkm() / $scope.getVastaajienLkm()) * 100);
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

        modalInstance.result.then(function(kohteiden_lkm) {
          Vastaajatunnus.muokkaaVastaajienLukumaaraa($scope.kyselykertaid, tunnus.vastaajatunnusid, kohteiden_lkm).success(function() {
            tunnus.kohteiden_lkm = kohteiden_lkm;
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

  .controller('LuoTunnuksiaModalController', ['$uibModalInstance', '$scope', '$filter', 'Oppilaitos', 'kielet',
    'tutkinnot', 'koulutustoimijat', 'kyselykerta', 'aktiivinenKoulutustoimija',
    'viimeksiValittuTutkinto', 'kayttooikeudet', 'Tutkinto', 'kyselytyyppi', 'laajennettu', 'Koulutustoimija',
    function ($uibModalInstance, $scope, $filter, Oppilaitos, kielet, tutkinnot, koulutustoimijat,
              kyselykerta, aktiivinenKoulutustoimija, viimeksiValittuTutkinto, kayttooikeudet, Tutkinto, kyselytyyppi,
              laajennettu, Koulutustoimija) {

      $scope.tutkinnonJarjestajat = [];

      $scope.vastaajatunnus = {
        henkilokohtainen: true,
        koulutuksen_jarjestaja: aktiivinenKoulutustoimija,
        suorituskieli: 'fi',
        tutkinto: viimeksiValittuTutkinto,
        kohteiden_lkm: 1,
        koulutusmuoto: null
      };

      $scope.isYllapitaja = kayttooikeudet.isYllapitaja();

      $scope.naytaKoulutusmuoto = kyselytyyppi === "avop";

      $scope.kielet = kielet;
      $scope.kyselykerta = kyselykerta;
      var tanaan = new Date();
      tanaan.setUTCHours(0, 0, 0, 0);
      var alkupvm = new Date(kyselykerta.voimassa_alkupvm),
        loppupvm = kyselykerta.voimassa_loppupvm ? new Date(kyselykerta.voimassa_loppupvm) : alkupvm;

      $scope.haeKaikkiTutkinnot = false;

      $scope.menneisyydessa = !_.isNull(kyselykerta.voimassa_loppupvm) && loppupvm < tanaan;

      $scope.oletusalkupvm = alkupvm > tanaan ? alkupvm : ($scope.menneisyydessa ? loppupvm : tanaan);

      $scope.tutkinnot = laajennettu ? [] : tutkinnot;

      $scope.koulutustoimijat = koulutustoimijat;

      $scope.toimipaikat = [];

      $scope.naytaLisaa = function () {
        $scope.rullausrajoite += 5;
      };
      $scope.nollaaRajoite = function () {
        $scope.rullausrajoite = 20;
      };
      $scope.nollaaRajoite();

      function asetaLoppupvm () {
        var vastaajatunnusVoimassaAlkaen = $scope.vastaajatunnus.voimassa_alkupvm;
        if(kyselytyyppi === 'amispalaute' && vastaajatunnusVoimassaAlkaen){
          // Asetetaan viimeinen sallittu loppupäivämäärä seuraavan rahoituskauden alkuun tai kyselykerran loppupäivä,
          // kumpi vain on ensin. Tähän lisätään 30 päivää vastausaikaa.
          var rahoituskausiVaihtuu = new Date(tanaan.getFullYear(), 5, 30);
          if (rahoituskausiVaihtuu < tanaan) {
            rahoituskausiVaihtuu.setFullYear(tanaan.getFullYear() + 1);
          }
          var astiMaxLoppupvm = kyselykerta.voimassa_loppupvm && kyselykerta.voimassa_loppupvm < rahoituskausiVaihtuu ? new Date(kyselykerta.voimassa_loppupvm) : rahoituskausiVaihtuu;
          astiMaxLoppupvm.setDate(astiMaxLoppupvm.getDate() + 30);
          $scope.astiMaxLoppupvm = astiMaxLoppupvm;

          // Ehdotetaan käyttäjälle 30 päivää nykyhetkestä, vastaajatunnuksen voimassaolosta tai viimeisestä sallitusta päivämäärästä.
          var oletusloppupvm = (vastaajatunnusVoimassaAlkaen.getTime() > tanaan.getTime()) ? new Date(vastaajatunnusVoimassaAlkaen) : new Date(tanaan);
          oletusloppupvm.setDate(oletusloppupvm.getDate() + 30);
          var min = oletusloppupvm < $scope.astiMaxLoppupvm ? oletusloppupvm : $scope.astiMaxLoppupvm;
          $scope.oletusloppupvm = min;
          $scope.vastaajatunnus.voimassa_loppupvm = min;
        }
      }

      $scope.$watch('vastaajatunnus.voimassa_alkupvm', function(){
        asetaLoppupvm();
      });

      if(laajennettu){
        Koulutustoimija.haeKoulutusluvalliset().success(function(koulutustoimijat) {
          $scope.tutkinnonJarjestajat = koulutustoimijat;
        });
      }

      function haeOppilaitokset(koulutustoimija) {
        Oppilaitos.haeKoulutustoimijanOppilaitokset(koulutustoimija).success(function (oppilaitokset) {

          if (oppilaitokset.length === 1) {
            $scope.vastaajatunnus.koulutuksen_jarjestaja_oppilaitos = oppilaitokset[0];
          } else {
            $scope.vastaajatunnus.koulutuksen_jarjestaja_oppilaitos = null;
          }
          $scope.oppilaitokset = oppilaitokset;
        });
      }

      function haeToimipaikat(oppilaitos) {
        Oppilaitos.haeOppilaitoksenToimipaikat(oppilaitos).success(function (toimipaikat) {
          $scope.toimipaikat = toimipaikat;
        })
      }

      function haeTutkinnonJarjestajat(tutkinto) {
        Tutkinto.haeTutkinnonJarjestajat(tutkinto.tutkintotunnus).success(function (jarjestajat) {
          $scope.tutkinnonJarjestajat = jarjestajat;
        })
      }

      function haeJarjestajanTutkinnot(ytunnus, kyselytyyppi) {
        Tutkinto.haeKoulutustoimijanTutkinnot(ytunnus, kyselytyyppi).success(function (tutkinnot){
          $scope.tutkinnot = tutkinnot;
        })
      }

      function haeTutkinnot(){
        if($scope.haeKaikkiTutkinnot){
          Tutkinto.kyselytyypinTutkinnot(kyselytyyppi).success(function (tutkinnot){
            $scope.tutkinnot = tutkinnot;
          })
        } else {
          if(laajennettu){
            haeJarjestajanTutkinnot($scope.vastaajatunnus.hankintakoulutuksen_toteuttaja.ytunnus, kyselytyyppi);
          } else{
            Tutkinto.koulutustoimijanTutkinnot(kyselytyyppi).success(function (tutkinnot){
              $scope.tutkinnot = tutkinnot;
            })
          }
        }
      }

      if(viimeksiValittuTutkinto && !laajennettu){
        haeTutkinnonJarjestajat(viimeksiValittuTutkinto);
      }

      haeOppilaitokset(aktiivinenKoulutustoimija.ytunnus);
      $scope.$watch('vastaajatunnus.koulutuksen_jarjestaja', function (koulutustoimija) {
        haeOppilaitokset(koulutustoimija.ytunnus);
      });

      $scope.$watch('vastaajatunnus.koulutuksen_jarjestaja_oppilaitos', function (oppilaitos) {
        if (oppilaitos && oppilaitos.oppilaitoskoodi) {
          haeToimipaikat(oppilaitos.oppilaitoskoodi);
        }
      });

      $scope.toggleHaeKaikkiTutkinnot = function(){
        haeTutkinnot();
      }

      $scope.$watch('vastaajatunnus.tutkinto', function (tutkinto) {
        if(tutkinto && !laajennettu){
          haeTutkinnonJarjestajat(tutkinto)
        }
      });

      $scope.$watch('vastaajatunnus.hankintakoulutuksen_toteuttaja', function (koulutustoimija){
        if(koulutustoimija && laajennettu){
          haeJarjestajanTutkinnot(koulutustoimija.ytunnus, kyselytyyppi);
          $scope.vastaajatunnus.tutkinto = null;
        }
      });

      $scope.luoTunnuksia = function (vastaajatunnus) {
        $uibModalInstance.close(vastaajatunnus);
      };
      $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
      };
      $scope.lokalisoiNimi = function (tutkinto) {
        if (typeof tutkinto === 'object') {
          return $filter('lokalisoiKentta')(tutkinto, 'nimi');
        } else {
          return tutkinto;
        }
      };
    }])

  .controller('MuokkaaVastaajiaModalController', ['$uibModalInstance', '$scope', 'i18n', 'tunnus',
    function ($uibModalInstance, $scope, i18n, tunnus) {
      $scope.i18n = i18n;

      $scope.minimi = Math.max(1, tunnus.vastausten_lkm);
      $scope.vastausten_lkm = tunnus.vastausten_lkm;
      $scope.kohteiden_lkm = tunnus.kohteiden_lkm;

      $scope.save = function () {
        $uibModalInstance.close(parseInt($scope.kohteiden_lkm));
      };

      $scope.cancel = $uibModalInstance.dismiss;
    }])
;
