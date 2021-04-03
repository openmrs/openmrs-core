#!/bin/bash
#
#  This Source Code Form is subject to the terms of the Mozilla Public License,
#  v. 2.0. If a copy of the MPL was not distributed with this file, You can
#  obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
#  the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
#
#  Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
#  graphic logo is a trademark of OpenMRS Inc.
#

#
#  This script drops an existing OpenMRS database, creates an empty one and uses 
#  Liquibase snapshots to create the OpenMRS schena and insert data into tables. 
#
#  Do NOT use this script on a production database as it drops the database.
#
#  Run this script from the openmrs-core/liquibase folder.
#
#  The snapshots are written to the openmrs-core/liquibase/snapshots folder.
#

function drop_and_create_database() {
	echo "[INFO] dropping OpenMRS database..."
	mysql -u "${1}" -p"${2}" < scripts/drop_openmrs_schema.sql

	echo "[INFO] creating an empty OpenMRS database..."
	mysql -u "${1}" -p"${2}" < scripts/create_openmrs_database.sql		
}

function update_database() {
	mvn \
	  -Dchangelogfile=liquibase-schema-only-UPDATED-SNAPSHOT.xml  \
	  -Dusername="${1}" \
	  -Dpassword="${2}" \
	  liquibase:update

	mvn \
	  -Dchangelogfile=liquibase-core-data-UPDATED-SNAPSHOT.xml  \
	  -Dusername="${1}" \
	  -Dpassword="${2}" \
	  liquibase:update
}

function echo_usage() {
	echo "usage: . scripts/test_liquibase_snapshots.sh <username> <password>"
}

if [ "${1}" == "" ] || [ "${2}" == "" ]; then
	echo_usage
else
	drop_and_create_database "${1}" "${2}"
	update_database "${1}" "${2}"
fi

