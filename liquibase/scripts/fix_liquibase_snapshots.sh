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
#  This script fixes Liquibase snapshots by changing selected data types and re-ordering inserts etc.  
#
#  Run this script from the openmrs-core/liquibase folder.
#
#  The updated snapshots are written to the openmrs-core/liquibase/snapshots folder.
#

openmrs_version=2.4.0-SNAPSHOT

java -jar ./target/openmrs-liquibase-${openmrs_version}-jar-with-dependencies.jar
