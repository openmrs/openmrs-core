-- MySQL dump 10.19  Distrib 10.3.37-MariaDB, for debian-linux-gnu (aarch64)
--
-- Host: localhost    Database: openmrs
-- ------------------------------------------------------
-- Server version	10.3.37-MariaDB-1:10.3.37+maria~ubu2004

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `allergy`
--

DROP TABLE IF EXISTS `allergy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `allergy` (
						   `allergy_id` int(11) NOT NULL AUTO_INCREMENT,
						   `patient_id` int(11) NOT NULL,
						   `severity_concept_id` int(11) DEFAULT NULL,
						   `coded_allergen` int(11) NOT NULL,
						   `non_coded_allergen` varchar(255) DEFAULT NULL,
						   `allergen_type` varchar(50) NOT NULL,
						   `comments` varchar(1024) DEFAULT NULL,
						   `creator` int(11) NOT NULL,
						   `date_created` datetime NOT NULL,
						   `changed_by` int(11) DEFAULT NULL,
						   `date_changed` datetime DEFAULT NULL,
						   `voided` tinyint(1) NOT NULL DEFAULT 1,
						   `voided_by` int(11) DEFAULT NULL,
						   `date_voided` datetime DEFAULT NULL,
						   `void_reason` varchar(255) DEFAULT NULL,
						   `uuid` char(38) DEFAULT NULL,
						   `form_namespace_and_path` varchar(255) DEFAULT NULL,
						   `encounter_id` int(11) DEFAULT NULL,
						   PRIMARY KEY (`allergy_id`),
						   KEY `allergy_changed_by_fk` (`changed_by`),
						   KEY `allergy_coded_allergen_fk` (`coded_allergen`),
						   KEY `allergy_creator_fk` (`creator`),
						   KEY `allergy_patient_id_fk` (`patient_id`),
						   KEY `allergy_severity_concept_id_fk` (`severity_concept_id`),
						   KEY `allergy_voided_by_fk` (`voided_by`),
						   KEY `allergy_encounter_id_fk` (`encounter_id`),
						   CONSTRAINT `allergy_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
						   CONSTRAINT `allergy_coded_allergen_fk` FOREIGN KEY (`coded_allergen`) REFERENCES `concept` (`concept_id`),
						   CONSTRAINT `allergy_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
						   CONSTRAINT `allergy_encounter_id_fk` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
						   CONSTRAINT `allergy_patient_id_fk` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
						   CONSTRAINT `allergy_severity_concept_id_fk` FOREIGN KEY (`severity_concept_id`) REFERENCES `concept` (`concept_id`),
						   CONSTRAINT `allergy_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `allergy`
--

LOCK TABLES `allergy` WRITE;
/*!40000 ALTER TABLE `allergy` DISABLE KEYS */;
/*!40000 ALTER TABLE `allergy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `allergy_reaction`
--

DROP TABLE IF EXISTS `allergy_reaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `allergy_reaction` (
									`allergy_reaction_id` int(11) NOT NULL AUTO_INCREMENT,
									`allergy_id` int(11) NOT NULL,
									`reaction_concept_id` int(11) NOT NULL,
									`reaction_non_coded` varchar(255) DEFAULT NULL,
									`uuid` char(38) DEFAULT NULL,
									PRIMARY KEY (`allergy_reaction_id`),
									KEY `allergy_reaction_allergy_id_fk` (`allergy_id`),
									KEY `allergy_reaction_reaction_concept_id_fk` (`reaction_concept_id`),
									CONSTRAINT `allergy_reaction_allergy_id_fk` FOREIGN KEY (`allergy_id`) REFERENCES `allergy` (`allergy_id`),
									CONSTRAINT `allergy_reaction_reaction_concept_id_fk` FOREIGN KEY (`reaction_concept_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `allergy_reaction`
--

LOCK TABLES `allergy_reaction` WRITE;
/*!40000 ALTER TABLE `allergy_reaction` DISABLE KEYS */;
/*!40000 ALTER TABLE `allergy_reaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `care_setting`
--

DROP TABLE IF EXISTS `care_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `care_setting` (
								`care_setting_id` int(11) NOT NULL AUTO_INCREMENT,
								`name` varchar(255) NOT NULL,
								`description` varchar(255) DEFAULT NULL,
								`care_setting_type` varchar(50) NOT NULL,
								`creator` int(11) NOT NULL,
								`date_created` datetime NOT NULL,
								`retired` tinyint(1) NOT NULL DEFAULT 0,
								`retired_by` int(11) DEFAULT NULL,
								`date_retired` datetime DEFAULT NULL,
								`retire_reason` varchar(255) DEFAULT NULL,
								`changed_by` int(11) DEFAULT NULL,
								`date_changed` datetime DEFAULT NULL,
								`uuid` char(38) NOT NULL,
								PRIMARY KEY (`care_setting_id`),
								UNIQUE KEY `care_setting_name` (`name`),
								UNIQUE KEY `uuid_care_setting` (`uuid`),
								KEY `care_setting_changed_by` (`changed_by`),
								KEY `care_setting_creator` (`creator`),
								KEY `care_setting_retired_by` (`retired_by`),
								CONSTRAINT `care_setting_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
								CONSTRAINT `care_setting_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								CONSTRAINT `care_setting_retired_by` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `care_setting`
--

LOCK TABLES `care_setting` WRITE;
/*!40000 ALTER TABLE `care_setting` DISABLE KEYS */;
INSERT INTO `care_setting` VALUES (1,'Outpatient','Out-patient care setting','OUTPATIENT',1,'2013-12-27 00:00:00',0,NULL,NULL,NULL,NULL,NULL,'6f0c9a92-6f24-11e3-af88-005056821db0'),(2,'Inpatient','In-patient care setting','INPATIENT',1,'2013-12-27 00:00:00',0,NULL,NULL,NULL,NULL,NULL,'c365e560-c3ec-11e3-9c1a-0800200c9a66');
/*!40000 ALTER TABLE `care_setting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clob_datatype_storage`
--

DROP TABLE IF EXISTS `clob_datatype_storage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clob_datatype_storage` (
										 `id` int(11) NOT NULL AUTO_INCREMENT,
										 `uuid` char(38) NOT NULL,
										 `value` longtext NOT NULL,
										 PRIMARY KEY (`id`),
										 UNIQUE KEY `uuid_clob_datatype_storage` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clob_datatype_storage`
--

LOCK TABLES `clob_datatype_storage` WRITE;
/*!40000 ALTER TABLE `clob_datatype_storage` DISABLE KEYS */;
/*!40000 ALTER TABLE `clob_datatype_storage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cohort`
--

DROP TABLE IF EXISTS `cohort`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cohort` (
						  `cohort_id` int(11) NOT NULL AUTO_INCREMENT,
						  `name` varchar(255) NOT NULL,
						  `description` varchar(1000) DEFAULT NULL,
						  `creator` int(11) NOT NULL,
						  `date_created` datetime NOT NULL,
						  `voided` tinyint(1) NOT NULL DEFAULT 0,
						  `voided_by` int(11) DEFAULT NULL,
						  `date_voided` datetime DEFAULT NULL,
						  `void_reason` varchar(255) DEFAULT NULL,
						  `changed_by` int(11) DEFAULT NULL,
						  `date_changed` datetime DEFAULT NULL,
						  `uuid` char(38) NOT NULL,
						  PRIMARY KEY (`cohort_id`),
						  UNIQUE KEY `uuid_cohort` (`uuid`),
						  KEY `cohort_creator` (`creator`),
						  KEY `user_who_changed_cohort` (`changed_by`),
						  KEY `user_who_voided_cohort` (`voided_by`),
						  CONSTRAINT `cohort_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
						  CONSTRAINT `user_who_changed_cohort` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
						  CONSTRAINT `user_who_voided_cohort` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cohort`
--

LOCK TABLES `cohort` WRITE;
/*!40000 ALTER TABLE `cohort` DISABLE KEYS */;
/*!40000 ALTER TABLE `cohort` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cohort_member`
--

DROP TABLE IF EXISTS `cohort_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cohort_member` (
								 `cohort_id` int(11) NOT NULL,
								 `patient_id` int(11) NOT NULL,
								 `cohort_member_id` int(11) NOT NULL AUTO_INCREMENT,
								 `start_date` datetime DEFAULT NULL,
								 `end_date` datetime DEFAULT NULL,
								 `creator` int(11) NOT NULL,
								 `date_created` datetime NOT NULL,
								 `voided` tinyint(1) NOT NULL DEFAULT 0,
								 `voided_by` int(11) DEFAULT NULL,
								 `date_voided` datetime DEFAULT NULL,
								 `void_reason` varchar(255) DEFAULT NULL,
								 `uuid` char(38) NOT NULL,
								 PRIMARY KEY (`cohort_member_id`),
								 UNIQUE KEY `uuid_cohort_member` (`uuid`),
								 KEY `cohort_member_creator` (`creator`),
								 KEY `member_patient` (`patient_id`),
								 KEY `parent_cohort` (`cohort_id`),
								 CONSTRAINT `cohort_member_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								 CONSTRAINT `member_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
								 CONSTRAINT `parent_cohort` FOREIGN KEY (`cohort_id`) REFERENCES `cohort` (`cohort_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cohort_member`
--

LOCK TABLES `cohort_member` WRITE;
/*!40000 ALTER TABLE `cohort_member` DISABLE KEYS */;
/*!40000 ALTER TABLE `cohort_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept`
--

DROP TABLE IF EXISTS `concept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept` (
						   `concept_id` int(11) NOT NULL AUTO_INCREMENT,
						   `retired` tinyint(1) NOT NULL DEFAULT 0,
						   `short_name` varchar(255) DEFAULT NULL,
						   `description` text DEFAULT NULL,
						   `form_text` text DEFAULT NULL,
						   `datatype_id` int(11) NOT NULL DEFAULT 0,
						   `class_id` int(11) NOT NULL DEFAULT 0,
						   `is_set` tinyint(1) NOT NULL DEFAULT 0,
						   `creator` int(11) NOT NULL DEFAULT 0,
						   `date_created` datetime NOT NULL,
						   `version` varchar(50) DEFAULT NULL,
						   `changed_by` int(11) DEFAULT NULL,
						   `date_changed` datetime DEFAULT NULL,
						   `retired_by` int(11) DEFAULT NULL,
						   `date_retired` datetime DEFAULT NULL,
						   `retire_reason` varchar(255) DEFAULT NULL,
						   `uuid` char(38) NOT NULL,
						   PRIMARY KEY (`concept_id`),
						   UNIQUE KEY `uuid_concept` (`uuid`),
						   KEY `concept_classes` (`class_id`),
						   KEY `concept_creator` (`creator`),
						   KEY `concept_datatypes` (`datatype_id`),
						   KEY `user_who_changed_concept` (`changed_by`),
						   KEY `user_who_retired_concept` (`retired_by`),
						   CONSTRAINT `concept_classes` FOREIGN KEY (`class_id`) REFERENCES `concept_class` (`concept_class_id`),
						   CONSTRAINT `concept_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
						   CONSTRAINT `concept_datatypes` FOREIGN KEY (`datatype_id`) REFERENCES `concept_datatype` (`concept_datatype_id`),
						   CONSTRAINT `user_who_changed_concept` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
						   CONSTRAINT `user_who_retired_concept` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept`
--

LOCK TABLES `concept` WRITE;
/*!40000 ALTER TABLE `concept` DISABLE KEYS */;
INSERT INTO `concept` VALUES (1,0,'','',NULL,4,11,0,1,'2018-06-04 18:29:58',NULL,NULL,NULL,NULL,NULL,NULL,'cf82933b-3f3f-45e7-a5ab-5d31aaee3da3'),(2,0,'','',NULL,4,11,0,1,'2018-06-04 18:29:58',NULL,NULL,NULL,NULL,NULL,NULL,'488b58ff-64f5-4f8a-8979-fa79940b1594');
/*!40000 ALTER TABLE `concept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_answer`
--

DROP TABLE IF EXISTS `concept_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_answer` (
								  `concept_answer_id` int(11) NOT NULL AUTO_INCREMENT,
								  `concept_id` int(11) NOT NULL DEFAULT 0,
								  `answer_concept` int(11) DEFAULT NULL,
								  `answer_drug` int(11) DEFAULT NULL,
								  `creator` int(11) NOT NULL DEFAULT 0,
								  `date_created` datetime NOT NULL,
								  `sort_weight` double DEFAULT NULL,
								  `uuid` char(38) NOT NULL,
								  PRIMARY KEY (`concept_answer_id`),
								  UNIQUE KEY `uuid_concept_answer` (`uuid`),
								  KEY `answer` (`answer_concept`),
								  KEY `answer_answer_drug_fk` (`answer_drug`),
								  KEY `answer_creator` (`creator`),
								  KEY `answers_for_concept` (`concept_id`),
								  CONSTRAINT `answer` FOREIGN KEY (`answer_concept`) REFERENCES `concept` (`concept_id`),
								  CONSTRAINT `answer_answer_drug_fk` FOREIGN KEY (`answer_drug`) REFERENCES `drug` (`drug_id`),
								  CONSTRAINT `answer_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								  CONSTRAINT `answers_for_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_answer`
--

LOCK TABLES `concept_answer` WRITE;
/*!40000 ALTER TABLE `concept_answer` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_answer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_attribute`
--

DROP TABLE IF EXISTS `concept_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_attribute` (
									 `concept_attribute_id` int(11) NOT NULL AUTO_INCREMENT,
									 `concept_id` int(11) NOT NULL,
									 `attribute_type_id` int(11) NOT NULL,
									 `value_reference` text NOT NULL,
									 `uuid` char(38) NOT NULL,
									 `creator` int(11) NOT NULL,
									 `date_created` datetime NOT NULL,
									 `changed_by` int(11) DEFAULT NULL,
									 `date_changed` datetime DEFAULT NULL,
									 `voided` tinyint(1) NOT NULL DEFAULT 0,
									 `voided_by` int(11) DEFAULT NULL,
									 `date_voided` datetime DEFAULT NULL,
									 `void_reason` varchar(255) DEFAULT NULL,
									 PRIMARY KEY (`concept_attribute_id`),
									 UNIQUE KEY `uuid_concept_attribute` (`uuid`),
									 KEY `concept_attribute_attribute_type_id_fk` (`attribute_type_id`),
									 KEY `concept_attribute_changed_by_fk` (`changed_by`),
									 KEY `concept_attribute_concept_fk` (`concept_id`),
									 KEY `concept_attribute_creator_fk` (`creator`),
									 KEY `concept_attribute_voided_by_fk` (`voided_by`),
									 CONSTRAINT `concept_attribute_attribute_type_id_fk` FOREIGN KEY (`attribute_type_id`) REFERENCES `concept_attribute_type` (`concept_attribute_type_id`),
									 CONSTRAINT `concept_attribute_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									 CONSTRAINT `concept_attribute_concept_fk` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
									 CONSTRAINT `concept_attribute_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									 CONSTRAINT `concept_attribute_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_attribute`
--

LOCK TABLES `concept_attribute` WRITE;
/*!40000 ALTER TABLE `concept_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_attribute_type`
--

DROP TABLE IF EXISTS `concept_attribute_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_attribute_type` (
										  `concept_attribute_type_id` int(11) NOT NULL AUTO_INCREMENT,
										  `name` varchar(255) NOT NULL,
										  `description` varchar(1024) DEFAULT NULL,
										  `datatype` varchar(255) DEFAULT NULL,
										  `datatype_config` text DEFAULT NULL,
										  `preferred_handler` varchar(255) DEFAULT NULL,
										  `handler_config` text DEFAULT NULL,
										  `min_occurs` int(11) NOT NULL,
										  `max_occurs` int(11) DEFAULT NULL,
										  `creator` int(11) NOT NULL,
										  `date_created` datetime NOT NULL,
										  `changed_by` int(11) DEFAULT NULL,
										  `date_changed` datetime DEFAULT NULL,
										  `retired` tinyint(1) NOT NULL DEFAULT 0,
										  `retired_by` int(11) DEFAULT NULL,
										  `date_retired` datetime DEFAULT NULL,
										  `retire_reason` varchar(255) DEFAULT NULL,
										  `uuid` char(38) NOT NULL,
										  PRIMARY KEY (`concept_attribute_type_id`),
										  UNIQUE KEY `uuid_concept_attribute_type` (`uuid`),
										  KEY `concept_attribute_type_changed_by_fk` (`changed_by`),
										  KEY `concept_attribute_type_creator_fk` (`creator`),
										  KEY `concept_attribute_type_retired_by_fk` (`retired_by`),
										  CONSTRAINT `concept_attribute_type_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
										  CONSTRAINT `concept_attribute_type_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
										  CONSTRAINT `concept_attribute_type_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_attribute_type`
--

LOCK TABLES `concept_attribute_type` WRITE;
/*!40000 ALTER TABLE `concept_attribute_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_attribute_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_class`
--

DROP TABLE IF EXISTS `concept_class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_class` (
								 `concept_class_id` int(11) NOT NULL AUTO_INCREMENT,
								 `name` varchar(255) NOT NULL DEFAULT '',
								 `description` varchar(255) DEFAULT NULL,
								 `creator` int(11) NOT NULL DEFAULT 0,
								 `date_created` datetime NOT NULL,
								 `retired` tinyint(1) NOT NULL DEFAULT 0,
								 `retired_by` int(11) DEFAULT NULL,
								 `date_retired` datetime DEFAULT NULL,
								 `retire_reason` varchar(255) DEFAULT NULL,
								 `uuid` char(38) NOT NULL,
								 `date_changed` datetime DEFAULT NULL,
								 `changed_by` int(11) DEFAULT NULL,
								 PRIMARY KEY (`concept_class_id`),
								 UNIQUE KEY `uuid_concept_class` (`uuid`),
								 KEY `concept_class_changed_by` (`changed_by`),
								 KEY `concept_class_creator` (`creator`),
								 KEY `concept_class_name_index` (`name`),
								 KEY `concept_class_retired_status` (`retired`),
								 KEY `user_who_retired_concept_class` (`retired_by`),
								 CONSTRAINT `concept_class_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
								 CONSTRAINT `concept_class_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								 CONSTRAINT `user_who_retired_concept_class` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_class`
--

LOCK TABLES `concept_class` WRITE;
/*!40000 ALTER TABLE `concept_class` DISABLE KEYS */;
INSERT INTO `concept_class` VALUES (1,'Test','Acq. during patient encounter (vitals, labs, etc.)',1,'2004-02-02 00:00:00',0,NULL,NULL,NULL,'8d4907b2-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(2,'Procedure','Describes a clinical procedure',1,'2004-03-02 00:00:00',0,NULL,NULL,NULL,'8d490bf4-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(3,'Drug','Drug',1,'2004-02-02 00:00:00',0,NULL,NULL,NULL,'8d490dfc-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(4,'Diagnosis','Conclusion drawn through findings',1,'2004-02-02 00:00:00',0,NULL,NULL,NULL,'8d4918b0-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(5,'Finding','Practitioner observation/finding',1,'2004-03-02 00:00:00',0,NULL,NULL,NULL,'8d491a9a-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(6,'Anatomy','Anatomic sites / descriptors',1,'2004-03-02 00:00:00',0,NULL,NULL,NULL,'8d491c7a-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(7,'Question','Question (eg, patient history, SF36 items)',1,'2004-03-02 00:00:00',0,NULL,NULL,NULL,'8d491e50-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(8,'LabSet','Term to describe laboratory sets',1,'2004-03-02 00:00:00',0,NULL,NULL,NULL,'8d492026-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(9,'MedSet','Term to describe medication sets',1,'2004-02-02 00:00:00',0,NULL,NULL,NULL,'8d4923b4-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(10,'ConvSet','Term to describe convenience sets',1,'2004-03-02 00:00:00',0,NULL,NULL,NULL,'8d492594-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(11,'Misc','Terms which don\'t fit other categories',1,'2004-03-02 00:00:00',0,NULL,NULL,NULL,'8d492774-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(12,'Symptom','Patient-reported observation',1,'2004-10-04 00:00:00',0,NULL,NULL,NULL,'8d492954-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(13,'Symptom/Finding','Observation that can be reported from patient or found on exam',1,'2004-10-04 00:00:00',0,NULL,NULL,NULL,'8d492b2a-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(14,'Specimen','Body or fluid specimen',1,'2004-12-02 00:00:00',0,NULL,NULL,NULL,'8d492d0a-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(15,'Misc Order','Orderable items which aren\'t tests or drugs',1,'2005-02-17 00:00:00',0,NULL,NULL,NULL,'8d492ee0-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(16,'Frequency','A class for order frequencies',1,'2014-03-06 00:00:00',0,NULL,NULL,NULL,'8e071bfe-520c-44c0-a89b-538e9129b42a',NULL,NULL);
/*!40000 ALTER TABLE `concept_class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_complex`
--

DROP TABLE IF EXISTS `concept_complex`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_complex` (
								   `concept_id` int(11) NOT NULL,
								   `handler` varchar(255) DEFAULT NULL,
								   PRIMARY KEY (`concept_id`),
								   CONSTRAINT `concept_attributes` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_complex`
--

LOCK TABLES `concept_complex` WRITE;
/*!40000 ALTER TABLE `concept_complex` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_complex` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_datatype`
--

DROP TABLE IF EXISTS `concept_datatype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_datatype` (
									`concept_datatype_id` int(11) NOT NULL AUTO_INCREMENT,
									`name` varchar(255) NOT NULL DEFAULT '',
									`hl7_abbreviation` varchar(3) DEFAULT NULL,
									`description` varchar(255) DEFAULT NULL,
									`creator` int(11) NOT NULL DEFAULT 0,
									`date_created` datetime NOT NULL,
									`retired` tinyint(1) NOT NULL DEFAULT 0,
									`retired_by` int(11) DEFAULT NULL,
									`date_retired` datetime DEFAULT NULL,
									`retire_reason` varchar(255) DEFAULT NULL,
									`uuid` char(38) NOT NULL,
									PRIMARY KEY (`concept_datatype_id`),
									UNIQUE KEY `uuid_concept_datatype` (`uuid`),
									KEY `concept_datatype_creator` (`creator`),
									KEY `concept_datatype_name_index` (`name`),
									KEY `concept_datatype_retired_status` (`retired`),
									KEY `user_who_retired_concept_datatype` (`retired_by`),
									CONSTRAINT `concept_datatype_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									CONSTRAINT `user_who_retired_concept_datatype` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_datatype`
--

LOCK TABLES `concept_datatype` WRITE;
/*!40000 ALTER TABLE `concept_datatype` DISABLE KEYS */;
INSERT INTO `concept_datatype` VALUES (1,'Numeric','NM','Numeric value, including integer or float (e.g., creatinine, weight)',1,'2004-02-02 00:00:00',0,NULL,NULL,NULL,'8d4a4488-c2cc-11de-8d13-0010c6dffd0f'),(2,'Coded','CWE','Value determined by term dictionary lookup (i.e., term identifier)',1,'2004-02-02 00:00:00',0,NULL,NULL,NULL,'8d4a48b6-c2cc-11de-8d13-0010c6dffd0f'),(3,'Text','ST','Free text',1,'2004-02-02 00:00:00',0,NULL,NULL,NULL,'8d4a4ab4-c2cc-11de-8d13-0010c6dffd0f'),(4,'N/A','ZZ','Not associated with a datatype (e.g., term answers, sets)',1,'2004-02-02 00:00:00',0,NULL,NULL,NULL,'8d4a4c94-c2cc-11de-8d13-0010c6dffd0f'),(5,'Document','RP','Pointer to a binary or text-based document (e.g., clinical document, RTF, XML, EKG, image, etc.) stored in complex_obs table',1,'2004-04-15 00:00:00',0,NULL,NULL,NULL,'8d4a4e74-c2cc-11de-8d13-0010c6dffd0f'),(6,'Date','DT','Absolute date',1,'2004-07-22 00:00:00',0,NULL,NULL,NULL,'8d4a505e-c2cc-11de-8d13-0010c6dffd0f'),(7,'Time','TM','Absolute time of day',1,'2004-07-22 00:00:00',0,NULL,NULL,NULL,'8d4a591e-c2cc-11de-8d13-0010c6dffd0f'),(8,'Datetime','TS','Absolute date and time',1,'2004-07-22 00:00:00',0,NULL,NULL,NULL,'8d4a5af4-c2cc-11de-8d13-0010c6dffd0f'),(10,'Boolean','BIT','Boolean value (yes/no, true/false)',1,'2004-08-26 00:00:00',0,NULL,NULL,NULL,'8d4a5cca-c2cc-11de-8d13-0010c6dffd0f'),(11,'Rule','ZZ','Value derived from other data',1,'2006-09-11 00:00:00',0,NULL,NULL,NULL,'8d4a5e96-c2cc-11de-8d13-0010c6dffd0f'),(12,'Structured Numeric','SN','Complex numeric values possible (ie, <5, 1-10, etc.)',1,'2005-08-06 00:00:00',0,NULL,NULL,NULL,'8d4a606c-c2cc-11de-8d13-0010c6dffd0f'),(13,'Complex','ED','Complex value.  Analogous to HL7 Embedded Datatype',1,'2008-05-28 12:25:34',0,NULL,NULL,NULL,'8d4a6242-c2cc-11de-8d13-0010c6dffd0f');
/*!40000 ALTER TABLE `concept_datatype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_description`
--

DROP TABLE IF EXISTS `concept_description`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_description` (
									   `concept_description_id` int(11) NOT NULL AUTO_INCREMENT,
									   `concept_id` int(11) NOT NULL DEFAULT 0,
									   `description` text NOT NULL,
									   `locale` varchar(50) NOT NULL DEFAULT '',
									   `creator` int(11) NOT NULL DEFAULT 0,
									   `date_created` datetime NOT NULL,
									   `changed_by` int(11) DEFAULT NULL,
									   `date_changed` datetime DEFAULT NULL,
									   `uuid` char(38) NOT NULL,
									   PRIMARY KEY (`concept_description_id`),
									   UNIQUE KEY `uuid_concept_description` (`uuid`),
									   KEY `description_for_concept` (`concept_id`),
									   KEY `user_who_changed_description` (`changed_by`),
									   KEY `user_who_created_description` (`creator`),
									   CONSTRAINT `description_for_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
									   CONSTRAINT `user_who_changed_description` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									   CONSTRAINT `user_who_created_description` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_description`
--

LOCK TABLES `concept_description` WRITE;
/*!40000 ALTER TABLE `concept_description` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_description` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_map_type`
--

DROP TABLE IF EXISTS `concept_map_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_map_type` (
									`concept_map_type_id` int(11) NOT NULL AUTO_INCREMENT,
									`name` varchar(255) NOT NULL,
									`description` varchar(255) DEFAULT NULL,
									`creator` int(11) NOT NULL,
									`date_created` datetime NOT NULL,
									`changed_by` int(11) DEFAULT NULL,
									`date_changed` datetime DEFAULT NULL,
									`is_hidden` tinyint(1) NOT NULL DEFAULT 0,
									`retired` tinyint(1) NOT NULL DEFAULT 0,
									`retired_by` int(11) DEFAULT NULL,
									`date_retired` datetime DEFAULT NULL,
									`retire_reason` varchar(255) DEFAULT NULL,
									`uuid` char(38) NOT NULL,
									PRIMARY KEY (`concept_map_type_id`),
									UNIQUE KEY `concept_map_type_name` (`name`),
									UNIQUE KEY `uuid_concept_map_type` (`uuid`),
									KEY `mapped_user_changed_concept_map_type` (`changed_by`),
									KEY `mapped_user_creator_concept_map_type` (`creator`),
									KEY `mapped_user_retired_concept_map_type` (`retired_by`),
									CONSTRAINT `mapped_user_changed_concept_map_type` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									CONSTRAINT `mapped_user_creator_concept_map_type` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									CONSTRAINT `mapped_user_retired_concept_map_type` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_map_type`
--

LOCK TABLES `concept_map_type` WRITE;
/*!40000 ALTER TABLE `concept_map_type` DISABLE KEYS */;
INSERT INTO `concept_map_type` VALUES (1,'SAME-AS',NULL,1,'2018-06-04 00:00:00',NULL,NULL,0,0,NULL,NULL,NULL,'35543629-7d8c-11e1-909d-c80aa9edcf4e'),(2,'NARROWER-THAN',NULL,1,'2018-06-04 00:00:00',NULL,NULL,0,0,NULL,NULL,NULL,'43ac5109-7d8c-11e1-909d-c80aa9edcf4e'),(3,'BROADER-THAN',NULL,1,'2018-06-04 00:00:00',NULL,NULL,0,0,NULL,NULL,NULL,'4b9d9421-7d8c-11e1-909d-c80aa9edcf4e'),(4,'Associated finding',NULL,1,'2018-06-04 00:00:00',NULL,NULL,0,0,NULL,NULL,NULL,'55e02065-7d8c-11e1-909d-c80aa9edcf4e'),(5,'Associated morphology',NULL,1,'2018-06-04 00:00:00',NULL,NULL,0,0,NULL,NULL,NULL,'605f4a61-7d8c-11e1-909d-c80aa9edcf4e'),(6,'Associated procedure',NULL,1,'2018-06-04 00:00:00',NULL,NULL,0,0,NULL,NULL,NULL,'6eb1bfce-7d8c-11e1-909d-c80aa9edcf4e'),(7,'Associated with',NULL,1,'2018-06-04 00:00:00',NULL,NULL,0,0,NULL,NULL,NULL,'781bdc8f-7d8c-11e1-909d-c80aa9edcf4e'),(8,'Causative agent',NULL,1,'2018-06-04 00:00:00',NULL,NULL,0,0,NULL,NULL,NULL,'808f9e19-7d8c-11e1-909d-c80aa9edcf4e'),(9,'Finding site',NULL,1,'2018-06-04 00:00:00',NULL,NULL,0,0,NULL,NULL,NULL,'889c3013-7d8c-11e1-909d-c80aa9edcf4e'),(10,'Has specimen',NULL,1,'2018-06-04 00:00:00',NULL,NULL,0,0,NULL,NULL,NULL,'929600b9-7d8c-11e1-909d-c80aa9edcf4e'),(11,'Laterality',NULL,1,'2018-06-04 00:00:00',NULL,NULL,0,0,NULL,NULL,NULL,'999c6fc0-7d8c-11e1-909d-c80aa9edcf4e'),(12,'Severity',NULL,1,'2018-06-04 00:00:00',NULL,NULL,0,0,NULL,NULL,NULL,'a0e52281-7d8c-11e1-909d-c80aa9edcf4e'),(13,'Access',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'f9e90b29-7d8c-11e1-909d-c80aa9edcf4e'),(14,'After',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'01b60e29-7d8d-11e1-909d-c80aa9edcf4e'),(15,'Clinical course',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'5f7c3702-7d8d-11e1-909d-c80aa9edcf4e'),(16,'Component',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'67debecc-7d8d-11e1-909d-c80aa9edcf4e'),(17,'Direct device',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'718c00da-7d8d-11e1-909d-c80aa9edcf4e'),(18,'Direct morphology',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'7b9509cb-7d8d-11e1-909d-c80aa9edcf4e'),(19,'Direct substance',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'82bb495d-7d8d-11e1-909d-c80aa9edcf4e'),(20,'Due to',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'8b77f7d3-7d8d-11e1-909d-c80aa9edcf4e'),(21,'Episodicity',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'94a81179-7d8d-11e1-909d-c80aa9edcf4e'),(22,'Finding context',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'9d23c22e-7d8d-11e1-909d-c80aa9edcf4e'),(23,'Finding informer',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'a4524368-7d8d-11e1-909d-c80aa9edcf4e'),(24,'Finding method',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'af089254-7d8d-11e1-909d-c80aa9edcf4e'),(25,'Has active ingredient',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'b65aa605-7d8d-11e1-909d-c80aa9edcf4e'),(26,'Has definitional manifestation',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'c2b7b2fa-7d8d-11e1-909d-c80aa9edcf4'),(27,'Has dose form',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'cc3878e6-7d8d-11e1-909d-c80aa9edcf4e'),(28,'Has focus',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'d67c5840-7d8d-11e1-909d-c80aa9edcf4e'),(29,'Has intent',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'de2fb2c5-7d8d-11e1-909d-c80aa9edcf4e'),(30,'Has interpretation',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'e758838b-7d8d-11e1-909d-c80aa9edcf4e'),(31,'Indirect device',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'ee63c142-7d8d-11e1-909d-c80aa9edcf4e'),(32,'Indirect morphology',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'f4f36681-7d8d-11e1-909d-c80aa9edcf4e'),(33,'Interprets',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'fc7f5fed-7d8d-11e1-909d-c80aa9edcf4e'),(34,'Measurement method',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'06b11d79-7d8e-11e1-909d-c80aa9edcf4e'),(35,'Method',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'0efb4753-7d8e-11e1-909d-c80aa9edcf4e'),(36,'Occurrence',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'16e7b617-7d8e-11e1-909d-c80aa9edcf4e'),(37,'Part of',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'1e82007b-7d8e-11e1-909d-c80aa9edcf4e'),(38,'Pathological process',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'2969915e-7d8e-11e1-909d-c80aa9edcf4e'),(39,'Priority',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'32d57796-7d8e-11e1-909d-c80aa9edcf4e'),(40,'Procedure context',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'3f11904c-7d8e-11e1-909d-c80aa9edcf4e'),(41,'Procedure device',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'468c4aa3-7d8e-11e1-909d-c80aa9edcf4e'),(42,'Procedure morphology',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'5383e889-7d8e-11e1-909d-c80aa9edcf4e'),(43,'Procedure site',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'5ad2655d-7d8e-11e1-909d-c80aa9edcf4e'),(44,'Procedure site - Direct',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'66085196-7d8e-11e1-909d-c80aa9edcf4e'),(45,'Procedure site - Indirect',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'7080e843-7d8e-11e1-909d-c80aa9edcf4e'),(46,'Property',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'76bfb796-7d8e-11e1-909d-c80aa9edcf4e'),(47,'Recipient category',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'7e7d00e4-7d8e-11e1-909d-c80aa9edcf4e'),(48,'Revision status',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'851e14c1-7d8e-11e1-909d-c80aa9edcf4e'),(49,'Route of administration',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'8ee5b13d-7d8e-11e1-909d-c80aa9edcf4e'),(50,'Scale type',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'986acf48-7d8e-11e1-909d-c80aa9edcf4e'),(51,'Specimen procedure',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'a6937642-7d8e-11e1-909d-c80aa9edcf4e'),(52,'Specimen source identity',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'b1d6941e-7d8e-11e1-909d-c80aa9edcf4e'),(53,'Specimen source morphology',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'b7c793c1-7d8e-11e1-909d-c80aa9edcf4e'),(54,'Specimen source topography',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'be9f9eb8-7d8e-11e1-909d-c80aa9edcf4e'),(55,'Specimen substance',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'c8f2bacb-7d8e-11e1-909d-c80aa9edcf4e'),(56,'Subject of information',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'d0664c4f-7d8e-11e1-909d-c80aa9edcf4e'),(57,'Subject relationship context',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'dace9d13-7d8e-11e1-909d-c80aa9edcf4e'),(58,'Surgical approach',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'e3cd666d-7d8e-11e1-909d-c80aa9edcf4e'),(59,'Temporal context',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'ed96447d-7d8e-11e1-909d-c80aa9edcf4e'),(60,'Time aspect',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'f415bcce-7d8e-11e1-909d-c80aa9edcf4e'),(61,'Using access device',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'fa9538a9-7d8e-11e1-909d-c80aa9edcf4e'),(62,'Using device',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'06588655-7d8f-11e1-909d-c80aa9edcf4e'),(63,'Using energy',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'0c2ae0bc-7d8f-11e1-909d-c80aa9edcf4e'),(64,'Using substance',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'13d2c607-7d8f-11e1-909d-c80aa9edcf4e'),(65,'IS A',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'1ce7a784-7d8f-11e1-909d-c80aa9edcf4e'),(66,'MAY BE A',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'267812a3-7d8f-11e1-909d-c80aa9edcf4e'),(67,'MOVED FROM',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'2de3168e-7d8f-11e1-909d-c80aa9edcf4e'),(68,'MOVED TO',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'32f0fd99-7d8f-11e1-909d-c80aa9edcf4e'),(69,'REPLACED BY',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'3b3b9a7d-7d8f-11e1-909d-c80aa9edcf4e'),(70,'WAS A',NULL,1,'2018-06-04 00:00:00',NULL,NULL,1,0,NULL,NULL,NULL,'41a034da-7d8f-11e1-909d-c80aa9edcf4e');
/*!40000 ALTER TABLE `concept_map_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_name`
--

DROP TABLE IF EXISTS `concept_name`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_name` (
								`concept_name_id` int(11) NOT NULL AUTO_INCREMENT,
								`concept_id` int(11) DEFAULT NULL,
								`name` varchar(255) NOT NULL DEFAULT '',
								`locale` varchar(50) NOT NULL DEFAULT '',
								`locale_preferred` tinyint(1) DEFAULT 0,
								`creator` int(11) NOT NULL DEFAULT 0,
								`date_created` datetime NOT NULL,
								`concept_name_type` varchar(50) DEFAULT NULL,
								`voided` tinyint(1) NOT NULL DEFAULT 0,
								`voided_by` int(11) DEFAULT NULL,
								`date_voided` datetime DEFAULT NULL,
								`void_reason` varchar(255) DEFAULT NULL,
								`uuid` char(38) NOT NULL,
								`date_changed` datetime DEFAULT NULL,
								`changed_by` int(11) DEFAULT NULL,
								PRIMARY KEY (`concept_name_id`),
								UNIQUE KEY `uuid_concept_name` (`uuid`),
								KEY `concept_name_changed_by` (`changed_by`),
								KEY `name_for_concept` (`concept_id`),
								KEY `name_of_concept` (`name`),
								KEY `user_who_created_name` (`creator`),
								KEY `user_who_voided_this_name` (`voided_by`),
								CONSTRAINT `concept_name_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
								CONSTRAINT `name_for_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
								CONSTRAINT `user_who_created_name` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								CONSTRAINT `user_who_voided_this_name` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_name`
--

LOCK TABLES `concept_name` WRITE;
/*!40000 ALTER TABLE `concept_name` DISABLE KEYS */;
INSERT INTO `concept_name` VALUES (1,1,'Verdadeiro','pt',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'7561f550-ccee-43b6-bcf4-269992d6c284',NULL,NULL),(2,1,'Sim','pt',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'89522273-55b5-4c4a-8e0b-cc7a66f3f3aa',NULL,NULL),(3,1,'True','en',1,1,'2018-06-04 18:29:58','FULLY_SPECIFIED',0,NULL,NULL,NULL,'111eb000-3113-4d74-9d9b-98ae5d566a98',NULL,NULL),(4,1,'Yes','en',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'a31778d2-53ab-4c4b-a333-a0149d1c25c6',NULL,NULL),(5,1,'Vero','it',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'a069d777-3999-4bb4-8217-d4fae1f337bc',NULL,NULL),(6,1,'Sì','it',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'f16c811c-d970-4f02-be47-0c7afea9eddc',NULL,NULL),(7,1,'Vrai','fr',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'bb75175c-5d1b-492a-8c38-98506e2e6b2d',NULL,NULL),(8,1,'Oui','fr',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'c4c4b0f6-c80c-410b-9f48-72b683072184',NULL,NULL),(9,1,'Verdadero','es',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'bf9ace2a-ff96-44fc-b529-172f5c3ac0b0',NULL,NULL),(10,1,'Sí','es',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'b58d53a3-c6dd-469a-8db1-0a80dfe4c542',NULL,NULL),(11,2,'Falso','pt',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'2553457e-849d-4a5c-9824-2f0a9ba79461',NULL,NULL),(12,2,'Não','pt',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'38a0828c-c54e-4bdb-8217-defb0fdfc4b6',NULL,NULL),(13,2,'False','en',1,1,'2018-06-04 18:29:58','FULLY_SPECIFIED',0,NULL,NULL,NULL,'ac6d888e-dc00-4402-9a2a-576438161f96',NULL,NULL),(14,2,'No','en',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'17a5cdd3-a0a9-406a-b39f-5cd7e8d634c4',NULL,NULL),(15,2,'Falso','it',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'026e4e70-28c5-458d-8bbf-be7abaf09519',NULL,NULL),(16,2,'No','it',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'23851ffa-da5d-4086-8107-e0318b4d26eb',NULL,NULL),(17,2,'Faux','fr',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'5709a1b5-b3de-472f-bf7a-eb57c5cb7144',NULL,NULL),(18,2,'Non','fr',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'3990b17b-22a0-4b66-9d06-1645030e6b60',NULL,NULL),(19,2,'Falso','es',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'ae3cf994-72e1-471d-9af0-96dace0b6787',NULL,NULL),(20,2,'No','es',0,1,'2018-06-04 18:29:58',NULL,0,NULL,NULL,NULL,'72071f05-d7e2-4687-ac2e-c7d9809d888a',NULL,NULL);
/*!40000 ALTER TABLE `concept_name` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_name_tag`
--

DROP TABLE IF EXISTS `concept_name_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_name_tag` (
									`concept_name_tag_id` int(11) NOT NULL AUTO_INCREMENT,
									`tag` varchar(50) NOT NULL,
									`description` text DEFAULT NULL,
									`creator` int(11) NOT NULL DEFAULT 0,
									`date_created` datetime NOT NULL,
									`voided` tinyint(1) NOT NULL DEFAULT 0,
									`voided_by` int(11) DEFAULT NULL,
									`date_voided` datetime DEFAULT NULL,
									`void_reason` varchar(255) DEFAULT NULL,
									`uuid` char(38) NOT NULL,
									`date_changed` datetime DEFAULT NULL,
									`changed_by` int(11) DEFAULT NULL,
									PRIMARY KEY (`concept_name_tag_id`),
									UNIQUE KEY `tag` (`tag`),
									UNIQUE KEY `uuid_concept_name_tag` (`uuid`),
									KEY `concept_name_tag_changed_by` (`changed_by`),
									KEY `user_who_created_name_tag` (`creator`),
									KEY `user_who_voided_name_tag` (`voided_by`),
									CONSTRAINT `concept_name_tag_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_name_tag`
--

LOCK TABLES `concept_name_tag` WRITE;
/*!40000 ALTER TABLE `concept_name_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_name_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_name_tag_map`
--

DROP TABLE IF EXISTS `concept_name_tag_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_name_tag_map` (
										`concept_name_id` int(11) NOT NULL,
										`concept_name_tag_id` int(11) NOT NULL,
										KEY `mapped_concept_name` (`concept_name_id`),
										KEY `mapped_concept_name_tag` (`concept_name_tag_id`),
										CONSTRAINT `mapped_concept_name` FOREIGN KEY (`concept_name_id`) REFERENCES `concept_name` (`concept_name_id`),
										CONSTRAINT `mapped_concept_name_tag` FOREIGN KEY (`concept_name_tag_id`) REFERENCES `concept_name_tag` (`concept_name_tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_name_tag_map`
--

LOCK TABLES `concept_name_tag_map` WRITE;
/*!40000 ALTER TABLE `concept_name_tag_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_name_tag_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_numeric`
--

DROP TABLE IF EXISTS `concept_numeric`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_numeric` (
								   `concept_id` int(11) NOT NULL DEFAULT 0,
								   `hi_absolute` double DEFAULT NULL,
								   `hi_critical` double DEFAULT NULL,
								   `hi_normal` double DEFAULT NULL,
								   `low_absolute` double DEFAULT NULL,
								   `low_critical` double DEFAULT NULL,
								   `low_normal` double DEFAULT NULL,
								   `units` varchar(50) DEFAULT NULL,
								   `allow_decimal` tinyint(1) DEFAULT 0,
								   `display_precision` int(11) DEFAULT NULL,
								   PRIMARY KEY (`concept_id`),
								   CONSTRAINT `numeric_attributes` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_numeric`
--

LOCK TABLES `concept_numeric` WRITE;
/*!40000 ALTER TABLE `concept_numeric` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_numeric` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_proposal`
--

DROP TABLE IF EXISTS `concept_proposal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_proposal` (
									`concept_proposal_id` int(11) NOT NULL AUTO_INCREMENT,
									`concept_id` int(11) DEFAULT NULL,
									`encounter_id` int(11) DEFAULT NULL,
									`original_text` varchar(255) NOT NULL DEFAULT '',
									`final_text` varchar(255) DEFAULT NULL,
									`obs_id` int(11) DEFAULT NULL,
									`obs_concept_id` int(11) DEFAULT NULL,
									`state` varchar(32) NOT NULL DEFAULT 'UNMAPPED',
									`comments` varchar(255) DEFAULT NULL,
									`creator` int(11) NOT NULL DEFAULT 0,
									`date_created` datetime NOT NULL,
									`changed_by` int(11) DEFAULT NULL,
									`date_changed` datetime DEFAULT NULL,
									`locale` varchar(50) NOT NULL DEFAULT '',
									`uuid` char(38) NOT NULL,
									PRIMARY KEY (`concept_proposal_id`),
									UNIQUE KEY `uuid_concept_proposal` (`uuid`),
									KEY `concept_for_proposal` (`concept_id`),
									KEY `encounter_for_proposal` (`encounter_id`),
									KEY `proposal_obs_concept_id` (`obs_concept_id`),
									KEY `proposal_obs_id` (`obs_id`),
									KEY `user_who_changed_proposal` (`changed_by`),
									KEY `user_who_created_proposal` (`creator`),
									CONSTRAINT `concept_for_proposal` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
									CONSTRAINT `encounter_for_proposal` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
									CONSTRAINT `proposal_obs_concept_id` FOREIGN KEY (`obs_concept_id`) REFERENCES `concept` (`concept_id`),
									CONSTRAINT `proposal_obs_id` FOREIGN KEY (`obs_id`) REFERENCES `obs` (`obs_id`),
									CONSTRAINT `user_who_changed_proposal` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									CONSTRAINT `user_who_created_proposal` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_proposal`
--

LOCK TABLES `concept_proposal` WRITE;
/*!40000 ALTER TABLE `concept_proposal` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_proposal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_proposal_tag_map`
--

DROP TABLE IF EXISTS `concept_proposal_tag_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_proposal_tag_map` (
											`concept_proposal_id` int(11) NOT NULL,
											`concept_name_tag_id` int(11) NOT NULL,
											KEY `mapped_concept_proposal` (`concept_proposal_id`),
											KEY `mapped_concept_proposal_tag` (`concept_name_tag_id`),
											CONSTRAINT `mapped_concept_proposal` FOREIGN KEY (`concept_proposal_id`) REFERENCES `concept_proposal` (`concept_proposal_id`),
											CONSTRAINT `mapped_concept_proposal_tag` FOREIGN KEY (`concept_name_tag_id`) REFERENCES `concept_name_tag` (`concept_name_tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_proposal_tag_map`
--

LOCK TABLES `concept_proposal_tag_map` WRITE;
/*!40000 ALTER TABLE `concept_proposal_tag_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_proposal_tag_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_reference_map`
--

DROP TABLE IF EXISTS `concept_reference_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_reference_map` (
										 `concept_map_id` int(11) NOT NULL AUTO_INCREMENT,
										 `concept_reference_term_id` int(11) NOT NULL,
										 `concept_map_type_id` int(11) NOT NULL DEFAULT 1,
										 `creator` int(11) NOT NULL DEFAULT 0,
										 `date_created` datetime NOT NULL,
										 `concept_id` int(11) NOT NULL DEFAULT 0,
										 `changed_by` int(11) DEFAULT NULL,
										 `date_changed` datetime DEFAULT NULL,
										 `uuid` char(38) NOT NULL,
										 PRIMARY KEY (`concept_map_id`),
										 UNIQUE KEY `uuid_concept_reference_map` (`uuid`),
										 KEY `map_creator` (`creator`),
										 KEY `map_for_concept` (`concept_id`),
										 KEY `mapped_concept_map_type` (`concept_map_type_id`),
										 KEY `mapped_concept_reference_term` (`concept_reference_term_id`),
										 KEY `mapped_user_changed_ref_term` (`changed_by`),
										 CONSTRAINT `map_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
										 CONSTRAINT `map_for_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
										 CONSTRAINT `mapped_concept_map_type` FOREIGN KEY (`concept_map_type_id`) REFERENCES `concept_map_type` (`concept_map_type_id`),
										 CONSTRAINT `mapped_concept_reference_term` FOREIGN KEY (`concept_reference_term_id`) REFERENCES `concept_reference_term` (`concept_reference_term_id`),
										 CONSTRAINT `mapped_user_changed_ref_term` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_reference_map`
--

LOCK TABLES `concept_reference_map` WRITE;
/*!40000 ALTER TABLE `concept_reference_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_reference_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_reference_source`
--

DROP TABLE IF EXISTS `concept_reference_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_reference_source` (
											`concept_source_id` int(11) NOT NULL AUTO_INCREMENT,
											`name` varchar(50) NOT NULL DEFAULT '',
											`description` text NOT NULL,
											`hl7_code` varchar(50) DEFAULT '',
											`creator` int(11) NOT NULL DEFAULT 0,
											`date_created` datetime NOT NULL,
											`retired` tinyint(1) NOT NULL DEFAULT 0,
											`retired_by` int(11) DEFAULT NULL,
											`date_retired` datetime DEFAULT NULL,
											`retire_reason` varchar(255) DEFAULT NULL,
											`uuid` char(38) NOT NULL,
											`unique_id` varchar(250) DEFAULT NULL,
											`date_changed` datetime DEFAULT NULL,
											`changed_by` int(11) DEFAULT NULL,
											PRIMARY KEY (`concept_source_id`),
											UNIQUE KEY `uuid_concept_reference_source` (`uuid`),
											UNIQUE KEY `hl7_code` (`hl7_code`),
											UNIQUE KEY `unique_id` (`unique_id`),
											KEY `concept_reference_source_changed_by` (`changed_by`),
											KEY `concept_source_creator` (`creator`),
											KEY `user_who_retired_concept_source` (`retired_by`),
											CONSTRAINT `concept_reference_source_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
											CONSTRAINT `concept_source_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
											CONSTRAINT `user_who_retired_concept_source` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_reference_source`
--

LOCK TABLES `concept_reference_source` WRITE;
/*!40000 ALTER TABLE `concept_reference_source` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_reference_source` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_reference_term`
--

DROP TABLE IF EXISTS `concept_reference_term`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_reference_term` (
										  `concept_reference_term_id` int(11) NOT NULL AUTO_INCREMENT,
										  `concept_source_id` int(11) NOT NULL,
										  `name` varchar(255) DEFAULT NULL,
										  `code` varchar(255) NOT NULL,
										  `version` varchar(255) DEFAULT NULL,
										  `description` varchar(255) DEFAULT NULL,
										  `creator` int(11) NOT NULL,
										  `date_created` datetime NOT NULL,
										  `date_changed` datetime DEFAULT NULL,
										  `changed_by` int(11) DEFAULT NULL,
										  `retired` tinyint(1) NOT NULL DEFAULT 0,
										  `retired_by` int(11) DEFAULT NULL,
										  `date_retired` datetime DEFAULT NULL,
										  `retire_reason` varchar(255) DEFAULT NULL,
										  `uuid` char(38) NOT NULL,
										  PRIMARY KEY (`concept_reference_term_id`),
										  UNIQUE KEY `uuid_concept_reference_term` (`uuid`),
										  KEY `idx_code_concept_reference_term` (`code`),
										  KEY `mapped_concept_source` (`concept_source_id`),
										  KEY `mapped_user_changed` (`changed_by`),
										  KEY `mapped_user_creator` (`creator`),
										  KEY `mapped_user_retired` (`retired_by`),
										  CONSTRAINT `mapped_concept_source` FOREIGN KEY (`concept_source_id`) REFERENCES `concept_reference_source` (`concept_source_id`),
										  CONSTRAINT `mapped_user_changed` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
										  CONSTRAINT `mapped_user_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
										  CONSTRAINT `mapped_user_retired` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_reference_term`
--

LOCK TABLES `concept_reference_term` WRITE;
/*!40000 ALTER TABLE `concept_reference_term` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_reference_term` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_reference_term_map`
--

DROP TABLE IF EXISTS `concept_reference_term_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_reference_term_map` (
											  `concept_reference_term_map_id` int(11) NOT NULL AUTO_INCREMENT,
											  `term_a_id` int(11) NOT NULL,
											  `term_b_id` int(11) NOT NULL,
											  `a_is_to_b_id` int(11) NOT NULL,
											  `creator` int(11) NOT NULL,
											  `date_created` datetime NOT NULL,
											  `changed_by` int(11) DEFAULT NULL,
											  `date_changed` datetime DEFAULT NULL,
											  `uuid` char(38) NOT NULL,
											  PRIMARY KEY (`concept_reference_term_map_id`),
											  UNIQUE KEY `uuid_concept_reference_term_map` (`uuid`),
											  KEY `mapped_concept_map_type_ref_term_map` (`a_is_to_b_id`),
											  KEY `mapped_term_a` (`term_a_id`),
											  KEY `mapped_term_b` (`term_b_id`),
											  KEY `mapped_user_changed_ref_term_map` (`changed_by`),
											  KEY `mapped_user_creator_ref_term_map` (`creator`),
											  CONSTRAINT `mapped_concept_map_type_ref_term_map` FOREIGN KEY (`a_is_to_b_id`) REFERENCES `concept_map_type` (`concept_map_type_id`),
											  CONSTRAINT `mapped_term_a` FOREIGN KEY (`term_a_id`) REFERENCES `concept_reference_term` (`concept_reference_term_id`),
											  CONSTRAINT `mapped_term_b` FOREIGN KEY (`term_b_id`) REFERENCES `concept_reference_term` (`concept_reference_term_id`),
											  CONSTRAINT `mapped_user_changed_ref_term_map` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
											  CONSTRAINT `mapped_user_creator_ref_term_map` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_reference_term_map`
--

LOCK TABLES `concept_reference_term_map` WRITE;
/*!40000 ALTER TABLE `concept_reference_term_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_reference_term_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_set`
--

DROP TABLE IF EXISTS `concept_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_set` (
							   `concept_set_id` int(11) NOT NULL AUTO_INCREMENT,
							   `concept_id` int(11) NOT NULL DEFAULT 0,
							   `concept_set` int(11) NOT NULL DEFAULT 0,
							   `sort_weight` double DEFAULT NULL,
							   `creator` int(11) NOT NULL DEFAULT 0,
							   `date_created` datetime NOT NULL,
							   `uuid` char(38) NOT NULL,
							   PRIMARY KEY (`concept_set_id`),
							   UNIQUE KEY `uuid_concept_set` (`uuid`),
							   KEY `has_a` (`concept_set`),
							   KEY `idx_concept_set_concept` (`concept_id`),
							   KEY `user_who_created` (`creator`),
							   CONSTRAINT `has_a` FOREIGN KEY (`concept_set`) REFERENCES `concept` (`concept_id`),
							   CONSTRAINT `user_who_created` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_set`
--

LOCK TABLES `concept_set` WRITE;
/*!40000 ALTER TABLE `concept_set` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_set` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_state_conversion`
--

DROP TABLE IF EXISTS `concept_state_conversion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_state_conversion` (
											`concept_state_conversion_id` int(11) NOT NULL AUTO_INCREMENT,
											`concept_id` int(11) DEFAULT 0,
											`program_workflow_id` int(11) DEFAULT 0,
											`program_workflow_state_id` int(11) DEFAULT 0,
											`uuid` char(38) NOT NULL,
											PRIMARY KEY (`concept_state_conversion_id`),
											UNIQUE KEY `uuid_concept_state_conversion` (`uuid`),
											UNIQUE KEY `unique_workflow_concept_in_conversion` (`program_workflow_id`,`concept_id`),
											KEY `concept_triggers_conversion` (`concept_id`),
											KEY `conversion_to_state` (`program_workflow_state_id`),
											CONSTRAINT `concept_triggers_conversion` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
											CONSTRAINT `conversion_involves_workflow` FOREIGN KEY (`program_workflow_id`) REFERENCES `program_workflow` (`program_workflow_id`),
											CONSTRAINT `conversion_to_state` FOREIGN KEY (`program_workflow_state_id`) REFERENCES `program_workflow_state` (`program_workflow_state_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_state_conversion`
--

LOCK TABLES `concept_state_conversion` WRITE;
/*!40000 ALTER TABLE `concept_state_conversion` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_state_conversion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `concept_stop_word`
--

DROP TABLE IF EXISTS `concept_stop_word`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `concept_stop_word` (
									 `concept_stop_word_id` int(11) NOT NULL AUTO_INCREMENT,
									 `word` varchar(50) NOT NULL,
									 `locale` varchar(50) DEFAULT NULL,
									 `uuid` char(38) NOT NULL,
									 PRIMARY KEY (`concept_stop_word_id`),
									 UNIQUE KEY `Unique_StopWord_Key` (`word`,`locale`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `concept_stop_word`
--

LOCK TABLES `concept_stop_word` WRITE;
/*!40000 ALTER TABLE `concept_stop_word` DISABLE KEYS */;
INSERT INTO `concept_stop_word` VALUES (1,'A','en','f5f45540-e2a7-11df-87ae-18a905e044dc'),(2,'AND','en','f5f469ae-e2a7-11df-87ae-18a905e044dc'),(3,'AT','en','f5f47070-e2a7-11df-87ae-18a905e044dc'),(4,'BUT','en','f5f476c4-e2a7-11df-87ae-18a905e044dc'),(5,'BY','en','f5f47d04-e2a7-11df-87ae-18a905e044dc'),(6,'FOR','en','f5f4834e-e2a7-11df-87ae-18a905e044dc'),(7,'HAS','en','f5f48a24-e2a7-11df-87ae-18a905e044dc'),(8,'OF','en','f5f49064-e2a7-11df-87ae-18a905e044dc'),(9,'THE','en','f5f496ae-e2a7-11df-87ae-18a905e044dc'),(10,'TO','en','f5f49cda-e2a7-11df-87ae-18a905e044dc');
/*!40000 ALTER TABLE `concept_stop_word` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conditions`
--

DROP TABLE IF EXISTS `conditions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `conditions` (
							  `condition_id` int(11) NOT NULL AUTO_INCREMENT,
							  `additional_detail` varchar(255) DEFAULT NULL,
							  `previous_version` int(11) DEFAULT NULL,
							  `condition_coded` int(11) DEFAULT NULL,
							  `condition_non_coded` varchar(255) DEFAULT NULL,
							  `condition_coded_name` int(11) DEFAULT NULL,
							  `clinical_status` varchar(50) NOT NULL,
							  `verification_status` varchar(50) DEFAULT NULL,
							  `onset_date` datetime DEFAULT NULL,
							  `date_created` datetime NOT NULL,
							  `voided` tinyint(1) NOT NULL DEFAULT 0,
							  `date_voided` datetime DEFAULT NULL,
							  `void_reason` varchar(255) DEFAULT NULL,
							  `uuid` varchar(38) DEFAULT NULL,
							  `creator` int(11) NOT NULL,
							  `voided_by` int(11) DEFAULT NULL,
							  `changed_by` int(11) DEFAULT NULL,
							  `patient_id` int(11) NOT NULL,
							  `end_date` datetime DEFAULT NULL,
							  `date_changed` datetime DEFAULT NULL,
							  `encounter_id` int(11) DEFAULT NULL,
							  `form_namespace_and_path` varchar(255) DEFAULT NULL,
							  PRIMARY KEY (`condition_id`),
							  UNIQUE KEY `uuid_conditions` (`uuid`),
							  KEY `condition_changed_by_fk` (`changed_by`),
							  KEY `condition_condition_coded_fk` (`condition_coded`),
							  KEY `condition_condition_coded_name_fk` (`condition_coded_name`),
							  KEY `condition_creator_fk` (`creator`),
							  KEY `condition_patient_fk` (`patient_id`),
							  KEY `condition_previous_version_fk` (`previous_version`),
							  KEY `condition_voided_by_fk` (`voided_by`),
							  KEY `conditions_encounter_id_fk` (`encounter_id`),
							  CONSTRAINT `condition_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
							  CONSTRAINT `condition_condition_coded_fk` FOREIGN KEY (`condition_coded`) REFERENCES `concept` (`concept_id`),
							  CONSTRAINT `condition_condition_coded_name_fk` FOREIGN KEY (`condition_coded_name`) REFERENCES `concept_name` (`concept_name_id`),
							  CONSTRAINT `condition_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
							  CONSTRAINT `condition_patient_fk` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
							  CONSTRAINT `condition_previous_version_fk` FOREIGN KEY (`previous_version`) REFERENCES `conditions` (`condition_id`),
							  CONSTRAINT `condition_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
							  CONSTRAINT `conditions_encounter_id_fk` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conditions`
--

LOCK TABLES `conditions` WRITE;
/*!40000 ALTER TABLE `conditions` DISABLE KEYS */;
/*!40000 ALTER TABLE `conditions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diagnosis_attribute`
--

DROP TABLE IF EXISTS `diagnosis_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `diagnosis_attribute` (
									   `diagnosis_attribute_id` int(11) NOT NULL AUTO_INCREMENT,
									   `diagnosis_id` int(11) NOT NULL,
									   `attribute_type_id` int(11) NOT NULL,
									   `value_reference` text NOT NULL,
									   `uuid` char(38) NOT NULL,
									   `creator` int(11) NOT NULL,
									   `date_created` datetime NOT NULL,
									   `changed_by` int(11) DEFAULT NULL,
									   `date_changed` datetime DEFAULT NULL,
									   `voided` tinyint(1) NOT NULL DEFAULT 0,
									   `voided_by` int(11) DEFAULT NULL,
									   `date_voided` datetime DEFAULT NULL,
									   `void_reason` varchar(255) DEFAULT NULL,
									   PRIMARY KEY (`diagnosis_attribute_id`),
									   UNIQUE KEY `uuid` (`uuid`),
									   KEY `diagnosis_attribute_diagnosis_fk` (`diagnosis_id`),
									   KEY `diagnosis_attribute_attribute_type_id_fk` (`attribute_type_id`),
									   KEY `diagnosis_attribute_creator_fk` (`creator`),
									   KEY `diagnosis_attribute_changed_by_fk` (`changed_by`),
									   KEY `diagnosis_attribute_voided_by_fk` (`voided_by`),
									   CONSTRAINT `diagnosis_attribute_attribute_type_id_fk` FOREIGN KEY (`attribute_type_id`) REFERENCES `diagnosis_attribute_type` (`diagnosis_attribute_type_id`),
									   CONSTRAINT `diagnosis_attribute_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									   CONSTRAINT `diagnosis_attribute_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									   CONSTRAINT `diagnosis_attribute_diagnosis_fk` FOREIGN KEY (`diagnosis_id`) REFERENCES `encounter_diagnosis` (`diagnosis_id`),
									   CONSTRAINT `diagnosis_attribute_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diagnosis_attribute`
--

LOCK TABLES `diagnosis_attribute` WRITE;
/*!40000 ALTER TABLE `diagnosis_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `diagnosis_attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diagnosis_attribute_type`
--

DROP TABLE IF EXISTS `diagnosis_attribute_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `diagnosis_attribute_type` (
											`diagnosis_attribute_type_id` int(11) NOT NULL AUTO_INCREMENT,
											`name` varchar(255) NOT NULL,
											`description` varchar(1024) DEFAULT NULL,
											`datatype` varchar(255) DEFAULT NULL,
											`datatype_config` text DEFAULT NULL,
											`preferred_handler` varchar(255) DEFAULT NULL,
											`handler_config` text DEFAULT NULL,
											`min_occurs` int(11) NOT NULL,
											`max_occurs` int(11) DEFAULT NULL,
											`creator` int(11) NOT NULL,
											`date_created` datetime NOT NULL,
											`changed_by` int(11) DEFAULT NULL,
											`date_changed` datetime DEFAULT NULL,
											`retired` tinyint(1) NOT NULL DEFAULT 0,
											`retired_by` int(11) DEFAULT NULL,
											`date_retired` datetime DEFAULT NULL,
											`retire_reason` varchar(255) DEFAULT NULL,
											`uuid` char(38) NOT NULL,
											PRIMARY KEY (`diagnosis_attribute_type_id`),
											UNIQUE KEY `name` (`name`),
											UNIQUE KEY `uuid` (`uuid`),
											KEY `diagnosis_attribute_type_creator_fk` (`creator`),
											KEY `diagnosis_attribute_type_changed_by_fk` (`changed_by`),
											KEY `diagnosis_attribute_type_retired_by_fk` (`retired_by`),
											CONSTRAINT `diagnosis_attribute_type_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
											CONSTRAINT `diagnosis_attribute_type_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
											CONSTRAINT `diagnosis_attribute_type_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diagnosis_attribute_type`
--

LOCK TABLES `diagnosis_attribute_type` WRITE;
/*!40000 ALTER TABLE `diagnosis_attribute_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `diagnosis_attribute_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drug`
--

DROP TABLE IF EXISTS `drug`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drug` (
						`drug_id` int(11) NOT NULL AUTO_INCREMENT,
						`concept_id` int(11) NOT NULL DEFAULT 0,
						`name` varchar(255) DEFAULT NULL,
						`combination` tinyint(1) NOT NULL DEFAULT 0,
						`dosage_form` int(11) DEFAULT NULL,
						`maximum_daily_dose` double DEFAULT NULL,
						`minimum_daily_dose` double DEFAULT NULL,
						`route` int(11) DEFAULT NULL,
						`creator` int(11) NOT NULL DEFAULT 0,
						`date_created` datetime NOT NULL,
						`retired` tinyint(1) NOT NULL DEFAULT 0,
						`changed_by` int(11) DEFAULT NULL,
						`date_changed` datetime DEFAULT NULL,
						`retired_by` int(11) DEFAULT NULL,
						`date_retired` datetime DEFAULT NULL,
						`retire_reason` varchar(255) DEFAULT NULL,
						`uuid` char(38) NOT NULL,
						`strength` varchar(255) DEFAULT NULL,
						`dose_limit_units` int(11) DEFAULT NULL,
						PRIMARY KEY (`drug_id`),
						UNIQUE KEY `uuid_drug` (`uuid`),
						KEY `dosage_form_concept` (`dosage_form`),
						KEY `drug_changed_by` (`changed_by`),
						KEY `drug_creator` (`creator`),
						KEY `drug_dose_limit_units_fk` (`dose_limit_units`),
						KEY `drug_retired_by` (`retired_by`),
						KEY `primary_drug_concept` (`concept_id`),
						KEY `route_concept` (`route`),
						CONSTRAINT `dosage_form_concept` FOREIGN KEY (`dosage_form`) REFERENCES `concept` (`concept_id`),
						CONSTRAINT `drug_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
						CONSTRAINT `drug_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
						CONSTRAINT `drug_dose_limit_units_fk` FOREIGN KEY (`dose_limit_units`) REFERENCES `concept` (`concept_id`),
						CONSTRAINT `drug_retired_by` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`),
						CONSTRAINT `primary_drug_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
						CONSTRAINT `route_concept` FOREIGN KEY (`route`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drug`
--

LOCK TABLES `drug` WRITE;
/*!40000 ALTER TABLE `drug` DISABLE KEYS */;
/*!40000 ALTER TABLE `drug` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drug_ingredient`
--

DROP TABLE IF EXISTS `drug_ingredient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drug_ingredient` (
								   `drug_id` int(11) NOT NULL,
								   `ingredient_id` int(11) NOT NULL,
								   `uuid` char(38) NOT NULL,
								   `strength` double DEFAULT NULL,
								   `units` int(11) DEFAULT NULL,
								   PRIMARY KEY (`drug_id`,`ingredient_id`),
								   UNIQUE KEY `uuid_drug_ingredient` (`uuid`),
								   KEY `drug_ingredient_ingredient_id_fk` (`ingredient_id`),
								   KEY `drug_ingredient_units_fk` (`units`),
								   CONSTRAINT `drug_ingredient_drug_id_fk` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`drug_id`),
								   CONSTRAINT `drug_ingredient_ingredient_id_fk` FOREIGN KEY (`ingredient_id`) REFERENCES `concept` (`concept_id`),
								   CONSTRAINT `drug_ingredient_units_fk` FOREIGN KEY (`units`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drug_ingredient`
--

LOCK TABLES `drug_ingredient` WRITE;
/*!40000 ALTER TABLE `drug_ingredient` DISABLE KEYS */;
/*!40000 ALTER TABLE `drug_ingredient` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drug_order`
--

DROP TABLE IF EXISTS `drug_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drug_order` (
							  `order_id` int(11) NOT NULL DEFAULT 0,
							  `drug_inventory_id` int(11) DEFAULT NULL,
							  `dose` double DEFAULT NULL,
							  `as_needed` tinyint(1) DEFAULT 0,
							  `dosing_type` varchar(255) DEFAULT NULL,
							  `quantity` double DEFAULT NULL,
							  `as_needed_condition` varchar(255) DEFAULT NULL,
							  `num_refills` int(11) DEFAULT NULL,
							  `dosing_instructions` text DEFAULT NULL,
							  `duration` int(11) DEFAULT NULL,
							  `duration_units` int(11) DEFAULT NULL,
							  `quantity_units` int(11) DEFAULT NULL,
							  `route` int(11) DEFAULT NULL,
							  `dose_units` int(11) DEFAULT NULL,
							  `frequency` int(11) DEFAULT NULL,
							  `brand_name` varchar(255) DEFAULT NULL,
							  `dispense_as_written` tinyint(1) NOT NULL DEFAULT 0,
							  `drug_non_coded` varchar(255) DEFAULT NULL,
							  PRIMARY KEY (`order_id`),
							  KEY `drug_order_dose_units` (`dose_units`),
							  KEY `drug_order_duration_units_fk` (`duration_units`),
							  KEY `drug_order_frequency_fk` (`frequency`),
							  KEY `drug_order_quantity_units` (`quantity_units`),
							  KEY `drug_order_route_fk` (`route`),
							  KEY `inventory_item` (`drug_inventory_id`),
							  CONSTRAINT `drug_order_dose_units` FOREIGN KEY (`dose_units`) REFERENCES `concept` (`concept_id`),
							  CONSTRAINT `drug_order_duration_units_fk` FOREIGN KEY (`duration_units`) REFERENCES `concept` (`concept_id`),
							  CONSTRAINT `drug_order_frequency_fk` FOREIGN KEY (`frequency`) REFERENCES `order_frequency` (`order_frequency_id`),
							  CONSTRAINT `drug_order_quantity_units` FOREIGN KEY (`quantity_units`) REFERENCES `concept` (`concept_id`),
							  CONSTRAINT `drug_order_route_fk` FOREIGN KEY (`route`) REFERENCES `concept` (`concept_id`),
							  CONSTRAINT `extends_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
							  CONSTRAINT `inventory_item` FOREIGN KEY (`drug_inventory_id`) REFERENCES `drug` (`drug_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drug_order`
--

LOCK TABLES `drug_order` WRITE;
/*!40000 ALTER TABLE `drug_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `drug_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drug_reference_map`
--

DROP TABLE IF EXISTS `drug_reference_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drug_reference_map` (
									  `drug_reference_map_id` int(11) NOT NULL AUTO_INCREMENT,
									  `drug_id` int(11) NOT NULL,
									  `term_id` int(11) NOT NULL,
									  `concept_map_type` int(11) NOT NULL,
									  `creator` int(11) NOT NULL,
									  `date_created` datetime NOT NULL,
									  `retired` tinyint(1) NOT NULL DEFAULT 0,
									  `retired_by` int(11) DEFAULT NULL,
									  `date_retired` datetime DEFAULT NULL,
									  `retire_reason` varchar(255) DEFAULT NULL,
									  `changed_by` int(11) DEFAULT NULL,
									  `date_changed` datetime DEFAULT NULL,
									  `uuid` char(38) NOT NULL,
									  PRIMARY KEY (`drug_reference_map_id`),
									  UNIQUE KEY `uuid_drug_reference_map` (`uuid`),
									  KEY `concept_map_type_for_drug_reference_map` (`concept_map_type`),
									  KEY `concept_reference_term_for_drug_reference_map` (`term_id`),
									  KEY `drug_for_drug_reference_map` (`drug_id`),
									  KEY `drug_reference_map_creator` (`creator`),
									  KEY `user_who_changed_drug_reference_map` (`changed_by`),
									  KEY `user_who_retired_drug_reference_map` (`retired_by`),
									  CONSTRAINT `concept_map_type_for_drug_reference_map` FOREIGN KEY (`concept_map_type`) REFERENCES `concept_map_type` (`concept_map_type_id`),
									  CONSTRAINT `concept_reference_term_for_drug_reference_map` FOREIGN KEY (`term_id`) REFERENCES `concept_reference_term` (`concept_reference_term_id`),
									  CONSTRAINT `drug_for_drug_reference_map` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`drug_id`),
									  CONSTRAINT `drug_reference_map_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									  CONSTRAINT `user_who_changed_drug_reference_map` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									  CONSTRAINT `user_who_retired_drug_reference_map` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drug_reference_map`
--

LOCK TABLES `drug_reference_map` WRITE;
/*!40000 ALTER TABLE `drug_reference_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `drug_reference_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `encounter`
--

DROP TABLE IF EXISTS `encounter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `encounter` (
							 `encounter_id` int(11) NOT NULL AUTO_INCREMENT,
							 `encounter_type` int(11) NOT NULL,
							 `patient_id` int(11) NOT NULL DEFAULT 0,
							 `location_id` int(11) DEFAULT NULL,
							 `form_id` int(11) DEFAULT NULL,
							 `encounter_datetime` datetime NOT NULL,
							 `creator` int(11) NOT NULL DEFAULT 0,
							 `date_created` datetime NOT NULL,
							 `voided` tinyint(1) NOT NULL DEFAULT 0,
							 `voided_by` int(11) DEFAULT NULL,
							 `date_voided` datetime DEFAULT NULL,
							 `void_reason` varchar(255) DEFAULT NULL,
							 `changed_by` int(11) DEFAULT NULL,
							 `date_changed` datetime DEFAULT NULL,
							 `visit_id` int(11) DEFAULT NULL,
							 `uuid` char(38) NOT NULL,
							 PRIMARY KEY (`encounter_id`),
							 UNIQUE KEY `uuid_encounter` (`uuid`),
							 KEY `encounter_changed_by` (`changed_by`),
							 KEY `encounter_datetime_idx` (`encounter_datetime`),
							 KEY `encounter_form` (`form_id`),
							 KEY `encounter_ibfk_1` (`creator`),
							 KEY `encounter_location` (`location_id`),
							 KEY `encounter_patient` (`patient_id`),
							 KEY `encounter_type_id` (`encounter_type`),
							 KEY `encounter_visit_id_fk` (`visit_id`),
							 KEY `user_who_voided_encounter` (`voided_by`),
							 CONSTRAINT `encounter_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
							 CONSTRAINT `encounter_form` FOREIGN KEY (`form_id`) REFERENCES `form` (`form_id`),
							 CONSTRAINT `encounter_ibfk_1` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
							 CONSTRAINT `encounter_location` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`),
							 CONSTRAINT `encounter_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE,
							 CONSTRAINT `encounter_type_id` FOREIGN KEY (`encounter_type`) REFERENCES `encounter_type` (`encounter_type_id`),
							 CONSTRAINT `encounter_visit_id_fk` FOREIGN KEY (`visit_id`) REFERENCES `visit` (`visit_id`),
							 CONSTRAINT `user_who_voided_encounter` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `encounter`
--

LOCK TABLES `encounter` WRITE;
/*!40000 ALTER TABLE `encounter` DISABLE KEYS */;
/*!40000 ALTER TABLE `encounter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `encounter_diagnosis`
--

DROP TABLE IF EXISTS `encounter_diagnosis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `encounter_diagnosis` (
									   `diagnosis_id` int(11) NOT NULL AUTO_INCREMENT,
									   `diagnosis_coded` int(11) DEFAULT NULL,
									   `diagnosis_non_coded` varchar(255) DEFAULT NULL,
									   `diagnosis_coded_name` int(11) DEFAULT NULL,
									   `encounter_id` int(11) NOT NULL,
									   `patient_id` int(11) NOT NULL,
									   `condition_id` int(11) DEFAULT NULL,
									   `certainty` varchar(255) NOT NULL,
									   `dx_rank` int(11) DEFAULT NULL,
									   `uuid` char(38) NOT NULL,
									   `creator` int(11) NOT NULL,
									   `date_created` datetime NOT NULL,
									   `changed_by` int(11) DEFAULT NULL,
									   `date_changed` datetime DEFAULT NULL,
									   `voided` tinyint(1) NOT NULL DEFAULT 0,
									   `voided_by` int(11) DEFAULT NULL,
									   `date_voided` datetime DEFAULT NULL,
									   `void_reason` varchar(255) DEFAULT NULL,
									   `form_namespace_and_path` varchar(255) DEFAULT NULL,
									   PRIMARY KEY (`diagnosis_id`),
									   UNIQUE KEY `uuid_encounter_diagnosis` (`uuid`),
									   KEY `encounter_diagnosis_changed_by_fk` (`changed_by`),
									   KEY `encounter_diagnosis_coded_fk` (`diagnosis_coded`),
									   KEY `encounter_diagnosis_coded_name_fk` (`diagnosis_coded_name`),
									   KEY `encounter_diagnosis_condition_id_fk` (`condition_id`),
									   KEY `encounter_diagnosis_creator_fk` (`creator`),
									   KEY `encounter_diagnosis_encounter_id_fk` (`encounter_id`),
									   KEY `encounter_diagnosis_patient_fk` (`patient_id`),
									   KEY `encounter_diagnosis_voided_by_fk` (`voided_by`),
									   CONSTRAINT `encounter_diagnosis_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									   CONSTRAINT `encounter_diagnosis_coded_fk` FOREIGN KEY (`diagnosis_coded`) REFERENCES `concept` (`concept_id`),
									   CONSTRAINT `encounter_diagnosis_coded_name_fk` FOREIGN KEY (`diagnosis_coded_name`) REFERENCES `concept_name` (`concept_name_id`),
									   CONSTRAINT `encounter_diagnosis_condition_id_fk` FOREIGN KEY (`condition_id`) REFERENCES `conditions` (`condition_id`),
									   CONSTRAINT `encounter_diagnosis_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									   CONSTRAINT `encounter_diagnosis_encounter_id_fk` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
									   CONSTRAINT `encounter_diagnosis_patient_fk` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
									   CONSTRAINT `encounter_diagnosis_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `encounter_diagnosis`
--

LOCK TABLES `encounter_diagnosis` WRITE;
/*!40000 ALTER TABLE `encounter_diagnosis` DISABLE KEYS */;
/*!40000 ALTER TABLE `encounter_diagnosis` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `encounter_provider`
--

DROP TABLE IF EXISTS `encounter_provider`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `encounter_provider` (
									  `encounter_provider_id` int(11) NOT NULL AUTO_INCREMENT,
									  `encounter_id` int(11) NOT NULL,
									  `provider_id` int(11) NOT NULL,
									  `encounter_role_id` int(11) NOT NULL,
									  `creator` int(11) NOT NULL DEFAULT 0,
									  `date_created` datetime NOT NULL,
									  `changed_by` int(11) DEFAULT NULL,
									  `date_changed` datetime DEFAULT NULL,
									  `voided` tinyint(1) NOT NULL DEFAULT 0,
									  `date_voided` datetime DEFAULT NULL,
									  `voided_by` int(11) DEFAULT NULL,
									  `void_reason` varchar(255) DEFAULT NULL,
									  `uuid` char(38) NOT NULL,
									  PRIMARY KEY (`encounter_provider_id`),
									  UNIQUE KEY `uuid_encounter_provider` (`uuid`),
									  KEY `encounter_id_fk` (`encounter_id`),
									  KEY `encounter_provider_changed_by` (`changed_by`),
									  KEY `encounter_provider_creator` (`creator`),
									  KEY `encounter_provider_voided_by` (`voided_by`),
									  KEY `encounter_role_id_fk` (`encounter_role_id`),
									  KEY `provider_id_fk` (`provider_id`),
									  CONSTRAINT `encounter_id_fk` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
									  CONSTRAINT `encounter_provider_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									  CONSTRAINT `encounter_provider_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									  CONSTRAINT `encounter_provider_voided_by` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
									  CONSTRAINT `encounter_role_id_fk` FOREIGN KEY (`encounter_role_id`) REFERENCES `encounter_role` (`encounter_role_id`),
									  CONSTRAINT `provider_id_fk` FOREIGN KEY (`provider_id`) REFERENCES `provider` (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `encounter_provider`
--

LOCK TABLES `encounter_provider` WRITE;
/*!40000 ALTER TABLE `encounter_provider` DISABLE KEYS */;
/*!40000 ALTER TABLE `encounter_provider` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `encounter_role`
--

DROP TABLE IF EXISTS `encounter_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `encounter_role` (
								  `encounter_role_id` int(11) NOT NULL AUTO_INCREMENT,
								  `name` varchar(255) NOT NULL,
								  `description` varchar(1024) DEFAULT NULL,
								  `creator` int(11) NOT NULL,
								  `date_created` datetime NOT NULL,
								  `changed_by` int(11) DEFAULT NULL,
								  `date_changed` datetime DEFAULT NULL,
								  `retired` tinyint(1) NOT NULL DEFAULT 0,
								  `retired_by` int(11) DEFAULT NULL,
								  `date_retired` datetime DEFAULT NULL,
								  `retire_reason` varchar(255) DEFAULT NULL,
								  `uuid` char(38) NOT NULL,
								  PRIMARY KEY (`encounter_role_id`),
								  UNIQUE KEY `encounter_role_name` (`name`),
								  UNIQUE KEY `uuid_encounter_role` (`uuid`),
								  KEY `encounter_role_changed_by_fk` (`changed_by`),
								  KEY `encounter_role_creator_fk` (`creator`),
								  KEY `encounter_role_retired_by_fk` (`retired_by`),
								  CONSTRAINT `encounter_role_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
								  CONSTRAINT `encounter_role_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								  CONSTRAINT `encounter_role_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `encounter_role`
--

LOCK TABLES `encounter_role` WRITE;
/*!40000 ALTER TABLE `encounter_role` DISABLE KEYS */;
INSERT INTO `encounter_role` VALUES (1,'Unknown','Unknown encounter role for legacy providers with no encounter role set',1,'2011-08-18 14:00:00',NULL,NULL,0,NULL,NULL,NULL,'a0b03050-c99b-11e0-9572-0800200c9a66');
/*!40000 ALTER TABLE `encounter_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `encounter_type`
--

DROP TABLE IF EXISTS `encounter_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `encounter_type` (
								  `encounter_type_id` int(11) NOT NULL AUTO_INCREMENT,
								  `name` varchar(50) NOT NULL DEFAULT '',
								  `description` text DEFAULT NULL,
								  `creator` int(11) NOT NULL DEFAULT 0,
								  `date_created` datetime NOT NULL,
								  `retired` tinyint(1) NOT NULL DEFAULT 0,
								  `retired_by` int(11) DEFAULT NULL,
								  `date_retired` datetime DEFAULT NULL,
								  `retire_reason` varchar(255) DEFAULT NULL,
								  `uuid` char(38) NOT NULL,
								  `edit_privilege` varchar(255) DEFAULT NULL,
								  `view_privilege` varchar(255) DEFAULT NULL,
								  `changed_by` int(11) DEFAULT NULL,
								  `date_changed` datetime DEFAULT NULL,
								  PRIMARY KEY (`encounter_type_id`),
								  UNIQUE KEY `encounter_type_name` (`name`),
								  UNIQUE KEY `uuid_encounter_type` (`uuid`),
								  KEY `encounter_type_changed_by` (`changed_by`),
								  KEY `encounter_type_retired_status` (`retired`),
								  KEY `privilege_which_can_edit_encounter_type` (`edit_privilege`),
								  KEY `privilege_which_can_view_encounter_type` (`view_privilege`),
								  KEY `user_who_created_type` (`creator`),
								  KEY `user_who_retired_encounter_type` (`retired_by`),
								  CONSTRAINT `encounter_type_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
								  CONSTRAINT `privilege_which_can_edit_encounter_type` FOREIGN KEY (`edit_privilege`) REFERENCES `privilege` (`privilege`),
								  CONSTRAINT `privilege_which_can_view_encounter_type` FOREIGN KEY (`view_privilege`) REFERENCES `privilege` (`privilege`),
								  CONSTRAINT `user_who_created_type` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								  CONSTRAINT `user_who_retired_encounter_type` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `encounter_type`
--

LOCK TABLES `encounter_type` WRITE;
/*!40000 ALTER TABLE `encounter_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `encounter_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `field`
--

DROP TABLE IF EXISTS `field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `field` (
						 `field_id` int(11) NOT NULL AUTO_INCREMENT,
						 `name` varchar(255) NOT NULL DEFAULT '',
						 `description` text DEFAULT NULL,
						 `field_type` int(11) DEFAULT NULL,
						 `concept_id` int(11) DEFAULT NULL,
						 `table_name` varchar(50) DEFAULT NULL,
						 `attribute_name` varchar(50) DEFAULT NULL,
						 `default_value` text DEFAULT NULL,
						 `select_multiple` tinyint(1) NOT NULL DEFAULT 0,
						 `creator` int(11) NOT NULL DEFAULT 0,
						 `date_created` datetime NOT NULL,
						 `changed_by` int(11) DEFAULT NULL,
						 `date_changed` datetime DEFAULT NULL,
						 `retired` tinyint(1) NOT NULL DEFAULT 0,
						 `retired_by` int(11) DEFAULT NULL,
						 `date_retired` datetime DEFAULT NULL,
						 `retire_reason` varchar(255) DEFAULT NULL,
						 `uuid` char(38) NOT NULL,
						 PRIMARY KEY (`field_id`),
						 UNIQUE KEY `uuid_field` (`uuid`),
						 KEY `concept_for_field` (`concept_id`),
						 KEY `field_retired_status` (`retired`),
						 KEY `type_of_field` (`field_type`),
						 KEY `user_who_changed_field` (`changed_by`),
						 KEY `user_who_created_field` (`creator`),
						 KEY `user_who_retired_field` (`retired_by`),
						 CONSTRAINT `concept_for_field` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
						 CONSTRAINT `type_of_field` FOREIGN KEY (`field_type`) REFERENCES `field_type` (`field_type_id`),
						 CONSTRAINT `user_who_changed_field` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
						 CONSTRAINT `user_who_created_field` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
						 CONSTRAINT `user_who_retired_field` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `field`
--

LOCK TABLES `field` WRITE;
/*!40000 ALTER TABLE `field` DISABLE KEYS */;
/*!40000 ALTER TABLE `field` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `field_answer`
--

DROP TABLE IF EXISTS `field_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `field_answer` (
								`field_id` int(11) NOT NULL DEFAULT 0,
								`answer_id` int(11) NOT NULL DEFAULT 0,
								`creator` int(11) NOT NULL DEFAULT 0,
								`date_created` datetime NOT NULL,
								`uuid` char(38) NOT NULL,
								PRIMARY KEY (`field_id`,`answer_id`),
								UNIQUE KEY `uuid_field_answer` (`uuid`),
								KEY `field_answer_concept` (`answer_id`),
								KEY `user_who_created_field_answer` (`creator`),
								CONSTRAINT `answers_for_field` FOREIGN KEY (`field_id`) REFERENCES `field` (`field_id`),
								CONSTRAINT `field_answer_concept` FOREIGN KEY (`answer_id`) REFERENCES `concept` (`concept_id`),
								CONSTRAINT `user_who_created_field_answer` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `field_answer`
--

LOCK TABLES `field_answer` WRITE;
/*!40000 ALTER TABLE `field_answer` DISABLE KEYS */;
/*!40000 ALTER TABLE `field_answer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `field_type`
--

DROP TABLE IF EXISTS `field_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `field_type` (
							  `field_type_id` int(11) NOT NULL AUTO_INCREMENT,
							  `name` varchar(50) DEFAULT NULL,
							  `description` text DEFAULT NULL,
							  `is_set` tinyint(1) NOT NULL DEFAULT 0,
							  `creator` int(11) NOT NULL DEFAULT 0,
							  `date_created` datetime NOT NULL,
							  `uuid` char(38) NOT NULL,
							  PRIMARY KEY (`field_type_id`),
							  UNIQUE KEY `uuid_field_type` (`uuid`),
							  KEY `user_who_created_field_type` (`creator`),
							  CONSTRAINT `user_who_created_field_type` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `field_type`
--

LOCK TABLES `field_type` WRITE;
/*!40000 ALTER TABLE `field_type` DISABLE KEYS */;
INSERT INTO `field_type` VALUES (1,'Concept','',0,1,'2005-02-22 00:00:00','8d5e7d7c-c2cc-11de-8d13-0010c6dffd0f'),(2,'Database element','',0,1,'2005-02-22 00:00:00','8d5e8196-c2cc-11de-8d13-0010c6dffd0f'),(3,'Set of Concepts','',1,1,'2005-02-22 00:00:00','8d5e836c-c2cc-11de-8d13-0010c6dffd0f'),(4,'Miscellaneous Set','',1,1,'2005-02-22 00:00:00','8d5e852e-c2cc-11de-8d13-0010c6dffd0f'),(5,'Section','',1,1,'2005-02-22 00:00:00','8d5e86fa-c2cc-11de-8d13-0010c6dffd0f');
/*!40000 ALTER TABLE `field_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `form`
--

DROP TABLE IF EXISTS `form`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `form` (
						`form_id` int(11) NOT NULL AUTO_INCREMENT,
						`name` varchar(255) NOT NULL DEFAULT '',
						`version` varchar(50) NOT NULL DEFAULT '',
						`build` int(11) DEFAULT NULL,
						`published` tinyint(1) NOT NULL DEFAULT 0,
						`xslt` text DEFAULT NULL,
						`template` text DEFAULT NULL,
						`description` text DEFAULT NULL,
						`encounter_type` int(11) DEFAULT NULL,
						`creator` int(11) NOT NULL DEFAULT 0,
						`date_created` datetime NOT NULL,
						`changed_by` int(11) DEFAULT NULL,
						`date_changed` datetime DEFAULT NULL,
						`retired` tinyint(1) NOT NULL DEFAULT 0,
						`retired_by` int(11) DEFAULT NULL,
						`date_retired` datetime DEFAULT NULL,
						`retired_reason` varchar(255) DEFAULT NULL,
						`uuid` char(38) NOT NULL,
						PRIMARY KEY (`form_id`),
						UNIQUE KEY `uuid_form` (`uuid`),
						KEY `form_encounter_type` (`encounter_type`),
						KEY `form_published_and_retired_index` (`published`,`retired`),
						KEY `form_published_index` (`published`),
						KEY `form_retired_index` (`retired`),
						KEY `user_who_created_form` (`creator`),
						KEY `user_who_last_changed_form` (`changed_by`),
						KEY `user_who_retired_form` (`retired_by`),
						CONSTRAINT `form_encounter_type` FOREIGN KEY (`encounter_type`) REFERENCES `encounter_type` (`encounter_type_id`),
						CONSTRAINT `user_who_created_form` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
						CONSTRAINT `user_who_last_changed_form` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
						CONSTRAINT `user_who_retired_form` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `form`
--

LOCK TABLES `form` WRITE;
/*!40000 ALTER TABLE `form` DISABLE KEYS */;
/*!40000 ALTER TABLE `form` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `form_field`
--

DROP TABLE IF EXISTS `form_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `form_field` (
							  `form_field_id` int(11) NOT NULL AUTO_INCREMENT,
							  `form_id` int(11) NOT NULL DEFAULT 0,
							  `field_id` int(11) NOT NULL DEFAULT 0,
							  `field_number` int(11) DEFAULT NULL,
							  `field_part` varchar(5) DEFAULT NULL,
							  `page_number` int(11) DEFAULT NULL,
							  `parent_form_field` int(11) DEFAULT NULL,
							  `min_occurs` int(11) DEFAULT NULL,
							  `max_occurs` int(11) DEFAULT NULL,
							  `required` tinyint(1) NOT NULL DEFAULT 0,
							  `changed_by` int(11) DEFAULT NULL,
							  `date_changed` datetime DEFAULT NULL,
							  `creator` int(11) NOT NULL DEFAULT 0,
							  `date_created` datetime NOT NULL,
							  `sort_weight` float DEFAULT NULL,
							  `uuid` char(38) NOT NULL,
							  PRIMARY KEY (`form_field_id`),
							  UNIQUE KEY `uuid_form_field` (`uuid`),
							  KEY `field_within_form` (`field_id`),
							  KEY `form_containing_field` (`form_id`),
							  KEY `form_field_hierarchy` (`parent_form_field`),
							  KEY `user_who_created_form_field` (`creator`),
							  KEY `user_who_last_changed_form_field` (`changed_by`),
							  CONSTRAINT `field_within_form` FOREIGN KEY (`field_id`) REFERENCES `field` (`field_id`),
							  CONSTRAINT `form_containing_field` FOREIGN KEY (`form_id`) REFERENCES `form` (`form_id`),
							  CONSTRAINT `form_field_hierarchy` FOREIGN KEY (`parent_form_field`) REFERENCES `form_field` (`form_field_id`),
							  CONSTRAINT `user_who_created_form_field` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
							  CONSTRAINT `user_who_last_changed_form_field` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `form_field`
--

LOCK TABLES `form_field` WRITE;
/*!40000 ALTER TABLE `form_field` DISABLE KEYS */;
/*!40000 ALTER TABLE `form_field` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `form_resource`
--

DROP TABLE IF EXISTS `form_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `form_resource` (
								 `form_resource_id` int(11) NOT NULL AUTO_INCREMENT,
								 `form_id` int(11) NOT NULL,
								 `name` varchar(255) NOT NULL,
								 `value_reference` text NOT NULL,
								 `datatype` varchar(255) DEFAULT NULL,
								 `datatype_config` text DEFAULT NULL,
								 `preferred_handler` varchar(255) DEFAULT NULL,
								 `handler_config` text DEFAULT NULL,
								 `uuid` char(38) NOT NULL,
								 `date_changed` datetime DEFAULT NULL,
								 `changed_by` int(11) DEFAULT NULL,
								 PRIMARY KEY (`form_resource_id`),
								 UNIQUE KEY `unique_form_and_name` (`form_id`,`name`),
								 UNIQUE KEY `uuid_form_resource` (`uuid`),
								 KEY `form_resource_changed_by` (`changed_by`),
								 CONSTRAINT `form_resource_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
								 CONSTRAINT `form_resource_form_fk` FOREIGN KEY (`form_id`) REFERENCES `form` (`form_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `form_resource`
--

LOCK TABLES `form_resource` WRITE;
/*!40000 ALTER TABLE `form_resource` DISABLE KEYS */;
/*!40000 ALTER TABLE `form_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `global_property`
--

DROP TABLE IF EXISTS `global_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `global_property` (
								   `property` varchar(255) NOT NULL DEFAULT '',
								   `property_value` text DEFAULT NULL,
								   `description` text DEFAULT NULL,
								   `uuid` char(38) NOT NULL,
								   `datatype` varchar(255) DEFAULT NULL,
								   `datatype_config` text DEFAULT NULL,
								   `preferred_handler` varchar(255) DEFAULT NULL,
								   `handler_config` text DEFAULT NULL,
								   `date_changed` datetime DEFAULT NULL,
								   `changed_by` int(11) DEFAULT NULL,
								   PRIMARY KEY (`property`),
								   UNIQUE KEY `uuid_global_property` (`uuid`),
								   KEY `global_property_changed_by` (`changed_by`),
								   CONSTRAINT `global_property_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `global_property`
--

LOCK TABLES `global_property` WRITE;
/*!40000 ALTER TABLE `global_property` DISABLE KEYS */;
INSERT INTO `global_property` VALUES ('allergy.allergen.ConceptClasses','Drug,MedSet','A comma-separated list of the allowed concept classes for the allergen field of the allergy dialog','eaf3f161-3752-412e-9a88-7bba5b702345',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.allergen.drug','162552AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the drug allergens concept','73901ac7-58af-4af7-ba24-84d4fa6cc5b9',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.allergen.environment','162554AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the environment allergens concept','11df86f4-9ac9-4ef0-9c27-794b6e80ac30',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.allergen.food','162553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the food allergens concept','7a5d3497-9451-43e3-9875-577401e7e3e8',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.otherNonCoded','5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the allergy other non coded concept','e58daa38-3811-44b1-b46e-576c33ef15c0',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.reactions','162555AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the allergy reactions concept','b31775b0-c9f7-4182-b5b3-8215057d0c36',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.severity.mild','1498AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the MILD severity concept','9cfc01ef-aed3-429f-afd7-5f5a85c1a8ee',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.severity.moderate','1499AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the MODERATE severity concept','48acac7b-3eb4-4aff-ae1b-19ce279ca7b6',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.severity.severe','1500AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the SEVERE severity concept','4fa87d11-8875-49d1-87d2-2750397670af',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.unknown','1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the allergy unknown concept','7e0827a8-d31b-4977-9b9a-fd8c2e2725f2',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.reaction.ConceptClasses','Symptom','A comma-separated list of the allowed concept classes for the reaction field of the allergy dialog','5362e179-b305-4f3d-a689-1171cfcd5cbc',NULL,NULL,NULL,NULL,NULL,NULL),('application.name','OpenMRS','The name of this application, as presented to the user, for example on the login and welcome pages.','e7681fbd-f513-40a2-bfdc-0c81c43842f4',NULL,NULL,NULL,NULL,NULL,NULL),('concept.defaultConceptMapType','NARROWER-THAN','Default concept map type which is used when no other is set','568e3a0f-77b2-4c9b-9509-d48dbc7c0957',NULL,NULL,NULL,NULL,NULL,NULL),('conceptDrug.dosageForm.conceptClasses',NULL,'A comma-separated list of the allowed concept classes for the dosage form field of the concept drug management form.','82290e93-eab7-40a3-8442-5169a9f2cacf',NULL,NULL,NULL,NULL,NULL,NULL),('conceptDrug.route.conceptClasses',NULL,'A comma-separated list of the allowed concept classes for the route field of the concept drug management form.','3ce0bad3-09e2-4aac-8359-abed0fc69490',NULL,NULL,NULL,NULL,NULL,NULL),('concepts.locked','false','if true, do not allow editing concepts','edeaadd0-3ecf-4f80-86c9-a11634cab259','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('concept_map_type_management.enable','false','Enables or disables management of concept map types','29b994cf-b5b6-4b08-8308-b0fbeb066407','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('dashboard.encounters.maximumNumberToShow','3','An integer which, if specified, would determine the maximum number of encounters to display on the encounter tab of the patient dashboard.','f8b9de03-d4cf-420c-805b-e216f7735c2f',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.encounters.providerDisplayRoles',NULL,'A comma-separated list of encounter roles (by name or id). Providers with these roles in an encounter will be displayed on the encounter tab of the patient dashboard.','8a5fe900-f468-44a1-a92b-844766e863a6',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.encounters.showEditLink','true','true/false whether or not to show the \'Edit Encounter\' link on the patient dashboard','1c3728bc-eb24-411b-adaf-960c6e2cf78c','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('dashboard.encounters.showEmptyFields','true','true/false whether or not to show empty fields on the \'View Encounter\' window','26f8a463-dbc3-4398-a224-ed3f8fbd2e37','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('dashboard.encounters.showViewLink','true','true/false whether or not to show the \'View Encounter\' link on the patient dashboard','3ba232cb-c046-41b0-9ef7-38bece3a9520','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('dashboard.encounters.usePages','smart','true/false/smart on how to show the pages on the \'View Encounter\' window.  \'smart\' means that if > 50% of the fields have page numbers defined, show data in pages','042ff68c-a802-4e13-b4d2-195fc2ce776f',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.header.programs_to_show',NULL,'List of programs to show Enrollment details of in the patient header. (Should be an ordered comma-separated list of program_ids or names.)','b03b7a65-001e-4280-9b0e-c37ff229db96',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.header.workflows_to_show',NULL,'List of programs to show Enrollment details of in the patient header. List of workflows to show current status of in the patient header. These will only be displayed if they belong to a program listed above. (Should be a comma-separated list of program_workflow_ids.)','81839be8-43cb-43fd-b1ba-7905d0bb85e9',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.metadata.caseConversion',NULL,'Indicates which type automatic case conversion is applied to program/workflow/state in the patient dashboard. Valid values: lowercase, uppercase, capitalize. If empty no conversion is applied.','9c2e3714-9885-46e2-928c-1a5ec7a8ca28',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.overview.showConcepts',NULL,'Comma delimited list of concepts ids to show on the patient dashboard overview tab','d84d155b-a2cc-439e-a4a3-ab8a8363bd9f',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.regimen.displayDrugSetIds','ANTIRETROVIRAL DRUGS,TUBERCULOSIS TREATMENT DRUGS','Drug sets that appear on the Patient Dashboard Regimen tab. Comma separated list of name of concepts that are defined as drug sets.','263d4083-3c2c-499b-98d7-53bd83f9b7de',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.regimen.displayFrequencies','7 days/week,6 days/week,5 days/week,4 days/week,3 days/week,2 days/week,1 days/week','Frequency of a drug order that appear on the Patient Dashboard. Comma separated list of name of concepts that are defined as drug frequencies.','69142fe9-886d-4a2f-839e-81f590f4595a',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.relationships.show_types',NULL,'Types of relationships separated by commas.  Doctor/Patient,Parent/Child','f21c41a3-d997-46df-8656-366f51299539',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.showPatientName','false','Whether or not to display the patient name in the patient dashboard title. Note that enabling this could be security risk if multiple users operate on the same computer.','397a09b8-3fd4-49d9-898c-6c4d2f75a032','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('datePicker.weekStart','0','First day of the week in the date picker. Domingo/Dimanche/Sunday:0  Lunes/Lundi/Monday:1','d0352c10-93d4-4f6e-a845-439ccab5f598',NULL,NULL,NULL,NULL,NULL,NULL),('default_locale','en','Specifies the default locale. You can specify both the language code(ISO-639) and the country code(ISO-3166), e.g. \'en_GB\' or just country: e.g. \'en\'','a152b28a-08dd-438f-a9d4-e409b95c1add',NULL,NULL,NULL,NULL,NULL,NULL),('default_location','Unknown Location','The name of the location to use as a system default','8dc531c6-f132-4ae0-ac33-0feb276e50b0',NULL,NULL,NULL,NULL,NULL,NULL),('default_theme',NULL,'Default theme for users.  OpenMRS ships with themes of \'green\', \'orange\', \'purple\', and \'legacy\'','4733581d-d84e-4373-b019-9e4d3ee552e6',NULL,NULL,NULL,NULL,NULL,NULL),('drugOrder.drugOther',NULL,'Specifies the uuid of the concept which represents drug other non coded','4fe96ffd-c390-4167-92aa-3a9c54057430',NULL,NULL,NULL,NULL,NULL,NULL),('drugOrder.requireDrug','false','Set to value true if you need to specify a formulation(Drug) when creating a drug order.','ea3b521f-4eb7-441c-bfaf-5ac523c2fe0c',NULL,NULL,NULL,NULL,NULL,NULL),('drugOrder.requireOutpatientQuantity','true','true/false whether to require quantity, quantityUnits, and numRefills for outpatient drug orders','8b9e5f86-7c17-4c4e-ae46-0afca4fc977a',NULL,NULL,NULL,NULL,NULL,NULL),('encounterForm.obsSortOrder','number','The sort order for the obs listed on the encounter edit form.  \'number\' sorts on the associated numbering from the form schema.  \'weight\' sorts on the order displayed in the form schema.','fc3671ef-c777-4878-87a7-977b59d5669a',NULL,NULL,NULL,NULL,NULL,NULL),('EncounterType.encounterTypes.locked','false','saving, retiring or deleting an Encounter Type is not permitted, if true','999c7e01-c3bc-44b7-800d-42fa489fec7b','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('FormEntry.enableDashboardTab','true','true/false whether or not to show a Form Entry tab on the patient dashboard','0d252df1-aaf4-4981-8c0a-d2ce53787b60','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('FormEntry.enableOnEncounterTab','false','true/false whether or not to show a Enter Form button on the encounters tab of the patient dashboard','090f38fa-dd24-4ee3-985d-9fac0bc62ccb','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('forms.locked','false','Set to a value of true if you do not want any changes to be made on forms, else set to false.','3c20dd4d-04e7-4a41-aa24-da61bb90d169',NULL,NULL,NULL,NULL,NULL,NULL),('graph.color.absolute','rgb(20,20,20)','Color of the \'invalid\' section of numeric graphs on the patient dashboard.','be51a832-ad5b-470c-9a4d-6faf116df6dc',NULL,NULL,NULL,NULL,NULL,NULL),('graph.color.critical','rgb(200,0,0)','Color of the \'critical\' section of numeric graphs on the patient dashboard.','685f48e8-1f69-4ad7-8534-00400047d763',NULL,NULL,NULL,NULL,NULL,NULL),('graph.color.normal','rgb(255,126,0)','Color of the \'normal\' section of numeric graphs on the patient dashboard.','f96c8671-0d6c-4467-b587-0ae50cc4ae56',NULL,NULL,NULL,NULL,NULL,NULL),('gzip.enabled','false','Set to \'true\' to turn on OpenMRS\'s gzip filter, and have the webapp compress data before sending it to any client that supports it. Generally use this if you are running Tomcat standalone. If you are running Tomcat behind Apache, then you\'d want to use Apache to do gzip compression.','499e2875-5a0b-49c0-8263-b21c3b484cd6','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('hl7_archive.dir','hl7_archives','The default name or absolute path for the folder where to write the hl7_in_archives.','8993648a-375c-452f-bcd6-0ff8707cb0eb',NULL,NULL,NULL,NULL,NULL,NULL),('hl7_processor.ignore_missing_patient_non_local','false','If true, hl7 messages for patients that are not found and are non-local will silently be dropped/ignored','b1ede925-6265-4257-a5ed-dbd2419c20ed','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('host.url',NULL,'The URL to redirect to after requesting for a password reset. Always provide a place holder in this url with name {activationKey}, it will get substituted by the actual activation key.','256fbf7d-546f-49f3-9cd0-566d7f4ff1db',NULL,NULL,NULL,NULL,NULL,NULL),('layout.address.format','<org.openmrs.layout.address.AddressTemplate>     <nameMappings class=\"properties\">       <property name=\"postalCode\" value=\"Location.postalCode\"/>       <property name=\"address2\" value=\"Location.address2\"/>       <property name=\"address1\" value=\"Location.address1\"/>       <property name=\"country\" value=\"Location.country\"/>       <property name=\"stateProvince\" value=\"Location.stateProvince\"/>       <property name=\"cityVillage\" value=\"Location.cityVillage\"/>     </nameMappings>     <sizeMappings class=\"properties\">       <property name=\"postalCode\" value=\"10\"/>       <property name=\"address2\" value=\"40\"/>       <property name=\"address1\" value=\"40\"/>       <property name=\"country\" value=\"10\"/>       <property name=\"stateProvince\" value=\"10\"/>       <property name=\"cityVillage\" value=\"10\"/>     </sizeMappings>     <lineByLineFormat>       <string>address1</string>       <string>address2</string>       <string>cityVillage stateProvince country postalCode</string>     </lineByLineFormat>    <requiredElements>\\\\n\" + \" </requiredElements>\\\\n\" + \" </org.openmrs.layout.address.AddressTemplate>','XML description of address formats','d506f19e-1220-4fda-802c-326870fe64c2',NULL,NULL,NULL,NULL,NULL,NULL),('layout.name.format','short','Format in which to display the person names.  Valid values are short, long','5d64f396-f660-4ff5-9e03-e120bddc4591',NULL,NULL,NULL,NULL,NULL,NULL),('locale.allowed.list','en, en_GB, es, fr, it, pt','Comma delimited list of locales allowed for use on system','ed779482-9150-4c4e-9124-561db7bcb420',NULL,NULL,NULL,NULL,NULL,NULL),('location.field.style','default','Type of widget to use for location fields','4910b45c-0f32-4d6b-bc0b-9c5a80a2f8eb',NULL,NULL,NULL,NULL,NULL,NULL),('log.layout','%p - %C{1}.%M(%L) |%d{ISO8601}| %m%n','A log layout pattern which is used by the OpenMRS file appender.','bb444578-e5bf-43b0-8b50-907165321ee8',NULL,NULL,NULL,NULL,NULL,NULL),('log.level','org.openmrs.api:info','Logging levels for log4j2.xml. Valid format is class:level,class:level. If class not specified, \'org.openmrs.api\' presumed. Valid levels are trace, debug, info, warn, error or fatal','089162f3-038f-45f5-a045-4584e4e8c877',NULL,NULL,NULL,NULL,NULL,NULL),('log.location',NULL,'A directory where the OpenMRS log file appender is stored. The log file name is \'openmrs.log\'.','1940b15e-cc1c-4b85-9bac-6897415f99bc',NULL,NULL,NULL,NULL,NULL,NULL),('login.url','login.htm','Responsible for defining the Authentication URL','54f496dd-b6a3-4dfa-8cc1-65f79b1b8015',NULL,NULL,NULL,NULL,NULL,NULL),('mail.debug','false','true/false whether to print debugging information during mailing','b5a07cb7-fbcb-4bc8-8489-70376577f4cf',NULL,NULL,NULL,NULL,NULL,NULL),('mail.default_content_type','text/plain','Content type to append to the mail messages','e4229af5-604a-4853-891c-df434d35f74d',NULL,NULL,NULL,NULL,NULL,NULL),('mail.from','info@openmrs.org','Email address to use as the default from address','9618cc7b-27cf-4677-acb5-b47c4f0629ea',NULL,NULL,NULL,NULL,NULL,NULL),('mail.password','test','Password for the SMTP user (if smtp_auth is enabled)','e48b2aa7-f360-481b-a604-383668abd27a',NULL,NULL,NULL,NULL,NULL,NULL),('mail.smtp.starttls.enable','false','Set to true to enable TLS encryption, else set to false','ca2a6d7d-9387-42cd-b845-4eccecd2a606',NULL,NULL,NULL,NULL,NULL,NULL),('mail.smtp_auth','false','true/false whether the smtp host requires authentication','cec5bf48-dcea-45e8-aa07-03f2addbf553',NULL,NULL,NULL,NULL,NULL,NULL),('mail.smtp_host','localhost','SMTP host name','7f26cfaa-9e8c-4fb9-a561-fe84e2c21983',NULL,NULL,NULL,NULL,NULL,NULL),('mail.smtp_port','25','SMTP port','17114e64-67c8-441d-9d37-4da26fe8f0d1',NULL,NULL,NULL,NULL,NULL,NULL),('mail.transport_protocol','smtp','Transport protocol for the messaging engine. Valid values: smtp','814e6dd6-192c-458d-afa5-c8079258600f',NULL,NULL,NULL,NULL,NULL,NULL),('mail.user','test','Username of the SMTP user (if smtp_auth is enabled)','99045648-3319-495a-80f0-dde97405154c',NULL,NULL,NULL,NULL,NULL,NULL),('minSearchCharacters','2','Number of characters user must input before searching is started.','c584e421-cd40-4ffd-9943-d4ef1ef75eb0',NULL,NULL,NULL,NULL,NULL,NULL),('module_repository_folder','modules','Name of the folder in which to store the modules','1f3763b2-fd77-4056-90e4-212b871ff305',NULL,NULL,NULL,NULL,NULL,NULL),('newPatientForm.relationships',NULL,'Comma separated list of the RelationshipTypes to show on the new/short patient form.  The list is defined like \'3a, 4b, 7a\'.  The number is the RelationshipTypeId and the \'a\' vs \'b\' part is which side of the relationship is filled in by the user.','f5ce96ed-6463-4cd1-a020-3792d44963b3',NULL,NULL,NULL,NULL,NULL,NULL),('new_patient_form.showRelationships','false','true/false whether or not to show the relationship editor on the addPatient.htm screen','6e5994b3-520d-4caf-a9fb-d61b19562523','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('obs.complex_obs_dir','complex_obs','Default directory for storing complex obs.','4e3ca595-2506-4548-be8b-5d18223918b3',NULL,NULL,NULL,NULL,NULL,NULL),('order.drugDispensingUnitsConceptUuid',NULL,'Specifies the uuid of the concept set where its members represent the possible drug dispensing units','38aac0e9-c759-4d19-ae54-24290e263c58',NULL,NULL,NULL,NULL,NULL,NULL),('order.drugDosingUnitsConceptUuid',NULL,'Specifies the uuid of the concept set where its members represent the possible drug dosing units','dad63678-79df-4e35-b71d-4d1a48dc6911',NULL,NULL,NULL,NULL,NULL,NULL),('order.drugRoutesConceptUuid',NULL,'Specifies the uuid of the concept set where its members represent the possible drug routes','c01d7641-125d-459e-8116-71366e8cdf7d',NULL,NULL,NULL,NULL,NULL,NULL),('order.durationUnitsConceptUuid',NULL,'Specifies the uuid of the concept set where its members represent the possible duration units','b28c2607-688e-4edd-a850-9c8cbacb57a6',NULL,NULL,NULL,NULL,NULL,NULL),('order.nextOrderNumberSeed','1','The next order number available for assignment','1740c7ef-0630-425f-bbc6-15979cb59cde',NULL,NULL,NULL,NULL,NULL,NULL),('order.orderNumberGeneratorBeanId',NULL,'Specifies spring bean id of the order generator to use when assigning order numbers','a75b7de3-91b7-4b50-89a8-428f1335cad9',NULL,NULL,NULL,NULL,NULL,NULL),('order.testSpecimenSourcesConceptUuid',NULL,'Specifies the uuid of the concept set where its members represent the possible test specimen sources','feec06fa-62b5-4d51-bd8e-4f081bf555e2',NULL,NULL,NULL,NULL,NULL,NULL),('patient.defaultPatientIdentifierValidator','org.openmrs.patient.impl.LuhnIdentifierValidator','This property sets the default patient identifier validator.  The default validator is only used in a handful of (mostly legacy) instances.  For example, it\'s used to generate the isValidCheckDigit calculated column and to append the string \"(default)\" to the name of the default validator on the editPatientIdentifierType form.','04762247-6d55-48d3-a2ad-36b40b29035e',NULL,NULL,NULL,NULL,NULL,NULL),('patient.headerAttributeTypes',NULL,'A comma delimited list of PersonAttributeType names that will be shown on the patient dashboard','4609457a-9d21-4ec2-80ce-c83a200cbd00',NULL,NULL,NULL,NULL,NULL,NULL),('patient.identifierPrefix',NULL,'This property is only used if patient.identifierRegex is empty.  The string here is prepended to the sql indentifier search string.  The sql becomes \"... where identifier like \'<PREFIX><QUERY STRING><SUFFIX>\';\".  Typically this value is either a percent sign (%) or empty.','8d9239b3-06d1-465a-aae1-da73018bb9db',NULL,NULL,NULL,NULL,NULL,NULL),('patient.identifierRegex',NULL,'WARNING: Using this search property can cause a drop in mysql performance with large patient sets.  A MySQL regular expression for the patient identifier search strings.  The @SEARCH@ string is replaced at runtime with the user\'s search string.  An empty regex will cause a simply \'like\' sql search to be used. Example: ^0*@SEARCH@([A-Z]+-[0-9])?$','a62944ef-6a15-42f6-9257-f99fb2c8c0ad',NULL,NULL,NULL,NULL,NULL,NULL),('patient.identifierSearchPattern',NULL,'If this is empty, the regex or suffix/prefix search is used.  Comma separated list of identifiers to check.  Allows for faster searching of multiple options rather than the slow regex. e.g. @SEARCH@,0@SEARCH@,@SEARCH-1@-@CHECKDIGIT@,0@SEARCH-1@-@CHECKDIGIT@ would turn a request for \"4127\" into a search for \"in (\'4127\',\'04127\',\'412-7\',\'0412-7\')\"','b1489289-c8be-4235-b219-09e81ffbe7b1',NULL,NULL,NULL,NULL,NULL,NULL),('patient.identifierSuffix',NULL,'This property is only used if patient.identifierRegex is empty.  The string here is prepended to the sql indentifier search string.  The sql becomes \"... where identifier like \'<PREFIX><QUERY STRING><SUFFIX>\';\".  Typically this value is either a percent sign (%) or empty.','6df515d8-297c-437e-94c6-2858c3c955ee',NULL,NULL,NULL,NULL,NULL,NULL),('patient.listingAttributeTypes',NULL,'A comma delimited list of PersonAttributeType names that should be displayed for patients in _lists_','e2057b0f-12c6-4a78-945d-0b08181f7977',NULL,NULL,NULL,NULL,NULL,NULL),('patient.nameValidationRegex',NULL,'Names of the patients must pass this regex. Eg : ^[a-zA-Z \\\\-]+$ contains only english alphabet letters, spaces, and hyphens. A value of .* or the empty string means no validation is done.','33215153-23f5-4e00-967b-243b81017851',NULL,NULL,NULL,NULL,NULL,NULL),('patient.viewingAttributeTypes',NULL,'A comma delimited list of PersonAttributeType names that should be displayed for patients when _viewing individually_','5805e57f-8f9a-4f3c-a0b1-a02a2504ef72',NULL,NULL,NULL,NULL,NULL,NULL),('patientIdentifierSearch.matchMode','EXACT','Specifies how patient identifiers are matched while searching for a patient. Valid values are \'EXACT, \'ANYWHERE\' or \'START\'. Defaults to \'EXACT\' if missing or invalid value is present.','f57be38c-490d-452e-9a52-1e7450fe7b61',NULL,NULL,NULL,NULL,NULL,NULL),('patientIdentifierTypes.locked','false','Set to a value of true if you do not want allow editing patient identifier types, else set to false.','1a799da5-986c-47f8-9685-b95cc5932dd5',NULL,NULL,NULL,NULL,NULL,NULL),('patientSearch.matchMode','START','Specifies how patient names are matched while searching patient. Valid values are \'ANYWHERE\' or \'START\'. Defaults to start if missing or invalid value is present.','43c4f838-51c1-495b-b474-b597b1fca708',NULL,NULL,NULL,NULL,NULL,NULL),('patient_identifier.importantTypes',NULL,'A comma delimited list of PatientIdentifier names : PatientIdentifier locations that will be displayed on the patient dashboard.  E.g.: TRACnet ID:Rwanda,ELDID:Kenya','782440dd-ab92-4c9b-aa5e-28a9734917a3',NULL,NULL,NULL,NULL,NULL,NULL),('person.attributeSearchMatchMode','EXACT','Specifies how person attributes are matched while searching person. Valid values are \'ANYWHERE\' or \'EXACT\'. Defaults to exact if missing or invalid value is present.','638d4774-7867-4cb4-a8a7-e67d962636a9',NULL,NULL,NULL,NULL,NULL,NULL),('person.searchMaxResults','1000','The maximum number of results returned by patient searches','6637deaf-0024-46bb-b999-e6d8c892e357',NULL,NULL,NULL,NULL,NULL,NULL),('personAttributeTypes.locked','false','Set to a value of true if you do not want allow editing person attribute types, else set to false.','a9ba4279-7ce0-4638-b4e0-86745c189375',NULL,NULL,NULL,NULL,NULL,NULL),('provider.unknownProviderUuid',NULL,'Specifies the uuid of the Unknown Provider account','d50ba61e-31ad-4d11-a674-49efca9c8b25',NULL,NULL,NULL,NULL,NULL,NULL),('providerSearch.matchMode','EXACT','Specifies how provider identifiers are matched while searching for providers. Valid values are START,EXACT, END or ANYWHERE','87cf37be-07a6-4650-b644-df9926bf1e01',NULL,NULL,NULL,NULL,NULL,NULL),('reportProblem.url','http://errors.openmrs.org/scrap','The openmrs url where to submit bug reports','9bb8a464-69cc-4ae1-b8dc-03d926161350',NULL,NULL,NULL,NULL,NULL,NULL),('scheduler.password','test','Password for the OpenMRS user that will perform the scheduler activities','f2f53ffd-c0c7-4123-bb7e-cfbd61ea1dcf',NULL,NULL,NULL,NULL,NULL,NULL),('scheduler.username','admin','Username for the OpenMRS user that will perform the scheduler activities','00dc0a29-3fa0-4a36-aa07-7f40ae7645d0',NULL,NULL,NULL,NULL,NULL,NULL),('search.caseSensitiveDatabaseStringComparison','false','Indicates whether database string comparison is case sensitive or not. Setting this to false for MySQL with a case insensitive collation improves search performance.','89272e39-8689-4428-8c2c-11ec2273f159',NULL,NULL,NULL,NULL,NULL,NULL),('search.indexVersion','7','Indicates the index version. If it is blank, the index needs to be rebuilt.','b3b885c5-d0f1-4ff1-93b0-995a331f128b',NULL,NULL,NULL,NULL,NULL,NULL),('searchWidget.batchSize','200','The maximum number of search results that are returned by an ajax call','39a2b99f-9f6a-4d7f-9857-8d4031f248cb',NULL,NULL,NULL,NULL,NULL,NULL),('searchWidget.dateDisplayFormat',NULL,'Date display format to be used to display the date somewhere in the UI i.e the search widgets and autocompletes','403c8cb8-4ea0-47ff-b296-d3c2acbbc10c',NULL,NULL,NULL,NULL,NULL,NULL),('searchWidget.maximumResults','2000','Specifies the maximum number of results to return from a single search in the search widgets','696452cf-0171-4755-9b64-912d5080a79c',NULL,NULL,NULL,NULL,NULL,NULL),('searchWidget.runInSerialMode','false','Specifies whether the search widgets should make ajax requests in serial or parallel order, a value of true is appropriate for implementations running on a slow network connection and vice versa','3ef448c6-3625-40df-8cba-d5bb0e92eaee','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('searchWidget.searchDelayInterval','300','Specifies time interval in milliseconds when searching, between keyboard keyup event and triggering the search off, should be higher if most users are slow when typing so as to minimise the load on the server','7668f58f-5178-46c4-ba8f-30ab409665b6',NULL,NULL,NULL,NULL,NULL,NULL),('security.allowedFailedLoginsBeforeLockout','7','Maximum number of failed logins allowed after which username is locked out','6ab3a5b8-dd9e-4f97-ab14-117e04c8a401',NULL,NULL,NULL,NULL,NULL,NULL),('security.passwordCannotMatchUsername','true','Configure whether passwords must not match user\'s username or system id','2dc79f65-ec53-4349-b9ec-82340cfbc1ca','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('security.passwordCustomRegex',NULL,'Configure a custom regular expression that a password must match','0e86a20d-e25c-47b3-bace-438f40a1be9c',NULL,NULL,NULL,NULL,NULL,NULL),('security.passwordMinimumLength','8','Configure the minimum length required of all passwords','5ff5841d-fa50-4532-8205-1808927b9259',NULL,NULL,NULL,NULL,NULL,NULL),('security.passwordRequiresDigit','true','Configure whether passwords must contain at least one digit','5431d53a-f6ca-4af5-9840-06a73ef14de1','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('security.passwordRequiresNonDigit','true','Configure whether passwords must contain at least one non-digit','50b7640d-b2c8-4bf4-81d4-730323df9dd3','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('security.passwordRequiresUpperAndLowerCase','true','Configure whether passwords must contain both upper and lower case characters','7ffcb927-1341-4113-b2ed-df9a35801a55','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('security.validTime','600000','Specifies the duration of time in seconds for which a password reset token is valid, the default value is 10 minutes and the allowed values range from 1 minute to 12hrs','28764d15-f64a-49e0-807f-a309f9a99f41',NULL,NULL,NULL,NULL,NULL,NULL),('user.headerAttributeTypes',NULL,'A comma delimited list of PersonAttributeType names that will be shown on the user dashboard. (not used in v1.5)','4f3f5fea-30eb-4e83-b7ae-9ae8704c09e0',NULL,NULL,NULL,NULL,NULL,NULL),('user.listingAttributeTypes',NULL,'A comma delimited list of PersonAttributeType names that should be displayed for users in _lists_','ecf4f41a-6690-460e-b765-616b10260a02',NULL,NULL,NULL,NULL,NULL,NULL),('user.requireEmailAsUsername','false','Indicates whether a username must be a valid e-mail or not.','01bc73a8-08c0-4723-9358-9d02df17a93c','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('user.viewingAttributeTypes',NULL,'A comma delimited list of PersonAttributeType names that should be displayed for users when _viewing individually_','14f1663b-19b3-44b2-bf8a-c30431e19598',NULL,NULL,NULL,NULL,NULL,NULL),('use_patient_attribute.healthCenter','false','Indicates whether or not the \'health center\' attribute is shown when viewing/searching for patients','7dea235a-3efd-40f3-9b3d-e64042043013','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('use_patient_attribute.mothersName','false','Indicates whether or not mother\'s name is able to be added/viewed for a patient','0ffb5e2b-9671-41e6-a504-88d02a24ccba','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('validation.disable','false','Disables validation of OpenMRS Objects. Only takes affect on next restart. Warning: only do this is you know what you are doing!','346a9dc0-5f6a-462d-9065-85bcc7b62203',NULL,NULL,NULL,NULL,NULL,NULL),('visits.allowOverlappingVisits','true','true/false whether or not to allow visits of a given patient to overlap','e0376867-c667-4c81-b82f-f65ec7bfd5c7','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('visits.assignmentHandler','org.openmrs.api.handler.ExistingVisitAssignmentHandler','Set to the name of the class responsible for assigning encounters to visits.','c90dd1db-0611-451a-94c4-ac4b0f44035d',NULL,NULL,NULL,NULL,NULL,NULL),('visits.autoCloseVisitType',NULL,'comma-separated list of the visit type(s) to automatically close','7abb62f4-81a6-4145-ac0d-f01034d666ce',NULL,NULL,NULL,NULL,NULL,NULL),('visits.enabled','true','Set to true to enable the Visits feature. This will replace the \'Encounters\' tab with a \'Visits\' tab on the dashboard.','9c594c46-482d-48b3-9643-05641111fc9f','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('visits.encounterTypeToVisitTypeMapping',NULL,'Specifies how encounter types are mapped to visit types when automatically assigning encounters to visits. e.g 1:1, 2:1, 3:2 in the format encounterTypeId:visitTypeId or encounterTypeUuid:visitTypeUuid or a combination of encounter/visit type uuids and ids e.g 1:759799ab-c9a5-435e-b671-77773ada74e4','c84f289c-3869-4683-9b14-ebc47c374212',NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `global_property` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hl7_in_archive`
--

DROP TABLE IF EXISTS `hl7_in_archive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hl7_in_archive` (
								  `hl7_in_archive_id` int(11) NOT NULL AUTO_INCREMENT,
								  `hl7_source` int(11) NOT NULL DEFAULT 0,
								  `hl7_source_key` varchar(255) DEFAULT NULL,
								  `hl7_data` text NOT NULL,
								  `date_created` datetime NOT NULL,
								  `message_state` int(11) DEFAULT 2,
								  `uuid` char(38) NOT NULL,
								  PRIMARY KEY (`hl7_in_archive_id`),
								  UNIQUE KEY `uuid_hl7_in_archive` (`uuid`),
								  KEY `hl7_in_archive_message_state_idx` (`message_state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hl7_in_archive`
--

LOCK TABLES `hl7_in_archive` WRITE;
/*!40000 ALTER TABLE `hl7_in_archive` DISABLE KEYS */;
/*!40000 ALTER TABLE `hl7_in_archive` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hl7_in_error`
--

DROP TABLE IF EXISTS `hl7_in_error`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hl7_in_error` (
								`hl7_in_error_id` int(11) NOT NULL AUTO_INCREMENT,
								`hl7_source` int(11) NOT NULL DEFAULT 0,
								`hl7_source_key` text DEFAULT NULL,
								`hl7_data` text NOT NULL,
								`error` varchar(255) NOT NULL DEFAULT '',
								`error_details` mediumtext DEFAULT NULL,
								`date_created` datetime NOT NULL,
								`uuid` char(38) NOT NULL,
								PRIMARY KEY (`hl7_in_error_id`),
								UNIQUE KEY `uuid_hl7_in_error` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hl7_in_error`
--

LOCK TABLES `hl7_in_error` WRITE;
/*!40000 ALTER TABLE `hl7_in_error` DISABLE KEYS */;
/*!40000 ALTER TABLE `hl7_in_error` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hl7_in_queue`
--

DROP TABLE IF EXISTS `hl7_in_queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hl7_in_queue` (
								`hl7_in_queue_id` int(11) NOT NULL AUTO_INCREMENT,
								`hl7_source` int(11) NOT NULL DEFAULT 0,
								`hl7_source_key` text DEFAULT NULL,
								`hl7_data` text NOT NULL,
								`message_state` int(11) NOT NULL DEFAULT 0,
								`date_processed` datetime DEFAULT NULL,
								`error_msg` text DEFAULT NULL,
								`date_created` datetime DEFAULT NULL,
								`uuid` char(38) NOT NULL,
								PRIMARY KEY (`hl7_in_queue_id`),
								UNIQUE KEY `uuid_hl7_in_queue` (`uuid`),
								KEY `hl7_source_with_queue` (`hl7_source`),
								CONSTRAINT `hl7_source_with_queue` FOREIGN KEY (`hl7_source`) REFERENCES `hl7_source` (`hl7_source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hl7_in_queue`
--

LOCK TABLES `hl7_in_queue` WRITE;
/*!40000 ALTER TABLE `hl7_in_queue` DISABLE KEYS */;
/*!40000 ALTER TABLE `hl7_in_queue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hl7_source`
--

DROP TABLE IF EXISTS `hl7_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hl7_source` (
							  `hl7_source_id` int(11) NOT NULL AUTO_INCREMENT,
							  `name` varchar(255) NOT NULL DEFAULT '',
							  `description` text DEFAULT NULL,
							  `creator` int(11) NOT NULL DEFAULT 0,
							  `date_created` datetime NOT NULL,
							  `uuid` char(38) NOT NULL,
							  PRIMARY KEY (`hl7_source_id`),
							  UNIQUE KEY `uuid_hl7_source` (`uuid`),
							  KEY `user_who_created_hl7_source` (`creator`),
							  CONSTRAINT `user_who_created_hl7_source` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hl7_source`
--

LOCK TABLES `hl7_source` WRITE;
/*!40000 ALTER TABLE `hl7_source` DISABLE KEYS */;
INSERT INTO `hl7_source` VALUES (1,'LOCAL','',1,'2006-09-01 00:00:00','8d6b8bb6-c2cc-11de-8d13-0010c6dffd0f');
/*!40000 ALTER TABLE `hl7_source` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `liquibasechangelog`
--

DROP TABLE IF EXISTS `liquibasechangelog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `liquibasechangelog` (
									  `ID` varchar(255) NOT NULL,
									  `AUTHOR` varchar(255) NOT NULL,
									  `FILENAME` varchar(255) NOT NULL,
									  `DATEEXECUTED` datetime NOT NULL,
									  `ORDEREXECUTED` int(11) NOT NULL,
									  `EXECTYPE` varchar(10) NOT NULL,
									  `MD5SUM` varchar(35) DEFAULT NULL,
									  `DESCRIPTION` varchar(255) DEFAULT NULL,
									  `COMMENTS` varchar(255) DEFAULT NULL,
									  `TAG` varchar(255) DEFAULT NULL,
									  `LIQUIBASE` varchar(20) DEFAULT NULL,
									  `CONTEXTS` varchar(255) DEFAULT NULL,
									  `LABELS` varchar(255) DEFAULT NULL,
									  `DEPLOYMENT_ID` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `liquibasechangelog`
--

LOCK TABLES `liquibasechangelog` WRITE;
/*!40000 ALTER TABLE `liquibasechangelog` DISABLE KEYS */;
INSERT INTO `liquibasechangelog` VALUES ('0','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',1,'MARK_RAN','8:12a6014284bbf4978e29e2f37d967125','customChange','Run the old sqldiff file to get database up to the 1.4.0.20 version if needed. (Requires \'mysql\' to be on the PATH)',NULL,'4.4.3',NULL,NULL,NULL),('20090214-2247','isherman','liquibase-update-to-latest.xml','2022-11-15 11:07:52',2,'MARK_RAN','8:210e9534dd7f6f5c1439050673176abb','sql','Add weight and cd4 to patientGraphConcepts user property (using standard sql)',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-cohort','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',3,'MARK_RAN','8:1e5f3c602f6c8b49623da909396f7154','update tableName=cohort','Generating UUIDs for all rows in cohort table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-concept','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',4,'MARK_RAN','8:301c96f73711b5296d5fd53335aff029','update tableName=concept','Generating UUIDs for all rows in concept table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-concept_answer','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',5,'MARK_RAN','8:b1994ad6889ef6553b40541c6f23a84d','update tableName=concept_answer','Generating UUIDs for all rows in concept_answer table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-concept_class','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',6,'MARK_RAN','8:a4b942db55d477f1fb0673bc055cdd44','update tableName=concept_class','Generating UUIDs for all rows in concept_class table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-concept_datatype','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',7,'MARK_RAN','8:afa76f459448d03a687d42373a90a348','update tableName=concept_datatype','Generating UUIDs for all rows in concept_datatype table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-concept_description','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',8,'MARK_RAN','8:153f544380ee0f05a31a1689a10859f1','update tableName=concept_description','Generating UUIDs for all rows in concept_description table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-concept_map','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',9,'MARK_RAN','8:91ca042fca4f139f53693f5d849bc0f9','update tableName=concept_map','Generating UUIDs for all rows in concept_map table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-concept_name','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',10,'MARK_RAN','8:03db9dcf2fd1ddc2e131a281a2727d2e','update tableName=concept_name','Generating UUIDs for all rows in concept_name table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-concept_name_tag','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',11,'MARK_RAN','8:c5b4c26a905cba386cc0838c3ac4b379','update tableName=concept_name_tag','Generating UUIDs for all rows in concept_name_tag table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-concept_proposal','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',12,'MARK_RAN','8:9318f8d61823201e1f4dee3fbdd3902f','update tableName=concept_proposal','Generating UUIDs for all rows in concept_proposal table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-concept_set','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',13,'MARK_RAN','8:57e8431d8a6b84330191258c910a0c29','update tableName=concept_set','Generating UUIDs for all rows in concept_set table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-concept_source','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',14,'MARK_RAN','8:5248e5c92bb4d1a122872722271c8b5d','update tableName=concept_source','Generating UUIDs for all rows in concept_source table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-concept_state_conversion','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',15,'MARK_RAN','8:90f354810f33ccf3a9aa5ecb61ce83e6','update tableName=concept_state_conversion','Generating UUIDs for all rows in concept_state_conversion table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-drug','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',16,'MARK_RAN','8:6e56e6c541fc1d44468ff5b5d4b45c93','update tableName=drug','Generating UUIDs for all rows in drug table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-encounter','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',17,'MARK_RAN','8:46bfdeb4071cd38b1323792f591109ee','update tableName=encounter','Generating UUIDs for all rows in encounter table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-encounter_type','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',18,'MARK_RAN','8:03361c5db151527184d9c54cf60a4a34','update tableName=encounter_type','Generating UUIDs for all rows in encounter_type table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-field','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',19,'MARK_RAN','8:5390d737b16093fd25c8f77203ceb98c','update tableName=field','Generating UUIDs for all rows in field table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-field_answer','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',20,'MARK_RAN','8:983524d767a006e6c21610a6c264cb65','update tableName=field_answer','Generating UUIDs for all rows in field_answer table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-field_type','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',21,'MARK_RAN','8:d15ce9d72c72f44c715be41e07f648a0','update tableName=field_type','Generating UUIDs for all rows in field_type table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-form','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',22,'MARK_RAN','8:6d8e6a2c1f640109c374156b03867fc2','update tableName=form','Generating UUIDs for all rows in form table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-form_field','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',23,'MARK_RAN','8:1bd95d4812302b4174578ea40e3dc196','update tableName=form_field','Generating UUIDs for all rows in form_field table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-global_property','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',24,'MARK_RAN','8:b3d79833def68ed6649f0cf68aff3514','update tableName=global_property','Generating UUIDs for all rows in global_property table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-hl7_in_archive','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',25,'MARK_RAN','8:0c67e57fe83d7b1cdca0e4c8e486d158','update tableName=hl7_in_archive','Generating UUIDs for all rows in hl7_in_archive table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-hl7_in_error','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',26,'MARK_RAN','8:d4bf29241a5c22e66c1c65ad583d7bb0','update tableName=hl7_in_error','Generating UUIDs for all rows in hl7_in_error table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-hl7_in_queue','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',27,'MARK_RAN','8:3e1b5ba36b59aae0e10f62623b78a58a','update tableName=hl7_in_queue','Generating UUIDs for all rows in hl7_in_queue table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-hl7_source','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',28,'MARK_RAN','8:208d340bd76d9c4ae380fcf3148d467c','update tableName=hl7_source','Generating UUIDs for all rows in hl7_source table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-location','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',29,'MARK_RAN','8:cec1c9879a7fcc9c60f284b2a306f883','update tableName=location','Generating UUIDs for all rows in location table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-location_tag','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',30,'MARK_RAN','8:ccd781790e6fce24ae813361539308f5','update tableName=location_tag','Generating UUIDs for all rows in location_tag table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-note','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',31,'MARK_RAN','8:ee3aa7f7826dc993fabf3359f06d17a9','update tableName=note','Generating UUIDs for all rows in note table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-notification_alert','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',32,'MARK_RAN','8:e6cb8ffbd327beee8ff2f1ad424e412f','update tableName=notification_alert','Generating UUIDs for all rows in notification_alert table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-notification_template','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',33,'MARK_RAN','8:107e1507cf0cb30e23d0509b336fe346','update tableName=notification_template','Generating UUIDs for all rows in notification_template table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-obs','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',34,'MARK_RAN','8:973322829828ffa60f9f4f6b1c4f232f','update tableName=obs','Generating UUIDs for all rows in obs table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-order_type','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',35,'MARK_RAN','8:d3235e472ac4229d44e71486d00e2653','update tableName=order_type','Generating UUIDs for all rows in order_type table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-orders','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',36,'MARK_RAN','8:1e408f995f276ab8d0881f782548702e','update tableName=orders','Generating UUIDs for all rows in orders table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-patient_identifier','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',37,'MARK_RAN','8:602a225c34101a21a01ef892422117ca','update tableName=patient_identifier','Generating UUIDs for all rows in patient_identifier table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-patient_identifier_type','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',38,'MARK_RAN','8:3e51460056f21cda05668e1bfe193b4c','update tableName=patient_identifier_type','Generating UUIDs for all rows in patient_identifier_type table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-patient_program','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',39,'MARK_RAN','8:3e221878d50f2e8002ae99d0014f2c56','update tableName=patient_program','Generating UUIDs for all rows in patient_program table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-patient_state','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',40,'MARK_RAN','8:33c02c81a05cfd878de7fa1d47fb6018','update tableName=patient_state','Generating UUIDs for all rows in patient_state table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-person','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',41,'MARK_RAN','8:0e99bfe3bc7565413f782ba4bee1e70f','update tableName=person','Generating UUIDs for all rows in person table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-person_address','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',42,'MARK_RAN','8:04291c5bd7f9b5f85ceb4864d22456c6','update tableName=person_address','Generating UUIDs for all rows in person_address table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-person_attribute','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',43,'MARK_RAN','8:04d1c05c210ef6ec16e43466fc8f5d38','update tableName=person_attribute','Generating UUIDs for all rows in person_attribute table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-person_attribute_type','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',44,'MARK_RAN','8:7f96ac2405ba35978d5b68e4e8ffa8d5','update tableName=person_attribute_type','Generating UUIDs for all rows in person_attribute_type table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-person_name','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',45,'MARK_RAN','8:96860b46e32160ee40c8456882e1248d','update tableName=person_name','Generating UUIDs for all rows in person_name table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-privilege','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',46,'MARK_RAN','8:226c0ffefa7e77f763d798d66c417179','update tableName=privilege','Generating UUIDs for all rows in privilege table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-program','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',47,'MARK_RAN','8:ea52e8613c60e1e91ea243e1ac20d5d4','update tableName=program','Generating UUIDs for all rows in program table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-program_workflow','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',48,'MARK_RAN','8:4150a317ec5e2f2f11c355d7e84c7ae5','update tableName=program_workflow','Generating UUIDs for all rows in program_workflow table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-program_workflow_state','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',49,'MARK_RAN','8:716788fa6a2de077412fabf86968afc3','update tableName=program_workflow_state','Generating UUIDs for all rows in program_workflow_state table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-relationship','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',50,'MARK_RAN','8:0e52ba612e60ff92355729d15ec05c6f','update tableName=relationship','Generating UUIDs for all rows in relationship table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-relationship_type','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',51,'MARK_RAN','8:da5f6fa85345a6fdf10ee1c672c70742','update tableName=relationship_type','Generating UUIDs for all rows in relationship_type table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-report_object','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',52,'MARK_RAN','8:69f81f0e4303fd3189fb46f269a6e8af','update tableName=report_object','Generating UUIDs for all rows in report_object table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-report_schema_xml','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',53,'MARK_RAN','8:ee495c298b9f331daa4530a05b944fe7','update tableName=report_schema_xml','Generating UUIDs for all rows in report_schema_xml table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-role','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',54,'MARK_RAN','8:dbf35a3f8e8e98de8be9a92913ac4caf','update tableName=role','Generating UUIDs for all rows in role table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('20090402-1516-serialized_object','bwolfe','liquibase-update-to-latest.xml','2022-11-15 11:07:52',55,'MARK_RAN','8:9cf4de5d376fb228193ffd9b9ea54f8d','update tableName=serialized_object','Generating UUIDs for all rows in serialized_object table via built in uuid function.',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',56,'EXECUTED','8:26c712081ff14d254a418ebd82bdef12','createTable tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-2','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',57,'EXECUTED','8:0bdfac753b3aaf9faf6659bcd4953d72','createTable tableName=allergy_reaction','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-3','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',58,'EXECUTED','8:abebca46ba45f81705f8d425a58dda1d','createTable tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-4','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',59,'EXECUTED','8:9d0d3bc5a01e33bb63c214613b50821b','createTable tableName=clob_datatype_storage','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-5','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',60,'EXECUTED','8:d3a1c43749263502f0605b8121234539','createTable tableName=cohort','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-6','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',61,'EXECUTED','8:b67d5613aa64cfbed0be3c5ed786bf3a','createTable tableName=cohort_member','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-7','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',62,'EXECUTED','8:988e7d997f558892702b11a05ba461f0','createTable tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-8','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',63,'EXECUTED','8:3b42c6d9985be5ea419d8fa12b5c0b2b','createTable tableName=concept_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-9','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',64,'EXECUTED','8:f28c4d20be3dca41248a562371fe59a6','createTable tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-10','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',65,'EXECUTED','8:6cfe965c0ea8b7a206167b53d1039d36','createTable tableName=concept_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-11','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',66,'EXECUTED','8:c99e43b8daedeec8bd400bc6462306dd','createTable tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-12','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',67,'EXECUTED','8:a373e8bb2d16ed1f2df7a00ae4795c17','createTable tableName=concept_complex','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-13','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',68,'EXECUTED','8:10a0089a5b2f1702e57c767182466964','createTable tableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-14','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',69,'EXECUTED','8:02386c61ea387caeb079743d1afe558f','createTable tableName=concept_description','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-15','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',70,'EXECUTED','8:da973073d464a80d4bb69983e8d7bbf5','createTable tableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-16','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',71,'EXECUTED','8:b50063f6441b065981b2d2fa9302a936','createTable tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-17','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',72,'EXECUTED','8:e4c9b8865a099372a763cac03f377726','createTable tableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-18','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',73,'EXECUTED','8:2ad1f3969d4447ec88134a92452372cc','createTable tableName=concept_name_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-19','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',74,'EXECUTED','8:7ae77ec670ef35cefa138fa12c795698','createTable tableName=concept_numeric','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-20','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',75,'EXECUTED','8:b84071a1813d442c1452c12d5ceaa3e5','createTable tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-21','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',76,'EXECUTED','8:32485f3a60b8038b6bec0697ec1925c8','createTable tableName=concept_proposal_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-22','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',77,'EXECUTED','8:b5b2f385e430b8673c701492280041fd','createTable tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-23','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',78,'EXECUTED','8:17163b204fcdc42e80da747cfaf13003','createTable tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-24','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',79,'EXECUTED','8:44ab9bb2e4cca6b861afbbf5e3f03de0','createTable tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-25','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',80,'EXECUTED','8:3ff7bb6d8be9217e49253dafeb053a01','createTable tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-26','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',81,'EXECUTED','8:518994244e192656d2284406f4390a41','createTable tableName=concept_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-27','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',82,'EXECUTED','8:ad0098726e9d8a26725a003313bd94c0','createTable tableName=concept_state_conversion','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-28','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',83,'EXECUTED','8:b51ab8476cea184afd24720263db89a5','createTable tableName=concept_stop_word','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-29','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',84,'EXECUTED','8:73deb3764ae3e0098cca2ecbebcb4344','createTable tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-30','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',85,'EXECUTED','8:2a13656ff0a815963a0457ada6145ed7','createTable tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-31','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',86,'EXECUTED','8:d22a193e9213d0acaf7afe55e5a49c9c','createTable tableName=drug_ingredient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-32','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',87,'EXECUTED','8:ae34b6165f64bea4c3440b72d8b3a1a0','createTable tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-33','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',88,'EXECUTED','8:d39ed02fdb23f817638e477b8dba2bd1','createTable tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-34','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',89,'EXECUTED','8:03e8003cfed638ecf60d1cf39f542d03','createTable tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-35','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',90,'EXECUTED','8:de38614bca47ae080ba3e440b66d1e43','createTable tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-36','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',91,'EXECUTED','8:10388258a58fa9f60a34093800e35a8d','createTable tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-37','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',92,'EXECUTED','8:ef762bf9e01d39ad97eec331e17dbfef','createTable tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-38','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',93,'EXECUTED','8:0017eef7a00c980a8281ed32e37b8b80','createTable tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-39','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',94,'EXECUTED','8:f704faea9d1e9481eded487d71275ab4','createTable tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-40','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',95,'EXECUTED','8:4aaca0c72edaf731840e0d762d80744d','createTable tableName=field_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-41','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',96,'EXECUTED','8:0bd0d8f993fd61918a35772bc64ef4a6','createTable tableName=field_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-42','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',97,'EXECUTED','8:1e8d4f0f271aa7080ceaaf4f8c655022','createTable tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-43','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',98,'EXECUTED','8:98c03ce6e1441f9aa43f1bd5628765fe','createTable tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-44','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',99,'EXECUTED','8:35e0d00275148d5d20443e281beed5d0','createTable tableName=form_resource','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-45','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',100,'EXECUTED','8:9bcd746f7bcca52643e473313c2d566f','createTable tableName=global_property','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-46','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',101,'EXECUTED','8:e9c5f2876f83da09b7e67134ec9966ea','createTable tableName=hl7_in_archive','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-47','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',102,'EXECUTED','8:d734f88fd306a6c21eade5276e703e7a','createTable tableName=hl7_in_error','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-48','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',103,'EXECUTED','8:8824cd3b13e03deac9a5974498ebea2b','createTable tableName=hl7_in_queue','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-49','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',104,'EXECUTED','8:52a1872d2be59ad89eab9b7b4e1c3b4e','createTable tableName=hl7_source','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-52','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',105,'EXECUTED','8:a0b1729b610d77c6816221c471565df6','createTable tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-53','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',106,'EXECUTED','8:21dff6f43e0e7d23c6726c47275132a5','createTable tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-54','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',107,'EXECUTED','8:e78fbfa8bc7a454a9016b7ca97e009c4','createTable tableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-55','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',108,'EXECUTED','8:d22f6049176c284124aa722298115a24','createTable tableName=location_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-56','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',109,'EXECUTED','8:f975333c0536c33eee965ef4c34efb31','createTable tableName=location_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-57','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',110,'EXECUTED','8:d17d80a5dd0d517f164f92ee719617b5','createTable tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-58','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',111,'EXECUTED','8:bfac6cd4c0a6cd94034fa0105adb21c1','createTable tableName=notification_alert','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-59','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',112,'EXECUTED','8:204bd233b229c37c2ac1ac5c377210c4','createTable tableName=notification_alert_recipient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-60','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',113,'EXECUTED','8:accb3c064e83a84bf8a49d77e676ff59','createTable tableName=notification_template','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-61','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',114,'EXECUTED','8:09baa1699e1a9bd86160c0d3bce0e42b','createTable tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-62','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',115,'EXECUTED','8:3f83c3c25dd59b8bd8f6a53c2b1b0950','createTable tableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-63','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',116,'EXECUTED','8:dd2b6eaed47c4b63a78ced37bc23ebe6','createTable tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-64','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',117,'EXECUTED','8:e2c1fba0dbd713c0cf6a951039ff0a3c','createTable tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-65','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',118,'EXECUTED','8:b5d5afb1a72a4996a812026fd64f31bf','createTable tableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-66','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',119,'EXECUTED','8:859ecfdf33d98692a3dbef0fee4bb073','createTable tableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-67','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',120,'EXECUTED','8:bf513c0a8a0b583f60b90bc62a9f6236','createTable tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-68','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',121,'EXECUTED','8:5a07973d35e2cc24f1dbb68dd6eccc48','createTable tableName=order_set_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-69','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',122,'EXECUTED','8:7efc90f9f9c9baeb46cf494b1f674355','createTable tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-70','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',123,'EXECUTED','8:1fb793dcdee62f398937e3833e2ce698','createTable tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-71','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',124,'EXECUTED','8:a6c4a23a3a4e796b50be4273ab3dbe84','createTable tableName=order_type_class_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-72','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',125,'EXECUTED','8:30986f6ac9058a0c70b36f158c7777b7','createTable tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-73','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',126,'EXECUTED','8:2f713a0e337c85c077c53d42abfcad88','createTable tableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-74','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',127,'EXECUTED','8:473d8a9785f768d2957acae4c7d6669c','createTable tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-75','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',128,'EXECUTED','8:eee165f2844f906c80ed4de8e582d6f9','createTable tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-76','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',129,'EXECUTED','8:e43818358d7395a5f6aed4a37727cf1d','createTable tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-77','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',130,'EXECUTED','8:821bcc3e0372734911c535c00ed66e8e','createTable tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-78','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',131,'EXECUTED','8:d3c1957cc259a769971a11adb559bb2d','createTable tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-79','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',132,'EXECUTED','8:22d30fdc5cd555c97bfe337c6e4482a9','createTable tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-80','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',133,'EXECUTED','8:0dda6d85e9a31ff95fa7849e3cdaf18e','createTable tableName=person_address','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-81','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',134,'EXECUTED','8:82b23f2950a3d8d972bf15d1d738be13','createTable tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-82','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',135,'EXECUTED','8:105d8355de2b2344af2cd918d779bdd8','createTable tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-83','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',136,'EXECUTED','8:46610271daa598aa68a790cc49b52e53','createTable tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-84','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',137,'EXECUTED','8:afb9789b20738fbf1920ba1fb9ee90aa','createTable tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-85','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',138,'EXECUTED','8:1b5de20b6f93874d0705c3eb6274ee6b','createTable tableName=privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-86','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',139,'EXECUTED','8:14c1b86f49f10be136c4e47be4998812','createTable tableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-87','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',140,'EXECUTED','8:f7de0ee2c752357d3f6eb0d40a8e3d28','createTable tableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-88','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',141,'EXECUTED','8:7013b6754c7ea01190acf4dc641016ef','createTable tableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-89','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',142,'EXECUTED','8:2166d98e3305b1a70b63df07c7399c65','createTable tableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-90','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:56',143,'EXECUTED','8:67b17554e334e147b350bbec83b12de2','createTable tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-91','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',144,'EXECUTED','8:2386c9f1e87ad3340f6484f8ca44718f','createTable tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-92','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',145,'EXECUTED','8:297a942d94cb6c388d4ff179d1d64ffc','createTable tableName=provider_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-93','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',146,'EXECUTED','8:870d6000836c7bda4eaad274cc246405','createTable tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-94','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',147,'EXECUTED','8:2946418ca683917584879dbd469be1d1','createTable tableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-95','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',148,'EXECUTED','8:35c2bc05383570caef43abb612746034','createTable tableName=report_object','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-96','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',149,'EXECUTED','8:9fcc554f574408d19ad460aa7e6021e6','createTable tableName=report_schema_xml','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-97','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',150,'EXECUTED','8:b63f9a6a5b31fff9be9201568697b0e1','createTable tableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-98','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',151,'EXECUTED','8:9bb9fd852e8009445c684b6fcf7c5bff','createTable tableName=role_privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-99','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',152,'EXECUTED','8:9af94c59c630b93a4b6d829e3fd16275','createTable tableName=role_role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-100','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',153,'EXECUTED','8:880ec6b4716ceee3604cad114f9488aa','createTable tableName=scheduler_task_config','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-101','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',154,'EXECUTED','8:3776cbd8675a3f636c5114ffab09110b','createTable tableName=scheduler_task_config_property','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-102','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',155,'EXECUTED','8:e25abb45de29bf7a4bc73806ca2c0cda','createTable tableName=serialized_object','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-103','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',156,'EXECUTED','8:96a49eb243f68b2567df271d4f293023','createTable tableName=test_order','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-104','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',157,'EXECUTED','8:344fc8699766bd923b3f0f6cbb3f9313','createTable tableName=user_property','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-105','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',158,'EXECUTED','8:048d763cc7964737faa57a9de4d740a2','createTable tableName=user_role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-106','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',159,'EXECUTED','8:1e9503c135a2da5303e07853ed188b3d','createTable tableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-107','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',160,'EXECUTED','8:a15cb7bbf04aa5ff782153047f56fc94','createTable tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-108','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',161,'EXECUTED','8:912b8fd020c7a302e907072e8af1a1f9','createTable tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-109','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',162,'EXECUTED','8:ae836f1886927ccb51ccd748785fc166','createTable tableName=visit_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-110','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',163,'EXECUTED','8:d02affbd103cf5b39bbfb42f0a876f35','createTable tableName=visit_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-111','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',164,'EXECUTED','8:785fa875154432f6a14919abbf246e4e','createIndex indexName=Unique_StopWord_Key, tableName=concept_stop_word','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-112','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',165,'EXECUTED','8:2b65da6d449adb71b3207069a7144795','createIndex indexName=address_for_person, tableName=person_address','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-113','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',166,'EXECUTED','8:66771b59765b7d074573fc820d0cd3fe','createIndex indexName=alert_creator, tableName=notification_alert','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-114','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',167,'EXECUTED','8:9ce55e5b29c279ba2bc2e4fc96557892','createIndex indexName=alert_date_to_expire_idx, tableName=notification_alert','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-115','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',168,'EXECUTED','8:3191e265a3b57b0521ac0f34acec54a3','createIndex indexName=alert_read_by_user, tableName=notification_alert_recipient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-116','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',169,'EXECUTED','8:025f604ceef39c1cfdec72aa97b97224','createIndex indexName=allergy_changed_by_fk, tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-117','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',170,'EXECUTED','8:21221492eb4e97daa9035b20a84f906a','createIndex indexName=allergy_coded_allergen_fk, tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-118','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',171,'EXECUTED','8:b1d6b67f44e93f9b02919352b5aacaad','createIndex indexName=allergy_creator_fk, tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-119','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',172,'EXECUTED','8:3802de1996f1d6d8960ff81b4e7a8490','createIndex indexName=allergy_patient_id_fk, tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-120','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',173,'EXECUTED','8:73c8f4df0e4b017640c1dbd16aa29dc7','createIndex indexName=allergy_reaction_allergy_id_fk, tableName=allergy_reaction','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-121','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',174,'EXECUTED','8:0c7772d6190cbac7f586dddedfddc53a','createIndex indexName=allergy_reaction_reaction_concept_id_fk, tableName=allergy_reaction','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-122','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',175,'EXECUTED','8:6d2ea59930c0679b8a433cd905864c51','createIndex indexName=allergy_severity_concept_id_fk, tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-123','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',176,'EXECUTED','8:1e2d9087a923a9c4a51fbc98c846bad7','createIndex indexName=allergy_voided_by_fk, tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-124','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',177,'EXECUTED','8:28dd409d4f30a39b7ad15eb987d52e10','createIndex indexName=answer, tableName=concept_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-125','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',178,'EXECUTED','8:dceed4605b21c692d6e15aebd5376752','createIndex indexName=answer_answer_drug_fk, tableName=concept_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-126','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',179,'EXECUTED','8:a7b64bba8111bdf055dd15e691603cc7','createIndex indexName=answer_concept, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-127','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',180,'EXECUTED','8:3a49fe6189afd1ee4e5c4286d6fff068','createIndex indexName=answer_concept_drug, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-128','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',181,'EXECUTED','8:cb0f8b09f946ae6f4de454a9b3c73d14','createIndex indexName=answer_creator, tableName=concept_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-129','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',182,'EXECUTED','8:aafb0f0bbcc38efaea28ef6a0d0279b9','createIndex indexName=answers_for_concept, tableName=concept_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-130','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',183,'EXECUTED','8:d5fc7dc3f1f960e47a8c7509590c2640','createIndex indexName=attribute_changer, tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-131','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',184,'EXECUTED','8:5efc1f8e26031e3b01c73abefbd04d97','createIndex indexName=attribute_creator, tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-132','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',185,'EXECUTED','8:9685bc73a05b10f134eb4094de4454fd','createIndex indexName=attribute_is_searchable, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-133','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',186,'EXECUTED','8:efff827acdb0744ac2ed37825b9bcd05','createIndex indexName=attribute_type_changer, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-134','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',187,'EXECUTED','8:2d33f303d66728b36c2613fbdbf1aaee','createIndex indexName=attribute_type_creator, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-135','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',188,'EXECUTED','8:154807a5dc054391d50c35c8bb61978b','createIndex indexName=attribute_voider, tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-136','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',189,'EXECUTED','8:114f3a5e1e12f2a0298aed9526443d2a','createIndex indexName=care_setting_changed_by, tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-137','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',190,'EXECUTED','8:bbe2e850c9553c682b0b81a09d28bea2','createIndex indexName=care_setting_creator, tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-138','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',191,'EXECUTED','8:6e06a22ce5d030fe4ab8666a620b2618','createIndex indexName=care_setting_retired_by, tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-139','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',192,'EXECUTED','8:c38dc46868737972f405e6bd24c5b620','createIndex indexName=category_order_set_fk, tableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-140','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',193,'EXECUTED','8:f09e2fb2224ce21fcef36d29b130aa92','createIndex indexName=cohort_creator, tableName=cohort','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-141','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',194,'EXECUTED','8:4029038a2a09d5f0815d7e34592c64be','createIndex indexName=cohort_member_creator, tableName=cohort_member','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-142','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',195,'EXECUTED','8:606356b2cb0496204c4d88883957bd25','createIndex indexName=concept_attribute_attribute_type_id_fk, tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-143','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',196,'EXECUTED','8:9369993a9f7a223e2f5e5a6c64772b67','createIndex indexName=concept_attribute_changed_by_fk, tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-144','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',197,'EXECUTED','8:5f96f8798e5b7b9da72c45b3dc81ac9d','createIndex indexName=concept_attribute_concept_fk, tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-145','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',198,'EXECUTED','8:c89c712442a89f2d07ea21f6b60ffc1a','createIndex indexName=concept_attribute_creator_fk, tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-146','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',199,'EXECUTED','8:458cd8dfa75fcb0d2ce928655d46d034','createIndex indexName=concept_attribute_type_changed_by_fk, tableName=concept_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-147','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',200,'EXECUTED','8:28845acfb2c6fa84d47ce725c57f8794','createIndex indexName=concept_attribute_type_creator_fk, tableName=concept_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-148','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',201,'EXECUTED','8:3c8ea1bcfb3898521892aa87fa3bc4c6','createIndex indexName=concept_attribute_type_retired_by_fk, tableName=concept_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-149','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',202,'EXECUTED','8:f0ce6d9a7d6aab981de0ee3543f620ac','createIndex indexName=concept_attribute_voided_by_fk, tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-150','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',203,'EXECUTED','8:8bf3fcba8375bbae51656f3e44e00c45','createIndex indexName=concept_class_changed_by, tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-151','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',204,'EXECUTED','8:d2fe72bcd363d8819fae99cfa833b6bb','createIndex indexName=concept_class_creator, tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-152','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',205,'EXECUTED','8:1ac47021261d5d2b0c17358d7f1f31ee','createIndex indexName=concept_class_id, tableName=order_type_class_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-153','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',206,'EXECUTED','8:6e676415380fef7f1ac0d9f9033aca69','createIndex indexName=concept_class_name_index, tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-154','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',207,'EXECUTED','8:38e8f613e1b7a518ba6ed47e80e6f8ca','createIndex indexName=concept_class_retired_status, tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-155','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',208,'EXECUTED','8:b74346ab17f12599f02e5bea00781e31','createIndex indexName=concept_classes, tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-156','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',209,'EXECUTED','8:f302942598e5281d4edaa8eab7b4ade4','createIndex indexName=concept_creator, tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-157','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',210,'EXECUTED','8:8af66bd30f40514f5663229d3f558a52','createIndex indexName=concept_datatype_creator, tableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-158','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',211,'EXECUTED','8:0eb97161e6a7cea441a2ed63e804feaa','createIndex indexName=concept_datatype_name_index, tableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-159','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',212,'EXECUTED','8:988e10b41ee14ab190aabac3111ecc84','createIndex indexName=concept_datatype_retired_status, tableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-160','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',213,'EXECUTED','8:e9d6fb1e47328e7d59c1ddbf308dea70','createIndex indexName=concept_datatypes, tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-161','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',214,'EXECUTED','8:52f85d930a3d36a5ad6c78b9f61290c3','createIndex indexName=concept_for_field, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-162','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',215,'EXECUTED','8:d575248e5ec5c8986b23ea4944cfb679','createIndex indexName=concept_for_proposal, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-163','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',216,'EXECUTED','8:aa653faa994913a1a341733b5f6d534f','createIndex indexName=concept_id, tableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-164','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',217,'EXECUTED','8:9d6067722574739babcf829fd7005083','createIndex indexName=concept_map_type_for_drug_reference_map, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-165','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',218,'EXECUTED','8:79506d357d610b77215c6376a8b51924','createIndex indexName=concept_name_changed_by, tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-166','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',219,'EXECUTED','8:8d575462dcadf2a8ba3b824103125a78','createIndex indexName=concept_name_tag_changed_by, tableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-167','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',220,'EXECUTED','8:339490cb412ca1189f24a4a6424a21f1','createIndex indexName=concept_reference_source_changed_by, tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-168','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',221,'EXECUTED','8:983653514a7c9e3c9e7cb3f06f845759','createIndex indexName=concept_reference_term_for_drug_reference_map, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-169','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',222,'EXECUTED','8:11669081562535928754bc5bcc11a180','createIndex indexName=concept_source_creator, tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-170','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',223,'EXECUTED','8:87a8f1b58cf4f932acdd05c7e1590ff4','createIndex indexName=concept_triggers_conversion, tableName=concept_state_conversion','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-171','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',224,'EXECUTED','8:c89b23cab92f853375bd3a8b8871b512','createIndex indexName=condition_changed_by_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-172','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',225,'EXECUTED','8:bfd4ebec86fda1f34b776b71fd148fbd','createIndex indexName=condition_condition_coded_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-173','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',226,'EXECUTED','8:c8b66c3b9be5794d3baf970e7ea00d3b','createIndex indexName=condition_condition_coded_name_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-174','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',227,'EXECUTED','8:e2bda57c42c5a9bf7c854a67b55bf3c7','createIndex indexName=condition_creator_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-175','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',228,'EXECUTED','8:ee4160d92a93933a9b2d17ff9f6b60c5','createIndex indexName=condition_patient_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-176','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',229,'EXECUTED','8:c2127880bbcc4d3d6bdb3586d7f9268c','createIndex indexName=condition_previous_version_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-177','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',230,'EXECUTED','8:e37db5b020b3bfdf3265addc4d1678e3','createIndex indexName=condition_voided_by_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-178','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',231,'EXECUTED','8:58731983c06100b4d201c83a0c036774','createIndex indexName=conditions_encounter_id_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-179','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',232,'EXECUTED','8:9036796f0a8298a97e6ebbbb44f60269','createIndex indexName=conversion_to_state, tableName=concept_state_conversion','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-180','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',233,'EXECUTED','8:ca5f8b4206708c7a0b265ec52b2036f1','createIndex indexName=defines_attribute_type, tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-181','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',234,'EXECUTED','8:0e8c8a71f1d7106ab0ee2b7a4e9c1a8e','createIndex indexName=defines_identifier_type, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-182','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',235,'EXECUTED','8:21f248120c7e245a62a62a73f0bca3ac','createIndex indexName=description_for_concept, tableName=concept_description','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-183','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',236,'EXECUTED','8:5b3517c1c0adb044bb49d80f3952d2bc','createIndex indexName=discontinued_because, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-184','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',237,'EXECUTED','8:5699d06e22f1088a576fa43d40e208c3','createIndex indexName=dosage_form_concept, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-185','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',238,'EXECUTED','8:d57a724ae220f3ba9758a3d91b2c7b06','createIndex indexName=drug_changed_by, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-186','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',239,'EXECUTED','8:7923f05b615200e9a0074c5891d30bf9','createIndex indexName=drug_creator, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-187','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',240,'EXECUTED','8:dbf9dbfd7b8bff04652494304fd50093','createIndex indexName=drug_dose_limit_units_fk, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-188','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',241,'EXECUTED','8:5504067b12cf6ae817487a6ca5d52d1d','createIndex indexName=drug_for_drug_reference_map, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-189','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',242,'EXECUTED','8:b3ff030e8d493d861782b7f31a050e0f','createIndex indexName=drug_ingredient_ingredient_id_fk, tableName=drug_ingredient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-190','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',243,'EXECUTED','8:ce528a21bf16d3246814853ba0bf1229','createIndex indexName=drug_ingredient_units_fk, tableName=drug_ingredient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-191','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',244,'EXECUTED','8:848d0f03bb7cbbbe3c8cd535723fc286','createIndex indexName=drug_order_dose_units, tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-192','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',245,'EXECUTED','8:f0bf80f6b3e0f21206ef510a1de398e9','createIndex indexName=drug_order_duration_units_fk, tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-193','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',246,'EXECUTED','8:94b059cadf82b710c23f5fb9c08f5368','createIndex indexName=drug_order_frequency_fk, tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-194','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',247,'EXECUTED','8:f5a2aec019e0413dcfd8f32eddc37935','createIndex indexName=drug_order_quantity_units, tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-195','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',248,'EXECUTED','8:d8321473ede7a94b94803af154f26ce5','createIndex indexName=drug_order_route_fk, tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-196','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',249,'EXECUTED','8:194d1348aad83d4663ff157b6980811b','createIndex indexName=drug_reference_map_creator, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-197','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',250,'EXECUTED','8:94c2f1caed4dfb8c1a8648f7eea01946','createIndex indexName=drug_retired_by, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-198','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',251,'EXECUTED','8:85b8420e32b96096af1feb9dcaea1aee','createIndex indexName=encounter_changed_by, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-199','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',252,'EXECUTED','8:4afd4079efd3ff663172a71a656cb0af','createIndex indexName=encounter_datetime_idx, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-200','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',253,'EXECUTED','8:69bb817fd5219d6dc83ef2193ee45493','createIndex indexName=encounter_diagnosis_changed_by_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-201','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',254,'EXECUTED','8:928468c53f6e742d08354537d50ae383','createIndex indexName=encounter_diagnosis_coded_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-202','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',255,'EXECUTED','8:33cf466b937ef01224c85f7902d538b7','createIndex indexName=encounter_diagnosis_coded_name_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-203','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',256,'EXECUTED','8:b0552173bf3dfd53c2e06d3f947e2804','createIndex indexName=encounter_diagnosis_condition_id_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-204','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',257,'EXECUTED','8:558a51844de3da4468a37e556f88550c','createIndex indexName=encounter_diagnosis_creator_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-205','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',258,'EXECUTED','8:ba1b9af8999b961c8bd24b959adda690','createIndex indexName=encounter_diagnosis_encounter_id_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-206','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',259,'EXECUTED','8:4751d44aad47533f7134557462f454d1','createIndex indexName=encounter_diagnosis_patient_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-207','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',260,'EXECUTED','8:9de921c11bf2be660c61cdbd76da5b60','createIndex indexName=encounter_diagnosis_voided_by_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-208','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',261,'EXECUTED','8:51d287065d57ec65f83f59e53275a2d3','createIndex indexName=encounter_for_proposal, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-209','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',262,'EXECUTED','8:3d3566657ec47cfe03228b6036af5b08','createIndex indexName=encounter_form, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-210','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',263,'EXECUTED','8:6a5edd213c96575bd6da0b995778673c','createIndex indexName=encounter_ibfk_1, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-211','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',264,'EXECUTED','8:dda7f3b1c8972a907b426fd36ba0602e','createIndex indexName=encounter_id_fk, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-212','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',265,'EXECUTED','8:1bf27c6a1d520366b18da383e3f44120','createIndex indexName=encounter_location, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-213','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',266,'EXECUTED','8:b01ffb394fe143dc3131901350d5d0f6','createIndex indexName=encounter_note, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-214','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',267,'EXECUTED','8:a809fd6fde71f84b7ba7ce570af8cc80','createIndex indexName=encounter_observations, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-215','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',268,'EXECUTED','8:c12352d4cf4efd3d17dd66f10d27d583','createIndex indexName=encounter_patient, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-216','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',269,'EXECUTED','8:808779a89c2702ee93e69d8895112912','createIndex indexName=encounter_provider_changed_by, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-217','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',270,'EXECUTED','8:d77e3dc635af12b46d4053e839f9e56a','createIndex indexName=encounter_provider_creator, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-218','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',271,'EXECUTED','8:50dbaaa713bdb18c50e68158315bfadb','createIndex indexName=encounter_provider_voided_by, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-219','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',272,'EXECUTED','8:ec5bb45b9454234194cf370cf6a6b4f5','createIndex indexName=encounter_role_changed_by_fk, tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-220','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',273,'EXECUTED','8:3b49284874dfe876e87273e8f2688bed','createIndex indexName=encounter_role_creator_fk, tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-221','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',274,'EXECUTED','8:a4a01769a58e5cd2dbd901d9b72c6dac','createIndex indexName=encounter_role_id_fk, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-222','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',275,'EXECUTED','8:d60f3e397dc8a8e27cbbe51c6c4649c0','createIndex indexName=encounter_role_retired_by_fk, tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-223','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',276,'EXECUTED','8:f35a2f0b6d781c7ed8b4908890bd8f6d','createIndex indexName=encounter_type_changed_by, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-224','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',277,'EXECUTED','8:c5a4bab1f539bd4ced495c005c1a6f7f','createIndex indexName=encounter_type_id, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-225','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',278,'EXECUTED','8:06769795c33557f4889394fc30179450','createIndex indexName=encounter_type_retired_status, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-226','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',279,'EXECUTED','8:0508c7a7c87d90b8166e2999c837dab1','createIndex indexName=encounter_visit_id_fk, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-227','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',280,'EXECUTED','8:d37207a166aaca60d194ee966bc7e120','createIndex indexName=family_name2, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-228','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',281,'EXECUTED','8:d4aa398734c837519572e6fcb143f355','createIndex indexName=field_answer_concept, tableName=field_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-229','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',282,'EXECUTED','8:5dd4c8d34f9c8e04b99ccb7c2d12598f','createIndex indexName=field_retired_status, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-230','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',283,'EXECUTED','8:3b5c94c711a49af9fb71d08971423bb8','createIndex indexName=field_within_form, tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-231','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',284,'EXECUTED','8:38c45e1e9f4030e9e93022f6dc4ed350','createIndex indexName=first_name, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-232','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',285,'EXECUTED','8:5fcbc0fefca2b7bb36b0d15a0eb99e8f','createIndex indexName=fk_orderer_provider, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-233','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',286,'EXECUTED','8:92329d4c3a194f8543b93d67fccecd19','createIndex indexName=form_containing_field, tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-234','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',287,'EXECUTED','8:3c224e2faf0ad1adf4ba03b0005c8bc8','createIndex indexName=form_encounter_type, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-235','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',288,'EXECUTED','8:8323a58c7eba3820e99e40bd0603d442','createIndex indexName=form_field_hierarchy, tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-236','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:57',289,'EXECUTED','8:c8a1b0aeda085a3c343812e6e2daa18d','createIndex indexName=form_published_and_retired_index, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-237','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',290,'EXECUTED','8:024161f440c8e3fbbf85b098b439ae3f','createIndex indexName=form_published_index, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-238','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',291,'EXECUTED','8:fc36d56224631de132820b321e89bf2e','createIndex indexName=form_resource_changed_by, tableName=form_resource','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-239','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',292,'EXECUTED','8:023c3e2259f8bb56b2a1cb4a8c35a867','createIndex indexName=form_retired_index, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-240','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',293,'EXECUTED','8:627e998fbc62b560695184acca2224cc','createIndex indexName=global_property_changed_by, tableName=global_property','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-241','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',294,'EXECUTED','8:43a5867d586a7bb3465aa89d95198815','createIndex indexName=has_a, tableName=concept_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-242','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',295,'EXECUTED','8:3d9ea542a05ba93fc2ccbdf08ccaa447','createIndex indexName=hl7_code, tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-243','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',296,'EXECUTED','8:117418151837815057613901205a080b','createIndex indexName=hl7_in_archive_message_state_idx, tableName=hl7_in_archive','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-244','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',297,'EXECUTED','8:6d3741d2da44192d82d16931f1ac6ac3','createIndex indexName=hl7_source_with_queue, tableName=hl7_in_queue','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-245','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',298,'EXECUTED','8:d95090902ac2b5d451f06d171c4cbf17','createIndex indexName=identifier_creator, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-246','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',299,'EXECUTED','8:5f75e8eaf134aa378d6abed32e7ffef2','createIndex indexName=identifier_name, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-247','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',300,'EXECUTED','8:d0aaa5c542b2c4dba94183e908e4fea0','createIndex indexName=identifier_voider, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-248','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',301,'EXECUTED','8:85e6b78d6cd2d2b38288c093ed45a50c','createIndex indexName=identifies_person, tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-249','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',302,'EXECUTED','8:d2ad7f39efadf4ae6e8cc21e028d3ae2','createIndex indexName=idx_code_concept_reference_term, tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-250','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',303,'EXECUTED','8:604f6d74b4b564008d52f4e4724025e0','createIndex indexName=idx_concept_set_concept, tableName=concept_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-251','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',304,'EXECUTED','8:073d2de556dcb0d7069d64fc1ac853ac','createIndex indexName=idx_patient_identifier_patient, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-252','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',305,'EXECUTED','8:a7118f2301f4b24a6342ef5b45ff9310','createIndex indexName=inherited_role, tableName=role_role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-253','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',306,'EXECUTED','8:62407eb8be95762422804e65de8277ac','createIndex indexName=inventory_item, tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-254','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',307,'EXECUTED','8:9ab129f6cd9a2eb318aa9b6188e10a05','createIndex indexName=last_name, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-255','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',308,'EXECUTED','8:7116fd0aa0d86192591b5b05562321f7','createIndex indexName=location_attribute_attribute_type_id_fk, tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-256','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',309,'EXECUTED','8:68202f13662e9a8cb697cedc9068fb35','createIndex indexName=location_attribute_changed_by_fk, tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-257','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',310,'EXECUTED','8:73ff410750d5bd6be3065cf8872da7ad','createIndex indexName=location_attribute_creator_fk, tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-258','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',311,'EXECUTED','8:49f63762203223d15f120ef9e10e0d37','createIndex indexName=location_attribute_location_fk, tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-259','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',312,'EXECUTED','8:315992d23794b47db16ab4831e28642d','createIndex indexName=location_attribute_type_changed_by_fk, tableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-260','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',313,'EXECUTED','8:dc08777f447f2daa7b3882f86c1bc70e','createIndex indexName=location_attribute_type_creator_fk, tableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-261','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',314,'EXECUTED','8:b762663bd14409ab9db4020bb0757bdd','createIndex indexName=location_attribute_type_retired_by_fk, tableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-262','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',315,'EXECUTED','8:8e8a23968f5c0a313874c107a75666a7','createIndex indexName=location_attribute_voided_by_fk, tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-263','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',316,'EXECUTED','8:aa1479b3373a6c385f19c2964fb32468','createIndex indexName=location_changed_by, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-264','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',317,'EXECUTED','8:da61c541398842b05fb3fdc5a2d44825','createIndex indexName=location_retired_status, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-265','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',318,'EXECUTED','8:58cd806aa83fe15d0656711208dced62','createIndex indexName=location_tag_changed_by, tableName=location_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-266','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',319,'EXECUTED','8:c1e4a003467d3a7586507788c79d6632','createIndex indexName=location_tag_creator, tableName=location_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-267','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',320,'EXECUTED','8:40cceeab104549fd7756a182c7b31fce','createIndex indexName=location_tag_map_tag, tableName=location_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-268','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',321,'EXECUTED','8:3f82c098e31ca3a3c8d4c5f1304ea14f','createIndex indexName=location_tag_retired_by, tableName=location_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-269','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',322,'EXECUTED','8:b88c90f1f923cde2f9e5b575bc897d24','createIndex indexName=map_creator, tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-270','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',323,'EXECUTED','8:aaec6329d2d4888261e450178f7169e0','createIndex indexName=map_for_concept, tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-271','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',324,'EXECUTED','8:30d99738aea457ba6229a7b6f5e20cbd','createIndex indexName=mapped_concept_map_type, tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-272','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',325,'EXECUTED','8:10e3e6f6299728e62066649948857d9a','createIndex indexName=mapped_concept_map_type_ref_term_map, tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-273','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',326,'EXECUTED','8:51f56c62985729cfea11d79bef2c188b','createIndex indexName=mapped_concept_name, tableName=concept_name_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-274','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',327,'EXECUTED','8:d83c81bd65c2723b6e2db5d44a0c99b1','createIndex indexName=mapped_concept_name_tag, tableName=concept_name_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-275','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',328,'EXECUTED','8:b0c3a30b6d83eba35cc271b97bf2b88f','createIndex indexName=mapped_concept_proposal, tableName=concept_proposal_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-276','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',329,'EXECUTED','8:a3c3c7616093618de7f90a3bac2d27bc','createIndex indexName=mapped_concept_proposal_tag, tableName=concept_proposal_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-277','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',330,'EXECUTED','8:17a6d1ea0726018afcd68f60d77159f4','createIndex indexName=mapped_concept_reference_term, tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-278','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',331,'EXECUTED','8:9a0d34b3c6b7086cbc977a8be7baaff0','createIndex indexName=mapped_concept_source, tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-279','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',332,'EXECUTED','8:b550cb7b0906479412351e273260ad7f','createIndex indexName=mapped_term_a, tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-280','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',333,'EXECUTED','8:6e11a8dd0c3441dadbe67e7c9c68bf42','createIndex indexName=mapped_term_b, tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-281','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',334,'EXECUTED','8:a85859fe776be9d8451c15dbf38c0b32','createIndex indexName=mapped_user_changed, tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-282','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',335,'EXECUTED','8:d5c8a9a58153ef12287eebd473b539a9','createIndex indexName=mapped_user_changed_concept_map_type, tableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-283','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',336,'EXECUTED','8:a909b523ac1f4289708db4dcc9fdafa1','createIndex indexName=mapped_user_changed_ref_term, tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-284','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',337,'EXECUTED','8:7b8e93dc1d2c9a0ff376e349832f4494','createIndex indexName=mapped_user_changed_ref_term_map, tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-285','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',338,'EXECUTED','8:64ea4ebae8ea496a009d1f525367370a','createIndex indexName=mapped_user_creator, tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-286','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',339,'EXECUTED','8:b3435d6dd1b7880e93aa5edcc74846c7','createIndex indexName=mapped_user_creator_concept_map_type, tableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-287','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',340,'EXECUTED','8:3dc54f80ef612321c91462eccd3a55ec','createIndex indexName=mapped_user_creator_ref_term_map, tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-288','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',341,'EXECUTED','8:08d4f11fd50579428117a60fdee2a6b7','createIndex indexName=mapped_user_retired, tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-289','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',342,'EXECUTED','8:fb28e195cd8e9357e12391a4a74f778b','createIndex indexName=mapped_user_retired_concept_map_type, tableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-290','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',343,'EXECUTED','8:aad8af5f9b037c94eac525a386375b06','createIndex indexName=member_patient, tableName=cohort_member','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-291','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',344,'EXECUTED','8:bc24e713e119554b17147a3f6a2dfb93','createIndex indexName=middle_name, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-292','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',345,'EXECUTED','8:0c9233681e5faa9dd2536b33b8e763a0','createIndex indexName=care_setting_name, tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-293','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',346,'EXECUTED','8:ff3e34792a09fd668bcca62343ced16b','createIndex indexName=concept_map_type_name, tableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-294','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',347,'EXECUTED','8:3ab01680a3b2e784e3616c4b25600974','createIndex indexName=encounter_role_name, tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-295','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',348,'EXECUTED','8:1cbf454f90f0af4bc73ee5fb11dd05ee','createIndex indexName=encounter_type_name, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-296','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',349,'EXECUTED','8:20c35381d445f3c1114a4efda23dbc3e','createIndex indexName=location_attribute_type_name, tableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-297','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',350,'EXECUTED','8:70071e4c97e8754c6ecb480470a699ee','createIndex indexName=order_group_attribute_type_name, tableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-298','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',351,'EXECUTED','8:0231ecea82c82cda827ab3f001b62328','createIndex indexName=order_type_name, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-299','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',352,'EXECUTED','8:f7c7ce994adfdd0282914a56626c9397','createIndex indexName=program_attribute_type_name, tableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-300','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',353,'EXECUTED','8:cd7babb7dfc5e898eb8b6c0d2b0c1285','createIndex indexName=name_for_concept, tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-301','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',354,'EXECUTED','8:b9d77080d671f9befc731734d1082d2c','createIndex indexName=name_for_person, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-302','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',355,'EXECUTED','8:e1cf3551a6093982be076e202c877bea','createIndex indexName=name_of_attribute, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-303','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',356,'EXECUTED','8:f20bfdf41671ea836878754bf85cefaf','createIndex indexName=name_of_concept, tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-304','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',357,'EXECUTED','8:6ab3606ad0d1274f0f855eb01769c936','createIndex indexName=name_of_location, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-305','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',358,'EXECUTED','8:63c8b309eedd228a8a1ba5374d6d7f95','createIndex indexName=note_hierarchy, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-306','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',359,'EXECUTED','8:502b5e5c51859e2eec91b875bc9abcb0','createIndex indexName=obs_concept, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-307','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',360,'EXECUTED','8:722b0dba4b61dcab30b52c887cf9e0b8','createIndex indexName=obs_datetime_idx, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-308','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',361,'EXECUTED','8:dce7042a64be9d3a55a5cbcf432e7bf5','createIndex indexName=obs_enterer, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-309','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',362,'EXECUTED','8:ba4376ab37d59d2c4a85ce96e533d41e','createIndex indexName=obs_grouping_id, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-310','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',363,'EXECUTED','8:03aadd87359c003150d969af145fb4c1','createIndex indexName=obs_location, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-311','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',364,'EXECUTED','8:bba20f36616da2cbec86636ff46fa212','createIndex indexName=obs_name_of_coded_value, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-312','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',365,'EXECUTED','8:b6d61dfd54ecfe4845a7047d700bd53b','createIndex indexName=obs_note, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-313','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',366,'EXECUTED','8:c1f4c2b04a6f13c059872a34c35b0df4','createIndex indexName=obs_order, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-314','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',367,'EXECUTED','8:0c18f787da491494b46284585cd17820','createIndex indexName=order_creator, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-315','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',368,'EXECUTED','8:1667c90052e388e32df606174648c13a','createIndex indexName=order_for_patient, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-316','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',369,'EXECUTED','8:4b1ccee6baba41cbe80a1449e3774eea','createIndex indexName=order_frequency_changed_by_fk, tableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-317','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',370,'EXECUTED','8:9dce146c0bdb56e141c57d01f0a3d7a6','createIndex indexName=order_frequency_creator_fk, tableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-318','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',371,'EXECUTED','8:39844d3ca32350045b50c603aa80a152','createIndex indexName=order_frequency_retired_by_fk, tableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-319','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',372,'EXECUTED','8:5fe67bd43afa9b0317903ee93bc5f08d','createIndex indexName=order_group_attribute_attribute_type_id_fk, tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-320','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',373,'EXECUTED','8:dc0be0622219f8132ba20d0fb5e17d2a','createIndex indexName=order_group_attribute_changed_by_fk, tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-321','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',374,'EXECUTED','8:e45b69b5bcf322d09c0d1278309eb948','createIndex indexName=order_group_attribute_creator_fk, tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-322','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',375,'EXECUTED','8:43f137218504b56e701f37bc7c0befe1','createIndex indexName=order_group_attribute_order_group_fk, tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-323','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',376,'EXECUTED','8:f9b58a5b1f291febc36d6409987be350','createIndex indexName=order_group_attribute_type_changed_by_fk, tableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-324','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',377,'EXECUTED','8:cdd83fa85b9bc40873ef92d41b140a11','createIndex indexName=order_group_attribute_type_creator_fk, tableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-325','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',378,'EXECUTED','8:dbe2e46c1891eb3f7110719b09b787ad','createIndex indexName=order_group_attribute_type_retired_by_fk, tableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-326','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',379,'EXECUTED','8:27e0a2370b4ca7e72f80f2723df5c98a','createIndex indexName=order_group_attribute_voided_by_fk, tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-327','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',380,'EXECUTED','8:388f88ffecb27c80aadf3e3fe15b9363','createIndex indexName=order_group_changed_by_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-328','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',381,'EXECUTED','8:a1aafa6be68646d6461ea2ffbf1a8eaa','createIndex indexName=order_group_creator_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-329','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',382,'EXECUTED','8:1fd62bd470c97ec9131e196ac9cb2653','createIndex indexName=order_group_encounter_id_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-330','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',383,'EXECUTED','8:6e5901a4bd7fe17cb5e756c12959838a','createIndex indexName=order_group_order_group_reason_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-331','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',384,'EXECUTED','8:06527d1d19b7bea09ee4f8f39b1fd362','createIndex indexName=order_group_parent_order_group_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-332','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',385,'EXECUTED','8:b5d584dae229bb43b3d5dfac96ff4baa','createIndex indexName=order_group_patient_id_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-333','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',386,'EXECUTED','8:1ea0061d26118b44a9ec13b422742389','createIndex indexName=order_group_previous_order_group_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-334','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',387,'EXECUTED','8:7553098d9054df52e1cbc6537030543e','createIndex indexName=order_group_set_id_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-335','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',388,'EXECUTED','8:ac29ea7e6e42cdeb62f7b284a5af9294','createIndex indexName=order_group_voided_by_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-336','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',389,'EXECUTED','8:6aab691f5f0bfd66f72c75285215c0e7','createIndex indexName=order_set_attribute_attribute_type_id_fk, tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-337','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',390,'EXECUTED','8:0d94547b98e4d670a4fa3cb063e8122d','createIndex indexName=order_set_attribute_changed_by_fk, tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-338','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',391,'EXECUTED','8:4dd3631c767fb038eadbc0ffaceb3f86','createIndex indexName=order_set_attribute_creator_fk, tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-339','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',392,'EXECUTED','8:f0f868a21d97bccc759154377933ace0','createIndex indexName=order_set_attribute_order_set_fk, tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-340','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',393,'EXECUTED','8:4051aa74a4131065ba6d42df3553e8bd','createIndex indexName=order_set_attribute_type_changed_by_fk, tableName=order_set_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-341','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',394,'EXECUTED','8:e89fed18056235fbe9886fb152a2ba02','createIndex indexName=order_set_attribute_type_creator_fk, tableName=order_set_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-342','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',395,'EXECUTED','8:fe365acd0a2d0e45db3a77bd66817410','createIndex indexName=order_set_attribute_type_retired_by_fk, tableName=order_set_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-343','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',396,'EXECUTED','8:59ac227a5dfab8c420fb6f63c84c8d7e','createIndex indexName=order_set_attribute_voided_by_fk, tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-344','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',397,'EXECUTED','8:72a58bef28dfde2697d38f048dc269f9','createIndex indexName=order_set_changed_by_fk, tableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-345','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',398,'EXECUTED','8:fb89530b109069ba8be1c3bb717c2121','createIndex indexName=order_set_creator_fk, tableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-346','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',399,'EXECUTED','8:1f35032878e0f20879c0a86e4ebed584','createIndex indexName=order_set_member_changed_by_fk, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-347','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',400,'EXECUTED','8:9e380798192199cc558dcf76b86ee383','createIndex indexName=order_set_member_concept_id_fk, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-348','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',401,'EXECUTED','8:ab47cf75996ecd79d4f3e7d51add4f85','createIndex indexName=order_set_member_creator_fk, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-349','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',402,'EXECUTED','8:a4486ddcf3e2bfe7c2af98f689f999f0','createIndex indexName=order_set_member_order_set_id_fk, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-350','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',403,'EXECUTED','8:c33913fec57162ef1d536665aefaa1c4','createIndex indexName=order_set_member_order_type_fk, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-351','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',404,'EXECUTED','8:661398d2733c7c2c5c6f8fce58405000','createIndex indexName=order_set_member_retired_by_fk, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-352','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',405,'EXECUTED','8:1661701e02697cab29c67d2ae1ac42f7','createIndex indexName=order_set_retired_by_fk, tableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-353','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',406,'EXECUTED','8:1f0a7a83f77b6096f2690e583404e7c3','createIndex indexName=order_type_changed_by, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-354','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',407,'EXECUTED','8:8f98f997982f576c790a0383ac8aa975','createIndex indexName=order_type_parent_order_type, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-355','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',408,'EXECUTED','8:644ef92f3d47d365532eff9953db9793','createIndex indexName=order_type_retired_status, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-356','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',409,'EXECUTED','8:ebab8d61ca65d04b73dbed42acebbe6e','createIndex indexName=orders_accession_number, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-357','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',410,'EXECUTED','8:ecd6c3abe3ba7670d05bb4f66036e2d0','createIndex indexName=orders_care_setting, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-358','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',411,'EXECUTED','8:d43c7046b4cc96fb2bab5c015230ea82','createIndex indexName=orders_in_encounter, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-359','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',412,'EXECUTED','8:011e69b59b097ca0e832f38c4c70afad','createIndex indexName=orders_order_group_id_fk, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-360','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',413,'EXECUTED','8:b19d78a89af7cbbc1146acfdc3bc9ee5','createIndex indexName=orders_order_number, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-361','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',414,'EXECUTED','8:b015ed6aaed23eda9c8e04dad894a859','createIndex indexName=parent_cohort, tableName=cohort_member','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-362','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',415,'EXECUTED','8:23453ec0a57f55c47dc0bc03714104aa','createIndex indexName=parent_location, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-363','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',416,'EXECUTED','8:e9b88deaeac31b0432fbd4d4d483691e','createIndex indexName=patient_address_creator, tableName=person_address','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-364','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',417,'EXECUTED','8:b8273bc9a460019313af0de921e7bab6','createIndex indexName=patient_address_void, tableName=person_address','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-365','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',418,'EXECUTED','8:230aa6b6db9c47ffbb14f32552a51519','createIndex indexName=patient_identifier_changed_by, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-366','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',419,'EXECUTED','8:6d42130aab478c32de45a24a4bac2a24','createIndex indexName=patient_identifier_ibfk_2, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-367','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',420,'EXECUTED','8:de67177b4e73fa164b21cc4367e57c82','createIndex indexName=patient_identifier_type_changed_by, tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-368','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',421,'EXECUTED','8:38fdd8b1bdea75698ea31430790b340e','createIndex indexName=patient_identifier_type_retired_status, tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-369','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',422,'EXECUTED','8:dcfc5706248f1defc7ea124dbef8ec14','createIndex indexName=patient_in_program, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-370','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',423,'EXECUTED','8:71ebb118a32a50f163d3ba4193149626','createIndex indexName=patient_note, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-371','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',424,'EXECUTED','8:f6981472e24eb69ed6a696561588fd0e','createIndex indexName=patient_program_attribute_attributetype_fk, tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-372','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',425,'EXECUTED','8:a5cb389441e15055118ce92337e41bd4','createIndex indexName=patient_program_attribute_changed_by_fk, tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-373','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',426,'EXECUTED','8:2beba5b0be222efca6a121d118a4ab09','createIndex indexName=patient_program_attribute_creator_fk, tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-374','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',427,'EXECUTED','8:958d8644d04dcbe96aad90bb7ca4eed6','createIndex indexName=patient_program_attribute_programid_fk, tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-375','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',428,'EXECUTED','8:0c56d279caff0f6f82f17adda3e15883','createIndex indexName=patient_program_attribute_voided_by_fk, tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-376','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',429,'EXECUTED','8:ef920ed5a34363bd9ece75f6eb9071d7','createIndex indexName=patient_program_creator, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-377','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',430,'EXECUTED','8:2ce117f1b4c309dd58b48915890bba97','createIndex indexName=patient_program_for_state, tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-378','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',431,'EXECUTED','8:a00a77158a85232f9ffea1b0aa9850ef','createIndex indexName=patient_program_location_id, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-379','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',432,'EXECUTED','8:fb0a120007bca4b5f25662cbe182fc4c','createIndex indexName=patient_program_outcome_concept_id_fk, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-380','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',433,'EXECUTED','8:9ceb62d9a88ea96e097cc5e333b78a63','createIndex indexName=patient_state_changer, tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-381','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',434,'EXECUTED','8:59094c6a8698fa471c9b14945960111a','createIndex indexName=patient_state_creator, tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-382','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',435,'EXECUTED','8:73dd0ee64698e51d7c4701bf253bda70','createIndex indexName=patient_state_voider, tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-383','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',436,'EXECUTED','8:c4934fbf7bce4899a852b7e2ac7a0c62','createIndex indexName=person_a_is_person, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-384','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:58',437,'EXECUTED','8:0242d7f3c126171c16a5493b7631d0d7','createIndex indexName=person_address_changed_by, tableName=person_address','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-385','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',438,'EXECUTED','8:f3ec96539a9c6fa4a8e93f2387d65b25','createIndex indexName=person_attribute_type_retired_status, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-386','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',439,'EXECUTED','8:32c296ff73a60f3b9f44519f4e05ada3','createIndex indexName=person_b_is_person, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-387','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',440,'EXECUTED','8:86b8bc38c004ffceba0e6d45fd0a5141','createIndex indexName=person_birthdate, tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-388','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',441,'EXECUTED','8:c7165fa592cbbe08e7581be5a69740bb','createIndex indexName=person_death_date, tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-389','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',442,'EXECUTED','8:71b4f77c0d3c9734bd1746ffb7472bcc','createIndex indexName=person_died_because, tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-390','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',443,'EXECUTED','8:af83ca3362c3219a38a424b25e6cb9a0','createIndex indexName=person_id_for_user, tableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-391','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',444,'EXECUTED','8:2acdd5d7585b9fc898ad0eb750f1e347','createIndex indexName=person_merge_log_changed_by_fk, tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-392','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',445,'EXECUTED','8:aeba6374ce79692cc6c4afdbcbdae230','createIndex indexName=person_merge_log_creator, tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-393','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',446,'EXECUTED','8:44d3661ba03f1283fd7ad325dac42206','createIndex indexName=person_merge_log_loser, tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-394','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',447,'EXECUTED','8:4de63c278ed7eeefa8f78e1b592a100f','createIndex indexName=person_merge_log_voided_by_fk, tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-395','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',448,'EXECUTED','8:03ad1a480aa9c0448d3fd383e831beff','createIndex indexName=person_merge_log_winner, tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-396','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',449,'EXECUTED','8:f7b462971b2248a9a77bd476ce5591dc','createIndex indexName=person_obs, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-397','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',450,'EXECUTED','8:fed442d8e5ba4d5c399efb55d5caf925','createIndex indexName=previous_order_id_order_id, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-398','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',451,'EXECUTED','8:94d9a217e3ca38a6915ed4c30566ec81','createIndex indexName=previous_version, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-399','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',452,'EXECUTED','8:8b988fc20cd05cce87dfca918a67a853','createIndex indexName=primary_drug_concept, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-400','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',453,'EXECUTED','8:8035e065236d99f2120a0e0ae7e00e19','createIndex indexName=privilege_definitions, tableName=role_privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-401','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',454,'EXECUTED','8:569e556b5127fb85bbd2f762b8abcb0d','createIndex indexName=privilege_which_can_edit, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-402','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',455,'EXECUTED','8:b2d66237cf16273ca43a279787abd289','createIndex indexName=privilege_which_can_edit_encounter_type, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-403','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',456,'EXECUTED','8:0bad7afd4996f4532aabecc55c05c6f9','createIndex indexName=privilege_which_can_view_encounter_type, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-404','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',457,'EXECUTED','8:c925a99fe7461693242720e6d7b683a9','createIndex indexName=program_attribute_type_changed_by_fk, tableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-405','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',458,'EXECUTED','8:0666dfeda180082f4a8ce20d4672aabb','createIndex indexName=program_attribute_type_creator_fk, tableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-406','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',459,'EXECUTED','8:c3e9e1139230cbcd8182054f54301892','createIndex indexName=program_attribute_type_retired_by_fk, tableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-407','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',460,'EXECUTED','8:4fceeddc65527f6aff2db27629515acc','createIndex indexName=program_concept, tableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-408','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',461,'EXECUTED','8:c860ac227d0114f62112a04072c5d8b3','createIndex indexName=program_creator, tableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-409','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',462,'EXECUTED','8:fc67efa5176469047ee0366b3cf61606','createIndex indexName=program_for_patient, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-410','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',463,'EXECUTED','8:802d71f2abb2ed1c9596adaefa80f25a','createIndex indexName=program_for_workflow, tableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-411','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',464,'EXECUTED','8:31cb0da347f30e58d7e113f3417431a2','createIndex indexName=program_outcomes_concept_id_fk, tableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-412','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',465,'EXECUTED','8:513890cfde892110a3dca637640e85da','createIndex indexName=proposal_obs_concept_id, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-413','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',466,'EXECUTED','8:2a77a4033235e479c607e6e7d445c65f','createIndex indexName=proposal_obs_id, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-414','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',467,'EXECUTED','8:7878b52a263400367ae2f19ee2ffea2c','createIndex indexName=provider_attribute_attribute_type_id_fk, tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-415','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',468,'EXECUTED','8:758eac752cd7e79a0c336c3da5a2fea5','createIndex indexName=provider_attribute_changed_by_fk, tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-416','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',469,'EXECUTED','8:1a3991173a1a6c1cbedcfd8f38b23d46','createIndex indexName=provider_attribute_creator_fk, tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-417','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',470,'EXECUTED','8:2f926bc27f17c6b5761c743e12bc4704','createIndex indexName=provider_attribute_provider_fk, tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-418','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',471,'EXECUTED','8:a56208aa0a1e69c74926d0dab8e3e8e7','createIndex indexName=provider_attribute_type_changed_by_fk, tableName=provider_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-419','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',472,'EXECUTED','8:c8769a761efe89c1327d1f45c3e6804b','createIndex indexName=provider_attribute_type_creator_fk, tableName=provider_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-420','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',473,'EXECUTED','8:f596f610d8bce69e80336990f3c10172','createIndex indexName=provider_attribute_type_retired_by_fk, tableName=provider_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-421','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',474,'EXECUTED','8:3bbe5f379acea118ae77d083d955e988','createIndex indexName=provider_attribute_voided_by_fk, tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-422','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',475,'EXECUTED','8:12978f3e35ea102ffd53a1b0e89e3288','createIndex indexName=provider_changed_by_fk, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-423','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',476,'EXECUTED','8:6150efa0ff3b5fd0ac858125c1a470ce','createIndex indexName=provider_creator_fk, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-424','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',477,'EXECUTED','8:14b0b358db728ed4432e8de5dc0909e4','createIndex indexName=provider_id_fk, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-425','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',478,'EXECUTED','8:bb11f214e21cfbadd61077ae9e7ae570','createIndex indexName=provider_person_id_fk, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-426','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',479,'EXECUTED','8:24045eb3694321420fc128db7caaa768','createIndex indexName=provider_retired_by_fk, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-427','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',480,'EXECUTED','8:e5af562e04013bdb2182af236ef9974b','createIndex indexName=provider_role_id_fk, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-428','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',481,'EXECUTED','8:7f5b659896fe4e183820de53525329aa','createIndex indexName=provider_speciality_id_fk, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-429','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',482,'EXECUTED','8:c1d0126a6bf573cae8fcb664270333c7','createIndex indexName=relation_creator, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-430','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',483,'EXECUTED','8:a48f65a95c03c33b53565800d935cd49','createIndex indexName=relation_voider, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-431','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',484,'EXECUTED','8:be7d98ba6af3417a76d3f0262597686f','createIndex indexName=relationship_changed_by, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-432','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',485,'EXECUTED','8:d6c713e36bca3cc5166dd2fccc3aadcf','createIndex indexName=relationship_type_changed_by, tableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-433','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',486,'EXECUTED','8:2f66bc5bb41570ae45a5ab255469fcd4','createIndex indexName=relationship_type_id, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-434','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',487,'EXECUTED','8:60c176b60dcff5580d0f22a012633b25','createIndex indexName=report_object_creator, tableName=report_object','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-435','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',488,'EXECUTED','8:7c638e546bd699a279d62b0d36489c57','createIndex indexName=role_definitions, tableName=user_role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-436','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',489,'EXECUTED','8:c89862f1966d03c0884d245fdaf4a88f','createIndex indexName=role_privilege_to_role, tableName=role_privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-437','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',490,'EXECUTED','8:c19f178a09ab85f312759af2f16b060f','createIndex indexName=route_concept, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-438','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',491,'EXECUTED','8:b252ed5e84e2f72fa8db5dce3fecf336','createIndex indexName=scheduler_changer, tableName=scheduler_task_config','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-439','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',492,'EXECUTED','8:8ba6f3cf03f4489d612f25773dcad889','createIndex indexName=scheduler_creator, tableName=scheduler_task_config','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-440','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',493,'EXECUTED','8:d3e48dde30aa257722a433debe3cb4a1','createIndex indexName=serialized_object_changed_by, tableName=serialized_object','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-441','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',494,'EXECUTED','8:dc694719e402b67c9639afee8af3b75c','createIndex indexName=serialized_object_creator, tableName=serialized_object','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-442','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',495,'EXECUTED','8:efcaed6bda34c0382dc6d0d9f5095362','createIndex indexName=serialized_object_retired_by, tableName=serialized_object','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-443','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',496,'EXECUTED','8:20e383bcdf31d83b610e56cb26d73979','createIndex indexName=state_changed_by, tableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-444','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',497,'EXECUTED','8:3f7b636456460832ee260a18eb162a6c','createIndex indexName=state_concept, tableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-445','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',498,'EXECUTED','8:81fc984d8bff19515079254bdfce4802','createIndex indexName=state_creator, tableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-446','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',499,'EXECUTED','8:f2c40d95494570530e077694d500fbba','createIndex indexName=state_for_patient, tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-447','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',500,'EXECUTED','8:c2825227c10c514167c74e537c88a771','createIndex indexName=tag, tableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-448','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',501,'EXECUTED','8:c783566eb855f70b2b4c2034489ff324','createIndex indexName=task_config_for_property, tableName=scheduler_task_config_property','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-449','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',502,'EXECUTED','8:d0c6730f5a30d0e02294cbaa18dfb327','createIndex indexName=test_order_frequency_fk, tableName=test_order','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-450','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',503,'EXECUTED','8:c65591bb88a4177f02ca03d277f15f13','createIndex indexName=test_order_specimen_source_fk, tableName=test_order','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-451','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',504,'EXECUTED','8:30777b65a5778c2830ab812ea875d684','createIndex indexName=type_created_by, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-452','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',505,'EXECUTED','8:4f750963d8d22945764a40f9349c9bb9','createIndex indexName=type_creator, tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-453','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',506,'EXECUTED','8:d3647eb7f8f748f61b7e1f7428513e09','createIndex indexName=type_of_field, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-454','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',507,'EXECUTED','8:2635f2461e75c0f71a74049ee425d379','createIndex indexName=type_of_order, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-455','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',508,'EXECUTED','8:188adf59d09e6066878534296f4fa4ac','createIndex indexName=unique_form_and_name, tableName=form_resource','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-456','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',509,'EXECUTED','8:e55261322ad9928cfeab76281217b010','createIndex indexName=unique_id, tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-457','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',510,'EXECUTED','8:016d4ffa55c6a43fa90835b06cd452ef','createIndex indexName=unique_workflow_concept_in_conversion, tableName=concept_state_conversion','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-458','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',511,'EXECUTED','8:98a1414be57317ab27d544e18c5ff1a8','createIndex indexName=user_creator, tableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-459','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',512,'EXECUTED','8:6a79b0e1a0cf53e5fe69cd7e9fdb1a23','createIndex indexName=user_role_to_users, tableName=user_role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-460','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',513,'EXECUTED','8:7488e3ad5fa77d8bb8028a8a5ded2053','createIndex indexName=user_who_changed, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-461','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',514,'EXECUTED','8:2e15f0c83ae87edaf76b8e9bc598b09f','createIndex indexName=user_who_changed_alert, tableName=notification_alert','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-462','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',515,'EXECUTED','8:fa13ba6723d9cde878f5344d340f7a40','createIndex indexName=user_who_changed_cohort, tableName=cohort','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-463','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',516,'EXECUTED','8:5d01c7d634e6e5b0f704c623c0b1724d','createIndex indexName=user_who_changed_concept, tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-464','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',517,'EXECUTED','8:06048ac4476be2275bf2ee315f1f27ff','createIndex indexName=user_who_changed_description, tableName=concept_description','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-465','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',518,'EXECUTED','8:109931cbd7f05fbb5fe1de5fcf85d321','createIndex indexName=user_who_changed_drug_reference_map, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-466','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',519,'EXECUTED','8:5e79cc38ee626ced085d068f1cec7537','createIndex indexName=user_who_changed_field, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-467','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',520,'EXECUTED','8:43690f64af6a8f9f6b8310d801f64a23','createIndex indexName=user_who_changed_note, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-468','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',521,'EXECUTED','8:a3314d0d45c5610b39b7d8380af4fc11','createIndex indexName=user_who_changed_pat, tableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-469','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',522,'EXECUTED','8:af30a52a49b908b44d601e3d62076f4f','createIndex indexName=user_who_changed_person, tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-470','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',523,'EXECUTED','8:d0ddcbe0c07c691c2d6322d9fa78ae9e','createIndex indexName=user_who_changed_program, tableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-471','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',524,'EXECUTED','8:2874f5c69f71d4f3bab4764372bfa451','createIndex indexName=user_who_changed_proposal, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-472','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',525,'EXECUTED','8:54219e10121d5c5e2196fdacb03f149c','createIndex indexName=user_who_changed_report_object, tableName=report_object','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-473','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',526,'EXECUTED','8:373605e6f966e4fed7fca969bea5e4f5','createIndex indexName=user_who_changed_user, tableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-474','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',527,'EXECUTED','8:e9b1959ae3773a7b5381225a307b25a7','createIndex indexName=user_who_created, tableName=concept_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-475','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',528,'EXECUTED','8:dec84946e9ef350fee9bf9e564565ae7','createIndex indexName=user_who_created_description, tableName=concept_description','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-476','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',529,'EXECUTED','8:2267a0e2ef7c5c12e1136d8fc58ee817','createIndex indexName=user_who_created_field, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-477','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',530,'EXECUTED','8:cc980da2d5334af135b31baab720a8b7','createIndex indexName=user_who_created_field_answer, tableName=field_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-478','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',531,'EXECUTED','8:b4e1b4ef31eed23b036e69759ed6d362','createIndex indexName=user_who_created_field_type, tableName=field_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-479','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',532,'EXECUTED','8:4c9ab7f6f116fb6d180165aa4314c4f9','createIndex indexName=user_who_created_form, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-480','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',533,'EXECUTED','8:d1435b63968d4ac2ff7656d2f26c596d','createIndex indexName=user_who_created_form_field, tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-481','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',534,'EXECUTED','8:d60b12848c9dbbf1c87a640ba1f1464e','createIndex indexName=user_who_created_hl7_source, tableName=hl7_source','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-482','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',535,'EXECUTED','8:77e3f63aae12ba9d898a35ab6fe95592','createIndex indexName=user_who_created_location, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-483','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',536,'EXECUTED','8:e5b1e2c2a208fc667cab964139db2a5b','createIndex indexName=user_who_created_name, tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-484','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',537,'EXECUTED','8:482b94f4c8a17498c0ea738a069e3b64','createIndex indexName=user_who_created_name_tag, tableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-485','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',538,'EXECUTED','8:b7ac3e14096bec7ec7488c25b0f3a459','createIndex indexName=user_who_created_note, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-486','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',539,'EXECUTED','8:831f45000783dbdccbd088d9f1fe2b94','createIndex indexName=user_who_created_patient, tableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-487','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',540,'EXECUTED','8:6e8dc1731b55a3dd3be6432bb64b5aa4','createIndex indexName=user_who_created_person, tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-488','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',541,'EXECUTED','8:35887a2b53f4cdfc1d6bfbdf1f08c935','createIndex indexName=user_who_created_proposal, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-489','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',542,'EXECUTED','8:4f2fdf3f432374182921e1e7549ec23c','createIndex indexName=user_who_created_rel, tableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-490','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',543,'EXECUTED','8:2e9acebbfe25234f77fb27d8073d54a3','createIndex indexName=user_who_created_type, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-491','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',544,'EXECUTED','8:d095578afb4053e946f971bf040949ba','createIndex indexName=user_who_last_changed_form, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-492','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',545,'EXECUTED','8:57be9930bcfb4298f23e3ed66bbe35b0','createIndex indexName=user_who_last_changed_form_field, tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-493','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',546,'EXECUTED','8:dba003f29a0c1a018bb4d6b19bb8ea82','createIndex indexName=user_who_made_name, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-494','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',547,'EXECUTED','8:56ae508a018a7c6e67d084a80c9930c7','createIndex indexName=user_who_retired_concept, tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-495','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',548,'EXECUTED','8:6519f3e14c682885b22e4a608dcba725','createIndex indexName=user_who_retired_concept_class, tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-496','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',549,'EXECUTED','8:3c215ef898ef35c0ea455f0372ab9744','createIndex indexName=user_who_retired_concept_datatype, tableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-497','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',550,'EXECUTED','8:e93ba205905daf19b24254832d90fd35','createIndex indexName=user_who_retired_concept_source, tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-498','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',551,'EXECUTED','8:4ce039b652acd8b7038d88b40606c889','createIndex indexName=user_who_retired_drug_reference_map, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-499','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',552,'EXECUTED','8:c3d8d48b9bd0a96a14b5af1e78933b09','createIndex indexName=user_who_retired_encounter_type, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-500','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',553,'EXECUTED','8:ddd02afc73e351c8b44fac35c7a272ac','createIndex indexName=user_who_retired_field, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-501','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',554,'EXECUTED','8:23ae98461ab866bc735bd9e6f1750e68','createIndex indexName=user_who_retired_form, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-502','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',555,'EXECUTED','8:5b618c318c3f850d265d8b75325b5452','createIndex indexName=user_who_retired_location, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-503','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',556,'EXECUTED','8:556e2625366df6d72fba9dcc50bcab03','createIndex indexName=user_who_retired_order_type, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-504','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',557,'EXECUTED','8:9d5b5af19e8b5800e85cb71ff61cb927','createIndex indexName=user_who_retired_patient_identifier_type, tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-505','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',558,'EXECUTED','8:bda67eba05113afb500194f8c79d2675','createIndex indexName=user_who_retired_person_attribute_type, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-506','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',559,'EXECUTED','8:90a5635aa3e6f451a31761aa6721cf23','createIndex indexName=user_who_retired_relationship_type, tableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-507','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',560,'EXECUTED','8:b1b0741520592edf278e546075cf3a6a','createIndex indexName=user_who_retired_this_user, tableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-508','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',561,'EXECUTED','8:7c045275e23e0edc8af8db2ba05cf44e','createIndex indexName=user_who_voided_cohort, tableName=cohort','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-509','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',562,'EXECUTED','8:f92c9ecbd5a527301b28351eca4dff67','createIndex indexName=user_who_voided_encounter, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-510','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',563,'EXECUTED','8:bfc6a312f9093b895155853f98fdfef3','createIndex indexName=user_who_voided_name, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-511','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',564,'EXECUTED','8:aa048a9decf3cb8d5b88d000d4a9d6b2','createIndex indexName=user_who_voided_name_tag, tableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-512','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',565,'EXECUTED','8:efbebe330236a246ce86491934380259','createIndex indexName=user_who_voided_obs, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-513','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',566,'EXECUTED','8:69fa65f9ec02c1b5d6f87a39726e2750','createIndex indexName=user_who_voided_order, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-514','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',567,'EXECUTED','8:840b8b22d7581eaa44482ef248361553','createIndex indexName=user_who_voided_patient, tableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-515','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',568,'EXECUTED','8:aa1119223b220560fe92e00184ea7abd','createIndex indexName=user_who_voided_patient_program, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-516','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',569,'EXECUTED','8:1b0828ab9964b093adad262aa817b446','createIndex indexName=user_who_voided_person, tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-517','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',570,'EXECUTED','8:66e99e70702efa7b309ac12d556d566e','createIndex indexName=user_who_voided_report_object, tableName=report_object','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-518','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',571,'EXECUTED','8:e20a5694e67b45b7ecb5410e566c11f6','createIndex indexName=user_who_voided_this_name, tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-519','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',572,'EXECUTED','8:615d8d1c4cc4debb2a34f436262f565c','createIndex indexName=uuid_care_setting, tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-520','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',573,'EXECUTED','8:e886faf65c185292efbfa6abec09e27b','createIndex indexName=uuid_clob_datatype_storage, tableName=clob_datatype_storage','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-521','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',574,'EXECUTED','8:2fcb13bb42ebfe0c32d1cad882519d6c','createIndex indexName=uuid_cohort, tableName=cohort','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-522','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',575,'EXECUTED','8:282138d38a297b81dcecb202d3be291f','createIndex indexName=uuid_cohort_member, tableName=cohort_member','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-523','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:48:59',576,'EXECUTED','8:8c034623a2dc5d702102ef1820c03054','createIndex indexName=uuid_concept, tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-524','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',577,'EXECUTED','8:6126ac43911b68657b65ea896c1174c7','createIndex indexName=uuid_concept_answer, tableName=concept_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-525','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',578,'EXECUTED','8:633f1aec7dd087d7e0dbf6d4c372870d','createIndex indexName=uuid_concept_attribute, tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-526','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',579,'EXECUTED','8:4376f445a25c5b5ff11b587c0639301c','createIndex indexName=uuid_concept_attribute_type, tableName=concept_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-527','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',580,'EXECUTED','8:44c0b31fe18bf32850d632db20b2ade0','createIndex indexName=uuid_concept_class, tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-528','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',581,'EXECUTED','8:d806509f2caf2316e474c4fbe9f15e5b','createIndex indexName=uuid_concept_datatype, tableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-529','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',582,'EXECUTED','8:0444f1ba5fc9a14a35c1581e511e5209','createIndex indexName=uuid_concept_description, tableName=concept_description','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-530','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',583,'EXECUTED','8:06e83d1b5eebbab38d2c5c24c6b9eace','createIndex indexName=uuid_concept_map_type, tableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-531','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',584,'EXECUTED','8:fb9b7c16781ff1fb3986e9f0424c3858','createIndex indexName=uuid_concept_name, tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-532','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',585,'EXECUTED','8:828e36a31197ecd94b5032a65bd3d51a','createIndex indexName=uuid_concept_name_tag, tableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-533','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',586,'EXECUTED','8:d5182d65882c5bf52efde0c54710448f','createIndex indexName=uuid_concept_proposal, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-534','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',587,'EXECUTED','8:4afef575d799fd530d64e0d646609de0','createIndex indexName=uuid_concept_reference_map, tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-535','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',588,'EXECUTED','8:f2b14357b8d781f0430b1af95329a30b','createIndex indexName=uuid_concept_reference_source, tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-536','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',589,'EXECUTED','8:7be4c5c7012ac8250595eed163936017','createIndex indexName=uuid_concept_reference_term, tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-537','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',590,'EXECUTED','8:9a569d16757ec4224843e48c43c120d8','createIndex indexName=uuid_concept_reference_term_map, tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-538','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',591,'EXECUTED','8:52706fb6763b7d2323047446a66a94ac','createIndex indexName=uuid_concept_set, tableName=concept_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-539','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',592,'EXECUTED','8:cfe4f16b4a2bfee696d853b5c3393e61','createIndex indexName=uuid_concept_state_conversion, tableName=concept_state_conversion','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-540','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',593,'EXECUTED','8:097eee9bab0e09f85e3b189ae35fa2e9','createIndex indexName=uuid_conditions, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-541','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',594,'EXECUTED','8:9e1b2910d3b78c7b4a58f9fcbfe738a8','createIndex indexName=uuid_drug, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-542','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',595,'EXECUTED','8:4694e6de93f21a4de4ce4c2a1018962d','createIndex indexName=uuid_drug_ingredient, tableName=drug_ingredient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-543','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',596,'EXECUTED','8:2639385f325b1d03008df17fbc53cd1d','createIndex indexName=uuid_drug_reference_map, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-544','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',597,'EXECUTED','8:8d8faeabcc242bca40ac00a86c6ef13a','createIndex indexName=uuid_encounter, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-545','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',598,'EXECUTED','8:406c4da214dc0a5d96b56445825a45d9','createIndex indexName=uuid_encounter_diagnosis, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-546','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',599,'EXECUTED','8:32e1441fda83b3f09b08495f54224097','createIndex indexName=uuid_encounter_provider, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-547','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',600,'EXECUTED','8:223e7ccad5d92ac9ac6ce466e78149b3','createIndex indexName=uuid_encounter_role, tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-548','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',601,'EXECUTED','8:283bfe503fece369904d99289348dcb7','createIndex indexName=uuid_encounter_type, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-549','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',602,'EXECUTED','8:5147b6dd155cbfac471071e43e5ab63d','createIndex indexName=uuid_field, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-550','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',603,'EXECUTED','8:7aa933f48b1567b874cbed32b7e7facc','createIndex indexName=uuid_field_answer, tableName=field_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-551','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',604,'EXECUTED','8:3de5f3270a63bb332ae84384e4891f3f','createIndex indexName=uuid_field_type, tableName=field_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-552','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',605,'EXECUTED','8:b6b3ae0801cbfbabd935198c84d6f5a5','createIndex indexName=uuid_form, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-553','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',606,'EXECUTED','8:d44c4274900c82f6e726df0c832051a7','createIndex indexName=uuid_form_field, tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-554','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',607,'EXECUTED','8:1ca9493ffc823394b1ff763bdc53359f','createIndex indexName=uuid_form_resource, tableName=form_resource','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-555','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',608,'EXECUTED','8:13eed271d4b2a4ff087afd2b93210e23','createIndex indexName=uuid_global_property, tableName=global_property','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-556','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',609,'EXECUTED','8:f9e55637df872bcb714a21eeb7c2fa01','createIndex indexName=uuid_hl7_in_archive, tableName=hl7_in_archive','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-557','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',610,'EXECUTED','8:0bdca7f6359e1fc2140882724278b182','createIndex indexName=uuid_hl7_in_error, tableName=hl7_in_error','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-558','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',611,'EXECUTED','8:99f1bad1b56e258bf640f07506df8199','createIndex indexName=uuid_hl7_in_queue, tableName=hl7_in_queue','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-559','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',612,'EXECUTED','8:cde9ea9ca79aded8c2ed6ece5c5e415e','createIndex indexName=uuid_hl7_source, tableName=hl7_source','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-560','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',613,'EXECUTED','8:ce86134810c67a0de9164e3ff75b4a91','createIndex indexName=uuid_location, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-561','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',614,'EXECUTED','8:16ab88c13f7f2ba53cc24f8887ce3c47','createIndex indexName=uuid_location_attribute, tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-562','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',615,'EXECUTED','8:81b6e85f9ee792d53b08461d311d88f3','createIndex indexName=uuid_location_attribute_type, tableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-563','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',616,'EXECUTED','8:c806c2eb61f0d4c7024e5062a83f33a1','createIndex indexName=uuid_location_tag, tableName=location_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-564','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',617,'EXECUTED','8:10d17c582a837dc1a1d2dc8d81998689','createIndex indexName=uuid_note, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-565','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',618,'EXECUTED','8:b03da5af51f83613980593b82670ca5c','createIndex indexName=uuid_notification_alert, tableName=notification_alert','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-566','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',619,'EXECUTED','8:ab622a82083bbe1ae3c558b8140ac73f','createIndex indexName=uuid_notification_template, tableName=notification_template','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-567','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',620,'EXECUTED','8:8cd659def795315703eee48c881cbe29','createIndex indexName=uuid_obs, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-568','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',621,'EXECUTED','8:938c4c7000ae719c2c1f33f179b5edf2','createIndex indexName=uuid_order_frequency, tableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-569','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',622,'EXECUTED','8:e1b3011dd209c587856156f278e3d8f9','createIndex indexName=uuid_order_group, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-570','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',623,'EXECUTED','8:26d0f9c7362107a4578ba50eee4e3a7c','createIndex indexName=uuid_order_group_attribute, tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-571','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',624,'EXECUTED','8:a9e412747b7532594d672fad0dcaf898','createIndex indexName=uuid_order_group_attribute_type, tableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-572','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',625,'EXECUTED','8:09712dcc508a5a17f393a3b3374aeeca','createIndex indexName=uuid_order_set, tableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-573','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',626,'EXECUTED','8:c578ca58e004c53375d33d4dd68d530e','createIndex indexName=uuid_order_set_attribute, tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-574','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',627,'EXECUTED','8:f52c57bbeb4dd99bf182fec61e4561dd','createIndex indexName=uuid_order_set_member, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-575','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',628,'EXECUTED','8:ce3300ba331719ed25bb81957cabb9ff','createIndex indexName=uuid_order_type, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-576','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',629,'EXECUTED','8:9ffb56c1902c6f157ff1697eb195d7c9','createIndex indexName=uuid_orders, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-577','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',630,'EXECUTED','8:6c4cd3ef03c3e4115254ba26d3e36211','createIndex indexName=uuid_patient_identifier, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-578','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',631,'EXECUTED','8:83200954f6c9d5e6dc0f788a780e3357','createIndex indexName=uuid_patient_identifier_type, tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-579','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',632,'EXECUTED','8:d7a55d0d66b744db62080bf4ecacf39f','createIndex indexName=uuid_patient_program, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-580','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',633,'EXECUTED','8:4efbfe8c2d21976fd8381551738b0c70','createIndex indexName=uuid_patient_program_attribute, tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-581','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',634,'EXECUTED','8:bd319e6e9ac85db273b802d1d733be5c','createIndex indexName=uuid_patient_state, tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-582','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',635,'EXECUTED','8:8dc72b4ca819c1e7de5aa3ed4b56d67f','createIndex indexName=uuid_person_address, tableName=person_address','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-583','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',636,'EXECUTED','8:da82b5ac5839028d75855498c9a97664','createIndex indexName=uuid_person_attribute, tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-584','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',637,'EXECUTED','8:30f723c741fe8fc98dfb9f1a884b5a4b','createIndex indexName=uuid_person_attribute_type, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-585','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',638,'EXECUTED','8:fc61ad2574d4866a1872721e66175ba9','createIndex indexName=uuid_person_merge_log, tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-586','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',639,'EXECUTED','8:7596a3600e3806af81e9ccc989c2583d','createIndex indexName=uuid_person_name, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-587','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',640,'EXECUTED','8:a319718eec1084aca2296db9515fa9bc','createIndex indexName=uuid_privilege, tableName=privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-588','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',641,'EXECUTED','8:1458dae38e1b91ebd601c7bae1daa537','createIndex indexName=uuid_program, tableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-589','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',642,'EXECUTED','8:0fef693257b5679cbe49731457b57ea4','createIndex indexName=uuid_program_attribute_type, tableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-590','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',643,'EXECUTED','8:3a6acfc955da308b21751f939427570b','createIndex indexName=uuid_program_workflow, tableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-591','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',644,'EXECUTED','8:b1d2a9dd8f28151a49829cb2f871d1e5','createIndex indexName=uuid_program_workflow_state, tableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-592','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',645,'EXECUTED','8:ef81758e91bc5011410856fad5df29b6','createIndex indexName=uuid_provider, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-593','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',646,'EXECUTED','8:8073556d039836758d2036783163a0c2','createIndex indexName=uuid_provider_attribute, tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-594','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',647,'EXECUTED','8:006f5f7e6e8155fc8007848059a6712b','createIndex indexName=uuid_provider_attribute_type, tableName=provider_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-595','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',648,'EXECUTED','8:a1a9ed615a43ee77323f1acf8ec67716','createIndex indexName=uuid_relationship, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-596','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',649,'EXECUTED','8:a08d190fe858505f5aa0002118731f0a','createIndex indexName=uuid_relationship_type, tableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-597','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',650,'EXECUTED','8:897c4c8ce0e87879ee7516180fc7f85b','createIndex indexName=uuid_report_object, tableName=report_object','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-598','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',651,'EXECUTED','8:efbefc03b593d873d7edb241265e61cc','createIndex indexName=uuid_report_schema_xml, tableName=report_schema_xml','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-599','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',652,'EXECUTED','8:ea472a86e036546830cd94d0dd0239e8','createIndex indexName=uuid_role, tableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-600','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',653,'EXECUTED','8:083f19c9950e474e1093dd957671407c','createIndex indexName=uuid_scheduler_task_config, tableName=scheduler_task_config','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-601','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',654,'EXECUTED','8:1372ff83779a8b1c4fe50a848d87b190','createIndex indexName=uuid_serialized_object, tableName=serialized_object','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-602','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',655,'EXECUTED','8:332254ada48e98ee35b2ed2cea9f43d6','createIndex indexName=uuid_visit, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-603','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',656,'EXECUTED','8:0fa6d73b7ea70add4c675f5b53cb00f7','createIndex indexName=uuid_visit_attribute, tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-604','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',657,'EXECUTED','8:5265350241f2bb2b7efd70407931fdff','createIndex indexName=uuid_visit_attribute_type, tableName=visit_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-605','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',658,'EXECUTED','8:592c2f0f72a977e7d68b7f471eedea8a','createIndex indexName=uuid_visit_type, tableName=visit_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-606','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',659,'EXECUTED','8:b9a65b7ab309bdd46ba4f2c449115ff7','createIndex indexName=visit_attribute_attribute_type_id_fk, tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-607','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',660,'EXECUTED','8:bff418618905840385438546a1093fad','createIndex indexName=visit_attribute_changed_by_fk, tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-608','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',661,'EXECUTED','8:a2626b27d70186539932878526685946','createIndex indexName=visit_attribute_creator_fk, tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-609','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',662,'EXECUTED','8:70472d2621b57da066feb029ad765a79','createIndex indexName=visit_attribute_type_changed_by_fk, tableName=visit_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-610','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',663,'EXECUTED','8:9e79bc736ad1da58baa5e7e6a7865b63','createIndex indexName=visit_attribute_type_creator_fk, tableName=visit_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-611','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',664,'EXECUTED','8:41d8b09103b88574501e559342445064','createIndex indexName=visit_attribute_type_retired_by_fk, tableName=visit_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-612','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',665,'EXECUTED','8:7abf34988c450d665e0cdeb647f80119','createIndex indexName=visit_attribute_visit_fk, tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-613','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',666,'EXECUTED','8:78e06dbf873461a07afad0733a4f7be5','createIndex indexName=visit_attribute_voided_by_fk, tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-614','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',667,'EXECUTED','8:f4a37d45253fe3f4ea7bed81a4172541','createIndex indexName=visit_changed_by_fk, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-615','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',668,'EXECUTED','8:821728f2ac5511a778103702e2758d66','createIndex indexName=visit_creator_fk, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-616','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',669,'EXECUTED','8:1a328bcea029e857f7d7bd514eba5c96','createIndex indexName=visit_indication_concept_fk, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-617','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',670,'EXECUTED','8:168b0d2d0e5528d06d2095f7b8ff37a7','createIndex indexName=visit_location_fk, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-618','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',671,'EXECUTED','8:fe8454bcd5df128318c029e22ad9e9ea','createIndex indexName=visit_patient_index, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-619','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',672,'EXECUTED','8:577caf36435f041eb283f8cc50f0325d','createIndex indexName=visit_type_changed_by, tableName=visit_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-620','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',673,'EXECUTED','8:d334a839fdff81402550989673440de6','createIndex indexName=visit_type_creator, tableName=visit_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-621','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',674,'EXECUTED','8:42aae037b40bfe2fae6e57dba78e4381','createIndex indexName=visit_type_fk, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-622','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',675,'EXECUTED','8:c04aab7b591158e11de038130195e63a','createIndex indexName=visit_type_retired_by, tableName=visit_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-623','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',676,'EXECUTED','8:595569367d14b2cdc7d5430359cf3351','createIndex indexName=visit_voided_by_fk, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-624','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',677,'EXECUTED','8:f53f0b393960de128b82445ad97eb01a','createIndex indexName=workflow_changed_by, tableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-625','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',678,'EXECUTED','8:3000c224f29f0dae2ab9005d36b178a0','createIndex indexName=workflow_concept, tableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-626','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',679,'EXECUTED','8:6bcb3a192f7a5cf3c1ace2f3bd41cd77','createIndex indexName=workflow_creator, tableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-627','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',680,'EXECUTED','8:eb75a8f45e6abce58d20c2215b7e4f14','createIndex indexName=workflow_for_state, tableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-628','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',681,'EXECUTED','8:21e4444dccbbb9ffcf79985eae831653','addForeignKeyConstraint baseTableName=person_address, constraintName=address_for_person, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-629','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',682,'EXECUTED','8:f8fbb4d31ed3a0336a6023a0726ab8bf','addForeignKeyConstraint baseTableName=notification_alert, constraintName=alert_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-630','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',683,'EXECUTED','8:6aed63a4d3250cd19e0bf7b77d868b4a','addForeignKeyConstraint baseTableName=notification_alert_recipient, constraintName=alert_read_by_user, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-631','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',684,'EXECUTED','8:62093fa49246930f05a852cad0cf9c94','addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-632','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',685,'EXECUTED','8:627b4123afb4b8a0ad0a87f6d39b0019','addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_coded_allergen_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-633','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',686,'EXECUTED','8:709cd85f99c0f717ecb0f323c49e61b2','addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-634','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',687,'EXECUTED','8:3dce98137a7ed3eb6c76f0e175dc0355','addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_patient_id_fk, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-635','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',688,'EXECUTED','8:cd83523b718ea8201962039359abe096','addForeignKeyConstraint baseTableName=allergy_reaction, constraintName=allergy_reaction_allergy_id_fk, referencedTableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-636','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',689,'EXECUTED','8:7a3a4545172d21601bdbb19a02f6029b','addForeignKeyConstraint baseTableName=allergy_reaction, constraintName=allergy_reaction_reaction_concept_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-637','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',690,'EXECUTED','8:99f3abb29249a480cbc3186d7a85e822','addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_severity_concept_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-638','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',691,'EXECUTED','8:2114890a2aced1cd87422ee331fd20aa','addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-639','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:00',692,'EXECUTED','8:51fc5239565a46a290d349bc6d262d7b','addForeignKeyConstraint baseTableName=concept_answer, constraintName=answer, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-640','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',693,'EXECUTED','8:179aae55c847f4334da18bd4ef85e810','addForeignKeyConstraint baseTableName=concept_answer, constraintName=answer_answer_drug_fk, referencedTableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-641','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',694,'EXECUTED','8:6e0b5cf940460d64f3dc6fdda6f4ca2d','addForeignKeyConstraint baseTableName=obs, constraintName=answer_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-642','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',695,'EXECUTED','8:97f4e978fd1db04d340dc1f145926cfb','addForeignKeyConstraint baseTableName=obs, constraintName=answer_concept_drug, referencedTableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-643','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',696,'EXECUTED','8:2e2acc511f4c99af3398acb5cf0e7a78','addForeignKeyConstraint baseTableName=concept_answer, constraintName=answer_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-644','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',697,'EXECUTED','8:ce6986a76f4a90bc8bf4bf4e6621495d','addForeignKeyConstraint baseTableName=concept_answer, constraintName=answers_for_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-645','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',698,'EXECUTED','8:18e8a645f41407e4f42649245d6e43e7','addForeignKeyConstraint baseTableName=field_answer, constraintName=answers_for_field, referencedTableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-646','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',699,'EXECUTED','8:438477689b2f1ba483c25a65ab64e029','addForeignKeyConstraint baseTableName=person_attribute, constraintName=attribute_changer, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-647','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',700,'EXECUTED','8:ae6f8de598106d7531bc0ba121154c51','addForeignKeyConstraint baseTableName=person_attribute, constraintName=attribute_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-648','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',701,'EXECUTED','8:e3b34c754c33527d3a459fa20e2b94e3','addForeignKeyConstraint baseTableName=person_attribute_type, constraintName=attribute_type_changer, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-649','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',702,'EXECUTED','8:4b6ef3923376e7e0721ccaf9020ede72','addForeignKeyConstraint baseTableName=person_attribute_type, constraintName=attribute_type_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-650','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',703,'EXECUTED','8:ac0fb330641770b623f338d5f4ff5358','addForeignKeyConstraint baseTableName=person_attribute, constraintName=attribute_voider, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-651','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',704,'EXECUTED','8:1ff320a83539608ff8a193844a60f03c','addForeignKeyConstraint baseTableName=care_setting, constraintName=care_setting_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-652','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',705,'EXECUTED','8:d0b918b4fc771f07c0e566fb21e9b8b6','addForeignKeyConstraint baseTableName=care_setting, constraintName=care_setting_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-653','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',706,'EXECUTED','8:ee5680d2bc5dced96ab96cddc844b809','addForeignKeyConstraint baseTableName=care_setting, constraintName=care_setting_retired_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-654','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',707,'EXECUTED','8:6079cb6c0682f1f6ecb2bd93c25e1598','addForeignKeyConstraint baseTableName=order_set, constraintName=category_order_set_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-655','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',708,'EXECUTED','8:0db1d53e9acc90451fc92d3e1e1e7be4','addForeignKeyConstraint baseTableName=cohort, constraintName=cohort_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-656','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',709,'EXECUTED','8:e75a549692dcc556ebf39c3d9667c79d','addForeignKeyConstraint baseTableName=cohort_member, constraintName=cohort_member_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-657','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',710,'EXECUTED','8:021e09eccc220735850c69397d9b6ab0','addForeignKeyConstraint baseTableName=concept_attribute, constraintName=concept_attribute_attribute_type_id_fk, referencedTableName=concept_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-658','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',711,'EXECUTED','8:7385e0991344ccfc5d8c806821095c49','addForeignKeyConstraint baseTableName=concept_attribute, constraintName=concept_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-659','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',712,'EXECUTED','8:bbf31552bcea02b8cc050ea1714d55be','addForeignKeyConstraint baseTableName=concept_attribute, constraintName=concept_attribute_concept_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-660','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',713,'EXECUTED','8:cd37bd06d0fbfe8017ac3cb391ee4323','addForeignKeyConstraint baseTableName=concept_attribute, constraintName=concept_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-661','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',714,'EXECUTED','8:eefaeefbf3c76760769aa5f0ad3839b6','addForeignKeyConstraint baseTableName=concept_attribute_type, constraintName=concept_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-662','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',715,'EXECUTED','8:00bbf1b5ec53c61fcd779a11812408ae','addForeignKeyConstraint baseTableName=concept_attribute_type, constraintName=concept_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-663','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',716,'EXECUTED','8:58897ca4daed63fdbba0e2453d299491','addForeignKeyConstraint baseTableName=concept_attribute_type, constraintName=concept_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-664','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',717,'EXECUTED','8:48c2223b9c175371d6b9ba53823e4973','addForeignKeyConstraint baseTableName=concept_attribute, constraintName=concept_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-665','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',718,'EXECUTED','8:f929b0bfd0dd87fab0c89251957e167d','addForeignKeyConstraint baseTableName=concept_complex, constraintName=concept_attributes, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-666','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',719,'EXECUTED','8:41ab10429622c3a5953e9d817dca547e','addForeignKeyConstraint baseTableName=concept_class, constraintName=concept_class_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-667','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',720,'EXECUTED','8:cb54d714296e4dcb6f9f4f230f9f25fb','addForeignKeyConstraint baseTableName=concept_class, constraintName=concept_class_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-668','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',721,'EXECUTED','8:eba8bd5ca8f41ca5e456cb53a50006a0','addForeignKeyConstraint baseTableName=concept, constraintName=concept_classes, referencedTableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-669','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',722,'EXECUTED','8:6ff8679ef26b961f0a07b668389dbf8a','addForeignKeyConstraint baseTableName=concept, constraintName=concept_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-670','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',723,'EXECUTED','8:22d195ad33f1d77935c59c9193cfd907','addForeignKeyConstraint baseTableName=concept_datatype, constraintName=concept_datatype_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-671','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',724,'EXECUTED','8:b2cf520e20f19628418b52023909d00d','addForeignKeyConstraint baseTableName=concept, constraintName=concept_datatypes, referencedTableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-672','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',725,'EXECUTED','8:ab770f280406848f7b36728484b25804','addForeignKeyConstraint baseTableName=field, constraintName=concept_for_field, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-673','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',726,'EXECUTED','8:e99530b4a995dc6cea349702e50406a6','addForeignKeyConstraint baseTableName=concept_proposal, constraintName=concept_for_proposal, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-674','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',727,'EXECUTED','8:8183e49821e72eb0289753b8f011a664','addForeignKeyConstraint baseTableName=drug_reference_map, constraintName=concept_map_type_for_drug_reference_map, referencedTableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-675','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',728,'EXECUTED','8:cf86f465e88e378e1d3503ebfafa044c','addForeignKeyConstraint baseTableName=concept_name, constraintName=concept_name_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-676','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',729,'EXECUTED','8:fd9c6937014abfbdf61aee886f2acd22','addForeignKeyConstraint baseTableName=concept_name_tag, constraintName=concept_name_tag_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-677','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',730,'EXECUTED','8:d019d1f383bc872c8591a7fde598483c','addForeignKeyConstraint baseTableName=concept_reference_source, constraintName=concept_reference_source_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-678','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',731,'EXECUTED','8:459dfafc34ee923a41535c728aea8141','addForeignKeyConstraint baseTableName=drug_reference_map, constraintName=concept_reference_term_for_drug_reference_map, referencedTableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-679','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',732,'EXECUTED','8:4232ef29a4b143201b39d56b11832ebb','addForeignKeyConstraint baseTableName=concept_reference_source, constraintName=concept_source_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-680','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',733,'EXECUTED','8:55b15bb8e884a146a2505f119672f34b','addForeignKeyConstraint baseTableName=concept_state_conversion, constraintName=concept_triggers_conversion, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-681','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',734,'EXECUTED','8:f54ec9638bf157480a0aa11e34ade225','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-682','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',735,'EXECUTED','8:b2f10c190910bddb9b90fe972f7e536b','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_condition_coded_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-683','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',736,'EXECUTED','8:704a8e3c95d42a350bc1be82cb9231db','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_condition_coded_name_fk, referencedTableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-684','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',737,'EXECUTED','8:d63bc9fbedc29df5c79dd2025637fbaa','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-685','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',738,'EXECUTED','8:7aefaa5685c2bc534197737aa38b2d03','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_patient_fk, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-686','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',739,'EXECUTED','8:ebaa5d6c4bf4324fcde5ff97f53b621c','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_previous_version_fk, referencedTableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-687','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:01',740,'EXECUTED','8:f2ec27dbee2e559dc3c27b4e28aeed88','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-688','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',741,'EXECUTED','8:2b29be399565099eb0ecae1a232ed977','addForeignKeyConstraint baseTableName=conditions, constraintName=conditions_encounter_id_fk, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-689','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',742,'EXECUTED','8:d621b28037111ad0e3f38f4758442799','addForeignKeyConstraint baseTableName=concept_state_conversion, constraintName=conversion_involves_workflow, referencedTableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-690','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',743,'EXECUTED','8:46c431397c29ca016df9ff6ec59ebeef','addForeignKeyConstraint baseTableName=concept_state_conversion, constraintName=conversion_to_state, referencedTableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-691','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',744,'EXECUTED','8:6865a1067e1322b6983e863a6423abce','addForeignKeyConstraint baseTableName=person_attribute, constraintName=defines_attribute_type, referencedTableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-692','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',745,'EXECUTED','8:5de385975ca54cf445dca515599dce4a','addForeignKeyConstraint baseTableName=patient_identifier, constraintName=defines_identifier_type, referencedTableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-693','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',746,'EXECUTED','8:ca3d804bc69117d7196ba07535c55616','addForeignKeyConstraint baseTableName=concept_description, constraintName=description_for_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-694','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',747,'EXECUTED','8:d4c32666dbaad85b2f9d1205cb009379','addForeignKeyConstraint baseTableName=orders, constraintName=discontinued_because, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-695','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',748,'EXECUTED','8:552e8c193ed0293ab6784cd2c0240347','addForeignKeyConstraint baseTableName=drug, constraintName=dosage_form_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-696','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',749,'EXECUTED','8:ea226408f1b91fe254ead838c4bcfb1c','addForeignKeyConstraint baseTableName=drug, constraintName=drug_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-697','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',750,'EXECUTED','8:19d1ad53b74d0fa79f7c53d0b285e782','addForeignKeyConstraint baseTableName=drug, constraintName=drug_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-698','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',751,'EXECUTED','8:92254e82b2a826e1abed02bfe818ef94','addForeignKeyConstraint baseTableName=drug, constraintName=drug_dose_limit_units_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-699','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',752,'EXECUTED','8:550473a898fb2adf90ec003baed81e18','addForeignKeyConstraint baseTableName=drug_reference_map, constraintName=drug_for_drug_reference_map, referencedTableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-700','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',753,'EXECUTED','8:d3b694d9711ca75ad013a7b2c43a37f1','addForeignKeyConstraint baseTableName=drug_ingredient, constraintName=drug_ingredient_drug_id_fk, referencedTableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-701','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',754,'EXECUTED','8:89cf7a97bcc99e3fbd9a730b13862250','addForeignKeyConstraint baseTableName=drug_ingredient, constraintName=drug_ingredient_ingredient_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-702','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',755,'EXECUTED','8:1e1453c761a933bb931ed579bcf2fbd9','addForeignKeyConstraint baseTableName=drug_ingredient, constraintName=drug_ingredient_units_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-703','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',756,'EXECUTED','8:834c46f1296b4b879c5255b9d5711efc','addForeignKeyConstraint baseTableName=drug_order, constraintName=drug_order_dose_units, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-704','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',757,'EXECUTED','8:45c24a24e91405ebcbc399245e6aa9fc','addForeignKeyConstraint baseTableName=drug_order, constraintName=drug_order_duration_units_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-705','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',758,'EXECUTED','8:6acf2370b8ca38a3a0bd45ba93af17c0','addForeignKeyConstraint baseTableName=drug_order, constraintName=drug_order_frequency_fk, referencedTableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-706','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',759,'EXECUTED','8:d29ffc460e25957deb024daf835959ff','addForeignKeyConstraint baseTableName=drug_order, constraintName=drug_order_quantity_units, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-707','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',760,'EXECUTED','8:e9a593d247aa42e0c3312971e70a0a83','addForeignKeyConstraint baseTableName=drug_order, constraintName=drug_order_route_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-708','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',761,'EXECUTED','8:18e8f99fe4dc5b41eef2749d7e8c4378','addForeignKeyConstraint baseTableName=drug_reference_map, constraintName=drug_reference_map_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-709','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',762,'EXECUTED','8:62239dfc46c6261b819cfc325689b72d','addForeignKeyConstraint baseTableName=drug, constraintName=drug_retired_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-710','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',763,'EXECUTED','8:46d37cc722f5aaf21abffde567de9490','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-711','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',764,'EXECUTED','8:d181df09c383122a23ab7292f91fb8ab','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-712','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',765,'EXECUTED','8:3b54116dc075d8f76db6b03d0c20297b','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_coded_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-713','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',766,'EXECUTED','8:124aadd3d48288d84f0f25a1910f23d7','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_coded_name_fk, referencedTableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-714','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',767,'EXECUTED','8:db06bdbe85cdc65e7a1c302f2cc0cb40','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_condition_id_fk, referencedTableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-715','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',768,'EXECUTED','8:f1b5042e0b19475fe81463316ecdb305','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-716','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',769,'EXECUTED','8:07320bedaca7821ea6a42d768066ae08','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_encounter_id_fk, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-717','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',770,'EXECUTED','8:df53582256da26d4215450e651aeb611','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_patient_fk, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-718','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',771,'EXECUTED','8:ba41a6241d972c59e6fab8183b8903ed','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-719','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',772,'EXECUTED','8:9095e3455d0d74c8eb236a089e891f30','addForeignKeyConstraint baseTableName=concept_proposal, constraintName=encounter_for_proposal, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-720','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',773,'EXECUTED','8:9d9148db86066d8accf65a3573e9225f','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_form, referencedTableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-721','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',774,'EXECUTED','8:44688bbda008838d9cff13981bd0e618','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_ibfk_1, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-722','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',775,'EXECUTED','8:de938cb3f56e4006356bab461f48613b','addForeignKeyConstraint baseTableName=encounter_provider, constraintName=encounter_id_fk, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-723','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',776,'EXECUTED','8:3887c054e5cc31b74eb9da3f18ad6f1d','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_location, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-724','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',777,'EXECUTED','8:301f43698f7f418a04b5bb46303c37b7','addForeignKeyConstraint baseTableName=note, constraintName=encounter_note, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-725','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',778,'EXECUTED','8:1533bc819393e19d5cd6b685f12e1dfd','addForeignKeyConstraint baseTableName=obs, constraintName=encounter_observations, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-726','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',779,'EXECUTED','8:54860a8c7870abf426eecabf87d2bbb4','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_patient, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-727','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',780,'EXECUTED','8:8059e13020d9b3b831c0e84fdd9f35f3','addForeignKeyConstraint baseTableName=encounter_provider, constraintName=encounter_provider_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-728','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',781,'EXECUTED','8:a2de6b3cd36e31fa2479ba4a8f419976','addForeignKeyConstraint baseTableName=encounter_provider, constraintName=encounter_provider_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-729','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',782,'EXECUTED','8:49fc50403bd138f9540de67acdd56ba5','addForeignKeyConstraint baseTableName=encounter_provider, constraintName=encounter_provider_voided_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-730','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',783,'EXECUTED','8:1c04c7b8260bb52579894dfd41bc7ae8','addForeignKeyConstraint baseTableName=encounter_role, constraintName=encounter_role_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-731','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',784,'EXECUTED','8:b4cdcc63e08f474c8fb7b7c3f11bbecc','addForeignKeyConstraint baseTableName=encounter_role, constraintName=encounter_role_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-732','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',785,'EXECUTED','8:a6284ec6b62d4c67c648284299ed6c2c','addForeignKeyConstraint baseTableName=encounter_provider, constraintName=encounter_role_id_fk, referencedTableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-733','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',786,'EXECUTED','8:6ede99ec449cc4ad1810a85937f8fb1a','addForeignKeyConstraint baseTableName=encounter_role, constraintName=encounter_role_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-734','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',787,'EXECUTED','8:7e90744d284d34c8c24c3ca40d17ee46','addForeignKeyConstraint baseTableName=encounter_type, constraintName=encounter_type_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-735','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',788,'EXECUTED','8:31f1cc3a5ef337a9e2e7d925d968b40f','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_type_id, referencedTableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-736','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',789,'EXECUTED','8:96900e1f9722443e27fc0f4fd8fc9c94','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_visit_id_fk, referencedTableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-737','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',790,'EXECUTED','8:ec809e1d1fb0bb80d27772c601a316fd','addForeignKeyConstraint baseTableName=drug_order, constraintName=extends_order, referencedTableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-738','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:02',791,'EXECUTED','8:a7ddedb1f7d304cd0c1fa4fc3614e801','addForeignKeyConstraint baseTableName=field_answer, constraintName=field_answer_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-739','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',792,'EXECUTED','8:462867755db1686fa0e869f702a7a607','addForeignKeyConstraint baseTableName=form_field, constraintName=field_within_form, referencedTableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-740','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',793,'EXECUTED','8:7ddfef12f5dd33eb33fa790d08703557','addForeignKeyConstraint baseTableName=order_type_class_map, constraintName=fk_order_type_class_map_concept_class_concept_class_id, referencedTableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-741','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',794,'EXECUTED','8:dc0e0afa49e37712f28c6a46a8aad2fe','addForeignKeyConstraint baseTableName=order_type_class_map, constraintName=fk_order_type_order_type_id, referencedTableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-742','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',795,'EXECUTED','8:13446e40e44b0402e59022507c21e455','addForeignKeyConstraint baseTableName=orders, constraintName=fk_orderer_provider, referencedTableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-743','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',796,'EXECUTED','8:092613b9ee888f657c30543538f39260','addForeignKeyConstraint baseTableName=patient_identifier, constraintName=fk_patient_id_patient_identifier, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-744','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',797,'EXECUTED','8:81b88efda66069fed3e8efd4949bb5c9','addForeignKeyConstraint baseTableName=form_field, constraintName=form_containing_field, referencedTableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-745','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',798,'EXECUTED','8:581804e4c331f9e9d120460cb3d911f1','addForeignKeyConstraint baseTableName=form, constraintName=form_encounter_type, referencedTableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-746','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',799,'EXECUTED','8:d548b5df5ade6c41909d20883f6d3603','addForeignKeyConstraint baseTableName=form_field, constraintName=form_field_hierarchy, referencedTableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-747','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',800,'EXECUTED','8:5c8a2b583f468b001c99f42240fef8fb','addForeignKeyConstraint baseTableName=form_resource, constraintName=form_resource_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-748','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',801,'EXECUTED','8:634fd2b4c9e9353d4fc0217e02f9f536','addForeignKeyConstraint baseTableName=form_resource, constraintName=form_resource_form_fk, referencedTableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-749','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',802,'EXECUTED','8:d01943188cc9a7de6d477ce03d9b75d8','addForeignKeyConstraint baseTableName=global_property, constraintName=global_property_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-750','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',803,'EXECUTED','8:554f3ecd038c24ad03c11f90a9eed5b2','addForeignKeyConstraint baseTableName=concept_set, constraintName=has_a, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-751','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',804,'EXECUTED','8:34d1f304af0097cc11d84a3a197c0888','addForeignKeyConstraint baseTableName=hl7_in_queue, constraintName=hl7_source_with_queue, referencedTableName=hl7_source','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-752','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',805,'EXECUTED','8:c80769ca20a15737c5324380cfde8fa5','addForeignKeyConstraint baseTableName=notification_alert_recipient, constraintName=id_of_alert, referencedTableName=notification_alert','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-753','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',806,'EXECUTED','8:4575acff7e7864408a6b80f6593468fc','addForeignKeyConstraint baseTableName=patient_identifier, constraintName=identifier_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-754','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',807,'EXECUTED','8:305c970aafd1bc1938958c621a91ebce','addForeignKeyConstraint baseTableName=patient_identifier, constraintName=identifier_voider, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-755','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',808,'EXECUTED','8:f9a9e3a536c17c2c156d53692358a3dd','addForeignKeyConstraint baseTableName=person_attribute, constraintName=identifies_person, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-756','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',809,'EXECUTED','8:24c34b76746ec045dee8a349aa56ac9c','addForeignKeyConstraint baseTableName=role_role, constraintName=inherited_role, referencedTableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-757','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',810,'EXECUTED','8:f549f14e485daa46df216d897b16112d','addForeignKeyConstraint baseTableName=drug_order, constraintName=inventory_item, referencedTableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-758','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',811,'EXECUTED','8:33d706edd195109d4a6340ca29863bec','addForeignKeyConstraint baseTableName=location_attribute, constraintName=location_attribute_attribute_type_id_fk, referencedTableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-759','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',812,'EXECUTED','8:b4f4a7aded7b47acd2f19d3ea5ed9c38','addForeignKeyConstraint baseTableName=location_attribute, constraintName=location_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-760','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',813,'EXECUTED','8:557b52f502c3d90d97069fb071661a0b','addForeignKeyConstraint baseTableName=location_attribute, constraintName=location_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-761','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',814,'EXECUTED','8:770021fdec02992fa3681d043eec83eb','addForeignKeyConstraint baseTableName=location_attribute, constraintName=location_attribute_location_fk, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-762','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',815,'EXECUTED','8:8b4f373bae9db1fd844a9864e5cdddba','addForeignKeyConstraint baseTableName=location_attribute_type, constraintName=location_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-763','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',816,'EXECUTED','8:d84f3918d536bbf5307d923d7f3020be','addForeignKeyConstraint baseTableName=location_attribute_type, constraintName=location_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-764','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',817,'EXECUTED','8:87923b1e7201befca237a14aeec0a9fa','addForeignKeyConstraint baseTableName=location_attribute_type, constraintName=location_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-765','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',818,'EXECUTED','8:b0f81097e2d6e80b7e9a55e1e6a6588b','addForeignKeyConstraint baseTableName=location_attribute, constraintName=location_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-766','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',819,'EXECUTED','8:0ebfc2ccea985d43830b059418c925c6','addForeignKeyConstraint baseTableName=location, constraintName=location_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-767','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',820,'EXECUTED','8:b9659675bcdb5b15be5d3e04d1b306bd','addForeignKeyConstraint baseTableName=location_tag, constraintName=location_tag_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-768','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',821,'EXECUTED','8:1a6596d2d7d328fb949405e3cf585e16','addForeignKeyConstraint baseTableName=location_tag, constraintName=location_tag_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-769','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',822,'EXECUTED','8:a0ae313e026a7f6f9cee4278859e1dcc','addForeignKeyConstraint baseTableName=location_tag_map, constraintName=location_tag_map_location, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-770','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',823,'EXECUTED','8:c51245b6e4d91a95ada1eba7bf8bcb0c','addForeignKeyConstraint baseTableName=location_tag_map, constraintName=location_tag_map_tag, referencedTableName=location_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-771','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',824,'EXECUTED','8:d51a90d2c4de90a286cc7bf3c10f57b7','addForeignKeyConstraint baseTableName=location_tag, constraintName=location_tag_retired_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-772','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',825,'EXECUTED','8:1f608428ac7658d92103ea3d69e6ae9f','addForeignKeyConstraint baseTableName=concept_reference_map, constraintName=map_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-773','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',826,'EXECUTED','8:be49584e8688d0d3b6f878d8dca29f5a','addForeignKeyConstraint baseTableName=concept_reference_map, constraintName=map_for_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-774','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',827,'EXECUTED','8:d416fa1984701b2cebe9e1e2b6c3b8f2','addForeignKeyConstraint baseTableName=concept_reference_map, constraintName=mapped_concept_map_type, referencedTableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-775','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',828,'EXECUTED','8:1e0439821c666f1c716ba26c80ef7701','addForeignKeyConstraint baseTableName=concept_reference_term_map, constraintName=mapped_concept_map_type_ref_term_map, referencedTableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-776','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',829,'EXECUTED','8:b2dddec16d2679c0990c2035f9e78e00','addForeignKeyConstraint baseTableName=concept_name_tag_map, constraintName=mapped_concept_name, referencedTableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-777','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',830,'EXECUTED','8:12c7cf8c691aa926199519434ad03413','addForeignKeyConstraint baseTableName=concept_name_tag_map, constraintName=mapped_concept_name_tag, referencedTableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-778','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',831,'EXECUTED','8:0bec568cbeb81dbe2518cff57be788e3','addForeignKeyConstraint baseTableName=concept_proposal_tag_map, constraintName=mapped_concept_proposal, referencedTableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-779','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',832,'EXECUTED','8:8e6a0691fc1f926acd3a4d26d41fb010','addForeignKeyConstraint baseTableName=concept_proposal_tag_map, constraintName=mapped_concept_proposal_tag, referencedTableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-780','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',833,'EXECUTED','8:788154a99a273faaf0d4e352793b5f1c','addForeignKeyConstraint baseTableName=concept_reference_map, constraintName=mapped_concept_reference_term, referencedTableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-781','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',834,'EXECUTED','8:9a18311294e0ef7d6019959277730b19','addForeignKeyConstraint baseTableName=concept_reference_term, constraintName=mapped_concept_source, referencedTableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-782','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',835,'EXECUTED','8:8a681c7d2ae4d9334fd0602144b878fd','addForeignKeyConstraint baseTableName=concept_reference_term_map, constraintName=mapped_term_a, referencedTableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-783','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',836,'EXECUTED','8:212689ff4e3937f4e8ccafde1e9c33ca','addForeignKeyConstraint baseTableName=concept_reference_term_map, constraintName=mapped_term_b, referencedTableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-784','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',837,'EXECUTED','8:574be9614f17cc39f647467658459bb5','addForeignKeyConstraint baseTableName=concept_reference_term, constraintName=mapped_user_changed, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-785','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',838,'EXECUTED','8:d604b0c1a59f8371919d17eca4fd9fb4','addForeignKeyConstraint baseTableName=concept_map_type, constraintName=mapped_user_changed_concept_map_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-786','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',839,'EXECUTED','8:9fad410381b49fff0e9845bd52b01979','addForeignKeyConstraint baseTableName=concept_reference_map, constraintName=mapped_user_changed_ref_term, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-787','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',840,'EXECUTED','8:a3fe5cfe7315c7f199ea9b6cb8f14cc2','addForeignKeyConstraint baseTableName=concept_reference_term_map, constraintName=mapped_user_changed_ref_term_map, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-788','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',841,'EXECUTED','8:bf67dbdd2a18d66991ac19afe6613cb7','addForeignKeyConstraint baseTableName=concept_reference_term, constraintName=mapped_user_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-789','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',842,'EXECUTED','8:7af1ad5eb936878b328315c223486a1f','addForeignKeyConstraint baseTableName=concept_map_type, constraintName=mapped_user_creator_concept_map_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-790','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:03',843,'EXECUTED','8:d15a3809e6bde80dfb09b890cd7dc7da','addForeignKeyConstraint baseTableName=concept_reference_term_map, constraintName=mapped_user_creator_ref_term_map, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-791','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',844,'EXECUTED','8:d0ed2f4f2c62732f945530fe5260e2f1','addForeignKeyConstraint baseTableName=concept_reference_term, constraintName=mapped_user_retired, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-792','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',845,'EXECUTED','8:384de9582bfb3bf713164815d6eb3ef8','addForeignKeyConstraint baseTableName=concept_map_type, constraintName=mapped_user_retired_concept_map_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-793','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',846,'EXECUTED','8:6e45c7a91e88c35f8647eee56578b655','addForeignKeyConstraint baseTableName=cohort_member, constraintName=member_patient, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-794','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',847,'EXECUTED','8:e2bb60d7a69cc6866ce15cd7c01e81db','addForeignKeyConstraint baseTableName=concept_name, constraintName=name_for_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-795','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',848,'EXECUTED','8:8fcf4f6343d695755508cc42b4a4bd96','addForeignKeyConstraint baseTableName=person_name, constraintName=name_for_person, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-796','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',849,'EXECUTED','8:1f635a1b5b931f3cf99f23b42375cc6f','addForeignKeyConstraint baseTableName=note, constraintName=note_hierarchy, referencedTableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-797','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',850,'EXECUTED','8:3505f76084af733f6cccbf76e5c38d83','addForeignKeyConstraint baseTableName=concept_numeric, constraintName=numeric_attributes, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-798','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',851,'EXECUTED','8:c9732c3472d8ea7432d7f81f6f5a364d','addForeignKeyConstraint baseTableName=obs, constraintName=obs_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-799','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',852,'EXECUTED','8:8268277839315e6c98c9628043e7ff7a','addForeignKeyConstraint baseTableName=obs, constraintName=obs_enterer, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-800','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',853,'EXECUTED','8:978d54886f99fefb9e7ad0dc6b22039a','addForeignKeyConstraint baseTableName=obs, constraintName=obs_grouping_id, referencedTableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-801','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',854,'EXECUTED','8:17401ff295cbe4915de4c9525aa78d5b','addForeignKeyConstraint baseTableName=obs, constraintName=obs_location, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-802','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',855,'EXECUTED','8:3a81fc6dbab540a5c54aff400e275a1f','addForeignKeyConstraint baseTableName=obs, constraintName=obs_name_of_coded_value, referencedTableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-803','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',856,'EXECUTED','8:fdd335669cdfbf9d32c61431f90a33c2','addForeignKeyConstraint baseTableName=note, constraintName=obs_note, referencedTableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-804','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',857,'EXECUTED','8:432c70cfc7e56e006716bcb9b2081d5e','addForeignKeyConstraint baseTableName=obs, constraintName=obs_order, referencedTableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-805','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',858,'EXECUTED','8:c0cc8be50e42088cf80d11f4e163ea7d','addForeignKeyConstraint baseTableName=orders, constraintName=order_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-806','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',859,'EXECUTED','8:f685e35a4e9b7be9d77310c3ee7e6650','addForeignKeyConstraint baseTableName=orders, constraintName=order_for_patient, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-807','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',860,'EXECUTED','8:35ee5d376901b289c588811974e87a7d','addForeignKeyConstraint baseTableName=order_frequency, constraintName=order_frequency_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-808','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',861,'EXECUTED','8:ed8b876f5f143f0cba8e6cbe34736fb1','addForeignKeyConstraint baseTableName=order_frequency, constraintName=order_frequency_concept_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-809','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',862,'EXECUTED','8:fa666fcbaf13dda689b99cc7cf461f73','addForeignKeyConstraint baseTableName=order_frequency, constraintName=order_frequency_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-810','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',863,'EXECUTED','8:a42da69630cf17b47154a3f920f11945','addForeignKeyConstraint baseTableName=order_frequency, constraintName=order_frequency_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-811','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',864,'EXECUTED','8:b6d8666b5a284fe45a0a99bdec3ae05f','addForeignKeyConstraint baseTableName=order_group_attribute, constraintName=order_group_attribute_attribute_type_id_fk, referencedTableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-812','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',865,'EXECUTED','8:d5b3a8130e70af6e1bad1d45af868161','addForeignKeyConstraint baseTableName=order_group_attribute, constraintName=order_group_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-813','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',866,'EXECUTED','8:2d97855468cd1eb3a913e0619be17cc5','addForeignKeyConstraint baseTableName=order_group_attribute, constraintName=order_group_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-814','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',867,'EXECUTED','8:134e39fdd57e873f854fe6330f152131','addForeignKeyConstraint baseTableName=order_group_attribute, constraintName=order_group_attribute_order_group_fk, referencedTableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-815','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',868,'EXECUTED','8:41eed75e834cd501df0635e629086869','addForeignKeyConstraint baseTableName=order_group_attribute_type, constraintName=order_group_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-816','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',869,'EXECUTED','8:af578c3200ea4e8d6a7ee67830e2dee3','addForeignKeyConstraint baseTableName=order_group_attribute_type, constraintName=order_group_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-817','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',870,'EXECUTED','8:93a543e85718bc84800ff2ef6f1d56be','addForeignKeyConstraint baseTableName=order_group_attribute_type, constraintName=order_group_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-818','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',871,'EXECUTED','8:237fe0e6b320c65d53b09adbce550815','addForeignKeyConstraint baseTableName=order_group_attribute, constraintName=order_group_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-819','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',872,'EXECUTED','8:9afd39bfd39b523493df6ca7a18959ad','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-820','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',873,'EXECUTED','8:165a8e6fd586ec7b9aaba466a0e72b72','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-821','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',874,'EXECUTED','8:62befe3cbb631d4cce795c3bd0ffce33','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_encounter_id_fk, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-822','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',875,'EXECUTED','8:434d6baf570210d69bf2cd1563523c3a','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_order_group_reason_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-823','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',876,'EXECUTED','8:1077139c52eafdb43b82685cdda42094','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_parent_order_group_fk, referencedTableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-824','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',877,'EXECUTED','8:cfba1c3e93a86bf7f0d5cbad7e6fb11a','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_patient_id_fk, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-825','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',878,'EXECUTED','8:9254a443cd8cb8563080beab50178b80','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_previous_order_group_fk, referencedTableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-826','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',879,'EXECUTED','8:adf9e68f34363f7ab5225c34619aeb72','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_set_id_fk, referencedTableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-827','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',880,'EXECUTED','8:a3831c1d7876bf02b28394b18241d456','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-828','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',881,'EXECUTED','8:c5798f9d76b0410ee0751458ad63ce10','addForeignKeyConstraint baseTableName=order_set_attribute, constraintName=order_set_attribute_attribute_type_id_fk, referencedTableName=order_set_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-829','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',882,'EXECUTED','8:10f8a8f94c59c1ca08f3d92af41d6177','addForeignKeyConstraint baseTableName=order_set_attribute, constraintName=order_set_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-830','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',883,'EXECUTED','8:8effc2d76e6e2db156d78ba477612b8d','addForeignKeyConstraint baseTableName=order_set_attribute, constraintName=order_set_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-831','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',884,'EXECUTED','8:6ba2f04b606798319b688fbc3ec66da8','addForeignKeyConstraint baseTableName=order_set_attribute, constraintName=order_set_attribute_order_set_fk, referencedTableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-832','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',885,'EXECUTED','8:8c138d212ce61dfe85d4cc94152b0214','addForeignKeyConstraint baseTableName=order_set_attribute_type, constraintName=order_set_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-833','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:04',886,'EXECUTED','8:1ffd97c016e06a9364eafe58d7bb9c10','addForeignKeyConstraint baseTableName=order_set_attribute_type, constraintName=order_set_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-834','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',887,'EXECUTED','8:840fd6d949b76467eca0a45df50b5455','addForeignKeyConstraint baseTableName=order_set_attribute_type, constraintName=order_set_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-835','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',888,'EXECUTED','8:5be1cea2506a9b4bf28e88f03d2ac435','addForeignKeyConstraint baseTableName=order_set_attribute, constraintName=order_set_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-836','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',889,'EXECUTED','8:d6a5dd977622b9a45104b639fea27057','addForeignKeyConstraint baseTableName=order_set, constraintName=order_set_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-837','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',890,'EXECUTED','8:a33e570f46675760d7f918cafe09f470','addForeignKeyConstraint baseTableName=order_set, constraintName=order_set_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-838','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',891,'EXECUTED','8:7fc0b8b57c306844f3f967e9ea88ba95','addForeignKeyConstraint baseTableName=order_set_member, constraintName=order_set_member_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-839','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',892,'EXECUTED','8:f5dbac0b9db31f6d17e7d9e4b4ecc377','addForeignKeyConstraint baseTableName=order_set_member, constraintName=order_set_member_concept_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-840','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',893,'EXECUTED','8:38c854821271ac80902335e4ee69711b','addForeignKeyConstraint baseTableName=order_set_member, constraintName=order_set_member_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-841','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',894,'EXECUTED','8:2d496e3f4c55684c462f610904dfc668','addForeignKeyConstraint baseTableName=order_set_member, constraintName=order_set_member_order_set_id_fk, referencedTableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-842','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',895,'EXECUTED','8:7eeb953070b3e000bf2c8588532a1ce2','addForeignKeyConstraint baseTableName=order_set_member, constraintName=order_set_member_order_type_fk, referencedTableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-843','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',896,'EXECUTED','8:0526053665c948124eea436de5222a65','addForeignKeyConstraint baseTableName=order_set_member, constraintName=order_set_member_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-844','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',897,'EXECUTED','8:732454ce0f5ed67b04c53e5b9aa3e9c6','addForeignKeyConstraint baseTableName=order_set, constraintName=order_set_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-845','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',898,'EXECUTED','8:435a1e100712f1e93b3b65b003a86ac3','addForeignKeyConstraint baseTableName=order_type, constraintName=order_type_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-846','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',899,'EXECUTED','8:d34a6c90298741958538167827a6b3dd','addForeignKeyConstraint baseTableName=order_type, constraintName=order_type_parent_order_type, referencedTableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-847','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',900,'EXECUTED','8:05590ce6d45e9d21de5f2d145d0b7695','addForeignKeyConstraint baseTableName=orders, constraintName=orders_care_setting, referencedTableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-848','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',901,'EXECUTED','8:35723632c7575cb3d80b418598356632','addForeignKeyConstraint baseTableName=orders, constraintName=orders_in_encounter, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-849','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',902,'EXECUTED','8:b916b89186b29d6ddb09be718e528973','addForeignKeyConstraint baseTableName=orders, constraintName=orders_order_group_id_fk, referencedTableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-850','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',903,'EXECUTED','8:50107b202250e00a620f9f972a017fc9','addForeignKeyConstraint baseTableName=cohort_member, constraintName=parent_cohort, referencedTableName=cohort','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-851','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',904,'EXECUTED','8:36d997ad0f295fd015f873b1d130013b','addForeignKeyConstraint baseTableName=location, constraintName=parent_location, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-852','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',905,'EXECUTED','8:b5aa149236a07ecdd72a57127f4a75d2','addForeignKeyConstraint baseTableName=role_role, constraintName=parent_role, referencedTableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-853','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',906,'EXECUTED','8:5d087e8d6ad7c8e2d7c1fb3228468d27','addForeignKeyConstraint baseTableName=person_address, constraintName=patient_address_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-854','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',907,'EXECUTED','8:90a22152501f9631b0014a93179f965a','addForeignKeyConstraint baseTableName=person_address, constraintName=patient_address_void, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-855','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',908,'EXECUTED','8:020f0a732ee5a6f8661f3cafc9fc55b4','addForeignKeyConstraint baseTableName=patient_identifier, constraintName=patient_identifier_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-856','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',909,'EXECUTED','8:6fadd0b9f398428c180326f1473c39de','addForeignKeyConstraint baseTableName=patient_identifier, constraintName=patient_identifier_ibfk_2, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-857','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',910,'EXECUTED','8:43aaee8b48063350605399fc915d314c','addForeignKeyConstraint baseTableName=patient_identifier_type, constraintName=patient_identifier_type_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-858','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',911,'EXECUTED','8:23b0efcc933d062f0118f49b8058cad5','addForeignKeyConstraint baseTableName=patient_program, constraintName=patient_in_program, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-859','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',912,'EXECUTED','8:76e9bc625c11684e3edc195ef8b3b1e9','addForeignKeyConstraint baseTableName=note, constraintName=patient_note, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-860','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',913,'EXECUTED','8:32b8ebd8d130b8f175c87684d8bc2d1c','addForeignKeyConstraint baseTableName=patient_program_attribute, constraintName=patient_program_attribute_attributetype_fk, referencedTableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-861','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',914,'EXECUTED','8:72fea5869b6141a1cf80603a5de3558d','addForeignKeyConstraint baseTableName=patient_program_attribute, constraintName=patient_program_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-862','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',915,'EXECUTED','8:851e0d7e2d1dbe924624ea405fd6cc94','addForeignKeyConstraint baseTableName=patient_program_attribute, constraintName=patient_program_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-863','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',916,'EXECUTED','8:02385045d5711d24d92f78dc108d99ca','addForeignKeyConstraint baseTableName=patient_program_attribute, constraintName=patient_program_attribute_programid_fk, referencedTableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-864','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',917,'EXECUTED','8:f73483a028df661a2c26d1323f3c8451','addForeignKeyConstraint baseTableName=patient_program_attribute, constraintName=patient_program_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-865','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',918,'EXECUTED','8:a1fe4a7f940aa20f1a6651c1964577c2','addForeignKeyConstraint baseTableName=patient_program, constraintName=patient_program_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-866','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',919,'EXECUTED','8:751d160f00ab311740538463c7cfb69b','addForeignKeyConstraint baseTableName=patient_state, constraintName=patient_program_for_state, referencedTableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-867','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',920,'EXECUTED','8:8b152de3747c8b27f678c4e9387535cf','addForeignKeyConstraint baseTableName=patient_program, constraintName=patient_program_location_id, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-868','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',921,'EXECUTED','8:ca216dc9b815b69e18ab3771a0772427','addForeignKeyConstraint baseTableName=patient_program, constraintName=patient_program_outcome_concept_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-869','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',922,'EXECUTED','8:91f1a39c3b6007783100753e95b429a5','addForeignKeyConstraint baseTableName=patient_state, constraintName=patient_state_changer, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-870','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',923,'EXECUTED','8:12838849537a52d96e46e3745a8997dd','addForeignKeyConstraint baseTableName=patient_state, constraintName=patient_state_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-871','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',924,'EXECUTED','8:598555dff114215f0080878d92cd6a2b','addForeignKeyConstraint baseTableName=patient_state, constraintName=patient_state_voider, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-872','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',925,'EXECUTED','8:ee8e441bcba80d23fc6415d1c9307d5f','addForeignKeyConstraint baseTableName=relationship, constraintName=person_a_is_person, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-873','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',926,'EXECUTED','8:0d06092ed078138eebcbe296d50db88e','addForeignKeyConstraint baseTableName=person_address, constraintName=person_address_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-874','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',927,'EXECUTED','8:c620d324af31116f91d22f7ac9e60440','addForeignKeyConstraint baseTableName=relationship, constraintName=person_b_is_person, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-875','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',928,'EXECUTED','8:9cadafea81dd484831240e834e846934','addForeignKeyConstraint baseTableName=person, constraintName=person_died_because, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-876','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',929,'EXECUTED','8:112516eb2ddcdb1b30ad14897fabd706','addForeignKeyConstraint baseTableName=patient, constraintName=person_id_for_patient, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-877','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',930,'EXECUTED','8:d308c4c8ed3fbf658a0310dc458d6636','addForeignKeyConstraint baseTableName=users, constraintName=person_id_for_user, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-878','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',931,'EXECUTED','8:7da7e954b405e74e5686454131f25f6e','addForeignKeyConstraint baseTableName=person_merge_log, constraintName=person_merge_log_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-879','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',932,'EXECUTED','8:cea7193597e608255e8b4d22ba7c8e8e','addForeignKeyConstraint baseTableName=person_merge_log, constraintName=person_merge_log_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-880','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',933,'EXECUTED','8:38c7fea990ed555a7924ac3176269f36','addForeignKeyConstraint baseTableName=person_merge_log, constraintName=person_merge_log_loser, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-881','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',934,'EXECUTED','8:3a6ec5b7592017c6eed57644ba25e332','addForeignKeyConstraint baseTableName=person_merge_log, constraintName=person_merge_log_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-882','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:05',935,'EXECUTED','8:705eb5a5b5aca27e84dfa405f77431ee','addForeignKeyConstraint baseTableName=person_merge_log, constraintName=person_merge_log_winner, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-883','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',936,'EXECUTED','8:bc7705fb1e89f96b987bd301022e3914','addForeignKeyConstraint baseTableName=obs, constraintName=person_obs, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-884','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',937,'EXECUTED','8:94ab85718e0cb640ea5758c113fdeced','addForeignKeyConstraint baseTableName=orders, constraintName=previous_order_id_order_id, referencedTableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-885','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',938,'EXECUTED','8:ba54d4d21cf2e60c43f3381ade3930cd','addForeignKeyConstraint baseTableName=obs, constraintName=previous_version, referencedTableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-886','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',939,'EXECUTED','8:864c68b497d770d4cfc3106f936987a6','addForeignKeyConstraint baseTableName=drug, constraintName=primary_drug_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-887','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',940,'EXECUTED','8:368d2c6558d4ca7fc3eb5955e1c313b2','addForeignKeyConstraint baseTableName=role_privilege, constraintName=privilege_definitions, referencedTableName=privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-888','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',941,'EXECUTED','8:bec8d14fbcd98750801f07d810eebfd5','addForeignKeyConstraint baseTableName=person_attribute_type, constraintName=privilege_which_can_edit, referencedTableName=privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-889','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',942,'EXECUTED','8:8f5da1efd8164891c226627748b5ae9c','addForeignKeyConstraint baseTableName=encounter_type, constraintName=privilege_which_can_edit_encounter_type, referencedTableName=privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-890','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',943,'EXECUTED','8:28e365841cb092772641e81f39e26c77','addForeignKeyConstraint baseTableName=encounter_type, constraintName=privilege_which_can_view_encounter_type, referencedTableName=privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-891','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',944,'EXECUTED','8:2888047b26688496274be6d6c7ccdbb0','addForeignKeyConstraint baseTableName=program_attribute_type, constraintName=program_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-892','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',945,'EXECUTED','8:b78050728415dfdc0642be01e09f28cd','addForeignKeyConstraint baseTableName=program_attribute_type, constraintName=program_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-893','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',946,'EXECUTED','8:0aaa34b309311e9006bc7b4f98b1893e','addForeignKeyConstraint baseTableName=program_attribute_type, constraintName=program_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-894','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',947,'EXECUTED','8:c757b39c031a0b4f7a1ef80801f234f6','addForeignKeyConstraint baseTableName=program, constraintName=program_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-895','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',948,'EXECUTED','8:85de92efbfe6c6ec80f41a48a0bfaed7','addForeignKeyConstraint baseTableName=program, constraintName=program_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-896','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',949,'EXECUTED','8:8b1f049920652db516dbca511076a9f4','addForeignKeyConstraint baseTableName=patient_program, constraintName=program_for_patient, referencedTableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-897','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',950,'EXECUTED','8:4740603fdc07139d43e29af6daf3cb10','addForeignKeyConstraint baseTableName=program_workflow, constraintName=program_for_workflow, referencedTableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-898','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',951,'EXECUTED','8:48891931556beb2fc6590306e055d8d8','addForeignKeyConstraint baseTableName=program, constraintName=program_outcomes_concept_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-899','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',952,'EXECUTED','8:873823d50904b587afe3aec452a52329','addForeignKeyConstraint baseTableName=concept_proposal, constraintName=proposal_obs_concept_id, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-900','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',953,'EXECUTED','8:30fd438046736007e73c9c833eeea60b','addForeignKeyConstraint baseTableName=concept_proposal, constraintName=proposal_obs_id, referencedTableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-901','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',954,'EXECUTED','8:3877465c4a9799b19b60fa3af3dfbc65','addForeignKeyConstraint baseTableName=provider_attribute, constraintName=provider_attribute_attribute_type_id_fk, referencedTableName=provider_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-902','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',955,'EXECUTED','8:03799fef9c0f5ae043962de95b0dc202','addForeignKeyConstraint baseTableName=provider_attribute, constraintName=provider_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-903','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',956,'EXECUTED','8:1c771ae73e6480bb861a645fe909a131','addForeignKeyConstraint baseTableName=provider_attribute, constraintName=provider_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-904','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',957,'EXECUTED','8:03c94a2f727c43f67d929a4214879a91','addForeignKeyConstraint baseTableName=provider_attribute, constraintName=provider_attribute_provider_fk, referencedTableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-905','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',958,'EXECUTED','8:d227c9a4d2485d4be77681aec1467350','addForeignKeyConstraint baseTableName=provider_attribute_type, constraintName=provider_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-906','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',959,'EXECUTED','8:f0cb284e7145d21ea42788e7ce776bf1','addForeignKeyConstraint baseTableName=provider_attribute_type, constraintName=provider_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-907','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',960,'EXECUTED','8:f1975c9f9588a0f1436bffa9df32b972','addForeignKeyConstraint baseTableName=provider_attribute_type, constraintName=provider_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-908','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',961,'EXECUTED','8:30f3c93397432465637261f6185a9b65','addForeignKeyConstraint baseTableName=provider_attribute, constraintName=provider_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-909','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',962,'EXECUTED','8:aee4d6d3b99903a1245a01bb28d72d9b','addForeignKeyConstraint baseTableName=provider, constraintName=provider_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-910','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',963,'EXECUTED','8:130425bf5258dfe55545e05d7df34595','addForeignKeyConstraint baseTableName=provider, constraintName=provider_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-911','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',964,'EXECUTED','8:e62bd1b601fe168319dbf0a34de6784e','addForeignKeyConstraint baseTableName=encounter_provider, constraintName=provider_id_fk, referencedTableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-912','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',965,'EXECUTED','8:e18b12fbe82ba34a22a38958bc5a351b','addForeignKeyConstraint baseTableName=provider, constraintName=provider_person_id_fk, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-913','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',966,'EXECUTED','8:0cd186d7421cc9da75a66a01d1a1e5ff','addForeignKeyConstraint baseTableName=provider, constraintName=provider_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-914','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',967,'EXECUTED','8:ef8fb54896552fc0f061ee4d1af8314a','addForeignKeyConstraint baseTableName=provider, constraintName=provider_role_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-915','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',968,'EXECUTED','8:61fb202a141d970ce143baa009dfecb7','addForeignKeyConstraint baseTableName=provider, constraintName=provider_speciality_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-916','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',969,'EXECUTED','8:ff8404b9b7aeb224be5b903cda3801ba','addForeignKeyConstraint baseTableName=relationship, constraintName=relation_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-917','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',970,'EXECUTED','8:b7535ef52fdec7216712b89bc8617624','addForeignKeyConstraint baseTableName=relationship, constraintName=relation_voider, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-918','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',971,'EXECUTED','8:b17a6b362473678c91ccaa4027b0209a','addForeignKeyConstraint baseTableName=relationship, constraintName=relationship_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-919','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',972,'EXECUTED','8:8f89a8d5c2a956aff04700656c9396e5','addForeignKeyConstraint baseTableName=relationship_type, constraintName=relationship_type_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-920','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',973,'EXECUTED','8:78087e61efe77335148fc92b95840fae','addForeignKeyConstraint baseTableName=relationship, constraintName=relationship_type_id, referencedTableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-921','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',974,'EXECUTED','8:ae0fa4466b4f779155f24ea6e9e825f3','addForeignKeyConstraint baseTableName=report_object, constraintName=report_object_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-922','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',975,'EXECUTED','8:b0297e8d8bc0a4adf11037fbc9af3431','addForeignKeyConstraint baseTableName=user_role, constraintName=role_definitions, referencedTableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-923','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',976,'EXECUTED','8:ed8f5077d539f345d0810adc39aab70b','addForeignKeyConstraint baseTableName=role_privilege, constraintName=role_privilege_to_role, referencedTableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-924','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',977,'EXECUTED','8:6f387fc05df49edb56c2164d482f47f9','addForeignKeyConstraint baseTableName=drug, constraintName=route_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-925','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',978,'EXECUTED','8:4652f64b910edf57a1380bbdf33205b9','addForeignKeyConstraint baseTableName=scheduler_task_config, constraintName=scheduler_changer, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-926','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',979,'EXECUTED','8:bb9e5e2975c8294c6be76b283668c275','addForeignKeyConstraint baseTableName=scheduler_task_config, constraintName=scheduler_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-927','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:06',980,'EXECUTED','8:ebf77f5dcca8d4f309b902359ee3cc63','addForeignKeyConstraint baseTableName=serialized_object, constraintName=serialized_object_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-928','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',981,'EXECUTED','8:c03789a6b3105e78e8b04beb23db60d4','addForeignKeyConstraint baseTableName=serialized_object, constraintName=serialized_object_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-929','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',982,'EXECUTED','8:ab33850c20390d9a94667bdefb2c84fc','addForeignKeyConstraint baseTableName=serialized_object, constraintName=serialized_object_retired_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-930','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',983,'EXECUTED','8:afc394554a225f746e7e180b5f8fed42','addForeignKeyConstraint baseTableName=program_workflow_state, constraintName=state_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-931','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',984,'EXECUTED','8:43d77120664e32253c6946c918fd24b8','addForeignKeyConstraint baseTableName=program_workflow_state, constraintName=state_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-932','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',985,'EXECUTED','8:ce3e47a7b841fc73bf96504bf7db8749','addForeignKeyConstraint baseTableName=program_workflow_state, constraintName=state_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-933','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',986,'EXECUTED','8:db6e8528879c9f79296e24af5ca075be','addForeignKeyConstraint baseTableName=patient_state, constraintName=state_for_patient, referencedTableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-934','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',987,'EXECUTED','8:20b95ac353b4e626dc6018013b5663d5','addForeignKeyConstraint baseTableName=scheduler_task_config_property, constraintName=task_config_for_property, referencedTableName=scheduler_task_config','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-935','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',988,'EXECUTED','8:7efca8cad4f197b4e9a393c0ccd6df10','addForeignKeyConstraint baseTableName=test_order, constraintName=test_order_frequency_fk, referencedTableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-936','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',989,'EXECUTED','8:d15937ce9e41099d41ef0fafed335109','addForeignKeyConstraint baseTableName=test_order, constraintName=test_order_order_id_fk, referencedTableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-937','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',990,'EXECUTED','8:96eb9f492204d23790dda339e5c9a7d7','addForeignKeyConstraint baseTableName=test_order, constraintName=test_order_specimen_source_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-938','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',991,'EXECUTED','8:e777121d9258621b5e3e221ada4b50ee','addForeignKeyConstraint baseTableName=order_type, constraintName=type_created_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-939','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',992,'EXECUTED','8:b6e18134ff3b5765f88fa3a6b9d2292c','addForeignKeyConstraint baseTableName=patient_identifier_type, constraintName=type_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-940','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',993,'EXECUTED','8:95bdd7fc9a1d53ee4e557767ad15a255','addForeignKeyConstraint baseTableName=field, constraintName=type_of_field, referencedTableName=field_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-941','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',994,'EXECUTED','8:106718fd2565c4e74c81f51608a81e2f','addForeignKeyConstraint baseTableName=orders, constraintName=type_of_order, referencedTableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-942','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',995,'EXECUTED','8:77ff24d236ce4e83e4b139ee0a2b7857','addForeignKeyConstraint baseTableName=users, constraintName=user_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-943','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',996,'EXECUTED','8:36496add7c8e546ef289e5dbcccc4f53','addForeignKeyConstraint baseTableName=user_property, constraintName=user_property_to_users, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-944','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',997,'EXECUTED','8:59966c2252770ba8f91be47557324da7','addForeignKeyConstraint baseTableName=user_role, constraintName=user_role_to_users, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-945','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',998,'EXECUTED','8:8602f3a50d36ad38b3923fbdf2e65f80','addForeignKeyConstraint baseTableName=patient_program, constraintName=user_who_changed, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-946','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',999,'EXECUTED','8:2411670a2f9854c2dc182090bafdb6e2','addForeignKeyConstraint baseTableName=notification_alert, constraintName=user_who_changed_alert, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-947','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1000,'EXECUTED','8:b7c5cf87111b45ecc5eb4957c895d551','addForeignKeyConstraint baseTableName=cohort, constraintName=user_who_changed_cohort, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-948','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1001,'EXECUTED','8:a38338459266c6758781e42e267282e9','addForeignKeyConstraint baseTableName=concept, constraintName=user_who_changed_concept, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-949','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1002,'EXECUTED','8:517ba79b2e58821cfbb633c7a432257f','addForeignKeyConstraint baseTableName=concept_description, constraintName=user_who_changed_description, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-950','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1003,'EXECUTED','8:1218284e419acdc1c2c9158fc505760f','addForeignKeyConstraint baseTableName=drug_reference_map, constraintName=user_who_changed_drug_reference_map, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-951','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1004,'EXECUTED','8:9b1303dc86a4937b111ff0924ca58816','addForeignKeyConstraint baseTableName=field, constraintName=user_who_changed_field, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-952','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1005,'EXECUTED','8:f101e27d192a0eb9dec0517ea8af6267','addForeignKeyConstraint baseTableName=note, constraintName=user_who_changed_note, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-953','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1006,'EXECUTED','8:7b004f293df92626e6d28c7e7f0f2b0d','addForeignKeyConstraint baseTableName=patient, constraintName=user_who_changed_pat, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-954','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1007,'EXECUTED','8:27366629e9d449b737e4a7e1caa52826','addForeignKeyConstraint baseTableName=person, constraintName=user_who_changed_person, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-955','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1008,'EXECUTED','8:51f5fa0d3c3f1178d78eca763ecbf657','addForeignKeyConstraint baseTableName=program, constraintName=user_who_changed_program, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-956','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1009,'EXECUTED','8:95bfdeb0859dc01f919e413e1c3e44de','addForeignKeyConstraint baseTableName=concept_proposal, constraintName=user_who_changed_proposal, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-957','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1010,'EXECUTED','8:7ce2c902ec94326aaee23059c4389aff','addForeignKeyConstraint baseTableName=report_object, constraintName=user_who_changed_report_object, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-958','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1011,'EXECUTED','8:687aab2046f9ab308a2e2798b82f136d','addForeignKeyConstraint baseTableName=users, constraintName=user_who_changed_user, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-959','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1012,'EXECUTED','8:f47fdd4ea85c023d3b66d0550667a7f9','addForeignKeyConstraint baseTableName=concept_set, constraintName=user_who_created, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-960','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1013,'EXECUTED','8:b8fcf2ac41670a9793e894f08b071d15','addForeignKeyConstraint baseTableName=concept_description, constraintName=user_who_created_description, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-961','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1014,'EXECUTED','8:8a7d6bbae90553077ddf45dcbe5a51e2','addForeignKeyConstraint baseTableName=field, constraintName=user_who_created_field, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-962','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1015,'EXECUTED','8:b009990ac5bd183936b4f522f0052c08','addForeignKeyConstraint baseTableName=field_answer, constraintName=user_who_created_field_answer, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-963','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1016,'EXECUTED','8:dcafbee6699d224b99e0938948016001','addForeignKeyConstraint baseTableName=field_type, constraintName=user_who_created_field_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-964','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1017,'EXECUTED','8:7088c7abae9366bcada546e8d38c2fc7','addForeignKeyConstraint baseTableName=form, constraintName=user_who_created_form, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-965','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1018,'EXECUTED','8:076b36f0dfe98f1aecf50fb60319b2b3','addForeignKeyConstraint baseTableName=form_field, constraintName=user_who_created_form_field, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-966','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1019,'EXECUTED','8:2ffd8fd5073b127999b7c77524e26568','addForeignKeyConstraint baseTableName=hl7_source, constraintName=user_who_created_hl7_source, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-967','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1020,'EXECUTED','8:e29ec5f0b4cd7b0d7b5e16b3561c9f86','addForeignKeyConstraint baseTableName=location, constraintName=user_who_created_location, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-968','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1021,'EXECUTED','8:8c201b88de87df5ac321b202cd46c87a','addForeignKeyConstraint baseTableName=concept_name, constraintName=user_who_created_name, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-969','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1022,'EXECUTED','8:f306d1317e3adcf71faf8a5253a2804a','addForeignKeyConstraint baseTableName=note, constraintName=user_who_created_note, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-970','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1023,'EXECUTED','8:36a4cb95348cdb42c1faa5fff6e0a2f0','addForeignKeyConstraint baseTableName=patient, constraintName=user_who_created_patient, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-971','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1024,'EXECUTED','8:3d51e7e09960c2f167d3ccfd325b684b','addForeignKeyConstraint baseTableName=person, constraintName=user_who_created_person, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-972','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1025,'EXECUTED','8:9889d4ba6279c6c46bf99fa4b44a497a','addForeignKeyConstraint baseTableName=concept_proposal, constraintName=user_who_created_proposal, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-973','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1026,'EXECUTED','8:631b9e2ca82364f3851f43e515d030c8','addForeignKeyConstraint baseTableName=relationship_type, constraintName=user_who_created_rel, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-974','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1027,'EXECUTED','8:99ba86b3b1ed8eca70b86a80c60e9ea2','addForeignKeyConstraint baseTableName=encounter_type, constraintName=user_who_created_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-975','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:07',1028,'EXECUTED','8:e263f26377e04c5eaf990141c659426f','addForeignKeyConstraint baseTableName=form, constraintName=user_who_last_changed_form, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-976','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1029,'EXECUTED','8:69baf6d56760f324322ea154cd7b6e9d','addForeignKeyConstraint baseTableName=form_field, constraintName=user_who_last_changed_form_field, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-977','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1030,'EXECUTED','8:33378ea2cc2d531fa6896c1f7ffe89ce','addForeignKeyConstraint baseTableName=person_name, constraintName=user_who_made_name, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-978','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1031,'EXECUTED','8:085f124f4e2470919cc13dc948677744','addForeignKeyConstraint baseTableName=concept, constraintName=user_who_retired_concept, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-979','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1032,'EXECUTED','8:7a76f208d58c03138ba1121debe02887','addForeignKeyConstraint baseTableName=concept_class, constraintName=user_who_retired_concept_class, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-980','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1033,'EXECUTED','8:5f94b99552467c24aee8f94e6647c083','addForeignKeyConstraint baseTableName=concept_datatype, constraintName=user_who_retired_concept_datatype, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-981','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1034,'EXECUTED','8:e071e0bbc8f0b2e7d9393743df7bb572','addForeignKeyConstraint baseTableName=concept_reference_source, constraintName=user_who_retired_concept_source, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-982','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1035,'EXECUTED','8:b6845ac52006a6db339f0bba8b284294','addForeignKeyConstraint baseTableName=drug_reference_map, constraintName=user_who_retired_drug_reference_map, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-983','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1036,'EXECUTED','8:357083bc1fba4415682131a64ebd9878','addForeignKeyConstraint baseTableName=encounter_type, constraintName=user_who_retired_encounter_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-984','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1037,'EXECUTED','8:280f08a02dde8204ebb872fd1af22cd9','addForeignKeyConstraint baseTableName=field, constraintName=user_who_retired_field, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-985','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1038,'EXECUTED','8:43d0cfcfb7702147a9022dd9e65e80cc','addForeignKeyConstraint baseTableName=form, constraintName=user_who_retired_form, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-986','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1039,'EXECUTED','8:1304d05f67d6ce15e45c7cbb1a6dc007','addForeignKeyConstraint baseTableName=location, constraintName=user_who_retired_location, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-987','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1040,'EXECUTED','8:c974d9f1a5794c6e465876321c62baa8','addForeignKeyConstraint baseTableName=order_type, constraintName=user_who_retired_order_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-988','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1041,'EXECUTED','8:21ced5e3d600731dc773eb3799397f26','addForeignKeyConstraint baseTableName=patient_identifier_type, constraintName=user_who_retired_patient_identifier_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-989','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1042,'EXECUTED','8:a16ec0f5694584f4d391beab8c1b024d','addForeignKeyConstraint baseTableName=person_attribute_type, constraintName=user_who_retired_person_attribute_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-990','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1043,'EXECUTED','8:24cf680ad246c318a513b8f933761418','addForeignKeyConstraint baseTableName=relationship_type, constraintName=user_who_retired_relationship_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-991','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1044,'EXECUTED','8:2e729344b24c164838b036860da3071d','addForeignKeyConstraint baseTableName=users, constraintName=user_who_retired_this_user, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-992','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1045,'EXECUTED','8:c3b47f085ee1c554733559c865c20b14','addForeignKeyConstraint baseTableName=cohort, constraintName=user_who_voided_cohort, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-993','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1046,'EXECUTED','8:53cd75514719543e6b43ff55de401797','addForeignKeyConstraint baseTableName=encounter, constraintName=user_who_voided_encounter, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-994','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1047,'EXECUTED','8:c57620a91d3f54cae590929f3dcad33e','addForeignKeyConstraint baseTableName=person_name, constraintName=user_who_voided_name, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-995','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1048,'EXECUTED','8:372a49dc1af12aebc3461dfd3849ffa1','addForeignKeyConstraint baseTableName=obs, constraintName=user_who_voided_obs, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-996','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1049,'EXECUTED','8:8a9f7042da846cc80057cc02efd2e510','addForeignKeyConstraint baseTableName=orders, constraintName=user_who_voided_order, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-997','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1050,'EXECUTED','8:e2a1ef8d5bc27124c509a3a1333fbd3d','addForeignKeyConstraint baseTableName=patient, constraintName=user_who_voided_patient, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-998','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1051,'EXECUTED','8:3ed24fd80fa06dac2c49d40903af9e97','addForeignKeyConstraint baseTableName=patient_program, constraintName=user_who_voided_patient_program, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-999','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1052,'EXECUTED','8:cf72bfec4f97d3d83163191c5db5e287','addForeignKeyConstraint baseTableName=person, constraintName=user_who_voided_person, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1000','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1053,'EXECUTED','8:d7105e8d63af1e2d234a22b3081e8ed3','addForeignKeyConstraint baseTableName=report_object, constraintName=user_who_voided_report_object, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1001','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1054,'EXECUTED','8:1bbb32c6cfc1794a5db7952ef5f0f1e9','addForeignKeyConstraint baseTableName=concept_name, constraintName=user_who_voided_this_name, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1002','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1055,'EXECUTED','8:9a3d956b0d13cf53387fb83083b64a14','addForeignKeyConstraint baseTableName=visit_attribute, constraintName=visit_attribute_attribute_type_id_fk, referencedTableName=visit_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1003','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1056,'EXECUTED','8:741755734c210fa644166e6ee82b1b01','addForeignKeyConstraint baseTableName=visit_attribute, constraintName=visit_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1004','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1057,'EXECUTED','8:a019be47cce0d75a13cd8d36c3644ab5','addForeignKeyConstraint baseTableName=visit_attribute, constraintName=visit_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1005','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1058,'EXECUTED','8:968b89848268b8f160b6f7ae0f121ef3','addForeignKeyConstraint baseTableName=visit_attribute_type, constraintName=visit_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1006','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1059,'EXECUTED','8:bef70d726c43ccae39ed7f4da3d879a1','addForeignKeyConstraint baseTableName=visit_attribute_type, constraintName=visit_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1007','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1060,'EXECUTED','8:4046bcdbac255468446a94061c1c6d05','addForeignKeyConstraint baseTableName=visit_attribute_type, constraintName=visit_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1008','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1061,'EXECUTED','8:86fe77ed791220c2e07e48d2194c1bcc','addForeignKeyConstraint baseTableName=visit_attribute, constraintName=visit_attribute_visit_fk, referencedTableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1009','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1062,'EXECUTED','8:1f20c1b226184660b36f681f46ae1dea','addForeignKeyConstraint baseTableName=visit_attribute, constraintName=visit_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1010','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1063,'EXECUTED','8:48ef11adc19c3774bf7c9ee3bc901dd0','addForeignKeyConstraint baseTableName=visit, constraintName=visit_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1011','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1064,'EXECUTED','8:70d01b1643c8c1ba0a8c99d2762bfec2','addForeignKeyConstraint baseTableName=visit, constraintName=visit_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1012','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1065,'EXECUTED','8:c363012eb8513835980abf5c690f32c9','addForeignKeyConstraint baseTableName=visit, constraintName=visit_indication_concept_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1013','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1066,'EXECUTED','8:8991b8fdf04c1910e9e864a7bdd1d620','addForeignKeyConstraint baseTableName=visit, constraintName=visit_location_fk, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1014','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1067,'EXECUTED','8:37d6df6fce98e57fbacfa56264e0bfc3','addForeignKeyConstraint baseTableName=visit, constraintName=visit_patient_fk, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1015','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1068,'EXECUTED','8:663a07b153a05ad3e4f32d6544c20e1e','addForeignKeyConstraint baseTableName=visit_type, constraintName=visit_type_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1016','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1069,'EXECUTED','8:9f8b48b4cc01c4ffb044e3b4ddf00341','addForeignKeyConstraint baseTableName=visit_type, constraintName=visit_type_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1017','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1070,'EXECUTED','8:417a8b642747bbff10ed4ecdf095da74','addForeignKeyConstraint baseTableName=visit, constraintName=visit_type_fk, referencedTableName=visit_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1018','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1071,'EXECUTED','8:53ac855d10a18def31c32ef717582e1a','addForeignKeyConstraint baseTableName=visit_type, constraintName=visit_type_retired_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1019','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1072,'EXECUTED','8:99eb27815589c6c9e80d8ac4bb8d6a6a','addForeignKeyConstraint baseTableName=visit, constraintName=visit_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1020','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1073,'EXECUTED','8:eec82397d9dfa7cf4d937440ae0f0405','addForeignKeyConstraint baseTableName=program_workflow, constraintName=workflow_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1021','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1074,'EXECUTED','8:56783b7bfc7556dcd750925352aca137','addForeignKeyConstraint baseTableName=program_workflow, constraintName=workflow_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1022','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:08',1075,'EXECUTED','8:f7f4682878845e23d811d83940fa0a2d','addForeignKeyConstraint baseTableName=program_workflow, constraintName=workflow_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003709353-1023','rasztabigab (generated)','liquibase-schema-only.xml','2022-11-15 11:49:09',1076,'EXECUTED','8:d977b0f4353d94104dce1117148d438b','addForeignKeyConstraint baseTableName=program_workflow_state, constraintName=workflow_for_state, referencedTableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-17','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1077,'EXECUTED','8:255e6601599e56ae696678ceaa497147','insert tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-27','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1078,'EXECUTED','8:6f812034bb92571156a30901b2de7686','insert tableName=users; insert tableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-1','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1079,'EXECUTED','8:15e1a2cab8858f62d69204ccd0b30c82','insert tableName=care_setting; insert tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-3','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1080,'EXECUTED','8:7f05eb80307fdfc709eb088650d15a6e','insert tableName=concept_class; insert tableName=concept_class; insert tableName=concept_class; insert tableName=concept_class; insert tableName=concept_class; insert tableName=concept_class; insert tableName=concept_class; insert tableName=concep...','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-4','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1081,'EXECUTED','8:50ec890abd419ac57991bbd28127253d','insert tableName=concept_datatype; insert tableName=concept_datatype; insert tableName=concept_datatype; insert tableName=concept_datatype; insert tableName=concept_datatype; insert tableName=concept_datatype; insert tableName=concept_datatype; in...','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-2','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1082,'EXECUTED','8:7cc25ed675cea3aab16933306a6f926c','insert tableName=concept; insert tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-5','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1083,'EXECUTED','8:5b490bb870c72fb23233b094c26046fe','insert tableName=concept_map_type; insert tableName=concept_map_type; insert tableName=concept_map_type; insert tableName=concept_map_type; insert tableName=concept_map_type; insert tableName=concept_map_type; insert tableName=concept_map_type; in...','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-6','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1084,'EXECUTED','8:02641afe8826c2086d092326d2760279','insert tableName=concept_name; insert tableName=concept_name; insert tableName=concept_name; insert tableName=concept_name; insert tableName=concept_name; insert tableName=concept_name; insert tableName=concept_name; insert tableName=concept_name;...','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-7','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1085,'EXECUTED','8:f20b5d7db739e72d0254bcc5badd9393','insert tableName=concept_stop_word; insert tableName=concept_stop_word; insert tableName=concept_stop_word; insert tableName=concept_stop_word; insert tableName=concept_stop_word; insert tableName=concept_stop_word; insert tableName=concept_stop_w...','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-8','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1086,'EXECUTED','8:6801580817ea05b1c8d3031de247112a','insert tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-9','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1087,'EXECUTED','8:80d9a081c517c5450b311b2076411160','insert tableName=field_type; insert tableName=field_type; insert tableName=field_type; insert tableName=field_type; insert tableName=field_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-10','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1088,'EXECUTED','8:f2f907bf119e3011ede64e5d6e63dfb0','insert tableName=global_property; insert tableName=global_property; insert tableName=global_property; insert tableName=global_property; insert tableName=global_property; insert tableName=global_property; insert tableName=global_property; insert ta...','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-11','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1089,'EXECUTED','8:98aae149885eadc3ec45db23026d263f','insert tableName=hl7_source','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-14','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1090,'EXECUTED','8:78aba44d5fd8096db49515cb30cec7b9','insert tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-15','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1091,'EXECUTED','8:01249863645b038d25a5e3865b630bee','insert tableName=order_type; insert tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-16','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1092,'EXECUTED','8:8d099363142933cea82f5ddfddcb28af','insert tableName=patient_identifier_type; insert tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-18','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1093,'EXECUTED','8:1690ac38e5c1c4dcea2d7dced569f187','insert tableName=person_attribute_type; insert tableName=person_attribute_type; insert tableName=person_attribute_type; insert tableName=person_attribute_type; insert tableName=person_attribute_type; insert tableName=person_attribute_type; insert ...','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-19','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1094,'EXECUTED','8:1458c1fc57bc69a529569da02ca87f72','insert tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-20','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1095,'EXECUTED','8:38bf298addef7942e26b817441655f1c','insert tableName=privilege; insert tableName=privilege; insert tableName=privilege; insert tableName=privilege; insert tableName=privilege; insert tableName=privilege; insert tableName=privilege; insert tableName=privilege; insert tableName=privil...','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-21','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1096,'EXECUTED','8:28a763094594b829bc80f3d6a894d450','insert tableName=relationship_type; insert tableName=relationship_type; insert tableName=relationship_type; insert tableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-22','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1097,'EXECUTED','8:3e059f57b088f635d2c19d2e3cbb488e','insert tableName=role; insert tableName=role; insert tableName=role; insert tableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-23','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1098,'EXECUTED','8:21180f55b63fb7d74dc53d35b827746b','insert tableName=role_privilege; insert tableName=role_privilege; insert tableName=role_privilege; insert tableName=role_privilege; insert tableName=role_privilege; insert tableName=role_privilege; insert tableName=role_privilege; insert tableName...','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-24','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1099,'EXECUTED','8:0cb4daf54d5d34edb866c37179b22284','insert tableName=scheduler_task_config','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-25','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1100,'EXECUTED','8:78b09b530a915c9ed205b1950a622f66','insert tableName=user_property; insert tableName=user_property; insert tableName=user_property','',NULL,'4.4.3',NULL,NULL,NULL),('1616003711789-26','rasztabigab (generated)','liquibase-core-data.xml','2022-11-15 11:49:09',1101,'EXECUTED','8:c2366d8db8ef9d8a623360430d7a1115','insert tableName=user_role; insert tableName=user_role','',NULL,'4.4.3',NULL,NULL,NULL),('TRUNK-6001','rasztabigab','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:09',1102,'EXECUTED','8:00bd333022b8d08c676749c2e68f3190','dropNotNullConstraint columnName=start_date, tableName=cohort_member','Delete non-null constraint from column cohort_member.start_date',NULL,'4.4.3',NULL,NULL,NULL),('10000000-TRUNK-6015','dkayiwa','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:09',1103,'EXECUTED','8:10276a470199bd3ce0addf3f49f68f17','addColumn tableName=encounter_diagnosis','Adding \"form_namespace_and_path\" column to encounter_diagnosis table',NULL,'4.4.3',NULL,NULL,NULL),('10000000-TRUNK-6016','dkayiwa','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:09',1104,'EXECUTED','8:f0ce0a60bdab2256858d966eabb0bc5e','addColumn tableName=allergy','Adding \"form_namespace_and_path\" column to the allergy table',NULL,'4.4.3',NULL,NULL,NULL),('10000001-TRUNK-6016','dkayiwa','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:09',1105,'EXECUTED','8:76d5f880ebc99cb1739172b4a52d013f','addColumn tableName=allergy; addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_encounter_id_fk, referencedTableName=encounter','Adding \'encounter_id\' column to the allergy table',NULL,'4.4.3',NULL,NULL,NULL),('10000000-TRUNK-6017','dkayiwa','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:09',1106,'EXECUTED','8:ec16bf7b6f70059ad300110b68192dc0','addColumn tableName=orders','Adding \"form_namespace_and_path\" column to the orders table',NULL,'4.4.3',NULL,NULL,NULL),('10000000-TRUNK-6018','aojwang','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:09',1107,'EXECUTED','8:ebf094385dad9b9cf36fbe62aa22b4b7','addColumn tableName=patient_state','Adding \"form_namespace_and_path\" column to the patient_state table',NULL,'4.4.3',NULL,NULL,NULL),('10000001-TRUNK-6018','aojwang','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:09',1108,'EXECUTED','8:b4a0ccd8c23e64e4f3fd702fe943456e','addColumn tableName=patient_state; addForeignKeyConstraint baseTableName=patient_state, constraintName=patient_state_encounter_id_fk, referencedTableName=encounter','Adding \'encounter_id\' column to the patient_state table',NULL,'4.4.3',NULL,NULL,NULL),('2021-09-02-TRUNK-6020-a','tendomart','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:09',1109,'EXECUTED','8:aadbb027ec7a7517476c91d72adfeb81','modifyDataType columnName=property, tableName=user_property','Increasing user_property.property from VARCHAR(100) to VARCHAR(255)',NULL,'4.4.3',NULL,NULL,NULL),('2021-09-02-TRUNK-6020-b','tendomart','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:09',1110,'EXECUTED','8:85b6045f5edd6bb0e8196d34ce45af93','modifyDataType columnName=property_value, tableName=user_property','Changing user_property.property_value from VARCHAR(255) to LONGTEXT',NULL,'4.4.3',NULL,NULL,NULL),('2021-27-09-0200-TRUNK-6027','miirochristopher','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:09',1111,'EXECUTED','8:06f763bdcac48ebd7f3a3c27b1659968','createTable tableName=order_attribute_type; addForeignKeyConstraint baseTableName=order_attribute_type, constraintName=order_attribute_type_creator_fk, referencedTableName=users; addForeignKeyConstraint baseTableName=order_attribute_type, constrai...','Creating order_attribute_type table',NULL,'4.4.3',NULL,NULL,NULL),('2021-27-09-0300-TRUNK-6027','miirochristopher','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:10',1112,'EXECUTED','8:61f02d74b284d8b7ce979fa00a81a4a7','createTable tableName=order_attribute; addForeignKeyConstraint baseTableName=order_attribute, constraintName=order_attribute_order_fk, referencedTableName=orders; addForeignKeyConstraint baseTableName=order_attribute, constraintName=order_attribut...','Creating order_attribute table',NULL,'4.4.3',NULL,NULL,NULL),('TRUNK-6036','dkayiwa','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:10',1113,'EXECUTED','8:a03e4dfd1b62637612a7de0eb6b8f16c','createTable tableName=referral_order; addForeignKeyConstraint baseTableName=referral_order, constraintName=referral_order_order_id_fk, referencedTableName=orders; addForeignKeyConstraint baseTableName=referral_order, constraintName=referral_order_...','Create referral_order table',NULL,'4.4.3',NULL,NULL,NULL),('TRUNK-6035','dkayiwa','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:10',1114,'EXECUTED','8:a83b3c51647a7b4cd149c317b0305a1c','addColumn tableName=test_order; addForeignKeyConstraint baseTableName=test_order, constraintName=test_order_location_fk, referencedTableName=concept','Adding location column to the test_order table',NULL,'4.4.3',NULL,NULL,NULL),('TRUNK-6045','pmanko','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:10',1115,'EXECUTED','8:8d3cf8206c717b64458982d991f40555','addColumn tableName=location; addForeignKeyConstraint baseTableName=location, constraintName=location_type_fk, referencedTableName=concept','Adding type field to the Location table',NULL,'4.4.3',NULL,NULL,NULL),('2021-24-10-1000-TRUNK-6038','miirochristopher','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:10',1116,'EXECUTED','8:64667c33ce83ef617adcec76bb0d8d07','createTable tableName=diagnosis_attribute_type; addForeignKeyConstraint baseTableName=diagnosis_attribute_type, constraintName=diagnosis_attribute_type_creator_fk, referencedTableName=users; addForeignKeyConstraint baseTableName=diagnosis_attribut...','Creating diagnosis_attribute_type table',NULL,'4.4.3',NULL,NULL,NULL),('2021-24-10-1145-TRUNK-6038','miirochristopher','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:10',1117,'EXECUTED','8:f8ab3e462613d00033a3c048dd1dfd20','createTable tableName=diagnosis_attribute; addForeignKeyConstraint baseTableName=diagnosis_attribute, constraintName=diagnosis_attribute_diagnosis_fk, referencedTableName=encounter_diagnosis; addForeignKeyConstraint baseTableName=diagnosis_attribu...','Creating diagnosis_attribute table',NULL,'4.4.3',NULL,NULL,NULL),('2020-09-16-1700-TRUNK-5736','miirochristopher','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:10',1118,'EXECUTED','8:40649e743e95f164311fbe7a4d9db53c','dropForeignKeyConstraint baseTableName=users, constraintName=user_who_changed_user; addForeignKeyConstraint baseTableName=users, constraintName=user_who_changed_user, referencedTableName=users','Updating foreign key user_who_changed_user to add delete CASCADE',NULL,'4.4.3',NULL,NULL,NULL),('2021-17-11-0222-TRUNK-6044','miirochristopher','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.5.x.xml','2022-11-15 11:49:10',1119,'EXECUTED','8:891345f735354d676708be6a6901e6d6','renameColumn newColumnName=dx_rank, oldColumnName=rank, tableName=encounter_diagnosis','Renaming column rank to dx_rank because rank is a reserved word in MySQL 8.0.2 and later',NULL,'4.4.3',NULL,NULL,NULL);
/*!40000 ALTER TABLE `liquibasechangelog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `liquibasechangeloglock`
--

DROP TABLE IF EXISTS `liquibasechangeloglock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `liquibasechangeloglock` (
										  `ID` int(11) NOT NULL,
										  `LOCKED` tinyint(1) NOT NULL,
										  `LOCKGRANTED` datetime DEFAULT NULL,
										  `LOCKEDBY` varchar(255) DEFAULT NULL,
										  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `liquibasechangeloglock`
--

LOCK TABLES `liquibasechangeloglock` WRITE;
/*!40000 ALTER TABLE `liquibasechangeloglock` DISABLE KEYS */;
INSERT INTO `liquibasechangeloglock` VALUES (1,0,NULL,NULL);
/*!40000 ALTER TABLE `liquibasechangeloglock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location` (
							`location_id` int(11) NOT NULL AUTO_INCREMENT,
							`name` varchar(255) NOT NULL DEFAULT '',
							`description` varchar(255) DEFAULT NULL,
							`address1` varchar(255) DEFAULT NULL,
							`address2` varchar(255) DEFAULT NULL,
							`city_village` varchar(255) DEFAULT NULL,
							`state_province` varchar(255) DEFAULT NULL,
							`postal_code` varchar(50) DEFAULT NULL,
							`country` varchar(50) DEFAULT NULL,
							`latitude` varchar(50) DEFAULT NULL,
							`longitude` varchar(50) DEFAULT NULL,
							`creator` int(11) NOT NULL DEFAULT 0,
							`date_created` datetime NOT NULL,
							`county_district` varchar(255) DEFAULT NULL,
							`address3` varchar(255) DEFAULT NULL,
							`address4` varchar(255) DEFAULT NULL,
							`address5` varchar(255) DEFAULT NULL,
							`address6` varchar(255) DEFAULT NULL,
							`retired` tinyint(1) NOT NULL DEFAULT 0,
							`retired_by` int(11) DEFAULT NULL,
							`date_retired` datetime DEFAULT NULL,
							`retire_reason` varchar(255) DEFAULT NULL,
							`parent_location` int(11) DEFAULT NULL,
							`uuid` char(38) NOT NULL,
							`changed_by` int(11) DEFAULT NULL,
							`date_changed` datetime DEFAULT NULL,
							`address7` varchar(255) DEFAULT NULL,
							`address8` varchar(255) DEFAULT NULL,
							`address9` varchar(255) DEFAULT NULL,
							`address10` varchar(255) DEFAULT NULL,
							`address11` varchar(255) DEFAULT NULL,
							`address12` varchar(255) DEFAULT NULL,
							`address13` varchar(255) DEFAULT NULL,
							`address14` varchar(255) DEFAULT NULL,
							`address15` varchar(255) DEFAULT NULL,
							`location_type_concept_id` int(11) DEFAULT NULL,
							PRIMARY KEY (`location_id`),
							UNIQUE KEY `uuid_location` (`uuid`),
							KEY `location_changed_by` (`changed_by`),
							KEY `location_retired_status` (`retired`),
							KEY `name_of_location` (`name`),
							KEY `parent_location` (`parent_location`),
							KEY `user_who_created_location` (`creator`),
							KEY `user_who_retired_location` (`retired_by`),
							KEY `location_type_fk` (`location_type_concept_id`),
							CONSTRAINT `location_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
							CONSTRAINT `location_type_fk` FOREIGN KEY (`location_type_concept_id`) REFERENCES `concept` (`concept_id`),
							CONSTRAINT `parent_location` FOREIGN KEY (`parent_location`) REFERENCES `location` (`location_id`),
							CONSTRAINT `user_who_created_location` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
							CONSTRAINT `user_who_retired_location` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location`
--

LOCK TABLES `location` WRITE;
/*!40000 ALTER TABLE `location` DISABLE KEYS */;
INSERT INTO `location` VALUES (1,'Unknown Location',NULL,'','','','','','',NULL,NULL,1,'2005-09-22 00:00:00',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'8d6c993e-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location_attribute`
--

DROP TABLE IF EXISTS `location_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location_attribute` (
									  `location_attribute_id` int(11) NOT NULL AUTO_INCREMENT,
									  `location_id` int(11) NOT NULL,
									  `attribute_type_id` int(11) NOT NULL,
									  `value_reference` text NOT NULL,
									  `uuid` char(38) NOT NULL,
									  `creator` int(11) NOT NULL,
									  `date_created` datetime NOT NULL,
									  `changed_by` int(11) DEFAULT NULL,
									  `date_changed` datetime DEFAULT NULL,
									  `voided` tinyint(1) NOT NULL DEFAULT 0,
									  `voided_by` int(11) DEFAULT NULL,
									  `date_voided` datetime DEFAULT NULL,
									  `void_reason` varchar(255) DEFAULT NULL,
									  PRIMARY KEY (`location_attribute_id`),
									  UNIQUE KEY `uuid_location_attribute` (`uuid`),
									  KEY `location_attribute_attribute_type_id_fk` (`attribute_type_id`),
									  KEY `location_attribute_changed_by_fk` (`changed_by`),
									  KEY `location_attribute_creator_fk` (`creator`),
									  KEY `location_attribute_location_fk` (`location_id`),
									  KEY `location_attribute_voided_by_fk` (`voided_by`),
									  CONSTRAINT `location_attribute_attribute_type_id_fk` FOREIGN KEY (`attribute_type_id`) REFERENCES `location_attribute_type` (`location_attribute_type_id`),
									  CONSTRAINT `location_attribute_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									  CONSTRAINT `location_attribute_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									  CONSTRAINT `location_attribute_location_fk` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`),
									  CONSTRAINT `location_attribute_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location_attribute`
--

LOCK TABLES `location_attribute` WRITE;
/*!40000 ALTER TABLE `location_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `location_attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location_attribute_type`
--

DROP TABLE IF EXISTS `location_attribute_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location_attribute_type` (
										   `location_attribute_type_id` int(11) NOT NULL AUTO_INCREMENT,
										   `name` varchar(255) NOT NULL,
										   `description` varchar(1024) DEFAULT NULL,
										   `datatype` varchar(255) DEFAULT NULL,
										   `datatype_config` text DEFAULT NULL,
										   `preferred_handler` varchar(255) DEFAULT NULL,
										   `handler_config` text DEFAULT NULL,
										   `min_occurs` int(11) NOT NULL,
										   `max_occurs` int(11) DEFAULT NULL,
										   `creator` int(11) NOT NULL,
										   `date_created` datetime NOT NULL,
										   `changed_by` int(11) DEFAULT NULL,
										   `date_changed` datetime DEFAULT NULL,
										   `retired` tinyint(1) NOT NULL DEFAULT 0,
										   `retired_by` int(11) DEFAULT NULL,
										   `date_retired` datetime DEFAULT NULL,
										   `retire_reason` varchar(255) DEFAULT NULL,
										   `uuid` char(38) NOT NULL,
										   PRIMARY KEY (`location_attribute_type_id`),
										   UNIQUE KEY `location_attribute_type_name` (`name`),
										   UNIQUE KEY `uuid_location_attribute_type` (`uuid`),
										   KEY `location_attribute_type_changed_by_fk` (`changed_by`),
										   KEY `location_attribute_type_creator_fk` (`creator`),
										   KEY `location_attribute_type_retired_by_fk` (`retired_by`),
										   CONSTRAINT `location_attribute_type_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
										   CONSTRAINT `location_attribute_type_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
										   CONSTRAINT `location_attribute_type_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location_attribute_type`
--

LOCK TABLES `location_attribute_type` WRITE;
/*!40000 ALTER TABLE `location_attribute_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `location_attribute_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location_tag`
--

DROP TABLE IF EXISTS `location_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location_tag` (
								`location_tag_id` int(11) NOT NULL AUTO_INCREMENT,
								`name` varchar(50) NOT NULL,
								`description` varchar(255) DEFAULT NULL,
								`creator` int(11) NOT NULL,
								`date_created` datetime NOT NULL,
								`retired` tinyint(1) NOT NULL DEFAULT 0,
								`retired_by` int(11) DEFAULT NULL,
								`date_retired` datetime DEFAULT NULL,
								`retire_reason` varchar(255) DEFAULT NULL,
								`uuid` char(38) NOT NULL,
								`changed_by` int(11) DEFAULT NULL,
								`date_changed` datetime DEFAULT NULL,
								PRIMARY KEY (`location_tag_id`),
								UNIQUE KEY `uuid_location_tag` (`uuid`),
								KEY `location_tag_changed_by` (`changed_by`),
								KEY `location_tag_creator` (`creator`),
								KEY `location_tag_retired_by` (`retired_by`),
								CONSTRAINT `location_tag_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
								CONSTRAINT `location_tag_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								CONSTRAINT `location_tag_retired_by` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location_tag`
--

LOCK TABLES `location_tag` WRITE;
/*!40000 ALTER TABLE `location_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `location_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location_tag_map`
--

DROP TABLE IF EXISTS `location_tag_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location_tag_map` (
									`location_id` int(11) NOT NULL,
									`location_tag_id` int(11) NOT NULL,
									PRIMARY KEY (`location_id`,`location_tag_id`),
									KEY `location_tag_map_tag` (`location_tag_id`),
									CONSTRAINT `location_tag_map_location` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`),
									CONSTRAINT `location_tag_map_tag` FOREIGN KEY (`location_tag_id`) REFERENCES `location_tag` (`location_tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location_tag_map`
--

LOCK TABLES `location_tag_map` WRITE;
/*!40000 ALTER TABLE `location_tag_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `location_tag_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `note`
--

DROP TABLE IF EXISTS `note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `note` (
						`note_id` int(11) NOT NULL DEFAULT 0,
						`note_type` varchar(50) DEFAULT NULL,
						`patient_id` int(11) DEFAULT NULL,
						`obs_id` int(11) DEFAULT NULL,
						`encounter_id` int(11) DEFAULT NULL,
						`text` text NOT NULL,
						`priority` int(11) DEFAULT NULL,
						`parent` int(11) DEFAULT NULL,
						`creator` int(11) NOT NULL DEFAULT 0,
						`date_created` datetime NOT NULL,
						`changed_by` int(11) DEFAULT NULL,
						`date_changed` datetime DEFAULT NULL,
						`uuid` char(38) NOT NULL,
						PRIMARY KEY (`note_id`),
						UNIQUE KEY `uuid_note` (`uuid`),
						KEY `encounter_note` (`encounter_id`),
						KEY `note_hierarchy` (`parent`),
						KEY `obs_note` (`obs_id`),
						KEY `patient_note` (`patient_id`),
						KEY `user_who_changed_note` (`changed_by`),
						KEY `user_who_created_note` (`creator`),
						CONSTRAINT `encounter_note` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
						CONSTRAINT `note_hierarchy` FOREIGN KEY (`parent`) REFERENCES `note` (`note_id`),
						CONSTRAINT `obs_note` FOREIGN KEY (`obs_id`) REFERENCES `obs` (`obs_id`),
						CONSTRAINT `patient_note` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE,
						CONSTRAINT `user_who_changed_note` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
						CONSTRAINT `user_who_created_note` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `note`
--

LOCK TABLES `note` WRITE;
/*!40000 ALTER TABLE `note` DISABLE KEYS */;
/*!40000 ALTER TABLE `note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_alert`
--

DROP TABLE IF EXISTS `notification_alert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notification_alert` (
									  `alert_id` int(11) NOT NULL AUTO_INCREMENT,
									  `text` varchar(512) NOT NULL,
									  `satisfied_by_any` tinyint(1) NOT NULL DEFAULT 0,
									  `alert_read` tinyint(1) NOT NULL DEFAULT 0,
									  `date_to_expire` datetime DEFAULT NULL,
									  `creator` int(11) NOT NULL,
									  `date_created` datetime NOT NULL,
									  `changed_by` int(11) DEFAULT NULL,
									  `date_changed` datetime DEFAULT NULL,
									  `uuid` char(38) NOT NULL,
									  PRIMARY KEY (`alert_id`),
									  UNIQUE KEY `uuid_notification_alert` (`uuid`),
									  KEY `alert_creator` (`creator`),
									  KEY `alert_date_to_expire_idx` (`date_to_expire`),
									  KEY `user_who_changed_alert` (`changed_by`),
									  CONSTRAINT `alert_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									  CONSTRAINT `user_who_changed_alert` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_alert`
--

LOCK TABLES `notification_alert` WRITE;
/*!40000 ALTER TABLE `notification_alert` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification_alert` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_alert_recipient`
--

DROP TABLE IF EXISTS `notification_alert_recipient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notification_alert_recipient` (
												`alert_id` int(11) NOT NULL,
												`user_id` int(11) NOT NULL,
												`alert_read` tinyint(1) NOT NULL DEFAULT 0,
												`date_changed` timestamp NOT NULL DEFAULT current_timestamp(),
												`uuid` char(38) NOT NULL,
												PRIMARY KEY (`alert_id`,`user_id`),
												KEY `alert_read_by_user` (`user_id`),
												CONSTRAINT `alert_read_by_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
												CONSTRAINT `id_of_alert` FOREIGN KEY (`alert_id`) REFERENCES `notification_alert` (`alert_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_alert_recipient`
--

LOCK TABLES `notification_alert_recipient` WRITE;
/*!40000 ALTER TABLE `notification_alert_recipient` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification_alert_recipient` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_template`
--

DROP TABLE IF EXISTS `notification_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notification_template` (
										 `template_id` int(11) NOT NULL AUTO_INCREMENT,
										 `name` varchar(50) DEFAULT NULL,
										 `template` text DEFAULT NULL,
										 `subject` varchar(100) DEFAULT NULL,
										 `sender` varchar(255) DEFAULT NULL,
										 `recipients` varchar(512) DEFAULT NULL,
										 `ordinal` int(11) DEFAULT 0,
										 `uuid` char(38) NOT NULL,
										 PRIMARY KEY (`template_id`),
										 UNIQUE KEY `uuid_notification_template` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_template`
--

LOCK TABLES `notification_template` WRITE;
/*!40000 ALTER TABLE `notification_template` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `obs`
--

DROP TABLE IF EXISTS `obs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `obs` (
					   `obs_id` int(11) NOT NULL AUTO_INCREMENT,
					   `person_id` int(11) NOT NULL,
					   `concept_id` int(11) NOT NULL DEFAULT 0,
					   `encounter_id` int(11) DEFAULT NULL,
					   `order_id` int(11) DEFAULT NULL,
					   `obs_datetime` datetime NOT NULL,
					   `location_id` int(11) DEFAULT NULL,
					   `obs_group_id` int(11) DEFAULT NULL,
					   `accession_number` varchar(255) DEFAULT NULL,
					   `value_group_id` int(11) DEFAULT NULL,
					   `value_coded` int(11) DEFAULT NULL,
					   `value_coded_name_id` int(11) DEFAULT NULL,
					   `value_drug` int(11) DEFAULT NULL,
					   `value_datetime` datetime DEFAULT NULL,
					   `value_numeric` double DEFAULT NULL,
					   `value_modifier` varchar(2) DEFAULT NULL,
					   `value_text` text DEFAULT NULL,
					   `value_complex` varchar(1000) DEFAULT NULL,
					   `comments` varchar(255) DEFAULT NULL,
					   `creator` int(11) NOT NULL DEFAULT 0,
					   `date_created` datetime NOT NULL,
					   `voided` tinyint(1) NOT NULL DEFAULT 0,
					   `voided_by` int(11) DEFAULT NULL,
					   `date_voided` datetime DEFAULT NULL,
					   `void_reason` varchar(255) DEFAULT NULL,
					   `uuid` char(38) NOT NULL,
					   `previous_version` int(11) DEFAULT NULL,
					   `form_namespace_and_path` varchar(255) DEFAULT NULL,
					   `status` varchar(16) NOT NULL DEFAULT 'FINAL',
					   `interpretation` varchar(32) DEFAULT NULL,
					   PRIMARY KEY (`obs_id`),
					   UNIQUE KEY `uuid_obs` (`uuid`),
					   KEY `answer_concept` (`value_coded`),
					   KEY `answer_concept_drug` (`value_drug`),
					   KEY `encounter_observations` (`encounter_id`),
					   KEY `obs_concept` (`concept_id`),
					   KEY `obs_datetime_idx` (`obs_datetime`),
					   KEY `obs_enterer` (`creator`),
					   KEY `obs_grouping_id` (`obs_group_id`),
					   KEY `obs_location` (`location_id`),
					   KEY `obs_name_of_coded_value` (`value_coded_name_id`),
					   KEY `obs_order` (`order_id`),
					   KEY `person_obs` (`person_id`),
					   KEY `previous_version` (`previous_version`),
					   KEY `user_who_voided_obs` (`voided_by`),
					   CONSTRAINT `answer_concept` FOREIGN KEY (`value_coded`) REFERENCES `concept` (`concept_id`),
					   CONSTRAINT `answer_concept_drug` FOREIGN KEY (`value_drug`) REFERENCES `drug` (`drug_id`),
					   CONSTRAINT `encounter_observations` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
					   CONSTRAINT `obs_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
					   CONSTRAINT `obs_enterer` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
					   CONSTRAINT `obs_grouping_id` FOREIGN KEY (`obs_group_id`) REFERENCES `obs` (`obs_id`),
					   CONSTRAINT `obs_location` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`),
					   CONSTRAINT `obs_name_of_coded_value` FOREIGN KEY (`value_coded_name_id`) REFERENCES `concept_name` (`concept_name_id`),
					   CONSTRAINT `obs_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
					   CONSTRAINT `person_obs` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
					   CONSTRAINT `previous_version` FOREIGN KEY (`previous_version`) REFERENCES `obs` (`obs_id`),
					   CONSTRAINT `user_who_voided_obs` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `obs`
--

LOCK TABLES `obs` WRITE;
/*!40000 ALTER TABLE `obs` DISABLE KEYS */;
/*!40000 ALTER TABLE `obs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_attribute`
--

DROP TABLE IF EXISTS `order_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_attribute` (
								   `order_attribute_id` int(11) NOT NULL AUTO_INCREMENT,
								   `order_id` int(11) NOT NULL,
								   `attribute_type_id` int(11) NOT NULL,
								   `value_reference` text NOT NULL,
								   `uuid` char(38) NOT NULL,
								   `creator` int(11) NOT NULL,
								   `date_created` datetime NOT NULL,
								   `changed_by` int(11) DEFAULT NULL,
								   `date_changed` datetime DEFAULT NULL,
								   `voided` tinyint(1) NOT NULL DEFAULT 0,
								   `voided_by` int(11) DEFAULT NULL,
								   `date_voided` datetime DEFAULT NULL,
								   `void_reason` varchar(255) DEFAULT NULL,
								   PRIMARY KEY (`order_attribute_id`),
								   UNIQUE KEY `uuid` (`uuid`),
								   KEY `order_attribute_order_fk` (`order_id`),
								   KEY `order_attribute_attribute_type_id_fk` (`attribute_type_id`),
								   KEY `order_attribute_creator_fk` (`creator`),
								   KEY `order_attribute_changed_by_fk` (`changed_by`),
								   KEY `order_attribute_voided_by_fk` (`voided_by`),
								   CONSTRAINT `order_attribute_attribute_type_id_fk` FOREIGN KEY (`attribute_type_id`) REFERENCES `order_attribute_type` (`order_attribute_type_id`),
								   CONSTRAINT `order_attribute_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
								   CONSTRAINT `order_attribute_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								   CONSTRAINT `order_attribute_order_fk` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
								   CONSTRAINT `order_attribute_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_attribute`
--

LOCK TABLES `order_attribute` WRITE;
/*!40000 ALTER TABLE `order_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_attribute_type`
--

DROP TABLE IF EXISTS `order_attribute_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_attribute_type` (
										`order_attribute_type_id` int(11) NOT NULL AUTO_INCREMENT,
										`name` varchar(255) NOT NULL,
										`description` varchar(1024) DEFAULT NULL,
										`datatype` varchar(255) DEFAULT NULL,
										`datatype_config` text DEFAULT NULL,
										`preferred_handler` varchar(255) DEFAULT NULL,
										`handler_config` text DEFAULT NULL,
										`min_occurs` int(11) NOT NULL,
										`max_occurs` int(11) DEFAULT NULL,
										`creator` int(11) NOT NULL,
										`date_created` datetime NOT NULL,
										`changed_by` int(11) DEFAULT NULL,
										`date_changed` datetime DEFAULT NULL,
										`retired` tinyint(1) NOT NULL DEFAULT 0,
										`retired_by` int(11) DEFAULT NULL,
										`date_retired` datetime DEFAULT NULL,
										`retire_reason` varchar(255) DEFAULT NULL,
										`uuid` char(38) NOT NULL,
										PRIMARY KEY (`order_attribute_type_id`),
										UNIQUE KEY `name` (`name`),
										UNIQUE KEY `uuid` (`uuid`),
										KEY `order_attribute_type_creator_fk` (`creator`),
										KEY `order_attribute_type_changed_by_fk` (`changed_by`),
										KEY `order_attribute_type_retired_by_fk` (`retired_by`),
										CONSTRAINT `order_attribute_type_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
										CONSTRAINT `order_attribute_type_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
										CONSTRAINT `order_attribute_type_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_attribute_type`
--

LOCK TABLES `order_attribute_type` WRITE;
/*!40000 ALTER TABLE `order_attribute_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_attribute_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_frequency`
--

DROP TABLE IF EXISTS `order_frequency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_frequency` (
								   `order_frequency_id` int(11) NOT NULL AUTO_INCREMENT,
								   `concept_id` int(11) NOT NULL,
								   `frequency_per_day` double DEFAULT NULL,
								   `creator` int(11) NOT NULL,
								   `date_created` datetime NOT NULL,
								   `retired` tinyint(1) NOT NULL DEFAULT 0,
								   `retired_by` int(11) DEFAULT NULL,
								   `date_retired` datetime DEFAULT NULL,
								   `retire_reason` varchar(255) DEFAULT NULL,
								   `changed_by` int(11) DEFAULT NULL,
								   `date_changed` datetime DEFAULT NULL,
								   `uuid` char(38) NOT NULL,
								   PRIMARY KEY (`order_frequency_id`),
								   UNIQUE KEY `concept_id` (`concept_id`),
								   UNIQUE KEY `uuid_order_frequency` (`uuid`),
								   KEY `order_frequency_changed_by_fk` (`changed_by`),
								   KEY `order_frequency_creator_fk` (`creator`),
								   KEY `order_frequency_retired_by_fk` (`retired_by`),
								   CONSTRAINT `order_frequency_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
								   CONSTRAINT `order_frequency_concept_id_fk` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
								   CONSTRAINT `order_frequency_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								   CONSTRAINT `order_frequency_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_frequency`
--

LOCK TABLES `order_frequency` WRITE;
/*!40000 ALTER TABLE `order_frequency` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_frequency` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_group`
--

DROP TABLE IF EXISTS `order_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_group` (
							   `order_group_id` int(11) NOT NULL AUTO_INCREMENT,
							   `order_set_id` int(11) DEFAULT NULL,
							   `patient_id` int(11) NOT NULL,
							   `encounter_id` int(11) NOT NULL,
							   `creator` int(11) NOT NULL,
							   `date_created` datetime NOT NULL,
							   `voided` tinyint(1) NOT NULL DEFAULT 0,
							   `voided_by` int(11) DEFAULT NULL,
							   `date_voided` datetime DEFAULT NULL,
							   `void_reason` varchar(255) DEFAULT NULL,
							   `changed_by` int(11) DEFAULT NULL,
							   `date_changed` datetime DEFAULT NULL,
							   `uuid` char(38) NOT NULL,
							   `order_group_reason` int(11) DEFAULT NULL,
							   `parent_order_group` int(11) DEFAULT NULL,
							   `previous_order_group` int(11) DEFAULT NULL,
							   PRIMARY KEY (`order_group_id`),
							   UNIQUE KEY `uuid_order_group` (`uuid`),
							   KEY `order_group_changed_by_fk` (`changed_by`),
							   KEY `order_group_creator_fk` (`creator`),
							   KEY `order_group_encounter_id_fk` (`encounter_id`),
							   KEY `order_group_order_group_reason_fk` (`order_group_reason`),
							   KEY `order_group_parent_order_group_fk` (`parent_order_group`),
							   KEY `order_group_patient_id_fk` (`patient_id`),
							   KEY `order_group_previous_order_group_fk` (`previous_order_group`),
							   KEY `order_group_set_id_fk` (`order_set_id`),
							   KEY `order_group_voided_by_fk` (`voided_by`),
							   CONSTRAINT `order_group_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
							   CONSTRAINT `order_group_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
							   CONSTRAINT `order_group_encounter_id_fk` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
							   CONSTRAINT `order_group_order_group_reason_fk` FOREIGN KEY (`order_group_reason`) REFERENCES `concept` (`concept_id`),
							   CONSTRAINT `order_group_parent_order_group_fk` FOREIGN KEY (`parent_order_group`) REFERENCES `order_group` (`order_group_id`),
							   CONSTRAINT `order_group_patient_id_fk` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
							   CONSTRAINT `order_group_previous_order_group_fk` FOREIGN KEY (`previous_order_group`) REFERENCES `order_group` (`order_group_id`),
							   CONSTRAINT `order_group_set_id_fk` FOREIGN KEY (`order_set_id`) REFERENCES `order_set` (`order_set_id`),
							   CONSTRAINT `order_group_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_group`
--

LOCK TABLES `order_group` WRITE;
/*!40000 ALTER TABLE `order_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_group_attribute`
--

DROP TABLE IF EXISTS `order_group_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_group_attribute` (
										 `order_group_attribute_id` int(11) NOT NULL AUTO_INCREMENT,
										 `order_group_id` int(11) NOT NULL,
										 `attribute_type_id` int(11) NOT NULL,
										 `value_reference` text NOT NULL,
										 `uuid` char(38) NOT NULL,
										 `creator` int(11) NOT NULL,
										 `date_created` datetime NOT NULL,
										 `changed_by` int(11) DEFAULT NULL,
										 `date_changed` datetime DEFAULT NULL,
										 `voided` tinyint(1) NOT NULL DEFAULT 0,
										 `voided_by` int(11) DEFAULT NULL,
										 `date_voided` datetime DEFAULT NULL,
										 `void_reason` varchar(255) DEFAULT NULL,
										 PRIMARY KEY (`order_group_attribute_id`),
										 UNIQUE KEY `uuid_order_group_attribute` (`uuid`),
										 KEY `order_group_attribute_attribute_type_id_fk` (`attribute_type_id`),
										 KEY `order_group_attribute_changed_by_fk` (`changed_by`),
										 KEY `order_group_attribute_creator_fk` (`creator`),
										 KEY `order_group_attribute_order_group_fk` (`order_group_id`),
										 KEY `order_group_attribute_voided_by_fk` (`voided_by`),
										 CONSTRAINT `order_group_attribute_attribute_type_id_fk` FOREIGN KEY (`attribute_type_id`) REFERENCES `order_group_attribute_type` (`order_group_attribute_type_id`),
										 CONSTRAINT `order_group_attribute_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
										 CONSTRAINT `order_group_attribute_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
										 CONSTRAINT `order_group_attribute_order_group_fk` FOREIGN KEY (`order_group_id`) REFERENCES `order_group` (`order_group_id`),
										 CONSTRAINT `order_group_attribute_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_group_attribute`
--

LOCK TABLES `order_group_attribute` WRITE;
/*!40000 ALTER TABLE `order_group_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_group_attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_group_attribute_type`
--

DROP TABLE IF EXISTS `order_group_attribute_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_group_attribute_type` (
											  `order_group_attribute_type_id` int(11) NOT NULL AUTO_INCREMENT,
											  `name` varchar(255) NOT NULL,
											  `description` varchar(1024) DEFAULT NULL,
											  `datatype` varchar(255) DEFAULT NULL,
											  `datatype_config` text DEFAULT NULL,
											  `preferred_handler` varchar(255) DEFAULT NULL,
											  `handler_config` text DEFAULT NULL,
											  `min_occurs` int(11) NOT NULL,
											  `max_occurs` int(11) DEFAULT NULL,
											  `creator` int(11) NOT NULL,
											  `date_created` datetime NOT NULL,
											  `changed_by` int(11) DEFAULT NULL,
											  `date_changed` datetime DEFAULT NULL,
											  `retired` tinyint(1) NOT NULL DEFAULT 0,
											  `retired_by` int(11) DEFAULT NULL,
											  `date_retired` datetime DEFAULT NULL,
											  `retire_reason` varchar(255) DEFAULT NULL,
											  `uuid` char(38) NOT NULL,
											  PRIMARY KEY (`order_group_attribute_type_id`),
											  UNIQUE KEY `order_group_attribute_type_name` (`name`),
											  UNIQUE KEY `uuid_order_group_attribute_type` (`uuid`),
											  KEY `order_group_attribute_type_changed_by_fk` (`changed_by`),
											  KEY `order_group_attribute_type_creator_fk` (`creator`),
											  KEY `order_group_attribute_type_retired_by_fk` (`retired_by`),
											  CONSTRAINT `order_group_attribute_type_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
											  CONSTRAINT `order_group_attribute_type_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
											  CONSTRAINT `order_group_attribute_type_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_group_attribute_type`
--

LOCK TABLES `order_group_attribute_type` WRITE;
/*!40000 ALTER TABLE `order_group_attribute_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_group_attribute_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_set`
--

DROP TABLE IF EXISTS `order_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_set` (
							 `order_set_id` int(11) NOT NULL AUTO_INCREMENT,
							 `operator` varchar(50) NOT NULL,
							 `name` varchar(255) NOT NULL,
							 `description` varchar(1000) DEFAULT NULL,
							 `creator` int(11) NOT NULL,
							 `date_created` datetime NOT NULL,
							 `retired` tinyint(1) NOT NULL DEFAULT 0,
							 `retired_by` int(11) DEFAULT NULL,
							 `date_retired` datetime DEFAULT NULL,
							 `retire_reason` varchar(255) DEFAULT NULL,
							 `changed_by` int(11) DEFAULT NULL,
							 `date_changed` datetime DEFAULT NULL,
							 `uuid` char(38) NOT NULL,
							 `category` int(11) DEFAULT NULL,
							 PRIMARY KEY (`order_set_id`),
							 UNIQUE KEY `uuid_order_set` (`uuid`),
							 KEY `category_order_set_fk` (`category`),
							 KEY `order_set_changed_by_fk` (`changed_by`),
							 KEY `order_set_creator_fk` (`creator`),
							 KEY `order_set_retired_by_fk` (`retired_by`),
							 CONSTRAINT `category_order_set_fk` FOREIGN KEY (`category`) REFERENCES `concept` (`concept_id`),
							 CONSTRAINT `order_set_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
							 CONSTRAINT `order_set_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
							 CONSTRAINT `order_set_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_set`
--

LOCK TABLES `order_set` WRITE;
/*!40000 ALTER TABLE `order_set` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_set` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_set_attribute`
--

DROP TABLE IF EXISTS `order_set_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_set_attribute` (
									   `order_set_attribute_id` int(11) NOT NULL AUTO_INCREMENT,
									   `order_set_id` int(11) NOT NULL,
									   `attribute_type_id` int(11) NOT NULL,
									   `value_reference` text NOT NULL,
									   `uuid` char(38) NOT NULL,
									   `creator` int(11) NOT NULL,
									   `date_created` datetime NOT NULL,
									   `changed_by` int(11) DEFAULT NULL,
									   `date_changed` datetime DEFAULT NULL,
									   `voided` tinyint(1) NOT NULL DEFAULT 0,
									   `voided_by` int(11) DEFAULT NULL,
									   `date_voided` datetime DEFAULT NULL,
									   `void_reason` varchar(255) DEFAULT NULL,
									   PRIMARY KEY (`order_set_attribute_id`),
									   UNIQUE KEY `uuid_order_set_attribute` (`uuid`),
									   KEY `order_set_attribute_attribute_type_id_fk` (`attribute_type_id`),
									   KEY `order_set_attribute_changed_by_fk` (`changed_by`),
									   KEY `order_set_attribute_creator_fk` (`creator`),
									   KEY `order_set_attribute_order_set_fk` (`order_set_id`),
									   KEY `order_set_attribute_voided_by_fk` (`voided_by`),
									   CONSTRAINT `order_set_attribute_attribute_type_id_fk` FOREIGN KEY (`attribute_type_id`) REFERENCES `order_set_attribute_type` (`order_set_attribute_type_id`),
									   CONSTRAINT `order_set_attribute_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									   CONSTRAINT `order_set_attribute_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									   CONSTRAINT `order_set_attribute_order_set_fk` FOREIGN KEY (`order_set_id`) REFERENCES `order_set` (`order_set_id`),
									   CONSTRAINT `order_set_attribute_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_set_attribute`
--

LOCK TABLES `order_set_attribute` WRITE;
/*!40000 ALTER TABLE `order_set_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_set_attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_set_attribute_type`
--

DROP TABLE IF EXISTS `order_set_attribute_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_set_attribute_type` (
											`order_set_attribute_type_id` int(11) NOT NULL AUTO_INCREMENT,
											`name` varchar(255) NOT NULL,
											`description` varchar(1024) DEFAULT NULL,
											`datatype` varchar(255) DEFAULT NULL,
											`datatype_config` text DEFAULT NULL,
											`preferred_handler` varchar(255) DEFAULT NULL,
											`handler_config` text DEFAULT NULL,
											`min_occurs` int(11) NOT NULL,
											`max_occurs` int(11) DEFAULT NULL,
											`creator` int(11) NOT NULL,
											`date_created` datetime NOT NULL,
											`changed_by` int(11) DEFAULT NULL,
											`date_changed` datetime DEFAULT NULL,
											`retired` tinyint(1) NOT NULL DEFAULT 0,
											`retired_by` int(11) DEFAULT NULL,
											`date_retired` datetime DEFAULT NULL,
											`retire_reason` varchar(255) DEFAULT NULL,
											`uuid` char(38) NOT NULL,
											PRIMARY KEY (`order_set_attribute_type_id`),
											UNIQUE KEY `name` (`name`),
											UNIQUE KEY `uuid` (`uuid`),
											KEY `order_set_attribute_type_changed_by_fk` (`changed_by`),
											KEY `order_set_attribute_type_creator_fk` (`creator`),
											KEY `order_set_attribute_type_retired_by_fk` (`retired_by`),
											CONSTRAINT `order_set_attribute_type_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
											CONSTRAINT `order_set_attribute_type_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
											CONSTRAINT `order_set_attribute_type_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_set_attribute_type`
--

LOCK TABLES `order_set_attribute_type` WRITE;
/*!40000 ALTER TABLE `order_set_attribute_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_set_attribute_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_set_member`
--

DROP TABLE IF EXISTS `order_set_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_set_member` (
									`order_set_member_id` int(11) NOT NULL AUTO_INCREMENT,
									`order_type` int(11) NOT NULL,
									`order_template` text DEFAULT NULL,
									`order_template_type` varchar(1024) DEFAULT NULL,
									`order_set_id` int(11) NOT NULL,
									`sequence_number` int(11) NOT NULL,
									`concept_id` int(11) NOT NULL,
									`creator` int(11) NOT NULL,
									`date_created` datetime NOT NULL,
									`retired` tinyint(1) NOT NULL DEFAULT 0,
									`retired_by` int(11) DEFAULT NULL,
									`date_retired` datetime DEFAULT NULL,
									`retire_reason` varchar(255) DEFAULT NULL,
									`changed_by` int(11) DEFAULT NULL,
									`date_changed` datetime DEFAULT NULL,
									`uuid` char(38) NOT NULL,
									PRIMARY KEY (`order_set_member_id`),
									UNIQUE KEY `uuid_order_set_member` (`uuid`),
									KEY `order_set_member_changed_by_fk` (`changed_by`),
									KEY `order_set_member_concept_id_fk` (`concept_id`),
									KEY `order_set_member_creator_fk` (`creator`),
									KEY `order_set_member_order_set_id_fk` (`order_set_id`),
									KEY `order_set_member_order_type_fk` (`order_type`),
									KEY `order_set_member_retired_by_fk` (`retired_by`),
									CONSTRAINT `order_set_member_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									CONSTRAINT `order_set_member_concept_id_fk` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
									CONSTRAINT `order_set_member_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									CONSTRAINT `order_set_member_order_set_id_fk` FOREIGN KEY (`order_set_id`) REFERENCES `order_set` (`order_set_id`),
									CONSTRAINT `order_set_member_order_type_fk` FOREIGN KEY (`order_type`) REFERENCES `order_type` (`order_type_id`),
									CONSTRAINT `order_set_member_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_set_member`
--

LOCK TABLES `order_set_member` WRITE;
/*!40000 ALTER TABLE `order_set_member` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_set_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_type`
--

DROP TABLE IF EXISTS `order_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_type` (
							  `order_type_id` int(11) NOT NULL AUTO_INCREMENT,
							  `name` varchar(255) NOT NULL DEFAULT '',
							  `description` text DEFAULT NULL,
							  `creator` int(11) NOT NULL DEFAULT 0,
							  `date_created` datetime NOT NULL,
							  `retired` tinyint(1) NOT NULL DEFAULT 0,
							  `retired_by` int(11) DEFAULT NULL,
							  `date_retired` datetime DEFAULT NULL,
							  `retire_reason` varchar(255) DEFAULT NULL,
							  `uuid` char(38) NOT NULL,
							  `java_class_name` varchar(255) NOT NULL,
							  `parent` int(11) DEFAULT NULL,
							  `changed_by` int(11) DEFAULT NULL,
							  `date_changed` datetime DEFAULT NULL,
							  PRIMARY KEY (`order_type_id`),
							  UNIQUE KEY `order_type_name` (`name`),
							  UNIQUE KEY `uuid_order_type` (`uuid`),
							  KEY `order_type_changed_by` (`changed_by`),
							  KEY `order_type_parent_order_type` (`parent`),
							  KEY `order_type_retired_status` (`retired`),
							  KEY `type_created_by` (`creator`),
							  KEY `user_who_retired_order_type` (`retired_by`),
							  CONSTRAINT `order_type_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
							  CONSTRAINT `order_type_parent_order_type` FOREIGN KEY (`parent`) REFERENCES `order_type` (`order_type_id`),
							  CONSTRAINT `type_created_by` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
							  CONSTRAINT `user_who_retired_order_type` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_type`
--

LOCK TABLES `order_type` WRITE;
/*!40000 ALTER TABLE `order_type` DISABLE KEYS */;
INSERT INTO `order_type` VALUES (2,'Drug Order','An order for a medication to be given to the patient',1,'2010-05-12 00:00:00',0,NULL,NULL,NULL,'131168f4-15f5-102d-96e4-000c29c2a5d7','org.openmrs.DrugOrder',NULL,NULL,NULL),(3,'Test Order','Order type for test orders',1,'2014-03-09 00:00:00',0,NULL,NULL,NULL,'52a447d3-a64a-11e3-9aeb-50e549534c5e','org.openmrs.TestOrder',NULL,NULL,NULL);
/*!40000 ALTER TABLE `order_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_type_class_map`
--

DROP TABLE IF EXISTS `order_type_class_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_type_class_map` (
										`order_type_id` int(11) NOT NULL,
										`concept_class_id` int(11) NOT NULL,
										PRIMARY KEY (`order_type_id`,`concept_class_id`),
										UNIQUE KEY `concept_class_id` (`concept_class_id`),
										CONSTRAINT `fk_order_type_class_map_concept_class_concept_class_id` FOREIGN KEY (`concept_class_id`) REFERENCES `concept_class` (`concept_class_id`),
										CONSTRAINT `fk_order_type_order_type_id` FOREIGN KEY (`order_type_id`) REFERENCES `order_type` (`order_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_type_class_map`
--

LOCK TABLES `order_type_class_map` WRITE;
/*!40000 ALTER TABLE `order_type_class_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_type_class_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `orders` (
						  `order_id` int(11) NOT NULL AUTO_INCREMENT,
						  `order_type_id` int(11) NOT NULL DEFAULT 0,
						  `concept_id` int(11) NOT NULL DEFAULT 0,
						  `orderer` int(11) NOT NULL,
						  `encounter_id` int(11) NOT NULL,
						  `instructions` text DEFAULT NULL,
						  `date_activated` datetime DEFAULT NULL,
						  `auto_expire_date` datetime DEFAULT NULL,
						  `date_stopped` datetime DEFAULT NULL,
						  `order_reason` int(11) DEFAULT NULL,
						  `order_reason_non_coded` varchar(255) DEFAULT NULL,
						  `creator` int(11) NOT NULL DEFAULT 0,
						  `date_created` datetime NOT NULL,
						  `voided` tinyint(1) NOT NULL DEFAULT 0,
						  `voided_by` int(11) DEFAULT NULL,
						  `date_voided` datetime DEFAULT NULL,
						  `void_reason` varchar(255) DEFAULT NULL,
						  `patient_id` int(11) NOT NULL,
						  `accession_number` varchar(255) DEFAULT NULL,
						  `uuid` char(38) NOT NULL,
						  `urgency` varchar(50) NOT NULL DEFAULT 'ROUTINE',
						  `order_number` varchar(50) NOT NULL,
						  `previous_order_id` int(11) DEFAULT NULL,
						  `order_action` varchar(50) NOT NULL,
						  `comment_to_fulfiller` varchar(1024) DEFAULT NULL,
						  `care_setting` int(11) NOT NULL,
						  `scheduled_date` datetime DEFAULT NULL,
						  `order_group_id` int(11) DEFAULT NULL,
						  `sort_weight` double DEFAULT NULL,
						  `fulfiller_comment` varchar(1024) DEFAULT NULL,
						  `fulfiller_status` varchar(50) DEFAULT NULL,
						  `form_namespace_and_path` varchar(255) DEFAULT NULL,
						  PRIMARY KEY (`order_id`),
						  UNIQUE KEY `uuid_orders` (`uuid`),
						  KEY `discontinued_because` (`order_reason`),
						  KEY `fk_orderer_provider` (`orderer`),
						  KEY `order_creator` (`creator`),
						  KEY `order_for_patient` (`patient_id`),
						  KEY `orders_accession_number` (`accession_number`),
						  KEY `orders_care_setting` (`care_setting`),
						  KEY `orders_in_encounter` (`encounter_id`),
						  KEY `orders_order_group_id_fk` (`order_group_id`),
						  KEY `orders_order_number` (`order_number`),
						  KEY `previous_order_id_order_id` (`previous_order_id`),
						  KEY `type_of_order` (`order_type_id`),
						  KEY `user_who_voided_order` (`voided_by`),
						  CONSTRAINT `discontinued_because` FOREIGN KEY (`order_reason`) REFERENCES `concept` (`concept_id`),
						  CONSTRAINT `fk_orderer_provider` FOREIGN KEY (`orderer`) REFERENCES `provider` (`provider_id`),
						  CONSTRAINT `order_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
						  CONSTRAINT `order_for_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE,
						  CONSTRAINT `orders_care_setting` FOREIGN KEY (`care_setting`) REFERENCES `care_setting` (`care_setting_id`),
						  CONSTRAINT `orders_in_encounter` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
						  CONSTRAINT `orders_order_group_id_fk` FOREIGN KEY (`order_group_id`) REFERENCES `order_group` (`order_group_id`),
						  CONSTRAINT `previous_order_id_order_id` FOREIGN KEY (`previous_order_id`) REFERENCES `orders` (`order_id`),
						  CONSTRAINT `type_of_order` FOREIGN KEY (`order_type_id`) REFERENCES `order_type` (`order_type_id`),
						  CONSTRAINT `user_who_voided_order` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patient`
--

DROP TABLE IF EXISTS `patient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `patient` (
						   `patient_id` int(11) NOT NULL,
						   `creator` int(11) NOT NULL DEFAULT 0,
						   `date_created` datetime NOT NULL,
						   `changed_by` int(11) DEFAULT NULL,
						   `date_changed` datetime DEFAULT NULL,
						   `voided` tinyint(1) NOT NULL DEFAULT 0,
						   `voided_by` int(11) DEFAULT NULL,
						   `date_voided` datetime DEFAULT NULL,
						   `void_reason` varchar(255) DEFAULT NULL,
						   `allergy_status` varchar(50) NOT NULL DEFAULT 'Unknown',
						   PRIMARY KEY (`patient_id`),
						   KEY `user_who_changed_pat` (`changed_by`),
						   KEY `user_who_created_patient` (`creator`),
						   KEY `user_who_voided_patient` (`voided_by`),
						   CONSTRAINT `person_id_for_patient` FOREIGN KEY (`patient_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
						   CONSTRAINT `user_who_changed_pat` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
						   CONSTRAINT `user_who_created_patient` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
						   CONSTRAINT `user_who_voided_patient` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient`
--

LOCK TABLES `patient` WRITE;
/*!40000 ALTER TABLE `patient` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patient_identifier`
--

DROP TABLE IF EXISTS `patient_identifier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `patient_identifier` (
									  `patient_identifier_id` int(11) NOT NULL AUTO_INCREMENT,
									  `patient_id` int(11) NOT NULL DEFAULT 0,
									  `identifier` varchar(50) NOT NULL DEFAULT '',
									  `identifier_type` int(11) NOT NULL DEFAULT 0,
									  `preferred` tinyint(1) NOT NULL DEFAULT 0,
									  `location_id` int(11) DEFAULT 0,
									  `creator` int(11) NOT NULL DEFAULT 0,
									  `date_created` datetime NOT NULL,
									  `date_changed` datetime DEFAULT NULL,
									  `changed_by` int(11) DEFAULT NULL,
									  `voided` tinyint(1) NOT NULL DEFAULT 0,
									  `voided_by` int(11) DEFAULT NULL,
									  `date_voided` datetime DEFAULT NULL,
									  `void_reason` varchar(255) DEFAULT NULL,
									  `uuid` char(38) NOT NULL,
									  PRIMARY KEY (`patient_identifier_id`),
									  UNIQUE KEY `uuid_patient_identifier` (`uuid`),
									  KEY `defines_identifier_type` (`identifier_type`),
									  KEY `identifier_creator` (`creator`),
									  KEY `identifier_name` (`identifier`),
									  KEY `identifier_voider` (`voided_by`),
									  KEY `idx_patient_identifier_patient` (`patient_id`),
									  KEY `patient_identifier_changed_by` (`changed_by`),
									  KEY `patient_identifier_ibfk_2` (`location_id`),
									  CONSTRAINT `defines_identifier_type` FOREIGN KEY (`identifier_type`) REFERENCES `patient_identifier_type` (`patient_identifier_type_id`),
									  CONSTRAINT `fk_patient_id_patient_identifier` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
									  CONSTRAINT `identifier_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									  CONSTRAINT `identifier_voider` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
									  CONSTRAINT `patient_identifier_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									  CONSTRAINT `patient_identifier_ibfk_2` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_identifier`
--

LOCK TABLES `patient_identifier` WRITE;
/*!40000 ALTER TABLE `patient_identifier` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient_identifier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patient_identifier_type`
--

DROP TABLE IF EXISTS `patient_identifier_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `patient_identifier_type` (
										   `patient_identifier_type_id` int(11) NOT NULL AUTO_INCREMENT,
										   `name` varchar(50) NOT NULL DEFAULT '',
										   `description` text DEFAULT NULL,
										   `format` varchar(255) DEFAULT NULL,
										   `check_digit` tinyint(1) NOT NULL DEFAULT 0,
										   `creator` int(11) NOT NULL DEFAULT 0,
										   `date_created` datetime NOT NULL,
										   `required` tinyint(1) NOT NULL DEFAULT 0,
										   `format_description` varchar(255) DEFAULT NULL,
										   `validator` varchar(200) DEFAULT NULL,
										   `location_behavior` varchar(50) DEFAULT NULL,
										   `retired` tinyint(1) NOT NULL DEFAULT 0,
										   `retired_by` int(11) DEFAULT NULL,
										   `date_retired` datetime DEFAULT NULL,
										   `retire_reason` varchar(255) DEFAULT NULL,
										   `uuid` char(38) NOT NULL,
										   `uniqueness_behavior` varchar(50) DEFAULT NULL,
										   `date_changed` datetime DEFAULT NULL,
										   `changed_by` int(11) DEFAULT NULL,
										   PRIMARY KEY (`patient_identifier_type_id`),
										   UNIQUE KEY `uuid_patient_identifier_type` (`uuid`),
										   KEY `patient_identifier_type_changed_by` (`changed_by`),
										   KEY `patient_identifier_type_retired_status` (`retired`),
										   KEY `type_creator` (`creator`),
										   KEY `user_who_retired_patient_identifier_type` (`retired_by`),
										   CONSTRAINT `patient_identifier_type_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
										   CONSTRAINT `type_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
										   CONSTRAINT `user_who_retired_patient_identifier_type` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_identifier_type`
--

LOCK TABLES `patient_identifier_type` WRITE;
/*!40000 ALTER TABLE `patient_identifier_type` DISABLE KEYS */;
INSERT INTO `patient_identifier_type` VALUES (1,'OpenMRS Identification Number','Unique number used in OpenMRS','',1,1,'2005-09-22 00:00:00',0,NULL,'org.openmrs.patient.impl.LuhnIdentifierValidator',NULL,0,NULL,NULL,NULL,'8d793bee-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL,NULL),(2,'Old Identification Number','Number given out prior to the OpenMRS system (No check digit)','',0,1,'2005-09-22 00:00:00',0,NULL,NULL,NULL,0,NULL,NULL,NULL,'8d79403a-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL,NULL);
/*!40000 ALTER TABLE `patient_identifier_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patient_program`
--

DROP TABLE IF EXISTS `patient_program`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `patient_program` (
								   `patient_program_id` int(11) NOT NULL AUTO_INCREMENT,
								   `patient_id` int(11) NOT NULL DEFAULT 0,
								   `program_id` int(11) NOT NULL DEFAULT 0,
								   `date_enrolled` datetime DEFAULT NULL,
								   `date_completed` datetime DEFAULT NULL,
								   `location_id` int(11) DEFAULT NULL,
								   `outcome_concept_id` int(11) DEFAULT NULL,
								   `creator` int(11) NOT NULL DEFAULT 0,
								   `date_created` datetime NOT NULL,
								   `changed_by` int(11) DEFAULT NULL,
								   `date_changed` datetime DEFAULT NULL,
								   `voided` tinyint(1) NOT NULL DEFAULT 0,
								   `voided_by` int(11) DEFAULT NULL,
								   `date_voided` datetime DEFAULT NULL,
								   `void_reason` varchar(255) DEFAULT NULL,
								   `uuid` char(38) NOT NULL,
								   PRIMARY KEY (`patient_program_id`),
								   UNIQUE KEY `uuid_patient_program` (`uuid`),
								   KEY `patient_in_program` (`patient_id`),
								   KEY `patient_program_creator` (`creator`),
								   KEY `patient_program_location_id` (`location_id`),
								   KEY `patient_program_outcome_concept_id_fk` (`outcome_concept_id`),
								   KEY `program_for_patient` (`program_id`),
								   KEY `user_who_changed` (`changed_by`),
								   KEY `user_who_voided_patient_program` (`voided_by`),
								   CONSTRAINT `patient_in_program` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE,
								   CONSTRAINT `patient_program_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								   CONSTRAINT `patient_program_location_id` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`),
								   CONSTRAINT `patient_program_outcome_concept_id_fk` FOREIGN KEY (`outcome_concept_id`) REFERENCES `concept` (`concept_id`),
								   CONSTRAINT `program_for_patient` FOREIGN KEY (`program_id`) REFERENCES `program` (`program_id`),
								   CONSTRAINT `user_who_changed` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
								   CONSTRAINT `user_who_voided_patient_program` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_program`
--

LOCK TABLES `patient_program` WRITE;
/*!40000 ALTER TABLE `patient_program` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient_program` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patient_program_attribute`
--

DROP TABLE IF EXISTS `patient_program_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `patient_program_attribute` (
											 `patient_program_attribute_id` int(11) NOT NULL AUTO_INCREMENT,
											 `patient_program_id` int(11) NOT NULL,
											 `attribute_type_id` int(11) NOT NULL,
											 `value_reference` text NOT NULL,
											 `uuid` char(38) NOT NULL,
											 `creator` int(11) NOT NULL,
											 `date_created` datetime NOT NULL,
											 `changed_by` int(11) DEFAULT NULL,
											 `date_changed` datetime DEFAULT NULL,
											 `voided` tinyint(1) NOT NULL DEFAULT 0,
											 `voided_by` int(11) DEFAULT NULL,
											 `date_voided` datetime DEFAULT NULL,
											 `void_reason` varchar(255) DEFAULT NULL,
											 PRIMARY KEY (`patient_program_attribute_id`),
											 UNIQUE KEY `uuid_patient_program_attribute` (`uuid`),
											 KEY `patient_program_attribute_attributetype_fk` (`attribute_type_id`),
											 KEY `patient_program_attribute_changed_by_fk` (`changed_by`),
											 KEY `patient_program_attribute_creator_fk` (`creator`),
											 KEY `patient_program_attribute_programid_fk` (`patient_program_id`),
											 KEY `patient_program_attribute_voided_by_fk` (`voided_by`),
											 CONSTRAINT `patient_program_attribute_attributetype_fk` FOREIGN KEY (`attribute_type_id`) REFERENCES `program_attribute_type` (`program_attribute_type_id`),
											 CONSTRAINT `patient_program_attribute_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
											 CONSTRAINT `patient_program_attribute_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
											 CONSTRAINT `patient_program_attribute_programid_fk` FOREIGN KEY (`patient_program_id`) REFERENCES `patient_program` (`patient_program_id`),
											 CONSTRAINT `patient_program_attribute_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_program_attribute`
--

LOCK TABLES `patient_program_attribute` WRITE;
/*!40000 ALTER TABLE `patient_program_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient_program_attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patient_state`
--

DROP TABLE IF EXISTS `patient_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `patient_state` (
								 `patient_state_id` int(11) NOT NULL AUTO_INCREMENT,
								 `patient_program_id` int(11) NOT NULL DEFAULT 0,
								 `state` int(11) NOT NULL DEFAULT 0,
								 `start_date` date DEFAULT NULL,
								 `end_date` date DEFAULT NULL,
								 `creator` int(11) NOT NULL DEFAULT 0,
								 `date_created` datetime NOT NULL,
								 `changed_by` int(11) DEFAULT NULL,
								 `date_changed` datetime DEFAULT NULL,
								 `voided` tinyint(1) NOT NULL DEFAULT 0,
								 `voided_by` int(11) DEFAULT NULL,
								 `date_voided` datetime DEFAULT NULL,
								 `void_reason` varchar(255) DEFAULT NULL,
								 `uuid` char(38) NOT NULL,
								 `form_namespace_and_path` varchar(255) DEFAULT NULL,
								 `encounter_id` int(11) DEFAULT NULL,
								 PRIMARY KEY (`patient_state_id`),
								 UNIQUE KEY `uuid_patient_state` (`uuid`),
								 KEY `patient_program_for_state` (`patient_program_id`),
								 KEY `patient_state_changer` (`changed_by`),
								 KEY `patient_state_creator` (`creator`),
								 KEY `patient_state_voider` (`voided_by`),
								 KEY `state_for_patient` (`state`),
								 KEY `patient_state_encounter_id_fk` (`encounter_id`),
								 CONSTRAINT `patient_program_for_state` FOREIGN KEY (`patient_program_id`) REFERENCES `patient_program` (`patient_program_id`),
								 CONSTRAINT `patient_state_changer` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
								 CONSTRAINT `patient_state_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								 CONSTRAINT `patient_state_encounter_id_fk` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
								 CONSTRAINT `patient_state_voider` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
								 CONSTRAINT `state_for_patient` FOREIGN KEY (`state`) REFERENCES `program_workflow_state` (`program_workflow_state_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_state`
--

LOCK TABLES `patient_state` WRITE;
/*!40000 ALTER TABLE `patient_state` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient_state` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `person`
--

DROP TABLE IF EXISTS `person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person` (
						  `person_id` int(11) NOT NULL AUTO_INCREMENT,
						  `gender` varchar(50) DEFAULT '',
						  `birthdate` date DEFAULT NULL,
						  `birthdate_estimated` tinyint(1) NOT NULL DEFAULT 0,
						  `dead` tinyint(1) NOT NULL DEFAULT 0,
						  `death_date` datetime DEFAULT NULL,
						  `cause_of_death` int(11) DEFAULT NULL,
						  `creator` int(11) DEFAULT NULL,
						  `date_created` datetime NOT NULL,
						  `changed_by` int(11) DEFAULT NULL,
						  `date_changed` datetime DEFAULT NULL,
						  `voided` tinyint(1) NOT NULL DEFAULT 0,
						  `voided_by` int(11) DEFAULT NULL,
						  `date_voided` datetime DEFAULT NULL,
						  `void_reason` varchar(255) DEFAULT NULL,
						  `uuid` char(38) NOT NULL,
						  `deathdate_estimated` tinyint(1) NOT NULL DEFAULT 0,
						  `birthtime` time DEFAULT NULL,
						  `cause_of_death_non_coded` varchar(255) DEFAULT NULL,
						  PRIMARY KEY (`person_id`),
						  UNIQUE KEY `uuid` (`uuid`),
						  KEY `person_birthdate` (`birthdate`),
						  KEY `person_death_date` (`death_date`),
						  KEY `person_died_because` (`cause_of_death`),
						  KEY `user_who_changed_person` (`changed_by`),
						  KEY `user_who_created_person` (`creator`),
						  KEY `user_who_voided_person` (`voided_by`),
						  CONSTRAINT `person_died_because` FOREIGN KEY (`cause_of_death`) REFERENCES `concept` (`concept_id`),
						  CONSTRAINT `user_who_changed_person` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
						  CONSTRAINT `user_who_created_person` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
						  CONSTRAINT `user_who_voided_person` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `person`
--

LOCK TABLES `person` WRITE;
/*!40000 ALTER TABLE `person` DISABLE KEYS */;
INSERT INTO `person` VALUES (1,'M',NULL,0,0,NULL,NULL,NULL,'2005-01-01 00:00:00',NULL,NULL,0,NULL,NULL,NULL,'5f87c042-6814-11e8-923f-e9a88dcb533f',0,NULL,NULL);
/*!40000 ALTER TABLE `person` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `person_address`
--

DROP TABLE IF EXISTS `person_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person_address` (
								  `person_address_id` int(11) NOT NULL AUTO_INCREMENT,
								  `person_id` int(11) DEFAULT NULL,
								  `preferred` tinyint(1) NOT NULL DEFAULT 0,
								  `address1` varchar(255) DEFAULT NULL,
								  `address2` varchar(255) DEFAULT NULL,
								  `city_village` varchar(255) DEFAULT NULL,
								  `state_province` varchar(255) DEFAULT NULL,
								  `postal_code` varchar(50) DEFAULT NULL,
								  `country` varchar(50) DEFAULT NULL,
								  `latitude` varchar(50) DEFAULT NULL,
								  `longitude` varchar(50) DEFAULT NULL,
								  `start_date` datetime DEFAULT NULL,
								  `end_date` datetime DEFAULT NULL,
								  `creator` int(11) NOT NULL DEFAULT 0,
								  `date_created` datetime NOT NULL,
								  `voided` tinyint(1) NOT NULL DEFAULT 0,
								  `voided_by` int(11) DEFAULT NULL,
								  `date_voided` datetime DEFAULT NULL,
								  `void_reason` varchar(255) DEFAULT NULL,
								  `county_district` varchar(255) DEFAULT NULL,
								  `address3` varchar(255) DEFAULT NULL,
								  `address4` varchar(255) DEFAULT NULL,
								  `address5` varchar(255) DEFAULT NULL,
								  `address6` varchar(255) DEFAULT NULL,
								  `date_changed` datetime DEFAULT NULL,
								  `changed_by` int(11) DEFAULT NULL,
								  `uuid` char(38) NOT NULL,
								  `address7` varchar(255) DEFAULT NULL,
								  `address8` varchar(255) DEFAULT NULL,
								  `address9` varchar(255) DEFAULT NULL,
								  `address10` varchar(255) DEFAULT NULL,
								  `address11` varchar(255) DEFAULT NULL,
								  `address12` varchar(255) DEFAULT NULL,
								  `address13` varchar(255) DEFAULT NULL,
								  `address14` varchar(255) DEFAULT NULL,
								  `address15` varchar(255) DEFAULT NULL,
								  PRIMARY KEY (`person_address_id`),
								  UNIQUE KEY `uuid_person_address` (`uuid`),
								  KEY `address_for_person` (`person_id`),
								  KEY `patient_address_creator` (`creator`),
								  KEY `patient_address_void` (`voided_by`),
								  KEY `person_address_changed_by` (`changed_by`),
								  CONSTRAINT `address_for_person` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
								  CONSTRAINT `patient_address_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								  CONSTRAINT `patient_address_void` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
								  CONSTRAINT `person_address_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `person_address`
--

LOCK TABLES `person_address` WRITE;
/*!40000 ALTER TABLE `person_address` DISABLE KEYS */;
/*!40000 ALTER TABLE `person_address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `person_attribute`
--

DROP TABLE IF EXISTS `person_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person_attribute` (
									`person_attribute_id` int(11) NOT NULL AUTO_INCREMENT,
									`person_id` int(11) NOT NULL DEFAULT 0,
									`value` varchar(50) NOT NULL DEFAULT '',
									`person_attribute_type_id` int(11) NOT NULL DEFAULT 0,
									`creator` int(11) NOT NULL DEFAULT 0,
									`date_created` datetime NOT NULL,
									`changed_by` int(11) DEFAULT NULL,
									`date_changed` datetime DEFAULT NULL,
									`voided` tinyint(1) NOT NULL DEFAULT 0,
									`voided_by` int(11) DEFAULT NULL,
									`date_voided` datetime DEFAULT NULL,
									`void_reason` varchar(255) DEFAULT NULL,
									`uuid` char(38) NOT NULL,
									PRIMARY KEY (`person_attribute_id`),
									UNIQUE KEY `uuid_person_attribute` (`uuid`),
									KEY `attribute_changer` (`changed_by`),
									KEY `attribute_creator` (`creator`),
									KEY `attribute_voider` (`voided_by`),
									KEY `defines_attribute_type` (`person_attribute_type_id`),
									KEY `identifies_person` (`person_id`),
									CONSTRAINT `attribute_changer` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									CONSTRAINT `attribute_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									CONSTRAINT `attribute_voider` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
									CONSTRAINT `defines_attribute_type` FOREIGN KEY (`person_attribute_type_id`) REFERENCES `person_attribute_type` (`person_attribute_type_id`),
									CONSTRAINT `identifies_person` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `person_attribute`
--

LOCK TABLES `person_attribute` WRITE;
/*!40000 ALTER TABLE `person_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `person_attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `person_attribute_type`
--

DROP TABLE IF EXISTS `person_attribute_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person_attribute_type` (
										 `person_attribute_type_id` int(11) NOT NULL AUTO_INCREMENT,
										 `name` varchar(50) NOT NULL DEFAULT '',
										 `description` text DEFAULT NULL,
										 `format` varchar(50) DEFAULT NULL,
										 `foreign_key` int(11) DEFAULT NULL,
										 `searchable` tinyint(1) NOT NULL DEFAULT 0,
										 `creator` int(11) NOT NULL DEFAULT 0,
										 `date_created` datetime NOT NULL,
										 `changed_by` int(11) DEFAULT NULL,
										 `date_changed` datetime DEFAULT NULL,
										 `retired` tinyint(1) NOT NULL DEFAULT 0,
										 `retired_by` int(11) DEFAULT NULL,
										 `date_retired` datetime DEFAULT NULL,
										 `retire_reason` varchar(255) DEFAULT NULL,
										 `edit_privilege` varchar(255) DEFAULT NULL,
										 `sort_weight` double DEFAULT NULL,
										 `uuid` char(38) NOT NULL,
										 PRIMARY KEY (`person_attribute_type_id`),
										 UNIQUE KEY `uuid_person_attribute_type` (`uuid`),
										 KEY `attribute_is_searchable` (`searchable`),
										 KEY `attribute_type_changer` (`changed_by`),
										 KEY `attribute_type_creator` (`creator`),
										 KEY `name_of_attribute` (`name`),
										 KEY `person_attribute_type_retired_status` (`retired`),
										 KEY `privilege_which_can_edit` (`edit_privilege`),
										 KEY `user_who_retired_person_attribute_type` (`retired_by`),
										 CONSTRAINT `attribute_type_changer` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
										 CONSTRAINT `attribute_type_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
										 CONSTRAINT `privilege_which_can_edit` FOREIGN KEY (`edit_privilege`) REFERENCES `privilege` (`privilege`),
										 CONSTRAINT `user_who_retired_person_attribute_type` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `person_attribute_type`
--

LOCK TABLES `person_attribute_type` WRITE;
/*!40000 ALTER TABLE `person_attribute_type` DISABLE KEYS */;
INSERT INTO `person_attribute_type` VALUES (1,'Race','Group of persons related by common descent or heredity','java.lang.String',0,0,1,'2007-05-04 00:00:00',NULL,NULL,0,NULL,NULL,NULL,NULL,6,'8d871386-c2cc-11de-8d13-0010c6dffd0f'),(2,'Birthplace','Location of persons birth','java.lang.String',0,0,1,'2007-05-04 00:00:00',NULL,NULL,0,NULL,NULL,NULL,NULL,0,'8d8718c2-c2cc-11de-8d13-0010c6dffd0f'),(3,'Citizenship','Country of which this person is a member','java.lang.String',0,0,1,'2007-05-04 00:00:00',NULL,NULL,0,NULL,NULL,NULL,NULL,1,'8d871afc-c2cc-11de-8d13-0010c6dffd0f'),(4,'Mother\'s Name','First or last name of this person\'s mother','java.lang.String',0,0,1,'2007-05-04 00:00:00',NULL,NULL,0,NULL,NULL,NULL,NULL,5,'8d871d18-c2cc-11de-8d13-0010c6dffd0f'),(5,'Civil Status','Marriage status of this person','org.openmrs.Concept',1054,0,1,'2007-05-04 00:00:00',NULL,NULL,0,NULL,NULL,NULL,NULL,2,'8d871f2a-c2cc-11de-8d13-0010c6dffd0f'),(6,'Health District','District/region in which this patient\' home health center resides','java.lang.String',0,0,1,'2007-05-04 00:00:00',NULL,NULL,0,NULL,NULL,NULL,NULL,4,'8d872150-c2cc-11de-8d13-0010c6dffd0f'),(7,'Health Center','Specific Location of this person\'s home health center.','org.openmrs.Location',0,0,1,'2007-05-04 00:00:00',NULL,NULL,0,NULL,NULL,NULL,NULL,3,'8d87236c-c2cc-11de-8d13-0010c6dffd0f');
/*!40000 ALTER TABLE `person_attribute_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `person_merge_log`
--

DROP TABLE IF EXISTS `person_merge_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person_merge_log` (
									`person_merge_log_id` int(11) NOT NULL AUTO_INCREMENT,
									`winner_person_id` int(11) NOT NULL,
									`loser_person_id` int(11) NOT NULL,
									`creator` int(11) NOT NULL,
									`date_created` datetime NOT NULL,
									`merged_data` text NOT NULL,
									`uuid` char(38) NOT NULL,
									`changed_by` int(11) DEFAULT NULL,
									`date_changed` datetime DEFAULT NULL,
									`voided` tinyint(1) NOT NULL DEFAULT 0,
									`voided_by` int(11) DEFAULT NULL,
									`date_voided` datetime DEFAULT NULL,
									`void_reason` varchar(255) DEFAULT NULL,
									PRIMARY KEY (`person_merge_log_id`),
									UNIQUE KEY `uuid_person_merge_log` (`uuid`),
									KEY `person_merge_log_changed_by_fk` (`changed_by`),
									KEY `person_merge_log_creator` (`creator`),
									KEY `person_merge_log_loser` (`loser_person_id`),
									KEY `person_merge_log_voided_by_fk` (`voided_by`),
									KEY `person_merge_log_winner` (`winner_person_id`),
									CONSTRAINT `person_merge_log_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									CONSTRAINT `person_merge_log_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									CONSTRAINT `person_merge_log_loser` FOREIGN KEY (`loser_person_id`) REFERENCES `person` (`person_id`),
									CONSTRAINT `person_merge_log_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
									CONSTRAINT `person_merge_log_winner` FOREIGN KEY (`winner_person_id`) REFERENCES `person` (`person_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `person_merge_log`
--

LOCK TABLES `person_merge_log` WRITE;
/*!40000 ALTER TABLE `person_merge_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `person_merge_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `person_name`
--

DROP TABLE IF EXISTS `person_name`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person_name` (
							   `person_name_id` int(11) NOT NULL AUTO_INCREMENT,
							   `preferred` tinyint(1) NOT NULL DEFAULT 0,
							   `person_id` int(11) NOT NULL,
							   `prefix` varchar(50) DEFAULT NULL,
							   `given_name` varchar(50) DEFAULT NULL,
							   `middle_name` varchar(50) DEFAULT NULL,
							   `family_name_prefix` varchar(50) DEFAULT NULL,
							   `family_name` varchar(50) DEFAULT NULL,
							   `family_name2` varchar(50) DEFAULT NULL,
							   `family_name_suffix` varchar(50) DEFAULT NULL,
							   `degree` varchar(50) DEFAULT NULL,
							   `creator` int(11) NOT NULL DEFAULT 0,
							   `date_created` datetime NOT NULL,
							   `voided` tinyint(1) NOT NULL DEFAULT 0,
							   `voided_by` int(11) DEFAULT NULL,
							   `date_voided` datetime DEFAULT NULL,
							   `void_reason` varchar(255) DEFAULT NULL,
							   `changed_by` int(11) DEFAULT NULL,
							   `date_changed` datetime DEFAULT NULL,
							   `uuid` char(38) NOT NULL,
							   PRIMARY KEY (`person_name_id`),
							   UNIQUE KEY `uuid_person_name` (`uuid`),
							   KEY `family_name2` (`family_name2`),
							   KEY `first_name` (`given_name`),
							   KEY `last_name` (`family_name`),
							   KEY `middle_name` (`middle_name`),
							   KEY `name_for_person` (`person_id`),
							   KEY `user_who_made_name` (`creator`),
							   KEY `user_who_voided_name` (`voided_by`),
							   CONSTRAINT `name_for_person` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE,
							   CONSTRAINT `user_who_made_name` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
							   CONSTRAINT `user_who_voided_name` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `person_name`
--

LOCK TABLES `person_name` WRITE;
/*!40000 ALTER TABLE `person_name` DISABLE KEYS */;
INSERT INTO `person_name` VALUES (1,1,1,NULL,'Super','',NULL,'User',NULL,NULL,NULL,1,'2005-01-01 00:00:00',0,NULL,NULL,NULL,NULL,NULL,'5f897a68-6814-11e8-923f-e9a88dcb533f');
/*!40000 ALTER TABLE `person_name` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `privilege`
--

DROP TABLE IF EXISTS `privilege`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `privilege` (
							 `privilege` varchar(255) NOT NULL,
							 `description` text DEFAULT NULL,
							 `uuid` char(38) NOT NULL,
							 PRIMARY KEY (`privilege`),
							 UNIQUE KEY `uuid_privilege` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `privilege`
--

LOCK TABLES `privilege` WRITE;
/*!40000 ALTER TABLE `privilege` DISABLE KEYS */;
INSERT INTO `privilege` VALUES ('Add Allergies','Add allergies','8d04fc61-45de-4d16-a2f7-39ccbda88739'),('Add Cohorts','Able to add a cohort to the system','5f8a2896-6814-11e8-923f-e9a88dcb533f'),('Add Concept Proposals','Able to add concept proposals to the system','5f8a2a1c-6814-11e8-923f-e9a88dcb533f'),('Add Encounters','Able to add patient encounters','5f8a2a8a-6814-11e8-923f-e9a88dcb533f'),('Add HL7 Inbound Archive','Able to add an HL7 archive item','3192b2c9-3844-42f7-8e8e-f8964768ca26'),('Add HL7 Inbound Exception','Able to add an HL7 error item','e41c644c-1866-4572-a78d-6bfe36dd6a4b'),('Add HL7 Inbound Queue','Able to add an HL7 Queue item','b20ee658-9faa-4c6a-bc95-b5a9a4bfa4c2'),('Add HL7 Source','Able to add an HL7 Source','b8a2625f-3130-4b15-a3ba-907600d1bb03'),('Add Observations','Able to add patient observations','5f8a2ada-6814-11e8-923f-e9a88dcb533f'),('Add Orders','Able to add orders','5f8a2b7a-6814-11e8-923f-e9a88dcb533f'),('Add Patient Identifiers','Able to add patient identifiers','5f8a2bc0-6814-11e8-923f-e9a88dcb533f'),('Add Patient Programs','Able to add patients to programs','5f8a2c10-6814-11e8-923f-e9a88dcb533f'),('Add Patients','Able to add patients','5f8a2c56-6814-11e8-923f-e9a88dcb533f'),('Add People','Able to add person objects','5f8a2c92-6814-11e8-923f-e9a88dcb533f'),('Add Problems','Add problems','2f6d256a-1f73-4776-ad5b-f94c80f7beea'),('Add Relationships','Able to add relationships','5f8a2cce-6814-11e8-923f-e9a88dcb533f'),('Add Report Objects','Able to add report objects','5f8a2cf6-6814-11e8-923f-e9a88dcb533f'),('Add Reports','Able to add reports','5f8a2d32-6814-11e8-923f-e9a88dcb533f'),('Add Users','Able to add users to OpenMRS','5f8a2d5a-6814-11e8-923f-e9a88dcb533f'),('Add Visits','Able to add visits','65d14b28-3989-11e6-899a-a4d646d86a8a'),('Assign System Developer Role','Able to assign System Developer role','09cbaabc-ae63-44ed-9d4d-34ec6610e0a5'),('Configure Visits','Able to choose encounter visit handler and enable/disable encounter visits','d7899df6-68d7-49f1-bc3a-a1a64be157d6'),('Delete Cohorts','Able to add a cohort to the system','5f8a2d8c-6814-11e8-923f-e9a88dcb533f'),('Delete Concept Proposals','Able to delete concept proposals from the system','5f8a2dbe-6814-11e8-923f-e9a88dcb533f'),('Delete Conditions','Able to delete conditions','ef725ffa-2d14-41d8-b655-2d2430963b21'),('Delete Diagnoses','Able to delete diagnoses','a7882352-41b4-4da7-b422-84470ede7052'),('Delete Encounters','Able to delete patient encounters','5f8a2de6-6814-11e8-923f-e9a88dcb533f'),('Delete HL7 Inbound Archive','Able to delete/retire an HL7 archive item','7cd1f479-4e6a-422d-8b4d-7a7e5758d253'),('Delete HL7 Inbound Exception','Able to delete an HL7 archive item','25109fd5-edd1-4d11-84fe-a50b4001b50b'),('Delete HL7 Inbound Queue','Able to delete an HL7 Queue item','0e39bf44-a08b-4d61-9d20-08d2cc0ab51e'),('Delete Notes','Able to delete patient notes','ff39bade-4ea2-463b-8a66-ff4b26f85f25'),('Delete Observations','Able to delete patient observations','5f8a2e18-6814-11e8-923f-e9a88dcb533f'),('Delete Orders','Able to delete orders','5f8a2e40-6814-11e8-923f-e9a88dcb533f'),('Delete Patient Identifiers','Able to delete patient identifiers','5f8a2e72-6814-11e8-923f-e9a88dcb533f'),('Delete Patient Programs','Able to delete patients from programs','5f8a2ea4-6814-11e8-923f-e9a88dcb533f'),('Delete Patients','Able to delete patients','5f8a2ecc-6814-11e8-923f-e9a88dcb533f'),('Delete People','Able to delete objects','5f8a2efe-6814-11e8-923f-e9a88dcb533f'),('Delete Relationships','Able to delete relationships','5f8a2f26-6814-11e8-923f-e9a88dcb533f'),('Delete Report Objects','Able to delete report objects','5f8a2f58-6814-11e8-923f-e9a88dcb533f'),('Delete Reports','Able to delete reports','5f8a2f8a-6814-11e8-923f-e9a88dcb533f'),('Delete Users','Able to delete users in OpenMRS','5f8a2fb2-6814-11e8-923f-e9a88dcb533f'),('Delete Visits','Able to delete visits','02d02051-931b-4325-88d9-bf1a6cc85f5d'),('Edit Allergies','Able to edit allergies','507e9f66-ee25-4113-a55a-4b050022f55a'),('Edit Cohorts','Able to add a cohort to the system','5f8a2fe4-6814-11e8-923f-e9a88dcb533f'),('Edit Concept Proposals','Able to edit concept proposals in the system','5f8a300c-6814-11e8-923f-e9a88dcb533f'),('Edit Conditions','Able to edit conditions','910ec2ec-a87b-4528-ad5c-02c500068bf0'),('Edit Diagnoses','Able to edit diagnoses','107f5169-77a2-48d3-9f69-9f2ca9b419ca'),('Edit Encounters','Able to edit patient encounters','5f8a303e-6814-11e8-923f-e9a88dcb533f'),('Edit Notes','Able to edit patient notes','888fe070-549c-429a-9784-edbaf42e3229'),('Edit Observations','Able to edit patient observations','5f8a3066-6814-11e8-923f-e9a88dcb533f'),('Edit Orders','Able to edit orders','5f8a308e-6814-11e8-923f-e9a88dcb533f'),('Edit Patient Identifiers','Able to edit patient identifiers','5f8a30c0-6814-11e8-923f-e9a88dcb533f'),('Edit Patient Programs','Able to edit patients in programs','5f8a30e8-6814-11e8-923f-e9a88dcb533f'),('Edit Patients','Able to edit patients','5f8a311a-6814-11e8-923f-e9a88dcb533f'),('Edit People','Able to edit person objects','5f8a3142-6814-11e8-923f-e9a88dcb533f'),('Edit Problems','Able to edit problems','5131daf7-2c4d-4c2b-8f6e-bf7cf8290444'),('Edit Relationships','Able to edit relationships','5f8a3174-6814-11e8-923f-e9a88dcb533f'),('Edit Report Objects','Able to edit report objects','5f8a319c-6814-11e8-923f-e9a88dcb533f'),('Edit Reports','Able to edit reports','5f8a31ce-6814-11e8-923f-e9a88dcb533f'),('Edit User Passwords','Able to change the passwords of users in OpenMRS','5f8a31f6-6814-11e8-923f-e9a88dcb533f'),('Edit Users','Able to edit users in OpenMRS','5f8a3228-6814-11e8-923f-e9a88dcb533f'),('Edit Visits','Able to edit visits','14f4da3c-d1e1-4571-b379-0b533496f5b7'),('Form Entry','Allows user to access Form Entry pages/functions','5f8a3250-6814-11e8-923f-e9a88dcb533f'),('Get Allergies','Able to get allergies','d05118c6-2490-4d78-a41a-390e3596a220'),('Get Care Settings','Able to get Care Settings','1ab26030-a207-4a22-be52-b40be3e401dd'),('Get Concept Attribute Types','Able to get concept attribute types','498317ea-e55f-4488-9e87-0db1349c3d11'),('Get Concept Classes','Able to get concept classes','d05118c6-2490-4d78-a41a-390e3596a238'),('Get Concept Datatypes','Able to get concept datatypes','d05118c6-2490-4d78-a41a-390e3596a237'),('Get Concept Map Types','Able to get concept map types','d05118c6-2490-4d78-a41a-390e3596a230'),('Get Concept Proposals','Able to get concept proposals to the system','d05118c6-2490-4d78-a41a-390e3596a250'),('Get Concept Reference Terms','Able to get concept reference terms','d05118c6-2490-4d78-a41a-390e3596a229'),('Get Concept Sources','Able to get concept sources','d05118c6-2490-4d78-a41a-390e3596a231'),('Get Concepts','Able to get concept entries','d05118c6-2490-4d78-a41a-390e3596a251'),('Get Conditions','Able to get conditions','6b470c18-04e8-42c5-bc34-0ac871a0beb6'),('Get Database Changes','Able to get database changes from the admin screen','d05118c6-2490-4d78-a41a-390e3596a222'),('Get Diagnoses','Able to get diagnoses','77b0b402-c928-4811-b084-f0ef7087a49c'),('Get Diagnoses Attribute Types','Able to get diagnoses attribute types','c3d210ba-4676-42fa-a8ff-f1e5a1712525'),('Get Encounter Roles','Able to get encounter roles','d05118c6-2490-4d78-a41a-390e3596a210'),('Get Encounter Types','Able to get encounter types','d05118c6-2490-4d78-a41a-390e3596a247'),('Get Encounters','Able to get patient encounters','d05118c6-2490-4d78-a41a-390e3596a248'),('Get Field Types','Able to get field types','d05118c6-2490-4d78-a41a-390e3596a234'),('Get Forms','Able to get forms','d05118c6-2490-4d78-a41a-390e3596a240'),('Get Global Properties','Able to get global properties on the administration screen','d05118c6-2490-4d78-a41a-390e3596a226'),('Get HL7 Inbound Archive','Able to get an HL7 archive item','d05118c6-2490-4d78-a41a-390e3596a217'),('Get HL7 Inbound Exception','Able to get an HL7 error item','d05118c6-2490-4d78-a41a-390e3596a216'),('Get HL7 Inbound Queue','Able to get an HL7 Queue item','d05118c6-2490-4d78-a41a-390e3596a218'),('Get HL7 Source','Able to get an HL7 Source','d05118c6-2490-4d78-a41a-390e3596a219'),('Get Identifier Types','Able to get patient identifier types','d05118c6-2490-4d78-a41a-390e3596a239'),('Get Location Attribute Types','Able to get location attribute types','d05118c6-2490-4d78-a41a-390e3596a212'),('Get Locations','Able to get locations','d05118c6-2490-4d78-a41a-390e3596a246'),('Get Notes','Able to get patient notes','dcd11774-e77c-48d5-8f34-685741dec359'),('Get Observations','Able to get patient observations','d05118c6-2490-4d78-a41a-390e3596a245'),('Get Order Frequencies','Able to get Order Frequencies','c78007dd-c641-400b-9aac-04420aecc5b6'),('Get Order Set Attribute Types','Able to get order set attribute types','bfc2eca6-fa0a-4700-b5ba-3bbe5a44413d'),('Get Order Sets','Able to get order sets','e52af909-2baf-4ab6-9862-8a6848448ec0'),('Get Order Types','Able to get order types','d05118c6-2490-4d78-a41a-390e3596a233'),('Get Orders','Able to get orders','d05118c6-2490-4d78-a41a-390e3596a241'),('Get Patient Cohorts','Able to get patient cohorts','d05118c6-2490-4d78-a41a-390e3596a242'),('Get Patient Identifiers','Able to get patient identifiers','d05118c6-2490-4d78-a41a-390e3596a243'),('Get Patient Programs','Able to get which programs that patients are in','d05118c6-2490-4d78-a41a-390e3596a227'),('Get Patients','Able to get patients','d05118c6-2490-4d78-a41a-390e3596a244'),('Get People','Able to get person objects','d05118c6-2490-4d78-a41a-390e3596a224'),('Get Person Attribute Types','Able to get person attribute types','d05118c6-2490-4d78-a41a-390e3596a225'),('Get Privileges','Able to get user privileges','d05118c6-2490-4d78-a41a-390e3596a236'),('Get Problems','Able to get problems','d05118c6-2490-4d78-a41a-390e3596a221'),('Get Programs','Able to get patient programs','d05118c6-2490-4d78-a41a-390e3596a228'),('Get Providers','Able to get Providers','d05118c6-2490-4d78-a41a-390e3596a211'),('Get Relationship Types','Able to get relationship types','d05118c6-2490-4d78-a41a-390e3596a232'),('Get Relationships','Able to get relationships','d05118c6-2490-4d78-a41a-390e3596a223'),('Get Roles','Able to get user roles','d05118c6-2490-4d78-a41a-390e3596a235'),('Get Users','Able to get users in OpenMRS','d05118c6-2490-4d78-a41a-390e3596a249'),('Get Visit Attribute Types','Able to get visit attribute types','d05118c6-2490-4d78-a41a-390e3596a213'),('Get Visit Types','Able to get visit types','d05118c6-2490-4d78-a41a-390e3596a215'),('Get Visits','Able to get visits','d05118c6-2490-4d78-a41a-390e3596a214'),('Manage Address Templates','Able to add/edit/delete address templates','fb21b9ee-fd8a-47b6-8656-bd3a68a44925'),('Manage Alerts','Able to add/edit/delete user alerts','5f8a3282-6814-11e8-923f-e9a88dcb533f'),('Manage Concept Attribute Types','Able to add/edit/retire concept attribute types','7550fbcb-cb61-4a9f-94d5-eb376afc727f'),('Manage Concept Classes','Able to add/edit/retire concept classes','5f8a32aa-6814-11e8-923f-e9a88dcb533f'),('Manage Concept Datatypes','Able to add/edit/retire concept datatypes','5f8a32dc-6814-11e8-923f-e9a88dcb533f'),('Manage Concept Map Types','Able to add/edit/retire concept map types','385096c5-6492-41f3-8304-c0144e8fb2e6'),('Manage Concept Name tags','Able to add/edit/delete concept name tags','401cc09a-84c6-4d9e-bf93-4659837edbde'),('Manage Concept Reference Terms','Able to add/edit/retire reference terms','80e45896-d4cd-48a1-b35e-30709bab36b6'),('Manage Concept Sources','Able to add/edit/delete concept sources','5f8a3304-6814-11e8-923f-e9a88dcb533f'),('Manage Concept Stop Words','Able to view/add/remove the concept stop words','47b5b16e-0ff5-4a4a-ae26-947af73c88ca'),('Manage Concepts','Able to add/edit/delete concept entries','5f8a3336-6814-11e8-923f-e9a88dcb533f'),('Manage Encounter Roles','Able to add/edit/retire encounter roles','0fbaddea-b053-4841-a1f1-2ca93fd71d9a'),('Manage Encounter Types','Able to add/edit/delete encounter types','5f8a3368-6814-11e8-923f-e9a88dcb533f'),('Manage Field Types','Able to add/edit/retire field types','5f8a3390-6814-11e8-923f-e9a88dcb533f'),('Manage FormEntry XSN','Allows user to upload and edit the xsns stored on the server','5f8a33b8-6814-11e8-923f-e9a88dcb533f'),('Manage Forms','Able to add/edit/delete forms','5f8a33ea-6814-11e8-923f-e9a88dcb533f'),('Manage Global Properties','Able to add/edit global properties','5f8a341c-6814-11e8-923f-e9a88dcb533f'),('Manage HL7 Messages','Able to add/edit/delete HL7 messages','e43b8830-7c95-42e6-8259-538fec951c66'),('Manage Identifier Types','Able to add/edit/delete patient identifier types','5f8a3444-6814-11e8-923f-e9a88dcb533f'),('Manage Implementation Id','Able to view/add/edit the implementation id for the system','20e102e1-74b4-4198-905e-9b598c8474f1'),('Manage Location Attribute Types','Able to add/edit/retire location attribute types','5c266720-20c2-4fa2-89d3-56886176d63a'),('Manage Location Tags','Able to add/edit/delete location tags','d6d83d7f-9241-42e6-9689-393920a4f733'),('Manage Locations','Able to add/edit/delete locations','5f8a3476-6814-11e8-923f-e9a88dcb533f'),('Manage Modules','Able to add/remove modules to the system','5f8a349e-6814-11e8-923f-e9a88dcb533f'),('Manage Order Frequencies','Able to add/edit/retire Order Frequencies','e3a5205d-ab12-40ca-9160-9ba02198e389'),('Manage Order Set Attribute Types','Able to add/edit/retire order set attribute types','35360a98-eebf-4cc0-812d-80b6025cfea3'),('Manage Order Sets','Able to manage order sets','059955e6-014f-4e50-b198-24e179784025'),('Manage Order Types','Able to add/edit/retire order types','5f8a34c6-6814-11e8-923f-e9a88dcb533f'),('Manage Person Attribute Types','Able to add/edit/delete person attribute types','5f8a34f8-6814-11e8-923f-e9a88dcb533f'),('Manage Privileges','Able to add/edit/delete privileges','5f8a3520-6814-11e8-923f-e9a88dcb533f'),('Manage Programs','Able to add/view/delete patient programs','5f8a3552-6814-11e8-923f-e9a88dcb533f'),('Manage Providers','Able to edit Provider','b0b6fe18-9940-42b4-80fc-b05e6ee546f5'),('Manage Relationship Types','Able to add/edit/retire relationship types','5f8a357a-6814-11e8-923f-e9a88dcb533f'),('Manage Relationships','Able to add/edit/delete relationships','5f8a35ac-6814-11e8-923f-e9a88dcb533f'),('Manage Roles','Able to add/edit/delete user roles','5f8a35d4-6814-11e8-923f-e9a88dcb533f'),('Manage Scheduler','Able to add/edit/remove scheduled tasks','5f8a3606-6814-11e8-923f-e9a88dcb533f'),('Manage Search Index','Able to manage the search index','657bfbbb-a008-43f3-85ac-3163d201b389'),('Manage Visit Attribute Types','Able to add/edit/retire visit attribute types','d0e19ed6-a299-4435-ab8f-2ea76e22fcc4'),('Manage Visit Types','Able to add/edit/delete visit types','f9196849-164f-4522-80d8-488d36faeec2'),('Patient Dashboard - View Demographics Section','Able to view the \'Demographics\' tab on the patient dashboard','5f8a362e-6814-11e8-923f-e9a88dcb533f'),('Patient Dashboard - View Encounters Section','Able to view the \'Encounters\' tab on the patient dashboard','5f8a3660-6814-11e8-923f-e9a88dcb533f'),('Patient Dashboard - View Forms Section','Allows user to view the Forms tab on the patient dashboard','5f8a3692-6814-11e8-923f-e9a88dcb533f'),('Patient Dashboard - View Graphs Section','Able to view the \'Graphs\' tab on the patient dashboard','5f8a36ce-6814-11e8-923f-e9a88dcb533f'),('Patient Dashboard - View Overview Section','Able to view the \'Overview\' tab on the patient dashboard','5f8a3700-6814-11e8-923f-e9a88dcb533f'),('Patient Dashboard - View Patient Summary','Able to view the \'Summary\' tab on the patient dashboard','5f8a3732-6814-11e8-923f-e9a88dcb533f'),('Patient Dashboard - View Regimen Section','Able to view the \'Regimen\' tab on the patient dashboard','5f8a375a-6814-11e8-923f-e9a88dcb533f'),('Patient Overview - View Allergies','Able to view the Allergies portlet on the patient overview tab','d05118c6-2490-4d78-a41a-390e3596a261'),('Patient Overview - View Patient Actions','Able to view the Patient Actions portlet on the patient overview tab','d05118c6-2490-4d78-a41a-390e3596a264'),('Patient Overview - View Problem List','Able to view the Problem List portlet on the patient overview tab','d05118c6-2490-4d78-a41a-390e3596a260'),('Patient Overview - View Programs','Able to view the Programs portlet on the patient overview tab','d05118c6-2490-4d78-a41a-390e3596a263'),('Patient Overview - View Relationships','Able to view the Relationships portlet on the patient overview tab','d05118c6-2490-4d78-a41a-390e3596a262'),('Purge Field Types','Able to purge field types','5f8a3796-6814-11e8-923f-e9a88dcb533f'),('Remove Allergies','Remove allergies','dbdea8f5-9fca-4527-9ef6-5fa03eebf2bd'),('Remove Problems','Remove problems','28ea7e84-e21a-4031-9f9f-2d3eda9d1200'),('Task: Modify Allergies','Able to add, edit, delete allergies','eeb9108e-6905-4712-a13c-b9435db4abcb'),('Update HL7 Inbound Archive','Able to update an HL7 archive item','5dd8306f-eeb4-430e-b5da-a6e02bcafb79'),('Update HL7 Inbound Exception','Able to update an HL7 archive item','e2614f6d-a730-4292-9c10-baf6d912500f'),('Update HL7 Inbound Queue','Able to update an HL7 Queue item','f82b7723-110e-47d3-a35b-4552ea1bff9e'),('Update HL7 Source','Able to update an HL7 Source','6e0abfb7-e95c-4efd-82a0-00a02b0e8335'),('Upload XSN','Allows user to upload/overwrite the XSNs defined for forms','5f8a37c8-6814-11e8-923f-e9a88dcb533f'),('View Administration Functions','Able to view the \'Administration\' link in the navigation bar','5f8a37fa-6814-11e8-923f-e9a88dcb533f'),('View Allergies','Able to view allergies in OpenMRS','5f8a382c-6814-11e8-923f-e9a88dcb533f'),('View Concept Classes','Able to view concept classes','5f8a3958-6814-11e8-923f-e9a88dcb533f'),('View Concept Datatypes','Able to view concept datatypes','5f8a398a-6814-11e8-923f-e9a88dcb533f'),('View Concept Proposals','Able to view concept proposals to the system','5f8a39bc-6814-11e8-923f-e9a88dcb533f'),('View Concept Sources','Able to view concept sources','5f8a39ee-6814-11e8-923f-e9a88dcb533f'),('View Concepts','Able to view concept entries','5f8a3a16-6814-11e8-923f-e9a88dcb533f'),('View Data Entry Statistics','Able to view data entry statistics from the admin screen','5f8a3a48-6814-11e8-923f-e9a88dcb533f'),('View Encounter Types','Able to view encounter types','5f8a3a7a-6814-11e8-923f-e9a88dcb533f'),('View Encounters','Able to view patient encounters','5f8a3aa2-6814-11e8-923f-e9a88dcb533f'),('View Field Types','Able to view field types','5f8a3ad4-6814-11e8-923f-e9a88dcb533f'),('View Forms','Able to view forms','5f8a3afc-6814-11e8-923f-e9a88dcb533f'),('View Global Properties','Able to view global properties on the administration screen','5f8a3b2e-6814-11e8-923f-e9a88dcb533f'),('View Identifier Types','Able to view patient identifier types','5f8a3b60-6814-11e8-923f-e9a88dcb533f'),('View Locations','Able to view locations','5f8a3b88-6814-11e8-923f-e9a88dcb533f'),('View Navigation Menu','Ability to see the navigation menu','5f8a3bba-6814-11e8-923f-e9a88dcb533f'),('View Observations','Able to view patient observations','5f8a3be2-6814-11e8-923f-e9a88dcb533f'),('View Order Types','Able to view order types','5f8a3c14-6814-11e8-923f-e9a88dcb533f'),('View Orders','Able to view orders','5f8a3c46-6814-11e8-923f-e9a88dcb533f'),('View Patient Cohorts','Able to view patient cohorts','5f8a3c6e-6814-11e8-923f-e9a88dcb533f'),('View Patient Identifiers','Able to view patient identifiers','5f8a3ca0-6814-11e8-923f-e9a88dcb533f'),('View Patient Programs','Able to see which programs that patients are in','5f8a3cd2-6814-11e8-923f-e9a88dcb533f'),('View Patients','Able to view patients','5f8a3cfa-6814-11e8-923f-e9a88dcb533f'),('View People','Able to view person objects','5f8a3d2c-6814-11e8-923f-e9a88dcb533f'),('View Person Attribute Types','Able to view person attribute types','5f8a3d5e-6814-11e8-923f-e9a88dcb533f'),('View Privileges','Able to view user privileges','5f8a3d86-6814-11e8-923f-e9a88dcb533f'),('View Problems','Able to view problems in OpenMRS','5f8a3db8-6814-11e8-923f-e9a88dcb533f'),('View Programs','Able to view patient programs','5f8a3de0-6814-11e8-923f-e9a88dcb533f'),('View Relationship Types','Able to view relationship types','5f8a3e12-6814-11e8-923f-e9a88dcb533f'),('View Relationships','Able to view relationships','5f8a3e44-6814-11e8-923f-e9a88dcb533f'),('View Report Objects','Able to view report objects','5f8a3e6c-6814-11e8-923f-e9a88dcb533f'),('View Reports','Able to view reports','5f8a3e9e-6814-11e8-923f-e9a88dcb533f'),('View Roles','Able to view user roles','5f8a3ec6-6814-11e8-923f-e9a88dcb533f'),('View Unpublished Forms','Able to view and fill out unpublished forms','5f8a3ef8-6814-11e8-923f-e9a88dcb533f'),('View Users','Able to view users in OpenMRS','5f8a3f2a-6814-11e8-923f-e9a88dcb533f');
/*!40000 ALTER TABLE `privilege` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `program`
--

DROP TABLE IF EXISTS `program`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `program` (
						   `program_id` int(11) NOT NULL AUTO_INCREMENT,
						   `concept_id` int(11) NOT NULL DEFAULT 0,
						   `outcomes_concept_id` int(11) DEFAULT NULL,
						   `creator` int(11) NOT NULL DEFAULT 0,
						   `date_created` datetime NOT NULL,
						   `changed_by` int(11) DEFAULT NULL,
						   `date_changed` datetime DEFAULT NULL,
						   `retired` tinyint(1) NOT NULL DEFAULT 0,
						   `name` varchar(50) NOT NULL,
						   `description` text DEFAULT NULL,
						   `uuid` char(38) NOT NULL,
						   PRIMARY KEY (`program_id`),
						   UNIQUE KEY `uuid_program` (`uuid`),
						   KEY `program_concept` (`concept_id`),
						   KEY `program_creator` (`creator`),
						   KEY `program_outcomes_concept_id_fk` (`outcomes_concept_id`),
						   KEY `user_who_changed_program` (`changed_by`),
						   CONSTRAINT `program_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
						   CONSTRAINT `program_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
						   CONSTRAINT `program_outcomes_concept_id_fk` FOREIGN KEY (`outcomes_concept_id`) REFERENCES `concept` (`concept_id`),
						   CONSTRAINT `user_who_changed_program` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `program`
--

LOCK TABLES `program` WRITE;
/*!40000 ALTER TABLE `program` DISABLE KEYS */;
/*!40000 ALTER TABLE `program` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `program_attribute_type`
--

DROP TABLE IF EXISTS `program_attribute_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `program_attribute_type` (
										  `program_attribute_type_id` int(11) NOT NULL AUTO_INCREMENT,
										  `name` varchar(255) NOT NULL,
										  `description` varchar(1024) DEFAULT NULL,
										  `datatype` varchar(255) DEFAULT NULL,
										  `datatype_config` text DEFAULT NULL,
										  `preferred_handler` varchar(255) DEFAULT NULL,
										  `handler_config` text DEFAULT NULL,
										  `min_occurs` int(11) NOT NULL,
										  `max_occurs` int(11) DEFAULT NULL,
										  `creator` int(11) NOT NULL,
										  `date_created` datetime NOT NULL,
										  `changed_by` int(11) DEFAULT NULL,
										  `date_changed` datetime DEFAULT NULL,
										  `retired` tinyint(1) NOT NULL DEFAULT 0,
										  `retired_by` int(11) DEFAULT NULL,
										  `date_retired` datetime DEFAULT NULL,
										  `retire_reason` varchar(255) DEFAULT NULL,
										  `uuid` char(38) NOT NULL,
										  PRIMARY KEY (`program_attribute_type_id`),
										  UNIQUE KEY `program_attribute_type_name` (`name`),
										  UNIQUE KEY `uuid_program_attribute_type` (`uuid`),
										  KEY `program_attribute_type_changed_by_fk` (`changed_by`),
										  KEY `program_attribute_type_creator_fk` (`creator`),
										  KEY `program_attribute_type_retired_by_fk` (`retired_by`),
										  CONSTRAINT `program_attribute_type_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
										  CONSTRAINT `program_attribute_type_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
										  CONSTRAINT `program_attribute_type_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `program_attribute_type`
--

LOCK TABLES `program_attribute_type` WRITE;
/*!40000 ALTER TABLE `program_attribute_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `program_attribute_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `program_workflow`
--

DROP TABLE IF EXISTS `program_workflow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `program_workflow` (
									`program_workflow_id` int(11) NOT NULL AUTO_INCREMENT,
									`program_id` int(11) NOT NULL DEFAULT 0,
									`concept_id` int(11) NOT NULL DEFAULT 0,
									`creator` int(11) NOT NULL DEFAULT 0,
									`date_created` datetime NOT NULL,
									`retired` tinyint(1) NOT NULL DEFAULT 0,
									`changed_by` int(11) DEFAULT NULL,
									`date_changed` datetime DEFAULT NULL,
									`uuid` char(38) NOT NULL,
									PRIMARY KEY (`program_workflow_id`),
									UNIQUE KEY `uuid_program_workflow` (`uuid`),
									KEY `program_for_workflow` (`program_id`),
									KEY `workflow_changed_by` (`changed_by`),
									KEY `workflow_concept` (`concept_id`),
									KEY `workflow_creator` (`creator`),
									CONSTRAINT `program_for_workflow` FOREIGN KEY (`program_id`) REFERENCES `program` (`program_id`),
									CONSTRAINT `workflow_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									CONSTRAINT `workflow_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
									CONSTRAINT `workflow_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `program_workflow`
--

LOCK TABLES `program_workflow` WRITE;
/*!40000 ALTER TABLE `program_workflow` DISABLE KEYS */;
/*!40000 ALTER TABLE `program_workflow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `program_workflow_state`
--

DROP TABLE IF EXISTS `program_workflow_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `program_workflow_state` (
										  `program_workflow_state_id` int(11) NOT NULL AUTO_INCREMENT,
										  `program_workflow_id` int(11) NOT NULL DEFAULT 0,
										  `concept_id` int(11) NOT NULL DEFAULT 0,
										  `initial` tinyint(1) NOT NULL DEFAULT 0,
										  `terminal` tinyint(1) NOT NULL DEFAULT 0,
										  `creator` int(11) NOT NULL DEFAULT 0,
										  `date_created` datetime NOT NULL,
										  `retired` tinyint(1) NOT NULL DEFAULT 0,
										  `changed_by` int(11) DEFAULT NULL,
										  `date_changed` datetime DEFAULT NULL,
										  `uuid` char(38) NOT NULL,
										  PRIMARY KEY (`program_workflow_state_id`),
										  UNIQUE KEY `uuid_program_workflow_state` (`uuid`),
										  KEY `state_changed_by` (`changed_by`),
										  KEY `state_concept` (`concept_id`),
										  KEY `state_creator` (`creator`),
										  KEY `workflow_for_state` (`program_workflow_id`),
										  CONSTRAINT `state_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
										  CONSTRAINT `state_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
										  CONSTRAINT `state_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
										  CONSTRAINT `workflow_for_state` FOREIGN KEY (`program_workflow_id`) REFERENCES `program_workflow` (`program_workflow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `program_workflow_state`
--

LOCK TABLES `program_workflow_state` WRITE;
/*!40000 ALTER TABLE `program_workflow_state` DISABLE KEYS */;
/*!40000 ALTER TABLE `program_workflow_state` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `provider`
--

DROP TABLE IF EXISTS `provider`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `provider` (
							`provider_id` int(11) NOT NULL AUTO_INCREMENT,
							`person_id` int(11) DEFAULT NULL,
							`name` varchar(255) DEFAULT NULL,
							`identifier` varchar(255) DEFAULT NULL,
							`creator` int(11) NOT NULL,
							`date_created` datetime NOT NULL,
							`changed_by` int(11) DEFAULT NULL,
							`date_changed` datetime DEFAULT NULL,
							`retired` tinyint(1) NOT NULL DEFAULT 0,
							`retired_by` int(11) DEFAULT NULL,
							`date_retired` datetime DEFAULT NULL,
							`retire_reason` varchar(255) DEFAULT NULL,
							`uuid` char(38) NOT NULL,
							`role_id` int(11) DEFAULT NULL,
							`speciality_id` int(11) DEFAULT NULL,
							PRIMARY KEY (`provider_id`),
							UNIQUE KEY `uuid_provider` (`uuid`),
							KEY `provider_changed_by_fk` (`changed_by`),
							KEY `provider_creator_fk` (`creator`),
							KEY `provider_person_id_fk` (`person_id`),
							KEY `provider_retired_by_fk` (`retired_by`),
							KEY `provider_role_id_fk` (`role_id`),
							KEY `provider_speciality_id_fk` (`speciality_id`),
							CONSTRAINT `provider_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
							CONSTRAINT `provider_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
							CONSTRAINT `provider_person_id_fk` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`),
							CONSTRAINT `provider_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`),
							CONSTRAINT `provider_role_id_fk` FOREIGN KEY (`role_id`) REFERENCES `concept` (`concept_id`),
							CONSTRAINT `provider_speciality_id_fk` FOREIGN KEY (`speciality_id`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `provider`
--

LOCK TABLES `provider` WRITE;
/*!40000 ALTER TABLE `provider` DISABLE KEYS */;
/*!40000 ALTER TABLE `provider` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `provider_attribute`
--

DROP TABLE IF EXISTS `provider_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `provider_attribute` (
									  `provider_attribute_id` int(11) NOT NULL AUTO_INCREMENT,
									  `provider_id` int(11) NOT NULL,
									  `attribute_type_id` int(11) NOT NULL,
									  `value_reference` text NOT NULL,
									  `uuid` char(38) NOT NULL,
									  `creator` int(11) NOT NULL,
									  `date_created` datetime NOT NULL,
									  `changed_by` int(11) DEFAULT NULL,
									  `date_changed` datetime DEFAULT NULL,
									  `voided` tinyint(1) NOT NULL DEFAULT 0,
									  `voided_by` int(11) DEFAULT NULL,
									  `date_voided` datetime DEFAULT NULL,
									  `void_reason` varchar(255) DEFAULT NULL,
									  PRIMARY KEY (`provider_attribute_id`),
									  UNIQUE KEY `uuid_provider_attribute` (`uuid`),
									  KEY `provider_attribute_attribute_type_id_fk` (`attribute_type_id`),
									  KEY `provider_attribute_changed_by_fk` (`changed_by`),
									  KEY `provider_attribute_creator_fk` (`creator`),
									  KEY `provider_attribute_provider_fk` (`provider_id`),
									  KEY `provider_attribute_voided_by_fk` (`voided_by`),
									  CONSTRAINT `provider_attribute_attribute_type_id_fk` FOREIGN KEY (`attribute_type_id`) REFERENCES `provider_attribute_type` (`provider_attribute_type_id`),
									  CONSTRAINT `provider_attribute_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									  CONSTRAINT `provider_attribute_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									  CONSTRAINT `provider_attribute_provider_fk` FOREIGN KEY (`provider_id`) REFERENCES `provider` (`provider_id`),
									  CONSTRAINT `provider_attribute_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `provider_attribute`
--

LOCK TABLES `provider_attribute` WRITE;
/*!40000 ALTER TABLE `provider_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `provider_attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `provider_attribute_type`
--

DROP TABLE IF EXISTS `provider_attribute_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `provider_attribute_type` (
										   `provider_attribute_type_id` int(11) NOT NULL AUTO_INCREMENT,
										   `name` varchar(255) NOT NULL,
										   `description` varchar(1024) DEFAULT NULL,
										   `datatype` varchar(255) DEFAULT NULL,
										   `datatype_config` text DEFAULT NULL,
										   `preferred_handler` varchar(255) DEFAULT NULL,
										   `handler_config` text DEFAULT NULL,
										   `min_occurs` int(11) NOT NULL,
										   `max_occurs` int(11) DEFAULT NULL,
										   `creator` int(11) NOT NULL,
										   `date_created` datetime NOT NULL,
										   `changed_by` int(11) DEFAULT NULL,
										   `date_changed` datetime DEFAULT NULL,
										   `retired` tinyint(1) NOT NULL DEFAULT 0,
										   `retired_by` int(11) DEFAULT NULL,
										   `date_retired` datetime DEFAULT NULL,
										   `retire_reason` varchar(255) DEFAULT NULL,
										   `uuid` char(38) NOT NULL,
										   PRIMARY KEY (`provider_attribute_type_id`),
										   UNIQUE KEY `uuid_provider_attribute_type` (`uuid`),
										   KEY `provider_attribute_type_changed_by_fk` (`changed_by`),
										   KEY `provider_attribute_type_creator_fk` (`creator`),
										   KEY `provider_attribute_type_retired_by_fk` (`retired_by`),
										   CONSTRAINT `provider_attribute_type_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
										   CONSTRAINT `provider_attribute_type_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
										   CONSTRAINT `provider_attribute_type_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `provider_attribute_type`
--

LOCK TABLES `provider_attribute_type` WRITE;
/*!40000 ALTER TABLE `provider_attribute_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `provider_attribute_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `referral_order`
--

DROP TABLE IF EXISTS `referral_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `referral_order` (
								  `order_id` int(11) NOT NULL AUTO_INCREMENT,
								  `specimen_source` int(11) DEFAULT NULL,
								  `laterality` varchar(20) DEFAULT NULL,
								  `clinical_history` text DEFAULT NULL,
								  `frequency` int(11) DEFAULT NULL,
								  `number_of_repeats` int(11) DEFAULT NULL,
								  `location` int(11) DEFAULT NULL,
								  PRIMARY KEY (`order_id`),
								  KEY `referral_order_location_fk` (`location`),
								  KEY `referral_order_frequency_index` (`frequency`),
								  KEY `referral_order_specimen_source_index` (`specimen_source`),
								  CONSTRAINT `referral_order_frequency_fk` FOREIGN KEY (`frequency`) REFERENCES `order_frequency` (`order_frequency_id`),
								  CONSTRAINT `referral_order_location_fk` FOREIGN KEY (`location`) REFERENCES `concept` (`concept_id`),
								  CONSTRAINT `referral_order_order_id_fk` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
								  CONSTRAINT `referral_order_specimen_source_fk` FOREIGN KEY (`specimen_source`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `referral_order`
--

LOCK TABLES `referral_order` WRITE;
/*!40000 ALTER TABLE `referral_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `referral_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `relationship`
--

DROP TABLE IF EXISTS `relationship`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `relationship` (
								`relationship_id` int(11) NOT NULL AUTO_INCREMENT,
								`person_a` int(11) NOT NULL,
								`relationship` int(11) NOT NULL DEFAULT 0,
								`person_b` int(11) NOT NULL,
								`start_date` datetime DEFAULT NULL,
								`end_date` datetime DEFAULT NULL,
								`creator` int(11) NOT NULL DEFAULT 0,
								`date_created` datetime NOT NULL,
								`date_changed` datetime DEFAULT NULL,
								`changed_by` int(11) DEFAULT NULL,
								`voided` tinyint(1) NOT NULL DEFAULT 0,
								`voided_by` int(11) DEFAULT NULL,
								`date_voided` datetime DEFAULT NULL,
								`void_reason` varchar(255) DEFAULT NULL,
								`uuid` char(38) NOT NULL,
								PRIMARY KEY (`relationship_id`),
								UNIQUE KEY `uuid_relationship` (`uuid`),
								KEY `person_a_is_person` (`person_a`),
								KEY `person_b_is_person` (`person_b`),
								KEY `relation_creator` (`creator`),
								KEY `relation_voider` (`voided_by`),
								KEY `relationship_changed_by` (`changed_by`),
								KEY `relationship_type_id` (`relationship`),
								CONSTRAINT `person_a_is_person` FOREIGN KEY (`person_a`) REFERENCES `person` (`person_id`),
								CONSTRAINT `person_b_is_person` FOREIGN KEY (`person_b`) REFERENCES `person` (`person_id`),
								CONSTRAINT `relation_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
								CONSTRAINT `relation_voider` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
								CONSTRAINT `relationship_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
								CONSTRAINT `relationship_type_id` FOREIGN KEY (`relationship`) REFERENCES `relationship_type` (`relationship_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `relationship`
--

LOCK TABLES `relationship` WRITE;
/*!40000 ALTER TABLE `relationship` DISABLE KEYS */;
/*!40000 ALTER TABLE `relationship` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `relationship_type`
--

DROP TABLE IF EXISTS `relationship_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `relationship_type` (
									 `relationship_type_id` int(11) NOT NULL AUTO_INCREMENT,
									 `a_is_to_b` varchar(50) NOT NULL,
									 `b_is_to_a` varchar(50) NOT NULL,
									 `preferred` tinyint(1) NOT NULL DEFAULT 0,
									 `weight` int(11) NOT NULL DEFAULT 0,
									 `description` varchar(255) DEFAULT NULL,
									 `creator` int(11) NOT NULL DEFAULT 0,
									 `date_created` datetime NOT NULL,
									 `retired` tinyint(1) NOT NULL DEFAULT 0,
									 `retired_by` int(11) DEFAULT NULL,
									 `date_retired` datetime DEFAULT NULL,
									 `retire_reason` varchar(255) DEFAULT NULL,
									 `uuid` char(38) NOT NULL,
									 `date_changed` datetime DEFAULT NULL,
									 `changed_by` int(11) DEFAULT NULL,
									 PRIMARY KEY (`relationship_type_id`),
									 UNIQUE KEY `uuid_relationship_type` (`uuid`),
									 KEY `relationship_type_changed_by` (`changed_by`),
									 KEY `user_who_created_rel` (`creator`),
									 KEY `user_who_retired_relationship_type` (`retired_by`),
									 CONSTRAINT `relationship_type_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
									 CONSTRAINT `user_who_created_rel` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
									 CONSTRAINT `user_who_retired_relationship_type` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `relationship_type`
--

LOCK TABLES `relationship_type` WRITE;
/*!40000 ALTER TABLE `relationship_type` DISABLE KEYS */;
INSERT INTO `relationship_type` VALUES (1,'Doctor','Patient',0,0,'Relationship from a primary care provider to the patient',1,'2007-05-04 00:00:00',0,NULL,NULL,NULL,'8d919b58-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(2,'Sibling','Sibling',0,0,'Relationship between brother/sister, brother/brother, and sister/sister',1,'2007-05-04 00:00:00',0,NULL,NULL,NULL,'8d91a01c-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(3,'Parent','Child',0,0,'Relationship from a mother/father to the child',1,'2007-05-04 00:00:00',0,NULL,NULL,NULL,'8d91a210-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL),(4,'Aunt/Uncle','Niece/Nephew',0,0,'Relationship from a parent\'s sibling to a child of that parent',1,'2007-05-04 00:00:00',0,NULL,NULL,NULL,'8d91a3dc-c2cc-11de-8d13-0010c6dffd0f',NULL,NULL);
/*!40000 ALTER TABLE `relationship_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report_object`
--

DROP TABLE IF EXISTS `report_object`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `report_object` (
  `report_object_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `report_object_type` varchar(255) NOT NULL,
  `report_object_sub_type` varchar(255) NOT NULL,
  `xml_data` text DEFAULT NULL,
  `creator` int(11) NOT NULL,
  `date_created` datetime NOT NULL,
  `changed_by` int(11) DEFAULT NULL,
  `date_changed` datetime DEFAULT NULL,
  `voided` tinyint(1) NOT NULL DEFAULT 0,
  `voided_by` int(11) DEFAULT NULL,
  `date_voided` datetime DEFAULT NULL,
  `void_reason` varchar(255) DEFAULT NULL,
  `uuid` char(38) NOT NULL,
  PRIMARY KEY (`report_object_id`),
  UNIQUE KEY `uuid_report_object` (`uuid`),
  KEY `report_object_creator` (`creator`),
  KEY `user_who_changed_report_object` (`changed_by`),
  KEY `user_who_voided_report_object` (`voided_by`),
  CONSTRAINT `report_object_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_changed_report_object` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_voided_report_object` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report_object`
--

LOCK TABLES `report_object` WRITE;
/*!40000 ALTER TABLE `report_object` DISABLE KEYS */;
/*!40000 ALTER TABLE `report_object` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report_schema_xml`
--

DROP TABLE IF EXISTS `report_schema_xml`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `report_schema_xml` (
  `report_schema_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` text NOT NULL,
  `xml_data` text NOT NULL,
  `uuid` char(38) NOT NULL,
  PRIMARY KEY (`report_schema_id`),
  UNIQUE KEY `uuid_report_schema_xml` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report_schema_xml`
--

LOCK TABLES `report_schema_xml` WRITE;
/*!40000 ALTER TABLE `report_schema_xml` DISABLE KEYS */;
/*!40000 ALTER TABLE `report_schema_xml` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role` (
  `role` varchar(50) NOT NULL DEFAULT '',
  `description` varchar(255) DEFAULT NULL,
  `uuid` char(38) NOT NULL,
  PRIMARY KEY (`role`),
  UNIQUE KEY `uuid_role` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES ('Anonymous','Privileges for non-authenticated users.','774b2af3-6437-4e5a-a310-547554c7c65c'),('Authenticated','Privileges gained once authentication has been established.','f7fd42ef-880e-40c5-972d-e4ae7c990de2'),('Provider','All users with the \'Provider\' role will appear as options in the default Infopath ','8d94f280-c2cc-11de-8d13-0010c6dffd0f'),('System Developer','Developers of the OpenMRS .. have additional access to change fundamental structure of the database model.','8d94f852-c2cc-11de-8d13-0010c6dffd0f');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_privilege`
--

DROP TABLE IF EXISTS `role_privilege`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role_privilege` (
  `role` varchar(50) NOT NULL DEFAULT '',
  `privilege` varchar(255) NOT NULL,
  PRIMARY KEY (`role`,`privilege`),
  KEY `privilege_definitions` (`privilege`),
  KEY `role_privilege_to_role` (`role`),
  CONSTRAINT `privilege_definitions` FOREIGN KEY (`privilege`) REFERENCES `privilege` (`privilege`),
  CONSTRAINT `role_privilege_to_role` FOREIGN KEY (`role`) REFERENCES `role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_privilege`
--

LOCK TABLES `role_privilege` WRITE;
/*!40000 ALTER TABLE `role_privilege` DISABLE KEYS */;
INSERT INTO `role_privilege` VALUES ('Authenticated','Get Concept Classes'),('Authenticated','Get Concept Datatypes'),('Authenticated','Get Encounter Types'),('Authenticated','Get Field Types'),('Authenticated','Get Global Properties'),('Authenticated','Get Identifier Types'),('Authenticated','Get Locations'),('Authenticated','Get Order Types'),('Authenticated','Get Person Attribute Types'),('Authenticated','Get Privileges'),('Authenticated','Get Relationship Types'),('Authenticated','Get Relationships'),('Authenticated','Get Roles'),('Authenticated','Patient Overview - View Relationships'),('Authenticated','View Concept Classes'),('Authenticated','View Concept Datatypes'),('Authenticated','View Encounter Types'),('Authenticated','View Field Types'),('Authenticated','View Global Properties'),('Authenticated','View Identifier Types'),('Authenticated','View Locations'),('Authenticated','View Order Types'),('Authenticated','View Person Attribute Types'),('Authenticated','View Privileges'),('Authenticated','View Relationship Types'),('Authenticated','View Relationships'),('Authenticated','View Roles');
/*!40000 ALTER TABLE `role_privilege` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_role`
--

DROP TABLE IF EXISTS `role_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role_role` (
  `parent_role` varchar(50) NOT NULL DEFAULT '',
  `child_role` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`parent_role`,`child_role`),
  KEY `inherited_role` (`child_role`),
  CONSTRAINT `inherited_role` FOREIGN KEY (`child_role`) REFERENCES `role` (`role`),
  CONSTRAINT `parent_role` FOREIGN KEY (`parent_role`) REFERENCES `role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_role`
--

LOCK TABLES `role_role` WRITE;
/*!40000 ALTER TABLE `role_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `role_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scheduler_task_config`
--

DROP TABLE IF EXISTS `scheduler_task_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `scheduler_task_config` (
  `task_config_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(1024) DEFAULT NULL,
  `schedulable_class` text DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `start_time_pattern` varchar(50) DEFAULT NULL,
  `repeat_interval` int(11) NOT NULL DEFAULT 0,
  `start_on_startup` tinyint(1) NOT NULL DEFAULT 0,
  `started` tinyint(1) NOT NULL DEFAULT 0,
  `created_by` int(11) DEFAULT 0,
  `date_created` datetime DEFAULT NULL,
  `changed_by` int(11) DEFAULT NULL,
  `date_changed` datetime DEFAULT NULL,
  `last_execution_time` datetime DEFAULT NULL,
  `uuid` char(38) NOT NULL,
  PRIMARY KEY (`task_config_id`),
  UNIQUE KEY `uuid_scheduler_task_config` (`uuid`),
  KEY `scheduler_changer` (`changed_by`),
  KEY `scheduler_creator` (`created_by`),
  CONSTRAINT `scheduler_changer` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `scheduler_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scheduler_task_config`
--

LOCK TABLES `scheduler_task_config` WRITE;
/*!40000 ALTER TABLE `scheduler_task_config` DISABLE KEYS */;
INSERT INTO `scheduler_task_config` VALUES (2,'Auto Close Visits Task','Stops all active visits that match the visit type(s) specified by the value of the global property \'visits.autoCloseVisitType\'','org.openmrs.scheduler.tasks.AutoCloseVisitsTask','2011-11-28 23:59:59','MM/dd/yyyy HH:mm:ss',86400,0,0,1,'2018-06-04 18:30:16',NULL,NULL,NULL,'8c17b376-1a2b-11e1-a51a-00248140a5eb');
/*!40000 ALTER TABLE `scheduler_task_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scheduler_task_config_property`
--

DROP TABLE IF EXISTS `scheduler_task_config_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `scheduler_task_config_property` (
  `task_config_property_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `value` text DEFAULT NULL,
  `task_config_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`task_config_property_id`),
  KEY `task_config_for_property` (`task_config_id`),
  CONSTRAINT `task_config_for_property` FOREIGN KEY (`task_config_id`) REFERENCES `scheduler_task_config` (`task_config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scheduler_task_config_property`
--

LOCK TABLES `scheduler_task_config_property` WRITE;
/*!40000 ALTER TABLE `scheduler_task_config_property` DISABLE KEYS */;
/*!40000 ALTER TABLE `scheduler_task_config_property` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `serialized_object`
--

DROP TABLE IF EXISTS `serialized_object`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `serialized_object` (
  `serialized_object_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(5000) DEFAULT NULL,
  `type` varchar(255) NOT NULL,
  `subtype` varchar(255) NOT NULL,
  `serialization_class` varchar(255) NOT NULL,
  `serialized_data` mediumtext NOT NULL,
  `date_created` datetime NOT NULL,
  `creator` int(11) NOT NULL,
  `date_changed` datetime DEFAULT NULL,
  `changed_by` int(11) DEFAULT NULL,
  `retired` tinyint(1) NOT NULL DEFAULT 0,
  `date_retired` datetime DEFAULT NULL,
  `retired_by` int(11) DEFAULT NULL,
  `retire_reason` varchar(1000) DEFAULT NULL,
  `uuid` char(38) NOT NULL,
  PRIMARY KEY (`serialized_object_id`),
  UNIQUE KEY `uuid_serialized_object` (`uuid`),
  KEY `serialized_object_changed_by` (`changed_by`),
  KEY `serialized_object_creator` (`creator`),
  KEY `serialized_object_retired_by` (`retired_by`),
  CONSTRAINT `serialized_object_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `serialized_object_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `serialized_object_retired_by` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `serialized_object`
--

LOCK TABLES `serialized_object` WRITE;
/*!40000 ALTER TABLE `serialized_object` DISABLE KEYS */;
/*!40000 ALTER TABLE `serialized_object` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_order`
--

DROP TABLE IF EXISTS `test_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_order` (
  `order_id` int(11) NOT NULL DEFAULT 0,
  `specimen_source` int(11) DEFAULT NULL,
  `laterality` varchar(20) DEFAULT NULL,
  `clinical_history` text DEFAULT NULL,
  `frequency` int(11) DEFAULT NULL,
  `number_of_repeats` int(11) DEFAULT NULL,
  `location` int(11) DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  KEY `test_order_frequency_fk` (`frequency`),
  KEY `test_order_specimen_source_fk` (`specimen_source`),
  KEY `test_order_location_fk` (`location`),
  CONSTRAINT `test_order_frequency_fk` FOREIGN KEY (`frequency`) REFERENCES `order_frequency` (`order_frequency_id`),
  CONSTRAINT `test_order_location_fk` FOREIGN KEY (`location`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `test_order_order_id_fk` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `test_order_specimen_source_fk` FOREIGN KEY (`specimen_source`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_order`
--

LOCK TABLES `test_order` WRITE;
/*!40000 ALTER TABLE `test_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `test_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_property`
--

DROP TABLE IF EXISTS `user_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_property` (
  `user_id` int(11) NOT NULL DEFAULT 0,
  `property` varchar(255) NOT NULL,
  `property_value` longtext DEFAULT NULL,
  PRIMARY KEY (`user_id`,`property`),
  CONSTRAINT `user_property_to_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_property`
--

LOCK TABLES `user_property` WRITE;
/*!40000 ALTER TABLE `user_property` DISABLE KEYS */;
INSERT INTO `user_property` VALUES (1,'defaultLocale','en'),(1,'lockoutTimestamp',''),(1,'loginAttempts','0');
/*!40000 ALTER TABLE `user_property` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_role` (
  `user_id` int(11) NOT NULL DEFAULT 0,
  `role` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`user_id`,`role`),
  KEY `role_definitions` (`role`),
  KEY `user_role_to_users` (`user_id`),
  CONSTRAINT `role_definitions` FOREIGN KEY (`role`) REFERENCES `role` (`role`),
  CONSTRAINT `user_role_to_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,'Provider'),(1,'System Developer');
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `system_id` varchar(50) NOT NULL DEFAULT '',
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(128) DEFAULT NULL,
  `salt` varchar(128) DEFAULT NULL,
  `secret_question` varchar(255) DEFAULT NULL,
  `secret_answer` varchar(255) DEFAULT NULL,
  `creator` int(11) NOT NULL DEFAULT 0,
  `date_created` datetime NOT NULL,
  `changed_by` int(11) DEFAULT NULL,
  `date_changed` datetime DEFAULT NULL,
  `person_id` int(11) NOT NULL,
  `retired` tinyint(1) NOT NULL DEFAULT 0,
  `retired_by` int(11) DEFAULT NULL,
  `date_retired` datetime DEFAULT NULL,
  `retire_reason` varchar(255) DEFAULT NULL,
  `uuid` char(38) NOT NULL,
  `activation_key` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email` (`email`),
  KEY `person_id_for_user` (`person_id`),
  KEY `user_creator` (`creator`),
  KEY `user_who_changed_user` (`changed_by`),
  KEY `user_who_retired_this_user` (`retired_by`),
  CONSTRAINT `person_id_for_user` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`),
  CONSTRAINT `user_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_changed_user` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `user_who_retired_this_user` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','','6f0be51d599f59dd1269e12e17949f8ecb9ac963e467ac1400cf0a02eb9f8861ce3cca8f6d34d93c0ca34029497542cbadda20c949affb4cb59269ef4912087b','c788c6ad82a157b712392ca695dfcf2eed193d7f',NULL,NULL,1,'2005-01-01 00:00:00',1,'2022-11-15 11:49:15',1,0,NULL,NULL,NULL,'82f18b44-6814-11e8-923f-e9a88dcb533f',NULL,NULL),(2,'daemon','daemon',NULL,NULL,NULL,NULL,1,'2010-04-26 13:25:00',NULL,NULL,1,0,NULL,NULL,NULL,'A4F30A1B-5EB9-11DF-A648-37A07F9C90FB',NULL,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visit`
--

DROP TABLE IF EXISTS `visit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `visit` (
  `visit_id` int(11) NOT NULL AUTO_INCREMENT,
  `patient_id` int(11) NOT NULL,
  `visit_type_id` int(11) NOT NULL,
  `date_started` datetime NOT NULL,
  `date_stopped` datetime DEFAULT NULL,
  `indication_concept_id` int(11) DEFAULT NULL,
  `location_id` int(11) DEFAULT NULL,
  `creator` int(11) NOT NULL,
  `date_created` datetime NOT NULL,
  `changed_by` int(11) DEFAULT NULL,
  `date_changed` datetime DEFAULT NULL,
  `voided` tinyint(1) NOT NULL DEFAULT 0,
  `voided_by` int(11) DEFAULT NULL,
  `date_voided` datetime DEFAULT NULL,
  `void_reason` varchar(255) DEFAULT NULL,
  `uuid` char(38) NOT NULL,
  PRIMARY KEY (`visit_id`),
  UNIQUE KEY `uuid_visit` (`uuid`),
  KEY `visit_changed_by_fk` (`changed_by`),
  KEY `visit_creator_fk` (`creator`),
  KEY `visit_indication_concept_fk` (`indication_concept_id`),
  KEY `visit_location_fk` (`location_id`),
  KEY `visit_patient_index` (`patient_id`),
  KEY `visit_type_fk` (`visit_type_id`),
  KEY `visit_voided_by_fk` (`voided_by`),
  CONSTRAINT `visit_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `visit_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `visit_indication_concept_fk` FOREIGN KEY (`indication_concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `visit_location_fk` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`),
  CONSTRAINT `visit_patient_fk` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `visit_type_fk` FOREIGN KEY (`visit_type_id`) REFERENCES `visit_type` (`visit_type_id`),
  CONSTRAINT `visit_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visit`
--

LOCK TABLES `visit` WRITE;
/*!40000 ALTER TABLE `visit` DISABLE KEYS */;
/*!40000 ALTER TABLE `visit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visit_attribute`
--

DROP TABLE IF EXISTS `visit_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `visit_attribute` (
  `visit_attribute_id` int(11) NOT NULL AUTO_INCREMENT,
  `visit_id` int(11) NOT NULL,
  `attribute_type_id` int(11) NOT NULL,
  `value_reference` text NOT NULL,
  `uuid` char(38) NOT NULL,
  `creator` int(11) NOT NULL,
  `date_created` datetime NOT NULL,
  `changed_by` int(11) DEFAULT NULL,
  `date_changed` datetime DEFAULT NULL,
  `voided` tinyint(1) NOT NULL DEFAULT 0,
  `voided_by` int(11) DEFAULT NULL,
  `date_voided` datetime DEFAULT NULL,
  `void_reason` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`visit_attribute_id`),
  UNIQUE KEY `uuid_visit_attribute` (`uuid`),
  KEY `visit_attribute_attribute_type_id_fk` (`attribute_type_id`),
  KEY `visit_attribute_changed_by_fk` (`changed_by`),
  KEY `visit_attribute_creator_fk` (`creator`),
  KEY `visit_attribute_visit_fk` (`visit_id`),
  KEY `visit_attribute_voided_by_fk` (`voided_by`),
  CONSTRAINT `visit_attribute_attribute_type_id_fk` FOREIGN KEY (`attribute_type_id`) REFERENCES `visit_attribute_type` (`visit_attribute_type_id`),
  CONSTRAINT `visit_attribute_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `visit_attribute_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `visit_attribute_visit_fk` FOREIGN KEY (`visit_id`) REFERENCES `visit` (`visit_id`),
  CONSTRAINT `visit_attribute_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visit_attribute`
--

LOCK TABLES `visit_attribute` WRITE;
/*!40000 ALTER TABLE `visit_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `visit_attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visit_attribute_type`
--

DROP TABLE IF EXISTS `visit_attribute_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `visit_attribute_type` (
  `visit_attribute_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(1024) DEFAULT NULL,
  `datatype` varchar(255) DEFAULT NULL,
  `datatype_config` text DEFAULT NULL,
  `preferred_handler` varchar(255) DEFAULT NULL,
  `handler_config` text DEFAULT NULL,
  `min_occurs` int(11) NOT NULL,
  `max_occurs` int(11) DEFAULT NULL,
  `creator` int(11) NOT NULL,
  `date_created` datetime NOT NULL,
  `changed_by` int(11) DEFAULT NULL,
  `date_changed` datetime DEFAULT NULL,
  `retired` tinyint(1) NOT NULL DEFAULT 0,
  `retired_by` int(11) DEFAULT NULL,
  `date_retired` datetime DEFAULT NULL,
  `retire_reason` varchar(255) DEFAULT NULL,
  `uuid` char(38) NOT NULL,
  PRIMARY KEY (`visit_attribute_type_id`),
  UNIQUE KEY `uuid_visit_attribute_type` (`uuid`),
  KEY `visit_attribute_type_changed_by_fk` (`changed_by`),
  KEY `visit_attribute_type_creator_fk` (`creator`),
  KEY `visit_attribute_type_retired_by_fk` (`retired_by`),
  CONSTRAINT `visit_attribute_type_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `visit_attribute_type_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `visit_attribute_type_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visit_attribute_type`
--

LOCK TABLES `visit_attribute_type` WRITE;
/*!40000 ALTER TABLE `visit_attribute_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `visit_attribute_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visit_type`
--

DROP TABLE IF EXISTS `visit_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `visit_type` (
  `visit_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(1024) DEFAULT NULL,
  `creator` int(11) NOT NULL,
  `date_created` datetime NOT NULL,
  `changed_by` int(11) DEFAULT NULL,
  `date_changed` datetime DEFAULT NULL,
  `retired` tinyint(1) NOT NULL DEFAULT 0,
  `retired_by` int(11) DEFAULT NULL,
  `date_retired` datetime DEFAULT NULL,
  `retire_reason` varchar(255) DEFAULT NULL,
  `uuid` char(38) NOT NULL,
  PRIMARY KEY (`visit_type_id`),
  UNIQUE KEY `uuid_visit_type` (`uuid`),
  KEY `visit_type_changed_by` (`changed_by`),
  KEY `visit_type_creator` (`creator`),
  KEY `visit_type_retired_by` (`retired_by`),
  CONSTRAINT `visit_type_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `visit_type_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `visit_type_retired_by` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visit_type`
--

LOCK TABLES `visit_type` WRITE;
/*!40000 ALTER TABLE `visit_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `visit_type` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-11-15 11:52:18
