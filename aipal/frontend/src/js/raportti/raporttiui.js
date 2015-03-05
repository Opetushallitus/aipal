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

angular.module('raportti.raporttiui', ['ngRoute', 'rest.raportti', 'raportti.kyselykerta.kaavioapurit', 'yhteiset.direktiivit.valintalista'])
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/raportit', {
        controller: 'RaportitController',
        templateUrl: 'template/raportti/raportit.html',
        label: 'i18n.raportit.breadcrumb_raportit'
      });
  }])

.factory('raporttiApurit', [function(){
    return {
      poistaKyselyValinnat: function(kyselyt, raportti) {
        _.forEach(kyselyt, function(kysely) {
          delete kysely.valittu;
        });
        raportti.koulutuksen_jarjestajat = [];
        raportti.jarjestavat_oppilaitokset = [];
        delete raportti.kyselyid;
      },

      poistaKyselykertaValinnat: function(kyselykerrat, raportti) {
        _.forEach(kyselykerrat, function(kyselykerta) {
          delete kyselykerta.valittu;
        });
        raportti.koulutuksen_jarjestajat = [];
        raportti.jarjestavat_oppilaitokset = [];
        delete raportti.kyselykertaid;
      },

      poistaTutkintoValinnat: function(koulutusalat, vanhentuneetKoulutusalat, raportti) {
        _.forEach(koulutusalat, function(koulutusala) {
          _.forEach(koulutusala.opintoalat, function(opintoala) {
            _.forEach(opintoala.tutkinnot, function(tutkinto) {
              delete tutkinto.valittu;
            });
          });
        });
        _.forEach(vanhentuneetKoulutusalat, function(koulutusala) {
          _.forEach(koulutusala.opintoalat, function(opintoala) {
            _.forEach(opintoala.tutkinnot, function(tutkinto) {
              delete tutkinto.valittu;
            });
          });
        });
        raportti.tutkinnot = [];
      }
    };
  }])

  .controller('RaportitController', ['$scope', 'Koulutustoimija', 'kyselyValilehti', 'kyselykertaValilehti', 'Kieli', 'Kysymysryhma', 'Rahoitusmuoto', 'Raportti', 'Tutkinto', 'kaavioApurit', 'kieli', 'i18n', 'ilmoitus', 'raporttiApurit', 'seuranta',
    function($scope, Koulutustoimija, kyselyValilehti, kyselykertaValilehti, Kieli, Kysymysryhma, Rahoitusmuoto, Raportti, Tutkinto, kaavioApurit, kieli, i18n, ilmoitus, raporttiApurit, seuranta) {
    $scope.kyselykertaraportitValittu = !$scope.yllapitaja;
    $scope.raportti = {};
    $scope.raportti.kieli = kieli;
    $scope.raportti.tyyppi = 'vertailu';
    $scope.raportti.tutkintorakennetaso = 'tutkinto';
    $scope.printgraphs = true;
    $scope.printfreetext = true;
    $scope.$watch('raportti', function(uusi) {
      $scope.parametrit = JSON.stringify(uusi);
    }, true);

    var poistaKoulutusalaValinnat = function() {
      _.forEach($scope.koulutusalat, function(koulutusala) {
        delete koulutusala.valittu;
      });
      _.forEach($scope.vanhentuneetKoulutusalat, function(koulutusala) {
        delete koulutusala.valittu;
      });
      $scope.raportti.koulutusalat = [];
    };
    var poistaOpintoalaValinnat = function() {
      _.forEach($scope.koulutusalat, function(koulutusala) {
        _.forEach(koulutusala.opintoalat, function(opintoala) {
          delete opintoala.valittu;
        });
      });
      _.forEach($scope.vanhentuneetKoulutusalat, function(koulutusala) {
        _.forEach(koulutusala.opintoalat, function(opintoala) {
          delete opintoala.valittu;
        });
      });
      $scope.raportti.opintoalat = [];
    };

    var tyhjaaTaustakysymysvalinnat = function() {
      _.forEach($scope.raportti.kysymykset, function (kysymys) {
        kysymys.monivalinnat = {};
      });
    };

    $scope.vaihdaTyyppi = function(tyyppi, nimi) {
      $scope.raportti.tyyppi = tyyppi;
      $scope.selectedTabName = nimi;
      // Vain vertailuraportilla voi valita useamman tutkinnon/alan, joten tyhjennä valinnat raportin tyypin vaihtuessa
      poistaKoulutusalaValinnat();
      poistaOpintoalaValinnat();
      raporttiApurit.poistaTutkintoValinnat($scope.koulutusalat, $scope.vanhentuneetKoulutusalat, $scope.raportti);
      tyhjaaTaustakysymysvalinnat();
      raporttiApurit.poistaKyselykertaValinnat($scope.kyselykerrat, $scope.raportti);
      raporttiApurit.poistaKyselyValinnat($scope.kyselyt, $scope.raportti);

      // Kysely- ja kyselykertaraportilla asetetaan alkupvm kysely(kerra)n mukaan, joten tyhjennä se välilehteä vaihtaessa
      delete $scope.raportti.vertailujakso_alkupvm;
      delete $scope.raportti.vertailujakso_loppupvm;

      // Kysely-ja kyselykertaraportilla tutkintorakennetasovalintaa ei ole näkyvissä, joten pitää valita tasoksi tutkinto
      if (tyyppi === 'kysely' || tyyppi === 'kyselykerta') {
        $scope.raportti.tutkintorakennetaso = 'tutkinto';
      }
      // Säilytetään rahoitusmuodon ja suorituskielen valinta kysely- ja kyselykertasivuilla
      else {
        delete $scope.raportti.rahoitusmuotoid;
        delete $scope.raportti.suorituskieli;
      }
    };

    $scope.vaihdaTyyppi('vertailu','Vertailuraportti');

    var haeTaustakysymykset = function(kysymysryhmaid) {
      Kysymysryhma.haeTaustakysymysryhma(kysymysryhmaid).success(function(kysymysryhma) {
        $scope.taustakysymysryhma = kysymysryhma;

        $scope.raportti.kysymykset = {};
        _.forEach(kysymysryhma.kysymykset, function(kysymys) {
          $scope.raportti.kysymykset[kysymys.kysymysid] = { monivalinnat: {} };
        });
      });
    };

    Kysymysryhma.haeTaustakysymysryhmat().success(function(kysymysryhmat) {
      $scope.taustakysymysryhmat = kysymysryhmat;
      _.forEach($scope.taustakysymysryhmat, function(taustakysymysryhma) {
        if (taustakysymysryhma.kysymysryhmaid === 1) {
          taustakysymysryhma.lisateksti = i18n.raportit.taustakysymysryhma_vanha_lisateksti;
        }
        else if (taustakysymysryhma.kysymysryhmaid === 3341885) {
          taustakysymysryhma.lisateksti = i18n.raportit.taustakysymysryhma_uusi_lisateksti;
        }
      });

      $scope.$watch('raportti.taustakysymysryhmaid', function(kysymysryhmaid) {
        haeTaustakysymykset(kysymysryhmaid);
      });

      $scope.raportti.taustakysymysryhmaid = $scope.taustakysymysryhmat[0].kysymysryhmaid.toString();
    });

    Koulutustoimija.haeKaikki().success(function(koulutustoimijat) {
      $scope.koulutustoimijat = koulutustoimijat;
    });

    Tutkinto.haeVoimassaolevatTutkinnotHierarkiassa().success(function(koulutusalat) {
      $scope.koulutusalat = koulutusalat;
    });

    Tutkinto.haeVanhentuneetTutkinnotHierarkiassa().success(function(koulutusalat) {
      $scope.vanhentuneetKoulutusalat = koulutusalat;
    });

    Rahoitusmuoto.haeKaikki(function(rahoitusmuodot) {
      $scope.rahoitusmuodot = rahoitusmuodot;
    });
    Kieli.haeKaikki().success(function(kielet) {
      $scope.kielet = _.pluck(kielet, 'kieli');
    });

    $scope.piilotaTutkintorakenneVaihto = function() {
      return $scope.raportti.tyyppi === 'kysely' || $scope.raportti.tyyppi === 'kyselykerta';
    };

    var voikoValitaUseita = function() {
      return $scope.raportti.tyyppi === 'vertailu';
    };
    $scope.raportti.koulutusalat = [];
    $scope.valitseKoulutusala = function(koulutusala) {
      if ($scope.raportti.tutkintorakennetaso === 'koulutusala') {
        // Vain vertailuraportilla voi valita useamman
        if (!voikoValitaUseita() && !koulutusala.valittu) {
          poistaKoulutusalaValinnat();
        }
        koulutusala.valittu = !koulutusala.valittu;
        $scope.raportti.koulutusalat = _.xor($scope.raportti.koulutusalat, [koulutusala.koulutusalatunnus]);
      }
    };
    $scope.raportti.opintoalat = [];
    $scope.valitseOpintoala = function(opintoala) {
      if ($scope.raportti.tutkintorakennetaso === 'opintoala') {
        if (!voikoValitaUseita() && !opintoala.valittu) {
          poistaOpintoalaValinnat();
        }
        opintoala.valittu = !opintoala.valittu;
        $scope.raportti.opintoalat = _.xor($scope.raportti.opintoalat, [opintoala.opintoalatunnus]);
      }
    };
    $scope.raportti.tutkinnot = [];
    $scope.valitseTutkintoja = function(tutkinto) {
      tutkinto.valittu = !tutkinto.valittu;
      $scope.raportti.tutkinnot = _.xor($scope.raportti.tutkinnot, [tutkinto.tutkintotunnus]);
    };
    $scope.valitseTutkinto = function(tutkinto) {
      if ($scope.raportti.tutkintorakennetaso === 'tutkinto') {
        if (!voikoValitaUseita() && !tutkinto.valittu) {
          raporttiApurit.poistaTutkintoValinnat($scope.koulutusalat, $scope.vanhentuneetKoulutusalat, $scope.raportti);
        }
        $scope.valitseTutkintoja(tutkinto);
      }
    };

    $scope.raportti.koulutustoimijat = [];
    $scope.raportti.koulutuksen_jarjestajat = [];
    $scope.raportti.jarjestavat_oppilaitokset = [];
    $scope.vaihdaValinta = function(elementti, taulukko, idAvain) {
      if (_.remove($scope.raportti[taulukko], function(item) { return item === elementti[idAvain]; }).length === 0) {
        elementti.valittu = true;
        $scope.raportti[taulukko].push(elementti[idAvain]);
      } else {
        delete elementti.valittu;
      }
    };

    $scope.raporttiValidi = function() {
      if ($scope.raportti.tyyppi === 'vertailu') {
        return ($scope.raportti.tutkintorakennetaso === 'tutkinto' && $scope.raportti.tutkinnot.length > 0) ||
          ($scope.raportti.tutkintorakennetaso === 'opintoala' && $scope.raportti.opintoalat.length > 0) ||
          ($scope.raportti.tutkintorakennetaso === 'koulutusala' && $scope.raportti.koulutusalat.length > 0);
      }
      else if ($scope.raportti.tyyppi === 'koulutustoimijat') {
        return $scope.raportti.koulutustoimijat.length > 0;
      }
      else if ($scope.raportti.tyyppi === 'kysely') {
        return _.isNumber($scope.raportti.kyselyid);
      }
      else if ($scope.raportti.tyyppi === 'kyselykerta') {
        return _.isNumber($scope.raportti.kyselykertaid);
      }
      return true;
    };

    $scope.muodostaRaportti = function() {
      seuranta.asetaLatausIndikaattori(Raportti.muodosta($scope.raportti), 'raportinMuodostus')
        .success(function(tulos) {
          $scope.tulokset = tulos;
          $scope.tulos = tulos;
          $scope.raporttiIndeksit = _.range(tulos.raportoitavia);
        }).error(function(data, status) {
          if (status !== 500) {
            ilmoitus.virhe(i18n.hae('raportti.muodostus_epaonnistui'));
          }
        });
    };

    kyselykertaValilehti.alusta($scope);
    kyselyValilehti.alusta($scope);

    var lukumaaratYhteensa = function (jakauma, i) {
      var lukumaarat = _.map(_.pluck(jakauma, 'lukumaara'), function(taulukko) {return taulukko[i];});
      return _.reduce(lukumaarat, function (sum, n) {return sum + n;});
    };
    $scope.lukumaaratYhteensa = lukumaaratYhteensa;
    $scope.prosenttiosuus = kaavioApurit.prosenttiosuus;
  }])

  .controller('RaporttiController', ['$scope', function($scope) {
    $scope.$watch('tulokset', function(tulokset) {
      if (tulokset !== undefined) {
//        $scope.tulos = tulokset;
      }
    });
    $scope.naytaRaportti = function(raportti) {
//      $scope.tulos = raportti;
    };
  }])

  .factory('kyselyValilehti', ['$filter', 'i18n', 'ilmoitus', 'Kysely', 'Raportti', 'raporttiApurit', 'seuranta', function($filter, i18n, ilmoitus, Kysely, Raportti, raporttiApurit, seuranta) {
    return {
      alusta: function alusta($scope) {
        var suodataKyselyt = function() {
          var tila = $scope.tilafilter.tila;
          if (tila !== 'kaikki') {
            $scope.kyselytTilassa = $filter('filter')($scope.kyselyt, {tila: tila});
          } else {
            $scope.kyselytTilassa = $scope.kyselyt;
          }
        };

        $scope.tilafilter = { 'tila': 'julkaistu'};
        $scope.$watch('tilafilter.tila', function() {
          suodataKyselyt();
        });

        Kysely.hae().success(function (data) {
          $scope.kyselyt = data;
          suodataKyselyt();
        });

        $scope.$watch('raportti.kyselyid', function(uusi) {
          if (!_.isUndefined(uusi)) {
            Kysely.haeVastaustunnustiedot(uusi).success(function(tiedot) {
              $scope.vastaustunnustiedot = tiedot;
            });
          }
          else {
            $scope.vastaustunnustiedot = {};
          }
        });

        $scope.valitseKysely = function(kysely) {
          if(!kysely.valittu) {
            raporttiApurit.poistaKyselyValinnat($scope.kyselyt, $scope.raportti);
          }
          kysely.valittu = !kysely.valittu;
          if (kysely.valittu) {
            $scope.raportti.kyselyid = kysely.kyselyid;
            $scope.raportti.vertailujakso_alkupvm = kysely.voimassa_alkupvm;
          } else {
            delete $scope.raportti.kyselyid;
          }
        };

        $scope.muodostaKyselyraportti = function(raportti) {
          seuranta.asetaLatausIndikaattori(Raportti.muodostaKyselyraportti(raportti.kyselyid, raportti), 'raportinMuodostus')
            .success(function(tulos) {
              $scope.tulokset = tulos;
              $scope.tulos = tulos;
              $scope.raporttiIndeksit = _.range(tulos.raportoitavia);
            })
            .error(function(value) {
              if (value.status !== 500) {
                ilmoitus.virhe(i18n.hae('raportti.muodostus_epaonnistui'));
              }
            });
        };

        $scope.$watch('raportti.ei_tutkintoa', function(ei_tutkintoa) {
          if (ei_tutkintoa) {
            raporttiApurit.poistaTutkintoValinnat($scope.koulutusalat, $scope.vanhentuneetKoulutusalat, $scope.raportti);
          }
        });
      }
    };
  }])

  .factory('kyselykertaValilehti', ['$filter', 'i18n', 'ilmoitus', 'Kyselykerta', 'Raportti', 'raporttiApurit', 'seuranta', function($filter, i18n, ilmoitus, Kyselykerta, Raportti, raporttiApurit, seuranta){
    return {
      alusta: function alusta($scope) {
        var suodataKyselykerrat = function() {
          var tila = $scope.tilafilterKyselykerta.tila;
          if (tila !== 'kaikki') {
            $scope.kyselykerratTilassa = $filter('filter')($scope.kyselykerrat, {kaytettavissa: tila === 'avoin'});
          } else {
            $scope.kyselykerratTilassa = $scope.kyselykerrat;
          }
        };

        $scope.tilafilterKyselykerta = { 'tila': 'avoin'};
        $scope.$watch('tilafilterKyselykerta.tila', function() {
          suodataKyselykerrat();
        });

        Kyselykerta.hae().success(function(data) {
          $scope.kyselykerrat = data;
          suodataKyselykerrat();
        });

        $scope.$watch('raportti.kyselykertaid', function(uusi) {
          if (!_.isUndefined(uusi)) {
            Kyselykerta.haeVastaustunnustiedot(uusi).success(function(tiedot) {
              $scope.vastaustunnustiedot = tiedot;
            });
          }
          else {
            $scope.vastaustunnustiedot = {};
          }
        });

        $scope.valitseKyselykerta = function(kyselykerta) {
          if (!kyselykerta.valittu) {
            raporttiApurit.poistaKyselykertaValinnat($scope.kyselykerrat, $scope.raportti);
          }
          kyselykerta.valittu = !kyselykerta.valittu;
          if (kyselykerta.valittu) {
            $scope.raportti.kyselykertaid = kyselykerta.kyselykertaid;
            $scope.raportti.vertailujakso_alkupvm = kyselykerta.voimassa_alkupvm;
          } else {
            delete $scope.raportti.kyselykertaid;
          }
        };

        $scope.muodostaKyselykertaraportti = function(raportti) {
          seuranta.asetaLatausIndikaattori(Raportti.muodostaKyselykertaraportti(raportti.kyselykertaid, raportti), 'raportinMuodostus')
            .success(function(tulos) {
              $scope.tulokset = tulos;
              $scope.tulos = tulos;
              $scope.raporttiIndeksit = _.range(tulos.raportoitavia);
            })
            .error(function(value) {
              if (value.status !== 500) {
                ilmoitus.virhe(i18n.hae('raportti.muodostus_epaonnistui'));
              }
            });
        };

        $scope.$watch('raportti.ei_tutkintoa', function(ei_tutkintoa) {
          if (ei_tutkintoa) {
            raporttiApurit.poistaTutkintoValinnat($scope.koulutusalat, $scope.vanhentuneetKoulutusalat, $scope.raportti);
          }
        });
      }
    };
  }])
;
