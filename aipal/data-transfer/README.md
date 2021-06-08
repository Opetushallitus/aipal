# Anonymisoidun datan siirtäminen tuotannosta testiympäristöön
snap-ympäristössä ei tällä hetkellä ole riittävästi tilaa erillisen /data-osion puutten takia, että anonymisointi voitaisiin tehdä siellä vaan on tämä tehtävä omalla koneella ja luotava erillinen arkistoidussa muodossa oleva dumppi joka mahtuu /home-osioon.

test-ympäristössä datan tuonti voidaan tehdä alla olevilla ohjeilla.

## Tarvitset
- arvo_snap_adm käyttäjän salasanan
- sudo oikeudet arvon koneille

## Dumpin ottaminen
Ota tuotannosta dumppi. Vie tällä hetkellä pakatussa (directory) muodossa ~300Mt.

    cd /data/
    pg_dump -d arvo -U postgres -c -x -O -F directory -f not_anon_dump -T api_kayttajat -T koulutustoimija -T oppilaitos -T toimipaikka -T schema_version

## Dumpin ajaminen snap-ympäristöön
Kopioi dumppi haluttuun ympäristöön. Dumppi ajetaan vanhan kannan päälle jolloin tarvittavat taulut tyhjennetään ja korvataan uudella datalla.

Ota kopio kannasta.

    sudo -u postgres psql
    create database arvo_snap_20210226 with template arvo_snap;
Aja tuotantodata äsken luotuun kannan kopioon.
Huom. pg_restoren pitää olla sama versio kuin millä dumppi on otettu

    pg_restore -d arvo_snap_20210226 -U arvo_snap_adm -c -O not_anon_dump -h snaparvo.csc.fi

## Anonymisointi
Anonymisoi tuotantodata. anonymisointi.sql löytyy tämän ohjeen kanssa samasta kansiosta. Tähän kannattaa varata aikaa 3-4h joten voi olla järkevää ajaa anonymisointi taustalla.

    psql -f anonymisointi.sql -d arvo_snap -U arvo_snap_adm
    [1]+  Stopped                 myprogram
    $ disown -h
    $ bg
    [1]+ myprogram &
    $ logout

Tarkista, ettei anonymisoinnissa ole tullut virheitä. Jos virheitä löytyy palauta varmuuskopio.

Sammuta palvelut, vaihda kannat ja käynnistä palvelut

    sudo -u tomcat /opt/arvo-snap/stop-arvo-snap.sh
    sudo -u tomcat /opt/arvovastaus-snap/stop-arvovastaus-snap.sh

    sudo su - postgres
    psql
    ALTER DATABASE arvo_snap RENAME TO arvo_snap_backup;
    ALTER DATABASE arvo_snap_20210226 RENAME TO arvo_snap;
    \q
    exit

    sudo -u tomcat /opt/arvo-snap/start-arvo-snap.sh
    sudo -u tomcat /opt/arvovastaus-snap/start-arvovastaus-snap.sh

## Siivous
Poista 
- tuotannon dumppi (not_anon_dump)
- anonymisointi-skripti (anonymisointi.sql)
- vanhat varmuuskopiot.
