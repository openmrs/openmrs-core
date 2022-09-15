# syntax=docker/dockerfile:1

#	This Source Code Form is subject to the terms of the Mozilla Public License, 
#	v. 2.0. If a copy of the MPL was not distributed with this file, You can 
#	obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under 
#	the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
#	
#	Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS 
#	graphic logo is a trademark of OpenMRS Inc.

### Development Stage
FROM maven:3.8-jdk-11 as dev
WORKDIR /app

ENV DEPENDENCY_PLUGIN="org.apache.maven.plugins:maven-dependency-plugin:3.3.0"
ENV OPENMRS_SDK_PLUGIN="org.openmrs.maven.plugins:openmrs-sdk-maven-plugin:4.5.0"
ENV OPENMRS_SDK_PLUGIN_VERSION="4.5.0"
ENV MVN_ARGS_SETTINGS="-s /usr/share/maven/ref/settings-docker.xml"

COPY pom.xml .
RUN mvn $OPENMRS_SDK_PLUGIN:setup-sdk -N -DbatchAnswers=n $MVN_ARGS_SETTINGS

# Copy poms to resolve dependencies
COPY liquibase/pom.xml ./liquibase/
COPY tools/pom.xml ./tools/
COPY test/pom.xml ./test/
COPY web/pom.xml ./web/
COPY api/pom.xml ./api/
COPY webapp/pom.xml ./webapp/

# Resolve dependencies in order to cache them and run offline builds
# Store dependencies in /usr/share/maven/ref/repository for re-use when running
# If mounting ~/.m2:/root/.m2 then the /usr/share/maven/ref content will be copied over from the image to /root/.m2
RUN mvn $DEPENDENCY_PLUGIN:go-offline -U -B $MVN_ARGS_SETTINGS

ARG MVN_ARGS='install'

# Build the app using cached dependencies
# Append --build-arg MVN_ARGS='install -o' to change default maven arguments
# Build modules individually to benefit from caching
COPY checkstyle.xml checkstyle-suppressions.xml CONTRIBUTING.md findbugs-include.xml LICENSE license-header.txt \
 NOTICE.md README.md ruleset.xml SECURITY.md ./

# Build the parent project first
RUN mvn --non-recursive $MVN_ARGS_SETTINGS $MVN_ARGS

COPY liquibase ./liquibase/
RUN mvn -pl liquibase $MVN_ARGS_SETTINGS $MVN_ARGS

COPY tools/ ./tools/
RUN mvn -pl tools $MVN_ARGS_SETTINGS $MVN_ARGS

COPY test/ ./test/
RUN mvn -pl test $MVN_ARGS_SETTINGS $MVN_ARGS

COPY api/ ./api/
RUN mvn -pl api $MVN_ARGS_SETTINGS $MVN_ARGS

COPY web/ ./web/
RUN mvn -pl web $MVN_ARGS_SETTINGS $MVN_ARGS

COPY webapp/ ./webapp/
RUN mvn -pl webapp $MVN_ARGS_SETTINGS $MVN_ARGS

USER root

# Copy in the start-up scripts
COPY wait-for-it.sh startup-init.sh startup.sh startup-dev.sh ./
RUN chmod 755 wait-for-it.sh && chmod 755 startup-init.sh && chmod 755 startup.sh  \
    && chmod 755 startup-dev.sh 

EXPOSE 8080

# Startup jetty by default for the dev image
# TODO: Use Tomcat with spring devtools instead
CMD ["bash", "./startup-dev.sh"]

### Production Stage
FROM tomcat:8.5-jdk8-adoptopenjdk-hotspot

RUN apt-get update && apt-get install -y zip dumb-init \
    && apt-get clean  \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /usr/local/tomcat/webapps/*

RUN groupadd -r openmrs  \
    && useradd --no-log-init -r -g openmrs openmrs  \
    && chown -R openmrs $CATALINA_HOME  \
    && mkdir -p /openmrs/data/modules \
    && mkdir -p /openmrs/data/owa  \
    && mkdir -p /openmrs/data/configuration  \
    && chown -R openmrs /openmrs 

# Copy in the start-up scripts
COPY wait-for-it.sh startup-init.sh startup.sh /openmrs/
RUN chmod -R 755 /openmrs/wait-for-it.sh && chmod -R 755 /openmrs/startup-init.sh && chmod -R 755 /openmrs/startup.sh

USER openmrs

WORKDIR /openmrs

RUN sed -i '/Connector port="8080"/a URIEncoding="UTF-8" relaxedPathChars="[]|" relaxedQueryChars="[]|{}^&#x5c;&#x60;&quot;&lt;&gt;"' /usr/local/tomcat/conf/server.xml

COPY --from=dev /app/LICENSE LICENSE
# Copy the app
COPY --from=dev /app/webapp/target/openmrs.war /openmrs/distribution/openmrs_core/openmrs.war

EXPOSE 8080

# See startup-init.sh for all configurable environment variables
CMD ["dumb-init", "./startup.sh"]

