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








