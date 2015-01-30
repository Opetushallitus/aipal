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

  .controller('RaportitController', ['$scope', 'Koulutustoimija', 'Kysely', 'kyselykertaValilehti', 'Kysymysryhma', 'Raportti', 'Tutkinto', 'kaavioApurit', 'kieli', 'i18n', 'ilmoitus', 'seuranta',
    function($scope, Koulutustoimija, Kysely, kyselykertaValilehti, Kysymysryhma, Raportti, Tutkinto, kaavioApurit, kieli, i18n, ilmoitus, seuranta) {
    $scope.kyselykertaraportitValittu = !$scope.yllapitaja;
    $scope.raportti = {};
    $scope.kieli = kieli;
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
    var poistaTutkintoValinnat = function() {
      _.forEach($scope.koulutusalat, function(koulutusala) {
        _.forEach(koulutusala.opintoalat, function(opintoala) {
          _.forEach(opintoala.tutkinnot, function(tutkinto) {
            delete tutkinto.valittu;
          });
        });
      });
      _.forEach($scope.vanhentuneetKoulutusalat, function(koulutusala) {
        _.forEach(koulutusala.opintoalat, function(opintoala) {
          _.forEach(opintoala.tutkinnot, function(tutkinto) {
            delete tutkinto.valittu;
          });
        });
      });
      $scope.raportti.tutkinnot = [];
    };

    var poistaKyselyValinnat = function() {
      _.forEach($scope.kyselyt, function(kysely) {
        delete kysely.valittu;
      });
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
      poistaTutkintoValinnat();
      tyhjaaTaustakysymysvalinnat();

      // Kysely-ja kyselykertaraportilla tutkintorakennetasovalintaa ei ole näkyvissä, joten pitää valita tasoksi tutkinto
      if (tyyppi === 'kysely' || tyyppi === 'kyselykerta') {
        $scope.raportti.tutkintorakennetaso = 'tutkinto';
      }
    };

    $scope.vaihdaTyyppi('vertailu','Vertailuraportti');

    var haeTaustakysymykset = function(kysymysryhmaid) {
      Kysymysryhma.haeTaustakysymysryhma(kysymysryhmaid).success(function(kysymysryhma) {
        $scope.kysymysryhma = kysymysryhma;

        $scope.raportti.kysymykset = {};
        _.forEach(kysymysryhma.kysymykset, function(kysymys) {
          $scope.raportti.kysymykset[kysymys.kysymysid] = { monivalinnat: {} };
        });
      });
    };

    Kysymysryhma.haeTaustakysymysryhmat().success(function(kysymysryhmat) {
      $scope.taustakysymysryhmat = kysymysryhmat;

      $scope.$watch('raportti.taustakysymysryhmaid', function(kysymysryhmaid) {
        haeTaustakysymykset(kysymysryhmaid);
      });

      $scope.raportti.taustakysymysryhmaid = $scope.taustakysymysryhmat[0].kysymysryhmaid.toString();
    });

    Koulutustoimija.haeKaikki().success(function(koulutustoimijat) {
      $scope.koulutustoimijat = koulutustoimijat;
    });

    Kysely.hae().success(function (data) {
      $scope.kyselyt = data;
    });

    Tutkinto.haeVoimassaolevatTutkinnotHierarkiassa().success(function(koulutusalat) {
      $scope.koulutusalat = koulutusalat;
    });

    Tutkinto.haeVanhentuneetTutkinnotHierarkiassa().success(function(koulutusalat) {
      $scope.vanhentuneetKoulutusalat = koulutusalat;
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
          poistaTutkintoValinnat();
        }
        $scope.valitseTutkintoja(tutkinto);
      }
    };

    $scope.valitseKysely = function(kysely) {
      if(!kysely.valittu) {
        poistaKyselyValinnat();
      }
      kysely.valittu = !kysely.valittu;
      if (kysely.valittu) {
        $scope.raportti.kyselyid = kysely.kyselyid;
      } else {
        delete $scope.raportti.kyselyid;
      }
    };

    $scope.raportti.koulutustoimijat = [];
    $scope.valitseTaiPoistaKoulutustoimija = function(koulutustoimija) {
      if (_.remove($scope.raportti.koulutustoimijat, function(ytunnus) { return ytunnus === koulutustoimija.ytunnus; }).length === 0) {
        koulutustoimija.valittu = true;
        $scope.raportti.koulutustoimijat.push(koulutustoimija.ytunnus);
      } else {
        delete koulutustoimija.valittu;
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
        .success(function(tulokset) {
          $scope.tulokset = tulokset;
          $scope.tulos = tulokset[0];
        }).error(function(data, status) {
          if (status !== 500) {
            ilmoitus.virhe(i18n.hae('raportti.muodostus_epaonnistui'));
          }
        });
    };

    $scope.muodostaKyselyraportti = function(raportti) {
      seuranta.asetaLatausIndikaattori(Raportti.muodostaKyselyraportti(raportti.kyselyid, raportti), 'raportinMuodostus')
        .success(function(tulokset) {
          $scope.tulokset = tulokset;
          $scope.tulos = tulokset[0];
        })
        .error(function(value) {
          if (value.status !== 500) {
            ilmoitus.virhe(i18n.hae('raportti.muodostus_epaonnistui'));
          }
        });
    };

    kyselykertaValilehti.alusta($scope);

    $scope.lukumaaratYhteensa = kaavioApurit.lukumaaratYhteensa;
    $scope.prosenttiosuus = kaavioApurit.prosenttiosuus;
  }])

  .controller('RaporttiController', ['$scope', function($scope) {
    $scope.$watch('tulokset', function(tulokset) {
      if (tulokset !== undefined) {
        $scope.tulos = tulokset[0];
      }
    });
    $scope.naytaRaportti = function(raportti) {
      $scope.tulos = raportti;
    };
  }])

  .factory('kyselykertaValilehti', ['i18n', 'ilmoitus', 'Kyselykerta', 'Raportti', 'seuranta', function(i18n, ilmoitus, Kyselykerta, Raportti, seuranta){
    return {
      alusta: function alusta($scope) {
        Kyselykerta.hae().success(function(data) {
          $scope.kyselykerrat = data;
        });

        var poistaKyselykertaValinnat = function() {
          _.forEach($scope.kyselykerrat, function(kyselykerta) {
            delete kyselykerta.valittu;
          });
        };

        $scope.valitseKyselykerta = function(kyselykerta) {
          if (!kyselykerta.valittu) {
            poistaKyselykertaValinnat();
          }
          kyselykerta.valittu = !kyselykerta.valittu;
          if (kyselykerta.valittu) {
            $scope.raportti.kyselykertaid = kyselykerta.kyselykertaid;
          } else {
            delete $scope.raportti.kyselykertaid;
          }
        };

        $scope.muodostaKyselykertaraportti = function(raportti) {
          var parametrit = _.pick(raportti, 'vertailujakso_alkupvm', 'vertailujakso_loppupvm', 'tutkinnot');

          seuranta.asetaLatausIndikaattori(Raportti.muodostaKyselykertaraportti(raportti.kyselykertaid, parametrit), 'raportinMuodostus')
            .success(function(tulos) {
              $scope.tulokset = [tulos];
              $scope.tulos = tulos;
            })
            .error(function(value) {
              if (value.status !== 500) {
                ilmoitus.virhe(i18n.hae('raportti.muodostus_epaonnistui'));
              }
            });
        };
      }
    };
  }])
;
