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
# JVM startup parameters, read by the shell like Tomcat's JAVA_OPTS: quote special characters, e.g. -Dkey='a$b'
OMRS_JAVA_SERVER_OPTS=${OMRS_JAVA_SERVER_OPTS:-'-Dfile.encoding=UTF-8 -server -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Djava.awt.headlesslib=true'}
# Appended to JAVA_SERVER_OPTS
OMRS_JAVA_MEMORY_OPTS=${OMRS_JAVA_MEMORY_OPTS:-'-XX:NewSize=128m'}
# Enables modules' installation/update/removal via web UI
OMRS_MODULE_WEB_ADMIN=${OMRS_MODULE_WEB_ADMIN:-true}
# This should match the name of the distribution supplied OpenMRS war file
OMRS_WEBAPP_NAME=${OMRS_WEBAPP_NAME:-'openmrs'}
# Can be set to lucene or elasticsearch
OMRS_SEARCH=${OMRS_SEARCH:-'lucene'}
OMRS_SEARCH_CONFIG=${OMRS_SEARCH_CONFIG:-"${OMRS_SEARCH}Config"}
OMRS_SEARCH_ES_URIS=${OMRS_SEARCH_ES_URIS:-'http://es:9200'}
# End of configurable environment variables
# -----------------------------------------

# Support deprecated environment names
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
OMRS_DISTRO_FRONTEND="$OMRS_DISTRO_DIR/openmrs_spa"

# Each of these mounted directories are used to populate expected configurations on the server, defined here

OMRS_DATA_DIR="$OMRS_HOME/data"
OMRS_MODULES_DIR="$OMRS_DATA_DIR/modules"
OMRS_OWA_DIR="$OMRS_DATA_DIR/owa"
OMRS_CONFIG_DIR="$OMRS_DATA_DIR/configuration"
OMRS_FRONTEND_DIR="$OMRS_DATA_DIR/frontend"

OMRS_SERVER_PROPERTIES_FILE="$OMRS_HOME/$OMRS_WEBAPP_NAME-server.properties"
OMRS_RUNTIME_PROPERTIES_FILE="$OMRS_DATA_DIR/$OMRS_WEBAPP_NAME-runtime.properties"

echo "Deleting modules, OWAs, configuration and frontend from OpenMRS"

rm -fR "${OMRS_MODULES_DIR:?}"
mkdir "${OMRS_MODULES_DIR}"
rm -fR "${OMRS_OWA_DIR:?}"
mkdir "${OMRS_OWA_DIR}"
rm -fR "${OMRS_CONFIG_DIR:?}"
mkdir "${OMRS_CONFIG_DIR}"
rm -fR "${OMRS_FRONTEND_DIR:?}"
mkdir "${OMRS_FRONTEND_DIR}"

echo "Loading distribution artifacts into OpenMRS"

# Copy preserving timestamps to avoid redeploys
[ -d "$OMRS_DISTRO_MODULES" ] && cp -a "$OMRS_DISTRO_MODULES/." "$OMRS_MODULES_DIR"
[ -d "$OMRS_DISTRO_OWAS" ] && cp -a "$OMRS_DISTRO_OWAS/." "$OMRS_OWA_DIR"
[ -d "$OMRS_DISTRO_CONFIG" ] && cp -a "$OMRS_DISTRO_CONFIG/." "$OMRS_CONFIG_DIR"
[ -d "$OMRS_DISTRO_FRONTEND" ] && cp -a "$OMRS_DISTRO_FRONTEND/." "$OMRS_FRONTEND_DIR"

# Setup database configuration properties
if [[ -z $OMRS_DB || "$OMRS_DB" == "mysql" ]]; then
  OMRS_DB_JDBC_PROTOCOL=${OMRS_DB_JDBC_PROTOCOL:-mysql}
  OMRS_DB_DRIVER_CLASS=${OMRS_DB_DRIVER_CLASS:-com.mysql.jdbc.Driver}
  OMRS_DB_PORT=${OMRS_DB_PORT:-3306}
  OMRS_DB_ARGS="${OMRS_DB_ARGS:-?autoReconnect=true&sessionVariables=default_storage_engine=InnoDB&useUnicode=true&characterEncoding=UTF-8}"
elif [[ "$OMRS_DB" == "mariadb" ]]; then
  OMRS_DB_JDBC_PROTOCOL=${OMRS_DB_JDBC_PROTOCOL:-mariadb}
  OMRS_DB_DRIVER_CLASS=${OMRS_DB_DRIVER_CLASS:-org.mariadb.jdbc.Driver}
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

# Escapes a value for a Java properties file, so that OpenMRS reads it exactly as given: backslashes, line breaks
# and leading whitespace would otherwise be interpreted or dropped
escape_property() {
  local value=$1 bs='\'
  value=${value//"$bs"/"$bs$bs"}
  value=${value//$'\n'/"${bs}n"}
  value=${value//$'\r'/"${bs}r"}
  if [[ "$value" == [[:space:]]* ]]; then
    value="${bs}${value}"
  fi
  printf '%s' "$value"
}
# allexport (-a) exports functions too; keep this one out of Tomcat's environment
export -fn escape_property

echo "Writing out $OMRS_SERVER_PROPERTIES_FILE"

cat > $OMRS_SERVER_PROPERTIES_FILE << EOF
admin_user_password=$(escape_property "${OMRS_ADMIN_USER_PASSWORD}")
admin_password_locked=$(escape_property "${OMRS_ADMIN_PASSWORD_LOCKED}")
auto_update_database=$(escape_property "${OMRS_AUTO_UPDATE_DATABASE}")
connection.driver_class=$(escape_property "${OMRS_DB_DRIVER_CLASS}")
connection.username=$(escape_property "${OMRS_DB_USERNAME}")
connection.password=$(escape_property "${OMRS_DB_PASSWORD}")
connection.url=$(escape_property "${OMRS_DB_URL}")
create_database_user=$(escape_property "${OMRS_CREATE_DATABASE_USER}")
create_tables=$(escape_property "${OMRS_CREATE_TABLES}")
has_current_openmrs_database=$(escape_property "${OMRS_HAS_CURRENT_OPENMRS_DATABASE}")
install_method=$(escape_property "${OMRS_INSTALL_METHOD}")
module_web_admin=$(escape_property "${OMRS_MODULE_WEB_ADMIN}")
module.allow_web_admin=$(escape_property "${OMRS_MODULE_WEB_ADMIN}")

property.hibernate.search.backend.type=$(escape_property "${OMRS_SEARCH}")
property.hibernate.search.backend.analysis.configurer=$(escape_property "${OMRS_SEARCH_CONFIG}")
property.hibernate.search.backend.uris=$(escape_property "${OMRS_SEARCH_ES_URIS}")
property.hibernate.search.backend.discovery.enabled=true

EOF

# Supports any custom env variable with the OMRS_EXTRA_ prefix, which translates to a property without the 
# OMRS_EXTRA_ prefix. The '_' is replaced with '.' and '__' with '_'. An all upper case name is lowercased,
# while a name containing any lower case letter keeps its case, e.g. OMRS_EXTRA_mail_smtp_socketFactory_class
# becomes mail.smtp.socketFactory.class. If several variables give the same property, the first in sorted
# order is used and the others are ignored with a warning.
EXTRA_VARS=(${!OMRS_EXTRA_@})
if [[ -n "${EXTRA_VARS+x}" ]]; then
	EXTRA_PROPERTIES=()
	declare -A EXTRA_PROPERTY_SOURCES=()
	for i in "${EXTRA_VARS[@]}"
	do
	  :
	  var=${i#OMRS_EXTRA_}
	  if [[ -z "$var" ]]; then
	    echo "Warning: $i does not name a property. Ignoring it."
	    continue
	  fi
	  if [[ "$var" != *[[:lower:]]* ]]; then
	    # tr with ASCII ranges, as ${var,,} follows the locale (e.g. I becomes ı under tr_TR)
	    var=$(tr 'A-Z' 'a-z' <<< "$var")
	  fi
	  var=${var//_/.}
	  var=${var//../_}
	  if [[ -n "${EXTRA_PROPERTY_SOURCES[$var]+x}" ]]; then
	    echo "Warning: $i and ${EXTRA_PROPERTY_SOURCES[$var]} both set property $var. Ignoring $i."
	    continue
	  fi
	  EXTRA_PROPERTY_SOURCES[$var]=$i
	  EXTRA_PROPERTIES+=("${var}=$(escape_property "${!i}")")
	done
	
	if [[ ${#EXTRA_PROPERTIES[@]} -gt 0 ]]; then
	  printf '%s\n' "${EXTRA_PROPERTIES[@]}" >> "openmrs-extra.properties"
	fi
fi

if [ -f "$OMRS_RUNTIME_PROPERTIES_FILE" ]; then
  if [ -f "openmrs-extra.properties" ]; then
    echo "Found existing runtime properties file at $OMRS_RUNTIME_PROPERTIES_FILE. Merging with extra properties."
    awk -F= '!a[$1]++' "openmrs-extra.properties" "$OMRS_RUNTIME_PROPERTIES_FILE" > openmrs-merged.properties
    mv openmrs-merged.properties "$OMRS_RUNTIME_PROPERTIES_FILE"
  fi

  # Ensure admin.password.locked is set in runtime properties
  printf 'admin.password.locked=%s\n' "$(escape_property "${OMRS_ADMIN_PASSWORD_LOCKED}")" > openmrs-admin-locked.properties
  awk -F= '!a[$1]++' openmrs-admin-locked.properties "$OMRS_RUNTIME_PROPERTIES_FILE" > openmrs-merged.properties
  mv openmrs-merged.properties "$OMRS_RUNTIME_PROPERTIES_FILE"
  rm openmrs-admin-locked.properties
else
  # on installation, we place the extra properties in the server properties, prefixed with `property` so that they
  # are correctly pulled out by the InitializationFilter. This leverages a system originally used by the SDK.
  if [ -f openmrs-extra.properties ]; then
  	if [ -f openmrs-extra.properties.tmp ]; then
  	  : > openmrs-extra.properties.tmp
  	fi
  	
  	while IFS="" read -r line; do
  	  [[ -n "$line" ]] && printf 'property.%s\n' "$line" >> openmrs-extra.properties.tmp
	done < openmrs-extra.properties
	
	echo >> openmrs-extra.properties.tmp
	
	if [ -f openmrs-extra.properties.tmp ]; then
	  mv openmrs-extra.properties.tmp openmrs-extra.properties
	  cat "openmrs-extra.properties" >> "$OMRS_SERVER_PROPERTIES_FILE"
	fi
  fi
fi

if [ -f openmrs-extra.properties ]; then
  rm openmrs-extra.properties;
fi

