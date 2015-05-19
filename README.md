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

* aipal-hakemistossa oma readme-tiedosto. Frontend-hakemistossa myös.
* OPH:n [Aitu-projekti](https://github.com/Opetushallitus/aitu) on ollut mallina ja monet käytännöt ja työkalut ovat samoja.

Toteutuskoodilla on riippuvuus yleiskäyttöisiä kirjastofunktioita sisältävään [clojure-utils](https://github.com/Opetushallitus/clojure-utils) repositoryyn siten että molemmat täytyy paikallisesti kloonata rinnakkaisiin hakemistoihin.

# Virtuaalikoneiden käyttö

Sovellusta voi ajaa paikallisesti [Vagrant](http://www.vagrantup.com/) ohjelman avulla. Virtuaalikoneiden ajamisesta huolehtii [https://www.virtualbox.org/](Oracle Virtualbox). Molemmat ovat ilmaisia ohjelmia. Virtuaalikoneissa ajetaan [CentOS](http://www.centos.org/) Linux-käyttöjärjestelmää ja palvelinohjelmistoina erilaisia avoimen lähdekoodin ilmaisia sovelluksia, kuten [PostgreSQL](http://www.postgresql.org/).

# Kehitystyö

Koodi on enimmäkseen [Clojurea](http://clojure.org/). Tarvitset Java-virtuaalikoneen ja [leiningen](http://leiningen.org/) työkalun.

