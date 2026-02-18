# Enhanced OpenMRS Service Coupling and Dependency Analysis
======================================================================

## Executive Summary
- Total Services Analyzed: 60
- Average Fan-in: 1.37
- Average Fan-out: 1.37
- Average Instability: 0.39

## Service Dependency Rankings
### Top 10 Most Depended Upon Services (High Fan-in):
- AdministrationService: 14 dependencies
- MessageSourceService: 8 dependencies
- UserService: 6 dependencies
- PatientService: 6 dependencies
- ObsService: 6 dependencies
- ConceptService: 6 dependencies
- PersonService: 4 dependencies
- LocationService: 4 dependencies
- EncounterService: 4 dependencies
- VisitService: 3 dependencies

### Top 10 Most Dependent Services (High Fan-out):
- PatientServiceImpl: depends on 12 other services
- EncounterServiceImpl: depends on 9 other services
- HL7ServiceImpl: depends on 6 other services
- ObsServiceImpl: depends on 4 other services
- PersonServiceImpl: depends on 4 other services
- AdministrationServiceImpl: depends on 4 other services
- UserServiceImpl: depends on 4 other services
- ConceptServiceImpl: depends on 4 other services
- FormServiceImpl: depends on 3 other services
- OrderServiceImpl: depends on 3 other services

### Top 10 Most Unstable Services:
- HL7ServiceImpl: 1.000 instability
- PatientServiceImpl: 1.000 instability
- FormServiceImpl: 1.000 instability
- ProviderServiceImpl: 1.000 instability
- OrderServiceImpl: 1.000 instability
- LocationServiceImpl: 1.000 instability
- ObsServiceImpl: 1.000 instability
- PersonServiceImpl: 1.000 instability
- AdministrationServiceImpl: 1.000 instability
- CohortServiceImpl: 1.000 instability

## Coupling Matrix Analysis
Coupling Density: 0.023

## Tightly Coupled Service Clusters
### Cluster 1:
- LogicService
- PatientService

### Cluster 2:
- HL7ServiceImpl
- AdministrationService
- UserService
- PatientService
- PersonService
- LocationService
- HL7Service

### Cluster 3:
- PatientServiceImpl
- MessageSourceService
- VisitService
- AdministrationService
- UserService
- PatientService
- ObsService
- PersonService
- OrderService
- LocationService
- EncounterService
- ConceptService
- ProgramWorkflowService

### Cluster 4:
- FormServiceImpl
- FormService
- AdministrationService
- ObsService

### Cluster 5:
- ProviderServiceImpl
- AdministrationService
- ProviderService

### Cluster 6:
- OrderServiceImpl
- AdministrationService
- OrderService
- ConceptService

### Cluster 7:
- LocationServiceImpl
- AdministrationService
- LocationService

### Cluster 8:
- ObsServiceImpl
- PatientService
- ObsService
- EncounterService
- ConceptService

### Cluster 9:
- PersonServiceImpl
- MessageSourceService
- SerializationService
- AdministrationService
- PersonService

### Cluster 10:
- AdministrationServiceImpl
- MessageSourceService
- SerializationService
- AdministrationService
- ConceptService

### Cluster 11:
- CohortServiceImpl
- CohortService

### Cluster 12:
- DiagnosisServiceImpl
- DiagnosisService

### Cluster 13:
- UserServiceImpl
- MessageService
- MessageSourceService
- AdministrationService
- UserService

### Cluster 14:
- EncounterServiceImpl
- MessageSourceService
- VisitService
- DiagnosisService
- AdministrationService
- PatientService
- ObsService
- OrderService
- EncounterService
- ConditionService

### Cluster 15:
- OrderSetServiceImpl
- OrderSetService

### Cluster 16:
- VisitServiceImpl
- VisitService
- AdministrationService
- EncounterService

### Cluster 17:
- ConceptServiceImpl
- MessageSourceService
- AdministrationService
- ObsService
- ConceptService

### Cluster 18:
- SerializationServiceImpl
- AdministrationService

### Cluster 19:
- ProgramWorkflowServiceImpl
- ProgramWorkflowService

### Cluster 20:
- MessageSourceServiceImpl
- MessageSourceService

### Cluster 21:
- MessageServiceImpl
- MessageService
- UserService

### Cluster 22:
- AlertServiceImpl
- AlertService
- MessageSourceService
- UserService

## Detailed Service Metrics
### Slf4JLogService (implementation)
- Lines of Code: 30
- Methods: 2
- Fields: 1
- Cyclomatic Complexity: 3
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 10.81%

### AlertService (interface)
- Lines of Code: 126
- Methods: 0
- Fields: 8
- Cyclomatic Complexity: 14
- Service Dependencies: 1
  - Depends on: AlertService
- Fan-in: 2
- Fan-out: 1
- Instability: 0.333
- Comment Ratio: 17.14%

### MessageService (interface)
- Lines of Code: 57
- Methods: 0
- Fields: 15
- Cyclomatic Complexity: 4
- Service Dependencies: 0
- Fan-in: 2
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 17.58%

### LogicService (interface)
- Lines of Code: 333
- Methods: 2
- Fields: 13
- Cyclomatic Complexity: 44
- Service Dependencies: 2
  - Depends on: PatientService, LogicService
- Fan-in: 1
- Fan-out: 2
- Instability: 0.667
- Comment Ratio: 16.35%

### MessageSourceService (interface)
- Lines of Code: 44
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 2
- Service Dependencies: 0
- Fan-in: 8
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 26.42%

### SchedulerService (interface)
- Lines of Code: 148
- Methods: 4
- Fields: 4
- Cyclomatic Complexity: 5
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 23.39%

### VisitService (interface)
- Lines of Code: 362
- Methods: 3
- Fields: 11
- Cyclomatic Complexity: 28
- Service Dependencies: 0
- Fan-in: 3
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 16.62%

### DiagnosisService (interface)
- Lines of Code: 219
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 15
- Service Dependencies: 0
- Fan-in: 2
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 17.07%

### FormService (interface)
- Lines of Code: 590
- Methods: 2
- Fields: 46
- Cyclomatic Complexity: 41
- Service Dependencies: 0
- Fan-in: 1
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 16.43%

### SerializationService (interface)
- Lines of Code: 84
- Methods: 1
- Fields: 1
- Cyclomatic Complexity: 11
- Service Dependencies: 0
- Fan-in: 2
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 15.05%

### AdministrationService (interface)
- Lines of Code: 385
- Methods: 0
- Fields: 2
- Cyclomatic Complexity: 56
- Service Dependencies: 1
  - Depends on: AdministrationService
- Fan-in: 14
- Fan-out: 1
- Instability: 0.067
- Comment Ratio: 16.04%

### UserService (interface)
- Lines of Code: 557
- Methods: 25
- Fields: 37
- Cyclomatic Complexity: 59
- Service Dependencies: 1
  - Depends on: UserService
- Fan-in: 6
- Fan-out: 1
- Instability: 0.143
- Comment Ratio: 16.09%

### PatientService (interface)
- Lines of Code: 800
- Methods: 40
- Fields: 35
- Cyclomatic Complexity: 78
- Service Dependencies: 1
  - Depends on: PatientService
- Fan-in: 6
- Fan-out: 1
- Instability: 0.143
- Comment Ratio: 13.05%

### ProviderService (interface)
- Lines of Code: 328
- Methods: 1
- Fields: 1
- Cyclomatic Complexity: 38
- Service Dependencies: 0
- Fan-in: 1
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 18.13%

### CohortService (interface)
- Lines of Code: 268
- Methods: 11
- Fields: 13
- Cyclomatic Complexity: 20
- Service Dependencies: 0
- Fan-in: 1
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 16.33%

### OrderSetService (interface)
- Lines of Code: 193
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 8
- Service Dependencies: 0
- Fan-in: 1
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 17.59%

### ObsService (interface)
- Lines of Code: 510
- Methods: 3
- Fields: 17
- Cyclomatic Complexity: 44
- Service Dependencies: 1
  - Depends on: ObsService
- Fan-in: 6
- Fan-out: 1
- Instability: 0.143
- Comment Ratio: 10.72%

### PersonService (interface)
- Lines of Code: 770
- Methods: 50
- Fields: 51
- Cyclomatic Complexity: 55
- Service Dependencies: 1
  - Depends on: PersonService
- Fan-in: 4
- Fan-out: 1
- Instability: 0.200
- Comment Ratio: 16.57%

### OpenmrsService (interface)
- Lines of Code: 30
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 1
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 22.22%

### OrderService (interface)
- Lines of Code: 1017
- Methods: 8
- Fields: 19
- Cyclomatic Complexity: 175
- Service Dependencies: 0
- Fan-in: 3
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 14.71%

### LocationService (interface)
- Lines of Code: 459
- Methods: 28
- Fields: 27
- Cyclomatic Complexity: 48
- Service Dependencies: 1
  - Depends on: LocationService
- Fan-in: 4
- Fan-out: 1
- Instability: 0.200
- Comment Ratio: 16.70%

### DatatypeService (interface)
- Lines of Code: 81
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 17
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 23.16%

### EncounterService (interface)
- Lines of Code: 674
- Methods: 33
- Fields: 29
- Cyclomatic Complexity: 88
- Service Dependencies: 0
- Fan-in: 4
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 14.52%

### MedicationDispenseService (interface)
- Lines of Code: 71
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 6
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 21.95%

### ConditionService (interface)
- Lines of Code: 107
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 10
- Service Dependencies: 0
- Fan-in: 1
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 18.33%

### StorageService (interface)
- Lines of Code: 159
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 19
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 15.91%

### ConceptService (interface)
- Lines of Code: 1895
- Methods: 21
- Fields: 94
- Cyclomatic Complexity: 190
- Service Dependencies: 1
  - Depends on: ConceptService
- Fan-in: 6
- Fan-out: 1
- Instability: 0.143
- Comment Ratio: 15.01%

### ProgramWorkflowService (interface)
- Lines of Code: 505
- Methods: 22
- Fields: 20
- Cyclomatic Complexity: 47
- Service Dependencies: 1
  - Depends on: ProgramWorkflowService
- Fan-in: 3
- Fan-out: 1
- Instability: 0.250
- Comment Ratio: 15.37%

### HL7Service (interface)
- Lines of Code: 470
- Methods: 10
- Fields: 41
- Cyclomatic Complexity: 34
- Service Dependencies: 0
- Fan-in: 1
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 18.39%

### HL7ServiceImpl (implementation)
- Lines of Code: 980
- Methods: 54
- Fields: 4
- Cyclomatic Complexity: 174
- Service Dependencies: 6
  - Depends on: AdministrationService, PatientService, HL7Service, PersonService, LocationService, UserService
- Fan-in: 0
- Fan-out: 6
- Instability: 1.000
- Comment Ratio: 15.25%

### PatientServiceImpl (implementation)
- Lines of Code: 1353
- Methods: 82
- Fields: 3
- Cyclomatic Complexity: 240
- Service Dependencies: 12
  - Depends on: AdministrationService, PatientService, MessageSourceService, ProgramWorkflowService, ConceptService, PersonService, LocationService, OrderService, VisitService, EncounterService, UserService, ObsService
- Fan-in: 0
- Fan-out: 12
- Instability: 1.000
- Comment Ratio: 12.99%

### FormServiceImpl (implementation)
- Lines of Code: 678
- Methods: 55
- Fields: 3
- Cyclomatic Complexity: 71
- Service Dependencies: 3
  - Depends on: AdministrationService, FormService, ObsService
- Fan-in: 0
- Fan-out: 3
- Instability: 1.000
- Comment Ratio: 16.21%

### ProviderServiceImpl (implementation)
- Lines of Code: 295
- Methods: 32
- Fields: 1
- Cyclomatic Complexity: 4
- Service Dependencies: 2
  - Depends on: ProviderService, AdministrationService
- Fan-in: 0
- Fan-out: 2
- Instability: 1.000
- Comment Ratio: 20.30%

### BaseOpenmrsService (implementation)
- Lines of Code: 34
- Methods: 2
- Fields: 0
- Cyclomatic Complexity: 3
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 20.00%

### OrderServiceImpl (implementation)
- Lines of Code: 1140
- Methods: 104
- Fields: 4
- Cyclomatic Complexity: 146
- Service Dependencies: 3
  - Depends on: AdministrationService, ConceptService, OrderService
- Fan-in: 0
- Fan-out: 3
- Instability: 1.000
- Comment Ratio: 13.40%

### LocationServiceImpl (implementation)
- Lines of Code: 438
- Methods: 42
- Fields: 1
- Cyclomatic Complexity: 32
- Service Dependencies: 2
  - Depends on: AdministrationService, LocationService
- Fan-in: 0
- Fan-out: 2
- Instability: 1.000
- Comment Ratio: 17.58%

### ObsServiceImpl (implementation)
- Lines of Code: 563
- Methods: 42
- Fields: 2
- Cyclomatic Complexity: 66
- Service Dependencies: 4
  - Depends on: ConceptService, EncounterService, PatientService, ObsService
- Fan-in: 0
- Fan-out: 4
- Instability: 1.000
- Comment Ratio: 14.33%

### MedicationDispenseServiceImpl (interface)
- Lines of Code: 71
- Methods: 9
- Fields: 1
- Cyclomatic Complexity: 3
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 6.90%

### PersonServiceImpl (implementation)
- Lines of Code: 849
- Methods: 71
- Fields: 2
- Cyclomatic Complexity: 91
- Service Dependencies: 4
  - Depends on: AdministrationService, PersonService, SerializationService, MessageSourceService
- Fan-in: 0
- Fan-out: 4
- Instability: 1.000
- Comment Ratio: 14.57%

### AdministrationServiceImpl (interface)
- Lines of Code: 826
- Methods: 47
- Fields: 13
- Cyclomatic Complexity: 123
- Service Dependencies: 4
  - Depends on: AdministrationService, ConceptService, SerializationService, MessageSourceService
- Fan-in: 0
- Fan-out: 4
- Instability: 1.000
- Comment Ratio: 11.87%

### CohortServiceImpl (implementation)
- Lines of Code: 250
- Methods: 22
- Fields: 2
- Cyclomatic Complexity: 14
- Service Dependencies: 1
  - Depends on: CohortService
- Fan-in: 0
- Fan-out: 1
- Instability: 1.000
- Comment Ratio: 15.19%

### DiagnosisServiceImpl (interface)
- Lines of Code: 234
- Methods: 21
- Fields: 1
- Cyclomatic Complexity: 5
- Service Dependencies: 1
  - Depends on: DiagnosisService
- Fan-in: 0
- Fan-out: 1
- Instability: 1.000
- Comment Ratio: 16.92%

### ConditionServiceImpl (interface)
- Lines of Code: 157
- Methods: 11
- Fields: 1
- Cyclomatic Complexity: 14
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 14.21%

### UserServiceImpl (implementation)
- Lines of Code: 687
- Methods: 57
- Fields: 5
- Cyclomatic Complexity: 79
- Service Dependencies: 4
  - Depends on: AdministrationService, MessageService, UserService, MessageSourceService
- Fan-in: 0
- Fan-out: 4
- Instability: 1.000
- Comment Ratio: 13.87%

### EncounterServiceImpl (implementation)
- Lines of Code: 817
- Methods: 57
- Fields: 1
- Cyclomatic Complexity: 116
- Service Dependencies: 9
  - Depends on: AdministrationService, PatientService, MessageSourceService, DiagnosisService, OrderService, VisitService, EncounterService, ConditionService, ObsService
- Fan-in: 0
- Fan-out: 9
- Instability: 1.000
- Comment Ratio: 17.73%

### OrderSetServiceImpl (implementation)
- Lines of Code: 189
- Methods: 18
- Fields: 1
- Cyclomatic Complexity: 14
- Service Dependencies: 1
  - Depends on: OrderSetService
- Fan-in: 0
- Fan-out: 1
- Instability: 1.000
- Comment Ratio: 17.67%

### VisitServiceImpl (implementation)
- Lines of Code: 366
- Methods: 36
- Fields: 1
- Cyclomatic Complexity: 19
- Service Dependencies: 3
  - Depends on: VisitService, EncounterService, AdministrationService
- Fan-in: 0
- Fan-out: 3
- Instability: 1.000
- Comment Ratio: 16.63%

### ConceptServiceImpl (implementation)
- Lines of Code: 1874
- Methods: 169
- Fields: 7
- Cyclomatic Complexity: 167
- Service Dependencies: 4
  - Depends on: AdministrationService, ConceptService, ObsService, MessageSourceService
- Fan-in: 0
- Fan-out: 4
- Instability: 1.000
- Comment Ratio: 16.56%

### SerializationServiceImpl (implementation)
- Lines of Code: 129
- Methods: 7
- Fields: 2
- Cyclomatic Complexity: 27
- Service Dependencies: 1
  - Depends on: AdministrationService
- Fan-in: 0
- Fan-out: 1
- Instability: 1.000
- Comment Ratio: 14.84%

### ProgramWorkflowServiceImpl (implementation)
- Lines of Code: 539
- Methods: 50
- Fields: 2
- Cyclomatic Complexity: 66
- Service Dependencies: 1
  - Depends on: ProgramWorkflowService
- Fan-in: 0
- Fan-out: 1
- Instability: 1.000
- Comment Ratio: 16.90%

### DatatypeServiceImpl (interface)
- Lines of Code: 212
- Methods: 13
- Fields: 4
- Cyclomatic Complexity: 57
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 11.81%

### StreamDataService (implementation)
- Lines of Code: 194
- Methods: 11
- Fields: 7
- Cyclomatic Complexity: 22
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 7.21%

### S3StorageService (implementation)
- Lines of Code: 214
- Methods: 9
- Fields: 3
- Cyclomatic Complexity: 25
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 2.03%

### LocalStorageService (implementation)
- Lines of Code: 167
- Methods: 7
- Fields: 3
- Cyclomatic Complexity: 17
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 3.59%

### BaseStorageService (implementation)
- Lines of Code: 103
- Methods: 10
- Fields: 3
- Cyclomatic Complexity: 11
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 3.31%

### TimerSchedulerServiceImpl (implementation)
- Lines of Code: 444
- Methods: 25
- Fields: 5
- Cyclomatic Complexity: 67
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 17.54%

### MessageSourceServiceImpl (implementation)
- Lines of Code: 182
- Methods: 17
- Fields: 3
- Cyclomatic Complexity: 7
- Service Dependencies: 1
  - Depends on: MessageSourceService
- Fan-in: 0
- Fan-out: 1
- Instability: 1.000
- Comment Ratio: 18.01%

### DefaultMessageSourceServiceImpl (implementation)
- Lines of Code: 118
- Methods: 20
- Fields: 2
- Cyclomatic Complexity: 3
- Service Dependencies: 0
- Fan-in: 0
- Fan-out: 0
- Instability: 0.000
- Comment Ratio: 5.26%

### MessageServiceImpl (implementation)
- Lines of Code: 272
- Methods: 22
- Fields: 4
- Cyclomatic Complexity: 10
- Service Dependencies: 2
  - Depends on: UserService, MessageService
- Fan-in: 0
- Fan-out: 2
- Instability: 1.000
- Comment Ratio: 13.11%

### AlertServiceImpl (implementation)
- Lines of Code: 168
- Methods: 11
- Fields: 3
- Cyclomatic Complexity: 24
- Service Dependencies: 3
  - Depends on: UserService, AlertService, MessageSourceService
- Fan-in: 0
- Fan-out: 3
- Instability: 1.000
- Comment Ratio: 16.35%

## Recommendations
### Services with High Fan-out (Consider Refactoring):
- HL7ServiceImpl: 6 dependencies
- PatientServiceImpl: 12 dependencies
- FormServiceImpl: 3 dependencies
- OrderServiceImpl: 3 dependencies
- ObsServiceImpl: 4 dependencies
- PersonServiceImpl: 4 dependencies
- AdministrationServiceImpl: 4 dependencies
- UserServiceImpl: 4 dependencies
- EncounterServiceImpl: 9 dependencies
- VisitServiceImpl: 3 dependencies
- ConceptServiceImpl: 4 dependencies
- AlertServiceImpl: 3 dependencies

### Highly Unstable Services (Review Interface Design):
- HL7ServiceImpl: 1.000 instability
- PatientServiceImpl: 1.000 instability
- FormServiceImpl: 1.000 instability
- ProviderServiceImpl: 1.000 instability
- OrderServiceImpl: 1.000 instability
- LocationServiceImpl: 1.000 instability
- ObsServiceImpl: 1.000 instability
- PersonServiceImpl: 1.000 instability
- AdministrationServiceImpl: 1.000 instability
- CohortServiceImpl: 1.000 instability
- DiagnosisServiceImpl: 1.000 instability
- UserServiceImpl: 1.000 instability
- EncounterServiceImpl: 1.000 instability
- OrderSetServiceImpl: 1.000 instability
- VisitServiceImpl: 1.000 instability
- ConceptServiceImpl: 1.000 instability
- SerializationServiceImpl: 1.000 instability
- ProgramWorkflowServiceImpl: 1.000 instability
- MessageSourceServiceImpl: 1.000 instability
- MessageServiceImpl: 1.000 instability
- AlertServiceImpl: 1.000 instability

### General Recommendations:
1. Services with high fan-out may benefit from dependency injection refactoring
2. Highly unstable services should be reviewed for interface segregation
3. Tightly coupled clusters may indicate opportunities for service consolidation
4. Services with zero fan-in might be candidates for removal if unused