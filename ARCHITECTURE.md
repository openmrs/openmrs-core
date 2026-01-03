# OpenMRS Core Architecture Documentation

> **Purpose**: This document provides a comprehensive architectural overview of OpenMRS Core to support the migration from Java to Kotlin.

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Module Structure](#2-module-structure)
3. [Domain Model Architecture](#3-domain-model-architecture)
4. [Service Layer Architecture](#4-service-layer-architecture)
5. [Data Access Layer (DAO)](#5-data-access-layer-dao)
6. [AOP & Interceptors](#6-aop--interceptors)
7. [Configuration Patterns](#7-configuration-patterns)
8. [Dependencies & Frameworks](#8-dependencies--frameworks)
9. [Kotlin Migration Considerations](#9-kotlin-migration-considerations)

---

## 1. Project Overview

| Property | Value |
|----------|-------|
| **Version** | 3.0.0-SNAPSHOT |
| **Build Tool** | Apache Maven 3.8.0+ |
| **Java Version** | 21 (with support for 17, 24) |
| **License** | Mozilla Public License 2.0 |
| **Type** | Healthcare EMR (Electronic Medical Record) System |

### Directory Structure

```
openmrs-core-fork/
├── pom.xml                    # Root POM (7 modules)
├── api/                       # Core business logic & domain models
├── web/                       # Web layer components
├── webapp/                    # WAR packaging (deployable)
├── tools/                     # Build tools & formatters
├── test/                      # Test dependencies POM
├── liquibase/                 # Database migrations
├── test-suite/                # Integration & performance tests
├── docker-compose.yml         # Development environment
└── Dockerfile                 # Production build
```

---

## 2. Module Structure

The project is organized as a multi-module Maven project with the following build order:

```
tools → test → api → web → webapp → liquibase → test-suite
```

### Module Details

| Module | Artifact ID | Type | Purpose |
|--------|-------------|------|---------|
| **api** | openmrs-api | JAR | Core service interfaces, domain models, business logic |
| **web** | openmrs-web | JAR | Web layer controllers, request handlers |
| **webapp** | openmrs-webapp | WAR | Deployable web application |
| **tools** | openmrs-tools | JAR | Build tools, formatters, utilities |
| **test** | openmrs-test | POM | Centralized test dependencies |
| **liquibase** | openmrs-liquibase | JAR | Database schema versioning |
| **test-suite** | openmrs-test-suite | POM | Integration & performance tests |

### API Module Package Structure

```
org.openmrs
├── (domain models)           # Entity classes (Patient, Person, Concept, etc.)
├── api/                      # Service interfaces
│   ├── impl/                 # Service implementations
│   ├── db/                   # DAO interfaces
│   │   └── hibernate/        # Hibernate DAO implementations
│   ├── context/              # Context/security management
│   ├── cache/                # Caching layer
│   └── handler/              # Data handlers
├── aop/                      # Aspect-oriented programming
├── annotation/               # Custom annotations
├── attribute/                # Attribute management system
├── customdatatype/           # Custom data types
├── validator/                # Data validation
├── util/                     # Utility classes
├── hl7/                      # HL7 message handling
├── scheduler/                # Task scheduling
├── module/                   # Module loading system
└── notification/             # Alert/notification system
```

---

## 3. Domain Model Architecture

### Inheritance Hierarchy

OpenMRS uses a well-defined inheritance hierarchy for all domain objects:

```
OpenmrsObject (interface)
└── BaseOpenmrsObject (abstract, @MappedSuperclass, @Audited)
    │
    ├── BaseOpenmrsData (abstract) → OpenmrsData (interface)
    │   └── BaseChangeableOpenmrsData (abstract)
    │       ├── BaseCustomizableData<A> (generic, supports attributes)
    │       ├── BaseFormRecordableOpenmrsData (implements FormRecordable)
    │       └── (Clinical entities: Person, Encounter, Obs, etc.)
    │
    └── BaseOpenmrsMetadata (abstract) → OpenmrsMetadata (interface)
        └── BaseChangeableOpenmrsMetadata (abstract)
            ├── BaseCustomizableMetadata<A> (generic, supports attributes)
            └── (Metadata entities: EncounterType, ConceptClass, etc.)

Special Cases:
├── Concept (extends BaseOpenmrsObject, implements Auditable, Retireable)
│   ├── ConceptNumeric
│   └── ConceptComplex
└── User (extends BaseOpenmrsObject, implements Auditable, Retireable)
```

### Key Interfaces

| Interface | Purpose |
|-----------|---------|
| `OpenmrsObject` | Base interface: provides `getId()`, `setId()`, `getUuid()`, `setUuid()` |
| `Creatable` | Tracks `creator` and `dateCreated` |
| `Changeable` | Tracks `changedBy` and `dateChanged` |
| `Auditable` | Combines Creatable and Changeable |
| `Voidable` | Soft delete for clinical data (void/unvoid) |
| `Retireable` | Soft delete for metadata (retire/unretire) |
| `Customizable<A>` | Supports custom user-defined attributes |
| `FormRecordable` | Tracks form field path and namespace |

### Core Entity Classes

#### Clinical Data Entities (extend BaseOpenmrsData)

| Entity | Location | Description |
|--------|----------|-------------|
| `Person` | `api/.../Person.java` | Demographics, addresses, names, attributes |
| `Patient` | `api/.../Patient.java` | Extends Person, adds patient identifiers |
| `Encounter` | `api/.../Encounter.java` | Patient-provider interaction event |
| `Obs` | `api/.../Obs.java` | Clinical observation/measurement |
| `Order` | `api/.../Order.java` | Clinical order (test, medication, referral) |
| `Visit` | `api/.../Visit.java` | Collection of encounters over time |
| `Diagnosis` | `api/.../Diagnosis.java` | Clinical diagnosis |
| `Condition` | `api/.../Condition.java` | Patient condition |
| `Allergy` | `api/.../Allergy.java` | Patient allergy record |

#### Order Subclasses

| Entity | Description |
|--------|-------------|
| `DrugOrder` | Medication prescription |
| `TestOrder` | Laboratory/diagnostic test order |
| `ReferralOrder` | Referral to another provider |
| `ServiceOrder` | General service order |

#### Metadata Entities (extend BaseOpenmrsMetadata)

| Entity | Description |
|--------|-------------|
| `Concept` | Medical dictionary term |
| `ConceptNumeric` | Numeric concept with ranges |
| `ConceptComplex` | Complex data (images, documents) |
| `Location` | Healthcare facility |
| `Provider` | Healthcare provider |
| `EncounterType` | Type of encounter |
| `VisitType` | Type of visit |
| `OrderType` | Type of order |
| `PatientIdentifierType` | Type of patient ID |
| `PersonAttributeType` | Type of person attribute |
| `Drug` | Medication definition |
| `Form` | Data entry form |
| `Program` | Care program |
| `Role` | User role |
| `Privilege` | System privilege |

### Entity Relationships

```
Patient (who)
├── PatientIdentifiers[]      (MRN, passport, etc.)
├── PersonNames[]             (names in different formats/languages)
├── PersonAddresses[]         (current/previous addresses)
├── PersonAttributes[]        (custom attributes)
├── Encounters[]              (clinical interactions)
│   ├── Obs[]                 (observations)
│   ├── Orders[]              (orders placed)
│   ├── Diagnoses[]           (diagnoses made)
│   ├── EncounterProviders[]  (providers involved)
│   └── Visit                 (parent visit)
├── Allergies[]
└── Conditions[]

Concept (vocabulary)
├── ConceptNames[]            (multilingual names)
├── ConceptAnswers[]          (possible coded answers)
├── ConceptDescriptions[]     (multilingual descriptions)
├── ConceptMaps[]             (external terminology mappings)
└── ConceptAttributes[]       (custom attributes)

Location (where)
├── parentLocation            (hierarchical structure)
├── childLocations[]
├── LocationTags[]            (categorization)
└── LocationAttributes[]      (custom attributes)
```

---

## 4. Service Layer Architecture

### Pattern Overview

OpenMRS implements a clean service layer with interface/implementation separation:

```
Controller → Service Interface → Service Implementation → DAO
```

### Service Locations

| Layer | Package |
|-------|---------|
| Interfaces | `org.openmrs.api.*` |
| Implementations | `org.openmrs.api.impl.*` |

### Core Services

| Service | Responsibilities |
|---------|------------------|
| `PatientService` | Patient CRUD, merge, search |
| `PersonService` | Person demographics management |
| `ConceptService` | Concept dictionary operations |
| `EncounterService` | Encounter management |
| `ObsService` | Observation CRUD |
| `OrderService` | Order management, discontinuation |
| `VisitService` | Visit management |
| `LocationService` | Location hierarchy management |
| `ProviderService` | Provider management |
| `UserService` | User account management |
| `FormService` | Form definitions |
| `ProgramService` | Care program management |
| `CohortService` | Patient cohort management |
| `AdministrationService` | System configuration |
| `HL7Service` | HL7 message processing |

### Service Implementation Pattern

```java
@Service("patientService")
@Transactional
public class PatientServiceImpl extends BaseOpenmrsService implements PatientService {

    private PatientDAO dao;

    @Autowired
    public PatientServiceImpl(PatientDAO dao) {
        this.dao = dao;
    }

    @Override
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    public Patient getPatient(Integer patientId) {
        return dao.getPatient(patientId);
    }
}
```

### Service Context

The `ServiceContext` singleton manages all service instances and provides centralized access:

```java
// Accessing services
PatientService patientService = Context.getPatientService();
ConceptService conceptService = Context.getConceptService();
```

---

## 5. Data Access Layer (DAO)

### Pattern Overview

DAO interfaces define the data access contract, with Hibernate implementations:

```
Service → DAO Interface → Hibernate DAO Implementation → SessionFactory → Database
```

### DAO Locations

| Layer | Package |
|-------|---------|
| Interfaces | `org.openmrs.api.db.*` |
| Implementations | `org.openmrs.api.db.hibernate.*` |

### DAO Implementation Pattern

```java
@Repository("patientDAO")
public class HibernatePatientDAO implements PatientDAO {

    private final SessionFactory sessionFactory;
    private final SearchSessionFactory searchSessionFactory;

    @Autowired
    public HibernatePatientDAO(SessionFactory sessionFactory,
                                SearchSessionFactory searchSessionFactory) {
        this.sessionFactory = sessionFactory;
        this.searchSessionFactory = searchSessionFactory;
    }

    @Override
    public Patient getPatient(Integer patientId) {
        return sessionFactory.getCurrentSession().get(Patient.class, patientId);
    }
}
```

### Hibernate Configuration

- **Entity Mappings**: 41 Hibernate mapping files in `api/src/main/resources/org/openmrs/api/db/hibernate/`
- **Configuration**: `api/src/main/resources/hibernate.cfg.xml`
- **Caching**: Selective L2 cache with Infinispan
- **Search**: Hibernate Search with Lucene/Elasticsearch backends

---

## 6. AOP & Interceptors

### AOP Execution Order

OpenMRS uses extensive AOP for cross-cutting concerns, applied in this order:

| Order | Advisor | Purpose |
|-------|---------|---------|
| 1 | `AuthorizationAdvice` | Privilege checks via `@Authorized` |
| 2 | `LoggingAdvice` | Method call logging |
| 3 | `RequiredDataAdvice` | Automatic audit field population |
| 4 | `CacheInterceptor` | `@Cacheable` method caching |
| 5 | `TransactionInterceptor` | `@Transactional` management |

### AOP Configuration

```java
@Configuration
@EnableTransactionManagement(order = 5, proxyTargetClass = true)
@EnableCaching(order = 4, proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AOPConfig { ... }
```

### Key AOP Advisors

#### AuthorizationAdvice
Checks user privileges before service method execution using `@Authorized` annotation:

```java
@Authorized(PrivilegeConstants.GET_PATIENTS)
public Patient getPatient(Integer patientId);
```

#### RequiredDataAdvice
Automatically sets audit fields and calls handler chain:

- Sets `creator`, `dateCreated` on save
- Sets `changedBy`, `dateChanged` on update
- Sets `voidedBy`, `dateVoided`, `voidReason` on void
- Sets `retiredBy`, `dateRetired`, `retireReason` on retire

### Handler Pattern

Pluggable handlers for lifecycle events:

```java
@Handler(supports = Patient.class, order = 1)
public class PatientSaveHandler implements SaveHandler<Patient> {
    @Override
    public void handle(Patient patient, User creator, Date dateCreated, String reason) {
        // Custom save logic
    }
}
```

Handler types: `SaveHandler`, `VoidHandler`, `UnvoidHandler`, `RetireHandler`, `UnretireHandler`

### Hibernate Interceptors

| Interceptor | Purpose |
|-------------|---------|
| `AuditableInterceptor` | Sets audit fields on insert/update |
| `ImmutableObsInterceptor` | Prevents Obs modification after save |
| `ImmutableOrderInterceptor` | Prevents Order modification after save |
| `DropMillisecondsHibernateInterceptor` | Normalizes datetime precision |
| `ChainingInterceptor` | Orchestrates multiple interceptors |

---

## 7. Configuration Patterns

### Spring Configuration

| Type | Location | Purpose |
|------|----------|---------|
| XML | `api/.../applicationContext-service.xml` | Service beans, DAOs, validators |
| XML | `web/.../openmrs-servlet.xml` | Web configuration |
| XML | `webapp/.../WEB-INF/web.xml` | Servlet configuration |
| Java | `api/.../aop/AOPConfig.java` | AOP configuration |

### Spring Context Loading

Context files loaded on startup (from web.xml):

1. `classpath:applicationContext-service.xml` - Services
2. `classpath:openmrs-servlet.xml` - Servlet configuration
3. `classpath*:/moduleApplicationContext.xml` - Module contexts
4. `classpath*:/webModuleApplicationContext.xml` - Web module contexts

### Web Entry Points

| Servlet | URL Pattern | Purpose |
|---------|-------------|---------|
| `DispatcherServlet` | `/ws/*` | REST API requests |
| `StaticDispatcherServlet` | `/scripts/*` | Static content |
| `ModuleServlet` | `/moduleServlet/*`, `/ms/*` | Module requests |
| `ModuleResourcesServlet` | `/moduleResources/*` | Module resources |
| `OpenmrsJspServlet` | JSP handling | View rendering |

---

## 8. Dependencies & Frameworks

### Core Framework Versions

| Framework | Version | Kotlin Compatibility |
|-----------|---------|---------------------|
| Spring Framework | 6.2.12 | Excellent |
| Hibernate ORM | 6.6.23.Final | Excellent |
| Hibernate Validator | 9.1.0.Final | Excellent |
| Hibernate Search | 7.1.2.Final | Excellent |
| Jakarta Servlet | 6.1.0 | Excellent |
| AspectJ | 1.9.25.1 | Good |

### Persistence & Caching

| Library | Version |
|---------|---------|
| Hibernate Core | 6.6.23.Final |
| Hibernate C3P0 | 6.6.23.Final |
| Hibernate Envers | 6.6.23.Final |
| Infinispan | 15.2.2.Final |
| Lucene | 10.3.2 |

### Database Drivers

| Database | Driver Version |
|----------|----------------|
| MySQL | 8.0.30 |
| PostgreSQL | 42.7.8 |
| MariaDB | 3.5.7 |

### Data Serialization

| Library | Version | Purpose |
|---------|---------|---------|
| Jackson | 2.19.2 | JSON processing |
| jackson-datatype-hibernate6 | 2.19.2 | Hibernate lazy loading |
| jackson-datatype-jsr310 | 2.19.2 | Java 8+ Date/Time |
| XStream | 1.4.21 | XML serialization |

### Testing

| Library | Version |
|---------|---------|
| JUnit 5 (Jupiter) | 5.11.4 |
| JUnit 4 (Legacy) | 4.13.2 |
| Mockito | 5.21.0 |
| Hamcrest | 3.0 |
| DBUnit | 3.0.0 |
| TestContainers | 2.0.3 |
| H2 Database | 2.3.232 |

### Apache Commons

| Library | Version |
|---------|---------|
| commons-lang3 | 3.20.0 |
| commons-io | 2.21.0 |
| commons-collections | 3.2.2 |
| commons-beanutils | 1.11.0 |
| commons-fileupload | 1.6.0 |
| commons-validator | 1.10.1 |

### Logging

| Library | Version |
|---------|---------|
| SLF4J | 1.7.36 |
| Log4j 2 | 2.25.3 |

### Healthcare Standards

| Library | Version |
|---------|---------|
| HAPI HL7 | 2.1 (v23, v24, v25, v26 structures) |

### Security

| Library | Version |
|---------|---------|
| OWASP CSRFGuard | 4.5.0-jakarta |
| OWASP Encoder | 1.4.0 |

### Utilities

| Library | Version |
|---------|---------|
| Guava | 33.5.0-jre |
| Liquibase | 4.32.0 |
| Velocity | 1.7 |
| GraalVM JS | 25.0.1 |

---

## 9. Kotlin Migration Considerations

### Positive Factors

1. **Jakarta Namespace Ready**: Already migrated from `javax.*` to `jakarta.*`
2. **Modern Framework Versions**: Spring 6.x and Hibernate 6.x have excellent Kotlin support
3. **Constructor Injection**: Widely used pattern translates directly to Kotlin
4. **Java 21 Target**: Can leverage latest Kotlin/JVM features
5. **No Java Records**: Entities are mutable classes, convertible to Kotlin data classes
6. **Interface-Based Design**: Clean service/DAO interfaces work well with Kotlin

### Migration Strategy Recommendations

#### Phase 1: Build Configuration
```kotlin
// Add to root pom.xml
<kotlin.version>1.9.x</kotlin.version>
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
</dependency>
```

#### Phase 2: Domain Models
Start with simpler entities and work toward complex ones:

**Priority Order**:
1. Base classes (`BaseOpenmrsObject`, `BaseOpenmrsData`, `BaseOpenmrsMetadata`)
2. Simple entities (`Role`, `Privilege`, `LocationTag`)
3. Core metadata (`EncounterType`, `VisitType`, `OrderType`)
4. Complex entities (`Person`, `Patient`, `Encounter`, `Obs`)

**Entity Conversion Example**:

```java
// Java
@Entity
public class Role extends BaseChangeableOpenmrsMetadata {
    private Set<Privilege> privileges;

    public Set<Privilege> getPrivileges() { return privileges; }
    public void setPrivileges(Set<Privilege> privileges) {
        this.privileges = privileges;
    }
}
```

```kotlin
// Kotlin
@Entity
class Role : BaseChangeableOpenmrsMetadata() {
    var privileges: Set<Privilege> = mutableSetOf()
}
```

#### Phase 3: Service Layer
- Convert service implementations to Kotlin classes
- Use Kotlin's `?.` operator for null safety
- Replace Java streams with Kotlin collection operations

#### Phase 4: DAO Layer
- Convert Hibernate DAOs to Kotlin
- Use Kotlin's type-safe builders for Criteria queries

### Key Considerations

#### Null Safety
- Audit fields (`creator`, `changedBy`) can be null - use `?` types
- IDs are nullable before persist - use `Int?`
- Collections should never be null - initialize as empty

#### JPA Annotations
- Place annotations on properties (not getters) in Kotlin
- Use `open` classes or Kotlin AllOpen plugin for JPA proxies
- Use `data class` only for value objects, not entities

#### Build Configuration
Required Kotlin compiler plugins:

```xml
<plugin>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-maven-plugin</artifactId>
    <configuration>
        <compilerPlugins>
            <plugin>all-open</plugin>
            <plugin>no-arg</plugin>
            <plugin>jpa</plugin>
        </compilerPlugins>
    </configuration>
</plugin>
```

#### Testing
- Use `mockito-kotlin` extension for cleaner mocking
- Kotlin coroutines can be added for async operations
- JUnit 5 works seamlessly with Kotlin

### Files to Convert First

1. **Base Classes** (foundational):
   - `api/src/main/java/org/openmrs/BaseOpenmrsObject.java`
   - `api/src/main/java/org/openmrs/BaseOpenmrsData.java`
   - `api/src/main/java/org/openmrs/BaseOpenmrsMetadata.java`

2. **Simple Entities** (low risk):
   - `api/src/main/java/org/openmrs/Privilege.java`
   - `api/src/main/java/org/openmrs/Role.java`
   - `api/src/main/java/org/openmrs/LocationTag.java`

3. **Utility Classes** (standalone):
   - `api/src/main/java/org/openmrs/util/*.java`

### Avoiding Common Pitfalls

1. **Lazy Loading**: Use `lateinit var` or nullable types for lazy-loaded associations
2. **Equals/HashCode**: Don't use `data class` for entities - implement manually using ID
3. **Open Classes**: JPA entities must be `open` for proxy generation
4. **Collection Initialization**: Always initialize collections to avoid null issues
5. **Companion Objects**: Use for static-like factory methods

---

## Appendix: File Locations Reference

### Configuration Files
- `/api/src/main/resources/applicationContext-service.xml` - Spring beans
- `/api/src/main/resources/hibernate.cfg.xml` - Hibernate config
- `/web/src/main/resources/openmrs-servlet.xml` - Web config
- `/webapp/src/main/webapp/WEB-INF/web.xml` - Servlet config

### Domain Models
- `/api/src/main/java/org/openmrs/*.java` - Entity classes

### Services
- `/api/src/main/java/org/openmrs/api/*.java` - Service interfaces
- `/api/src/main/java/org/openmrs/api/impl/*.java` - Service implementations

### DAOs
- `/api/src/main/java/org/openmrs/api/db/*.java` - DAO interfaces
- `/api/src/main/java/org/openmrs/api/db/hibernate/*.java` - Hibernate implementations

### Validators
- `/api/src/main/java/org/openmrs/validator/*.java` - Entity validators

### AOP
- `/api/src/main/java/org/openmrs/aop/*.java` - AOP advisors and config

### Handlers
- `/api/src/main/java/org/openmrs/api/handler/*.java` - Lifecycle handlers
