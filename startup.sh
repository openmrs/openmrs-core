#!/bin/bash -aeu
#	This Source Code Form is subject to the terms of the Mozilla Public License, 
#	v. 2.0. If a copy of the MPL was not distributed with this file, You can 
#	obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under 
#	the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
#	
#	Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS 
#	graphic logo is a trademark of OpenMRS Inc.

source /openmrs/startup-init.sh

echo "Waiting for database to initialize..."

/openmrs/wait-for-it.sh -t 3600 -h "${OMRS_DB_HOSTNAME}" -p "${OMRS_DB_PORT}"

TOMCAT_DIR="/usr/local/tomcat"
TOMCAT_WEBAPPS_DIR="$TOMCAT_DIR/webapps"
TOMCAT_WORK_DIR="$TOMCAT_DIR/work"
TOMCAT_TEMP_DIR="$TOMCAT_DIR/temp"
TOMCAT_SETENV_FILE="$TOMCAT_DIR/bin/setenv.sh"

echo "Clearing out Tomcat directories"

rm -fR "${TOMCAT_WEBAPPS_DIR:?}/*"
rm -fR "${TOMCAT_WORK_DIR:?}/*"
rm -fR "${TOMCAT_TEMP_DIR:?}/*"

echo "Loading WAR into appropriate location"

cp -r "${OMRS_DISTRO_CORE}/." "${TOMCAT_WEBAPPS_DIR}"

echo "Writing out $TOMCAT_SETENV_FILE file"

JAVA_OPTS="$OMRS_JAVA_SERVER_OPTS"
CATALINA_OPTS="${OMRS_JAVA_MEMORY_OPTS} -DOPENMRS_INSTALLATION_SCRIPT=${OMRS_SERVER_PROPERTIES_FILE} -DOPENMRS_APPLICATION_DATA_DIRECTORY=${OMRS_DATA_DIR}/"

if [ -n "${OMRS_DEV_DEBUG_PORT-}" ]; then
  echo "Enabling debugging on port ${OMRS_DEV_DEBUG_PORT}"
  CATALINA_OPTS="$CATALINA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${OMRS_DEV_DEBUG_PORT}"
fi

cat > $TOMCAT_SETENV_FILE << EOF
export JAVA_OPTS="$JAVA_OPTS"
export CATALINA_OPTS="$CATALINA_OPTS"
EOF

echo "Starting up OpenMRS..."

/usr/local/tomcat/bin/catalina.sh run &

# Trigger first filter to start data import
sleep 15
curl -sL "http://localhost:8080/${OMRS_WEBAPP_NAME}/" > /dev/null
sleep 15

# Bring tomcat process to foreground again
wait ${!}
