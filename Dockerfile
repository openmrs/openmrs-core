# syntax=docker/dockerfile:1

#	This Source Code Form is subject to the terms of the Mozilla Public License, 
#	v. 2.0. If a copy of the MPL was not distributed with this file, You can 
#	obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under 
#	the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
#	
#	Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS 
#	graphic logo is a trademark of OpenMRS Inc.

### Compile Stage (platform-agnostic)
FROM --platform=$BUILDPLATFORM maven:3.8-amazoncorretto-8 as compile

RUN yum -y update && yum -y install git && yum clean all

WORKDIR /openmrs_core

ENV OMRS_SDK_PLUGIN="org.openmrs.maven.plugins:openmrs-sdk-maven-plugin"
ENV OMRS_SDK_PLUGIN_VERSION="4.5.0"

COPY docker-pom.xml .

ARG MVN_SETTINGS="-s /usr/share/maven/ref/settings-docker.xml"

# Setup and cache SDK
RUN mvn $MVN_SETTINGS -f docker-pom.xml $OMRS_SDK_PLUGIN:$OMRS_SDK_PLUGIN_VERSION:setup-sdk -N -DbatchAnswers=n

COPY pom.xml .
COPY test/pom.xml test/
COPY tools/pom.xml tools/
COPY liquibase/pom.xml liquibase/
COPY api/pom.xml api/
COPY web/pom.xml web/
COPY webapp/pom.xml webapp/

# Install dependencies
RUN mvn $MVN_SETTINGS -B dependency:go-offline -P !default-tools.jar,!mac-tools.jar

# Copy remaining files
COPY . .

# Append --build-arg MVN_ARGS='clean install' to change default maven arguments
ARG MVN_ARGS='clean install -DskipTests'

# Build the project
RUN mvn $MVN_SETTINGS $MVN_ARGS

### Development Stage
FROM maven:3.8-amazoncorretto-8 as dev

RUN yum -y update && yum -y install tar gzip git && yum clean all

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
ARG TOMCAT_VERSION=8.5.83
ARG TOMCAT_SHA="57cbe9608a9c4e88135e5f5480812e8d57690d5f3f6c43a7c05fe647bddb7c3b684bf0fc0efebad399d05e80c6d20c43d5ecdf38ec58f123e6653e443f9054e3"
ARG TOMCAT_URL="https://www.apache.org/dyn/closer.cgi?action=download&filename=tomcat/tomcat-8/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz"
RUN curl -fL -o /tmp/apache-tomcat.tar.gz "$TOMCAT_URL" \
    && echo "${TOMCAT_SHA}  /tmp/apache-tomcat.tar.gz" | sha512sum -c \
    && mkdir -p /usr/local/tomcat && gzip -d /tmp/apache-tomcat.tar.gz  \
    && tar -xvf /tmp/apache-tomcat.tar -C /usr/local/tomcat/ --strip-components=1 \
    && rm -rf /tmp/apache-tomcat.tar.gz /usr/local/tomcat/webapps/* 

WORKDIR /openmrs_core

COPY --from=compile /usr/share/maven/ref /usr/share/maven/ref

COPY --from=compile /openmrs_core /openmrs_core/

RUN mkdir -p /openmrs/distribution/openmrs_core/ \
    && cp /openmrs_core/webapp/target/openmrs.war /openmrs/distribution/openmrs_core/openmrs.war \
    && cp /openmrs_core/wait-for-it.sh /openmrs_core/startup-init.sh /openmrs_core/startup.sh /openmrs_core/startup-dev.sh /openmrs/  \
    && chmod +x /openmrs/wait-for-it.sh && chmod +x /openmrs/startup-init.sh && chmod +x /openmrs/startup.sh \
    && chmod +x /openmrs/startup-dev.sh 

EXPOSE 8080

ENTRYPOINT ["/usr/bin/tini", "--", "/usr/local/bin/mvn-entrypoint.sh"]

# See startup-init.sh for all configurable environment variables
# TODO: Use Tomcat with spring devtools instead
CMD ["/openmrs/startup-dev.sh"]

### Production Stage
FROM tomcat:8.5-jdk8-corretto

RUN yum -y update && yum clean all && rm -rf /usr/local/tomcat/webapps/*

# Setup Tini
ARG TARGETARCH
ARG TINI_VERSION=v0.19.0
ARG TINI_URL="https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini"
ARG TINI_SHA="93dcc18adc78c65a028a84799ecf8ad40c936fdfc5f2a57b1acda5a8117fa82c"
ARG TINI_SHA_ARM64="07952557df20bfd2a95f9bef198b445e006171969499a1d361bd9e6f8e5e0e81"
RUN if [ "$TARGETARCH" = "arm64" ] ; then TINI_URL="${TINI_URL}-arm64" TINI_SHA=${TINI_SHA_ARM64} ; fi \
    && curl -fsSL -o /usr/bin/tini ${TINI_URL} \
    && echo "${TINI_SHA}  /usr/bin/tini" | sha256sum -c \
    && chmod g+rx /usr/bin/tini 

RUN sed -i '/Connector port="8080"/a URIEncoding="UTF-8" relaxedPathChars="[]|" relaxedQueryChars="[]|{}^&#x5c;&#x60;&quot;&lt;&gt;"' \
    /usr/local/tomcat/conf/server.xml \
    && chmod -R g+rx /usr/local/tomcat \
    && touch /usr/local/tomcat/bin/setenv.sh && chmod g+w /usr/local/tomcat/bin/setenv.sh \
    && chmod -R g+w /usr/local/tomcat/webapps /usr/local/tomcat/logs /usr/local/tomcat/work /usr/local/tomcat/temp 

RUN mkdir -p /openmrs/data/modules \
    && mkdir -p /openmrs/data/owa  \
    && mkdir -p /openmrs/data/configuration \
    && mkdir -p /openmrs/data/configuration_checksums \
    && chmod -R g+rw /openmrs
    
# Copy in the start-up scripts
COPY --from=dev /openmrs/wait-for-it.sh /openmrs/startup-init.sh /openmrs/startup.sh /openmrs/
RUN chmod g+x /openmrs/wait-for-it.sh && chmod g+x /openmrs/startup-init.sh && chmod g+x /openmrs/startup.sh

WORKDIR /openmrs

COPY --from=dev /openmrs_core/LICENSE LICENSE
# Copy the app
COPY --from=dev /openmrs/distribution/openmrs_core/openmrs.war /openmrs/distribution/openmrs_core/openmrs.war

EXPOSE 8080

# Run as non-root user using Bitnami approach, see e.g.
# https://github.com/bitnami/containers/blob/6c8f10bbcf192ab4e575614491abf10697c46a3e/bitnami/tomcat/8.5/debian-11/Dockerfile#L54
USER 1001

ENTRYPOINT ["/usr/bin/tini", "--"]

# See startup-init.sh for all configurable environment variables
CMD ["/openmrs/startup.sh"]

