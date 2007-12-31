-- MySQL dump 10.10
--
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cohort`
--

DROP TABLE IF EXISTS `cohort`;
CREATE TABLE `cohort` (
  `cohort_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `description` varchar(1000) default NULL,
  `creator` int(11) NOT NULL,
  `date_created` datetime NOT NULL,
  `voided` tinyint(1) NOT NULL,
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`cohort_id`),
  KEY `cohort_creator` (`creator`),
  KEY `user_who_voided_cohort` (`voided_by`),
  CONSTRAINT `cohort_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_cohort` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `cohort_member`
--

DROP TABLE IF EXISTS `cohort_member`;
CREATE TABLE `cohort_member` (
  `cohort_id` int(11) NOT NULL default '0',
  `patient_id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`cohort_id`,`patient_id`),
  KEY `cohort` (`cohort_id`),
  KEY `patient` (`patient_id`),
  CONSTRAINT `member_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE,
  CONSTRAINT `parent_cohort` FOREIGN KEY (`cohort_id`) REFERENCES `cohort` (`cohort_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `complex_obs`
--

DROP TABLE IF EXISTS `complex_obs`;
CREATE TABLE `complex_obs` (
  `obs_id` int(11) NOT NULL default '0',
  `mime_type_id` int(11) NOT NULL default '0',
  `urn` text,
  `complex_value` longtext,
  PRIMARY KEY  (`obs_id`),
  KEY `mime_type_of_content` (`mime_type_id`),
  CONSTRAINT `complex_obs_ibfk_1` FOREIGN KEY (`mime_type_id`) REFERENCES `mime_type` (`mime_type_id`),
  CONSTRAINT `obs_pointing_to_complex_content` FOREIGN KEY (`obs_id`) REFERENCES `obs` (`obs_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `concept`
--

DROP TABLE IF EXISTS `concept`;
CREATE TABLE `concept` (
  `concept_id` int(11) NOT NULL auto_increment,
  `retired` tinyint(1) NOT NULL default '0',
  `short_name` varchar(255) default NULL,
  `description` text,
  `form_text` text,
  `datatype_id` int(11) NOT NULL default '0',
  `class_id` int(11) NOT NULL default '0',
  `is_set` tinyint(1) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `default_charge` int(11) default NULL,
  `version` varchar(50) default NULL,
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  PRIMARY KEY  (`concept_id`),
  KEY `concept_classes` (`class_id`),
  KEY `concept_creator` (`creator`),
  KEY `concept_datatypes` (`datatype_id`),
  KEY `user_who_changed_concept` (`changed_by`),
  CONSTRAINT `concept_classes` FOREIGN KEY (`class_id`) REFERENCES `concept_class` (`concept_class_id`),
  CONSTRAINT `concept_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `concept_datatypes` FOREIGN KEY (`datatype_id`) REFERENCES `concept_datatype` (`concept_datatype_id`),
  CONSTRAINT `user_who_changed_concept` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `concept` DISABLE KEYS */;
LOCK TABLES `concept` WRITE;
INSERT INTO `concept` VALUES (1,0,'','',NULL,2,7,0,1,'2007-05-04 09:59:01',NULL,'',1,'2007-05-04 09:59:01'),(2,0,'','',NULL,4,11,0,1,'2007-05-04 09:59:01',NULL,'',1,'2007-05-04 09:59:01'),(3,0,'','',NULL,4,11,0,1,'2007-05-04 09:59:02',NULL,'',1,'2007-05-04 09:59:02'),(4,0,'','',NULL,2,7,0,1,'2007-05-04 09:59:03',NULL,'',1,'2007-05-04 09:59:03');
UNLOCK TABLES;
/*!40000 ALTER TABLE `concept` ENABLE KEYS */;

--
-- Table structure for table `concept_answer`
--

DROP TABLE IF EXISTS `concept_answer`;
CREATE TABLE `concept_answer` (
  `concept_answer_id` int(11) NOT NULL auto_increment,
  `concept_id` int(11) NOT NULL default '0',
  `answer_concept` int(11) default NULL,
  `answer_drug` int(11) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`concept_answer_id`),
  KEY `answer_creator` (`creator`),
  KEY `answer` (`answer_concept`),
  KEY `answers_for_concept` (`concept_id`),
  CONSTRAINT `answer` FOREIGN KEY (`answer_concept`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `answers_for_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `answer_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `concept_answer` DISABLE KEYS */;
LOCK TABLES `concept_answer` WRITE;
INSERT INTO `concept_answer` VALUES (1,1,2,NULL,1,'2007-05-04 09:59:02'),(2,1,3,NULL,1,'2007-05-04 09:59:02'),(3,4,2,NULL,1,'2007-05-04 09:59:03'),(4,4,3,NULL,1,'2007-05-04 09:59:03');
UNLOCK TABLES;
/*!40000 ALTER TABLE `concept_answer` ENABLE KEYS */;

--
-- Table structure for table `concept_class`
--

DROP TABLE IF EXISTS `concept_class`;
CREATE TABLE `concept_class` (
  `concept_class_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `description` varchar(255) NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`concept_class_id`),
  KEY `concept_class_creator` (`creator`),
  CONSTRAINT `concept_class_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `concept_class` DISABLE KEYS */;
LOCK TABLES `concept_class` WRITE;
INSERT INTO `concept_class` VALUES (1,'Test','Acq. during patient encounter (vitals, labs, etc.)',1,'2004-02-02 00:00:00'),(2,'Procedure','Describes a clinical procedure',1,'2004-03-02 00:00:00'),(3,'Drug','Drug',1,'2004-02-02 00:00:00'),(4,'Diagnosis','Conclusion drawn through findings',1,'2004-02-02 00:00:00'),(5,'Finding','Practitioner observation/finding',1,'2004-03-02 00:00:00'),(6,'Anatomy','Anatomic sites / descriptors',1,'2004-03-02 00:00:00'),(7,'Question','Question (eg, patient history, SF36 items)',1,'2004-03-02 00:00:00'),(8,'LabSet','Term to describe laboratory sets',1,'2004-03-02 00:00:00'),(9,'MedSet','Term to describe medication sets',1,'2004-02-02 00:00:00'),(10,'ConvSet','Term to describe convenience sets',1,'2004-03-02 00:00:00'),(11,'Misc','Terms which don\'t fit other categories',1,'2004-03-02 00:00:00'),(12,'Symptom','Patient-reported observation',1,'2004-10-04 00:00:00'),(13,'Symptom/Finding','Observation that can be reported from patient or found on exam',1,'2004-10-04 00:00:00'),(14,'Specimen','Body or fluid specimen',1,'2004-12-02 00:00:00'),(15,'Misc Order','Orderable items which aren\'t tests or drugs',1,'2005-02-17 00:00:00');
UNLOCK TABLES;
/*!40000 ALTER TABLE `concept_class` ENABLE KEYS */;

--
-- Table structure for table `concept_datatype`
--

DROP TABLE IF EXISTS `concept_datatype`;
CREATE TABLE `concept_datatype` (
  `concept_datatype_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `hl7_abbreviation` varchar(3) default NULL,
  `description` varchar(255) NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`concept_datatype_id`),
  KEY `concept_datatype_creator` (`creator`),
  CONSTRAINT `concept_datatype_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `concept_datatype` DISABLE KEYS */;
LOCK TABLES `concept_datatype` WRITE;
INSERT INTO `concept_datatype` VALUES (1,'Numeric','NM','Numeric value, including integer or float (e.g., creatinine, weight)',1,'2004-02-02 00:00:00'),(2,'Coded','CWE','Value determined by term dictionary lookup (i.e., term identifier)',1,'2004-02-02 00:00:00'),(3,'Text','ST','Free text',1,'2004-02-02 00:00:00'),(4,'N/A','ZZ','Not associated with a datatype (e.g., term answers, sets)',1,'2004-02-02 00:00:00'),(5,'Document','RP','Pointer to a binary or text-based document (e.g., clinical document, RTF, XML, EKG, image, etc.) stored in complex_obs table',1,'2004-04-15 00:00:00'),(6,'Date','DT','Absolute date',1,'2004-07-22 00:00:00'),(7,'Time','TM','Absolute time of day',1,'2004-07-22 00:00:00'),(8,'Datetime','TS','Absolute date and time',1,'2004-07-22 00:00:00'),(10,'Boolean','BIT','Boolean value (yes/no, true/false)',1,'2004-08-26 00:00:00'),(11,'Rule','ZZ','Value derived from other data',1,'2006-09-11 13:22:00'),(12,'Structured Numeric','SN','Complex numeric values possible (ie, <5, 1-10, etc.)',1,'2005-08-06 00:00:00');
UNLOCK TABLES;
/*!40000 ALTER TABLE `concept_datatype` ENABLE KEYS */;

--
-- Table structure for table `concept_derived`
--

DROP TABLE IF EXISTS `concept_derived`;
CREATE TABLE `concept_derived` (
  `concept_id` int(11) NOT NULL default '0',
  `rule` mediumtext,
  `compile_date` datetime default NULL,
  `compile_status` varchar(255) default NULL,
  `class_name` varchar(1024) default NULL,
  PRIMARY KEY  (`concept_id`),
  CONSTRAINT `derived_attributes` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `concept_map`
--

DROP TABLE IF EXISTS `concept_map`;
CREATE TABLE `concept_map` (
  `concept_map_id` int(11) NOT NULL auto_increment,
  `source` int(11) default NULL,
  `source_id` int(11) default NULL,
  `comment` varchar(255) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`concept_map_id`),
  KEY `map_source` (`source`),
  KEY `map_creator` (`creator`),
  CONSTRAINT `map_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `map_source` FOREIGN KEY (`source`) REFERENCES `concept_source` (`concept_source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `concept_name`
--

DROP TABLE IF EXISTS `concept_name`;
CREATE TABLE `concept_name` (
  `concept_id` int(11) NOT NULL default '0',
  `name` varchar(255) NOT NULL default '',
  `short_name` varchar(255) default NULL,
  `description` text NOT NULL,
  `locale` varchar(50) NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`concept_id`,`locale`),
  KEY `user_who_created_name` (`creator`),
  KEY `name_of_concept` (`name`),
  KEY `short_name_of_concept` (`short_name`),
  CONSTRAINT `name_for_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `user_who_created_name` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `concept_name` DISABLE KEYS */;
LOCK TABLES `concept_name` WRITE;
INSERT INTO `concept_name` VALUES (1,'CAUSE OF DEATH','','Describes a cause of death for a patient.  Coded answer.','en',1,'2007-05-04 09:59:01'),(2,'OTHER NON-CODED','','Non-coded answer to a coded question - allows other as a coded answer.','en',1,'2007-05-04 09:59:01'),(3,'NONE','','Generic descriptive answer.','en',1,'2007-05-04 09:59:02'),(4,'REASON ORDER STOPPED','','Describes a reason for stopping an order.  Coded answer.','en',1,'2007-05-04 09:59:03');
UNLOCK TABLES;
/*!40000 ALTER TABLE `concept_name` ENABLE KEYS */;

--
-- Table structure for table `concept_numeric`
--

DROP TABLE IF EXISTS `concept_numeric`;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `concept_proposal`
--

DROP TABLE IF EXISTS `concept_proposal`;
CREATE TABLE `concept_proposal` (
  `concept_proposal_id` int(11) NOT NULL auto_increment,
  `concept_id` int(11) default NULL,
  `encounter_id` int(11) default NULL,
  `original_text` varchar(255) NOT NULL default '',
  `final_text` varchar(255) default NULL,
  `obs_id` int(11) default NULL,
  `obs_concept_id` int(11) default NULL,
  `state` varchar(32) NOT NULL default 'UNMAPPED' COMMENT 'Valid values are: UNMAPPED, SYNONYM, CONCEPT, REJECT',
  `comments` varchar(255) default NULL COMMENT 'Comment from concept admin/mapper',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  PRIMARY KEY  (`concept_proposal_id`),
  KEY `encounter_for_proposal` (`encounter_id`),
  KEY `concept_for_proposal` (`concept_id`),
  KEY `user_who_created_proposal` (`creator`),
  KEY `user_who_changed_proposal` (`changed_by`),
  KEY `proposal_obs_id` (`obs_id`),
  KEY `proposal_obs_concept_id` (`obs_concept_id`),
  CONSTRAINT `concept_for_proposal` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `encounter_for_proposal` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
  CONSTRAINT `proposal_obs_concept_id` FOREIGN KEY (`obs_concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `proposal_obs_id` FOREIGN KEY (`obs_id`) REFERENCES `obs` (`obs_id`),
  CONSTRAINT `user_who_changed_proposal` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_created_proposal` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `concept_set`
--

DROP TABLE IF EXISTS `concept_set`;
CREATE TABLE `concept_set` (
  `concept_id` int(11) NOT NULL default '0',
  `concept_set` int(11) NOT NULL default '0',
  `sort_weight` double default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`concept_id`,`concept_set`),
  KEY `has_a` (`concept_set`),
  KEY `user_who_created` (`creator`),
  CONSTRAINT `has_a` FOREIGN KEY (`concept_set`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `is_a` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `user_who_created` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `concept_set_derived`
--

DROP TABLE IF EXISTS `concept_set_derived`;
CREATE TABLE `concept_set_derived` (
  `concept_id` int(11) NOT NULL default '0',
  `concept_set` int(11) NOT NULL default '0',
  `sort_weight` double default NULL,
  PRIMARY KEY  (`concept_id`,`concept_set`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `concept_source`
--

DROP TABLE IF EXISTS `concept_source`;
CREATE TABLE `concept_source` (
  `concept_source_id` int(11) NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `description` text NOT NULL,
  `hl7_code` varchar(50) NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `voided` tinyint(4) default NULL,
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`concept_source_id`),
  KEY `concept_source_creator` (`creator`),
  KEY `user_who_voided_concept_source` (`voided_by`),
  CONSTRAINT `concept_source_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_concept_source` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `concept_state_conversion`
--

DROP TABLE IF EXISTS `concept_state_conversion`;
CREATE TABLE `concept_state_conversion` (
  `concept_state_conversion_id` int(11) NOT NULL auto_increment,
  `concept_id` int(11) default '0',
  `program_workflow_id` int(11) default '0',
  `program_workflow_state_id` int(11) default '0',
  PRIMARY KEY  (`concept_state_conversion_id`),
  UNIQUE KEY `unique_workflow_concept_in_conversion` (`program_workflow_id`,`concept_id`),
  KEY `triggering_concept` (`concept_id`),
  KEY `affected_workflow` (`program_workflow_id`),
  KEY `resulting_state` (`program_workflow_state_id`),
  CONSTRAINT `concept_triggers_conversion` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `conversion_involves_workflow` FOREIGN KEY (`program_workflow_id`) REFERENCES `program_workflow` (`program_workflow_id`),
  CONSTRAINT `conversion_to_state` FOREIGN KEY (`program_workflow_state_id`) REFERENCES `program_workflow_state` (`program_workflow_state_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `concept_synonym`
--

DROP TABLE IF EXISTS `concept_synonym`;
CREATE TABLE `concept_synonym` (
  `concept_id` int(11) NOT NULL default '0',
  `synonym` varchar(255) NOT NULL default '',
  `locale` varchar(255) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`synonym`,`concept_id`),
  KEY `synonym_for` (`concept_id`),
  KEY `synonym_creator` (`creator`),
  CONSTRAINT `synonym_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `synonym_for` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `concept_word`
--

DROP TABLE IF EXISTS `concept_word`;
CREATE TABLE `concept_word` (
  `concept_id` int(11) NOT NULL default '0',
  `word` varchar(50) NOT NULL default '',
  `synonym` varchar(255) NOT NULL default '',
  `locale` varchar(20) NOT NULL default '',
  PRIMARY KEY  (`concept_id`,`word`,`synonym`,`locale`),
  KEY `word_in_concept_name` (`word`),
  CONSTRAINT `word_for` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `concept_word` DISABLE KEYS */;
LOCK TABLES `concept_word` WRITE;
INSERT INTO `concept_word` VALUES (3,'AUCUN','','fr'),(3,'AUCUN','AUCUN','en'),(2,'AUTRE','','fr'),(2,'AUTRES','','fr'),(1,'CAUSE','','en'),(2,'CODED','','en'),(1,'DEATH','','en'),(4,'DISCONTINUE','','en'),(4,'DISCONTINUED','','en'),(4,'DRUG','','en'),(2,'NON','','en'),(4,'ORDER','','en'),(2,'OTHER','','en'),(3,'OTHER','','en'),(4,'REASON','','en'),(4,'STOP','','en'),(4,'STOPPED','','en');
UNLOCK TABLES;
/*!40000 ALTER TABLE `concept_word` ENABLE KEYS */;

--
-- Table structure for table `drug`
--

DROP TABLE IF EXISTS `drug`;
CREATE TABLE `drug` (
  `drug_id` int(11) NOT NULL auto_increment,
  `concept_id` int(11) NOT NULL default '0',
  `name` varchar(50) default NULL,
  `combination` tinyint(1) NOT NULL default '0',
  `dosage_form` int(11) default NULL,
  `dose_strength` double default NULL,
  `maximum_daily_dose` double default NULL,
  `minimum_daily_dose` double default NULL,
  `route` int(11) default NULL,
  `units` varchar(50) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`drug_id`),
  KEY `drug_creator` (`creator`),
  KEY `primary_drug_concept` (`concept_id`),
  KEY `dosage_form_concept` (`dosage_form`),
  KEY `route_concept` (`route`),
  KEY `user_who_voided_drug` (`voided_by`),
  CONSTRAINT `dosage_form_concept` FOREIGN KEY (`dosage_form`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `drug_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `primary_drug_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `route_concept` FOREIGN KEY (`route`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `user_who_voided_drug` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `drug_ingredient`
--

DROP TABLE IF EXISTS `drug_ingredient`;
CREATE TABLE `drug_ingredient` (
  `concept_id` int(11) NOT NULL default '0',
  `ingredient_id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`ingredient_id`,`concept_id`),
  KEY `combination_drug` (`concept_id`),
  CONSTRAINT `combination_drug` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `ingredient` FOREIGN KEY (`ingredient_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `drug_order`
--

DROP TABLE IF EXISTS `drug_order`;
CREATE TABLE `drug_order` (
  `order_id` int(11) NOT NULL default '0',
  `drug_inventory_id` int(11) default '0',
  `dose` double default NULL,
  `equivalent_daily_dose` double default NULL,
  `units` varchar(255) default NULL,
  `frequency` varchar(255) default NULL,
  `prn` tinyint(1) NOT NULL default '0',
  `complex` tinyint(1) NOT NULL default '0',
  `quantity` int(11) default NULL,
  PRIMARY KEY  (`order_id`),
  KEY `inventory_item` (`drug_inventory_id`),
  CONSTRAINT `extends_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `inventory_item` FOREIGN KEY (`drug_inventory_id`) REFERENCES `drug` (`drug_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `encounter`
--

DROP TABLE IF EXISTS `encounter`;
CREATE TABLE `encounter` (
  `encounter_id` int(11) NOT NULL auto_increment,
  `encounter_type` int(11) default NULL,
  `patient_id` int(11) NOT NULL default '0',
  `provider_id` int(11) NOT NULL default '0',
  `location_id` int(11) NOT NULL default '0',
  `form_id` int(11) default NULL,
  `encounter_datetime` datetime NOT NULL default '0000-00-00 00:00:00',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`encounter_id`),
  KEY `encounter_location` (`location_id`),
  KEY `encounter_patient` (`patient_id`),
  KEY `encounter_provider` (`provider_id`),
  KEY `encounter_type_id` (`encounter_type`),
  KEY `encounter_creator` (`creator`),
  KEY `encounter_form` (`form_id`),
  KEY `user_who_voided_encounter` (`voided_by`),
  CONSTRAINT `encounter_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE,
  CONSTRAINT `encounter_form` FOREIGN KEY (`form_id`) REFERENCES `form` (`form_id`),
  CONSTRAINT `encounter_ibfk_1` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `encounter_location` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`),
  CONSTRAINT `encounter_provider` FOREIGN KEY (`provider_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `encounter_type_id` FOREIGN KEY (`encounter_type`) REFERENCES `encounter_type` (`encounter_type_id`),
  CONSTRAINT `user_who_voided_encounter` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `encounter_type`
--

DROP TABLE IF EXISTS `encounter_type`;
CREATE TABLE `encounter_type` (
  `encounter_type_id` int(11) NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `description` varchar(50) NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`encounter_type_id`),
  KEY `user_who_created_type` (`creator`),
  CONSTRAINT `user_who_created_type` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `encounter_type` DISABLE KEYS */;
LOCK TABLES `encounter_type` WRITE;
INSERT INTO `encounter_type` VALUES (1,'ADULTINITIAL','Outpatient Adult Initial Visit',1,'2005-02-24 00:00:00'),(2,'ADULTRETURN','Outpatient Adult Return Visit',1,'2005-02-24 00:00:00'),(3,'PEDSINITIAL','Outpatient Pediatric Initial Visit',1,'2005-02-24 00:00:00'),(4,'PEDSRETURN','Outpatient Pediatric Return Visit',1,'2005-02-24 00:00:00');
UNLOCK TABLES;
/*!40000 ALTER TABLE `encounter_type` ENABLE KEYS */;

--
-- Table structure for table `field`
--

DROP TABLE IF EXISTS `field`;
CREATE TABLE `field` (
  `field_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `description` text,
  `field_type` int(11) default NULL,
  `concept_id` int(11) default NULL,
  `table_name` varchar(50) default NULL,
  `attribute_name` varchar(50) default NULL,
  `default_value` text,
  `select_multiple` tinyint(1) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `field_answer`
--

DROP TABLE IF EXISTS `field_answer`;
CREATE TABLE `field_answer` (
  `field_id` int(11) NOT NULL default '0',
  `answer_id` int(11) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`field_id`,`answer_id`),
  KEY `answers_for_field` (`field_id`),
  KEY `field_answer_concept` (`answer_id`),
  KEY `user_who_created_field_answer` (`creator`),
  CONSTRAINT `answers_for_field` FOREIGN KEY (`field_id`) REFERENCES `field` (`field_id`),
  CONSTRAINT `field_answer_concept` FOREIGN KEY (`answer_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `user_who_created_field_answer` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `field_type`
--

DROP TABLE IF EXISTS `field_type`;
CREATE TABLE `field_type` (
  `field_type_id` int(11) NOT NULL auto_increment,
  `name` varchar(50) default NULL,
  `description` longtext,
  `is_set` tinyint(1) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`field_type_id`),
  KEY `user_who_created_field_type` (`creator`),
  CONSTRAINT `user_who_created_field_type` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `field_type` DISABLE KEYS */;
LOCK TABLES `field_type` WRITE;
INSERT INTO `field_type` VALUES (1,'Concept','',0,1,'2005-02-22 12:43:00'),(2,'Database element','',0,1,'2005-02-22 12:43:00'),(3,'Set of Concepts','',1,1,'2005-02-22 12:43:00'),(4,'Miscellaneous Set','',1,1,'2005-02-22 12:43:00'),(5,'Section','',1,1,'2005-02-22 12:43:00');
UNLOCK TABLES;
/*!40000 ALTER TABLE `field_type` ENABLE KEYS */;

--
-- Table structure for table `form`
--

DROP TABLE IF EXISTS `form`;
CREATE TABLE `form` (
  `form_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `version` varchar(50) NOT NULL default '',
  `build` int(11) default NULL,
  `published` tinyint(4) NOT NULL default '0',
  `description` text,
  `encounter_type` int(11) default NULL,
  `template` mediumtext,
  `xslt` mediumtext,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  `retired` tinyint(1) NOT NULL default '0',
  `retired_by` int(11) default NULL,
  `date_retired` datetime default NULL,
  `retired_reason` varchar(255) default NULL,
  PRIMARY KEY  (`form_id`),
  KEY `user_who_created_form` (`creator`),
  KEY `user_who_last_changed_form` (`changed_by`),
  KEY `user_who_retired_form` (`retired_by`),
  KEY `encounter_type` (`encounter_type`),
  CONSTRAINT `form_encounter_type` FOREIGN KEY (`encounter_type`) REFERENCES `encounter_type` (`encounter_type_id`),
  CONSTRAINT `user_who_created_form` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_last_changed_form` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_retired_form` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `form_field`
--

DROP TABLE IF EXISTS `form_field`;
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
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `sort_weight` float(11,5) default NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `formentry_archive`
--

DROP TABLE IF EXISTS `formentry_archive`;
CREATE TABLE `formentry_archive` (
  `formentry_archive_id` int(11) NOT NULL auto_increment,
  `form_data` mediumtext NOT NULL,
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `creator` int(11) NOT NULL default '0',
  PRIMARY KEY  (`formentry_archive_id`),
  KEY `User who created formentry_archive` (`creator`),
  CONSTRAINT `User who created formentry_archive` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `formentry_error`
--

DROP TABLE IF EXISTS `formentry_error`;
CREATE TABLE `formentry_error` (
  `formentry_error_id` int(11) NOT NULL auto_increment,
  `form_data` mediumtext NOT NULL,
  `error` varchar(255) NOT NULL default '',
  `error_details` text,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`formentry_error_id`),
  KEY `User who created formentry_error` (`creator`),
  CONSTRAINT `User who created formentry_error` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `formentry_queue`
--

DROP TABLE IF EXISTS `formentry_queue`;
CREATE TABLE `formentry_queue` (
  `formentry_queue_id` int(11) NOT NULL auto_increment,
  `form_data` mediumtext NOT NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`formentry_queue_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `global_property`
--

DROP TABLE IF EXISTS `global_property`;
CREATE TABLE `global_property` (
  `property` varchar(255) NOT NULL default '',
  `property_value` text,
  `description` text,
  PRIMARY KEY  (`property`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `global_property` DISABLE KEYS */;
LOCK TABLES `global_property` WRITE;
INSERT INTO `global_property` VALUES ('concept.causeOfDeath','1',NULL),('concept.reasonOrderStopped','4',NULL),('database_version','1.2.0',NULL),('patient.displayAttributeTypes','Birthplace',NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `global_property` ENABLE KEYS */;

--
-- Table structure for table `hl7_in_archive`
--

DROP TABLE IF EXISTS `hl7_in_archive`;
CREATE TABLE `hl7_in_archive` (
  `hl7_in_archive_id` int(11) NOT NULL auto_increment,
  `hl7_source` int(11) NOT NULL default '0',
  `hl7_source_key` varchar(255) default NULL,
  `hl7_data` mediumtext NOT NULL,
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`hl7_in_archive_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `hl7_in_error`
--

DROP TABLE IF EXISTS `hl7_in_error`;
CREATE TABLE `hl7_in_error` (
  `hl7_in_error_id` int(11) NOT NULL auto_increment,
  `hl7_source` int(11) NOT NULL default '0',
  `hl7_source_key` text,
  `hl7_data` mediumtext NOT NULL,
  `error` varchar(255) NOT NULL default '',
  `error_details` text,
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`hl7_in_error_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `hl7_in_queue`
--

DROP TABLE IF EXISTS `hl7_in_queue`;
CREATE TABLE `hl7_in_queue` (
  `hl7_in_queue_id` int(11) NOT NULL auto_increment,
  `hl7_source` int(11) NOT NULL default '0',
  `hl7_source_key` text,
  `hl7_data` mediumtext NOT NULL,
  `state` int(11) NOT NULL default '0' COMMENT '0=pending, 1=processing, 2=processed, 3=error',
  `date_processed` datetime default NULL,
  `error_msg` text,
  `date_created` datetime default NULL,
  PRIMARY KEY  (`hl7_in_queue_id`),
  KEY `hl7_source` (`hl7_source`),
  CONSTRAINT `hl7_source` FOREIGN KEY (`hl7_source`) REFERENCES `hl7_source` (`hl7_source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `hl7_source`
--

DROP TABLE IF EXISTS `hl7_source`;
CREATE TABLE `hl7_source` (
  `hl7_source_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `description` tinytext,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`hl7_source_id`),
  KEY `creator` (`creator`),
  CONSTRAINT `creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `hl7_source` DISABLE KEYS */;
LOCK TABLES `hl7_source` WRITE;
INSERT INTO `hl7_source` VALUES (1,'LOCAL','',1,'2006-09-01 09:00:00');
UNLOCK TABLES;
/*!40000 ALTER TABLE `hl7_source` ENABLE KEYS */;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
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
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `county_district` varchar(50) default NULL,
  `neighborhood_cell` varchar(50) default NULL,
  `region` varchar(50) default NULL,
  `subregion` varchar(50) default NULL,
  `township_division` varchar(50) default NULL,
  PRIMARY KEY  (`location_id`),
  KEY `user_who_created_location` (`creator`),
  KEY `name_of_location` (`name`),
  CONSTRAINT `user_who_created_location` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `location` DISABLE KEYS */;
LOCK TABLES `location` WRITE;
INSERT INTO `location` VALUES (1,'Unknown Location',NULL,'','','','','','',NULL,NULL,1,'2005-09-22 00:00:00',NULL,NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `location` ENABLE KEYS */;

--
-- Table structure for table `mime_type`
--

DROP TABLE IF EXISTS `mime_type`;
CREATE TABLE `mime_type` (
  `mime_type_id` int(11) NOT NULL auto_increment,
  `mime_type` varchar(75) NOT NULL default '',
  `description` text,
  PRIMARY KEY  (`mime_type_id`),
  KEY `mime_type_id` (`mime_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `note`
--

DROP TABLE IF EXISTS `note`;
CREATE TABLE `note` (
  `note_id` int(11) NOT NULL default '0',
  `note_type` varchar(50) default NULL,
  `patient_id` int(11) default NULL,
  `obs_id` int(11) default NULL,
  `encounter_id` int(11) default NULL,
  `text` text NOT NULL,
  `priority` int(11) default NULL,
  `parent` int(11) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  PRIMARY KEY  (`note_id`),
  KEY `patient_note` (`patient_id`),
  KEY `obs_note` (`obs_id`),
  KEY `encounter_note` (`encounter_id`),
  KEY `user_who_created_note` (`creator`),
  KEY `user_who_changed_note` (`changed_by`),
  KEY `note_hierarchy` (`parent`),
  CONSTRAINT `patient_note` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE,
  CONSTRAINT `encounter_note` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
  CONSTRAINT `note_hierarchy` FOREIGN KEY (`parent`) REFERENCES `note` (`note_id`),
  CONSTRAINT `obs_note` FOREIGN KEY (`obs_id`) REFERENCES `obs` (`obs_id`),
  CONSTRAINT `user_who_changed_note` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_created_note` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `notification_alert`
--

DROP TABLE IF EXISTS `notification_alert`;
CREATE TABLE `notification_alert` (
  `alert_id` int(11) NOT NULL auto_increment,
  `user_id` int(11) default NULL,
  `text` varchar(512) NOT NULL,
  `satisfied_by_any` int(1) NOT NULL default '0',
  `alert_read` int(1) NOT NULL default '0',
  `date_to_expire` datetime default NULL,
  `creator` int(11) NOT NULL,
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  PRIMARY KEY  (`alert_id`),
  KEY `alert_creator` (`creator`),
  KEY `alert_assigned_to_user` (`user_id`),
  KEY `user_who_changed_alert` (`changed_by`),
  CONSTRAINT `alert_assigned_to_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `alert_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_changed_alert` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `notification_alert_recipient`
--

DROP TABLE IF EXISTS `notification_alert_recipient`;
CREATE TABLE `notification_alert_recipient` (
  `alert_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `alert_read` int(1) NOT NULL default '0',
  `date_changed` timestamp NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`alert_id`,`user_id`),
  KEY `alert_read_by_user` (`user_id`),
  KEY `id_of_alert` (`alert_id`),
  CONSTRAINT `id_of_alert` FOREIGN KEY (`alert_id`) REFERENCES `notification_alert` (`alert_id`),
  CONSTRAINT `alert_read_by_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `notification_template`
--

DROP TABLE IF EXISTS `notification_template`;
CREATE TABLE `notification_template` (
  `template_id` int(11) NOT NULL auto_increment,
  `name` varchar(50) default NULL,
  `template` text,
  `subject` varchar(100) default NULL,
  `sender` varchar(255) default NULL,
  `recipients` varchar(512) default NULL,
  `ordinal` int(11) default '0',
  PRIMARY KEY  (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `obs`
--

DROP TABLE IF EXISTS `obs`;
CREATE TABLE `obs` (
  `obs_id` int(11) NOT NULL auto_increment,
  `person_id` int(11) NOT NULL,
  `concept_id` int(11) NOT NULL default '0',
  `encounter_id` int(11) default NULL,
  `order_id` int(11) default NULL,
  `obs_datetime` datetime NOT NULL default '0000-00-00 00:00:00',
  `location_id` int(11) NOT NULL default '0',
  `obs_group_id` int(11) default NULL,
  `accession_number` varchar(255) default NULL,
  `value_group_id` int(11) default NULL,
  `value_boolean` tinyint(1) default NULL,
  `value_coded` int(11) default NULL,
  `value_drug` int(11) default NULL,
  `value_datetime` datetime default NULL,
  `value_numeric` double default NULL,
  `value_modifier` varchar(2) default NULL,
  `value_text` text,
  `date_started` datetime default NULL,
  `date_stopped` datetime default NULL,
  `comments` varchar(255) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
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
  KEY `patient_obs` (`person_id`),
  KEY `user_who_voided_obs` (`voided_by`),
  KEY `answer_concept_drug` (`value_drug`),
  CONSTRAINT `person_obs` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
  CONSTRAINT `answer_concept` FOREIGN KEY (`value_coded`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `answer_concept_drug` FOREIGN KEY (`value_drug`) REFERENCES `drug` (`drug_id`),
  CONSTRAINT `encounter_observations` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
  CONSTRAINT `obs_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `obs_enterer` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `obs_location` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`),
  CONSTRAINT `obs_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `user_who_voided_obs` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `order_type`
--

DROP TABLE IF EXISTS `order_type`;
CREATE TABLE `order_type` (
  `order_type_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `description` varchar(255) NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`order_type_id`),
  KEY `type_created_by` (`creator`),
  CONSTRAINT `type_created_by` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
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
  `discontinued_reason` int(11) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  `patient_id` int(11) NOT NULL,
  PRIMARY KEY  (`order_id`),
  KEY `order_creator` (`creator`),
  KEY `orderer_not_drug` (`orderer`),
  KEY `orders_in_encounter` (`encounter_id`),
  KEY `type_of_order` (`order_type_id`),
  KEY `user_who_discontinued_order` (`discontinued_by`),
  KEY `user_who_voided_order` (`voided_by`),
  KEY `discontinued_because` (`discontinued_reason`),
  KEY `order_for_patient` (`patient_id`),
  CONSTRAINT `order_for_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE,
  CONSTRAINT `discontinued_because` FOREIGN KEY (`discontinued_reason`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `orderer_not_drug` FOREIGN KEY (`orderer`) REFERENCES `users` (`user_id`),
  CONSTRAINT `orders_in_encounter` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
  CONSTRAINT `order_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `type_of_order` FOREIGN KEY (`order_type_id`) REFERENCES `order_type` (`order_type_id`),
  CONSTRAINT `user_who_discontinued_order` FOREIGN KEY (`discontinued_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_order` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `patient`
--

DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient` (
  `patient_id` int(11) NOT NULL auto_increment,
  `tribe` int(11) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
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
  CONSTRAINT `person_id_for_patient` FOREIGN KEY (`patient_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
  CONSTRAINT `user_who_changed_pat` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_created_patient` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_patient` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `patient_identifier`
--

DROP TABLE IF EXISTS `patient_identifier`;
CREATE TABLE `patient_identifier` (
  `patient_id` int(11) NOT NULL default '0',
  `identifier` varchar(50) NOT NULL default '',
  `identifier_type` int(11) NOT NULL default '0',
  `preferred` tinyint(4) NOT NULL default '0',
  `location_id` int(11) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`patient_id`,`identifier`,`identifier_type`),
  KEY `defines_identifier_type` (`identifier_type`),
  KEY `identifier_creator` (`creator`),
  KEY `identifier_voider` (`voided_by`),
  KEY `identifier_location` (`location_id`),
  KEY `identifier_name` (`identifier`),
  CONSTRAINT `identifies_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE,
  CONSTRAINT `defines_identifier_type` FOREIGN KEY (`identifier_type`) REFERENCES `patient_identifier_type` (`patient_identifier_type_id`),
  CONSTRAINT `identifier_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `identifier_voider` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `patient_identifier_ibfk_2` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `patient_identifier_type`
--

DROP TABLE IF EXISTS `patient_identifier_type`;
CREATE TABLE `patient_identifier_type` (
  `patient_identifier_type_id` int(11) NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `description` text NOT NULL,
  `format` varchar(50) default NULL,
  `check_digit` tinyint(1) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `required` tinyint(1) NOT NULL default '0',
  `format_description` varchar(255) default NULL,
  PRIMARY KEY  (`patient_identifier_type_id`),
  KEY `type_creator` (`creator`),
  CONSTRAINT `type_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `patient_identifier_type` DISABLE KEYS */;
LOCK TABLES `patient_identifier_type` WRITE;
INSERT INTO `patient_identifier_type` VALUES (1,'OpenMRS Identification Number','Unique number used in OpenMRS','',1,1,'2005-09-22 00:00:00',0,NULL),(2,'Old Identification Number','Number given out prior to the OpenMRS system (No check digit)','',0,1,'2005-09-22 00:00:00',0,NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `patient_identifier_type` ENABLE KEYS */;

--
-- Table structure for table `patient_program`
--

DROP TABLE IF EXISTS `patient_program`;
CREATE TABLE `patient_program` (
  `patient_program_id` int(11) NOT NULL auto_increment,
  `patient_id` int(11) NOT NULL default '0',
  `program_id` int(11) NOT NULL default '0',
  `date_enrolled` datetime default NULL,
  `date_completed` datetime default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`patient_program_id`),
  KEY `patient_in_program` (`patient_id`),
  KEY `program_for_patient` (`program_id`),
  KEY `patient_program_creator` (`creator`),
  KEY `user_who_changed` (`changed_by`),
  KEY `user_who_voided_patient_program` (`voided_by`),
  CONSTRAINT `patient_in_program` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE,
  CONSTRAINT `patient_program_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `program_for_patient` FOREIGN KEY (`program_id`) REFERENCES `program` (`program_id`),
  CONSTRAINT `user_who_changed` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_patient_program` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `patient_state`
--

DROP TABLE IF EXISTS `patient_state`;
CREATE TABLE `patient_state` (
  `patient_state_id` int(11) NOT NULL auto_increment,
  `patient_program_id` int(11) NOT NULL default '0',
  `state` int(11) NOT NULL default '0',
  `start_date` date default NULL,
  `end_date` date default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`patient_state_id`),
  KEY `state_for_patient` (`state`),
  KEY `patient_program_for_state` (`patient_program_id`),
  KEY `patient_state_creator` (`creator`),
  KEY `patient_state_changer` (`changed_by`),
  KEY `patient_state_voider` (`voided_by`),
  CONSTRAINT `state_for_patient` FOREIGN KEY (`state`) REFERENCES `program_workflow_state` (`program_workflow_state_id`),
  CONSTRAINT `patient_program_for_state` FOREIGN KEY (`patient_program_id`) REFERENCES `patient_program` (`patient_program_id`),
  CONSTRAINT `patient_state_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `patient_state_changer` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `patient_state_voider` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `person`
--

DROP TABLE IF EXISTS `person`;
CREATE TABLE `person` (
  `person_id` int(11) NOT NULL auto_increment,
  `gender` varchar(50) default '',
  `birthdate` date default NULL,
  `birthdate_estimated` tinyint(1) default NULL,
  `dead` int(1) NOT NULL default '0',
  `death_date` datetime default NULL,
  `cause_of_death` int(11) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`person_id`),
  KEY `user_who_created_patient` (`creator`),
  KEY `user_who_voided_patient` (`voided_by`),
  KEY `user_who_changed_pat` (`changed_by`),
  KEY `person_birthdate` (`birthdate`),
  KEY `person_death_date` (`death_date`),
  KEY `person_died_because` (`cause_of_death`),
  CONSTRAINT `person_died_because` FOREIGN KEY (`cause_of_death`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `user_who_changed_person` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_created_person` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_person` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `person` DISABLE KEYS */;
LOCK TABLES `person` WRITE;
INSERT INTO `person` VALUES (1,'',NULL,NULL,0,NULL,NULL,1,'2005-01-01 00:00:00',NULL,NULL,0,NULL,NULL,NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `person` ENABLE KEYS */;

--
-- Table structure for table `person_address`
--

DROP TABLE IF EXISTS `person_address`;
CREATE TABLE `person_address` (
  `person_address_id` int(11) NOT NULL auto_increment,
  `person_id` int(11) default NULL,
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
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  `county_district` varchar(50) default NULL,
  `neighborhood_cell` varchar(50) default NULL,
  `region` varchar(50) default NULL,
  `subregion` varchar(50) default NULL,
  `township_division` varchar(50) default NULL,
  PRIMARY KEY  (`person_address_id`),
  KEY `patient_address_creator` (`creator`),
  KEY `patient_addresses` (`person_id`),
  KEY `patient_address_void` (`voided_by`),
  CONSTRAINT `address_for_person` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
  CONSTRAINT `patient_address_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `patient_address_void` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `person_attribute`
--

DROP TABLE IF EXISTS `person_attribute`;
CREATE TABLE `person_attribute` (
  `person_attribute_id` int(11) NOT NULL auto_increment,
  `person_id` int(11) NOT NULL default '0',
  `value` varchar(50) NOT NULL default '',
  `person_attribute_type_id` int(11) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`person_attribute_id`),
  KEY `identifies_person` (`person_id`),
  KEY `defines_attribute_type` (`person_attribute_type_id`),
  KEY `attribute_creator` (`creator`),
  KEY `attribute_changer` (`changed_by`),
  KEY `attribute_voider` (`voided_by`),
  CONSTRAINT `defines_attribute_type` FOREIGN KEY (`person_attribute_type_id`) REFERENCES `person_attribute_type` (`person_attribute_type_id`),
  CONSTRAINT `attribute_voider` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `attribute_changer` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `attribute_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `identifies_person` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `person_attribute_type`
--

DROP TABLE IF EXISTS `person_attribute_type`;
CREATE TABLE `person_attribute_type` (
  `person_attribute_type_id` int(11) NOT NULL auto_increment,
  `name` varchar(50) NOT NULL default '',
  `description` text NOT NULL,
  `format` varchar(50) default NULL,
  `foreign_key` int(11) default NULL,
  `searchable` int(1) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  PRIMARY KEY  (`person_attribute_type_id`),
  KEY `name_of_attribute` (`name`),
  KEY `type_creator` (`creator`),
  KEY `attribute_type_changer` (`changed_by`),
  KEY `attribute_is_searchable` (`searchable`),
  CONSTRAINT `attribute_type_changer` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `attribute_type_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `person_attribute_type` DISABLE KEYS */;
LOCK TABLES `person_attribute_type` WRITE;
INSERT INTO `person_attribute_type` VALUES (1,'Race','Group of persons related by common descent or heredity','java.lang.String',NULL,0,1,'2007-05-04 09:59:23',NULL,NULL),(2,'Birthplace','Location of persons birth','java.lang.String',NULL,0,1,'2007-05-04 09:59:23',NULL,NULL),(3,'Citizenship','Country of which this person is a member','java.lang.String',NULL,0,1,'2007-05-04 09:59:23',NULL,NULL),(4,'Mother\'s Name','First or last name of this person\'s mother','java.lang.String',NULL,0,1,'2007-05-04 09:59:23',NULL,NULL),(5,'Civil Status','Marriage status of this person','org.openmrs.Concept',NULL,0,1,'2007-05-04 09:59:23',NULL,NULL),(6,'Health District','District/region in which this patient\' home health center resides','java.lang.String',NULL,0,1,'2007-05-04 09:59:23',NULL,NULL),(7,'Health Center','Specific Location of this person\'s home health center.','org.openmrs.Location',NULL,0,1,'2007-05-04 09:59:23',NULL,NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `person_attribute_type` ENABLE KEYS */;

--
-- Table structure for table `person_name`
--

DROP TABLE IF EXISTS `person_name`;
CREATE TABLE `person_name` (
  `person_name_id` int(11) NOT NULL auto_increment,
  `preferred` tinyint(1) NOT NULL default '0',
  `person_id` int(11) default NULL,
  `prefix` varchar(50) default NULL,
  `given_name` varchar(50) default NULL,
  `middle_name` varchar(50) default NULL,
  `family_name_prefix` varchar(50) default NULL,
  `family_name` varchar(50) default NULL,
  `family_name2` varchar(50) default NULL,
  `family_name_suffix` varchar(50) default NULL,
  `degree` varchar(50) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  PRIMARY KEY  (`person_name_id`),
  KEY `name_for_patient` (`person_id`),
  KEY `user_who_made_name` (`creator`),
  KEY `user_who_voided_name` (`voided_by`),
  KEY `first_name` (`given_name`),
  KEY `middle_name` (`middle_name`),
  KEY `last_name` (`family_name`),
  CONSTRAINT `name for person` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
  CONSTRAINT `user_who_made_name` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_name` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `person_name` DISABLE KEYS */;
LOCK TABLES `person_name` WRITE;
INSERT INTO `person_name` VALUES (1,1,1,NULL,'Super','',NULL,'User',NULL,NULL,NULL,1,'2005-01-01 00:00:00',0,NULL,NULL,NULL,NULL,NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `person_name` ENABLE KEYS */;

--
-- Table structure for table `privilege`
--

DROP TABLE IF EXISTS `privilege`;
CREATE TABLE `privilege` (
  `privilege` varchar(50) NOT NULL default '',
  `description` varchar(250) NOT NULL default '',
  PRIMARY KEY  (`privilege`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `program`
--

DROP TABLE IF EXISTS `program`;
CREATE TABLE `program` (
  `program_id` int(11) NOT NULL auto_increment,
  `concept_id` int(11) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`program_id`),
  KEY `program_concept` (`concept_id`),
  KEY `program_creator` (`creator`),
  KEY `user_who_changed_program` (`changed_by`),
  KEY `user_who_voided_program` (`voided_by`),
  CONSTRAINT `user_who_voided_program` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `program_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `program_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_changed_program` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `program_workflow`
--

DROP TABLE IF EXISTS `program_workflow`;
CREATE TABLE `program_workflow` (
  `program_workflow_id` int(11) NOT NULL auto_increment,
  `program_id` int(11) NOT NULL default '0',
  `concept_id` int(11) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `voided` tinyint(1) default NULL,
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`program_workflow_id`),
  KEY `program_for_workflow` (`program_id`),
  KEY `workflow_concept` (`concept_id`),
  KEY `workflow_creator` (`creator`),
  KEY `workflow_voided_by` (`voided_by`),
  CONSTRAINT `program_for_workflow` FOREIGN KEY (`program_id`) REFERENCES `program` (`program_id`),
  CONSTRAINT `workflow_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `workflow_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `workflow_voided_by` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `program_workflow_state`
--

DROP TABLE IF EXISTS `program_workflow_state`;
CREATE TABLE `program_workflow_state` (
  `program_workflow_state_id` int(11) NOT NULL auto_increment,
  `program_workflow_id` int(11) NOT NULL default '0',
  `concept_id` int(11) NOT NULL default '0',
  `initial` tinyint(1) NOT NULL default '0',
  `terminal` tinyint(1) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `voided` tinyint(1) default NULL,
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`program_workflow_state_id`),
  KEY `workflow_for_state` (`program_workflow_id`),
  KEY `state_concept` (`concept_id`),
  KEY `state_creator` (`creator`),
  KEY `state_voided_by` (`voided_by`),
  CONSTRAINT `workflow_for_state` FOREIGN KEY (`program_workflow_id`) REFERENCES `program_workflow` (`program_workflow_id`),
  CONSTRAINT `state_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `state_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `state_voided_by` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `relationship`
--

DROP TABLE IF EXISTS `relationship`;
CREATE TABLE `relationship` (
  `relationship_id` int(11) NOT NULL auto_increment,
  `person_a` int(11) NOT NULL,
  `relationship` int(11) NOT NULL default '0',
  `person_b` int(11) NOT NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `voided` tinyint(1) NOT NULL default '0',
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`relationship_id`),
  KEY `related_person` (`person_a`),
  KEY `related_relative` (`person_b`),
  KEY `relationship_type` (`relationship`),
  KEY `relation_creator` (`creator`),
  KEY `relation_voider` (`voided_by`),
  CONSTRAINT `person_b` FOREIGN KEY (`person_b`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
  CONSTRAINT `person_a` FOREIGN KEY (`person_a`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
  CONSTRAINT `relationship_type_id` FOREIGN KEY (`relationship`) REFERENCES `relationship_type` (`relationship_type_id`),
  CONSTRAINT `relation_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `relation_voider` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `relationship_type`
--

DROP TABLE IF EXISTS `relationship_type`;
CREATE TABLE `relationship_type` (
  `relationship_type_id` int(11) NOT NULL auto_increment,
  `a_is_to_b` varchar(50) NOT NULL,
  `b_is_to_a` varchar(50) NOT NULL,
  `preferred` int(1) NOT NULL default '0',
  `weight` int(11) NOT NULL default '0',
  `description` varchar(255) NOT NULL default '',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`relationship_type_id`),
  KEY `user_who_created_rel` (`creator`),
  CONSTRAINT `user_who_created_rel` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `relationship_type` DISABLE KEYS */;
LOCK TABLES `relationship_type` WRITE;
INSERT INTO `relationship_type` VALUES (1,'Doctor','Patient',0,0,'Relationship from a primary care provider to the patient',1,'2007-05-04 09:59:22'),(2,'Sibling','Sibling',0,0,'Relationship between brother/sister, brother/brother, and sister/sister',1,'2007-05-04 09:59:22'),(3,'Parent','Child',0,0,'Relationship from a mother/father to the child',1,'2007-05-04 09:59:22'),(4,'Aunt/Uncle','Niece/Nephew',0,0,'',1,'2007-05-04 09:59:22');
UNLOCK TABLES;
/*!40000 ALTER TABLE `relationship_type` ENABLE KEYS */;

--
-- Table structure for table `report`
--

DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
  `report_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `description` text,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `report_object`
--

DROP TABLE IF EXISTS `report_object`;
CREATE TABLE `report_object` (
  `report_object_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `description` varchar(1000) default NULL,
  `report_object_type` varchar(255) NOT NULL,
  `report_object_sub_type` varchar(255) NOT NULL,
  `xml_data` text,
  `creator` int(11) NOT NULL,
  `date_created` datetime NOT NULL,
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  `voided` tinyint(1) NOT NULL,
  `voided_by` int(11) default NULL,
  `date_voided` datetime default NULL,
  `void_reason` varchar(255) default NULL,
  PRIMARY KEY  (`report_object_id`),
  KEY `report_object_creator` (`creator`),
  KEY `user_who_changed_report_object` (`changed_by`),
  KEY `user_who_voided_report_object` (`voided_by`),
  CONSTRAINT `report_object_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_changed_report_object` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_report_object` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `role` varchar(50) NOT NULL default '',
  `description` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `role` DISABLE KEYS */;
LOCK TABLES `role` WRITE;
INSERT INTO `role` VALUES ('System Developer','Developers of the OpenMRS .. have additional access to change fundamental structure of the database model.');
UNLOCK TABLES;
/*!40000 ALTER TABLE `role` ENABLE KEYS */;

--
-- Table structure for table `role_privilege`
--

DROP TABLE IF EXISTS `role_privilege`;
CREATE TABLE `role_privilege` (
  `role` varchar(50) NOT NULL default '',
  `privilege` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`privilege`,`role`),
  KEY `role_privilege` (`role`),
  CONSTRAINT `privilege_definitons` FOREIGN KEY (`privilege`) REFERENCES `privilege` (`privilege`),
  CONSTRAINT `role_privilege` FOREIGN KEY (`role`) REFERENCES `role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `role_role`
--

DROP TABLE IF EXISTS `role_role`;
CREATE TABLE `role_role` (
  `parent_role` varchar(50) NOT NULL default '',
  `child_role` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`parent_role`,`child_role`),
  KEY `inherited_role` (`child_role`),
  CONSTRAINT `inherited_role` FOREIGN KEY (`child_role`) REFERENCES `role` (`role`),
  CONSTRAINT `parent_role` FOREIGN KEY (`parent_role`) REFERENCES `role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `scheduler_task_config`
--

DROP TABLE IF EXISTS `scheduler_task_config`;
CREATE TABLE `scheduler_task_config` (
  `task_config_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `description` varchar(1024) default NULL,
  `schedulable_class` text,
  `start_time` datetime NOT NULL,
  `start_time_pattern` varchar(50) default NULL,
  `repeat_interval` int(11) NOT NULL default '0',
  `start_on_startup` int(1) NOT NULL default '0',
  `started` int(1) NOT NULL default '0',
  `created_by` int(11) default '0',
  `date_created` datetime default '2005-01-01 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  PRIMARY KEY  (`task_config_id`),
  KEY `schedule_creator` (`created_by`),
  KEY `schedule_changer` (`changed_by`),
  CONSTRAINT `scheduler_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `scheduler_changer` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `scheduler_task_config` DISABLE KEYS */;
LOCK TABLES `scheduler_task_config` WRITE;
INSERT INTO `scheduler_task_config` VALUES (1,'Process Form Entry Queue','Processes form entry queue.','org.openmrs.scheduler.tasks.ProcessFormEntryQueueTask','2006-04-24 00:00:00',NULL,30,0,0,1,'2006-04-24 00:00:00',NULL,NULL),(2,'Process HL7 Task','Processes HL7 messages.','org.openmrs.scheduler.tasks.ProcessHL7InQueueTask','2006-04-24 00:00:00',NULL,30,0,0,1,'2006-04-24 00:00:00',NULL,NULL),(3,'Alert Reminder Task','Sends email to users who have not checked their alerts.  Set to run every ten minutes.','org.openmrs.scheduler.tasks.AlertReminderTask','2006-04-24 00:00:00',NULL,600,0,0,1,'2006-04-24 00:00:00',NULL,NULL),(4,'Send Email Task','Doesn\'t do anything yet.','org.openmrs.scheduler.tasks.SendEmailTask','2006-04-24 00:00:00',NULL,600,0,0,1,'2006-04-24 00:00:00',NULL,NULL),(5,'Hello World Task','Writes \'hello world\' to log.  Demonstrates problem caused by spawning a thread from a timer task.','org.openmrs.scheduler.tasks.HelloWorldTask','2006-04-24 00:00:00',NULL,600,0,0,1,'2006-04-24 00:00:00',NULL,NULL),(6,'Check Internet Connectivity Task','Checks the external internet connection every ten minutes.  This is a trivial task that checks the connection to Google over port 80.  If the connection fails, we assume the internet is done and raise an alert.','org.openmrs.scheduler.tasks.CheckInternetConnectivityTask','2006-04-24 00:00:00',NULL,60,0,0,1,'2006-04-24 00:00:00',NULL,NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `scheduler_task_config` ENABLE KEYS */;

--
-- Table structure for table `scheduler_task_config_property`
--

DROP TABLE IF EXISTS `scheduler_task_config_property`;
CREATE TABLE `scheduler_task_config_property` (
  `task_config_property_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `value` text,
  `task_config_id` int(11) default NULL,
  PRIMARY KEY  (`task_config_property_id`),
  KEY `task_config` (`task_config_id`),
  CONSTRAINT `task_config_for_property` FOREIGN KEY (`task_config_id`) REFERENCES `scheduler_task_config` (`task_config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `tribe`
--

DROP TABLE IF EXISTS `tribe`;
CREATE TABLE `tribe` (
  `tribe_id` int(11) NOT NULL auto_increment,
  `retired` tinyint(1) NOT NULL default '0',
  `name` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`tribe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `user_property`
--

DROP TABLE IF EXISTS `user_property`;
CREATE TABLE `user_property` (
  `user_id` int(11) NOT NULL default '0',
  `property` varchar(100) NOT NULL default '',
  `property_value` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`user_id`,`property`),
  CONSTRAINT `user_property` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `user_id` int(11) NOT NULL default '0',
  `role` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`role`,`user_id`),
  KEY `user_role` (`user_id`),
  CONSTRAINT `role_definitions` FOREIGN KEY (`role`) REFERENCES `role` (`role`),
  CONSTRAINT `user_role` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
LOCK TABLES `user_role` WRITE;
INSERT INTO `user_role` VALUES (1,'System Developer');
UNLOCK TABLES;
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` int(11) NOT NULL auto_increment,
  `system_id` varchar(50) NOT NULL default '',
  `username` varchar(50) default NULL,
  `password` varchar(50) default NULL,
  `salt` varchar(50) default NULL,
  `secret_question` varchar(255) default NULL,
  `secret_answer` varchar(255) default NULL,
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
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
  CONSTRAINT `person_id_for_user` FOREIGN KEY (`user_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
  CONSTRAINT `user_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_changed_user` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_user` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40000 ALTER TABLE `users` DISABLE KEYS */;
LOCK TABLES `users` WRITE;
INSERT INTO `users` VALUES (1,'admin','','4a1750c8607dfa237de36c6305715c223415189','c788c6ad82a157b712392ca695dfcf2eed193d7f',NULL,NULL,1,'2005-01-01 00:00:00',NULL,NULL,0,NULL,NULL,NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

