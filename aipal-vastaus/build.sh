#!/bin/bash

# Kääntää ja paketoi Aituhaun version.
#
# Käyttö:
#
#     ./build.sh [-t]
#
# Parametrit:
#     -t              Jos annettu, aja yksikkötestit.

set -eu

run_tests=no
while getopts 'ct' o; do
    case $o in
        t)
            run_tests=yes
            ;;
    esac
done

repo_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

set -x

cd $repo_path/frontend
npm install
rm -rf src/bower_components
bower install
grunt build
if [ "$run_tests" = yes ]; then
   grunt test_ff --no-color
fi

cd $repo_path
if [ "$run_tests" = yes ]; then
    lein do test, clean, uberjar
else
    lein do clean, uberjar
fi

