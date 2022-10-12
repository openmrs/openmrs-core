#!/bin/bash -eu
set -e
#    This Source Code Form is subject to the terms of the Mozilla Public License,
#    v. 2.0. If a copy of the MPL was not distributed with this file, You can
#    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
#    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
#
#    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
#    graphic logo is a trademark of OpenMRS Inc.

# This startup script is responsible for fully preparing the OpenMRS development Tomcat environment.

source /usr/local/tomcat/startup-init.sh

export MODULE_SOURCE_DIR="$OMRS_HOME/module/source"

# Download required modules for development
OMRS_REFRESH_MODULES=${OMRS_REFRESH_MODULES:-false}
if [ "$OMRS_REFRESH_MODULES" == true ]; then
	source "$TOMCAT_DIR/deploy-module.sh --refresh"
else
	source "$TOMCAT_DIR/deploy-module.sh"
fi

# Build module src
mvn clean install -f "$MODULE_SOURCE_DIR/pom.xml" -DskipTests

find "$MODULE_SOURCE_DIR/omod/target" -name \*.omod -exec cp {} "$OMRS_MODULES_DIR" \;

# Loading artifacts into appropriate locations
cp -r /openmrs/openmrs_core/. "$TOMCAT_WEBAPPS_DIR"

echo "Starting up OpenMRS..."

/usr/local/tomcat/bin/catalina.sh run &

# Trigger first filter to start data importation
sleep 10
curl -sL "http://localhost:8080/$OMRS_WEBAPP_NAME/" >/dev/null
sleep 10

# bring tomcat process to foreground again
wait ${!}
