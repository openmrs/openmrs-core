# syntax=docker/dockerfile:1

#	This Source Code Form is subject to the terms of the Mozilla Public License, 
#	v. 2.0. If a copy of the MPL was not distributed with this file, You can 
#	obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under 
#	the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
#	
#	Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS 
#	graphic logo is a trademark of OpenMRS Inc.

### Build Stage
FROM maven:3.8-jdk-11 as build
WORKDIR /app

ENV DEPENDENCY_PLUGIN="org.apache.maven.plugins:maven-dependency-plugin:3.3.0"

# Copy poms to resolve dependencies
COPY pom.xml .
COPY liquibase/pom.xml ./liquibase/
COPY tools/pom.xml ./tools/
COPY test/pom.xml ./test/
COPY web/pom.xml ./web/
COPY api/pom.xml ./api/
COPY webapp/pom.xml ./webapp/

# Resolve dependencies in order to cache them and run offline builds
RUN mvn $DEPENDENCY_PLUGIN:go-offline

ARG MVN_ARGS='install'

# Build the app using cached dependencies
# Append --build-arg MVN_ARGS='install -o' to change default maven arguments
# Build modules individually to benefit from caching
COPY checkstyle.xml checkstyle-suppressions.xml CONTRIBUTING.md findbugs-include.xml LICENSE license-header.txt \
 NOTICE.md README.md ruleset.xml SECURITY.md ./

COPY liquibase ./liquibase/
RUN mvn -pl liquibase $MVN_ARGS

COPY tools/ ./tools/
RUN mvn -pl tools $MVN_ARGS

COPY test/ ./test/
RUN mvn -pl test $MVN_ARGS

COPY api/ ./api/
RUN mvn -pl api $MVN_ARGS

COPY web/ ./web/
RUN mvn -pl web $MVN_ARGS

COPY webapp/ ./webapp/
RUN mvn -pl webapp $MVN_ARGS

# Store dependencies for re-use when running
# If mounting ~/.m2:/root/.m2 then the m2 repo content will be copied over from the image
RUN cp -r /root/.m2/repository /usr/share/maven/ref/repository

WORKDIR /app/webapp

# Startup jetty by default for the dev image
# TODO: Use Tomcat with spring devtools instead
CMD ["mvn", "jetty:run", "-o"]

### Production Stage
FROM tomcat:9.0-jdk8-adoptopenjdk-hotspot

RUN apt-get update && apt-get install -y zip dumb-init \
    && apt-get clean  \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /usr/local/tomcat/webapps/*

RUN groupadd -r openmrs  \
    && useradd --no-log-init -r -g openmrs openmrs  \
    && chown -R openmrs $CATALINA_HOME  \
    && mkdir -p /openmrs  \
    && chown -R openmrs /openmrs 

# Copy in the start-up scripts
COPY wait-for-it.sh startup.sh /usr/local/tomcat/
RUN chmod -R 755 /usr/local/tomcat/wait-for-it.sh && chmod -R 755 /usr/local/tomcat/startup.sh

USER openmrs

WORKDIR /openmrs

# All environment variables that are available to configure on this container are listed here
# for clarity. These list the variables supported, and the default values if not overridden

# These environment variables are appended to configure the Tomcat JAVA_OPTS
ENV OMRS_JAVA_MEMORY_OPTS="-XX:NewSize=128m"
ENV OMRS_JAVA_SERVER_OPTS="-Dfile.encoding=UTF-8 -server -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Djava.awt.headlesslib=true"

# These environment variables are used to create the openmrs-server.properties file, which controls how OpenMRS initializes
ENV OMRS_CONFIG_ADD_DEMO_DATA="false"
ENV OMRS_CONFIG_ADMIN_USER_PASSWORD="Admin123"
ENV OMRS_CONFIG_AUTO_UPDATE_DATABASE="true"
ENV OMRS_CONFIG_CREATE_DATABASE_USER="false"
ENV OMRS_CONFIG_CREATE_TABLES="false"
ENV OMRS_CONFIG_HAS_CURRENT_OPENMRS_DATABASE="true"
ENV OMRS_CONFIG_INSTALL_METHOD="auto"
ENV OMRS_CONFIG_MODULE_WEB_ADMIN="true"

# These variables are specific to database connections
# Supported values for OMRS_CONFIG_CONNECTION_TYPE are "mysql" and "postgresql"
# other values are treated as MySQL
ENV OMRS_CONFIG_CONNECTION_TYPE="mysql"
ENV OMRS_CONFIG_CONNECTION_USERNAME="openmrs"
ENV OMRS_CONFIG_CONNECTION_PASSWORD="openmrs"
ENV OMRS_CONFIG_CONNECTION_SERVER="localhost"
ENV OMRS_CONFIG_CONNECTION_DATABASE="openmrs"

# These environment variables can be used to customise the database connection.
# Their default values depend on which database you are using.
# OMRS_CONFIG_CONNECTION_DRIVER_CLASS
# OMRS_CONFIG_CONNECTION_PORT
# OMRS_CONFIG_CONNECTION_ARGS
# OMRS_CONFIG_CONNECTION_EXTRA_ARGS
#
# If you really need complete control, you can just set
# OMRS_CONFIG_CONNECTION_URL to whatever the URL should be

# These environment variables are meant to enable developer settings
# OMRS_DEV_DEBUG_PORT

# Additional environment variables as needed. This should match the name of the distribution supplied OpenMRS war file
ENV OMRS_WEBAPP_NAME="openmrs"

RUN sed -i '/Connector port="8080"/a URIEncoding="UTF-8" relaxedPathChars="[]|" relaxedQueryChars="[]|{}^&#x5c;&#x60;&quot;&lt;&gt;"' /usr/local/tomcat/conf/server.xml
    
# Copy the app
COPY --from=build /app/webapp/target/openmrs.war /openmrs/distribution/openmrs_core/openmrs.war

EXPOSE 8080

CMD ["dumb-init", "/usr/local/tomcat/startup.sh"]

