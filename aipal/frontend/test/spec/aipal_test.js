'use strict';

describe('Module: aipal', function() {

  beforeEach(module('aipal'));

  describe('asetukset', function(){
    var asetukset;

    beforeEach(inject(function(_asetukset_) {
      asetukset = _asetukset_;
    }));

    it('requestTimeout asetetaan arvoon 120000', function(){
      expect(asetukset.requestTimeout).toEqual(120000);
    });
  });
});