Frontend asennus:

Asenna node ja npm:
http://nodejs.org/download/

Asenna grunt:

```
npm install -g grunt-cli
```

Asenna bower:

```
npm install -g bower
```

Uusin NodeJS+ NPM + node-sass ei toimi yhteen. Tämä voi auttaa:
```
npm install node-sass@3.7.0
```

Hakemistossa /frontend tee seuraavat:

```
npm install
bower install
```

Lokaali kehityspalvelin. (localhost:3000. Livereload ja SASS compile automaattisesti):

```
grunt
```

Frontin buildaus ja kopiointi resources/public/app -hakemistoon:

```
grunt build
```

Uuden bower kirjaston käyttöönotto:

```
bower install <kirjaston-nimi> --save
```

Uuden nodejs kirjaston asennus kehityskäyttöön:

```
npm install <kirjaston-nimi> --save-dev
npm shrinkwrap --dev
```

Testien ajaminen:

```
Grunt test
```

Testien ajaminen jatkuvasti:

```
Grunt autotest
```

## Bootstrap

Bootstrap-teema on generoitu Bootstrap Magicilla: http://pikock.github.io/bootstrap-magic/app/index.html#!/editor

Jos haluat muuttaa bootstrapin asetuksia, älä muokkaa tiedosta magic-bootstrap-min.css käsin, vaan toimi seuraavasti:

1.  Kopioi tiedoston aipal/aipal/frontend/src/magic-bootstrap.less sisältö leikepöydälle:

        # OS X
        cat magic-bootstrap.less | pbcopy

        # Linux
        xclip -selection c magic-bootstrap.less

2.  Avaa http://pikock.github.io/bootstrap-magic/app/index.html#!/editor, klikkaa Import Less Variables -nappia ja liitä tiedoston magic-boostrap.less sisältö leikepöydältä.

3.  Muuta asetukset haluamasi mukaiseksi ja paina Apply-nappia.

4.  Valitse asetus Minified, paina Save Less Variables -nappia ja ylikirjoita ladatulla tiedostolla aipal/aipal/frontend/src/magic-bootstrap.less.

5.  Paina alareunan Save CSS -nappia ja ylikirjoita ladatulla tiedostolla aipal/aipal/frontend/src/css/magic-bootstrap-min.css.
