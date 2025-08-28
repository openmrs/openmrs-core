# OpenMRS Service Cohesion and Coupling Analysis Report
============================================================

## Summary Statistics
Total Services Analyzed: 60
Service Interfaces: 34
Service Implementations: 26

## Average Metrics
Average Cyclomatic Complexity: 47.95
Average Coupling (CBO): 12.43
Average LCOM: 0.00
Average Halstead Volume: 30844.50
Average Lines of Code: 491.92

## Services with Highest Complexity/Coupling
### Top 5 by Cyclomatic Complexity:
- PatientServiceImpl: 240
- : 190
- : 175
- HL7ServiceImpl: 174
- ConceptServiceImpl: 167

### Top 5 by Coupling (CBO):
- PatientServiceImpl: 49
- OrderServiceImpl: 45
- ConceptServiceImpl: 42
- EncounterServiceImpl: 32
- AdministrationServiceImpl: 31

### Top 5 by Halstead Volume:
- : 155831.81
- ConceptServiceImpl: 127806.90
- PatientServiceImpl: 114310.62
- : 81713.71
- HL7ServiceImpl: 79294.09

## Detailed Service Metrics

### Implementations
#### HL7ServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/hl7/impl/HL7ServiceImpl.java
- Lines of Code: 1187
- Effective Lines: 781
- Methods: 54
- Fields: 4
- Cyclomatic Complexity: 174
- Coupling (CBO): 27
- LCOM: 0
- Fan-out: 61
- Halstead Volume: 79294.09
- Halstead Difficulty: 102.95
- Estimated Bugs: 26.43

#### PatientServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/PatientServiceImpl.java
- Lines of Code: 1640
- Effective Lines: 1085
- Methods: 82
- Fields: 3
- Cyclomatic Complexity: 240
- Coupling (CBO): 49
- LCOM: 0
- Fan-out: 87
- Halstead Volume: 114310.62
- Halstead Difficulty: 117.51
- Estimated Bugs: 38.10

#### FormServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/FormServiceImpl.java
- Lines of Code: 796
- Effective Lines: 480
- Methods: 55
- Fields: 3
- Cyclomatic Complexity: 71
- Coupling (CBO): 24
- LCOM: 0
- Fan-out: 28
- Halstead Volume: 46496.33
- Halstead Difficulty: 96.19
- Estimated Bugs: 15.50

#### ProviderServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/ProviderServiceImpl.java
- Lines of Code: 335
- Effective Lines: 179
- Methods: 32
- Fields: 1
- Cyclomatic Complexity: 4
- Coupling (CBO): 13
- LCOM: 0
- Fan-out: 4
- Halstead Volume: 14285.20
- Halstead Difficulty: 60.35
- Estimated Bugs: 4.76

#### OrderServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/OrderServiceImpl.java
- Lines of Code: 1291
- Effective Lines: 858
- Methods: 104
- Fields: 4
- Cyclomatic Complexity: 146
- Coupling (CBO): 45
- LCOM: 0
- Fan-out: 42
- Halstead Volume: 74792.50
- Halstead Difficulty: 129.42
- Estimated Bugs: 24.93

#### LocationServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/LocationServiceImpl.java
- Lines of Code: 512
- Effective Lines: 297
- Methods: 42
- Fields: 1
- Cyclomatic Complexity: 32
- Coupling (CBO): 13
- LCOM: 0
- Fan-out: 12
- Halstead Volume: 24367.53
- Halstead Difficulty: 99.53
- Estimated Bugs: 8.12

#### ObsServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/ObsServiceImpl.java
- Lines of Code: 677
- Effective Lines: 414
- Methods: 42
- Fields: 2
- Cyclomatic Complexity: 66
- Coupling (CBO): 23
- LCOM: 0
- Fan-out: 32
- Halstead Volume: 42605.48
- Halstead Difficulty: 101.38
- Estimated Bugs: 14.20

#### MedicationDispenseServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/MedicationDispenseServiceImpl.java
- Lines of Code: 87
- Effective Lines: 57
- Methods: 9
- Fields: 1
- Cyclomatic Complexity: 3
- Coupling (CBO): 7
- LCOM: 0
- Fan-out: 2
- Halstead Volume: 3479.64
- Halstead Difficulty: 27.27
- Estimated Bugs: 1.16

#### PersonServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/PersonServiceImpl.java
- Lines of Code: 1002
- Effective Lines: 633
- Methods: 71
- Fields: 2
- Cyclomatic Complexity: 91
- Coupling (CBO): 21
- LCOM: 0
- Fan-out: 40
- Halstead Volume: 53940.69
- Halstead Difficulty: 109.93
- Estimated Bugs: 17.98

#### AdministrationServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/AdministrationServiceImpl.java
- Lines of Code: 986
- Effective Lines: 662
- Methods: 47
- Fields: 13
- Cyclomatic Complexity: 123
- Coupling (CBO): 31
- LCOM: 0
- Fan-out: 55
- Halstead Volume: 63085.36
- Halstead Difficulty: 95.44
- Estimated Bugs: 21.03

#### CohortServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/CohortServiceImpl.java
- Lines of Code: 283
- Effective Lines: 179
- Methods: 22
- Fields: 2
- Cyclomatic Complexity: 14
- Coupling (CBO): 12
- LCOM: 0
- Fan-out: 12
- Halstead Volume: 13741.66
- Halstead Difficulty: 72.02
- Estimated Bugs: 4.58

#### DiagnosisServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/DiagnosisServiceImpl.java
- Lines of Code: 260
- Effective Lines: 118
- Methods: 21
- Fields: 1
- Cyclomatic Complexity: 5
- Coupling (CBO): 13
- LCOM: 0
- Fan-out: 5
- Halstead Volume: 12041.78
- Halstead Difficulty: 45.83
- Estimated Bugs: 4.01

#### ConditionServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/ConditionServiceImpl.java
- Lines of Code: 183
- Effective Lines: 91
- Methods: 11
- Fields: 1
- Cyclomatic Complexity: 14
- Coupling (CBO): 9
- LCOM: 0
- Fan-out: 7
- Halstead Volume: 8714.43
- Halstead Difficulty: 43.24
- Estimated Bugs: 2.90

#### UserServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/UserServiceImpl.java
- Lines of Code: 822
- Effective Lines: 523
- Methods: 57
- Fields: 5
- Cyclomatic Complexity: 79
- Coupling (CBO): 25
- LCOM: 0
- Fan-out: 37
- Halstead Volume: 50099.83
- Halstead Difficulty: 100.69
- Estimated Bugs: 16.70

#### EncounterServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/EncounterServiceImpl.java
- Lines of Code: 1004
- Effective Lines: 606
- Methods: 57
- Fields: 1
- Cyclomatic Complexity: 116
- Coupling (CBO): 32
- LCOM: 0
- Fan-out: 33
- Halstead Volume: 62157.85
- Halstead Difficulty: 100.61
- Estimated Bugs: 20.72

#### OrderSetServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/OrderSetServiceImpl.java
- Lines of Code: 215
- Effective Lines: 128
- Methods: 18
- Fields: 1
- Cyclomatic Complexity: 14
- Coupling (CBO): 10
- LCOM: 0
- Fan-out: 7
- Halstead Volume: 9107.42
- Halstead Difficulty: 53.78
- Estimated Bugs: 3.04

#### VisitServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/VisitServiceImpl.java
- Lines of Code: 421
- Effective Lines: 252
- Methods: 36
- Fields: 1
- Cyclomatic Complexity: 19
- Coupling (CBO): 17
- LCOM: 0
- Fan-out: 15
- Halstead Volume: 20800.95
- Halstead Difficulty: 75.07
- Estimated Bugs: 6.93

#### ConceptServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/ConceptServiceImpl.java
- Lines of Code: 2156
- Effective Lines: 1340
- Methods: 169
- Fields: 7
- Cyclomatic Complexity: 167
- Coupling (CBO): 42
- LCOM: 0
- Fan-out: 54
- Halstead Volume: 127806.90
- Halstead Difficulty: 141.80
- Estimated Bugs: 42.60

#### SerializationServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/SerializationServiceImpl.java
- Lines of Code: 155
- Effective Lines: 96
- Methods: 7
- Fields: 2
- Cyclomatic Complexity: 27
- Coupling (CBO): 8
- LCOM: 0
- Fan-out: 9
- Halstead Volume: 8022.22
- Halstead Difficulty: 49.64
- Estimated Bugs: 2.67

#### ProgramWorkflowServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/ProgramWorkflowServiceImpl.java
- Lines of Code: 639
- Effective Lines: 401
- Methods: 50
- Fields: 2
- Cyclomatic Complexity: 66
- Coupling (CBO): 18
- LCOM: 0
- Fan-out: 17
- Halstead Volume: 37052.82
- Halstead Difficulty: 93.78
- Estimated Bugs: 12.35

#### DatatypeServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/DatatypeServiceImpl.java
- Lines of Code: 237
- Effective Lines: 159
- Methods: 13
- Fields: 4
- Cyclomatic Complexity: 57
- Coupling (CBO): 8
- LCOM: 0
- Fan-out: 12
- Halstead Volume: 11357.95
- Halstead Difficulty: 65.74
- Estimated Bugs: 3.79

#### TimerSchedulerServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/scheduler/timer/TimerSchedulerServiceImpl.java
- Lines of Code: 553
- Effective Lines: 314
- Methods: 25
- Fields: 5
- Cyclomatic Complexity: 67
- Coupling (CBO): 11
- LCOM: 0
- Fan-out: 19
- Halstead Volume: 29841.00
- Halstead Difficulty: 69.67
- Estimated Bugs: 9.95

#### MessageSourceServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/messagesource/impl/MessageSourceServiceImpl.java
- Lines of Code: 211
- Effective Lines: 101
- Methods: 17
- Fields: 3
- Cyclomatic Complexity: 7
- Coupling (CBO): 4
- LCOM: 0
- Fan-out: 7
- Halstead Volume: 9588.81
- Halstead Difficulty: 49.25
- Estimated Bugs: 3.20

#### DefaultMessageSourceServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/messagesource/impl/DefaultMessageSourceServiceImpl.java
- Lines of Code: 152
- Effective Lines: 96
- Methods: 20
- Fields: 2
- Cyclomatic Complexity: 3
- Coupling (CBO): 5
- LCOM: 0
- Fan-out: 3
- Halstead Volume: 4861.77
- Halstead Difficulty: 30.68
- Estimated Bugs: 1.62

#### MessageServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/notification/impl/MessageServiceImpl.java
- Lines of Code: 305
- Effective Lines: 161
- Methods: 22
- Fields: 4
- Cyclomatic Complexity: 10
- Coupling (CBO): 11
- LCOM: 0
- Fan-out: 11
- Halstead Volume: 15250.84
- Halstead Difficulty: 63.54
- Estimated Bugs: 5.08

#### AlertServiceImpl
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/notification/impl/AlertServiceImpl.java
- Lines of Code: 208
- Effective Lines: 121
- Methods: 11
- Fields: 3
- Cyclomatic Complexity: 24
- Coupling (CBO): 12
- LCOM: 0
- Fan-out: 11
- Halstead Volume: 11287.85
- Halstead Difficulty: 46.64
- Estimated Bugs: 3.76

### Interfaces
#### Slf4JLogService
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/liquibase/ext/logging/slf4j/Slf4JLogService.java
- Lines of Code: 37
- Effective Lines: 16
- Methods: 2
- Fields: 1
- Cyclomatic Complexity: 3
- Coupling (CBO): 1
- LCOM: 0
- Fan-out: 1
- Halstead Volume: 1665.13
- Halstead Difficulty: 18.37
- Estimated Bugs: 0.56

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/notification/AlertService.java
- Lines of Code: 140
- Effective Lines: 29
- Methods: 0
- Fields: 8
- Cyclomatic Complexity: 14
- Coupling (CBO): 6
- LCOM: 0
- Fan-out: 2
- Halstead Volume: 6969.87
- Halstead Difficulty: 29.28
- Estimated Bugs: 2.32

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/notification/MessageService.java
- Lines of Code: 91
- Effective Lines: 29
- Methods: 0
- Fields: 15
- Cyclomatic Complexity: 4
- Coupling (CBO): 2
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 4103.81
- Halstead Difficulty: 24.57
- Estimated Bugs: 1.37

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/logic/LogicService.java
- Lines of Code: 367
- Effective Lines: 42
- Methods: 2
- Fields: 13
- Cyclomatic Complexity: 44
- Coupling (CBO): 5
- LCOM: 0
- Fan-out: 6
- Halstead Volume: 24482.01
- Halstead Difficulty: 65.31
- Estimated Bugs: 8.16

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/messagesource/MessageSourceService.java
- Lines of Code: 53
- Effective Lines: 9
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 2
- Coupling (CBO): 0
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 2217.11
- Halstead Difficulty: 17.40
- Estimated Bugs: 0.74

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/scheduler/SchedulerService.java
- Lines of Code: 171
- Effective Lines: 44
- Methods: 4
- Fields: 4
- Cyclomatic Complexity: 5
- Coupling (CBO): 4
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 7093.39
- Halstead Difficulty: 28.06
- Estimated Bugs: 2.36

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/VisitService.java
- Lines of Code: 397
- Effective Lines: 84
- Methods: 3
- Fields: 11
- Cyclomatic Complexity: 28
- Coupling (CBO): 11
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 24344.43
- Halstead Difficulty: 60.96
- Estimated Bugs: 8.11

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/DiagnosisService.java
- Lines of Code: 246
- Effective Lines: 50
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 15
- Coupling (CBO): 8
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 12755.67
- Halstead Difficulty: 43.91
- Estimated Bugs: 4.25

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/FormService.java
- Lines of Code: 645
- Effective Lines: 113
- Methods: 2
- Fields: 46
- Cyclomatic Complexity: 41
- Coupling (CBO): 10
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 42499.96
- Halstead Difficulty: 83.38
- Estimated Bugs: 14.17

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/SerializationService.java
- Lines of Code: 93
- Effective Lines: 16
- Methods: 1
- Fields: 1
- Cyclomatic Complexity: 11
- Coupling (CBO): 4
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 5426.90
- Halstead Difficulty: 28.20
- Estimated Bugs: 1.81

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/AdministrationService.java
- Lines of Code: 424
- Effective Lines: 72
- Methods: 0
- Fields: 2
- Cyclomatic Complexity: 56
- Coupling (CBO): 10
- LCOM: 0
- Fan-out: 1
- Halstead Volume: 30814.44
- Halstead Difficulty: 63.74
- Estimated Bugs: 10.27

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/UserService.java
- Lines of Code: 609
- Effective Lines: 114
- Methods: 25
- Fields: 37
- Cyclomatic Complexity: 59
- Coupling (CBO): 9
- LCOM: 0
- Fan-out: 1
- Halstead Volume: 40607.69
- Halstead Difficulty: 76.14
- Estimated Bugs: 13.54

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/PatientService.java
- Lines of Code: 858
- Effective Lines: 128
- Methods: 40
- Fields: 35
- Cyclomatic Complexity: 78
- Coupling (CBO): 15
- LCOM: 0
- Fan-out: 1
- Halstead Volume: 72818.85
- Halstead Difficulty: 112.06
- Estimated Bugs: 24.27

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/ProviderService.java
- Lines of Code: 364
- Effective Lines: 69
- Methods: 1
- Fields: 1
- Cyclomatic Complexity: 38
- Coupling (CBO): 8
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 21019.93
- Halstead Difficulty: 71.24
- Estimated Bugs: 7.01

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/CohortService.java
- Lines of Code: 294
- Effective Lines: 56
- Methods: 11
- Fields: 13
- Cyclomatic Complexity: 20
- Coupling (CBO): 7
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 17862.68
- Halstead Difficulty: 45.21
- Estimated Bugs: 5.95

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/OrderSetService.java
- Lines of Code: 216
- Effective Lines: 44
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 8
- Coupling (CBO): 7
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 9987.42
- Halstead Difficulty: 39.45
- Estimated Bugs: 3.33

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/ObsService.java
- Lines of Code: 541
- Effective Lines: 82
- Methods: 3
- Fields: 17
- Cyclomatic Complexity: 44
- Coupling (CBO): 13
- LCOM: 0
- Fan-out: 5
- Halstead Volume: 50088.46
- Halstead Difficulty: 92.65
- Estimated Bugs: 16.70

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/PersonService.java
- Lines of Code: 845
- Effective Lines: 151
- Methods: 50
- Fields: 51
- Cyclomatic Complexity: 55
- Coupling (CBO): 14
- LCOM: 0
- Fan-out: 1
- Halstead Volume: 59243.27
- Halstead Difficulty: 94.21
- Estimated Bugs: 19.75

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/OpenmrsService.java
- Lines of Code: 36
- Effective Lines: 7
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 1
- Coupling (CBO): 2
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 1736.01
- Halstead Difficulty: 12.90
- Estimated Bugs: 0.58

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/OrderService.java
- Lines of Code: 1101
- Effective Lines: 187
- Methods: 8
- Fields: 19
- Cyclomatic Complexity: 175
- Coupling (CBO): 20
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 81713.71
- Halstead Difficulty: 144.53
- Estimated Bugs: 27.24

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/LocationService.java
- Lines of Code: 503
- Effective Lines: 94
- Methods: 28
- Fields: 27
- Cyclomatic Complexity: 48
- Coupling (CBO): 9
- LCOM: 0
- Fan-out: 1
- Halstead Volume: 32802.98
- Halstead Difficulty: 88.61
- Estimated Bugs: 10.93

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/DatatypeService.java
- Lines of Code: 95
- Effective Lines: 19
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 17
- Coupling (CBO): 4
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 4096.61
- Halstead Difficulty: 24.58
- Estimated Bugs: 1.37

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/EncounterService.java
- Lines of Code: 730
- Effective Lines: 123
- Methods: 33
- Fields: 29
- Cyclomatic Complexity: 88
- Coupling (CBO): 16
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 52895.79
- Halstead Difficulty: 96.23
- Estimated Bugs: 17.63

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/MedicationDispenseService.java
- Lines of Code: 82
- Effective Lines: 22
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 6
- Coupling (CBO): 4
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 3973.08
- Halstead Difficulty: 21.42
- Estimated Bugs: 1.32

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/ConditionService.java
- Lines of Code: 120
- Effective Lines: 26
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 10
- Coupling (CBO): 5
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 5551.46
- Halstead Difficulty: 28.26
- Estimated Bugs: 1.85

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/StorageService.java
- Lines of Code: 176
- Effective Lines: 25
- Methods: 0
- Fields: 0
- Cyclomatic Complexity: 19
- Coupling (CBO): 2
- LCOM: 0
- Fan-out: 1
- Halstead Volume: 9820.55
- Halstead Difficulty: 47.09
- Estimated Bugs: 3.27

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/ConceptService.java
- Lines of Code: 2052
- Effective Lines: 351
- Methods: 21
- Fields: 94
- Cyclomatic Complexity: 190
- Coupling (CBO): 27
- LCOM: 0
- Fan-out: 4
- Halstead Volume: 155831.81
- Halstead Difficulty: 169.53
- Estimated Bugs: 51.94

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/ProgramWorkflowService.java
- Lines of Code: 566
- Effective Lines: 110
- Methods: 22
- Fields: 20
- Cyclomatic Complexity: 47
- Coupling (CBO): 14
- LCOM: 0
- Fan-out: 2
- Halstead Volume: 41343.25
- Halstead Difficulty: 86.52
- Estimated Bugs: 13.78

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/hl7/HL7Service.java
- Lines of Code: 522
- Effective Lines: 99
- Methods: 10
- Fields: 41
- Cyclomatic Complexity: 34
- Coupling (CBO): 7
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 30819.17
- Halstead Difficulty: 67.27
- Estimated Bugs: 10.27

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/impl/BaseOpenmrsService.java
- Lines of Code: 40
- Effective Lines: 10
- Methods: 2
- Fields: 0
- Cyclomatic Complexity: 3
- Coupling (CBO): 1
- LCOM: 0
- Fan-out: 0
- Halstead Volume: 1655.01
- Halstead Difficulty: 14.17
- Estimated Bugs: 0.55

#### StreamDataService
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/stream/StreamDataService.java
- Lines of Code: 222
- Effective Lines: 146
- Methods: 11
- Fields: 7
- Cyclomatic Complexity: 22
- Coupling (CBO): 0
- LCOM: 0
- Fan-out: 17
- Halstead Volume: 11341.84
- Halstead Difficulty: 47.16
- Estimated Bugs: 3.78

#### S3StorageService
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/storage/S3StorageService.java
- Lines of Code: 246
- Effective Lines: 186
- Methods: 9
- Fields: 3
- Cyclomatic Complexity: 25
- Coupling (CBO): 3
- LCOM: 0
- Fan-out: 28
- Halstead Volume: 17002.83
- Halstead Difficulty: 62.81
- Estimated Bugs: 5.67

#### LocalStorageService
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/storage/LocalStorageService.java
- Lines of Code: 195
- Effective Lines: 144
- Methods: 7
- Fields: 3
- Cyclomatic Complexity: 17
- Coupling (CBO): 4
- LCOM: 0
- Fan-out: 21
- Halstead Volume: 11069.09
- Halstead Difficulty: 45.28
- Estimated Bugs: 3.69

#### 
- File: /Users/shashivelur/Projects/openmrs-core/api/src/main/java/org/openmrs/api/storage/BaseStorageService.java
- Lines of Code: 121
- Effective Lines: 89
- Methods: 10
- Fields: 3
- Cyclomatic Complexity: 11
- Coupling (CBO): 4
- LCOM: 0
- Fan-out: 14
- Halstead Volume: 6624.52
- Halstead Difficulty: 43.10
- Estimated Bugs: 2.21
