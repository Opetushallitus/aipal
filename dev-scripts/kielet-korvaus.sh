#!/bin/bash
while read -r line; do set $line; key=$1; gsed -i "/^$key /c\\${line//\\/\\\\}" ./aipal/resources/i18n/tekstit_sv.properties; done
