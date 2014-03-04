#!/bin/bash

# Sammuttaa ajossa olevan AIPAL-prosessin. Ajettava käyttäjänä,
# jolla on riittävät oikeudet AIPAL-prosessin tappamiseen.
#
# Käyttö:
#
#     ./stop-aipal.sh

set -eu

pidfile='aipal.pid'

if [ -a $pidfile ]
then
    echo "Stopping Aipal..."
    kill `cat $pidfile` && rm $pidfile
fi
