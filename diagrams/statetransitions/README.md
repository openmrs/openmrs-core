# OpenMRS State Transition Diagrams

This directory contains PlantUML state transition diagrams for the key entities with non-trivial state machines in the OpenMRS core system.

## Individual State Machine Diagrams

### 1. Program Workflow States (`program-workflow-states.puml`)
**Entity**: `ProgramWorkflow`, `ProgramWorkflowState`, `PatientProgram`
- Manages patient progression through defined care pathways
- States marked as initial/terminal with transition validation
- Supports multiple concurrent workflows per patient

### 2. Order Fulfiller Status (`order-fulfiller-status.puml`)
**Entity**: `Order.FulfillerStatus` (enum)
- Tracks external fulfillment system processing
- States: RECEIVED → IN_PROGRESS → COMPLETED
- Supports error handling and cancellation flows

### 3. HL7 Message Processing (`hl7-message-processing.puml`)
**Entity**: `HL7InQueue.messageState`
- Manages HL7 message lifecycle from receipt to archival
- States: PENDING → PROCESSING → PROCESSED → MIGRATED
- Includes error handling and retry mechanisms

### 4. Condition Clinical Status (`condition-clinical-status.puml`)
**Entity**: `ConditionClinicalStatus` (enum)
- Tracks clinical progression of patient conditions
- Complex state transitions between ACTIVE, INACTIVE, REMISSION, etc.
- FHIR-aligned status definitions

### 5. Condition Verification Status (`condition-verification-status.puml`)
**Entity**: `ConditionVerificationStatus` (enum)
- Tracks diagnostic certainty progression
- Simple transition from PROVISIONAL to CONFIRMED
- Historically called "diagnosis certainty"

### 6. Patient State Integration (`patient-state-integration.puml`)
**Entity**: `PatientState`
- Manages audit trail of patient progression through states
- Tracks state history with start/end dates
- Integrates with forms and encounters

## Comprehensive Diagrams

### Detailed Program Workflow (`program-workflow-detailed.puml`)
Extended diagram showing:
- Patient program lifecycle
- Workflow state management
- Automatic state conversion via ConceptStateConversion
- State history tracking

### Legacy Combined View (`state-machines-openmrs.puml`)
Overview diagram showing all state machines in a single view.

## Key Features Across State Machines

### Transition Validation
- Legal transition checking (e.g., `ProgramWorkflow.isLegalTransition()`)
- Same-state transition blocking
- Initial state requirements

### Audit Trails
- Complete history preservation
- Date-stamped transitions
- User attribution where applicable

### Integration Points
- Concept-based automatic transitions
- Form/encounter integration
- Service-layer state management

### Error Handling
- Retry mechanisms (HL7 processing)
- Exception states (Order fulfillment)
- Validation failures (Program workflows)

## Usage Notes

These diagrams represent the conceptual state machines as implemented in the OpenMRS core codebase. They show:

1. **Allowed state transitions** and their conditions
2. **Business logic** governing state changes
3. **Integration points** between different systems
4. **Audit and history** mechanisms

For implementation details, refer to the corresponding Java classes and service implementations in the OpenMRS core API.
