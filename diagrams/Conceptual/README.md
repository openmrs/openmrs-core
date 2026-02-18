# OpenMRS Conceptual Domain Models

This directory contains PlantUML diagrams that represent the conceptual domain models for OpenMRS Core. These models focus on the essential domain concepts and their relationships, abstracting away implementation details and database-specific entities.

## Domain Models

### 1. Patient Management Domain Model
**File:** `patient-management-domain-model.puml`

The foundational domain model that defines core entities shared across multiple OpenMRS domain models:

- **Person and Patient entities**: Demographics, identifiers, names, addresses
- **Clinical care episodes**: Visit, Encounter, Observation
- **Healthcare providers**: Provider, ProviderRole  
- **Locations**: Care delivery locations and facilities
- **Program management**: Program, ProgramWorkflow, PatientProgram, PatientState

This model serves as the base for other domain models and contains entities marked as [SHARED ENTITY] that are referenced by other models.

### 2. Clinical Data Management Domain Model
**File:** `clinical-data-management-domain-model.puml`

Extends the Patient Management model with clinical data capture and documentation entities:

- **Enhanced clinical episodes**: EncounterType, ObservationGroup, ReferenceRange
- **Conditions and diagnoses**: Condition, Diagnosis, ClinicalStatus, VerificationStatus
- **Allergies and reactions**: Allergy, AllergenType, AllergySeverity, AllergyReaction
- **Clinical vocabulary**: ClinicalConcept, ConceptClass, ConceptDatatype, ConceptMap
- **Clinical forms**: ClinicalForm, FormField, Field, FieldType
- **Order references**: References order entities from the Order Management domain

### 3. Clinical Decision Support Domain Model
**File:** `clinical-decision-support-domain-model.puml`

Comprehensive model for clinical decision support, rule-based alerts, and care recommendations:

#### Core Decision Support Entities
- **ClinicalDecisionRule**: Executable clinical business rules for patient care decisions
- **RuleEvaluationContext**: Evaluation context with index date and global parameters
- **RuleLogicCriteria**: Logical conditions and data queries for rule evaluation
- **DecisionEvaluation**: Records rule execution against patient data with results

#### Alert and Notification System
- **DecisionSupportAlert**: Automated notifications to clinicians based on rule outcomes
- **AlertRecipient**: Recipients of alerts with read status and acknowledgment
- **ClinicalReminder**: Proactive reminders for preventive care and follow-ups
- **PatientReminder**: Patient-specific reminders with scheduling and status

#### Clinical Recommendations
- **ClinicalRecommendation**: Evidence-based suggestions for clinical actions
- **RecommendationAction**: Specific actions suggested with urgency and assignment
- **GuidelineRecommendation**: Recommendations from clinical practice guidelines
- **ClinicalGuideline**: Evidence-based clinical practice guidelines

#### Rule Engine and Knowledge Base
- **ClinicalKnowledgeBase**: Repository of clinical rules and knowledge
- **KnowledgeRule**: Organized clinical rules with categories and dependencies
- **RuleLibrary**: Collections of rules organized by namespace and version
- **DecisionTree**: Structured decision logic with nodes and conditions

#### Data Integration
- **RuleDataSource**: Abstraction layer for accessing clinical data sources
- **HealthDataSource**: External data sources providing clinical information
- **EvaluationResult**: Structured results with data types and confidence levels
- **EvaluationCache**: Performance optimization for rule evaluation results

### 4. Order Management Domain Model
**File:** `order-management-domain-model.puml`

Comprehensive model for clinical order management including medications, tests, and services:

#### Core Order Entities
- **ClinicalOrder**: Base order entity with activation dates, urgency, fulfiller status
- **OrderAction**: Workflow actions (New, Revise, Discontinue, Renew)
- **OrderType**: Classification of order types (Drug, Test, Referral)
- **CareSetting**: Scope of care (Inpatient, Outpatient, Emergency)
- **OrderGroup**: Groups related orders for treatment protocols

#### Medication Orders
- **MedicationOrder**: Prescription orders with dosing details
- **Medication**: Pharmaceutical products with ingredients and strength
- **OrderFrequency**: Standardized frequency patterns
- **DosingInstructions**: Complex dosing and administration instructions
- **MedicationDispense**: Record of medication supply to patients

#### Service Orders
- **TestOrder**: Laboratory and diagnostic test orders
- **ReferralOrder**: Referral requests to specialists
- **ServiceOrder**: Generic service orders with frequency and location

#### Order Workflow
- **OrderFulfillment**: Order fulfillment tracking
- **OrderResult**: Order results and outcomes
- **OrderRevision**: Order revision history and relationships

### 5. Messaging & Communication Domain Model
**File:** `messaging-communication-domain-model.puml`

Comprehensive model for healthcare messaging, communication, and integration systems:

#### Core Messaging Entities
- **CommunicationMessage**: Core messaging entity supporting multiple delivery channels (email, SMS, push notifications)
- **MessageAttachment**: File attachments with content type and metadata
- **MessageRecipient**: Recipients with delivery status and read tracking
- **MessageDeliveryChannel**: Configurable delivery channels with protocol-specific settings
- **MessageSender**: Pluggable sender implementations for different communication protocols

#### Alert and Notification System
- **SystemAlert**: Internal system notifications with read/acknowledgment tracking
- **AlertRecipient**: Alert recipients with user assignment and read status
- **NotificationRule**: Event-driven notification rules that trigger automated communications
- **NotificationEvent**: Events that trigger notification rules with context data

#### Message Templates and Content Generation
- **MessageTemplate**: Reusable message templates with variable substitution for consistent communication
- **TemplateVariable**: Template variables with types, validation, and default values
- **ContentEngine**: Template processing engines (Velocity, etc.) with extensible configuration
- **GeneratedContent**: Generated message content with context data and processing results

#### Healthcare Message Integration
- **HealthcareMessage**: Standardized healthcare messaging for clinical data exchange between systems (supports HL7, FHIR, and other standards)
- **MessageSource**: External message sources with configuration and metadata
- **InboundMessageQueue**: Queue for processing incoming healthcare messages with error handling
- **ProcessedMessageArchive**: Archive of successfully processed messages with state tracking
- **MessageProcessingError**: Error handling for failed message processing with retry logic
- **MessageHandler**: Pluggable message handlers for different healthcare message types
- **MessageRouter**: Message routing engine with configurable rules and fallback behavior
- **MessageProcessor**: Parallel message processing with configurable concurrency and error handling

#### Integration and External Systems
- **ExternalSystem**: External healthcare systems that exchange clinical data via standard protocols (HL7, FHIR, REST APIs, etc.)
- **DataExchange**: Data exchange protocols and configurations for system integration
- **MessageTransformation**: Data transformation rules for converting between message formats and standards

#### Scheduled Communication
- **CommunicationTask**: Scheduled communication tasks for periodic alerts, reminders, and system notifications
- **TaskConfiguration**: Flexible task configuration with key-value properties
- **ScheduledAlert**: Scheduled alerts with timing and targeting rules
- **AlertReminder**: Reminder system for follow-ups and care protocols
- **CommunicationSchedule**: Scheduling engine with cron expressions and timezone support

#### Audit and Monitoring
- **CommunicationAudit**: Audit trails for all communication activities
- **DeliveryMetrics**: Performance metrics for delivery channels and message processing
- **SystemHealthMonitor**: Health monitoring for communication subsystems

### 6. Visit Management Domain Model
**File:** `visit-management-domain-model.puml`

Comprehensive model for visit lifecycle management, scheduling, and workflow:

#### Core Visit Entities
- **VisitEpisode**: Extended visit entity with purpose, priority, duration, and status
- **VisitClassification**: Categorization by care level, complexity, and resource requirements
- **VisitIndication**: Clinical and administrative reasons for the visit

#### Visit Scheduling
- **VisitAppointment**: Scheduled appointments with status and priority management
- **AppointmentSlot**: Available time periods with capacity constraints
- **VisitReservation**: Hold and reservation management for visit slots
- **VisitSchedulingRule**: Rules governing appointment scheduling and conflicts

#### Visit Workflow
- **VisitWorkflowState**: Defined states in visit processing from arrival to departure
- **VisitWorkflowTransition**: State transitions with conditions and actions
- **VisitQueue**: Patient queuing systems for efficient workflow management
- **QueueEntry**: Individual queue positions with priority and wait time tracking

#### Visit Processes
- **VisitAdmission**: Formal admission process for inpatient care
- **VisitDischarge**: Discharge planning with summaries and follow-up
- **VisitTransfer**: Patient transfers between locations and services

#### Visit Documentation
- **VisitNote**: Clinical and administrative documentation
- **VisitCommunication**: Patient and provider communications
- **VisitAlert**: System alerts and notifications

#### Resource Management
- **VisitResource**: Healthcare resources (equipment, rooms, staff)
- **ResourceAllocation**: Resource assignment and utilization tracking
- **VisitCapacity**: Capacity management for services and locations

#### Quality and Performance
- **VisitQualityMetric**: Quality indicators and performance measurements
- **VisitSatisfactionSurvey**: Patient feedback and satisfaction tracking
- **VisitPerformanceIndicator**: Key performance indicators for visit management

#### Configuration
- **VisitAttributeDefinition**: Flexible visit attribute definitions
- **VisitBusinessRule**: Configurable business rules for visit management
- **VisitTemplate**: Standardized visit patterns and workflows

## Design Principles

### Conceptual Focus
- Models represent conceptual domain entities, not database tables or classes
- Removed database-specific attributes (IDs, timestamps, audit fields)
- Collapsed metadata and configuration entities into their parents where appropriate
- Eliminated abbreviations in entity names for clarity

### Domain Separation
- Clear separation of concerns across domain boundaries
- Shared entities are referenced from base models to avoid duplication
- Order management is separated into its own comprehensive domain model
- Each model maintains consistent color coding for related entity types

### Relationship Modeling
- Focus on essential business relationships between conceptual entities
- Removed purely technical relationships (foreign keys, database constraints)
- Maintained semantic relationships that represent real-world interactions
- Grouped related entities using consistent visual styling

## Color Legend

The models use consistent color coding to distinguish different types of domain entities:

- **Light Green**: Patient and patient-specific entities
- **Orange**: Healthcare provider entities
- **Light Gray**: Location and facility entities  
- **Light Yellow**: Clinical care episode entities
- **Light Cyan**: Clinical vocabulary and concept entities
- **Light Pink**: Clinical order and request entities
- **Light Blue**: Medication and pharmaceutical entities
- **Wheat**: Visit Management entities
- **Light Salmon**: Order workflow and management entities
- **White Smoke**: System and reference data entities

## Usage

These domain models serve multiple purposes:

1. **Architecture Documentation**: High-level view of OpenMRS domain structure
2. **Development Guide**: Understanding entity relationships for feature development
3. **Training Material**: Educational resource for new developers and implementers
4. **Design Reference**: Foundation for API design and data modeling decisions

## References

- [Apache Modeling Project](https://www.fmc-modeling.org/category/projects/apache/amp/Apache_Modeling_Project.html) - Reference for domain modeling best practices
- OpenMRS Data Model documentation
- OpenMRS API documentation

## Design Principles

### 1. Conceptual Focus
- Entities represent business concepts, not database tables or classes
- Removed technical implementation details (IDs, audit fields, etc.)
- Used full entity names without abbreviations (e.g., "Observation" not "Obs")

### 2. Modular Architecture
- Shared entities defined once in the base model
- Specialized models reference and extend base entities
- Clear separation of concerns between patient management and clinical data management

### 3. Domain-Driven Design
- Entities grouped by business capability
- Relationships reflect real-world clinical workflows
- Consistent with Apache Modeling Project guidelines

### 4. PlantUML Best Practices
- Used `entity` notation instead of `class` for conceptual modeling
- Consistent color coding across all domain models
- Clear relationship cardinalities and labels

### 5. Consistent Color Scheme
The models use a consistent color scheme to differentiate between domain areas:

#### Patient Domain Colors (Base Model):
- **Light Blue**: Person demographics and attributes
- **Light Green**: Patient-specific entities
- **Light Yellow**: Clinical care episodes (Visit, Encounter, Observation)
- **Light Salmon**: Programs and workflow management
- **Light Gray**: Locations and facilities
- **Orange**: Healthcare providers and roles

#### Clinical Domain Colors (Extension Models):
- **Light Cyan**: Clinical concepts and vocabulary
- **Light Pink**: Clinical orders and requests
- **Light Steel Blue**: Clinical forms and documentation
- **Misty Rose**: All Clinical Decision Support entities
- **White Smoke**: System and reference data

#### Messaging & Communication Colors (Specialized Model):
- **Light Pink**: All Messaging & Communication entities (unified color for entire domain)

#### Clinical Decision Support Colors (Specialized Model):
- **Misty Rose**: All Clinical Decision Support entities (unified color scheme)

This color scheme is maintained across all domain models to ensure visual consistency and easy identification of entity types.

## Usage

When creating new domain models:

1. **Identify shared entities** that exist in the Patient Management base model
2. **Reference shared entities** in your specialized model with `<<Shared>>` stereotype
3. **Add domain-specific entities** that extend or complement the base entities
4. **Document the relationship** between models in this README

## Model Relationships

```
Patient Management Domain Model (Base)
    ↑ extends/references
├── Clinical Data Management Domain Model (Specialized)
├── Clinical Decision Support Domain Model (Specialized)
├── Messaging & Communication Domain Model (Specialized)
├── Order Management Domain Model (Specialized)
└── Visit Management Domain Model (Specialized)
    ↑ could extend/reference
Future Domain Models (e.g., Financial Management, Inventory Management)
```

This architecture ensures consistency across domain models while avoiding duplication and maintaining clear boundaries between different business capabilities.
