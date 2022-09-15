#!/bin/bash -aeu
#	This Source Code Form is subject to the terms of the Mozilla Public License, 
#	v. 2.0. If a copy of the MPL was not distributed with this file, You can 
#	obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under 
#	the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
#	
#	Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS 
#	graphic logo is a trademark of OpenMRS Inc.

[[ -z "${OMRS_BUILD_CMD:-}" ]] && OMRS_BUILD_CMD="mvn"
[[ -z "${OMRS_BUILD_GOALS:-}" ]] && OMRS_BUILD_GOALS="install"
[[ -z "${OMRS_BUILD_ARGS:-}" ]] && OMRS_BUILD_ARGS="-DskipTests -Pskip-all-checks"
[[ -z "${OMRS_BUILD:-}" ]] && OMRS_BUILD="false"

if [ "${OMRS_BUILD}" == "true" ]; then
	echo "Building the project..."
	${OMRS_BUILD_CMD} ${OMRS_BUILD_GOALS} ${OMRS_BUILD_ARGS}
	[[ -e /openmrs_core/webapp/target/openmrs.war ]] && cp /openmrs_core/webapp/target/openmrs.war /openmrs/distribution/openmrs_core/
fi	

source /openmrs/startup.sh
