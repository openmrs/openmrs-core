<img src="https://talk.openmrs.org/uploads/default/original/2X/f/f1ec579b0398cb04c80a54c56da219b2440fe249.jpg" alt="OpenMRS" width="100%"/>

# OpenMRS Core

[![Build Status](https://travis-ci.org/openmrs/openmrs-core.svg?branch=master)](https://travis-ci.org/openmrs/openmrs-core)
[![Coverage Status](https://coveralls.io/repos/github/openmrs/openmrs-core/badge.svg?branch=master)](https://coveralls.io/github/openmrs/openmrs-core?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a51303ee46c34775a7c31c8d6016da6b)](https://www.codacy.com/app/openmrs/openmrs-core?utm_source=github.com&utm_medium=referral&utm_content=openmrs/openmrs-core&utm_campaign=Badge_Grade)
[![DPG Badge](https://img.shields.io/badge/Verified-DPG-3333AB)](https://digitalpublicgoods.net/r/openmrs)

---

## Module Status

| Module | Status |
|-------|--------|
| **API** | [![API](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=api/pom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=api/pom.xml) |
| **Test** | [![Test](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=test/pom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=test/pom.xml) |
| **Tools** | [![Tools](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=tools/pom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=tools/pom.xml) |
| **Web** | [![Web](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=web/pom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=web/pom.xml) |
| **Webapp** | [![Webapp](https://snyk.io/test/github/openmrs/openmrs-core/badge.svg?targetFile=webapp/pom.xml)](https://snyk.io/test/github/openmrs/openmrs-core?targetFile=webapp/pom.xml) |

---

## Overview

**OpenMRS** is a patient-centered, open-source Electronic Medical Record (EMR) system designed for healthcare providers worldwide.

**Mission:**  
To improve healthcare delivery in resource-constrained environments through a global, collaborative community that builds a robust and scalable medical record platform.

---

## Table of Contents

1. [Build and Installation](#build-and-installation)  
2. [Docker Build](#docker-build)  
3. [Repository Structure](#repository-structure)  
4. [Software Development Kit (SDK)](#software-development-kit-sdk)  
5. [Modules](#modules)  
6. [Documentation](#documentation)  
7. [Contributing](#contributing)  
8. [Community & Support](#community--support)  
9. [License](#license)

---

## Build and Installation

### Prerequisites

#### Java  
OpenMRS requires **Java JDK 8+** (depending on the branch you are building).

#### Maven  
Install [Apache Maven](https://maven.apache.org/) and verify:

```bash
mvn -version
