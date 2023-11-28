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

project_version=$(grep -m 1 '<version>' pom.xml | sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p') 


function extract_versions() {
    openmrs_version=$(echo "$project_version" | sed "s/\([0-9]*\.[0-9]*\).*$/\1/")
    
    # Extract the major and minor version numbers separately
    major_version=$(echo "$openmrs_version" | cut -d'.' -f1)
    minor_version=$(echo "$openmrs_version" | cut -d'.' -f2)
    
    # Increment the minor version by 1
    new_minor_version=$((minor_version + 1))
    
    # Create the new OpenMRS version
    new_openmrs_version="${major_version}.${new_minor_version}"

}

function generate_liquibase_snapshots_and_update() {
	cd liquibase/
	. scripts/create_liquibase_snapshots.sh "${1}" "${2}"
	. scripts/fix_liquibase_snapshots.sh
	cd ..
}

function move_updated_snapshots() {
	extract_versions
    mv liquibase/snapshots/liquibase-core-data-UPDATED-SNAPSHOT.xml api/src/main/resources/org/openmrs/liquibase/snapshots/core-data/"liquibase-core-data-$openmrs_version.x.xml"
    mv liquibase/snapshots/liquibase-schema-only-UPDATED-SNAPSHOT.xml api/src/main/resources/org/openmrs/liquibase/snapshots/schema-only/"liquibase-schema-only-$openmrs_version.x.xml"
}

function create_new_liquibase_update_file() {
	extract_versions
    new_liquibase_file="liquibase-update-to-latest-${new_openmrs_version}.x.xml"
    cp "api/src/main/resources/liquibase-update-to-latest-template.xml" "api/src/main/resources/org/openmrs/liquibase/updates/$new_liquibase_file"
    
    database_updater_class="api/src/test/java/org/openmrs/util/DatabaseUpdaterDatabaseIT.java"
    variable_name="CHANGE_SET_COUNT_FOR_GREATER_THAN_2_1_X"
    
    # Increase the variable value by 1
    sed -i "s/\($variable_name *= *\)[0-9]\+/\1$(( $(grep -o "$variable_name *= *[0-9]\+" "$database_updater_class" | awk '{print $NF}') + 1 ))/" "$database_updater_class"
    
    insert_line="	<include file=\"org/openmrs/liquibase/updates/$new_liquibase_file\"/>"
    sed -i -e '$!N;$!N;$i\'"$insert_line" "api/src/main/resources/liquibase-update-to-latest-from-1.9.x.xml"

}

function update_log_files_with_new_snapshots() {
	extract_versions
    sed -i 's/\(<include file="org\/openmrs\/liquibase\/snapshots\/schema-only\/liquibase-schema-only-\)\([^"]*\)\.x\.xml"\/>/\1'"$openmrs_version"'\.x.xml"\/>/' "api/src/main/resources/liquibase-schema-only.xml"
    sed -i 's/\(<include file="org\/openmrs\/liquibase\/snapshots\/core-data\/liquibase-core-data-\)\([^"]*\)\.x\.xml"\/>/\1'"$openmrs_version"'\.x.xml"\/>/' "api/src/main/resources/liquibase-core-data.xml"
    sed -i 's/\(<include file="org\/openmrs\/liquibase\/updates\/liquibase-update-to-latest-\)\([^"]*\)\.x\.xml"\/>/\1'"${new_openmrs_version}"'\.x.xml"\/>/' "api/src/main/resources/liquibase-update-to-latest.xml"
}

function update_snapshot_tests() {
	extract_versions
    change_log_java_file="api/src/main/java/org/openmrs/liquibase/ChangeLogVersions.java"
    sed -i '/private static final List<String> SNAPSHOT_VERSIONS = Arrays.asList(/s/);/,\ "'"${openmrs_version}.x"'"\);/' "$change_log_java_file"
    sed -i '/private static final List<String> UPDATE_VERSIONS = Arrays.asList(/s/);/,\ "'"${new_openmrs_version}.x"'"\);/' "$change_log_java_file"
}

function test_liquibase_snapshots() {
    cd liquibase/
    . scripts/test_liquibase_snapshots.sh "${1}" "${2}"
    cd ..
}

function echo_usage() {
	echo "usage: . liquibase/scripts/create_liquibase_snapshots.sh <username> <password>"
}



if [ "${1}" == "" ] || [ "${2}" == "" ]; then
	echo_usage
else
	echo "[INFO] generate liquibase snapshots"
	generate_liquibase_snapshots_and_update "${1}" "${2}"
	echo "[INFO] move updated snapshot files"
	move_updated_snapshots
	echo "[INFO] create new liquibase updated snapshots file"
	create_new_liquibase_update_file
	echo "[INFO] update the log files with new snapshot version"
	update_log_files_with_new_snapshots
	echo "[INFO] update the tests with new snapshot version"
	update_snapshot_tests
	echo "[INFO] generate liquibase snapshots successfully"
fi
#
