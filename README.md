ARVO
=====


Korkeakoulujen opiskelijapalautejärjestelmän lähdekoodi ja toteutus sijaitsee tässä repositoryssa.

Toteutus perustuu suurimmaksi osaksi Opetushallituksen Aipal-järjestelmään, joten lähdekoodissa ja dokumentaatiossa viitataan Aipaliin. Kaikkien viittausten muuttaminen johtaisi ylimääräisiin ongelmiin kun muutoksia halutaan tuoda molempien järjestelmien lähdekoodiin. Aipal-järjestelmän lähdekoodi löytyy [Opetushallituksen Aipal-repositorysta](https://github.com/Opetushallitus/aipal).

Asentamisesta varten kannattaa katsoa [Aitu-projekti](https://github.com/Opetushallitus/aitu). Muutamia eroja:

koneet nimet ovat aipal-db ja aipal-app

# Repositoryn sisältö ja rakenne

* **aipal**  - Varsinainen ARVO-sovellus
* **aipal/frontend** - ARVO käyttöliittymätoteutus
* **aipal-vastaus** - Vastaus-sovellus, jonka avulla palautetta kirjataan sisään
* **dev-scripts** - Kehitystyön avuksi tarkoitettuja skriptejä.
* **aipal-db** - Flyway-kirjastoon perustuva työkalu tietokannan automatisoituun hallintaan
* **e2e** - end-to-end selaintestit sovellukselle
* **vagrant** - virtuaalikonekonfiguraatiot sovelluksen ajamiseksi virtuaalikoneessa
* **env** - virtuaalikoneiden asetustiedostot

# Kehitystyöhön liittyviä ohjeita

Katso [Aipal-projekti](https://github.com/Opetushallitus/aipal). Sama käytäntö.

Arvoon kopioidaan ajoittain Aipal-projektiin tehdyt kommitit. Tietokantaongelmien välttämiseksi Aipal:a kehittävän tiimin kanssa on sovittu yhteisestä käytännöstä migraatioiden nimeämiseksi. Asiasta tarkemmin aipal-db:n [README:ssä](https://github.com/CSC-IT-Center-for-Science/arvo/blob/master/aipal-db/README.md)

### Aipal - Arvo merge
Esimerkki työnkulusta, jolla aipaliin tehdyt kommitit kopioidaan Arvoon:

* git checkout master
* git pull
* git checkout aipal-merge
* git merge master
* git remote add upstream git@github.com:Opetushallitus/aipal.git
* git pull upstream master
* git push
* git checkout master
* git merge aipal-merge
* git push

## Erityiset riippuvuudet

Katso [Aipal-projekti](https://github.com/Opetushallitus/aipal). Samat riippuvuudet on käytössä tässä.

# Virtuaalikoneiden käyttö

Katso [Aipal-projekti](https://github.com/Opetushallitus/aipal). Sama käytäntö.

Sovelluksen asennus virtuaalikoneeseen

HUOM! Ensimmäinen kerta ennen kun ajetaan **deploy.sh** skripti tietokannan skeema pitaisi olla etukäteen luottu. Aja **create-db-schema** ennen.

# Dokumentaatio

Katso [Aipal dokumentaatio](https://confluence.csc.fi/pages/viewpage.action?pageId=53517029) CSC:n Confluencesta. Confluenceen tulee näkyviin [arkkitehtuurin yleiskuvat](https://confluence.csc.fi/display/OPHPALV/Aipal+Arkkitehtuuri) ja vastaavat asiat.
