#!/bin/bash
iconv -f macintosh -t utf-8 | # enkoodaus
gsed 's/=[^;]*;/= /g'       | # suomenkieliset tekstit pois
gsed 's/\x0d/\x0a/g'        | # rivinvaihdot
tail -n +2                  | # otsikko pois
grep -v '^;;$'              | # tyhjät rivit pois
gsed 's/"\("\?\)/\1/g'      | # lainausmerkit pois, paitsi "" -> "
gsed 's/;$//g'              | # ; rivin lopussa pois
gsed 's/ *$//g'             | # välilyönnit rivin lopussa pois
gsed 's/;.*$//g'            | # ylimääräiset sarakkeet (kommentit) pois
gsed 's/å/\\u00e5/g'        | # umlautit properties-muotoon
gsed 's/ä/\\u00e4/g'        |
gsed 's/ö/\\u00f6/g'        |
gsed 's/Å/\\u00c5/g'        |
gsed 's/Ä/\\u00c4/g'        |
gsed 's/Ö/\\u00d6/g'        |
grep '='                      # ylimääräiset rivit pois

