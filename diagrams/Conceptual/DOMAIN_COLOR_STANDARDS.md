# OpenMRS Conceptual Domain Model - Color Standards

This document defines the standardized color scheme for all conceptual domain models in OpenMRS to ensure consistency and improve readability across diagrams.

## Standard Color Palette

### Core Domain Categories

| Color | Hex Code | Domain Category | Description |
|-------|----------|----------------|-------------|
| LightBlue | #ADD8E6 | Person | Person demographic and identity entities |
| LightGreen | #90EE90 | Patient | Patient-specific entities and identifiers |
| LightYellow | #FFFFE0 | Clinical | Clinical care episodes (Visit, Encounter, Observation) |
| LightCyan | #E0FFFF | Concept | Clinical vocabulary and concept entities |
| Orange | #FFA500 | Provider | Healthcare provider entities |
| LightGray | #D3D3D3 | Location | Location and facility entities |
| LightSalmon | #FFA07A | Program | Program and workflow entities |
| LightPink | #FFB6C1 | Order | Clinical orders and requests |
| LightSkyBlue | #87CEEB | Form | Clinical forms and documentation |
| Plum | #DDA0DD | Workflow | Domain-specific workflow entities |
| Wheat | #F5DEB3 | Scheduling | Scheduling and appointment entities |
| LightSteelBlue | #B0C4DE | Configuration | Configuration and metadata entities |

### Border Colors

| Entity Category | Border Color |
|----------------|-------------|
| Person | DarkBlue |
| Patient | DarkGreen |
| Clinical | DarkOrange |
| Concept | DarkCyan |
| Provider | DarkOrange |
| Location | DarkGray |
| Program | DarkRed |
| Order | DeepPink |
| Form | DeepSkyBlue |
| Workflow | DarkMagenta |
| Scheduling | DarkGoldenRod |
| Configuration | DarkSlateBlue |

## Entity Category Mapping

### Shared Entities (appear across multiple models)

| Entity | Category | Color | Notes |
|--------|----------|-------|-------|
| Person | Person | LightBlue | Base demographic entity |
| PersonName | Person | LightBlue | Person name components |
| PersonAddress | Person | LightBlue | Person address information |
| Patient | Patient | LightGreen | Core patient entity |
| PatientIdentifier | Patient | LightGreen | Patient identification |
| Visit | Clinical | LightYellow | Care episode |
| Encounter | Clinical | LightYellow | Clinical interaction |
| Observation | Clinical | LightYellow | Clinical measurements |
| Provider | Provider | Orange | Healthcare professionals |
| ProviderRole | Provider | Orange | Provider roles and permissions |
| Location | Location | LightGray | Physical locations |
| Program | Program | LightSalmon | Care programs |
| ProgramWorkflow | Program | LightSalmon | Program workflows |
| PatientProgram | Program | LightSalmon | Patient program enrollment |
| Concept | Concept | LightCyan | Clinical vocabulary |
| ConceptName | Concept | LightCyan | Concept localization |
| CodedOrFreeText | Concept | LightCyan | Flexible concept representation |
| Condition | Concept | LightCyan | Patient conditions (moved from Clinical) |
| Diagnosis | Concept | LightCyan | Clinical diagnoses (moved from Clinical) |

### Domain-Specific Entity Categories

#### Condition Management
- Condition, Diagnosis, ConditionClinicalStatus, ConditionVerificationStatus, DiagnosisAttribute, DiagnosisAttributeType
- Category: Concept
- Color: LightCyan

#### Order Management  
- ClinicalOrder, MedicationOrder, TestOrder, OrderType, CareSetting
- Category: Order
- Color: LightPink

#### Cohort Management
- PatientGroup, PatientGroupMembership, CohortStatistics, etc.
- Category: Workflow
- Color: Plum

#### Visit Management
- VisitType, VisitSchedule, AppointmentType, etc.
- Category: Scheduling
- Color: Wheat

## Implementation Guidelines

1. **Consistency**: All models must use the same color for the same conceptual entity
2. **Shared Entities**: Mark shared entities with "(from Domain Management)" notation
3. **Color Legend**: Include standardized color legend in all models
4. **Entity Notes**: Use consistent entity descriptions across models
5. **Categorization**: Follow the category mapping when adding new entities

## Migration Plan

1. Update Patient Management Domain Model (baseline)
2. Update Clinical Data Management Domain Model
3. Update Cohort Management Domain Model  
4. Update remaining domain models
5. Validate consistency across all models
