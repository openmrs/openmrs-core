# OpenMRS Database Schema Analysis

Total tables: 117

## Tables by Domain

### Core Patient Data (10 tables)

- **allergy** (11 columns, 0 PK, 2 FK)
- **allergy_reaction** (2 columns, 0 PK, 0 FK)
- **patient** (5 columns, 0 PK, 0 FK)
- **patient_identifier** (7 columns, 0 PK, 2 FK)
- **patient_identifier_type** (11 columns, 0 PK, 0 FK)
- **person** (12 columns, 0 PK, 0 FK)
- **person_address** (30 columns, 0 PK, 1 FK)
- **person_attribute** (5 columns, 0 PK, 0 FK)
- **person_attribute_type** (10 columns, 0 PK, 0 FK)
- **person_name** (13 columns, 0 PK, 0 FK)

### System (4 tables)

- **care_setting** (6 columns, 0 PK, 0 FK)
- **clob_datatype_storage** (0 columns, 0 PK, 0 FK)
- **global_property** (11 columns, 0 PK, 0 FK)
- **serialized_object** (6 columns, 0 PK, 0 FK)

### Cohorts (2 tables)

- **cohort** (6 columns, 0 PK, 0 FK)
- **cohort_member** (5 columns, 0 PK, 0 FK)

### Clinical Concepts (20 tables)

- **concept** (9 columns, 0 PK, 0 FK)
- **concept_answer** (3 columns, 0 PK, 0 FK)
- **concept_attribute** (5 columns, 0 PK, 0 FK)
- **concept_attribute_type** (11 columns, 0 PK, 0 FK)
- **concept_class** (6 columns, 0 PK, 0 FK)
- **concept_complex** (1 columns, 0 PK, 0 FK)
- **concept_datatype** (5 columns, 0 PK, 0 FK)
- **concept_description** (2 columns, 0 PK, 0 FK)
- **concept_map_type** (6 columns, 0 PK, 0 FK)
- **concept_name** (8 columns, 0 PK, 1 FK)
- **concept_numeric** (9 columns, 0 PK, 0 FK)
- **concept_proposal** (8 columns, 0 PK, 4 FK)
- **concept_reference_map** (2 columns, 0 PK, 0 FK)
- **concept_reference_range** (6 columns, 0 PK, 0 FK)
- **concept_reference_source** (5 columns, 0 PK, 0 FK)
- **concept_reference_term** (8 columns, 0 PK, 0 FK)
- **concept_reference_term_map** (2 columns, 0 PK, 0 FK)
- **concept_set** (1 columns, 0 PK, 0 FK)
- **concept_state_conversion** (3 columns, 0 PK, 3 FK)
- **concept_stop_word** (1 columns, 0 PK, 0 FK)

### Conditions (1 tables)

- **conditions** (15 columns, 0 PK, 1 FK)

### Orders & Medications (12 tables)

- **drug** (12 columns, 0 PK, 0 FK)
- **drug_ingredient** (2 columns, 0 PK, 0 FK)
- **drug_reference_map** (5 columns, 0 PK, 0 FK)
- **medication_dispense** (26 columns, 0 PK, 4 FK)
- **order_attribute** (5 columns, 0 PK, 0 FK)
- **order_attribute_type** (11 columns, 0 PK, 0 FK)
- **order_frequency** (6 columns, 0 PK, 0 FK)
- **order_group** (9 columns, 0 PK, 1 FK)
- **order_group_attribute** (5 columns, 0 PK, 0 FK)
- **order_group_attribute_type** (11 columns, 0 PK, 0 FK)
- **order_type** (7 columns, 0 PK, 0 FK)
- **orders** (18 columns, 0 PK, 2 FK)

### Clinical Encounters (7 tables)

- **encounter** (8 columns, 0 PK, 3 FK)
- **encounter_diagnosis** (10 columns, 0 PK, 1 FK)
- **encounter_provider** (5 columns, 0 PK, 0 FK)
- **encounter_role** (6 columns, 0 PK, 0 FK)
- **encounter_type** (8 columns, 0 PK, 0 FK)
- **obs** (21 columns, 0 PK, 6 FK)
- **obs_reference_range** (6 columns, 0 PK, 0 FK)

### Forms & Fields (6 tables)

- **field** (11 columns, 0 PK, 1 FK)
- **field_answer** (0 columns, 0 PK, 0 FK)
- **field_type** (2 columns, 0 PK, 0 FK)
- **form** (10 columns, 0 PK, 0 FK)
- **form_field** (9 columns, 0 PK, 0 FK)
- **form_resource** (6 columns, 0 PK, 0 FK)

### HL7 Integration (3 tables)

- **hl7_in_archive** (2 columns, 0 PK, 0 FK)
- **hl7_in_error** (2 columns, 0 PK, 0 FK)
- **hl7_in_queue** (4 columns, 0 PK, 0 FK)

### Locations (5 tables)

- **location** (30 columns, 0 PK, 1 FK)
- **location_attribute** (5 columns, 0 PK, 0 FK)
- **location_attribute_type** (11 columns, 0 PK, 0 FK)
- **location_tag** (6 columns, 0 PK, 0 FK)
- **location_tag_map** (0 columns, 0 PK, 0 FK)

### Notifications (3 tables)

- **notification_alert** (3 columns, 0 PK, 0 FK)
- **notification_alert_recipient** (0 columns, 0 PK, 0 FK)
- **notification_template** (6 columns, 0 PK, 0 FK)

### Programs & Workflows (6 tables)

- **patient_program** (9 columns, 0 PK, 2 FK)
- **patient_state** (9 columns, 0 PK, 1 FK)
- **program** (4 columns, 0 PK, 1 FK)
- **program_attribute_type** (11 columns, 0 PK, 0 FK)
- **program_workflow** (2 columns, 0 PK, 0 FK)
- **program_workflow_state** (2 columns, 0 PK, 0 FK)

### Users & Security (6 tables)

- **privilege** (1 columns, 0 PK, 0 FK)
- **role** (1 columns, 0 PK, 0 FK)
- **role_role** (0 columns, 0 PK, 0 FK)
- **user_property** (1 columns, 0 PK, 0 FK)
- **user_role** (0 columns, 0 PK, 0 FK)
- **users** (11 columns, 0 PK, 0 FK)

### Providers (4 tables)

- **provider** (11 columns, 0 PK, 4 FK)
- **provider_attribute** (5 columns, 0 PK, 0 FK)
- **provider_attribute_type** (11 columns, 0 PK, 0 FK)
- **provider_role** (6 columns, 0 PK, 0 FK)

### Scheduler (1 tables)

- **scheduler_task_config** (9 columns, 0 PK, 0 FK)

### Visits (4 tables)

- **visit** (8 columns, 0 PK, 2 FK)
- **visit_attribute** (5 columns, 0 PK, 0 FK)
- **visit_attribute_type** (11 columns, 0 PK, 0 FK)
- **visit_type** (6 columns, 0 PK, 0 FK)

### Other (23 tables)

- **concept_name_tag** (6 columns, 0 PK, 0 FK)
- **concept_name_tag_map** (0 columns, 0 PK, 0 FK)
- **concept_proposal_tag_map** (0 columns, 0 PK, 0 FK)
- **diagnosis_attribute** (5 columns, 0 PK, 0 FK)
- **diagnosis_attribute_type** (11 columns, 0 PK, 0 FK)
- **drug_order** (16 columns, 0 PK, 1 FK)
- **hl7_source** (1 columns, 0 PK, 0 FK)
- **note** (8 columns, 0 PK, 3 FK)
- **order_set** (7 columns, 0 PK, 0 FK)
- **order_set_attribute** (5 columns, 0 PK, 0 FK)
- **order_set_attribute_type** (11 columns, 0 PK, 0 FK)
- **order_set_member** (7 columns, 0 PK, 0 FK)
- **order_type_class_map** (0 columns, 0 PK, 0 FK)
- **patient_program_attribute** (5 columns, 0 PK, 0 FK)
- **person_merge_log** (5 columns, 0 PK, 0 FK)
- **referral_order** (6 columns, 0 PK, 0 FK)
- **relationship** (7 columns, 0 PK, 0 FK)
- **relationship_type** (6 columns, 0 PK, 0 FK)
- **report_object** (7 columns, 0 PK, 0 FK)
- **report_schema_xml** (0 columns, 0 PK, 0 FK)
- **role_privilege** (0 columns, 0 PK, 0 FK)
- **scheduler_task_config_property** (2 columns, 0 PK, 1 FK)
- **test_order** (6 columns, 0 PK, 0 FK)

## Summary Statistics
- Total tables: 117
- Total columns: 788
- Average columns per table: 6.7
