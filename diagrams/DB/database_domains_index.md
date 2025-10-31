# OpenMRS Database Domain Models

This document provides an overview of the OpenMRS database schema organized by functional domains.

## Domain Overview

### Patient Management
**File:** `openmrs_patient_management_domain.puml`
**Tables:** 11

**Purpose:** Core patient demographics, identifiers, personal information, and allergies

**Key Tables:**
- `patient` - Core patient records
- `person` - Person demographics and basic info
- `person_name` - Person name components
- `person_address` - Address information
- `person_attribute` - Data table
- ... and 6 more tables

---

### Clinical Encounters & Observations
**File:** `openmrs_clinical_encounters_domain.puml`
**Tables:** 9

**Purpose:** Clinical visits, observations, diagnoses, and encounter-related data

**Key Tables:**
- `encounter` - Clinical encounters/visits
- `encounter_type` - Data table
- `encounter_role` - Data table
- `encounter_provider` - Data table
- `encounter_diagnosis` - Data table
- ... and 4 more tables

---

### Orders & Medications
**File:** `openmrs_orders_medications_domain.puml`
**Tables:** 15

**Purpose:** Medical orders, prescriptions, medication dispensing, and drug information

**Key Tables:**
- `orders` - Medical orders and prescriptions
- `order_type` - Data table
- `order_group` - Data table
- `order_group_attribute` - Data table
- `order_group_attribute_type` - Data table
- ... and 10 more tables

---

### Clinical Concepts & Terminology
**File:** `openmrs_clinical_concepts_domain.puml`
**Tables:** 20

**Purpose:** Medical terminology, concept dictionaries, and reference mappings

**Key Tables:**
- `concept` - Medical concepts and terminology
- `concept_name` - Data table
- `concept_description` - Data table
- `concept_class` - Data table
- `concept_datatype` - Data table
- ... and 15 more tables

---

### Programs & Workflows
**File:** `openmrs_programs_workflows_domain.puml`
**Tables:** 7

**Purpose:** Care programs, treatment workflows, and patient program enrollment

**Key Tables:**
- `program` - Care programs
- `program_workflow` - Data table
- `program_workflow_state` - Data table
- `patient_program` - Data table
- `patient_state` - Data table
- ... and 2 more tables

---

### Visits & Locations
**File:** `openmrs_visits_locations_domain.puml`
**Tables:** 9

**Purpose:** Patient visits, healthcare facility locations, and location management

**Key Tables:**
- `visit` - Patient visits
- `visit_type` - Data table
- `visit_attribute` - Data table
- `visit_attribute_type` - Data table
- `location` - Healthcare facilities and locations
- ... and 4 more tables

---

### Users, Roles & Security
**File:** `openmrs_users_security_domain.puml`
**Tables:** 11

**Purpose:** System users, roles, privileges, healthcare providers, and security

**Key Tables:**
- `users` - System users and authentication
- `role` - Data table
- `privilege` - Data table
- `user_role` - Data table
- `role_role` - Data table
- ... and 6 more tables

---

### Forms & Fields
**File:** `openmrs_forms_fields_domain.puml`
**Tables:** 6

**Purpose:** Data collection forms, form fields, and form resources

**Key Tables:**
- `form` - Data collection forms
- `form_field` - Data table
- `form_resource` - Data table
- `field` - Data table
- `field_type` - Data table
- ... and 1 more tables

---

### Conditions & Relationships
**File:** `openmrs_conditions_relationships_domain.puml`
**Tables:** 3

**Purpose:** Patient conditions, diagnoses, and person-to-person relationships

**Key Tables:**
- `conditions` - Data table
- `relationship` - Data table
- `relationship_type` - Data table

---

## Using the Diagrams

1. **PlantUML Files**: Use with PlantUML to generate visual diagrams
2. **Online Viewer**: Copy content to plantuml.com/plantuml for instant visualization
3. **VS Code**: Use PlantUML extension for integrated viewing

## Legend

- **PK**: Primary Key
- **FK**: Foreign Key
- **UK**: Unique Key
- **AI**: Auto-increment
- `*` prefix: NOT NULL constraint

## Relationships

- `}o--||`: Many-to-One relationship
- `||--o{`: One-to-Many relationship
- `||--||`: One-to-One relationship