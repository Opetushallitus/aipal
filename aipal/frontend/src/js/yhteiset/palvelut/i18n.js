// Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
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

angular.module('yhteiset.palvelut.i18n', ['ngResource'])

  .factory('kieli', [function() {
    var kieli = 'fi';
    if ('kieli' in localStorage) {
      kieli = localStorage.getItem('kieli');
    }
    return kieli;
  }])

  .factory('i18nHae', ['$window', function($window){
    return function(avain){
      var tulos = _.reduce(avain.split('.'), function(arvot, avain){
        if (arvot) {
          return arvot[avain];
        }
      }, this);
      if ($window.developmentMode && tulos === undefined) {
        $window.alert('Tuntematon käännösavain: ' + avain);
      }
      return tulos;
    };
  }])

  .factory('i18n', ['$resource', 'kieli', 'i18nHae', function($resource, kieli, i18nHae) {
    var i18nResource = $resource('api/i18n/:kieli');
    var i18n = i18nResource.get({kieli : kieli});
    i18n.$promise.then(function(){
      i18n.hae = i18nHae;
    });
    return i18n;
  }])

  .factory('$locale', ['kieli', function(kieli) {
    var PLURAL_CATEGORY = {ZERO: 'zero', ONE: 'one', TWO: 'two', FEW: 'few', MANY: 'many', OTHER: 'other'};

    var kalenteri = {
      'paivat': {
        'fi': 'su,ma,ti,ke,to,pe,la',
        'sv': 'Sön,Mån,Tis,Ons,Tor,Fre,Lör'
      },
      'kuukaudet': {
        'fi': 'Tammi,Helmi,Maalis,Huhti,Touko,Kesä,Heinä,Elo,Syys,Loka,Marras,Joulu',
        'sv': 'Januari,Februari,Mars,April,Maj,Juni,Juli,Augusti,September,Oktober,November,December'
      }
    };

    var paivat = kalenteri.paivat[kieli].split(',');
    var kuukaudet = kalenteri.kuukaudet[kieli].split(',');

    return {
      'DATETIME_FORMATS': {
        'AMPMS': [
          'ap.',
          'ip.'
        ],
        'DAY': paivat,
        'MONTH': kuukaudet,
        'SHORTDAY': paivat,
        'SHORTMONTH': kuukaudet,
        'fullDate': 'cccc, d. MMMM y',
        'longDate': 'd. MMMM y',
        'medium': 'd.M.yyyy H.mm.ss',
        'mediumDate': 'd.M.yyyy',
        'mediumTime': 'H.mm.ss',
        'short': 'd.M.yyyy H.mm',
        'shortDate': 'dd.MM.yyyy',
        'shortTime': 'H.mm'
      },
      'NUMBER_FORMATS': {
        'CURRENCY_SYM': '\u20ac',
        'DECIMAL_SEP': ',',
        'GROUP_SEP': '\u00a0',
        'PATTERNS': [
          {
            'gSize': 3,
            'lgSize': 3,
            'macFrac': 0,
            'maxFrac': 3,
            'minFrac': 0,
            'minInt': 1,
            'negPre': '-',
            'negSuf': '',
            'posPre': '',
            'posSuf': ''
          },
          {
            'gSize': 3,
            'lgSize': 3,
            'macFrac': 0,
            'maxFrac': 2,
            'minFrac': 2,
            'minInt': 1,
            'negPre': '-',
            'negSuf': '\u00a0\u00a4',
            'posPre': '',
            'posSuf': '\u00a0\u00a4'
          }
        ]
      },
      'id': kieli,
      'pluralCat': function (n) {  if (n === 1) {   return PLURAL_CATEGORY.ONE;  }  return PLURAL_CATEGORY.OTHER;}
    };
  }]);
