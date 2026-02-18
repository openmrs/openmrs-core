```mermaid
erDiagram
  ConceptMapType {
    = 1L 
    Integer conceptMapTypeId PK
    = conceptMapTypeId 
    = conceptMapTypeId 
    = isHidden 
  }
  FieldAnswer {
    = 5656L 
    Date dateCreated 
    boolean dirty 
    = false 
    = true 
    = concept 
    = true 
    = creator 
    = true 
    = dateCreated 
    = true 
    = field 
  }
  ConceptReferenceRange {
    = 47329L 
    Integer conceptReferenceRangeId PK
    String criteria 
    = conceptReferenceRangeId 
    = criteria 
    = conceptNumeric 
  }
  Diagnosis {
    = 1L 
    Integer diagnosisId PK
    CodedOrFreeText diagnosis 
    ConditionVerificationStatus certainty 
    Integer rank 
    String formNamespaceAndPath 
    no-arg Constructor 
    = encounter 
    = diagnosis 
    = certainty 
    = rank 
    = patient 
    = encounter 
    = diagnosis 
    = certainty 
    = rank 
    = patient 
    = formNamespaceAndPath 
    = diagnosisId 
    = encounter 
    = diagnosis 
    = certainty 
    = rank 
    = condition 
    = patient 
    = formNamespaceAndPath 
  }
  Allergy {
    = 1 
    Integer allergyId PK
    Allergen allergen 
    String comments 
    = patient 
    = allergen 
    = severity 
    = comments 
    = reactions 
    = allergyId 
    = allergyId 
    = patient 
    = allergen 
    = severity 
    = comments 
    = reactions 
    = encounter 
  }
  ConceptClass {
    = 33473L 
    Integer conceptClassId 
    = conceptClassId 
    = conceptClassId 
  }
  ConceptReferenceTermMap {
    = 1L 
    Integer conceptReferenceTermMapId 
    = conceptReferenceTermMapId 
    = termB 
    = conceptReferenceTermMapId 
    = termA 
    = termB 
    (ConceptReferenceTermMap) obj 
    == obj 
    = 3 
  }
  FormResource {
    = 1L 
    Integer formResourceId 
    String name 
    String valueReference 
    String datatypeClassname 
    String datatypeConfig 
    String preferredHandlerClassname 
    String handlerConfig 
    = false 
    Object typedValue 
    Date dateChanged 
    = form 
    = formResourceId 
    = name 
    = datatypeClassname 
    = datatypeConfig 
    = preferredHandlerClassname 
    = handlerConfig 
    = typedValue 
    = true 
    = valueToPersist 
    = changedBy 
    = dateChanged 
  }
  ConceptReferenceTerm {
    = 1L 
    Integer conceptReferenceTermId 
    String code 
    String version 
    = conceptReferenceTermId 
    = source 
    = code 
    = conceptReferenceTermId 
    = conceptSource 
    = code 
    = version 
    = conceptReferenceTermMaps 
  }
  PatientProgram {
    = 0L 
    Integer patientProgramId PK
    Date dateEnrolled 
    Date dateCompleted 
    = null 
    = null 
    = null 
    = state 
    = concept 
    = dateCompleted 
    = dateEnrolled 
    = patient 
    = patientProgramId 
    = program 
    = states 
    = location 
    = attributes 
  }
  PatientState {
    = 0L 
    Integer patientStateId 
    Date startDate 
    Date endDate 
    = patientProgram 
    = patientStatusId 
    = state 
    = endDate 
    = startDate 
    = encounter 
  }
  ObsReferenceRange {
    = 473299L 
    Integer obsReferenceRangeId PK
    = obsReferenceRangeId 
    = obs 
  }
  ConceptStateConversion {
    = 3214511L 
    Integer conceptStateConversionId 
    = concept 
    = conceptStateConversionId 
    = programWorkflow 
    = programWorkflowState 
  }
  PatientIdentifierType {
    = 211231L 
    Integer patientIdentifierTypeId PK
    String format 
    String formatDescription 
    String validator 
    LocationBehavior locationBehavior 
    UniquenessBehavior uniquenessBehavior 
    = patientIdentifierTypeId 
    = formatDescription 
    = required 
    = locationBehavior 
    = uniquenessBehavior 
    = format 
    = patientIdentifierTypeId 
    = validator 
  }
  User {
    Integer userId 
    String systemId 
    String username 
    String email 
    String> userProperties 
    = null 
    Date dateCreated 
    Date dateChanged 
    boolean retired 
    Date dateRetired 
    String retireReason 
    = userId 
    = person 
    = tmprole 
    = roles 
    = systemId 
    = userId 
    = person 
    = username 
    = email 
    : systemId 
    = userProperties 
    = proficientLocalesProperty 
    = creator 
    = dateCreated 
    = changedBy 
    = dateChanged 
    = retired 
    = retiredBy 
    = dateRetired 
    = retireReason 
  }
  ConceptSource {
    = 375L 
    Integer conceptSourceId 
    String hl7Code 
    String uniqueId 
    = conceptSourceId 
    = conceptSourceId 
    = hl7Code 
    = uniqueId 
  }
  PersonAttributeType {
    = 2112313431211L 
    Integer personAttributeTypeId PK
    String format 
    Integer foreignKey 
    Double sortWeight 
    = false 
    = myPersonAttributeTypeId 
    = format 
    = foreignKey 
    = sortWeight 
    = newPersonAttributeTypeId 
    = searchable 
    = editPrivilege 
    = 1L 
  }
  ProviderRole {
    = 1L 
    Integer providerRoleId PK
    = id 
    = id 
  }
  DrugReferenceMap {
    = 1L 
    Integer drugReferenceMapId 
    Date dateCreated 
    Date dateChanged 
    = term 
    = conceptMapType 
    = drugReferenceMapId 
    = drug 
    = conceptReferenceTerm 
    = conceptMapType 
    = creator 
    = dateCreated 
    = changedBy 
    = dateChanged 
  }
  ProgramAttributeType {
    Integer programAttributeTypeId 
    = programAttributeTypeId 
  }
  ConceptAnswer {
    = 3744L 
    Integer conceptAnswerId 
    Date dateCreated 
    Double sortWeight 
    = conceptAnswerId 
    = answerConcept 
    = answerConcept 
    = d 
    = answerConcept 
    = answerDrug 
    = concept 
    = conceptAnswerId 
    = creator 
    = dateCreated 
    = sortWeight 
    : 0 
  }
  Encounter {
    = 2L 
    Integer encounterId PK
    Date encounterDatetime 
    = encounterId 
    = encounterDatetime 
    = encounterId 
    = encounterType 
    = location 
    = obs 
    = orders 
    = patient 
    = diagnoses 
    = conditions 
    = encounterProviders 
    = form 
    = visit 
    = false 
    = true 
    = allergies 
  }
  PersonAddress {
    = 343333L 
    Integer personAddressId PK
    = false 
    String address1 
    String address2 
    String address3 
    String address4 
    String address5 
    String address6 
    String address7 
    String address8 
    String address9 
    String address10 
    String address11 
    String address12 
    String address13 
    String address14 
    String address15 
    String cityVillage 
    String countyDistrict 
    String stateProvince 
    String country 
    String postalCode 
    String latitude 
    String longitude 
    Date startDate 
    Date endDate 
    = personAddressId 
    = address1 
    = address2 
    = cityVillage 
    = country 
    : preferred 
    = preferred 
    = latitude 
    = longitude 
    = person 
    = personAddressId 
    = postalCode 
    = stateProvince 
    = countyDistrict 
    = 0 
    = 1 
    = address3 
    = address4 
    = address6 
    = address5 
    = startDate 
    = endDate 
    == null 
    = address7 
    = address8 
    = address9 
    = address10 
    = address11 
    = address12 
    = address13 
    = address14 
    = address15 
  }
  Condition {
    = 1L 
    Integer conditionId PK
    CodedOrFreeText condition 
    ConditionClinicalStatus clinicalStatus 
    ConditionVerificationStatus verificationStatus 
    String additionalDetail 
    Date onsetDate 
    Date endDate 
    String endReason 
    = condition 
    = clinicalStatus 
    = verificationStatus 
    = previousVersion 
    = additionalDetail 
    : null 
    : null 
    = patient 
    = conditionId 
    = condition 
    = clinicalStatus 
    = verificationStatus 
    = previousVersion 
    = additionalDetail 
    : null 
    : null 
    : null 
    : null 
    = endReason 
    = patient 
    = encounter 
  }
  Location {
    = 455634L 
    = 1 
    Integer locationId PK
    String address1 
    String address2 
    String cityVillage 
    String stateProvince 
    String country 
    String postalCode 
    String latitude 
    String longitude 
    String countyDistrict 
    String address3 
    String address4 
    String address6 
    String address5 
    String address7 
    String address8 
    String address9 
    String address10 
    String address11 
    String address12 
    String address13 
    String address14 
    String address15 
    Set<LocationTag> tags 
    = locationId 
    = address1 
    = address2 
    = cityVillage 
    = country 
    = latitude 
    = locationId 
    = longitude 
    = postalCode 
    = stateProvince 
    = countyDistrict 
    = type 
    = parentLocationId 
    = childLocations 
    = this 
    = tags 
    = address3 
    = address4 
    = address6 
    = address5 
    = address7 
    = address8 
    = address9 
    = address10 
    = address11 
    = address12 
    = address13 
    = address14 
    = address15 
  }
  OrderType {
    = 23232L 
    Integer orderTypeId 
    String javaClassName 
    Set<ConceptClass> conceptClasses 
    = orderTypeId 
    = orderTypeId 
    = javaClassName 
    = parent 
  }
  VisitType {
    = 1L 
    Integer visitTypeId PK
    = visitTypeId 
    = visitTypeId 
  }
  Visit {
    Integer visitId PK
    Date startDatetime 
    Date stopDatetime 
    = visitId 
    = patient 
    = visitType 
    = startDatetime 
    = visitId 
    = patient 
    = visitType 
    = indication 
    = location 
    = startDatetime 
    = stopDatetime 
    = id 
    + visitId 
    = encounters 
  }
  OrderGroup {
    = 72232L 
    Integer orderGroupId PK
    = orderGroupId 
    = patient 
    = encounter 
    = orders 
    + 1 
    = size 
    / 2 
    / 2 
    = orderSet 
    = parentOrderGroup 
    = orderGroupReason 
    = previousOrderGroup 
    = nestedOrderGroups 
  }
  MedicationDispense {
    = 1L 
    Integer medicationDispenseId PK
    dispensing event 
    Double quantity 
    Double dose 
    Boolean asNeeded 
    String dosingInstructions 
    Date datePrepared 
    Date dateHandedOver 
    Boolean wasSubstituted 
    = medicationDispenseId 
    = patient 
    = encounter 
    = concept 
    = drug 
    = location 
    = dispenser 
    = drugOrder 
    = status 
    = statusReason 
    = type 
    = quantity 
    = quantityUnits 
    = dose 
    = doseUnits 
    = route 
    = frequency 
    = asNeeded 
    = dosingInstructions 
    = datePrepared 
    = dateHandedOver 
    = wasSubstituted 
    = substitutionType 
    = substitutionReason 
  }
  DrugIngredient {
    = 94023L 
    Double strength 
    = drug 
    = ingredient 
    = strength 
    = units 
  }
  ProviderAttributeType {
    Integer providerAttributeTypeId PK
    = providerAttributeTypeId 
  }
  VisitAttributeType {
    Integer visitAttributeTypeId PK
    = visitAttributeTypeId 
  }
  EncounterRole {
    Integer encounterRoleId 
    = encounterRoleId 
    = encounterRoleId 
  }
  Relationship {
    = 323423L 
    Integer relationshipId 
    Date startDate 
    Date endDate 
    = relationshipId 
    = personA 
    = personB 
    = type 
    = personA 
    = type 
    = relationshipId 
    = personB 
    = startDate 
    = endDate 
    + personB 
  }
  LocationAttributeType {
    Integer locationAttributeTypeId 
    = locationAttributeTypeId 
  }
  PersonMergeLog {
    = 1L 
    Integer personMergeLogId PK
    String serializedMergedData 
    PersonMergeLogData personMergeLogData 
    = personMergeLogId 
    = winner 
    = loser 
    = serializedMergedData 
    = personMergeLogData 
  }
  AlertRecipient {
    = false 
    Date dateChanged 
    Integer recipientId 
    = a 
    = a 
    = read 
    = alert 
    = dateChanged 
    + recipient 
    = alertRead 
    = user 
    = recipientId 
  }
  Template {
    Integer id PK
    String name 
    String template 
    Integer ordinal 
    String sender 
    String recipients 
    String subject 
    Map data 
    String content 
    = id 
    = name 
    = template 
    = id 
    = name 
    = template 
    = recipients 
    = sender 
    = subject 
    = ordinal 
    = data 
    = content 
  }
  TaskDefinition {
    Integer id 
    String taskClass 
    = null 
    Date startTime 
    Date lastExecutionTime 
    Long repeatInterval 
    Boolean startOnStartup 
    String startTimePattern 
    Boolean started 
    String> properties 
    = id 
    = taskClass 
    = id 
    = properties 
    = taskClass 
    = startTime 
    = lastExecutionTime 
    = repeatInterval 
    = pattern 
    = startOnStartup 
    = started 
    / 1000 
    = taskInstance 
  }
  ClobDatatypeStorage {
    Integer id PK
    = id 
    = value 
  }
  SerializedObject {
    Integer id 
    String type 
    String subtype 
    OpenmrsSerializer> serializationClass 
    String serializedData 
    = id 
    = type 
    = subtype 
    = serializationClass 
    = serializedData 
  }
  OpenmrsRevisionEntity {
    Integer changedBy 
    Date changedOn 
    = userId 
    = changedOn 
  }
  HL7InArchive {
    int hl7InArchiveId 
    Integer messageState 
    = false 
    = hl7InArchiveId 
    = messageState 
    = loaded 
  }
  HL7InError {
    = 16777215 
    Integer hl7InErrorId 
    String error 
    String errorDetails 
    = hl7InErrorId 
    = error 
    = errorDetails 
  }
  FieldAnswer }o--|| User : ""
  Diagnosis }o--|| Encounter : ""
  Diagnosis }o--|| Condition : ""
  Allergy }o--|| Encounter : ""
  ConceptReferenceTermMap }o--|| ConceptReferenceTerm : ""
  ConceptReferenceTermMap }o--|| ConceptReferenceTerm : ""
  FormResource }o--|| User : ""
  ConceptReferenceTerm }o--|| ConceptSource : ""
  PatientProgram }o--|| Location : ""
  PatientState }o--|| PatientProgram : ""
  PatientState }o--|| Encounter : ""
  User }o--|| User : ""
  User }o--|| User : ""
  User }o--|| User : ""
  DrugReferenceMap }o--|| ConceptReferenceTerm : ""
  DrugReferenceMap }o--|| ConceptMapType : ""
  DrugReferenceMap }o--|| User : ""
  DrugReferenceMap }o--|| User : ""
  ConceptAnswer }o--|| User : ""
  Encounter }o--|| Location : ""
  Encounter ||--o{ Diagnosis : ""
  Encounter ||--o{ Condition : ""
  Encounter }o--|| Visit : ""
  Encounter ||--o{ Allergy : ""
  Condition }o--|| Condition : ""
  Condition }o--|| Encounter : ""
  Location }o--|| Location : ""
  Location ||--o{ Location : ""
  OrderType }o--|| OrderType : ""
  Visit }o--|| VisitType : ""
  Visit }o--|| Location : ""
  Visit ||--o{ Encounter : ""
  OrderGroup }o--|| Encounter : ""
  OrderGroup }o--|| OrderGroup : ""
  OrderGroup }o--|| OrderGroup : ""
  OrderGroup ||--o{ OrderGroup : ""
  MedicationDispense }o--|| Encounter : ""
  MedicationDispense }o--|| Location : ""
  AlertRecipient }o--|| User : ""
```