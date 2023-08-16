#!/bin/bash -aeu
#	This Source Code Form is subject to the terms of the Mozilla Public License, 
#	v. 2.0. If a copy of the MPL was not distributed with this file, You can 
#	obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under 
#	the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
#	
#	Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS 
#	graphic logo is a trademark of OpenMRS Inc.

# ----------------------------------
# Configurable environment variables
OMRS_ADD_DEMO_DATA=${OMRS_ADD_DEMO_DATA:-false}
OMRS_ADMIN_USER_PASSWORD=${OMRS_ADMIN_USER_PASSWORD:-Admin123}
OMRS_ADMIN_PASSWORD_LOCKED=${OMRS_ADMIN_PASSWORD_LOCKED:-false}
# When set to 'true' all DB updates are applied automatically upon startup
OMRS_AUTO_UPDATE_DATABASE=${OMRS_AUTO_UPDATE_DATABASE:-true}
OMRS_CREATE_DATABASE_USER=${OMRS_CREATE_DATABASE_USER:-false}
OMRS_CREATE_TABLES=${OMRS_CREATE_TABLES:-true}
OMRS_DEV_DEBUG_PORT=${OMRS_DEV_DEBUG_PORT:-}
OMRS_DB=${OMRS_DB:-'mysql'}
# Defaults provided for OMRS_DB mysql and postgresql
OMRS_DB_ARGS=${OMRS_DB_ARGS:-}
# Additional DB url arguments
OMRS_DB_EXTRA_ARGS=${OMRS_DB_EXTRA_ARGS:-}
OMRS_DB_HOSTNAME=${OMRS_DB_HOSTNAME:-'localhost'}
OMRS_DB_JDBC_PROTOCOL=${OMRS_DB_JDBC_PROTOCOL:-}
OMRS_DB_NAME=${OMRS_DB_NAME:-'openmrs'}
OMRS_DB_PASSWORD=${OMRS_DB_PASSWORD:-'openmrs'}
OMRS_DB_PORT=${OMRS_DB_PORT:-3306}
# Overrides OMRS_DB_ARGS, OMRS_DB_EXTRA_ARGS, OMRS_DB_HOSTNAME, OMRS_DB_PORT, OMRS_DB_JDBC_PROTOCOL, OMRS_DB_NAME
OMRS_DB_URL=${OMRS_DB_URL:-}
OMRS_DB_USERNAME=${OMRS_DB_USERNAME:-'openmrs'}
OMRS_HAS_CURRENT_OPENMRS_DATABASE=${OMRS_HAS_CURRENT_OPENMRS_DATABASE:-true}
OMRS_HOME=${OMRS_HOME:-'/openmrs'}
# When Set to 'auto' disables installation wizard
OMRS_INSTALL_METHOD=${OMRS_INSTALL_METHOD:-'auto'}
# JVM startup parameters
OMRS_JAVA_SERVER_OPTS=${OMRS_JAVA_SERVER_OPTS:-'-Dfile.encoding=UTF-8 -server -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Djava.awt.headlesslib=true'}
# Appended to JAVA_SERVER_OPTS
OMRS_JAVA_MEMORY_OPTS=${OMRS_JAVA_MEMORY_OPTS:-'-XX:NewSize=128m'}
# Enables modules' installation/update/removal via web UI
OMRS_MODULE_WEB_ADMIN=${OMRS_MODULE_WEB_ADMIN:-true}
# This should match the name of the distribution supplied OpenMRS war file
OMRS_WEBAPP_NAME=${OMRS_WEBAPP_NAME:-'openmrs'}
# End of configurable environment variables
# -----------------------------------------

# Support deprecated environment names
[ -n "${OMRS_CONFIG_ADD_DEMO_DATA:-}" ] && OMRS_ADD_DEMO_DATA=${OMRS_CONFIG_ADD_DEMO_DATA}
[ -n "${OMRS_CONFIG_ADMIN_USER_PASSWORD:-}" ] && OMRS_ADMIN_USER_PASSWORD=${OMRS_CONFIG_ADMIN_USER_PASSWORD}
[ -n "${OMRS_CONFIG_AUTO_UPDATE_DATABASE:-}" ] && OMRS_AUTO_UPDATE_DATABASE=${OMRS_CONFIG_AUTO_UPDATE_DATABASE}
[ -n "${OMRS_CONFIG_CREATE_DATABASE_USER:-}" ] && OMRS_CREATE_DATABASE_USER=${OMRS_CONFIG_CREATE_DATABASE_USER}
[ -n "${OMRS_CONFIG_CREATE_TABLES:-}" ] && OMRS_CREATE_TABLES=${OMRS_CONFIG_CREATE_TABLES}
[ -n "${OMRS_CONFIG_DATABASE:-}" ] && OMRS_DB=${OMRS_CONFIG_DATABASE}
[ -n "${OMRS_CONFIG_CONNECTION_ARGS:-}" ] && OMRS_DB_ARGS=${OMRS_CONFIG_CONNECTION_ARGS}
[ -n "${OMRS_CONFIG_CONNECTION_EXTRA_ARGS:-}" ] && OMRS_DB_EXTRA_ARGS=${OMRS_CONFIG_CONNECTION_EXTRA_ARGS}
[ -n "${OMRS_CONFIG_CONNECTION_SERVER:-}" ] && OMRS_DB_HOSTNAME=${OMRS_CONFIG_CONNECTION_SERVER}
[ -n "${OMRS_CONFIG_JDBC_URL_PROTOCOL:-}" ] && OMRS_DB_JDBC_PROTOCOL=${OMRS_CONFIG_JDBC_URL_PROTOCOL}
[ -n "${OMRS_CONFIG_CONNECTION_DATABASE:-}" ] && OMRS_DB_NAME=${OMRS_CONFIG_CONNECTION_DATABASE}
[ -n "${OMRS_CONFIG_CONNECTION_PASSWORD:-}" ] && OMRS_DB_PASSWORD=${OMRS_CONFIG_CONNECTION_PASSWORD}
[ -n "${OMRS_CONFIG_CONNECTION_PORT:-}" ] && OMRS_DB_PORT=${OMRS_CONFIG_CONNECTION_PORT}
[ -n "${OMRS_CONFIG_CONNECTION_URL:-}" ] && OMRS_DB_URL=${OMRS_CONFIG_CONNECTION_URL}
[ -n "${OMRS_CONFIG_CONNECTION_USERNAME:-}" ] && OMRS_DB_USERNAME=${OMRS_CONFIG_CONNECTION_USERNAME}
[ -n "${OMRS_CONFIG_HAS_CURRENT_OPENMRS_DATABASE:-}" ] && OMRS_HAS_CURRENT_OPENMRS_DATABASE=${OMRS_CONFIG_HAS_CURRENT_OPENMRS_DATABASE}
[ -n "${OMRS_CONFIG_INSTALL_METHOD:-}" ] && OMRS_INSTALL_METHOD=${OMRS_CONFIG_INSTALL_METHOD}
[ -n "${OMRS_CONFIG_MODULE_WEB_ADMIN:-}" ] && OMRS_MODULE_WEB_ADMIN=${OMRS_CONFIG_MODULE_WEB_ADMIN}

echo "Initiating OpenMRS startup"

# This startup script is responsible for fully preparing the OpenMRS Tomcat environment.
# A volume mount is expected that contains distribution artifacts. The expected format is shown in these environment vars.

OMRS_DISTRO_DIR="$OMRS_HOME/distribution"
OMRS_DISTRO_CORE="$OMRS_DISTRO_DIR/openmrs_core"
OMRS_DISTRO_MODULES="$OMRS_DISTRO_DIR/openmrs_modules"
OMRS_DISTRO_OWAS="$OMRS_DISTRO_DIR/openmrs_owas"
OMRS_DISTRO_CONFIG="$OMRS_DISTRO_DIR/openmrs_config"

# Each of these mounted directories are used to populate expected configurations on the server, defined here

OMRS_DATA_DIR="$OMRS_HOME/data"
OMRS_MODULES_DIR="$OMRS_DATA_DIR/modules"
OMRS_OWA_DIR="$OMRS_DATA_DIR/owa"
OMRS_CONFIG_DIR="$OMRS_DATA_DIR/configuration"

OMRS_SERVER_PROPERTIES_FILE="$OMRS_HOME/$OMRS_WEBAPP_NAME-server.properties"
OMRS_RUNTIME_PROPERTIES_FILE="$OMRS_DATA_DIR/$OMRS_WEBAPP_NAME-runtime.properties"

echo "Deleting modules, OWAs and configuration from OpenMRS"

rm -fR "${OMRS_MODULES_DIR:?}/*"
rm -fR "${OMRS_OWA_DIR:?}/*"
rm -fR "${OMRS_CONFIG_DIR:?}/*"

echo "Loading distribution artifacts into OpenMRS"

[ -d "$OMRS_DISTRO_MODULES" ] && cp -R "$OMRS_DISTRO_MODULES/." "$OMRS_MODULES_DIR"
[ -d "$OMRS_DISTRO_OWAS" ] && cp -R "$OMRS_DISTRO_OWAS/." "$OMRS_OWA_DIR"
[ -d "$OMRS_DISTRO_CONFIG" ] && cp -R "$OMRS_DISTRO_CONFIG/." "$OMRS_CONFIG_DIR"

# Setup database configuration properties
if [[ -z $OMRS_DB || "$OMRS_DB" == "mysql" ]]; then
  OMRS_DB_JDBC_PROTOCOL=${OMRS_DB_JDBC_PROTOCOL:-mysql}
  OMRS_DB_DRIVER_CLASS=${OMRS_DB_DRIVER_CLASS:-com.mysql.jdbc.Driver}
  OMRS_DB_PORT=${OMRS_DB_PORT:-3306}
  OMRS_DB_ARGS="${OMRS_DB_ARGS:-?autoReconnect=true&sessionVariables=default_storage_engine=InnoDB&useUnicode=true&characterEncoding=UTF-8}"
elif [[ "$OMRS_DB" == "postgresql" ]]; then
  OMRS_DB_JDBC_PROTOCOL=${OMRS_DB_JDBC_PROTOCOL:-postgresql}
  OMRS_DB_DRIVER_CLASS=${OMRS_DB_DRIVER_CLASS:-org.postgresql.Driver}
  OMRS_DB_PORT=${OMRS_DB_PORT:-5432}
else
  echo "Unknown database type $OMRS_DB. Using properties for MySQL"
  OMRS_DB_JDBC_PROTOCOL=${OMRS_DB_JDBC_PROTOCOL:-mysql}
  OMRS_DB_DRIVER_CLASS=${OMRS_DB_DRIVER_CLASS:-com.mysql.jdbc.Driver}
  OMRS_DB_PORT=${OMRS_DB_PORT:-3306}
  OMRS_DB_ARGS="${OMRS_DB_ARGS:-?autoReconnect=true&sessionVariables=default_storage_engine=InnoDB&useUnicode=true&characterEncoding=UTF-8}"
fi

# Build the JDBC URL using the above properties
OMRS_DB_URL="${OMRS_DB_URL:-jdbc:${OMRS_DB_JDBC_PROTOCOL}://${OMRS_DB_HOSTNAME}:${OMRS_DB_PORT}/${OMRS_DB_NAME}${OMRS_DB_ARGS}${OMRS_DB_EXTRA_ARGS}}"

echo "Writing out $OMRS_SERVER_PROPERTIES_FILE"

cat > $OMRS_SERVER_PROPERTIES_FILE << EOF
add_demo_data=${OMRS_ADD_DEMO_DATA}
admin_user_password=${OMRS_ADMIN_USER_PASSWORD}
admin_password_locked=${OMRS_ADMIN_PASSWORD_LOCKED}
auto_update_database=${OMRS_AUTO_UPDATE_DATABASE}
connection.driver_class=${OMRS_DB_DRIVER_CLASS}
connection.username=${OMRS_DB_USERNAME}
connection.password=${OMRS_DB_PASSWORD}
connection.url=${OMRS_DB_URL}
create_database_user=${OMRS_CREATE_DATABASE_USER}
create_tables=${OMRS_CREATE_TABLES}
has_current_openmrs_database=${OMRS_HAS_CURRENT_OPENMRS_DATABASE}
install_method=${OMRS_INSTALL_METHOD}
module_web_admin=${OMRS_MODULE_WEB_ADMIN}
module.allow_web_admin=${OMRS_MODULE_WEB_ADMIN}

EOF


# Supports any custom env variable with the OMRS_EXTRA_ prefix, which translates to a property without the 
# OMRS_EXTRA_ prefix. The '_' is replaced with '.' and '__' with '_'.
EXTRA_VARS=(${!OMRS_EXTRA_@})
if [[ -n "${EXTRA_VARS+x}" ]]; then 
	EXTRA_PROPERTIES=""
	for i in "${EXTRA_VARS[@]}"
	do
	  :
	  var=$(echo "${i#OMRS_EXTRA_}" | tr [:upper:] [:lower:])
	  var=${var//_/.}
	  var=${var//../_}
	  EXTRA_PROPERTIES+="${var}=${!i} \n"
	done
	
	echo -e "$EXTRA_PROPERTIES" >> "$OMRS_SERVER_PROPERTIES_FILE"
fi

cat "$OMRS_SERVER_PROPERTIES_FILE"

if [ -f "$OMRS_RUNTIME_PROPERTIES_FILE" ]; then
  echo "Found existing runtime properties file at $OMRS_RUNTIME_PROPERTIES_FILE. Merging with $OMRS_SERVER_PROPERTIES_FILE"
  awk -F= '!a[$1]++' "$OMRS_SERVER_PROPERTIES_FILE" "$OMRS_RUNTIME_PROPERTIES_FILE" > openmrs-merged.properties
  cp openmrs-merged.properties "$OMRS_RUNTIME_PROPERTIES_FILE"
  cat "$OMRS_RUNTIME_PROPERTIES_FILE"
fi

