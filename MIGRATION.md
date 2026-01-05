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

### Step 2.2: Verify Build

```bash
export JAVA_HOME=/path/to/java/21
mvn clean compile -DskipTests -pl api -am
# Result: BUILD SUCCESS
```

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

| Java Class | Kotlin Class | Lines (Java→Kotlin) | Reduction |
|------------|--------------|---------------------|-----------|
| `BaseOpenmrsObject.java` | `BaseOpenmrsObject.kt` | 119→66 | 45% |
| `BaseOpenmrsData.java` | `BaseOpenmrsData.kt` | 222→68 | 69% |
| `BaseOpenmrsMetadata.java` | `BaseOpenmrsMetadata.kt` | 268→79 | 70% |

**Commit**: `[Phase 3] Migrate base classes and interfaces to Kotlin`

---

## Phase 4: Domain Entities

**Date**: 2026-01-05

### Tier 1: Core Entities (No Dependencies)

#### Step 4.1.1: Migrate Supporting Types

| Java File | Kotlin File | Notes |
|-----------|-------------|-------|
| `Address.java` | `Address.kt` | Interface with 22 address properties |
| `Attributable.java` | `Attributable.kt` | Generic interface for PersonAttribute values |
| `BaseChangeableOpenmrsMetadata.java` | `BaseChangeableOpenmrsMetadata.kt` | Marker class for mutable metadata |
| `BaseChangeableOpenmrsData.java` | `BaseChangeableOpenmrsData.kt` | Marker class for mutable data |

**Note**: `BaseCustomizableMetadata`, `BaseCustomizableData`, and `Customizable` remain in Java due to complex recursive generic type bounds that violate Kotlin's Finite Bound Restriction.

#### Step 4.1.2: Migrate Location

**File**: `Location.java` (857 lines) → `Location.kt` (~280 lines) = **67% reduction**

Key patterns:
```kotlin
// Properties replace getter/setter pairs
@Column(name = "address1")
override var address1: String? = null

// Collection operations with Kotlin stdlib
fun getChildLocations(includeRetired: Boolean): Set<Location> =
    if (includeRetired) childLocations ?: emptySet()
    else childLocations?.filterNot { it.retired == true }?.toSet() ?: emptySet()

// Safe exception handling
override fun findPossibleValues(searchText: String): List<Location> =
    runCatching { Context.getLocationService().getLocations(searchText) }
        .getOrDefault(emptyList())
```

#### Step 4.1.3: Migrate User

**File**: `User.java` (764 lines) → `User.kt` (~280 lines) = **63% reduction**

Key patterns:
```kotlin
// Computed property
val isSuperUser: Boolean
    get() = containsRole(RoleConstants.SUPERUSER)

// Collection operations
val allRoles: Set<Role>
    get() {
        val baseRoles = roles?.toMutableSet() ?: mutableSetOf()
        val totalRoles = baseRoles.toMutableSet()
        baseRoles.forEach { role -> totalRoles.addAll(role.allParentRoles) }
        return totalRoles
    }

// Privilege check
fun hasPrivilege(privilege: String?): Boolean {
    if (privilege.isNullOrEmpty()) return true
    if (isSuperUser) return true
    return allRoles.any { it.hasPrivilege(privilege) }
}
```

#### Step 4.1.4: Migrate Person

**File**: `Person.java` (1181 lines) → `Person.kt` (~530 lines) = **55% reduction**

Key patterns:
```kotlin
// Age calculation
val age: Int?
    get() = getAge(null)

fun getAge(onDate: Date?): Int? {
    if (birthdate == null) return null
    val today = Calendar.getInstance().apply {
        if (onDate != null) time = onDate
        if (deathDate != null && time.after(deathDate)) time = deathDate
    }
    // ... calculation
}

// Preferred name/address with null safety
val personName: PersonName?
    get() = names.firstOrNull { it.preferred == true && it.voided != true }
        ?: names.firstOrNull { it.voided != true }
```

#### Step 4.1.5: Migrate Concept

**File**: `Concept.java` (1705 lines) → `Concept.kt` (~680 lines) = **60% reduction**

Key patterns:
```kotlin
// Name lookup with locale fallback
fun getName(): ConceptName? {
    if (names.isEmpty()) return null
    for (currentLocale in LocaleUtility.getLocalesInOrder()) {
        getPreferredName(currentLocale)?.let { return it }
        getFullySpecifiedName(currentLocale)?.let { return it }
    }
    return names.firstOrNull { it.isFullySpecifiedName }
        ?: synonyms.firstOrNull()
}

// Grouped providers
fun getProvidersByRoles(includeVoided: Boolean = false): Map<EncounterRole, Set<Provider>> =
    encounterProviders
        .filter { includeVoided || it.voided != true }
        .groupBy({ it.encounterRole!! }, { it.provider!! })
        .mapValues { it.value.toSet() }
```

**Tier 1 Commit**: `Phase 4: Migrate Tier 1 entities to Kotlin (Person, Concept, User)`

### Tier 1 Summary

| Entity | Java Lines | Kotlin Lines | Reduction |
|--------|-----------|--------------|-----------|
| Location.kt | 857 | ~280 | 67% |
| User.kt | 764 | ~280 | 63% |
| Person.kt | 1181 | ~530 | 55% |
| Concept.kt | 1705 | ~680 | 60% |
| **Total** | **4507** | **~1770** | **61%** |

---

### Tier 2: Dependent Entities (Depend on Tier 1)

#### Step 4.2.1: Migrate Patient

**File**: `Patient.java` (413 lines) → `Patient.kt` (~180 lines) = **56% reduction**

Key patterns:
```kotlin
// Extends Person, syncs patientId with personId
class Patient() : Person() {
    private var _patientId: Int? = null

    fun setPatientId(patientId: Int?) {
        super.setPersonId(patientId)
        this._patientId = patientId
    }

    // Preferred identifier with null safety
    val patientIdentifier: PatientIdentifier?
        get() = identifiers.firstOrNull { it.preferred == true && it.voided != true }
            ?: identifiers.firstOrNull { it.voided != true }
}
```

#### Step 4.2.2: Migrate Provider

**File**: `Provider.java` (183 lines) → `Provider.kt` (~60 lines) = **67% reduction**

Key patterns:
```kotlin
@Audited
class Provider() : BaseCustomizableMetadata<ProviderAttribute>() {
    var providerId: Int? = null
    var person: Person? = null
    var identifier: String? = null
    var role: Concept? = null
    var speciality: Concept? = null
    var providerRole: ProviderRole? = null

    override var name: String?
        get() = person?.personName?.fullName
        set(value) { super.name = value }
}
```

#### Step 4.2.3: Migrate Encounter

**File**: `Encounter.java` (974 lines) → `Encounter.kt` (~330 lines) = **66% reduction**

Key patterns:
```kotlin
// Recursive obs traversal
var obs: MutableSet<Obs>
    get() {
        val ret = LinkedHashSet<Obs>()
        _obs?.forEach { o -> ret.addAll(getObsLeaves(o)) }
        return ret
    }
    set(value) { _obs = value }

private fun getObsLeaves(obsParent: Obs): List<Obs> {
    val leaves = mutableListOf<Obs>()
    if (obsParent.hasGroupMembers()) {
        for (child in obsParent.groupMembers ?: emptySet()) {
            if (child.voided != true) {
                if (!child.isObsGrouping) leaves.add(child)
                else leaves.addAll(getObsLeaves(child))
            }
        }
    } else if (obsParent.voided != true) {
        leaves.add(obsParent)
    }
    return leaves
}

// Add obs with attribute propagation using ArrayDeque
fun addObs(observation: Obs?) {
    observation ?: return
    _obs!!.add(observation)

    val obsToUpdate = ArrayDeque<Obs>()
    obsToUpdate.add(observation)
    val seenIt = LinkedHashSet<Obs>()

    while (obsToUpdate.isNotEmpty()) {
        val o = obsToUpdate.removeFirst()
        if (o in seenIt) continue
        seenIt.add(o)
        o.encounter = this
        o.obsDatetime = o.obsDatetime ?: encounterDatetime
        o.person = o.person ?: patient
        o.location = o.location ?: location
        o.getGroupMembers(true)?.let { obsToUpdate.addAll(it) }
    }
}
```

#### Step 4.2.4: Migrate Visit

**File**: `Visit.java` (276 lines) → `Visit.kt` (~110 lines) = **60% reduction**

Key patterns:
```kotlin
@Entity
@Table(name = "visit")
@Audited
class Visit() : BaseCustomizableData<VisitAttribute>(), Auditable, Customizable<VisitAttribute> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_id")
    var visitId: Int? = null

    var patient: Patient? = null
    var visitType: VisitType? = null
    var startDatetime: Date? = null
    var stopDatetime: Date? = null

    val nonVoidedEncounters: List<Encounter>
        get() = encounters.filterNot { it.voided == true }

    fun addEncounter(encounter: Encounter?) {
        encounter?.apply {
            visit = this@Visit
            encounters.add(this)
        }
    }
}
```

**Tier 2 Commit**: `Phase 4: Migrate Tier 2 entities to Kotlin (Patient, Provider, Encounter, Visit)`

### Tier 2 Summary

| Entity | Java Lines | Kotlin Lines | Reduction |
|--------|-----------|--------------|-----------|
| Patient.kt | 413 | ~180 | 56% |
| Provider.kt | 183 | ~60 | 67% |
| Encounter.kt | 974 | ~330 | 66% |
| Visit.kt | 276 | ~110 | 60% |
| **Total** | **1846** | **~680** | **63%** |

---

### Tier 3: Complex Entities (In Progress)

Entities to migrate:
- `Obs.java`
- `Order.java`
- `DrugOrder.java`
- `Diagnosis.java`

---

## Overall Migration Statistics

| Phase | Files Converted | Java Lines Removed | Kotlin Lines Added | Net Reduction |
|-------|-----------------|--------------------|--------------------|---------------|
| Phase 0 | 0 (config) | 0 | 139 | - |
| Phase 1 | 1 | 0 | 394 | - |
| Phase 2 | 7 | 243 | 363 | 0% (added features) |
| Phase 3 | 11 | 609 | 213 | 65% |
| Phase 4 Tier 1 | 4 | 4507 | ~1770 | 61% |
| Phase 4 Tier 2 | 4 | 1846 | ~680 | 63% |
| **Total** | **27** | **7205** | **~3559** | **51%** |

---

## Idiomatic Kotlin Patterns Reference

### 1. Properties Replace Getter/Setter Pairs
```kotlin
// Java: private String name; + getName() + setName()
// Kotlin:
var name: String? = null
```

### 2. Null-Safe Operators
```kotlin
// ?. - Safe call
patient?.name

// ?: - Elvis operator
name ?: "Unknown"

// ?.let - Execute if non-null
name?.let { println(it) }
```

### 3. Collection Operations
```kotlin
// filter, filterNot, map, firstOrNull, any, all
identifiers.filterNot { it.voided == true }
names.firstOrNull { it.preferred == true }
allRoles.any { it.hasPrivilege(privilege) }
```

### 4. Scope Functions
```kotlin
// apply - configure object
encounter.apply {
    encounterType = type
    patient = pat
}

// let - transform value
person?.let { it.givenName + " " + it.familyName }
```

### 5. runCatching for Exception Handling
```kotlin
runCatching { Context.getService().doSomething() }
    .getOrDefault(emptyList())
```

### 6. Backing Field Pattern for JPA
```kotlin
private var _personId: Int? = null

fun getPersonId(): Int? = _personId
fun setPersonId(personId: Int?) { _personId = personId }
```

---

## Build Commands

```bash
# Set Java 21 (required, Kotlin 2.1.0 doesn't support Java 25)
export JAVA_HOME=/path/to/java/21

# Compile API module
mvn clean compile -DskipTests -pl api -am

# Full build with tests
mvn clean verify -pl api -am
```

---

## Rollback Instructions

To rollback the migration:
```bash
git checkout master
git branch -D feature/kotlin-migration
```
