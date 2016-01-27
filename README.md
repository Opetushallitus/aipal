AVOP
=====


Korkeakoulujen opiskelijapalautejärjestelmän lähdekoodi ja toteutus sijaitsee tässä repositoryssa. 

Toteutus perustuu suurimmaksi osaksi Opetushallituksen Aipal-järjestelmään, joten lähdekoodissa ja dokumentaatiossa viitataan Aipaliin. Kaikkien viittausten muuttaminen johtaisi ylimääräisiin ongelmiin kun muutoksia halutaan tuoda molempien järjestelmien lähdekoodiin. Aipal-järjestelmän lähdekoodi löytyy [Opetushallituksen Aipal-repositorysta](https://github.com/Opetushallitus/aipal). 

# Repositoryn sisältö ja rakenne

* **aipal**  - Varsinainen AVOP-sovellus
* **aipal/frontend** - AVOP käyttöliittymätoteutus
* **aipal-vastaus** - Vastaus-sovellus, jonka avulla palautetta kirjataan sisään
* **dev-scripts** - Kehitystyön avuksi tarkoitettuja skriptejä.
* **aipal-db** - Flyway-kirjastoon perustuva työkalu tietokannan automatisoituun hallintaan
* **e2e** - end-to-end selaintestit sovellukselle
* **vagrant** - virtuaalikonekonfiguraatiot sovelluksen ajamiseksi virtuaalikoneessa
* **env** - virtuaalikoneiden asetustiedostot

# Kehitystyöhön liittyviä ohjeita

Koodi on enimmäkseen [Clojurea](http://clojure.org/). Tarvitset Oracle Java-virtuaalikoneen ja [leiningen](http://leiningen.org/) työkalun.

* aipal-hakemistossa oma readme-tiedosto. Frontend-hakemistossa myös.
* OPH:n [Aitu-projekti](https://github.com/Opetushallitus/aitu) on ollut mallina ja monet käytännöt ja työkalut ovat samoja.

## Erityiset riippuvuudet 

Katso [Aipal dokumentaatio](https://confluence.csc.fi/pages/viewpage.action?pageId=53517029) CSC:n Confluencesta. Confluenceen tulee näkyviin [arkkitehtuurin yleiskuvat](https://confluence.csc.fi/display/OPHPALV/Aipal+Arkkitehtuuri) ja vastaavat asiat.
