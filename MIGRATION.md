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

**File**: `api/src/main/kotlin/org/openmrs/util/OpenmrsExtensions.kt`

Created idiomatic Kotlin extension functions to replace static utility methods from `OpenmrsUtil.java`.

#### Extension Functions Created

**Null-Safe Comparison Extensions**:
```kotlin
fun Any?.nullSafeEquals(other: Any?): Boolean
fun String?.nullSafeEqualsIgnoreCase(other: String?): Boolean
```

**Date Extensions**:
```kotlin
fun Date.compareDates(other: Date): Int          // Handles Timestamp nanoseconds
fun Date?.compareWithNullAsEarliest(other: Date?): Int
fun Date?.compareWithNullAsLatest(other: Date?): Int
fun Date?.firstMomentOfDay(): Date?              // 00:00:00.000
fun Date?.lastMomentOfDay(): Date?               // 23:59:59.999
fun Date.safeCopy(): Date
fun Date?.isYesterday(): Boolean
```

**Collection Extensions**:
```kotlin
fun <T> Collection<T>?.safeContains(element: T?): Boolean  // Uses equals() not compareTo()
fun <T> Collection<T>.containsAny(elements: Collection<T>): Boolean
fun <K, V> MutableMap<K, MutableSet<V>>.addToSet(key: K, value: V)
fun <K, V> MutableMap<K, MutableList<V>>.addToList(key: K, value: V)
```

**String Extensions**:
```kotlin
fun String?.isInArray(array: Array<String>?): Boolean
fun String.startsWithAny(vararg prefixes: String): Boolean
fun String?.containsUpperAndLowerCase(): Boolean
fun String?.containsOnlyDigits(): Boolean
fun String?.containsDigit(): Boolean
fun String?.shortenedStackTrace(): String?       // Removes Spring/reflection frames
fun String?.parseParameterList(): Map<String, String>  // "key=val|key2=val2"
fun String.toIntList(delimiter: String = ","): List<Int>
```

**Comparable Extensions**:
```kotlin
fun <T : Comparable<T>> T?.compareWithNullAsLowest(other: T?): Int
fun <T : Comparable<T>> T?.compareWithNullAsGreatest(other: T?): Int
```

**Number Extensions**:
```kotlin
fun Long.toIntSafe(): Int  // Throws if value doesn't fit
```

#### Usage Examples

```kotlin
// Before (Java static methods):
OpenmrsUtil.nullSafeEquals(a, b)
OpenmrsUtil.collectionContains(collection, obj)
OpenmrsUtil.getLastMomentOfDay(date)

// After (Kotlin extensions):
a.nullSafeEquals(b)
collection.safeContains(obj)
date.lastMomentOfDay()
```

### Step 1.2: Verify Build

```bash
export JAVA_HOME=/path/to/java/21
mvn clean compile -DskipTests -pl api -am
# Result: BUILD SUCCESS
```

**Files Added**:
- `api/src/main/kotlin/org/openmrs/util/OpenmrsExtensions.kt` (+394 lines)

**Commit**: `[Phase 1] Add Kotlin extension functions for OpenmrsUtil`

---

## Phase 2: Enums & Simple Types

**Date**: 2026-01-05

### Step 2.1: Migrate Enums to Idiomatic Kotlin

Converted 7 Java enum files to idiomatic Kotlin with:
- KDoc documentation for all enum values
- `companion object` with `@JvmStatic fromString()` method for safe parsing
- Proper `@Deprecated` annotations with `ReplaceWith` hints

#### Files Migrated

| Java File | Kotlin File | Values | Notes |
|-----------|-------------|--------|-------|
| `AllergenType.java` | `AllergenType.kt` | 4 | Simple enum |
| `AllergySeverity.java` | `AllergySeverity.kt` | 5 | Simple enum |
| `ConditionClinicalStatus.java` | `ConditionClinicalStatus.kt` | 7 | Has deprecated `HISTORY_OF` |
| `ConditionVerificationStatus.java` | `ConditionVerificationStatus.kt` | 2 | Simple enum |
| `ConceptNameType.java` | `ConceptNameType.kt` | 3 | API package |
| `MatchMode.java` | `MatchMode.kt` | 4 | Has pattern methods |
| `PatientSearchMode.java` | `PatientSearchMode.kt` | 4 | Hibernate package |

#### Idiomatic Kotlin Patterns Applied

**1. Safe Parsing with `fromString()`**:
```kotlin
companion object {
    @JvmStatic
    fun fromString(value: String?): AllergenType? =
        value?.uppercase()?.let { name ->
            entries.firstOrNull { it.name == name }
        }
}
```

**2. Kotlin Deprecation with ReplaceWith**:
```kotlin
@Deprecated("as of 2.6.0", ReplaceWith("REMISSION"))
HISTORY_OF,
```

**3. Idiomatic String Templates (MatchMode)**:
```kotlin
// Java switch statement → Kotlin when expression
return when (this) {
    START -> "$processedStr%"
    END -> "%$processedStr"
    ANYWHERE -> "%$processedStr%"
    EXACT -> processedStr
}
```

### Step 2.2: Verify Build

```bash
export JAVA_HOME=/path/to/java/21
mvn clean compile -DskipTests -pl api -am
# Result: BUILD SUCCESS (767 Java + 8 Kotlin source files)
```

**Files Removed** (Java):
- `api/src/main/java/org/openmrs/AllergenType.java`
- `api/src/main/java/org/openmrs/AllergySeverity.java`
- `api/src/main/java/org/openmrs/ConditionClinicalStatus.java`
- `api/src/main/java/org/openmrs/ConditionVerificationStatus.java`
- `api/src/main/java/org/openmrs/api/ConceptNameType.java`
- `api/src/main/java/org/openmrs/api/db/hibernate/MatchMode.java`
- `api/src/main/java/org/openmrs/api/db/hibernate/PatientSearchMode.java`

**Files Added** (Kotlin):
- `api/src/main/kotlin/org/openmrs/AllergenType.kt`
- `api/src/main/kotlin/org/openmrs/AllergySeverity.kt`
- `api/src/main/kotlin/org/openmrs/ConditionClinicalStatus.kt`
- `api/src/main/kotlin/org/openmrs/ConditionVerificationStatus.kt`
- `api/src/main/kotlin/org/openmrs/api/ConceptNameType.kt`
- `api/src/main/kotlin/org/openmrs/api/db/hibernate/MatchMode.kt`
- `api/src/main/kotlin/org/openmrs/api/db/hibernate/PatientSearchMode.kt`

**Commit**: `[Phase 2] Migrate enums to idiomatic Kotlin`

---

## Phase 3: Base Classes & Interfaces

**Date**: 2026-01-05

### Step 3.1: Migrate Core Interfaces

Converted 8 Java interfaces to idiomatic Kotlin with interface properties.

| Java Interface | Kotlin Interface | Extends | Properties |
|----------------|------------------|---------|------------|
| `OpenmrsObject.java` | `OpenmrsObject.kt` | - | `id`, `uuid` |
| `Creatable.java` | `Creatable.kt` | OpenmrsObject | `creator`, `dateCreated` |
| `Changeable.java` | `Changeable.kt` | OpenmrsObject | `changedBy`, `dateChanged` |
| `Auditable.java` | `Auditable.kt` | Creatable, Changeable | (marker) |
| `Voidable.java` | `Voidable.kt` | OpenmrsObject | `voided`, `voidedBy`, `dateVoided`, `voidReason` |
| `Retireable.java` | `Retireable.kt` | OpenmrsObject | `retired`, `retiredBy`, `dateRetired`, `retireReason` |
| `OpenmrsData.java` | `OpenmrsData.kt` | Auditable, Voidable | (deprecated changeBy/dateChanged) |
| `OpenmrsMetadata.java` | `OpenmrsMetadata.kt` | Auditable, Retireable | `name`, `description` |

### Step 3.2: Migrate Abstract Base Classes

Converted 3 Java abstract classes to Kotlin with JPA annotations preserved.

| Java Class | Kotlin Class | Lines (Java→Kotlin) | Notes |
|------------|--------------|---------------------|-------|
| `BaseOpenmrsObject.java` | `BaseOpenmrsObject.kt` | 119→66 | 45% reduction |
| `BaseOpenmrsData.java` | `BaseOpenmrsData.kt` | 222→68 | 69% reduction |
| `BaseOpenmrsMetadata.java` | `BaseOpenmrsMetadata.kt` | 268→79 | 70% reduction |

#### Idiomatic Kotlin Patterns Applied

**1. Interface Properties (replaces getter/setter pairs)**:
```kotlin
// Java: public String getUuid(); public void setUuid(String uuid);
// Kotlin:
interface OpenmrsObject {
    var uuid: String
}
```

**2. Default Interface Implementation with `get()`**:
```kotlin
interface Voidable : OpenmrsObject {
    @get:JsonIgnore
    @Deprecated("as of 2.0, use voided property")
    val isVoided: Boolean?
        get() = voided  // Default implementation

    var voided: Boolean?
}
```

**3. Kotlin Properties with JPA Annotations**:
```kotlin
@MappedSuperclass
@Audited
abstract class BaseOpenmrsObject : Serializable, OpenmrsObject {
    @Column(name = "uuid", unique = true, nullable = false, length = 38, updatable = false)
    override var uuid: String = UUID.randomUUID().toString()
}
```

**4. Idiomatic equals/hashCode**:
```kotlin
override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BaseOpenmrsObject) return false

    val thisClass = Hibernate.getClass(this)
    val otherClass = Hibernate.getClass(other)

    if (!(thisClass.isAssignableFrom(otherClass) || otherClass.isAssignableFrom(thisClass))) {
        return false
    }
    return this.uuid == other.uuid
}
```

### Step 3.3: Verify Build

```bash
export JAVA_HOME=/path/to/java/21
mvn clean compile -DskipTests -pl api -am
# Result: BUILD SUCCESS
```

**Files Removed** (Java - 11 files):
- Interfaces: `OpenmrsObject.java`, `Creatable.java`, `Changeable.java`, `Auditable.java`, `Voidable.java`, `Retireable.java`, `OpenmrsData.java`, `OpenmrsMetadata.java`
- Classes: `BaseOpenmrsObject.java`, `BaseOpenmrsData.java`, `BaseOpenmrsMetadata.java`

**Files Added** (Kotlin - 11 files):
- Interfaces: `OpenmrsObject.kt`, `Creatable.kt`, `Changeable.kt`, `Auditable.kt`, `Voidable.kt`, `Retireable.kt`, `OpenmrsData.kt`, `OpenmrsMetadata.kt`
- Classes: `BaseOpenmrsObject.kt`, `BaseOpenmrsData.kt`, `BaseOpenmrsMetadata.kt`

**Commit**: `[Phase 3] Migrate base classes and interfaces to Kotlin`

---

## Migration Statistics

| Phase | Files Converted | Lines Removed | Lines Added | Net Change |
|-------|-----------------|---------------|-------------|------------|
| Phase 0 | 0 | 0 | 139 | +139 |
| Phase 1 | 1 | 0 | 394 | +394 |
| Phase 2 | 7 | 243 | 363 | +120 |
| Phase 3 | 11 | 609 | 425 | -184 |
| Phase 4 | - | - | - | - |

---

## Rollback Instructions

To rollback the migration:
```bash
git checkout master
git branch -D feature/kotlin-migration
```
