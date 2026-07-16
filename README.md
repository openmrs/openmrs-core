<img src="https://talk.openmrs.org/uploads/default/original/2X/f/f1ec579b0398cb04c80a54c56da219b2440fe249.jpg" alt="OpenMRS"/>

[![Build Status](https://github.com/openmrs/openmrs-core/actions/workflows/build.yaml/badge.svg?branch=master)](https://github.com/openmrs/openmrs-core/actions/workflows/build.yaml) [![Coverage Status](https://codecov.io/gh/openmrs/openmrs-core/branch/master/graph/badge.svg)](https://app.codecov.io/gh/openmrs/openmrs-core) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=openmrs_openmrs-core&amp;metric=alert_status)](https://sonarcloud.io/summary/new_code?id=openmrs_openmrs-core)

api: [![API](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=api%2Fpom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=api%2Fpom.xml)
test: [![test](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=test%2Fpom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=test%2Fpom.xml)
tools: [![tools](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=tools%2Fpom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=tools%2Fpom.xml)
web: [![web](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=web%2Fpom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=web%2Fpom.xml)
webapp: [![webapp](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=webapp%2Fpom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=webapp%2Fpom.xml)
DPGA: [![DPG Badge](https://img.shields.io/badge/Verified-DPG-3333AB?logo=data:image/svg%2bxml;base64,PHN2ZyB3aWR0aD0iMzEiIGhlaWdodD0iMzMiIHZpZXdCb3g9IjAgMCAzMSAzMyIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTE0LjIwMDggMjEuMzY3OEwxMC4xNzM2IDE4LjAxMjRMMTEuNTIxOSAxNi40MDAzTDEzLjk5MjggMTguNDU5TDE5LjYyNjkgMTIuMjExMUwyMS4xOTA5IDEzLjYxNkwxNC4yMDA4IDIxLjM2NzhaTTI0LjYyNDEgOS4zNTEyN0wyNC44MDcxIDMuMDcyOTdMMTguODgxIDUuMTg2NjJMMTUuMzMxNCAtMi4zMzA4MmUtMDVMMTEuNzgyMSA1LjE4NjYyTDUuODU2MDEgMy4wNzI5N0w2LjAzOTA2IDkuMzUxMjdMMCAxMS4xMTc3TDMuODQ1MjEgMTYuMDg5NUwwIDIxLjA2MTJMNi4wMzkwNiAyMi44Mjc3TDUuODU2MDEgMjkuMTA2TDExLjc4MjEgMjYuOTkyM0wxNS4zMzE0IDMyLjE3OUwxOC44ODEgMjYuOTkyM0wyNC44MDcxIDI5LjEwNkwyNC42MjQxIDIyLjgyNzdMMzAuNjYzMSAyMS4wNjEyTDI2LjgxNzYgMTYuMDg5NUwzMC42NjMxIDExLjExNzdMMjQuNjI0MSA5LjM1MTI3WiIgZmlsbD0id2hpdGUiLz4KPC9zdmc+Cg==)](https://digitalpublicgoods.net/r/openmrs)

OpenMRS is a patient-based medical record system focusing on giving providers a free customizable electronic medical record system (EMR).

The mission of OpenMRS is to improve health care delivery in resource-constrained environments by coordinating a global community that creates a robust, scalable, user-driven, open source medical record system platform.

#### Table of Contents

1. [Build](#build)
   1. [Prerequisites](#prerequisites)
   2. [Build Command](#build-command)
   3. [Deploy](#deploy)
2. [Docker build](#docker-build)
3. [Navigating the repository](#navigating-the-repository)
4. [Software Development Kit](#software-development-kit)
5. [Extending OpenMRS with Modules](#extending-openmrs-with-modules)
6. [Documentation](#documentation)
   1. [Developer guides](#developer-guides)
   2. [Wiki](#wiki)
   3. [Website](#website)
7. [Contributing](#contributing)
   1. [Code](#code)
   2. [Code Reviews](#code-reviews)
   3. [Translation](#translation)
8. [Issues](#issues)
9. [Community](#community)
10. [Support](#support)
11. [License](#license)

## Build

### Prerequisites

#### Java

OpenMRS is a Java application which is why you need to install a Java JDK.

If you want to build the master branch you will need a Java JDK of minimum version 8.

#### Maven

Install the build tool [Maven](https://maven.apache.org/).

You need to ensure that Maven uses the Java JDK needed for the branch you want to build.

To do so execute

```bash
mvn -version
```

which will tell you what version Maven is using. Refer to the [Maven docs](https://maven.apache.org/configure.html) if you need to configure Maven.

#### Git

Install the version control tool [git](https://git-scm.com/) and clone this repository with

```bash
git clone https://github.com/openmrs/openmrs-core.git
```

### Build Command

After you have taken care of the [Prerequisites](#prerequisites)

Execute the following

```bash
cd openmrs-core
./mvnw clean package
```

This will generate the OpenMRS application in `webapp/target/openmrs.war`, which you will have to deploy to an application server such as [Tomcat](https://tomcat.apache.org/) or [Jetty](https://jetty.org/).

### Deploy

For development purposes you can simply deploy the `openmrs.war` into the application server jetty via

```bash
cd openmrs-core/webapp
../mvnw jetty:run
```

To run Jetty on a custom port, use the `jetty.http.port` property:

```bash
../mvnw -Djetty.http.port=8081 jetty:run
```

To run on Cargo, use the `cargo:run` command:

```bash
../mvnw cargo:run
```

To run Cargo on a custom port, use the `cargo.servlet.port` property:

```bash
../mvnw -Dcargo.servlet.port=8081 cargo:run
```

If all goes well (check the console output) you can access the OpenMRS application at `localhost:8080/openmrs`.

Refer to [Getting Started as a Developer](https://wiki.openmrs.org/display/docs/Getting+Started+as+a+Developer) for some more information
on useful Maven commands and build options.

## Docker build

Docker builds are still a work in progress. We appreciate any feedback and improvements to the process.

The only prerequisite needed is Docker.

In order to build a development version run:

```bash
docker compose build
```

It calls `mvn install` by default. If you would like to customize mvn build arguments you can do so by running:

```bash
docker compose build --build-arg MVN_ARGS='install -DskipTests'
```

It is also possible to use the built dev image to run jetty:

```bash
docker compose up
```

In order to build a production version run:

```bash
docker compose -f docker-compose.yml build
```

It first builds the dev image and then an image with Tomcat and openmrs.war.
It has no dev dependencies.

The production version can be run with:

```bash
docker compose -f docker-compose.yml up
```

If you want to debug, you need to run a development version and connect your debugger to port 8000, which is exposed by default.

Unfortunately, at this point any code changes require full restart and rebuild of the docker container. To speed up the process,
please use:

```bash
docker compose build --build-arg MVN_ARGS='install -DskipTests'
docker compose up
```

We are working towards providing support for Spring Boot auto-reload feature, which will be documented here once ready.

It is also possible to deploy an image built by our CI, which is published at
[OpenMRS Core on Docker Hub](https://hub.docker.com/r/openmrs/openmrs-core).

You can run any tag available with:

```bash
TAG=nightly docker compose -f docker-compose.yml up
```

It is also possible to run a development version of an image with:

```bash
TAG=dev docker compose up
```

All development versions contain dev suffix. The cache suffix is for use by our CI.

### Running with ElasticSearch

OpenMRS can run with ElasticSearch backend instead of the in-built Lucene index. You can run it with:

```bash
docker compose -f docker-compose.yml -f docker-compose.override.yml -f docker-compose.es.yml up
```

If you change backend, you need to rebuild the search index by going to Legacy UI -> Administration -> Search Index. Alternatively, you can rebuild the search index using the `searchindexupdate` REST endpoint.

### Running with Grafana

OpenMRS can run with Grafana for monitoring logs. You can run it with:

```bash
docker compose -f docker-compose.yml -f docker-compose.override.yml -f docker-compose.grafana.yml up
```

Grafana will be available at http://localhost:3000. Use admin as username and see docker-compose.grafana.yml for the initial password.

### Scheduler Dashboard

The scheduler dashboard can be accessed at http://localhost:9000 by default (user: admin / password: Admin123). It is enabled by default when running `docker compose up` command. It is configured in `docker-compose.override.yml`.

If you want to enable it in other environments, you need to set the following properties in `openmrs-runtime.properties`:

```bash
jobrunr.dashboard.enabled=true
jobrunr.dashboard.password=someStrongPassword
```

## Navigating the repository

The project tree is set up as follows:

<table>
 <tr>
  <td>api/</td>
  <td>Java and resource files for building the java api jar file.</td>
 </tr>
 <tr>
  <td>tools/</td>
  <td>Meta code used during compiling and testing. Does not go into any released binary (like doclets).</td>
 </tr>
 <tr>
  <td>web/</td>
  <td>Java and resource files that are used in the webapp/war file.</td>
 </tr>
 <tr>
  <td>webapp/</td>
  <td>files used in building the war file (contains JSP files on older versions).</td>
 </tr>
 <tr>
  <td>pom.xml</td>
  <td>The main maven file used to build and package OpenMRS.</td>
 </tr>
</table>

## Software Development Kit

For rapid development of modules and the OpenMRS Platform code, see the
[OpenMRS SDK documentation](https://wiki.openmrs.org/display/docs/OpenMRS+SDK).

## Extending OpenMRS with Modules

OpenMRS has a modular architecture that allows developers to extend the OpenMRS core functionality by creating modules that can easily be added or removed to meet the needs of a specific implementation.

Before creating your own module, go to the [OpenMRS Module Repository](https://addons.openmrs.org/) and see if there is already a module for your specific use case. If so, deploy and try it. If functionality is missing, work with the module's developers to add the feature.

If you haven't found what you were looking for, refer to the [Module - wiki](https://wiki.openmrs.org/display/docs/Modules) to learn how you can create a new module.

## Documentation

### Developer guides

If you want to contribute please refer to these resources

* [Getting Started as a Developer](https://openmrs.atlassian.net/wiki/spaces/docs/pages/25477022/Getting+Started+as+a+Developer)
* [How To Configure Your IDE](https://openmrs.atlassian.net/wiki/spaces/Archives/pages/25506949/How-To+Setup+And+Use+Your+IDE)
* [How To Make a Pull Request](https://wiki.openmrs.org/display/docs/Pull+Request+Tips)

### Wiki

If you are looking for detailed guides on how to install, configure, contribute and
extend OpenMRS, visit [the OpenMRS Wiki](https://wiki.openmrs.org).

### Website

For more information about OpenMRS as an organization, check
[the OpenMRS website](https://openmrs.org).

## Contributing

Contributions are very welcome. We can definitely use your help!

OpenMRS organizes the privileges of its contributors in developer stages which
are documented [here](https://wiki.openmrs.org/display/RES/OpenMRS+Developer+Stages).

Read the following sections to find out where you could help.

### Code

Check out our [contributing guidelines](CONTRIBUTING.md), read through the [Developer guides](#developer-guides).

After you've read up :eyeglasses: [grab an introductory issue](https://openmrs.atlassian.net/wiki/x/a8GEAQ) that is `Ready For Work`.

### Code Reviews

If you do not have time to develop but have experience with OpenMRS or reviewing
code, your help with code reviews would be greatly appreciated!

Read [the code review guide](https://openmrs.atlassian.net/wiki/x/-r_EAQ) and get started reviewing pull requests!

### Translation

We use [Transifex](https://explore.transifex.com/openmrs/OpenMRS/) to manage our translations.

The `messages.properties` file in this repository is our single source of
truth. It contains key, value pairs for the English language which is the
default.

Transifex fetches updates to this file every night so they can be translated
on the Transifex website. At any time, we can pull new translations from Transifex
back into this repository. Other languages, such as Spanish, will then be in
the `messages_es.properties` file.

If you would like to know how to help with translations, see
[the OpenMRS translation guide](https://om.rs/translate).

## Issues

If you want to help fix existing issues or have found a bug to report, please go to
[the OpenMRS issue tracker](https://issues.openmrs.org).

## Community

[![OpenMRS Talk](https://img.shields.io/badge/OpenMRS_Talk-F26522?logo=discourse&logoColor=white)](https://talk.openmrs.org)
[![OpenMRS Slack](https://img.shields.io/badge/OpenMRS_Slack-4A154B)](https://slack.openmrs.org/)
[![OpenMRS Wiki](https://img.shields.io/badge/openmrs-wiki-5B57A6.svg?logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNjAiIGhlaWdodD0iMTQyIiB2aWV3Qm94PSIwIDAgMTYwIDE0MiI%2BPHBhdGggY2xhc3M9InN0MCIgZD0iTTExMy42MTUgOTQuNDk0Yy0yLjAxNi0zLjk3NC00LjQwNS03Ljk5LTcuMi0xMi4wNzctMi0yLjkzLTQuMTQ1LTUuNzc4LTYuMzg3LTguNTY3LS45MS0xLjEzNi0uNTMtMi41NDguMTY3LTMuMjUuNjg4LS43MDUgMS4zOC0xLjQxIDIuMDc2LTIuMTIgOS41OC05Ljc3IDE5LjQ5LTE5Ljg3MyAyNy4wOS0zMC43ODcgOC4wOC0xMS42MSAxMi41Ni0yMi42MjQgMTMuNjktMzMuOTU0LjEyLTEuMTQtLjQtMi4zNS0xLjMyLTMuMDUtLjYtLjQ2LTEuMzMtLjctMi4wNy0uNy0uNDEgMC0uODIuMDctMS4yMS4yMi03LjM3IDIuODItMTQuODUgNC45Ni0yMS42OCA2LjU1LTEuMzkuMzItMi41MSAxLjM2LTIuOTggMi42LTQuOTggMTMuNjMtMTcuNjggMjYuNjEtMzEuMDEgNDAuMi0uNTMuNTEtMS4yOCAxLjE4LTIuNSAxLjE4cy0xLjk2LS42NS0yLjUtMS4xOGMtMTMuMzMtMTMuNTktMjYuMDMtMjYuNTItMzEtNDAuMTUtLjQ2LTEuMjQtMS41OS0yLjI4LTIuOTgtMi42QzM2Ljk0IDUuMjIgMjkuNDUgMi45IDIyLjEuMDhjLS4zOTgtLjE1LS44MS0uMjI1LTEuMjItLjIyNS0uNzQgMC0xLjQ3LjI0LTIuMDcuNy0uOTQuNzE4LTEuNDQgMS44NzItMS4zMiAzLjA0OCAxLjEzIDExLjMzMiA1LjYgMjIuNDggMTMuNjg0IDM0LjA5IDcuNiAxMC45MTUgMTcuNTEgMjEuMDE3IDI3LjA5IDMwLjc4NyAxNy42NSAxNy45OTQgMzQuMzMgMzQuOTk3IDM1Ljc5IDU0LjcxMy4xMyAxLjc4IDEuNjIgMy4xNTggMy40IDMuMTU4aDIwLjc0Yy45NCAwIDEuODMtLjM4IDIuNDctMS4wNi42NS0uNjcuOTktMS41OC45NC0yLjUyLS4xOC0zLjcxLS43Mi03LjQyLTEuNTktMTEuMTZoLjAxYy0uMDI4LS4xMS0uMDQ3LS4yMi0uMDQ3LS4zMyAwLS43NS41ODgtMS4zOCAxLjM1Ny0xLjM4LjA3IDAgLjEzLjAyLjIuMDMgMTYuOTMgMi40OCAyNy42MzYgNi40NCAyNy42NSAxMC44di4wMWMwIDQuMTEtOS42MjMgMTAuMzEtMjUuMjY2IDE0Ljg1bC0uMDA1LjAxYy0xLjM5LjQtMi40MDYgMS42Ni0yLjQwNiAzLjE1IDAgMS44MSAxLjQ5MyAzLjI4IDMuMzQgMy4yOC4yNTUgMCAuNS0uMDMuNzQtLjA4IDIxLjAyNi00Ljg2IDM0Ljk2NS0xMy4wMzQgMzQuOTY1LTIyLjI2MiAwLTEwLjk1NC0xOC44NC0yMC43NC00Ni45LTI1LjE1MnpNNTguMDEgODMuODA2Yy0uNDI1LS40NDQtMS4yNzctMS4wMzgtMi40MjItMS4wMzgtMS41NDcgMC0yLjQ2NiAxLTIuODEyIDEuNTMtMi4yNjQgMy40NDQtNC4yNCA2Ljg0My01Ljk0NiAxMC4yMDhDMTguODEgOTguOTI0IDAgMTA4LjcgMCAxMTkuNjVjMCA5LjIzNyAxMy44NCAxNy4zOTQgMzQuOTA1IDIyLjI1NS4wMDMuMDAyLjAyMyAwIC4wMyAwIC4yNS4wNTguNTA0LjA5NS43Ny4wOTUgMS44NDYgMCAzLjM0LTEuNDcgMy4zNC0zLjI4IDAtMS40ODctMS4wMTctMi43My0yLjQtMy4xM2wtLjAxLS4wMjJjLTE1LjY0NS00LjU0LTI1LjI3LTEwLjc0NC0yNS4yNy0xNC44NTJ2LS4wMWMuMDE3LTQuMzUzIDEwLjY5My04LjMwNiAyNy41OC0xMC43ODcuMDYyLS4wMS4xMi0uMDIuMTgyLS4wMi43NzUgMCAxLjM2OC42MyAxLjM2OCAxLjM5IDAgLjExLS4wMi4yMy0uMDQ2LjMzbC4wMS4wMWMtLjg3IDMuNzEtMS40IDcuNDEtMS41OCAxMS4xMS0uMDUuOTMuMjkgMS44NS45NCAyLjUzLjY0LjY3IDEuNTQgMS4wNiAyLjQ4IDEuMDZoMjAuNzRjMS43OCAwIDMuMjgtMS40IDMuNDEtMy4xNy40NS02LjA3IDIuMzUtMTIuMTUgNS43OC0xOC41NCAxLjE5LTIuMjEuMjYtNC4yOS0uNDItNS4xOC0zLjQyLTQuNDMtNy41OS05LjE2LTEzLjgxLTE1LjY1eiIgZmlsbD0iI2ZmZiIvPjxwYXRoIGNsYXNzPSJzdDAiIGQ9Ik03Ny44NjggMzIuNTc4Yy44Mi43OTggMS43NS45NDcgMi4zOS45NDdoLjAwNmMuNjQyIDAgMS41Ny0uMTQ4IDIuMzktLjk0NiA3LjMxMy03LjExIDExLjI0Mi0xNS40IDEyLjEwMy0xNy43MS4xMjUtLjM0LjI1Mi0uNzMuMjUyLTEuMjYgMC0xLjg0LTEuNTQtMy4xNi0zLjE0LTMuMTYtMS4zMyAwLTUuMS4zOS0xMS41OS4zOWgtLjA1Yy02LjUgMC0xMC4yNy0uMzktMTEuNTktLjM5LTEuNjEgMC0zLjE0IDEuMzEtMy4xNCAzLjE1IDAgLjUzLjEzLjkyLjI1IDEuMjYuODYgMi4zIDQuNzkgMTAuNTkgMTIuMSAxNy43eiIgZmlsbD0iI2ZmZiIvPjwvc3ZnPg%3D%3D)](https://wiki.openmrs.org)

## Support

Talk to us on [OpenMRS Talk](https://talk.openmrs.org/)

## License

[MPL 2.0 w/ HD](LICENSE) © [OpenMRS Inc.](https://openmrs.org/)
