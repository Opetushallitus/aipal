#!/bin/bash

# Käynnistää AIPAL-version, johon aipal.jar-linkki osoittaa.
# Ajettava AIPAL-asennushakemistossa käyttäjänä, jolla AIPAL-prosessia halutaan
# ajaa.
#
# Käyttö:
#
#     ./start-aipal.sh

set -eu

current_jarfile='aipal.jar'
pidfile='aipal.pid'

echo "Starting Aipal... "
if [ -a $current_jarfile ]
then
    nohup java -jar $current_jarfile 1> aipal.out 2> aipal.err &
    echo -n $! > $pidfile
else
    echo "Tiedosto '$current_jarfile' puuttuu"
    exit 1
fi
