AIPAL
=====

Aikuiskoulutuksen palautejärjestelmä. 

# Repositoryn sisältö ja rakenne

* **aipal**  - Varsinainen AIPAL-sovellus
* **aipal/frontend** - AIPAL käyttöliittymätoteutus
* **aipal-vastaus** - Vastaus-sovellus, jonka avulla palautetta kirjataan sisään
* **dev-scripts** - Kehitystyön avuksi tarkoitettuja skriptejä.
* **aipal-db** - Flyway-kirjastoon perustuva työkalu tietokannan automatisoituun hallintaan
* **e2e** - end-to-end selaintestit AIPAL-sovellukselle
* **vagrant** - virtuaalikonekonfiguraatiot sovelluksen ajamiseksi virtuaalikoneessa
* **env** - virtuaalikoneiden asetustiedostot

# Kehitystyöhön liittyviä ohjeita

Koodi on enimmäkseen [Clojurea](http://clojure.org/). Tarvitset Java-virtuaalikoneen ja [leiningen](http://leiningen.org/) työkalun.

* aipal-hakemistossa oma readme-tiedosto. Frontend-hakemistossa myös.
* OPH:n [Aitu-projekti](https://github.com/Opetushallitus/aitu) on ollut mallina ja monet käytännöt ja työkalut ovat samoja.

## Erityiset riippuvuudet 

Toteutuskoodilla on riippuvuus yleiskäyttöisiä kirjastofunktioita sisältävään [clojure-utils](https://github.com/Opetushallitus/clojure-utils) repositoryyn joka on git submodulena.

Lisäksi käyttöliittymätoteutuksessa on riippuvuutena [aituaipaljs](https://github.com/Opetushallitus/aituaipaljs). Tätä repositorya ei tarvitse kloonata itselleen kehitystyötä varten.

Selaintesteissä käytettävä kirjasto löytyy valmiiksi paketoituna [Clojars palvelusta](https://clojars.org/solita/opetushallitus-aitu-e2e). Tämän komponentin lähdekoodi on [Aitun repositoryssa](https://github.com/Opetushallitus/aitu/tree/master/aitu-common-e2e).

# Virtuaalikoneiden käyttö

Sovellusta voi ajaa paikallisesti [Vagrant](http://www.vagrantup.com/) ohjelman avulla. Virtuaalikoneiden ajamisesta huolehtii [https://www.virtualbox.org/](Oracle Virtualbox). Molemmat ovat ilmaisia ohjelmia. Virtuaalikoneissa ajetaan [CentOS](http://www.centos.org/) Linux-käyttöjärjestelmää ja palvelinohjelmistoina erilaisia avoimen lähdekoodin ilmaisia sovelluksia, kuten [PostgreSQL](http://www.postgresql.org/).

# Dokumentaatio

Järjestelmän toimintaan liittyvä yleinen dokumentaatio löytyy [Aipal wiki-sivulta](https://confluence.csc.fi/pages/viewpage.action?pageId=53517029) CSC:n julkisesta palvelusta. Confluenceen tulee näkyviin [arkkitehtuurin yleiskuvat](https://confluence.csc.fi/display/OPHPALV/Aipal+Arkkitehtuuri) ja vastaavat asiat.
