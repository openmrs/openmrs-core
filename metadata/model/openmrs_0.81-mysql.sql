#Date:                  2006/01/13 15:30:38

use openmrs;

SET FOREIGN_KEY_CHECKS=0;
#----------------------------
# Table structure for complex_obs
#----------------------------
drop table if exists complex_obs;
CREATE TABLE `complex_obs` (
  `obs_id` int(11) NOT NULL default '0',
  `mime_type_id` int(11) NOT NULL default '0',
  `urn` text,
  `complex_value` longtext,
  PRIMARY KEY  (`obs_id`),
  KEY `mime_type_of_content` (`mime_type_id`),
  CONSTRAINT `complex_obs_ibfk_1` FOREIGN KEY (`mime_type_id`) REFERENCES `mime_type` (`mime_type_id`),
  CONSTRAINT `obs_pointing_to_complex_content` FOREIGN KEY (`obs_id`) REFERENCES `obs` (`obs_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for concept
#----------------------------
drop table if exists concept;
CREATE TABLE `concept` (
  `concept_id` int(11) NOT NULL auto_increment,
  `retired` tinyint(1) NOT NULL default '0',
  `name` varchar(255) NOT NULL default '',
  `short_name` varchar(255) default NULL,
  `description` text,
  `form_text` text,
  `datatype_id` int(11) NOT NULL default '0',
  `class_id` int(11) NOT NULL default '0',
  `icd10` varchar(255) default NULL,
  `loinc` varchar(255) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `default_charge` int(11) default NULL,
  `version` varchar(50) default NULL,
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  `form_location` varchar(50) default NULL,
  `units` varchar(50) default NULL,
  `view_count` int(11) default NULL,
  PRIMARY KEY  (`concept_id`),
  KEY `concept_classes` (`class_id`),
  KEY `concept_creator` (`creator`),
  KEY `concept_datatypes` (`datatype_id`),
  KEY `user_who_changed_concept` (`changed_by`),
  CONSTRAINT `concept_classes` FOREIGN KEY (`class_id`) REFERENCES `concept_class` (`concept_class_id`),
  CONSTRAINT `concept_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `concept_datatypes` FOREIGN KEY (`datatype_id`) REFERENCES `concept_datatype` (`concept_datatype_id`),
  CONSTRAINT `user_who_changed_concept` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 69632 kB; (`class_id`) REFER `openmrs/concept_c';
#----------------------------
# Table structure for concept_answer
#----------------------------
drop table if exists concept_answer;
CREATE TABLE `concept_answer` (
  `concept_answer_id` int(11) NOT NULL auto_increment,
  `concept_id` int(11) NOT NULL default '0',
  `answer_concept` int(11) default NULL,
  `answer_drug` int(11) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`concept_answer_id`),
  KEY `answer_creator` (`creator`),
  KEY `answer` (`answer_concept`),
  KEY `answers_for_concept` (`concept_id`),
  CONSTRAINT `answer` FOREIGN KEY (`answer_concept`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `answers_for_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `answer_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for concept_class
#----------------------------
drop table if exists concept_class;
CREATE TABLE `concept_class` (
  `concept_class_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `description` varchar(255) NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `is_set` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`concept_class_id`),
  KEY `concept_class_creator` (`creator`),
  CONSTRAINT `concept_class_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 68608 kB; (`creator`) REFER `openmrs/users`(`us';
#----------------------------
# Table structure for concept_datatype
#----------------------------
drop table if exists concept_datatype;
CREATE TABLE `concept_datatype` (
  `concept_datatype_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `hl7_abbreviation` char(3) default NULL,
  `description` varchar(255) NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`concept_datatype_id`),
  KEY `concept_datatype_creator` (`creator`),
  CONSTRAINT `concept_datatype_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 68608 kB; (`creator`) REFER `openmrs/users`(`us';
#----------------------------
# Table structure for concept_map
#----------------------------
drop table if exists concept_map;
CREATE TABLE `concept_map` (
  `concept_map_id` int(11) NOT NULL auto_increment,
  `source` int(11) default NULL,
  `source_id` int(11) default NULL,
  `comment` varchar(255) character set utf8 default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`concept_map_id`),
  KEY `map_source` (`source`),
  KEY `map_creator` (`creator`),
  CONSTRAINT `map_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `map_source` FOREIGN KEY (`source`) REFERENCES `concept_source` (`concept_source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for concept_name
#----------------------------
drop table if exists concept_name;
CREATE TABLE `concept_name` (
  `concept_id` int(11) NOT NULL default '0',
  `name` varchar(255) NOT NULL default '',
  `short_name` varchar(255) default NULL,
  `description` text NOT NULL,
  `locale` varchar(50) NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`concept_id`,`locale`),
  KEY `user_who_created_name` (`creator`),
  CONSTRAINT `name_for_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `user_who_created_name` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 49152 kB; (`concept_id`) REFER `openmrs/concept';
#----------------------------
# Table structure for concept_numeric
#----------------------------
drop table if exists concept_numeric;
CREATE TABLE `concept_numeric` (
  `concept_id` int(11) NOT NULL default '0',
  `hi_absolute` double default NULL,
  `hi_critical` double default NULL,
  `hi_normal` double default NULL,
  `low_absolute` double default NULL,
  `low_critical` double default NULL,
  `low_normal` double default NULL,
  `units` varchar(50) default NULL,
  `precise` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`concept_id`),
  CONSTRAINT `numeric_attributes` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 38912 kB; (`creator`) REFER `openmrs/users`(`us';
#----------------------------
# Table structure for concept_set
#----------------------------
drop table if exists concept_set;
CREATE TABLE `concept_set` (
  `concept_id` int(11) NOT NULL default '0',
  `concept_set` int(11) NOT NULL default '0',
  `sort_weight` double default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`concept_id`,`concept_set`),
  KEY `has_a` (`concept_set`),
  KEY `user_who_created` (`creator`),
  CONSTRAINT `has_a` FOREIGN KEY (`concept_set`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `is_a` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `user_who_created` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 47104 kB; InnoDB free: 47104 kB';
#----------------------------
# Table structure for concept_set_derived
#----------------------------
drop table if exists concept_set_derived;
CREATE TABLE `concept_set_derived` (
  `concept_id` int(11) NOT NULL default '0',
  `concept_set` int(11) NOT NULL default '0',
  `sort_weight` double default NULL,
  PRIMARY KEY  (`concept_id`,`concept_set`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for concept_source
#----------------------------
drop table if exists concept_source;
CREATE TABLE `concept_source` (
  `concept_source_id` int(11) NOT NULL auto_increment,
  `name` varchar(50) character set utf8 NOT NULL default '',
  `description` text character set utf8 NOT NULL,
  `hl7_code` varchar(50) character set utf8 NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `voided` tinyint(4) default NULL,
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) character set utf8 default NULL,
  PRIMARY KEY  (`concept_source_id`),
  KEY `concept_source_creator` (`creator`),
  KEY `user_who_voided_concept_source` (`voided_by`),
  CONSTRAINT `concept_source_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_concept_source` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for concept_synonym
#----------------------------
drop table if exists concept_synonym;
CREATE TABLE `concept_synonym` (
  `concept_id` int(11) NOT NULL default '0',
  `synonym` varchar(255) NOT NULL default '',
  `locale` varchar(255) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`synonym`,`concept_id`),
  KEY `synonym_for` (`concept_id`),
  KEY `synonym_creator` (`creator`),
  CONSTRAINT `synonym_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `synonym_for` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for concept_word
#----------------------------
drop table if exists concept_word;
CREATE TABLE `concept_word` (
  `concept_id` int(11) NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `synonym` varchar(255) NOT NULL default '',
  `locale` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`concept_id`,`word`,`synonym`,`locale`),
  CONSTRAINT `word_for` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 49152 kB; InnoDB free: 48128 kB; (`concept_id`)';
#----------------------------
# Table structure for drug
#----------------------------
drop table if exists drug;
CREATE TABLE `drug` (
  `drug_id` int(11) NOT NULL default '0',
  `concept_id` int(11) NOT NULL default '0',
  `name` varchar(50) default NULL,
  `combination` tinyint(1) NOT NULL default '0',
  `daily_mg_per_kg` double default NULL,
  `dosage_form` varchar(255) default NULL,
  `dose_strength` double default NULL,
  `inn` longtext,
  `maximum_dose` double default NULL,
  `minimum_dose` double default NULL,
  `route` varchar(255) default NULL,
  `shelf_life` int(11) default NULL,
  `therapy_class` int(11) default NULL,
  `units` varchar(50) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`drug_id`),
  KEY `drug_creator` (`creator`),
  KEY `primary_drug_concept` (`concept_id`),
  CONSTRAINT `drug_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `primary_drug_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 38912 kB; (`creator`) REFER `openmrs/users`(`us';
#----------------------------
# Table structure for drug_ingredient
#----------------------------
drop table if exists drug_ingredient;
CREATE TABLE `drug_ingredient` (
  `concept_id` int(11) NOT NULL default '0',
  `ingredient_id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`ingredient_id`,`concept_id`),
  KEY `combination_drug` (`concept_id`),
  CONSTRAINT `combination_drug` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `ingredient` FOREIGN KEY (`ingredient_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for drug_order
#----------------------------
drop table if exists drug_order;
CREATE TABLE `drug_order` (
  `order_id` int(11) NOT NULL default '0',
  `drug_inventory_id` int(11) default '0',
  `dose` int(11) default NULL,
  `units` varchar(255) default NULL,
  `frequency` varchar(255) default NULL,
  `prn` tinyint(1) NOT NULL default '0',
  `complex` tinyint(1) NOT NULL default '0',
  `quantity` int(11) default NULL,
  PRIMARY KEY  (`order_id`),
  KEY `inventory_item` (`drug_inventory_id`),
  CONSTRAINT `extends_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `inventory_item` FOREIGN KEY (`drug_inventory_id`) REFERENCES `drug` (`drug_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 38912 kB; (`order_id`) REFER `openmrs/orders`(`';
#----------------------------
# Table structure for encounter
#----------------------------
drop table if exists encounter;
CREATE TABLE `encounter` (
  `encounter_id` int(11) NOT NULL auto_increment,
  `encounter_type` int(11) default NULL,
  `patient_id` int(11) NOT NULL default '0',
  `provider_id` int(11) NOT NULL default '0',
  `location_id` int(11) NOT NULL default '0',
  `form_id` int(11) default NULL,
  `encounter_datetime` datetime NOT NULL default '2005-01-01 00:00:00',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`encounter_id`),
  KEY `encounter_location` (`location_id`),
  KEY `encounter_patient` (`patient_id`),
  KEY `encounter_provider` (`provider_id`),
  KEY `encounter_type_id` (`encounter_type`),
  KEY `encounter_creator` (`creator`),
  KEY `encounter_form` (`form_id`),
  CONSTRAINT `encounter_form` FOREIGN KEY (`form_id`) REFERENCES `form` (`form_id`),
  CONSTRAINT `encounter_ibfk_1` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `encounter_location` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`),
  CONSTRAINT `encounter_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `encounter_provider` FOREIGN KEY (`provider_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `encounter_type_id` FOREIGN KEY (`encounter_type`) REFERENCES `encounter_type` (`encounter_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 49152 kB; (`creator`) REFER `openmrs/users`(`us';
#----------------------------
# Table structure for encounter_type
#----------------------------
drop table if exists encounter_type;
CREATE TABLE `encounter_type` (
  `encounter_type_id` int(11) NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `description` varchar(50) NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`encounter_type_id`),
  KEY `user_who_created_type` (`creator`),
  CONSTRAINT `user_who_created_type` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 69632 kB; (`creator`) REFER `openmrs/users`(`us';
#----------------------------
# Table structure for field
#----------------------------
drop table if exists `field`;
CREATE TABLE `field` (
  `field_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `description` text,
  `field_type` int(11) default NULL,
  `concept_id` int(11) default NULL,
  `table_name` varchar(50) default NULL,
  `attribute_name` varchar(50) default NULL,
  `select_multiple` tinyint(1) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  PRIMARY KEY  (`field_id`),
  KEY `concept_for_field` (`concept_id`),
  KEY `user_who_changed_field` (`changed_by`),
  KEY `user_who_created_field` (`creator`),
  KEY `type_of_field` (`field_type`),
  CONSTRAINT `concept_for_field` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `type_of_field` FOREIGN KEY (`field_type`) REFERENCES `field_type` (`field_type_id`),
  CONSTRAINT `user_who_changed_field` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_created_field` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 38912 kB; (`concept_id`) REFER `openmrs/concept';
#----------------------------
# Table structure for field_answer
#----------------------------
drop table if exists field_answer;
CREATE TABLE `field_answer` (
  `field_id` int(11) NOT NULL default '0',
  `answer_id` int(11) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`field_id`,`answer_id`),
  KEY `answers_for_field` (`field_id`),
  KEY `field_answer_concept` (`answer_id`),
  KEY `user_who_created_field_answer` (`creator`),
  CONSTRAINT `answers_for_field` FOREIGN KEY (`field_id`) REFERENCES `field` (`field_id`),
  CONSTRAINT `field_answer_concept` FOREIGN KEY (`answer_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `user_who_created_field_answer` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for field_type
#----------------------------
drop table if exists field_type;
CREATE TABLE `field_type` (
  `field_type_id` int(11) NOT NULL auto_increment,
  `name` varchar(50) default NULL,
  `description` longtext,
  `is_set` tinyint(1) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`field_type_id`),
  KEY `user_who_created_field_type` (`creator`),
  CONSTRAINT `user_who_created_field_type` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 38912 kB; (`creator`) REFER `openmrs/users`(`us';
#----------------------------
# Table structure for form
#----------------------------
drop table if exists form;
CREATE TABLE `form` (
  `form_id` int(11) NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `version` varchar(50) NOT NULL default '',
  `description` longtext,
  `schema_namespace` varchar(255) default NULL,
  `definition` longtext,
  `retired` tinyint(1) NOT NULL default '0',
  `retired_by` int(11) default NULL,
  `date_retired` datetime default NULL,
  `retired_reason` varchar(255) default NULL,
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`form_id`),
  KEY `user_who_created_form` (`creator`),
  KEY `user_who_last_changed_form` (`changed_by`),
  KEY `user_who_retired_form` (`retired_by`),
  CONSTRAINT `user_who_created_form` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_last_changed_form` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_retired_form` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 38912 kB; (`creator`) REFER `openmrs/users`(`us';
#----------------------------
# Table structure for form_field
#----------------------------
drop table if exists form_field;
CREATE TABLE `form_field` (
  `form_field_id` int(11) NOT NULL auto_increment,
  `form_id` int(11) NOT NULL default '0',
  `field_id` int(11) NOT NULL default '0',
  `field_number` int(11) default NULL,
  `field_part` varchar(5) default NULL,
  `page_number` int(11) default NULL,
  `parent_form_field` int(11) default NULL,
  `min_occurs` int(11) default NULL,
  `max_occurs` int(11) default NULL,
  `required` tinyint(1) default NULL,
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  `creator` int(11) default NULL,
  `date_created` datetime default NULL,
  PRIMARY KEY  (`form_field_id`),
  KEY `user_who_last_changed_form_field` (`changed_by`),
  KEY `field_within_form` (`field_id`),
  KEY `form_containing_field` (`form_id`),
  KEY `form_field_hierarchy` (`parent_form_field`),
  KEY `user_who_created_form_field` (`creator`),
  CONSTRAINT `field_within_form` FOREIGN KEY (`field_id`) REFERENCES `field` (`field_id`),
  CONSTRAINT `form_containing_field` FOREIGN KEY (`form_id`) REFERENCES `form` (`form_id`),
  CONSTRAINT `form_field_hierarchy` FOREIGN KEY (`parent_form_field`) REFERENCES `form_field` (`form_field_id`),
  CONSTRAINT `user_who_created_form_field` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_last_changed_form_field` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 38912 kB; (`field_id`) REFER `openmrs/field`(`f';
#----------------------------
# Table structure for group_role
#----------------------------
drop table if exists group_role;
CREATE TABLE `group_role` (
  `group_name` varchar(50) NOT NULL default '',
  `role` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`group_name`,`role`),
  CONSTRAINT `group_definition` FOREIGN KEY (`group_name`) REFERENCES `groups` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='InnoDB free: 49152 kB; InnoDB free: 38912 kB; (`group`) REFE';
#----------------------------
# Table structure for groups
#----------------------------
drop table if exists groups;
CREATE TABLE `groups` (
  `group_name` varchar(50) NOT NULL default '',
  `description` varchar(255) default NULL,
  PRIMARY KEY  (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='InnoDB free: 38912 kB';
#----------------------------
# Table structure for icd10
#----------------------------
drop table if exists icd10;
CREATE TABLE `icd10` (
  `icd10_id` int(11) NOT NULL default '0',
  `code` varchar(255) default NULL,
  `name` varchar(255) default NULL,
  PRIMARY KEY  (`icd10_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for location
#----------------------------
drop table if exists location;
CREATE TABLE `location` (
  `location_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `description` varchar(255) default NULL,
  `address1` varchar(50) default NULL,
  `address2` varchar(50) default NULL,
  `city_village` varchar(50) default NULL,
  `state_province` varchar(50) default NULL,
  `postal_code` varchar(50) default NULL,
  `country` varchar(50) default NULL,
  `latitude` varchar(50) default NULL,
  `longitude` varchar(50) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`location_id`),
  KEY `user_who_created_location` (`creator`),
  CONSTRAINT `user_who_created_location` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for mime_type
#----------------------------
drop table if exists mime_type;
CREATE TABLE `mime_type` (
  `mime_type_id` int(11) NOT NULL auto_increment,
  `mime_type` varchar(75) NOT NULL default '',
  `description` text,
  PRIMARY KEY  (`mime_type_id`),
  KEY `mime_type_id` (`mime_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 69632 kB';
#----------------------------
# Table structure for note
#----------------------------
drop table if exists note;
CREATE TABLE `note` (
  `note_id` int(11) NOT NULL default '0',
  `note_type` varchar(50) default NULL,
  `patient_id` int(11) default NULL,
  `obs_id` int(11) default NULL,
  `encounter_id` int(11) default NULL,
  `text` text NOT NULL,
  `priority` int(11) default NULL,
  `weight` int(11) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  PRIMARY KEY  (`note_id`),
  KEY `patient_note` (`patient_id`),
  KEY `obs_note` (`obs_id`),
  KEY `encounter_note` (`encounter_id`),
  KEY `user_who_created_note` (`creator`),
  KEY `user_who_changed_note` (`changed_by`),
  CONSTRAINT `encounter_note` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
  CONSTRAINT `obs_note` FOREIGN KEY (`obs_id`) REFERENCES `obs` (`obs_id`),
  CONSTRAINT `patient_note` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `user_who_changed_note` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_created_note` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='InnoDB free: 49152 kB; InnoDB free: 49152 kB; (`patient_id`)';
#----------------------------
# Table structure for obs
#----------------------------
drop table if exists obs;
CREATE TABLE `obs` (
  `obs_id` int(11) NOT NULL auto_increment,
  `patient_id` int(11) NOT NULL default '0',
  `concept_id` int(11) NOT NULL default '0',
  `encounter_id` int(11) default NULL,
  `order_id` int(11) default NULL,
  `obs_datetime` datetime NOT NULL default '2005-01-01 00:00:00',
  `location_id` int(11) NOT NULL default '0',
  `obs_group_id` int(11) default NULL,
  `accession_number` varchar(255) default NULL,
  `value_group_id` int(11) default NULL,
  `value_boolean` tinyint(1) default NULL,
  `value_coded` int(11) default NULL,
  `value_datetime` datetime default NULL,
  `value_numeric` double default NULL,
  `value_modifier` char(2) default NULL,
  `value_text` text,
  `date_started` datetime default NULL,
  `date_stopped` datetime default NULL,
  `comment` varchar(255) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`obs_id`),
  KEY `answer_concept` (`value_coded`),
  KEY `encounter_observations` (`encounter_id`),
  KEY `obs_concept` (`concept_id`),
  KEY `obs_enterer` (`creator`),
  KEY `obs_location` (`location_id`),
  KEY `obs_order` (`order_id`),
  KEY `patient_obs` (`patient_id`),
  KEY `user_who_voided_obs` (`voided_by`),
  CONSTRAINT `answer_concept` FOREIGN KEY (`value_coded`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `encounter_observations` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
  CONSTRAINT `obs_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `obs_enterer` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `obs_location` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`),
  CONSTRAINT `obs_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `patient_obs` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `user_who_voided_obs` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 49152 kB; (`value_coded`) REFER `openmrs/concep';
#----------------------------
# Table structure for order_type
#----------------------------
drop table if exists order_type;
CREATE TABLE `order_type` (
  `order_type_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `description` varchar(255) NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`order_type_id`),
  KEY `type_created_by` (`creator`),
  CONSTRAINT `type_created_by` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 69632 kB; (`creator`) REFER `openmrs/users`(`us';
#----------------------------
# Table structure for orders
#----------------------------
drop table if exists orders;
CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL auto_increment,
  `order_type_id` int(11) NOT NULL default '0',
  `concept_id` int(11) NOT NULL default '0',
  `orderer` int(11) default '0',
  `encounter_id` int(11) default NULL,
  `instructions` text,
  `start_date` datetime default NULL,
  `auto_expire_date` datetime default NULL,
  `discontinued` tinyint(1) NOT NULL default '0',
  `discontinued_date` datetime default NULL,
  `discontinued_by` int(11) default NULL,
  `discontinued_reason` varchar(255) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`order_id`),
  KEY `order_creator` (`creator`),
  KEY `orderer_not_drug` (`orderer`),
  KEY `orders_in_encounter` (`encounter_id`),
  KEY `type_of_order` (`order_type_id`),
  KEY `user_who_discontinued_order` (`discontinued_by`),
  KEY `user_who_voided_order` (`voided_by`),
  CONSTRAINT `orderer_not_drug` FOREIGN KEY (`orderer`) REFERENCES `users` (`user_id`),
  CONSTRAINT `orders_in_encounter` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
  CONSTRAINT `order_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `type_of_order` FOREIGN KEY (`order_type_id`) REFERENCES `order_type` (`order_type_id`),
  CONSTRAINT `user_who_discontinued_order` FOREIGN KEY (`discontinued_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_order` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 69632 kB; (`orderer`) REFER `openmrs/users`(`us';
#----------------------------
# Table structure for patient
#----------------------------
drop table if exists patient;
CREATE TABLE `patient` (
  `patient_id` int(11) NOT NULL auto_increment,
  `gender` varchar(50) NOT NULL default '',
  `race` varchar(50) default NULL,
  `birthdate` date default NULL,
  `birthdate_estimated` tinyint(1) default NULL,
  `birthplace` varchar(50) default NULL,
  `tribe` int(11) default NULL,
  `citizenship` varchar(50) default NULL,
  `mothers_name` varchar(50) default NULL,
  `civil_status` int(11) default NULL,
  `death_date` datetime default NULL,
  `cause_of_death` varchar(255) default NULL,
  `health_district` varchar(255) default NULL,
  `health_center` int(11) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`patient_id`),
  KEY `belongs_to_tribe` (`tribe`),
  KEY `user_who_created_patient` (`creator`),
  KEY `user_who_voided_patient` (`voided_by`),
  KEY `user_who_changed_pat` (`changed_by`),
  CONSTRAINT `belongs_to_tribe` FOREIGN KEY (`tribe`) REFERENCES `tribe` (`tribe_id`),
  CONSTRAINT `user_who_changed_pat` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_created_patient` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_patient` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 38912 kB; (`tribe`) REFER `openmrs/tribe`(`trib';
#----------------------------
# Table structure for patient_address
#----------------------------
drop table if exists patient_address;
CREATE TABLE `patient_address` (
  `patient_address_id` int(11) NOT NULL auto_increment,
  `patient_id` int(11) NOT NULL default '0',
  `preferred` tinyint(1) NOT NULL default '0',
  `address1` varchar(50) default NULL,
  `address2` varchar(50) default NULL,
  `city_village` varchar(50) default NULL,
  `state_province` varchar(50) default NULL,
  `postal_code` varchar(50) default NULL,
  `country` varchar(50) default NULL,
  `latitude` varchar(50) default NULL,
  `longitude` varchar(50) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`patient_address_id`),
  KEY `patient_address_creator` (`creator`),
  KEY `patient_addresses` (`patient_id`),
  KEY `patient_address_void` (`voided_by`),
  CONSTRAINT `patient_addresses` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `patient_address_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `patient_address_void` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 68608 kB; (`patient_id`) REFER `openmrs/patient';
#----------------------------
# Table structure for patient_identifier
#----------------------------
drop table if exists patient_identifier;
CREATE TABLE `patient_identifier` (
  `patient_id` int(11) NOT NULL default '0',
  `identifier` varchar(50) NOT NULL default '',
  `identifier_type` int(11) NOT NULL default '0',
  `location_id` int(11) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`patient_id`,`identifier`,`identifier_type`,`location_id`),
  KEY `defines_identifier_type` (`identifier_type`),
  KEY `identifier_creator` (`creator`),
  KEY `identifier_voider` (`voided_by`),
  KEY `identifier_location` (`location_id`),
  CONSTRAINT `defines_identifier_type` FOREIGN KEY (`identifier_type`) REFERENCES `patient_identifier_type` (`patient_identifier_type_id`),
  CONSTRAINT `identifier_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `identifier_voider` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `identifies_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `patient_identifier_ibfk_2` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 49152 kB; (`identifier_type`) REFER `openmrs/pa';
#----------------------------
# Table structure for patient_identifier_type
#----------------------------
drop table if exists patient_identifier_type;
CREATE TABLE `patient_identifier_type` (
  `patient_identifier_type_id` int(11) NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `description` text NOT NULL,
  `format` varchar(50) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`patient_identifier_type_id`),
  KEY `type_creator` (`creator`),
  CONSTRAINT `type_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 38912 kB; (`creator`) REFER `openmrs/users`(`us';
#----------------------------
# Table structure for patient_name
#----------------------------
drop table if exists patient_name;
CREATE TABLE `patient_name` (
  `patient_name_id` int(11) NOT NULL auto_increment,
  `preferred` tinyint(1) NOT NULL default '0',
  `patient_id` int(11) NOT NULL default '0',
  `prefix` varchar(50) default NULL,
  `given_name` varchar(50) default NULL,
  `middle_name` varchar(50) default NULL,
  `family_name_prefix` varchar(50) default NULL,
  `family_name` varchar(50) default NULL,
  `family_name2` varchar(50) default NULL,
  `family_name_suffix` varchar(50) default NULL,
  `degree` varchar(50) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  PRIMARY KEY  (`patient_name_id`),
  KEY `name_for_patient` (`patient_id`),
  KEY `user_who_made_name` (`creator`),
  KEY `user_who_voided_name` (`voided_by`),
  CONSTRAINT `name_for_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `user_who_made_name` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_name` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 69632 kB; (`patient_id`) REFER `openmrs/patient';
#----------------------------
# Table structure for person
#----------------------------
drop table if exists person;
CREATE TABLE `person` (
  `person_id` int(11) NOT NULL auto_increment,
  `patient_id` int(11) default NULL,
  `user_id` int(11) default NULL,
  PRIMARY KEY  (`person_id`),
  KEY `patients` (`patient_id`),
  KEY `users` (`user_id`),
  CONSTRAINT `patients` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for privilege
#----------------------------
drop table if exists privilege;
CREATE TABLE `privilege` (
  `privilege` varchar(50) NOT NULL default '',
  `description` varchar(250) NOT NULL default '',
  PRIMARY KEY  (`privilege`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for relationship
#----------------------------
drop table if exists relationship;
CREATE TABLE `relationship` (
  `relationship_id` int(11) NOT NULL auto_increment,
  `person_id` int(11) NOT NULL default '0',
  `relationship` int(11) NOT NULL default '0',
  `relative_id` int(11) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`relationship_id`),
  KEY `related_person` (`person_id`),
  KEY `related_relative` (`relative_id`),
  KEY `relationship_type` (`relationship`),
  KEY `relation_creator` (`creator`),
  KEY `relation_voider` (`voided_by`),
  CONSTRAINT `related_person` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`),
  CONSTRAINT `related_relative` FOREIGN KEY (`relative_id`) REFERENCES `person` (`person_id`),
  CONSTRAINT `relationship_type` FOREIGN KEY (`relationship`) REFERENCES `relationship_type` (`relationship_id`),
  CONSTRAINT `relation_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `relation_voider` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 38912 kB; (`person_id`) REFER `openmrs/person`(';
#----------------------------
# Table structure for relationship_type
#----------------------------
drop table if exists relationship_type;
CREATE TABLE `relationship_type` (
  `relationship_id` int(11) NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `description` varchar(255) NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  PRIMARY KEY  (`relationship_id`),
  KEY `user_who_created_rel` (`creator`),
  CONSTRAINT `user_who_created_rel` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for role
#----------------------------
drop table if exists role;
CREATE TABLE `role` (
  `role` varchar(50) NOT NULL default '',
  `description` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for role_privilege
#----------------------------
drop table if exists role_privilege;
CREATE TABLE `role_privilege` (
  `role` varchar(50) NOT NULL default '',
  `privilege` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`privilege`,`role`),
  KEY `role_privilege` (`role`),
  CONSTRAINT `privilege_definitons` FOREIGN KEY (`privilege`) REFERENCES `privilege` (`privilege`),
  CONSTRAINT `role_privilege` FOREIGN KEY (`role`) REFERENCES `role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for tribe
#----------------------------
drop table if exists tribe;
CREATE TABLE `tribe` (
  `tribe_id` int(11) NOT NULL auto_increment,
  `retired` tinyint(1) NOT NULL default '0',
  `name` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`tribe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 67584 kB; InnoDB free: 38912 kB; InnoDB free: 3';
#----------------------------
# Table structure for user_group
#----------------------------
drop table if exists user_group;
CREATE TABLE `user_group` (
  `user_id` int(11) NOT NULL default '0',
  `group_name` varchar(50) NOT NULL default '',
  KEY `user_group` (`user_id`),
  KEY `group_role` (`group_name`),
  CONSTRAINT `group_role` FOREIGN KEY (`group_name`) REFERENCES `groups` (`group_name`),
  CONSTRAINT `user_group` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='InnoDB free: 38912 kB; (`user_id`) REFER `openmrs/users`(`us';
#----------------------------
# Table structure for user_property
#----------------------------
drop table if exists user_property;
CREATE TABLE `user_property` (
  `user_id` int(11) NOT NULL default '0',
  `key` varchar(100) NOT NULL default '',
  `value` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`user_id`,`key`),
  CONSTRAINT `user_property` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='InnoDB free: 38912 kB; (`user_id`) REFER `openmrs/users`(`us';
#----------------------------
# Table structure for user_role
#----------------------------
drop table if exists user_role;
CREATE TABLE `user_role` (
  `user_id` int(11) NOT NULL default '0',
  `role` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`role`,`user_id`),
  KEY `user_role` (`user_id`),
  CONSTRAINT `role_definitions` FOREIGN KEY (`role`) REFERENCES `role` (`role`),
  CONSTRAINT `user_role` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
#----------------------------
# Table structure for users
#----------------------------
drop table if exists users;
CREATE TABLE `users` (
  `user_id` int(11) NOT NULL auto_increment,
  `username` varchar(50) default NULL,
  `first_name` varchar(50) default NULL,
  `middle_name` varchar(50) default NULL,
  `last_name` varchar(50) default NULL,
  `password` varchar(50) default NULL,
  `salt` varchar(50) default NULL,
  `secret_question` varchar(255) default NULL,
  `secret_answer` varchar(255) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`user_id`),
  KEY `user_creator` (`creator`),
  KEY `user_who_changed_user` (`changed_by`),
  KEY `user_who_voided_user` (`voided_by`),
  CONSTRAINT `user_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_changed_user` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_user` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 66560 kB; (`creator`) REFER `openmrs/users`(`us';

#----------------------------
# Table structure for reports
#----------------------------
drop table if exists report;
CREATE TABLE `report` (
  `report_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `description` text default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '2005-01-01 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  `voided` tinyint(1) default NULL,
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`report_id`),
  KEY `report_creator` (`creator`),
  KEY `user_who_changed_report` (`changed_by`),
  KEY `user_who_voided_report` (`voided_by`),
  CONSTRAINT `report_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_changed_report` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_report` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 66560 kB; (`creator`) REFER `openmrs/users`(`us';

