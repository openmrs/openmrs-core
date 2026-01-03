# OpenMRS Core: Java to Kotlin Migration Log

This document tracks every step of the Java to Kotlin migration.

---

## Migration Overview

| Property | Value |
|----------|-------|
| **Start Date** | 2026-01-03 |
| **Branch** | `feature/kotlin-migration` |
| **Kotlin Version** | 2.1.0 |
| **Approach** | Layer-by-layer (base classes → entities → services → DAOs) |

---

## Phase 0: Build Configuration

**Date**: 2026-01-03

### Step 0.1: Create Feature Branch
```bash
git checkout -b feature/kotlin-migration
```

### Step 0.2: Configure Root pom.xml

Added Kotlin version properties:
```xml
<!-- In <properties> section -->
<kotlin.version>2.1.0</kotlin.version>
<kotlin.compiler.jvmTarget>21</kotlin.compiler.jvmTarget>
```

Added Kotlin dependencies to `<dependencyManagement>`:
```xml
<!-- Kotlin BOM -->
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-bom</artifactId>
    <version>${kotlin.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>${kotlin.version}</version>
</dependency>
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-reflect</artifactId>
    <version>${kotlin.version}</version>
</dependency>
<!-- MockK for Kotlin testing -->
<dependency>
    <groupId>io.mockk</groupId>
    <artifactId>mockk-jvm</artifactId>
    <version>1.13.13</version>
    <scope>test</scope>
</dependency>
```

Added kotlin-maven-plugin to `<pluginManagement>`:
```xml
<plugin>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-maven-plugin</artifactId>
    <version>${kotlin.version}</version>
    <configuration>
        <jvmTarget>${kotlin.compiler.jvmTarget}</jvmTarget>
        <compilerPlugins>
            <plugin>all-open</plugin>
            <plugin>no-arg</plugin>
            <plugin>jpa</plugin>
            <plugin>spring</plugin>
        </compilerPlugins>
        <pluginOptions>
            <option>all-open:annotation=jakarta.persistence.Entity</option>
            <option>all-open:annotation=jakarta.persistence.MappedSuperclass</option>
            <option>all-open:annotation=jakarta.persistence.Embeddable</option>
        </pluginOptions>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-maven-allopen</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-maven-noarg</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
    </dependencies>
</plugin>
```

### Step 0.3: Configure api/pom.xml

Added Kotlin dependencies:
```xml
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
</dependency>
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-reflect</artifactId>
</dependency>
<dependency>
    <groupId>io.mockk</groupId>
    <artifactId>mockk-jvm</artifactId>
    <scope>test</scope>
</dependency>
```

Added kotlin-maven-plugin for mixed compilation:
```xml
<plugin>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>compile</id>
            <phase>compile</phase>
            <goals><goal>compile</goal></goals>
            <configuration>
                <sourceDirs>
                    <sourceDir>${project.basedir}/src/main/kotlin</sourceDir>
                    <sourceDir>${project.basedir}/src/main/java</sourceDir>
                </sourceDirs>
            </configuration>
        </execution>
        <execution>
            <id>test-compile</id>
            <phase>test-compile</phase>
            <goals><goal>test-compile</goal></goals>
            <configuration>
                <sourceDirs>
                    <sourceDir>${project.basedir}/src/test/kotlin</sourceDir>
                    <sourceDir>${project.basedir}/src/test/java</sourceDir>
                </sourceDirs>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Configured Java compiler to run after Kotlin:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <executions>
        <execution>
            <id>default-compile</id>
            <phase>none</phase>
        </execution>
        <execution>
            <id>default-testCompile</id>
            <phase>none</phase>
        </execution>
        <execution>
            <id>java-compile</id>
            <phase>compile</phase>
            <goals><goal>compile</goal></goals>
        </execution>
        <execution>
            <id>java-test-compile</id>
            <phase>test-compile</phase>
            <goals><goal>testCompile</goal></goals>
        </execution>
    </executions>
</plugin>
```

### Step 0.4: Create Kotlin Source Directories
```bash
mkdir -p api/src/main/kotlin
mkdir -p api/src/test/kotlin
mkdir -p web/src/main/kotlin
mkdir -p web/src/test/kotlin
```

### Step 0.5: Verify Build
```bash
mvn clean compile -DskipTests -pl api -am
# Result: BUILD SUCCESS
```

**Files Modified**:
- `pom.xml` (+62 lines)
- `api/pom.xml` (+77 lines)

**Commit**: `[Phase 0] Configure Maven build for Kotlin 2.1.0`

---

## Phase 1: Utility Classes & Extensions

**Date**: 2026-01-03

### Step 1.1: Create Kotlin Extensions File

*TODO: Document after implementation*

---

## Migration Statistics

| Phase | Files Converted | Lines Removed | Lines Added | Net Change |
|-------|-----------------|---------------|-------------|------------|
| Phase 0 | 0 | 0 | 139 | +139 |
| Phase 1 | - | - | - | - |
| Phase 2 | - | - | - | - |
| Phase 3 | - | - | - | - |
| Phase 4 | - | - | - | - |

---

## Rollback Instructions

To rollback the migration:
```bash
git checkout master
git branch -D feature/kotlin-migration
```
