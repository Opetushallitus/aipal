'use strict';

describe('yhteiset.direktiivit,navigaatio', function() {

  var elementti, $rootScope, $location;

  beforeEach(module('yhteiset.direktiivit.navigaatio'));

  function onkoAktiivinen(osio) {
    return elementti.find('li.' + osio).hasClass('active');
  }

  beforeEach(inject(function($compile, _$rootScope_, _$location_) {
    var html = '<ul navigaatio><li class="li1"><a href="#/osio1">osio1</a></li><li class="li2"><a href="#/osio2">osio2</a></li></ul>';
    _$location_.path('/osio1');
    elementti = $compile(html)(_$rootScope_);
    _$rootScope_.$digest();

    $location = _$location_;
    $rootScope = _$rootScope_;
  }));

  it('Merkitsee valitun osion alussa oikein', function() {
    expect(onkoAktiivinen('li1')).toEqual(true);
    expect(onkoAktiivinen('li2')).toEqual(false);
  });

  it('Merkitsee valitun osion oikein kun osio vaihtuu', function() {
    $location.path('/osio2');
    $rootScope.$digest();
    expect(onkoAktiivinen('li1')).toEqual(false);
    expect(onkoAktiivinen('li2')).toEqual(true);
  });

});