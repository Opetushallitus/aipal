#!/bin/bash
set -eu

REPO_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

set -x

cd $REPO_PATH/aipal
./build.sh
aipal_jar=$REPO_PATH/aipal/target/aipal-standalone.jar

cd $REPO_PATH/aipal-db
./build.sh
aipal_db_jar=$REPO_PATH/aipal-db/target/aipal-db-standalone.jar

chmod go= $REPO_PATH/env/ssh/dev_id_rsa
cd $REPO_PATH/aipal

export AIPAL_DB_USER=aipal_user
export AIPAL_SSH_KEY=$REPO_PATH/env/ssh/dev_id_rsa
./deploy.sh -c -t $aipal_jar $aipal_db_jar aipaladmin@192.168.50.62
