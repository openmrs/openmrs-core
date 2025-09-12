# OpenMRS Conceptual Domain Models - Entity Cross-Reference

This document provides a comprehensive mapping of shared entities across all OpenMRS conceptual domain models, ensuring consistency and avoiding duplication.

## Shared Entity Definitions

All shared entities are **primarily defined** in `patient-management-domain-model.puml` and **referenced** in other domain models.

### Core Shared Entities

| Entity | Primary Definition | Color Category | Referenced In |
|--------|-------------------|----------------|---------------|
| Person | Patient Management | Person (LightBlue) | Patient Mgmt, Cohort Mgmt |
| PersonName | Patient Management | Person (LightBlue) | Patient Mgmt |
| PersonAddress | Patient Management | Person (LightBlue) | Patient Mgmt |
| Patient | Patient Management | Patient (LightGreen) | All domain models |
| PatientIdentifier | Patient Management | Patient (LightGreen) | Patient Mgmt, Clinical Data |
| Visit | Patient Management | Clinical (LightYellow) | Patient Mgmt, Clinical Data, Visit Mgmt |
| Encounter | Patient Management | Clinical (LightYellow) | All domain models |
| Observation | Patient Management | Clinical (LightYellow) | All domain models |
| Provider | Patient Management | Provider (Orange) | All domain models |
| ProviderRole | Patient Management | Provider (Orange) | Patient Mgmt, Clinical Data |
| Location | Patient Management | Location (LightGray) | All domain models |
| Program | Patient Management | Program (LightSalmon) | Patient Mgmt, Clinical Data |
| ProgramWorkflow | Patient Management | Program (LightSalmon) | Patient Mgmt, Clinical Data |
| ProgramWorkflowState | Patient Management | Program (LightSalmon) | Patient Mgmt, Clinical Data |
| PatientProgram | Patient Management | Program (LightSalmon) | Patient Mgmt, Clinical Data |
| ProgramOutcome | Patient Management | Program (LightSalmon) | Patient Mgmt, Clinical Data |
| PatientState | Patient Management | Program (LightSalmon) | Patient Mgmt, Clinical Data |

### Concept Management Entities

| Entity | Primary Definition | Color Category | Referenced In |
|--------|-------------------|----------------|---------------|
| Concept | Patient Management | Concept (LightCyan) | All domain models |
| ConceptName | Patient Management | Concept (LightCyan) | Patient Mgmt, Clinical Data |
| CodedOrFreeText | Patient Management | Concept (LightCyan) | Patient Mgmt, Clinical Data |
| Condition | Patient Management | Concept (LightCyan) | Patient Mgmt, Clinical Data |
| Diagnosis | Patient Management | Concept (LightCyan) | Patient Mgmt, Clinical Data |
| ConditionClinicalStatus | Patient Management | Concept (LightCyan) | Patient Mgmt, Clinical Data |
| ConditionVerificationStatus | Patient Management | Concept (LightCyan) | Patient Mgmt, Clinical Data |
| DiagnosisAttribute | Patient Management | Concept (LightCyan) | Patient Mgmt, Clinical Data |
| DiagnosisAttributeType | Patient Management | Concept (LightCyan) | Patient Mgmt, Clinical Data |

### Order Management Entities

| Entity | Primary Definition | Color Category | Referenced In |
|--------|-------------------|----------------|---------------|
| ClinicalOrder | Order Management | Order (LightPink) | Order Mgmt, Clinical Data, Medication Dispensing |
| MedicationOrder | Order Management | Order (LightPink) | Order Mgmt, Clinical Data, Medication Dispensing |
| TestOrder | Order Management | Order (LightPink) | Order Mgmt, Clinical Data |
| OrderType | Order Management | Order (LightPink) | Order Mgmt, Clinical Data |
| CareSetting | Order Management | Order (LightPink) | Order Mgmt, Clinical Data |

## Domain-Specific Entities

### Clinical Data Management Specific

| Entity | Color Category | Description |
|--------|----------------|-------------|
| EncounterType | Clinical (LightYellow) | Types of clinical encounters |
| ObservationGroup | Clinical (LightYellow) | Grouped observations |
| ReferenceRange | Clinical (LightYellow) | Normal/critical value ranges |
| ConceptClass | Concept (LightCyan) | Concept classifications |
| ConceptDatatype | Concept (LightCyan) | Concept data types |
| ConceptAnswer | Concept (LightCyan) | Concept answer options |
| ConceptSet | Concept (LightCyan) | Concept collections |
| ConceptMap | Concept (LightCyan) | External concept mappings |
| ConceptReferenceTerm | Concept (LightCyan) | External reference terms |
| ConceptMapType | Concept (LightCyan) | Mapping relationship types |
| EncounterProvider | Provider (Orange) | Provider-encounter relationships |
| EncounterRole | Provider (Orange) | Provider roles in encounters |
| ClinicalForm | Form (LightSkyBlue) | Clinical documentation forms |
| FormField | Form (LightSkyBlue) | Form field definitions |
| Field | Form (LightSkyBlue) | Individual form fields |
| FieldType | Form (LightSkyBlue) | Form field types |
| FieldAnswer | Form (LightSkyBlue) | Form field answers |
| LocationTag | Location (LightGray) | Location classifications |
| Allergy | Allergy (Plum) | Patient allergies |
| AllergenType | Allergy (Plum) | Types of allergens |
| AllergySeverity | Allergy (Plum) | Allergy severity levels |
| AllergyReaction | Allergy (Plum) | Allergy reactions |

### Cohort Management Specific

| Entity | Color Category | Description |
|--------|----------------|-------------|
| PatientGroup | Cohort (Plum) | Patient cohorts/groups |
| PatientGroupMembership | Cohort (Plum) | Cohort membership |
| PatientGroupDefinition | Cohort (Plum) | Cohort definitions |
| SelectionCriteria | Cohort (Plum) | Patient selection criteria |
| InclusionCriteria | Cohort (Plum) | Inclusion rules |
| ExclusionCriteria | Cohort (Plum) | Exclusion rules |
| EligibilityRule | Cohort (Plum) | Eligibility rules |
| ClinicalCriteria | Cohort (Plum) | Clinical selection criteria |
| DemographicCriteria | Cohort (Plum) | Demographic criteria |
| TemporalCriteria | Cohort (Plum) | Time-based criteria |
| PatientGroupUnion | Cohort (Plum) | Cohort union operations |
| PatientGroupIntersection | Cohort (Plum) | Cohort intersection operations |
| PatientGroupDifference | Cohort (Plum) | Cohort difference operations |
| PatientGroupComplement | Cohort (Plum) | Cohort complement operations |
| PatientGroupAnalysis | Cohort (Plum) | Cohort analysis |
| CohortStatistics | Cohort (Plum) | Cohort statistics |
| CohortDemographics | Cohort (Plum) | Cohort demographics |
| CohortOutcome | Cohort (Plum) | Cohort outcomes |
| CohortBenchmark | Cohort (Plum) | Cohort benchmarks |
| PatientGroupComparison | Cohort (Plum) | Cohort comparisons |
| CohortReport | Cohort (Plum) | Cohort reports |
| CohortEnrollmentProcess | Cohort (Plum) | Enrollment workflows |
| PatientRecruitment | Cohort (Plum) | Patient recruitment |
| CohortMonitoring | Cohort (Plum) | Cohort monitoring |
| MembershipTransition | Cohort (Plum) | Membership transitions |
| CohortMaintenance | Cohort (Plum) | Cohort maintenance |
| CohortValidation | Cohort (Plum) | Cohort validation |
| DataIntegrityCheck | Cohort (Plum) | Data integrity checks |
| MembershipAudit | Cohort (Plum) | Membership audits |

### Medication Dispensing Specific

| Entity | Color Category | Description |
|--------|----------------|-------------|
| MedicationProduct | Pharmaceutical (LightSalmon) | Pharmaceutical products |
| MedicationBatch | Pharmaceutical (LightSalmon) | Medication batches |
| MedicationLot | Pharmaceutical (LightSalmon) | Medication lots |
| ActiveIngredient | Pharmaceutical (LightSalmon) | Active ingredients |
| DosageForm | Pharmaceutical (LightSalmon) | Dosage forms |
| RouteOfAdministration | Pharmaceutical (LightSalmon) | Administration routes |
| MedicationStrength | Pharmaceutical (LightSalmon) | Medication strengths |
| DispensingRequest | Dispensing (LightSteelBlue) | Dispensing requests |
| DispensingEvent | Dispensing (LightSteelBlue) | Dispensing events |
| MedicationDispense | Dispensing (LightSteelBlue) | Medication dispensing |
| PharmaceuticalDispenser | Dispensing (LightSteelBlue) | Pharmaceutical dispensers |
| DispensingValidation | Dispensing (LightSteelBlue) | Dispensing validation |
| DispensingWorkflow | Dispensing (LightSteelBlue) | Dispensing workflows |
| PharmacyQueue | Dispensing (LightSteelBlue) | Pharmacy queues |
| DosingInstruction | Dosing (Lavender) | Dosing instructions |
| AdministrationSchedule | Dosing (Lavender) | Administration schedules |
| MedicationAdministration | Dosing (Lavender) | Medication administration |
| DoseCalculation | Dosing (Lavender) | Dose calculations |
| AdministrationRoute | Dosing (Lavender) | Administration routes |
| MedicationSubstitution | Substitution (MistyRose) | Medication substitutions |
| TherapeuticEquivalence | Substitution (MistyRose) | Therapeutic equivalence |
| GenericSubstitution | Substitution (MistyRose) | Generic substitutions |
| FormularySubstitution | Substitution (MistyRose) | Formulary substitutions |
| SubstitutionReason | Substitution (MistyRose) | Substitution reasons |
| MedicationInventory | Supply (LemonChiffon) | Medication inventory |
| InventoryItem | Supply (LemonChiffon) | Inventory items |
| StockLevel | Supply (LemonChiffon) | Stock levels |
| InventoryTransaction | Supply (LemonChiffon) | Inventory transactions |
| MedicationSupplier | Supply (LemonChiffon) | Medication suppliers |
| ProcurementOrder | Supply (LemonChiffon) | Procurement orders |
| SupplyChainEvent | Supply (LemonChiffon) | Supply chain events |
| MedicationSafety | Quality (LightCoral) | Medication safety |
| AdverseEvent | Quality (LightCoral) | Adverse events |
| MedicationError | Quality (LightCoral) | Medication errors |
| QualityAssurance | Quality (LightCoral) | Quality assurance |
| SafetyAlert | Quality (LightCoral) | Safety alerts |
| MedicationRecall | Quality (LightCoral) | Medication recalls |

## Color Standardization Rules

### Standard Colors by Category

1. **Person** (LightBlue): Person demographic and identity entities
2. **Patient** (LightGreen): Patient-specific entities and identifiers
3. **Clinical** (LightYellow): Clinical care episodes (Visit, Encounter, Observation)
4. **Concept** (LightCyan): Clinical vocabulary and concept entities (includes Condition/Diagnosis)
5. **Provider** (Orange): Healthcare provider entities
6. **Location** (LightGray): Location and facility entities
7. **Program** (LightSalmon): Program and workflow entities
8. **Order** (LightPink): Clinical orders and requests
9. **Form** (LightSkyBlue): Clinical forms and documentation
10. **Allergy** (Plum): Allergy and adverse reaction entities

### Domain-Specific Colors

- **Cohort Management**: Plum for all cohort-specific entities
- **Medication Dispensing**: Multiple colors by functional area
  - Pharmaceutical (LightSalmon)
  - Dispensing (LightSteelBlue)
  - Dosing (Lavender)
  - Substitution (MistyRose)
  - Supply (LemonChiffon)
  - Quality (LightCoral)

## Reference Guidelines

1. **Entity Naming**: Use exact same entity names across all models
2. **Shared Entity Notation**: Mark with "(from Domain Management)" in referencing models
3. **Color Consistency**: Use standardized colors for same conceptual entities
4. **Entity Descriptions**: Keep consistent descriptions and attributes
5. **Relationship Patterns**: Follow consistent relationship naming patterns

## Cross-Reference Validation

Each domain model should:
1. Reference shared entities from patient-management-domain-model.puml
2. Use standardized color scheme
3. Include proper domain attribution in entity descriptions
4. Follow consistent relationship patterns
5. Include standardized color legend with domain attribution

This ensures consistency, reduces duplication, and maintains clear conceptual boundaries across all OpenMRS domain models.
