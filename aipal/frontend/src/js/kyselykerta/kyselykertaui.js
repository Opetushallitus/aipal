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

angular.module('kyselykerta.kyselykertaui', ['yhteiset.palvelut.i18n', 'ui.bootstrap','ngRoute', 'rest.tutkinto', 'rest.koulutustoimija',
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

  .controller('KyselykertaController', ['Kyselykerta', 'Kysely', 'Tutkinto', 'Vastaajatunnus', 'Koulutustoimija', 'tallennusMuistutus', '$location', '$uibModal', '$routeParams', '$scope', 'ilmoitus', 'i18n', 'uusi', 'varmistus', 'pvm','kayttooikeudet', 'kieli',
    function (Kyselykerta, Kysely, Tutkinto, Vastaajatunnus, Koulutustoimija, tallennusMuistutus, $location, $uibModal, $routeParams, $scope, ilmoitus, i18n, uusi, varmistus, pvm, kayttooikeudet, kieli) {
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
        'amk-uraseuranta': 'template/kysely/uraseuranta-tunnukset.html',
        'tyoelamapalaute': 'template/kysely/tyoelamapalaute-tunnukset.html',
      };

      $scope.luoTunnuksiaDialogi = function (laajennettu) {
        var kyselykertaId = $routeParams.kyselykertaid;

        var templateUrl = laajennettu ? 'template/kysely/amis-laajennettu.html' : templateMap[$scope.kysely.tyyppi];
        var template = {templateUrl: templateUrl, controller: 'LuoTunnuksiaModalController'};

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
              return Vastaajatunnus.haeViimeisinTutkinto($routeParams.kyselykertaid).then(function(resp) {
                if (!resp.data) {
                  console.error('resp.data missing');
                }
                return resp.data;
              }).catch(function (e) {
                console.error(e);
              });
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
          Vastaajatunnus.luoUusia(kyselykertaId, vastaajatunnus).then(function(resp) {
            if (!resp.data) {
              console.error('resp.data missing');
            }
            const uudetTunnukset = resp.data;
            _.forEach(uudetTunnukset, function(tunnus) {
              tunnus.new = true;
              tunnus.vastauksia = 0;
              $scope.tunnukset.unshift(tunnus);
            });
            ilmoitus.onnistuminen(i18n.hae('vastaajatunnus.tallennus_onnistui'));
          }).catch(function() {
            ilmoitus.virhe(i18n.hae('vastaajatunnus.tallennus_epaonnistui'));
          });
        }).catch(function (e) {
          console.error(e);
        });
      };

      $scope.uusi = uusi;
      $scope.kyselykertaid = $routeParams.kyselykertaid;

      $scope.kielet = ["fi", "sv", "en"]

      function haeTutkinnot(kysely){
        Tutkinto.koulutustoimijanTutkinnot(kysely.tyyppi, false).then(function(resp) {
          if (!resp.data) {
            console.error('resp.data missing');
          }
          $scope.tutkinnot = resp.data;
        }).catch(function (e) {
          console.error(e);
        });
      }

      $scope.vain_omat = false;

      function haeTunnukset (){
        Vastaajatunnus.hae($routeParams.kyselykertaid, $scope.vain_omat)
          .then(function(resp) {
            if (!resp.data) {
              console.error('resp.data missing');
            }
            $scope.tunnukset = resp.data;
          }).catch(function (e) {
          console.error(e);
        });
      }

      function haeNiput () {
        Vastaajatunnus.haeNiput($routeParams.kyselykertaid)
          .then(function (resp) {
            if (resp.data) {
              $scope.niput = resp.data;
            }}).catch(function (e) {
              console.error(e)
        })
      }

      $scope.toggleOmat = function(){
        $scope.vain_omat = !$scope.vain_omat;
        haeTunnukset();
      };

      Koulutustoimija.haeKaikki().then(function(resp) {
        if (!resp.data) {
          console.error('resp.data missing');
        }
        $scope.koulutustoimijat = resp.data;
      }).catch(function (e) {
        console.error(e);
      });

      Koulutustoimija.haeAktiivinen().then(function(resp) {
        if (!resp.data) {
          console.error('resp.data missing');
        }
        $scope.aktiivinenKoulutustoimija = resp.data;
      }).catch(function (e) {
        console.error(e);
      });

      Kysely.haeId($routeParams.kyselyid).then(function(resp) {
        if (!resp.data) {
          console.error('resp.data missing');
        }
        const kysely = resp.data;
        $scope.kysely = pvm.parsePvm(kysely);

        if(!kysely.kaytettavissa || kysely.automatisoitu) { setMuokkaustila(false); }
        haeTutkinnot(kysely)
        $scope.vain_omat = kysely.tyyppi === 'amispalaute'
        if(!$scope.uusi){
          haeTunnukset();
          if(kysely.tyyppi === 'tyoelamapalaute'){
            haeNiput();
          }
        }
      }).catch(function() {
        $location.url('/');
      });

      if (!$scope.uusi) {
        Kyselykerta.haeYksi($scope.kyselykertaid)
          .then(function(resp) {
            if (!resp.data) {
              console.error('resp.data missing');
            }
            const kyselykerta = resp.data;
            $scope.kyselykerta = pvm.parsePvm(kyselykerta);
            if (kyselykerta.lukittu) { setMuokkaustila(false); }
          })
          .catch(function() {
            $location.url('/');
          });
      }
      else {
        $scope.tunnukset = [];
        $scope.niput = []
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
            .then(function(resp) {
              if (!resp.data) {
                console.error('resp.data missing');
              }
              const kyselykerta = resp.data;
              $scope.kyselykertaForm.$setPristine();
              $location.url('/kyselyt/' + $routeParams.kyselyid + '/kyselykerta/' + kyselykerta.kyselykertaid);
            })
            .catch(function(virhe) {
              ilmoitus.virhe(i18n.hae(virhe), i18n.hae('kyselykerta.tallennus_epaonnistui'));
            });
        } else {
          Kyselykerta.tallenna($scope.kyselykertaid, $scope.kyselykerta)
            .then(function() {
              $scope.kyselykertaForm.$setPristine();
              ilmoitus.onnistuminen(i18n.hae('kyselykerta.tallennus_onnistui'));
            })
            .catch(function(virhe) {
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
          Vastaajatunnus.muokkaaVastaajienLukumaaraa($scope.kyselykertaid, tunnus.vastaajatunnusid, kohteiden_lkm).then(function() {
            tunnus.kohteiden_lkm = kohteiden_lkm;
          }).catch(function (e) {
            console.error(e);
          });
        }).catch(function (e) {
          console.error(e);
        });
      };

      $scope.poistaTunnus = function(tunnus) {
        varmistus.varmista(i18n.hae('vastaajatunnus.poista_otsikko'), null, i18n.hae('vastaajatunnus.poista_teksti'), i18n.hae('yleiset.poista')).then(function() {
          Vastaajatunnus.poista($scope.kyselykertaid, tunnus.vastaajatunnusid).then(function() {
            $scope.tunnukset = _.reject($scope.tunnukset, function(t) { return t.vastaajatunnusid === tunnus.vastaajatunnusid; });
          }).catch(function (e) {
            console.error(e);
          });
        }).catch(function (e) {
          console.error(e);
        });
      };

      $scope.lukitseTunnus = function(tunnus, lukitse) {
        Vastaajatunnus.lukitse($routeParams.kyselykertaid, tunnus.vastaajatunnusid, lukitse).then(function(resp) {
          if (!resp.data) {
            console.error('resp.data missing');
          }
          const uusiTunnus = resp.data;
          _.assign(tunnus, uusiTunnus);
        }).catch(function (e) {
          console.error(e);
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
      $scope.tutkinnonOsat = [];

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
          if (rahoituskausiVaihtuu < tanaan || rahoituskausiVaihtuu < vastaajatunnusVoimassaAlkaen) {
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
        Koulutustoimija.haeKoulutusluvalliset().then(function(resp) {
          if (!resp.data) {
            console.error('resp.data missing');
          }
          $scope.tutkinnonJarjestajat = resp.data;
        }).catch(function (e) {
          console.error(e);
        });
      }

      //Työelämäpalaute-pilotti
      if(kyselytyyppi === 'tyoelamapalaute') {
        Tutkinto.haeTutkinnonOsat().then(function(resp) {
          console.log("Tutkinnon osat: " + resp.data.length)
          $scope.tutkinnonOsat = resp.data
        }).catch(function (e) {
          console.error(e)
        });
      }

      function haeOppilaitokset(koulutustoimija) {
        Oppilaitos.haeKoulutustoimijanOppilaitokset(koulutustoimija).then(function (resp) {
          if (!resp.data) {
            console.error('resp.data missing');
          }
          const oppilaitokset = resp.data;
          if (oppilaitokset.length === 1) {
            $scope.vastaajatunnus.koulutuksen_jarjestaja_oppilaitos = oppilaitokset[0];
          } else {
            $scope.vastaajatunnus.koulutuksen_jarjestaja_oppilaitos = null;
          }
          $scope.oppilaitokset = oppilaitokset;
        }).catch(function (e) {
          console.error(e);
        });
      }

      function haeToimipaikat(oppilaitos) {
        Oppilaitos.haeOppilaitoksenToimipaikat(oppilaitos).then(function (resp) {
          if (!resp.data) {
            console.error('resp.data missing');
          }
          $scope.toimipaikat = resp.data;
        }).catch(function (e) {
          console.error(e);
        });
      }

      function haeTutkinnonJarjestajat(tutkinto) {
        Tutkinto.haeTutkinnonJarjestajat(tutkinto.tutkintotunnus).then(function (resp) {
          if (!resp.data) {
            console.error('resp.data missing');
          }
          $scope.tutkinnonJarjestajat = resp.data;
        }).catch(function (e) {
          console.error(e);
        });
      }

      function haeJarjestajanTutkinnot(ytunnus, kyselytyyppi) {
        Tutkinto.haeKoulutustoimijanTutkinnot(ytunnus, kyselytyyppi).then(function (resp){
          if (!resp.data) {
            console.error('resp.data missing');
          }
          $scope.tutkinnot = resp.data;
        }).catch(function (e) {
          console.error(e);
        });
      }

      function haeTutkinnot(){
        if($scope.haeKaikkiTutkinnot){
          Tutkinto.kyselytyypinTutkinnot(kyselytyyppi).then(function (resp){
            if (!resp.data) {
              console.error('resp.data missing');
            }
            $scope.tutkinnot = resp.data;
          }).catch(function (e) {
            console.error(e);
          });
        } else {
          if(laajennettu){
            haeJarjestajanTutkinnot($scope.vastaajatunnus.hankintakoulutuksen_toteuttaja.ytunnus, kyselytyyppi);
          } else{
            Tutkinto.koulutustoimijanTutkinnot(kyselytyyppi).then(function (resp){
              if (!resp.data) {
                console.error('resp.data missing');
              }
              $scope.tutkinnot = resp.data;
            }).catch(function (e) {
              console.error(e);
            });
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
