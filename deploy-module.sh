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

OPENMRS_SDK_PLUGIN="org.openmrs.maven.plugins:openmrs-sdk-maven-plugin"

OMRS_MODULE_PROPERTIES=${OMRS_MODULE_PROPERTIES:-"$MODULE_SOURCE_DIR/module.properties"}
OMRS_REFRESH_MODULES=${OMRS_REFRESH_MODULES:-false}
RE_REFRESH_MODULES=0

PARAMS=""
while (("$#")); do
	case "$1" in
	-r | --refresh)
		RE_REFRESH_MODULES=1
		shift
		;;
#	-f | --filePath)
#		if [ -n "$2" ] && [ "${2:0:1}" != "-" ]; then
#			MODULE_PROPERTIES_FILE_PATH=$2
#			shift 2
#		else
#			echo "Error: Argument for $1 is missing" >&2
#			exit 1
#		fi
#		;;
	-* | --*=) # unsupported flags
		echo "Error: Unsupported flag $1" >&2
		exit 1
		;;
	*) # preserve positional arguments
		PARAMS="$PARAMS $1"
		shift
		;;
	esac
done
# set positional arguments in their proper place
eval set -- "$PARAMS"

download_artifacts() {
	DISTRO_BUILD_OUTPUTS=$(mktemp -d -t ci-"$(date +%Y-%m-%d-%H-%M-%S)"-XXXXXXXXXX)
	if [ -d "$DISTRO_BUILD_OUTPUTS" ]; then
		rm -R "${DISTRO_BUILD_OUTPUTS:?/*}"
	else
		mkdir -p "$DISTRO_BUILD_OUTPUTS"
	fi
	# download modules
	mvn ${OPENMRS_SDK_PLUGIN}:build-distro -Ddistro="${OMRS_MODULE_PROPERTIES}" -Ddir="$DISTRO_BUILD_OUTPUTS" -B
	# copy downloaded modules to $$OMRS_MODULES_DIR} directory
	cp -r "$DISTRO_BUILD_OUTPUTS"/web/modules/* "$OMRS_MODULES_DIR"
	# clean up artifacts
	rm -rf "$DISTRO_BUILD_OUTPUTS"
}

prepare_modules_dir() {
	if [ -d "$OMRS_MODULES_DIR" ]; then
		if [[ $RE_REFRESH_MODULES == 1 ]]; then
			echo "Re-downloading OpenMRS modules..."
			# remove contents
			rm "$OMRS_MODULES_DIR"/*
			download_artifacts
		else
			if [ "$(ls -A "$OMRS_MODULES_DIR")" ]; then
				echo "required modules for development"
				ls "$OMRS_MODULES_DIR"
			else
				#if empty download the modules
				download_artifacts
			fi
		fi
	else
		echo "Creating $OMRS_MODULES_DIR directory..."
		mkdir -p "$OMRS_MODULES_DIR"
		download_artifacts
	fi
}

#If module.properties is not provided, then no required modules needed!
if [[ -f ${OMRS_MODULE_PROPERTIES} ]]; then
	prepare_modules_dir
else
	echo "Specified required modules in the ${OMRS_MODULE_PROPERTIES} file."
fi
