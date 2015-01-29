#!/bin/bash
iconv -f macintosh -t utf-8 | # enkoodaus
gsed 's/=[^;]*;/= /g'       | # suomenkieliset tekstit pois
gsed 's/\x0d/\x0a/g'        | # rivinvaihdot
tail -n +2                  | # otsikko pois
grep -v '^;;$'              | # tyhjät rivit pois
gsed 's/"\("\?\)/\1/g'      | # lainausmerkit pois, paitsi "" -> "
gsed 's/;$//g'              | # ; rivin lopussa pois
gsed 's/ *$//g'             | # välilyönnit rivin lopussa pois
gsed 's/;.*$//g'              # ylimääräiset sarakkeet (kommentit) pois

