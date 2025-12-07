This README has been updated to improve clarity, structure, and developer onboarding experience.

<img src="https://talk.openmrs.org/uploads/default/original/2X/f/f1ec579b0398cb04c80a54c56da219b2440fe249.jpg" alt="OpenMRS Banner" width="100%"/>

# OpenMRS Core

[![Build Status](https://travis-ci.org/openmrs/openmrs-core.svg?branch=master)](https://travis-ci.org/openmrs/openmrs-core)
[![Coverage Status](https://coveralls.io/repos/github/openmrs/openmrs-core/badge.svg?branch=master)](https://coveralls.io/github/openmrs/openmrs-core?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a51303ee46c34775a7c31c8d6016da6b)](https://www.codacy.com/app/openmrs/openmrs-core)
[![DPG Badge](https://img.shields.io/badge/Verified-DPG-3333AB)](https://digitalpublicgoods.net/r/openmrs)

OpenMRS is an open-source, patient-centric Electronic Medical Record (EMR) platform built to support healthcare delivery in resource-constrained environments.  
Our mission is to empower a global community that develops scalable, customizable digital health tools for better patient care.

---

## Module Status

| Module    | Status |
|----------|--------|
| **API**     | [![API](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=api/pom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=api/pom.xml) |
| **Test**    | [![Test](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=test/pom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=test/pom.xml) |
| **Tools**   | [![Tools](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=tools/pom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=tools/pom.xml) |
| **Web**     | [![Web](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=web/pom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=web/pom.xml) |
| **Webapp**  | [![Webapp](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=webapp/pom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=webapp/pom.xml) |

---

## Table of Contents

1. [Build](#build)
   - [Prerequisites](#prerequisites)
   - [Build Command](#build-command)
   - [Deploy](#deploy)
2. [Docker Build](#docker-build)
3. [Navigating the Repository](#navigating-the-repository)
4. [Software Development Kit](#software-development-kit)
5. [Extending OpenMRS with Modules](#extending-openmrs-with-modules)
6. [Documentation](#documentation)
7. [Contributing](#contributing)
8. [Issues](#issues)
9. [Community](#community)
10. [Support](#support)
11. [License](#license)

---

# Build

## Prerequisites

### Java  
OpenMRS Core requires **Java JDK 8+** (depending on branch).

### Maven  
Install [Apache Maven](https://maven.apache.org/) and verify:

```bash
mvn -version
```

Ensure Maven uses the correct Java version.

### Git  
Clone the repository:

```bash
git clone https://github.com/openmrs/openmrs-core.git
```

---

## Build Command

Run:

```bash
cd openmrs-core
mvn clean package
```

This generates:

```
webapp/target/openmrs.war
```

---

## Deploy

For development, deploy via Jetty:

```bash
cd openmrs-core/webapp
mvn jetty:run
```

Custom port:

```bash
mvn -Djetty.http.port=8081 jetty:run
```

Using Cargo:

```bash
mvn cargo:run
```

Cargo with custom port:

```bash
mvn -Dcargo.servlet.port=8081 cargo:run
```

After the server starts, access:

```
http://localhost:8080/openmrs
```

---

# Docker Build

OpenMRS provides Docker-based development and production builds.

### Build development image:

```bash
docker compose build
```

Build with custom Maven arguments:

```bash
docker compose build --build-arg MVN_ARGS='install -DskipTests'
```

Run using Jetty inside Docker:

```bash
docker compose up
```

---

### Build production image:

```bash
docker compose -f docker-compose.yml build
```

Run production mode:

```bash
docker compose -f docker-compose.yml up
```

Run official CI-built image:

```bash
TAG=nightly docker compose -f docker-compose.yml up
```

---

# Navigating the Repository

| Directory | Description |
|----------|-------------|
| `api/`      | Java API source code |
| `tools/`    | Build/testing utilities |
| `web/`      | Backend code for the web application |
| `webapp/`   | Files used to build the final `.war` file |
| `pom.xml`   | Main Maven configuration |

---

# Software Development Kit

OpenMRS SDK makes development faster.

Docs:  
https://wiki.openmrs.org/display/docs/OpenMRS+SDK

---

# Extending OpenMRS with Modules

OpenMRS has a modular architecture.

Browse existing modules:  
https://addons.openmrs.org/

Module development guide:  
https://wiki.openmrs.org/display/docs/Modules

---

# Documentation

### Developer Guides  
- https://openmrs.atlassian.net/wiki/spaces/docs/pages/25477022/Getting+Started+as+a+Developer  
- https://openmrs.atlassian.net/wiki/spaces/Archives/pages/25506949/How-To+Setup+And+Use+Your+IDE  
- https://wiki.openmrs.org/display/docs/Pull+Request+Tips  

### Wiki  
https://wiki.openmrs.org  

### Website  
https://openmrs.org  

---

# Contributing

Contributions are welcome!  
Read contributor stages:  
https://wiki.openmrs.org/display/RES/OpenMRS+Developer+Stages

### Code  
Start with introductory issues:  
https://openmrs.atlassian.net/wiki/x/a8GEAQ

### Code Reviews  
https://openmrs.atlassian.net/wiki/x/-r_EAQ

### Translation  
https://explore.transifex.com/openmrs/OpenMRS/

---

# Issues

Report bugs or request features:  
https://issues.openmrs.org

---

# Community

Join discussions:

- **Talk Forum:** https://talk.openmrs.org  
- **IRC Channel:** http://irc.openmrs.org  
- **Telegram:** https://telegram.me/openmrs  
- **Wiki:** https://wiki.openmrs.org  

---

# Support

Need help?  
Ask on: https://talk.openmrs.org/

---

# License

Licensed under **MPL 2.0 w/ HD**  
https://openmrs.org/license/  
© OpenMRS Inc.
