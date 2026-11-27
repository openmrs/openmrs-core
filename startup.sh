#!/bin/bash -aeu
#	This Source Code Form is subject to the terms of the Mozilla Public License, 
#	v. 2.0. If a copy of the MPL was not distributed with this file, You can 
#	obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under 
#	the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
#	
#	Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS 
#	graphic logo is a trademark of OpenMRS Inc.

source /openmrs/startup-init.sh

# OMRS_TRUSTED_PROXIES becomes a regular expression for the RemoteIpValve's internalProxies: each address escaped,
# with * as any number. It's checked first, and a bad entry stops startup: if Tomcat were given an invalid regular
# expression, the valve would keep its default and trust every private address.
TRUSTED_PROXIES_REGEX=""
if [ -n "${OMRS_TRUSTED_PROXIES:-}" ]; then
  IPV4_PATTERN='^([0-9]{1,3}|\*)\.([0-9]{1,3}|\*)\.([0-9]{1,3}|\*)\.([0-9]{1,3}|\*)$'
  IFS=',' read -r -a TRUSTED_PROXIES <<< "${OMRS_TRUSTED_PROXIES//[[:space:]]/}"
  for PROXY in ${TRUSTED_PROXIES[@]+"${TRUSTED_PROXIES[@]}"}; do
    if [ -z "$PROXY" ]; then
      continue
    fi
    # Built from the numbers rather than the text, so 010.0.0.5 gives 10\.0\.0\.5 (Java's form of the address)
    PROXY_REGEX=""
    if [[ "$PROXY" =~ $IPV4_PATTERN ]]; then
      for OCTET in "${BASH_REMATCH[@]:1}"; do
        if [ "$OCTET" = "*" ]; then
          OCTET_REGEX='[0-9]+'
        elif [ "$((10#$OCTET))" -le 255 ]; then
          OCTET_REGEX="$((10#$OCTET))"
        else
          PROXY_REGEX=""
          break
        fi
        PROXY_REGEX="${PROXY_REGEX:+${PROXY_REGEX}\\.}${OCTET_REGEX}"
      done
    fi
    if [ -z "$PROXY_REGEX" ]; then
      echo "ERROR: OMRS_TRUSTED_PROXIES must be IPv4 addresses separated by commas, with * for any number (e.g. 10.0.0.5,172.18.0.*), but has '${PROXY}'" >&2
      exit 1
    fi
    TRUSTED_PROXIES_REGEX="${TRUSTED_PROXIES_REGEX:+${TRUSTED_PROXIES_REGEX}|}${PROXY_REGEX}"
  done
  if [ -z "$TRUSTED_PROXIES_REGEX" ]; then
    echo "ERROR: OMRS_TRUSTED_PROXIES has no addresses: '${OMRS_TRUSTED_PROXIES}'" >&2
    exit 1
  fi
fi

echo "Waiting for database to initialize..."

/openmrs/wait-for-it.sh -t 3600 -h "${OMRS_DB_HOSTNAME}" -p "${OMRS_DB_PORT}"

wait_for_es()
{
	IFS=',' read -a es_uris <<< "${OMRS_SEARCH_ES_URIS}"
    
    EXIT_STATUS=1  
    while [ $EXIT_STATUS -ne 0 ]
    do
		for es_uri in "${es_uris[@]}"; do
			echo "Waiting for ES node ${es_uri} to initialize..."
			ELASTIC_SEARCH_HOST_PORT=(${es_uri//// })
			/openmrs/wait-for-it.sh -t 15 "${ELASTIC_SEARCH_HOST_PORT[1]}" 
			EXIT_STATUS=$?
			if [ $EXIT_STATUS -eq 0 ]; then
            	break 
            fi
            
		done
    done
    
    return 0
}

if [ "${OMRS_SEARCH}" = "elasticsearch" ]; then
	set +e
	echo "Waiting for ElasticSearch ${OMRS_SEARCH_ES_URIS} to initialize..."
	wait_for_es
	set -e
fi

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

# Copy preserving timestamps to avoid redeploys
cp -a "${OMRS_DISTRO_CORE}/." "${TOMCAT_WEBAPPS_DIR}"

echo "Writing out $TOMCAT_SETENV_FILE file"

JAVA_OPTS="$OMRS_JAVA_SERVER_OPTS"
CATALINA_OPTS="${OMRS_JAVA_MEMORY_OPTS} -DOPENMRS_INSTALLATION_SCRIPT=${OMRS_SERVER_PROPERTIES_FILE} -DOPENMRS_APPLICATION_DATA_DIRECTORY=${OMRS_DATA_DIR}/"

if [ -n "${OMRS_DEV_DEBUG_PORT-}" ]; then
  echo "Enabling debugging on port ${OMRS_DEV_DEBUG_PORT}"
  
  JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '/.*/ {print $1}')
  
  if [[ "$JAVA_VERSION" -gt "8" ]]; then
  	CATALINA_OPTS="$CATALINA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${OMRS_DEV_DEBUG_PORT}"
  else
  	CATALINA_OPTS="$CATALINA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${OMRS_DEV_DEBUG_PORT}"
  fi
fi

if [ "${OMRS_OTEL_ENABLED:-false}" = "true" ]; then
  echo "Enabling OpenTelemetry, exporting to ${OTEL_EXPORTER_OTLP_ENDPOINT:-the default OTLP endpoint}"
  CATALINA_OPTS="$CATALINA_OPTS -javaagent:/openmrs/java-agents/opentelemetry-javaagent.jar"
fi

# Single-quoted, so catalina.sh's eval is the only shell interpretation
SQ="'"
# Expanded unquoted below on purpose: bash 4.2 keeps the quotes of a quoted replacement
SQ_ESCAPED="'\\''"

# The trusted proxies, for the RemoteIpValve in server.xml (see the Dockerfile)
if [ -n "$TRUSTED_PROXIES_REGEX" ]; then
  if grep -q 'org.apache.catalina.valves.RemoteIpValve' "$TOMCAT_DIR/conf/server.xml"; then
    echo "Using X-Forwarded-For, X-Forwarded-Proto and X-Forwarded-Port from the proxies in OMRS_TRUSTED_PROXIES (${OMRS_TRUSTED_PROXIES})"
  else
    echo "WARNING: OMRS_TRUSTED_PROXIES is set, but $TOMCAT_DIR/conf/server.xml has no RemoteIpValve, so it has no effect"
  fi
  # In single quotes, so catalina.sh's eval keeps the expression's backslashes and | as they are
  CATALINA_OPTS="$CATALINA_OPTS -Dopenmrs.remoteIp.internalProxies='${TRUSTED_PROXIES_REGEX}'"
fi

{
  printf "export JAVA_OPTS='%s'\n" "${JAVA_OPTS//"$SQ"/$SQ_ESCAPED}"
  printf "export CATALINA_OPTS='%s'\n" "${CATALINA_OPTS//"$SQ"/$SQ_ESCAPED}"
} > "$TOMCAT_SETENV_FILE"

echo "Starting up OpenMRS..."

exec /usr/local/tomcat/bin/catalina.sh run
