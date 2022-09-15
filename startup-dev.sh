#!/bin/bash -eu
#	This Source Code Form is subject to the terms of the Mozilla Public License, 
#	v. 2.0. If a copy of the MPL was not distributed with this file, You can 
#	obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under 
#	the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
#	
#	Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS 
#	graphic logo is a trademark of OpenMRS Inc.

source startup-init.sh

if [ -n "${OMRS_DEV_DEBUG_PORT-}" ]; then
  echo "Enabling debugging on port $OMRS_DEV_DEBUG_PORT"
  export MAVEN_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=$OMRS_DEV_DEBUG_PORT"
fi

echo "Building OpenMRS..."

OMRS_SKIP_BUILD=${OMRS_SKIP_BUILD:-false}
if [ "${OMRS_SKIP_BUILD}" = "false" ]; then
mvn install -Pskip-all-checks -o 
fi	

echo "Starting up OpenMRS..."

cd webapp

mvn jetty:run -o -DOPENMRS_INSTALLATION_SCRIPT=$OMRS_SERVER_PROPERTIES_FILE -DOPENMRS_APPLICATION_DATA_DIRECTORY=$OMRS_DATA_DIR &

# Trigger first filter to start data import
sleep 15
curl -sL "http://localhost:8080/$OMRS_WEBAPP_NAME/" > /dev/null
sleep 15

# Bring tomcat process to foreground again
wait ${!}
