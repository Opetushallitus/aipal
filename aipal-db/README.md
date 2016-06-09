Aipal-db (Arvo)
=====

* Arvon tietokantamigraatioissa käytössä on flyway
* Aipal-tiimin kanssa on sovittu, että Arvon migraatioskriptien nimien merkitsevin numero on pariton (Aipalin parillinen)
    * Migraatioskriptin merkitsevintä numeroa muutetaan tuotantodeployn yhteydessä
* Flywaylle on asetettu outOfOrder-flag trueksi, joten Aipalin migraatioskriptien, joilla on suurempi järjestysnumero kuin Arvon viimeisimmillä, mergeäminen Arvoon ei aiheuta Arvon migraatioskriptien ajamatta jäämistä.


