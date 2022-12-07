# syntax=docker/dockerfile:1

#	This Source Code Form is subject to the terms of the Mozilla Public License, 
#	v. 2.0. If a copy of the MPL was not distributed with this file, You can 
#	obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under 
#	the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
#	
#	Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS 
#	graphic logo is a trademark of OpenMRS Inc.

### Development Stage
FROM maven:3.8-amazoncorretto-8 as dev

RUN yum -y update && yum -y install tar gzip && yum clean all

# Setup Tini
ARG TARGETARCH
ARG TINI_VERSION=v0.19.0
ARG TINI_URL="https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini"
ARG TINI_SHA="93dcc18adc78c65a028a84799ecf8ad40c936fdfc5f2a57b1acda5a8117fa82c"
ARG TINI_SHA_ARM64="07952557df20bfd2a95f9bef198b445e006171969499a1d361bd9e6f8e5e0e81"
RUN if [ "$TARGETARCH" = "arm64" ] ; then TINI_URL="${TINI_URL}-arm64" TINI_SHA=${TINI_SHA_ARM64} ; fi \
    && curl -fsSL -o /usr/bin/tini ${TINI_URL} \
    && echo "${TINI_SHA}  /usr/bin/tini" | sha256sum -c \
    && chmod +x /usr/bin/tini 

# Setup Tomcat for development
ARG TOMCAT_VERSION=8.5.84
ARG TOMCAT_SHA="e595e906d62ff16545318108478aa101103181569dc6f4549dd0cdf8744147f7e9ba8a88cab6d33237b22981acb1085de86e7b2a4f1659efdbd4804df1303561"
RUN curl -fL -o /tmp/apache-tomcat.tar.gz \
    https://dlcdn.apache.org/tomcat/tomcat-8/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz \
    && echo "${TOMCAT_SHA}  /tmp/apache-tomcat.tar.gz" | sha512sum -c \
    && mkdir -p /usr/local/tomcat && gzip -d /tmp/apache-tomcat.tar.gz && tar -xvf /tmp/apache-tomcat.tar -C /usr/local/tomcat/ --strip-components=1 \
    && rm -rf /tmp/apache-tomcat.tar.gz /usr/local/tomcat/webapps/*

WORKDIR /openmrs_core

ENV OPENMRS_SDK_PLUGIN="org.openmrs.maven.plugins:openmrs-sdk-maven-plugin:4.5.0"
ENV OPENMRS_SDK_PLUGIN_VERSION="4.5.0"
ENV MVN_ARGS_SETTINGS="-s /usr/share/maven/ref/settings-docker.xml"

COPY checkstyle.xml checkstyle-suppressions.xml CONTRIBUTING.md findbugs-include.xml LICENSE license-header.txt \
 NOTICE.md README.md ruleset.xml SECURITY.md ./

COPY pom.xml .

# Setup and cache SDK
RUN mvn $OPENMRS_SDK_PLUGIN:setup-sdk -N -DbatchAnswers=n $MVN_ARGS_SETTINGS

# Store dependencies in /usr/share/maven/ref/repository for re-use when running
# If mounting ~/.m2:/root/.m2 then the /usr/share/maven/ref content will be copied over from the image to /root/.m2
RUN mvn --non-recursive dependency:go-offline $MVN_ARGS_SETTINGS

# Copy remainig poms to satisfy dependencies
COPY liquibase/pom.xml ./liquibase/
COPY tools/pom.xml ./tools/
COPY test/pom.xml ./test/
COPY api/pom.xml ./api/
COPY web/pom.xml ./web/
COPY webapp/pom.xml ./webapp/
	
# Exclude tools as it fails trying to fetch tools.jar
RUN mvn -pl !tools dependency:go-offline $MVN_ARGS_SETTINGS

# Append --build-arg MVN_ARGS='install' to change default maven arguments
# Build modules individually to benefit from caching
ARG MVN_ARGS='install'

# Build the parent project
RUN mvn --non-recursive $MVN_ARGS_SETTINGS $MVN_ARGS

# Build individually to benefit from caching
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

RUN mkdir -p /openmrs/distribution/openmrs_core/ \
    && cp /openmrs_core/webapp/target/openmrs.war /openmrs/distribution/openmrs_core/openmrs.war

# Copy in the start-up scripts
COPY wait-for-it.sh startup-init.sh startup.sh startup-dev.sh /openmrs/
RUN chmod +x /openmrs/wait-for-it.sh && chmod +x /openmrs/startup-init.sh && chmod +x /openmrs/startup.sh \
    && chmod +x /openmrs/startup-dev.sh 

EXPOSE 8080

ENTRYPOINT ["/usr/bin/tini", "--", "/usr/local/bin/mvn-entrypoint.sh"]

# See startup-init.sh for all configurable environment variables
# TODO: Use Tomcat with spring devtools instead
CMD ["/openmrs/startup-dev.sh"]

### Production Stage
FROM tomcat:8.5-jdk8-corretto

RUN yum -y update && yum -y install shadow-utils && yum clean all && rm -rf /usr/local/tomcat/webapps/*

# Setup Tini
ARG TARGETARCH
ARG TINI_VERSION=v0.19.0
ARG TINI_URL="https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini"
ARG TINI_SHA="93dcc18adc78c65a028a84799ecf8ad40c936fdfc5f2a57b1acda5a8117fa82c"
ARG TINI_SHA_ARM64="07952557df20bfd2a95f9bef198b445e006171969499a1d361bd9e6f8e5e0e81"
RUN if [ "$TARGETARCH" = "arm64" ] ; then TINI_URL="${TINI_URL}-arm64" TINI_SHA=${TINI_SHA_ARM64} ; fi \
    && curl -fsSL -o /usr/bin/tini ${TINI_URL} \
    && echo "${TINI_SHA}  /usr/bin/tini" | sha256sum -c \
    && chmod +x /usr/bin/tini 

RUN sed -i '/Connector port="8080"/a URIEncoding="UTF-8" relaxedPathChars="[]|" relaxedQueryChars="[]|{}^&#x5c;&#x60;&quot;&lt;&gt;"' \
    /usr/local/tomcat/conf/server.xml

RUN adduser openmrs && mkdir -p /openmrs/data/modules \
    && mkdir -p /openmrs/data/owa  \
    && mkdir -p /openmrs/data/configuration \
    && chown -R openmrs /openmrs
    
# Copy in the start-up scripts
COPY wait-for-it.sh startup-init.sh startup.sh /openmrs/
RUN chmod +x /openmrs/wait-for-it.sh && chmod +x /openmrs/startup-init.sh && chmod +x /openmrs/startup.sh

WORKDIR /openmrs

COPY --from=dev /openmrs_core/LICENSE LICENSE
# Copy the app
COPY --from=dev /openmrs/distribution/openmrs_core/openmrs.war /openmrs/distribution/openmrs_core/openmrs.war

EXPOSE 8080

ENTRYPOINT ["/usr/bin/tini", "--"]

# See startup-init.sh for all configurable environment variables
CMD ["/openmrs/startup.sh"]

