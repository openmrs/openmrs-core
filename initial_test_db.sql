-- MySQL dump 10.19  Distrib 10.3.34-MariaDB, for debian-linux-gnu (aarch64)
--
-- Host: localhost    Database: openmrs
-- ------------------------------------------------------
-- Server version	10.3.34-MariaDB-1:10.3.34+maria~focal

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
  KEY `allergy_encounter_id_fk` (`encounter_id`),
  KEY `allergy_patient_id_fk` (`patient_id`),
  KEY `allergy_severity_concept_id_fk` (`severity_concept_id`),
  KEY `allergy_voided_by_fk` (`voided_by`),
  CONSTRAINT `allergy_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `allergy_coded_allergen_fk` FOREIGN KEY (`coded_allergen`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `allergy_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `allergy_encounter_id_fk` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
  CONSTRAINT `allergy_patient_id_fk` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `allergy_severity_concept_id_fk` FOREIGN KEY (`severity_concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `allergy_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  UNIQUE KEY `uuid_diagnosis_attribute` (`uuid`),
  KEY `diagnosis_attribute_attribute_type_id_fk` (`attribute_type_id`),
  KEY `diagnosis_attribute_changed_by_fk` (`changed_by`),
  KEY `diagnosis_attribute_creator_fk` (`creator`),
  KEY `diagnosis_attribute_diagnosis_fk` (`diagnosis_id`),
  KEY `diagnosis_attribute_voided_by_fk` (`voided_by`),
  CONSTRAINT `diagnosis_attribute_attribute_type_id_fk` FOREIGN KEY (`attribute_type_id`) REFERENCES `diagnosis_attribute_type` (`diagnosis_attribute_type_id`),
  CONSTRAINT `diagnosis_attribute_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `diagnosis_attribute_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `diagnosis_attribute_diagnosis_fk` FOREIGN KEY (`diagnosis_id`) REFERENCES `encounter_diagnosis` (`diagnosis_id`),
  CONSTRAINT `diagnosis_attribute_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  UNIQUE KEY `diagnosis_attribute_type_name` (`name`),
  UNIQUE KEY `uuid_diagnosis_attribute_type` (`uuid`),
  KEY `diagnosis_attribute_type_changed_by_fk` (`changed_by`),
  KEY `diagnosis_attribute_type_creator_fk` (`creator`),
  KEY `diagnosis_attribute_type_retired_by_fk` (`retired_by`),
  CONSTRAINT `diagnosis_attribute_type_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `diagnosis_attribute_type_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `diagnosis_attribute_type_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `global_property`
--

LOCK TABLES `global_property` WRITE;
/*!40000 ALTER TABLE `global_property` DISABLE KEYS */;
INSERT INTO `global_property` VALUES ('allergy.allergen.ConceptClasses','Drug,MedSet','A comma-separated list of the allowed concept classes for the allergen field of the allergy dialog','eaf3f161-3752-412e-9a88-7bba5b702345',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.allergen.drug','162552AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the drug allergens concept','73901ac7-58af-4af7-ba24-84d4fa6cc5b9',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.allergen.environment','162554AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the environment allergens concept','11df86f4-9ac9-4ef0-9c27-794b6e80ac30',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.allergen.food','162553AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the food allergens concept','7a5d3497-9451-43e3-9875-577401e7e3e8',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.otherNonCoded','5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the allergy other non coded concept','e58daa38-3811-44b1-b46e-576c33ef15c0',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.reactions','162555AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the allergy reactions concept','b31775b0-c9f7-4182-b5b3-8215057d0c36',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.severity.mild','1498AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the MILD severity concept','9cfc01ef-aed3-429f-afd7-5f5a85c1a8ee',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.severity.moderate','1499AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the MODERATE severity concept','48acac7b-3eb4-4aff-ae1b-19ce279ca7b6',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.severity.severe','1500AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the SEVERE severity concept','4fa87d11-8875-49d1-87d2-2750397670af',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.concept.unknown','1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','UUID for the allergy unknown concept','7e0827a8-d31b-4977-9b9a-fd8c2e2725f2',NULL,NULL,NULL,NULL,NULL,NULL),('allergy.reaction.ConceptClasses','Symptom','A comma-separated list of the allowed concept classes for the reaction field of the allergy dialog','5362e179-b305-4f3d-a689-1171cfcd5cbc',NULL,NULL,NULL,NULL,NULL,NULL),('application.name','OpenMRS','The name of this application, as presented to the user, for example on the login and welcome pages.','e7681fbd-f513-40a2-bfdc-0c81c43842f4',NULL,NULL,NULL,NULL,NULL,NULL),('concept.defaultConceptMapType','NARROWER-THAN','Default concept map type which is used when no other is set','568e3a0f-77b2-4c9b-9509-d48dbc7c0957',NULL,NULL,NULL,NULL,NULL,NULL),('conceptDrug.dosageForm.conceptClasses',NULL,'A comma-separated list of the allowed concept classes for the dosage form field of the concept drug management form.','82290e93-eab7-40a3-8442-5169a9f2cacf',NULL,NULL,NULL,NULL,NULL,NULL),('conceptDrug.route.conceptClasses',NULL,'A comma-separated list of the allowed concept classes for the route field of the concept drug management form.','3ce0bad3-09e2-4aac-8359-abed0fc69490',NULL,NULL,NULL,NULL,NULL,NULL),('concepts.locked','false','if true, do not allow editing concepts','edeaadd0-3ecf-4f80-86c9-a11634cab259','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('concept_map_type_management.enable','false','Enables or disables management of concept map types','29b994cf-b5b6-4b08-8308-b0fbeb066407','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('dashboard.encounters.maximumNumberToShow','3','An integer which, if specified, would determine the maximum number of encounters to display on the encounter tab of the patient dashboard.','f8b9de03-d4cf-420c-805b-e216f7735c2f',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.encounters.providerDisplayRoles',NULL,'A comma-separated list of encounter roles (by name or id). Providers with these roles in an encounter will be displayed on the encounter tab of the patient dashboard.','8a5fe900-f468-44a1-a92b-844766e863a6',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.encounters.showEditLink','true','true/false whether or not to show the \'Edit Encounter\' link on the patient dashboard','1c3728bc-eb24-411b-adaf-960c6e2cf78c','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('dashboard.encounters.showEmptyFields','true','true/false whether or not to show empty fields on the \'View Encounter\' window','26f8a463-dbc3-4398-a224-ed3f8fbd2e37','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('dashboard.encounters.showViewLink','true','true/false whether or not to show the \'View Encounter\' link on the patient dashboard','3ba232cb-c046-41b0-9ef7-38bece3a9520','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('dashboard.encounters.usePages','smart','true/false/smart on how to show the pages on the \'View Encounter\' window.  \'smart\' means that if > 50% of the fields have page numbers defined, show data in pages','042ff68c-a802-4e13-b4d2-195fc2ce776f',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.header.programs_to_show',NULL,'List of programs to show Enrollment details of in the patient header. (Should be an ordered comma-separated list of program_ids or names.)','b03b7a65-001e-4280-9b0e-c37ff229db96',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.header.workflows_to_show',NULL,'List of programs to show Enrollment details of in the patient header. List of workflows to show current status of in the patient header. These will only be displayed if they belong to a program listed above. (Should be a comma-separated list of program_workflow_ids.)','81839be8-43cb-43fd-b1ba-7905d0bb85e9',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.metadata.caseConversion',NULL,'Indicates which type automatic case conversion is applied to program/workflow/state in the patient dashboard. Valid values: lowercase, uppercase, capitalize. If empty no conversion is applied.','9c2e3714-9885-46e2-928c-1a5ec7a8ca28',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.overview.showConcepts',NULL,'Comma delimited list of concepts ids to show on the patient dashboard overview tab','d84d155b-a2cc-439e-a4a3-ab8a8363bd9f',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.regimen.displayDrugSetIds','ANTIRETROVIRAL DRUGS,TUBERCULOSIS TREATMENT DRUGS','Drug sets that appear on the Patient Dashboard Regimen tab. Comma separated list of name of concepts that are defined as drug sets.','263d4083-3c2c-499b-98d7-53bd83f9b7de',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.regimen.displayFrequencies','7 days/week,6 days/week,5 days/week,4 days/week,3 days/week,2 days/week,1 days/week','Frequency of a drug order that appear on the Patient Dashboard. Comma separated list of name of concepts that are defined as drug frequencies.','69142fe9-886d-4a2f-839e-81f590f4595a',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.relationships.show_types',NULL,'Types of relationships separated by commas.  Doctor/Patient,Parent/Child','f21c41a3-d997-46df-8656-366f51299539',NULL,NULL,NULL,NULL,NULL,NULL),('dashboard.showPatientName','false','Whether or not to display the patient name in the patient dashboard title. Note that enabling this could be security risk if multiple users operate on the same computer.','397a09b8-3fd4-49d9-898c-6c4d2f75a032','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('datePicker.weekStart','0','First day of the week in the date picker. Domingo/Dimanche/Sunday:0  Lunes/Lundi/Monday:1','d0352c10-93d4-4f6e-a845-439ccab5f598',NULL,NULL,NULL,NULL,NULL,NULL),('default_locale','en','Specifies the default locale. You can specify both the language code(ISO-639) and the country code(ISO-3166), e.g. \'en_GB\' or just country: e.g. \'en\'','a152b28a-08dd-438f-a9d4-e409b95c1add',NULL,NULL,NULL,NULL,NULL,NULL),('default_location','Unknown Location','The name of the location to use as a system default','8dc531c6-f132-4ae0-ac33-0feb276e50b0',NULL,NULL,NULL,NULL,NULL,NULL),('default_theme',NULL,'Default theme for users.  OpenMRS ships with themes of \'green\', \'orange\', \'purple\', and \'legacy\'','4733581d-d84e-4373-b019-9e4d3ee552e6',NULL,NULL,NULL,NULL,NULL,NULL),('drugOrder.drugOther',NULL,'Specifies the uuid of the concept which represents drug other non coded','4fe96ffd-c390-4167-92aa-3a9c54057430',NULL,NULL,NULL,NULL,NULL,NULL),('drugOrder.requireDrug','false','Set to value true if you need to specify a formulation(Drug) when creating a drug order.','ea3b521f-4eb7-441c-bfaf-5ac523c2fe0c',NULL,NULL,NULL,NULL,NULL,NULL),('drugOrder.requireOutpatientQuantity','true','true/false whether to require quantity, quantityUnits, and numRefills for outpatient drug orders','8b9e5f86-7c17-4c4e-ae46-0afca4fc977a',NULL,NULL,NULL,NULL,NULL,NULL),('encounterForm.obsSortOrder','number','The sort order for the obs listed on the encounter edit form.  \'number\' sorts on the associated numbering from the form schema.  \'weight\' sorts on the order displayed in the form schema.','fc3671ef-c777-4878-87a7-977b59d5669a',NULL,NULL,NULL,NULL,NULL,NULL),('EncounterType.encounterTypes.locked','false','saving, retiring or deleting an Encounter Type is not permitted, if true','999c7e01-c3bc-44b7-800d-42fa489fec7b','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('FormEntry.enableDashboardTab','true','true/false whether or not to show a Form Entry tab on the patient dashboard','0d252df1-aaf4-4981-8c0a-d2ce53787b60','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('FormEntry.enableOnEncounterTab','false','true/false whether or not to show a Enter Form button on the encounters tab of the patient dashboard','090f38fa-dd24-4ee3-985d-9fac0bc62ccb','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('forms.locked','false','Set to a value of true if you do not want any changes to be made on forms, else set to false.','3c20dd4d-04e7-4a41-aa24-da61bb90d169',NULL,NULL,NULL,NULL,NULL,NULL),('graph.color.absolute','rgb(20,20,20)','Color of the \'invalid\' section of numeric graphs on the patient dashboard.','be51a832-ad5b-470c-9a4d-6faf116df6dc',NULL,NULL,NULL,NULL,NULL,NULL),('graph.color.critical','rgb(200,0,0)','Color of the \'critical\' section of numeric graphs on the patient dashboard.','685f48e8-1f69-4ad7-8534-00400047d763',NULL,NULL,NULL,NULL,NULL,NULL),('graph.color.normal','rgb(255,126,0)','Color of the \'normal\' section of numeric graphs on the patient dashboard.','f96c8671-0d6c-4467-b587-0ae50cc4ae56',NULL,NULL,NULL,NULL,NULL,NULL),('gzip.enabled','false','Set to \'true\' to turn on OpenMRS\'s gzip filter, and have the webapp compress data before sending it to any client that supports it. Generally use this if you are running Tomcat standalone. If you are running Tomcat behind Apache, then you\'d want to use Apache to do gzip compression.','499e2875-5a0b-49c0-8263-b21c3b484cd6','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('hl7_archive.dir','hl7_archives','The default name or absolute path for the folder where to write the hl7_in_archives.','8993648a-375c-452f-bcd6-0ff8707cb0eb',NULL,NULL,NULL,NULL,NULL,NULL),('hl7_processor.ignore_missing_patient_non_local','false','If true, hl7 messages for patients that are not found and are non-local will silently be dropped/ignored','b1ede925-6265-4257-a5ed-dbd2419c20ed','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('host.url',NULL,'The URL to redirect to after requesting for a password reset. Always provide a place holder in this url with name {activationKey}, it will get substituted by the actual activation key.','256fbf7d-546f-49f3-9cd0-566d7f4ff1db',NULL,NULL,NULL,NULL,NULL,NULL),('layout.address.format','<org.openmrs.layout.address.AddressTemplate>     <nameMappings class=\"properties\">       <property name=\"postalCode\" value=\"Location.postalCode\"/>       <property name=\"address2\" value=\"Location.address2\"/>       <property name=\"address1\" value=\"Location.address1\"/>       <property name=\"country\" value=\"Location.country\"/>       <property name=\"stateProvince\" value=\"Location.stateProvince\"/>       <property name=\"cityVillage\" value=\"Location.cityVillage\"/>     </nameMappings>     <sizeMappings class=\"properties\">       <property name=\"postalCode\" value=\"10\"/>       <property name=\"address2\" value=\"40\"/>       <property name=\"address1\" value=\"40\"/>       <property name=\"country\" value=\"10\"/>       <property name=\"stateProvince\" value=\"10\"/>       <property name=\"cityVillage\" value=\"10\"/>     </sizeMappings>     <lineByLineFormat>       <string>address1</string>       <string>address2</string>       <string>cityVillage stateProvince country postalCode</string>     </lineByLineFormat>    <requiredElements>\\\\\\\\n\" + \" </requiredElements>\\\\\\\\n\" + \" </org.openmrs.layout.address.AddressTemplate>','XML description of address formats','d506f19e-1220-4fda-802c-326870fe64c2',NULL,NULL,NULL,NULL,NULL,NULL),('layout.name.format','short','Format in which to display the person names.  Valid values are short, long','5d64f396-f660-4ff5-9e03-e120bddc4591',NULL,NULL,NULL,NULL,NULL,NULL),('locale.allowed.list','en, en_GB, es, fr, it, pt','Comma delimited list of locales allowed for use on system','ed779482-9150-4c4e-9124-561db7bcb420',NULL,NULL,NULL,NULL,NULL,NULL),('location.field.style','default','Type of widget to use for location fields','4910b45c-0f32-4d6b-bc0b-9c5a80a2f8eb',NULL,NULL,NULL,NULL,NULL,NULL),('log.layout','%p - %C{1}.%M(%L) |%d{ISO8601}| %m%n','A log layout pattern which is used by the OpenMRS file appender.','bb444578-e5bf-43b0-8b50-907165321ee8',NULL,NULL,NULL,NULL,NULL,NULL),('log.level','org.openmrs.api:info','Logging levels for log4j2.xml. Valid format is class:level,class:level. If class not specified, \'org.openmrs.api\' presumed. Valid levels are trace, debug, info, warn, error or fatal','089162f3-038f-45f5-a045-4584e4e8c877',NULL,NULL,NULL,NULL,NULL,NULL),('log.location',NULL,'A directory where the OpenMRS log file appender is stored. The log file name is \'openmrs.log\'.','1940b15e-cc1c-4b85-9bac-6897415f99bc',NULL,NULL,NULL,NULL,NULL,NULL),('login.url','login.htm','Responsible for defining the Authentication URL','54f496dd-b6a3-4dfa-8cc1-65f79b1b8015',NULL,NULL,NULL,NULL,NULL,NULL),('mail.debug','false','true/false whether to print debugging information during mailing','b5a07cb7-fbcb-4bc8-8489-70376577f4cf',NULL,NULL,NULL,NULL,NULL,NULL),('mail.default_content_type','text/plain','Content type to append to the mail messages','e4229af5-604a-4853-891c-df434d35f74d',NULL,NULL,NULL,NULL,NULL,NULL),('mail.from','info@openmrs.org','Email address to use as the default from address','9618cc7b-27cf-4677-acb5-b47c4f0629ea',NULL,NULL,NULL,NULL,NULL,NULL),('mail.password','test','Password for the SMTP user (if smtp_auth is enabled)','e48b2aa7-f360-481b-a604-383668abd27a',NULL,NULL,NULL,NULL,NULL,NULL),('mail.smtp.starttls.enable','false','Set to true to enable TLS encryption, else set to false','ca2a6d7d-9387-42cd-b845-4eccecd2a606',NULL,NULL,NULL,NULL,NULL,NULL),('mail.smtp_auth','false','true/false whether the smtp host requires authentication','cec5bf48-dcea-45e8-aa07-03f2addbf553',NULL,NULL,NULL,NULL,NULL,NULL),('mail.smtp_host','localhost','SMTP host name','7f26cfaa-9e8c-4fb9-a561-fe84e2c21983',NULL,NULL,NULL,NULL,NULL,NULL),('mail.smtp_port','25','SMTP port','17114e64-67c8-441d-9d37-4da26fe8f0d1',NULL,NULL,NULL,NULL,NULL,NULL),('mail.transport_protocol','smtp','Transport protocol for the messaging engine. Valid values: smtp','814e6dd6-192c-458d-afa5-c8079258600f',NULL,NULL,NULL,NULL,NULL,NULL),('mail.user','test','Username of the SMTP user (if smtp_auth is enabled)','99045648-3319-495a-80f0-dde97405154c',NULL,NULL,NULL,NULL,NULL,NULL),('minSearchCharacters','2','Number of characters user must input before searching is started.','c584e421-cd40-4ffd-9943-d4ef1ef75eb0',NULL,NULL,NULL,NULL,NULL,NULL),('module_repository_folder','modules','Name of the folder in which to store the modules','1f3763b2-fd77-4056-90e4-212b871ff305',NULL,NULL,NULL,NULL,NULL,NULL),('newPatientForm.relationships',NULL,'Comma separated list of the RelationshipTypes to show on the new/short patient form.  The list is defined like \'3a, 4b, 7a\'.  The number is the RelationshipTypeId and the \'a\' vs \'b\' part is which side of the relationship is filled in by the user.','f5ce96ed-6463-4cd1-a020-3792d44963b3',NULL,NULL,NULL,NULL,NULL,NULL),('new_patient_form.showRelationships','false','true/false whether or not to show the relationship editor on the addPatient.htm screen','6e5994b3-520d-4caf-a9fb-d61b19562523','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('obs.complex_obs_dir','complex_obs','Default directory for storing complex obs.','4e3ca595-2506-4548-be8b-5d18223918b3',NULL,NULL,NULL,NULL,NULL,NULL),('order.drugDispensingUnitsConceptUuid',NULL,'Specifies the uuid of the concept set where its members represent the possible drug dispensing units','38aac0e9-c759-4d19-ae54-24290e263c58',NULL,NULL,NULL,NULL,NULL,NULL),('order.drugDosingUnitsConceptUuid',NULL,'Specifies the uuid of the concept set where its members represent the possible drug dosing units','dad63678-79df-4e35-b71d-4d1a48dc6911',NULL,NULL,NULL,NULL,NULL,NULL),('order.drugRoutesConceptUuid',NULL,'Specifies the uuid of the concept set where its members represent the possible drug routes','c01d7641-125d-459e-8116-71366e8cdf7d',NULL,NULL,NULL,NULL,NULL,NULL),('order.durationUnitsConceptUuid',NULL,'Specifies the uuid of the concept set where its members represent the possible duration units','b28c2607-688e-4edd-a850-9c8cbacb57a6',NULL,NULL,NULL,NULL,NULL,NULL),('order.nextOrderNumberSeed','1','The next order number available for assignment','1740c7ef-0630-425f-bbc6-15979cb59cde',NULL,NULL,NULL,NULL,NULL,NULL),('order.orderNumberGeneratorBeanId',NULL,'Specifies spring bean id of the order generator to use when assigning order numbers','a75b7de3-91b7-4b50-89a8-428f1335cad9',NULL,NULL,NULL,NULL,NULL,NULL),('order.testSpecimenSourcesConceptUuid',NULL,'Specifies the uuid of the concept set where its members represent the possible test specimen sources','feec06fa-62b5-4d51-bd8e-4f081bf555e2',NULL,NULL,NULL,NULL,NULL,NULL),('patient.defaultPatientIdentifierValidator','org.openmrs.patient.impl.LuhnIdentifierValidator','This property sets the default patient identifier validator.  The default validator is only used in a handful of (mostly legacy) instances.  For example, it\'s used to generate the isValidCheckDigit calculated column and to append the string \"(default)\" to the name of the default validator on the editPatientIdentifierType form.','04762247-6d55-48d3-a2ad-36b40b29035e',NULL,NULL,NULL,NULL,NULL,NULL),('patient.headerAttributeTypes',NULL,'A comma delimited list of PersonAttributeType names that will be shown on the patient dashboard','4609457a-9d21-4ec2-80ce-c83a200cbd00',NULL,NULL,NULL,NULL,NULL,NULL),('patient.identifierPrefix',NULL,'This property is only used if patient.identifierRegex is empty.  The string here is prepended to the sql indentifier search string.  The sql becomes \"... where identifier like \'<PREFIX><QUERY STRING><SUFFIX>\';\".  Typically this value is either a percent sign (%) or empty.','8d9239b3-06d1-465a-aae1-da73018bb9db',NULL,NULL,NULL,NULL,NULL,NULL),('patient.identifierRegex',NULL,'WARNING: Using this search property can cause a drop in mysql performance with large patient sets.  A MySQL regular expression for the patient identifier search strings.  The @SEARCH@ string is replaced at runtime with the user\'s search string.  An empty regex will cause a simply \'like\' sql search to be used. Example: ^0*@SEARCH@([A-Z]+-[0-9])?$','a62944ef-6a15-42f6-9257-f99fb2c8c0ad',NULL,NULL,NULL,NULL,NULL,NULL),('patient.identifierSearchPattern',NULL,'If this is empty, the regex or suffix/prefix search is used.  Comma separated list of identifiers to check.  Allows for faster searching of multiple options rather than the slow regex. e.g. @SEARCH@,0@SEARCH@,@SEARCH-1@-@CHECKDIGIT@,0@SEARCH-1@-@CHECKDIGIT@ would turn a request for \"4127\" into a search for \"in (\'4127\',\'04127\',\'412-7\',\'0412-7\')\"','b1489289-c8be-4235-b219-09e81ffbe7b1',NULL,NULL,NULL,NULL,NULL,NULL),('patient.identifierSuffix',NULL,'This property is only used if patient.identifierRegex is empty.  The string here is prepended to the sql indentifier search string.  The sql becomes \"... where identifier like \'<PREFIX><QUERY STRING><SUFFIX>\';\".  Typically this value is either a percent sign (%) or empty.','6df515d8-297c-437e-94c6-2858c3c955ee',NULL,NULL,NULL,NULL,NULL,NULL),('patient.listingAttributeTypes',NULL,'A comma delimited list of PersonAttributeType names that should be displayed for patients in _lists_','e2057b0f-12c6-4a78-945d-0b08181f7977',NULL,NULL,NULL,NULL,NULL,NULL),('patient.nameValidationRegex',NULL,'Names of the patients must pass this regex. Eg : ^[a-zA-Z \\\\\\\\-]+$ contains only english alphabet letters, spaces, and hyphens. A value of .* or the empty string means no validation is done.','33215153-23f5-4e00-967b-243b81017851',NULL,NULL,NULL,NULL,NULL,NULL),('patient.viewingAttributeTypes',NULL,'A comma delimited list of PersonAttributeType names that should be displayed for patients when _viewing individually_','5805e57f-8f9a-4f3c-a0b1-a02a2504ef72',NULL,NULL,NULL,NULL,NULL,NULL),('patientIdentifierSearch.matchMode','EXACT','Specifies how patient identifiers are matched while searching for a patient. Valid values are \'EXACT, \'ANYWHERE\' or \'START\'. Defaults to \'EXACT\' if missing or invalid value is present.','f57be38c-490d-452e-9a52-1e7450fe7b61',NULL,NULL,NULL,NULL,NULL,NULL),('patientIdentifierTypes.locked','false','Set to a value of true if you do not want allow editing patient identifier types, else set to false.','1a799da5-986c-47f8-9685-b95cc5932dd5',NULL,NULL,NULL,NULL,NULL,NULL),('patientSearch.matchMode','START','Specifies how patient names are matched while searching patient. Valid values are \'ANYWHERE\' or \'START\'. Defaults to start if missing or invalid value is present.','43c4f838-51c1-495b-b474-b597b1fca708',NULL,NULL,NULL,NULL,NULL,NULL),('patient_identifier.importantTypes',NULL,'A comma delimited list of PatientIdentifier names : PatientIdentifier locations that will be displayed on the patient dashboard.  E.g.: TRACnet ID:Rwanda,ELDID:Kenya','782440dd-ab92-4c9b-aa5e-28a9734917a3',NULL,NULL,NULL,NULL,NULL,NULL),('person.attributeSearchMatchMode','EXACT','Specifies how person attributes are matched while searching person. Valid values are \'ANYWHERE\' or \'EXACT\'. Defaults to exact if missing or invalid value is present.','638d4774-7867-4cb4-a8a7-e67d962636a9',NULL,NULL,NULL,NULL,NULL,NULL),('person.searchMaxResults','1000','The maximum number of results returned by patient searches','6637deaf-0024-46bb-b999-e6d8c892e357',NULL,NULL,NULL,NULL,NULL,NULL),('personAttributeTypes.locked','false','Set to a value of true if you do not want allow editing person attribute types, else set to false.','a9ba4279-7ce0-4638-b4e0-86745c189375',NULL,NULL,NULL,NULL,NULL,NULL),('provider.unknownProviderUuid',NULL,'Specifies the uuid of the Unknown Provider account','d50ba61e-31ad-4d11-a674-49efca9c8b25',NULL,NULL,NULL,NULL,NULL,NULL),('providerSearch.matchMode','EXACT','Specifies how provider identifiers are matched while searching for providers. Valid values are START,EXACT, END or ANYWHERE','87cf37be-07a6-4650-b644-df9926bf1e01',NULL,NULL,NULL,NULL,NULL,NULL),('reportProblem.url','http://errors.openmrs.org/scrap','The openmrs url where to submit bug reports','9bb8a464-69cc-4ae1-b8dc-03d926161350',NULL,NULL,NULL,NULL,NULL,NULL),('scheduler.password','test','Password for the OpenMRS user that will perform the scheduler activities','f2f53ffd-c0c7-4123-bb7e-cfbd61ea1dcf',NULL,NULL,NULL,NULL,NULL,NULL),('scheduler.username','admin','Username for the OpenMRS user that will perform the scheduler activities','00dc0a29-3fa0-4a36-aa07-7f40ae7645d0',NULL,NULL,NULL,NULL,NULL,NULL),('search.caseSensitiveDatabaseStringComparison','false','Indicates whether database string comparison is case sensitive or not. Setting this to false for MySQL with a case insensitive collation improves search performance.','89272e39-8689-4428-8c2c-11ec2273f159',NULL,NULL,NULL,NULL,NULL,NULL),('search.indexVersion','7','Indicates the index version. If it is blank, the index needs to be rebuilt.','b3b885c5-d0f1-4ff1-93b0-995a331f128b',NULL,NULL,NULL,NULL,NULL,NULL),('searchWidget.batchSize','200','The maximum number of search results that are returned by an ajax call','39a2b99f-9f6a-4d7f-9857-8d4031f248cb',NULL,NULL,NULL,NULL,NULL,NULL),('searchWidget.dateDisplayFormat',NULL,'Date display format to be used to display the date somewhere in the UI i.e the search widgets and autocompletes','403c8cb8-4ea0-47ff-b296-d3c2acbbc10c',NULL,NULL,NULL,NULL,NULL,NULL),('searchWidget.maximumResults','2000','Specifies the maximum number of results to return from a single search in the search widgets','696452cf-0171-4755-9b64-912d5080a79c',NULL,NULL,NULL,NULL,NULL,NULL),('searchWidget.runInSerialMode','false','Specifies whether the search widgets should make ajax requests in serial or parallel order, a value of true is appropriate for implementations running on a slow network connection and vice versa','3ef448c6-3625-40df-8cba-d5bb0e92eaee','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('searchWidget.searchDelayInterval','300','Specifies time interval in milliseconds when searching, between keyboard keyup event and triggering the search off, should be higher if most users are slow when typing so as to minimise the load on the server','7668f58f-5178-46c4-ba8f-30ab409665b6',NULL,NULL,NULL,NULL,NULL,NULL),('security.allowedFailedLoginsBeforeLockout','7','Maximum number of failed logins allowed after which username is locked out','6ab3a5b8-dd9e-4f97-ab14-117e04c8a401',NULL,NULL,NULL,NULL,NULL,NULL),('security.passwordCannotMatchUsername','true','Configure whether passwords must not match user\'s username or system id','2dc79f65-ec53-4349-b9ec-82340cfbc1ca','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('security.passwordCustomRegex',NULL,'Configure a custom regular expression that a password must match','0e86a20d-e25c-47b3-bace-438f40a1be9c',NULL,NULL,NULL,NULL,NULL,NULL),('security.passwordMinimumLength','8','Configure the minimum length required of all passwords','5ff5841d-fa50-4532-8205-1808927b9259',NULL,NULL,NULL,NULL,NULL,NULL),('security.passwordRequiresDigit','true','Configure whether passwords must contain at least one digit','5431d53a-f6ca-4af5-9840-06a73ef14de1','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('security.passwordRequiresNonDigit','true','Configure whether passwords must contain at least one non-digit','50b7640d-b2c8-4bf4-81d4-730323df9dd3','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('security.passwordRequiresUpperAndLowerCase','true','Configure whether passwords must contain both upper and lower case characters','7ffcb927-1341-4113-b2ed-df9a35801a55','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('security.validTime','600000','Specifies the duration of time in seconds for which a password reset token is valid, the default value is 10 minutes and the allowed values range from 1 minute to 12hrs','28764d15-f64a-49e0-807f-a309f9a99f41',NULL,NULL,NULL,NULL,NULL,NULL),('user.headerAttributeTypes',NULL,'A comma delimited list of PersonAttributeType names that will be shown on the user dashboard. (not used in v1.5)','4f3f5fea-30eb-4e83-b7ae-9ae8704c09e0',NULL,NULL,NULL,NULL,NULL,NULL),('user.listingAttributeTypes',NULL,'A comma delimited list of PersonAttributeType names that should be displayed for users in _lists_','ecf4f41a-6690-460e-b765-616b10260a02',NULL,NULL,NULL,NULL,NULL,NULL),('user.requireEmailAsUsername','false','Indicates whether a username must be a valid e-mail or not.','01bc73a8-08c0-4723-9358-9d02df17a93c','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('user.viewingAttributeTypes',NULL,'A comma delimited list of PersonAttributeType names that should be displayed for users when _viewing individually_','14f1663b-19b3-44b2-bf8a-c30431e19598',NULL,NULL,NULL,NULL,NULL,NULL),('use_patient_attribute.healthCenter','false','Indicates whether or not the \'health center\' attribute is shown when viewing/searching for patients','7dea235a-3efd-40f3-9b3d-e64042043013','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('use_patient_attribute.mothersName','false','Indicates whether or not mother\'s name is able to be added/viewed for a patient','0ffb5e2b-9671-41e6-a504-88d02a24ccba','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('validation.disable','false','Disables validation of OpenMRS Objects. Only takes affect on next restart. Warning: only do this is you know what you are doing!','346a9dc0-5f6a-462d-9065-85bcc7b62203',NULL,NULL,NULL,NULL,NULL,NULL),('visits.allowOverlappingVisits','true','true/false whether or not to allow visits of a given patient to overlap','e0376867-c667-4c81-b82f-f65ec7bfd5c7','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('visits.assignmentHandler','org.openmrs.api.handler.ExistingVisitAssignmentHandler','Set to the name of the class responsible for assigning encounters to visits.','c90dd1db-0611-451a-94c4-ac4b0f44035d',NULL,NULL,NULL,NULL,NULL,NULL),('visits.autoCloseVisitType',NULL,'comma-separated list of the visit type(s) to automatically close','7abb62f4-81a6-4145-ac0d-f01034d666ce',NULL,NULL,NULL,NULL,NULL,NULL),('visits.enabled','true','Set to true to enable the Visits feature. This will replace the \'Encounters\' tab with a \'Visits\' tab on the dashboard.','9c594c46-482d-48b3-9643-05641111fc9f','org.openmrs.customdatatype.datatype.BooleanDatatype',NULL,NULL,NULL,NULL,NULL),('visits.encounterTypeToVisitTypeMapping',NULL,'Specifies how encounter types are mapped to visit types when automatically assigning encounters to visits. e.g 1:1, 2:1, 3:2 in the format encounterTypeId:visitTypeId or encounterTypeUuid:visitTypeUuid or a combination of encounter/visit type uuids and ids e.g 1:759799ab-c9a5-435e-b671-77773ada74e4','c84f289c-3869-4683-9b14-ebc47c374212',NULL,NULL,NULL,NULL,NULL,NULL);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `liquibasechangelog`
--

LOCK TABLES `liquibasechangelog` WRITE;
/*!40000 ALTER TABLE `liquibasechangelog` DISABLE KEYS */;
INSERT INTO `liquibasechangelog` VALUES ('1644357630219-1','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',1,'EXECUTED','8:a1574654e5663f2ce2e18afb8bea036e','createTable tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-2','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',2,'EXECUTED','8:0bdfac753b3aaf9faf6659bcd4953d72','createTable tableName=allergy_reaction','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-3','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',3,'EXECUTED','8:498ecc55fd38caca6f226955b3fc937e','createTable tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-4','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',4,'EXECUTED','8:9d0d3bc5a01e33bb63c214613b50821b','createTable tableName=clob_datatype_storage','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-5','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',5,'EXECUTED','8:26dc60d4c245e764e865d68376f7efc3','createTable tableName=cohort','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-6','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',6,'EXECUTED','8:4301e12d004a11ab4bf032d7899ae12f','createTable tableName=cohort_member','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-7','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',7,'EXECUTED','8:1c2d90967948a977873c16db3b403cc9','createTable tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-8','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',8,'EXECUTED','8:36d8146e1b82ac0847521ed807552e14','createTable tableName=concept_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-9','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',9,'EXECUTED','8:eebb54e1485288c13f794b72e4d5ff3e','createTable tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-10','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',10,'EXECUTED','8:a3d19d44dd757b56d9b9247bf75e6c60','createTable tableName=concept_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-11','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',11,'EXECUTED','8:1d8e430d9e8b6a12ac5e36a037cb4326','createTable tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-12','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',12,'EXECUTED','8:a373e8bb2d16ed1f2df7a00ae4795c17','createTable tableName=concept_complex','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-13','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',13,'EXECUTED','8:eb30d464c5050db9e42f64c11a82e3f4','createTable tableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-14','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',14,'EXECUTED','8:7af7460d16bdcbd91329e65f00f9a7a7','createTable tableName=concept_description','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-15','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',15,'EXECUTED','8:aaf736c91a19f1c5ea09380c55b48530','createTable tableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-16','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',16,'EXECUTED','8:f9bfd52d75904bb7089c621daa383638','createTable tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-17','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',17,'EXECUTED','8:63c9860c98026eacb9d0dc37f91deca7','createTable tableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-18','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',18,'EXECUTED','8:2ad1f3969d4447ec88134a92452372cc','createTable tableName=concept_name_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-19','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',19,'EXECUTED','8:fdb9dd9556be523db691daa0b32499a1','createTable tableName=concept_numeric','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-20','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',20,'EXECUTED','8:929c34963d98e23e9bd221dfc1010370','createTable tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-21','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',21,'EXECUTED','8:32485f3a60b8038b6bec0697ec1925c8','createTable tableName=concept_proposal_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-22','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',22,'EXECUTED','8:16e6be07eb5e0b2d3b1fa14b76cdae85','createTable tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-23','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',23,'EXECUTED','8:975f856eed215ac48d10bddc7df19ce8','createTable tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-24','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',24,'EXECUTED','8:b205b1094f9e4129665c97619d957acd','createTable tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-25','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',25,'EXECUTED','8:d226f03f28d47c23a6cdffeb5dfcce8d','createTable tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-26','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',26,'EXECUTED','8:ddee45f24fe0141e3f8978e88a2120d5','createTable tableName=concept_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-27','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',27,'EXECUTED','8:ad0098726e9d8a26725a003313bd94c0','createTable tableName=concept_state_conversion','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-28','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',28,'EXECUTED','8:b51ab8476cea184afd24720263db89a5','createTable tableName=concept_stop_word','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-29','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',29,'EXECUTED','8:057c0d1297132baabcf98b3da68c8e5a','createTable tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-30','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',30,'EXECUTED','8:5a6130a1b27f224b6bde0b4b606efcc3','createTable tableName=diagnosis_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-31','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',31,'EXECUTED','8:a9ec4258b3c2df3421e43a1076be3233','createTable tableName=diagnosis_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-32','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',32,'EXECUTED','8:ce10acaf245f54206e0c709a154a62a6','createTable tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-33','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',33,'EXECUTED','8:e6ff3c6b1b7d6afe35ac92e8db384226','createTable tableName=drug_ingredient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-34','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',34,'EXECUTED','8:2a30bbeb2e03d2f114c0f21dce852c7d','createTable tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-35','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',35,'EXECUTED','8:8a377238901bb96dc9f583c8595c5dfd','createTable tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-36','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',36,'EXECUTED','8:57280606907794c00f691f98d1c16923','createTable tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-37','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',37,'EXECUTED','8:cf0ba3a6b5b0dcafcf39baeac2b0729c','createTable tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-38','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',38,'EXECUTED','8:405a58234de94d432da838e702da2b80','createTable tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-39','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',39,'EXECUTED','8:1f139ee60816858509a3cac8448c4800','createTable tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-40','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',40,'EXECUTED','8:a921e31470e8d0ecdb523ca6e9f1209a','createTable tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-41','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',41,'EXECUTED','8:2a0e44531e5620966be205ccd46b7707','createTable tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-42','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',42,'EXECUTED','8:4aaca0c72edaf731840e0d762d80744d','createTable tableName=field_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-43','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',43,'EXECUTED','8:0bd0d8f993fd61918a35772bc64ef4a6','createTable tableName=field_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-44','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',44,'EXECUTED','8:d2bd2f4ae9d8df1bd05ba522a00d6ca9','createTable tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-45','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',45,'EXECUTED','8:20bc3a7d092df728468f39c9ec34e948','createTable tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-46','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',46,'EXECUTED','8:034c84d110c160303e0b364f7556ef12','createTable tableName=form_resource','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-47','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',47,'EXECUTED','8:6e46c2a8528e95c953f3b0f472e28196','createTable tableName=global_property','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-48','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',48,'EXECUTED','8:e9c5f2876f83da09b7e67134ec9966ea','createTable tableName=hl7_in_archive','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-49','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',49,'EXECUTED','8:d734f88fd306a6c21eade5276e703e7a','createTable tableName=hl7_in_error','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-50','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',50,'EXECUTED','8:d0545b2797865d0c7c759e2e8ad5484a','createTable tableName=hl7_in_queue','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-51','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',51,'EXECUTED','8:52a1872d2be59ad89eab9b7b4e1c3b4e','createTable tableName=hl7_source','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-54','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',52,'EXECUTED','8:df439256d6ed403a65fcbcc76eff93e8','createTable tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-55','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',53,'EXECUTED','8:c9536c9a6979581d5c885fca8ed4686c','createTable tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-56','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',54,'EXECUTED','8:c552e512fa21a2ef51c700a2da1da60e','createTable tableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-57','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',55,'EXECUTED','8:b8ffda55c09bb23e51609f31722d2b6c','createTable tableName=location_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-58','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',56,'EXECUTED','8:f975333c0536c33eee965ef4c34efb31','createTable tableName=location_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-59','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',57,'EXECUTED','8:7eb6bbc05db0e70b52f1030671af8909','createTable tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-60','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',58,'EXECUTED','8:595b50570d7dfd5e3fd391a68f2958ec','createTable tableName=notification_alert','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-61','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',59,'EXECUTED','8:bda22bd4901f0e517eeeaab5d26e7110','createTable tableName=notification_alert_recipient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-62','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',60,'EXECUTED','8:accb3c064e83a84bf8a49d77e676ff59','createTable tableName=notification_template','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-63','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',61,'EXECUTED','8:073a3f252e7433668db319e20712559f','createTable tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-64','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',62,'EXECUTED','8:fcdc4592fbcb2d063c6f8b2246749b52','createTable tableName=order_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-65','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',63,'EXECUTED','8:38e32f2ea177a0d48637f71cef298701','createTable tableName=order_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-66','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',64,'EXECUTED','8:eb7b0e1d100ec2c0064005885eebc105','createTable tableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-67','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',65,'EXECUTED','8:2a2a4a4c44bd651a39fec96dfd7db971','createTable tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-68','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',66,'EXECUTED','8:55fc0eecde5d0c2589a9899c7196431d','createTable tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-69','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',67,'EXECUTED','8:02bc3f9204cd8ca3a36933e1b30bbfdb','createTable tableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-70','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',68,'EXECUTED','8:06da221e27fc62dcbd5c86662d8a247b','createTable tableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-71','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',69,'EXECUTED','8:28a3a60247f16eb2de11239288f18d8c','createTable tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-72','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',70,'EXECUTED','8:f2f5721cbf141993c5a3283534141563','createTable tableName=order_set_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-73','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',71,'EXECUTED','8:9a2ec0afaa33351b8986cda8cfa73c9a','createTable tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-74','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:33',72,'EXECUTED','8:6db56faff7c0f6ba98c3fab083a309a3','createTable tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-75','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',73,'EXECUTED','8:a6c4a23a3a4e796b50be4273ab3dbe84','createTable tableName=order_type_class_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-76','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',74,'EXECUTED','8:64b6b7667d3f56c02b2126f311e5ef8f','createTable tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-77','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',75,'EXECUTED','8:b057534c5180a7e2f6c99a8558112baa','createTable tableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-78','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',76,'EXECUTED','8:dc9bc02838a418e2ee1481ebabadb163','createTable tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-79','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',77,'EXECUTED','8:a75a6bc79540a0b19993756ec3560890','createTable tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-80','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',78,'EXECUTED','8:69beac5e7891d18084f7cbcbfdff4cd9','createTable tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-81','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',79,'EXECUTED','8:1b175a096d0d8fc8c736dac4b2f1779a','createTable tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-82','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',80,'EXECUTED','8:b2298eac93b57e111dc21383c511974d','createTable tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-83','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',81,'EXECUTED','8:2d27ab5a30c986e6f3170f0514147906','createTable tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-84','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',82,'EXECUTED','8:f8324d9b43d4e8e1fe4cbb2f443dbfa9','createTable tableName=person_address','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-85','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',83,'EXECUTED','8:da12a57586acad6fcbe00bdb2a1b291d','createTable tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-86','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',84,'EXECUTED','8:5d3282bf4f46fc1455c6d6ef25ccba48','createTable tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-87','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',85,'EXECUTED','8:7d36fb0c0c5f682678eccb725368718f','createTable tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-88','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',86,'EXECUTED','8:b8e718fdb25ac97bad87ca79d168d9a3','createTable tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-89','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',87,'EXECUTED','8:1b5de20b6f93874d0705c3eb6274ee6b','createTable tableName=privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-90','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',88,'EXECUTED','8:8dd949da1d263102d34d2b5893951173','createTable tableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-91','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',89,'EXECUTED','8:3c2a98dfeda65f54d5df02a1d010c882','createTable tableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-92','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',90,'EXECUTED','8:aa8e59c5bfb827373b8ad8dec9ac339f','createTable tableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-93','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',91,'EXECUTED','8:16f18cf52c3b9974f00ed3483864f859','createTable tableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-94','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',92,'EXECUTED','8:23300a0f37a689c77cf4774468cf52fb','createTable tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-95','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',93,'EXECUTED','8:5216d51c5bf49b907642b573bca7abfc','createTable tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-96','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',94,'EXECUTED','8:81610803007428cc131d306d44b75f8d','createTable tableName=provider_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-97','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',95,'EXECUTED','8:f28239600bcbb9411adebe3321db73ad','createTable tableName=referral_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-98','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',96,'EXECUTED','8:733ea5367510b8c5503f891352aa56b5','createTable tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-99','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',97,'EXECUTED','8:593dc9e9d07772efc4595c3bc15fd525','createTable tableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-100','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',98,'EXECUTED','8:9bdc7472dc26e1288012daee56a2a5fe','createTable tableName=report_object','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-101','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',99,'EXECUTED','8:9fcc554f574408d19ad460aa7e6021e6','createTable tableName=report_schema_xml','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-102','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',100,'EXECUTED','8:b63f9a6a5b31fff9be9201568697b0e1','createTable tableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-103','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',101,'EXECUTED','8:9bb9fd852e8009445c684b6fcf7c5bff','createTable tableName=role_privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-104','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',102,'EXECUTED','8:9af94c59c630b93a4b6d829e3fd16275','createTable tableName=role_role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-105','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',103,'EXECUTED','8:b26ee6e10c418e13d685ee24ce19cc78','createTable tableName=scheduler_task_config','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-106','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',104,'EXECUTED','8:269fe53fcbab5cc01fc4b13397d6f1a8','createTable tableName=scheduler_task_config_property','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-107','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',105,'EXECUTED','8:7a043935cb2c490dc309af316eed491b','createTable tableName=serialized_object','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-108','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',106,'EXECUTED','8:9ed4571520deb25952d938e7245bdd92','createTable tableName=test_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-109','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',107,'EXECUTED','8:9bef2106c6365ee60bc8ed42245fdca1','createTable tableName=user_property','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-110','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',108,'EXECUTED','8:048d763cc7964737faa57a9de4d740a2','createTable tableName=user_role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-111','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',109,'EXECUTED','8:3ee2e0a856dc18ee108ee26cd4e2e1d4','createTable tableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-112','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',110,'EXECUTED','8:cf6e864f7607074229284d4f35dde78e','createTable tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-113','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',111,'EXECUTED','8:314b71bba804fc31c2d6d246c2cd5c8a','createTable tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-114','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',112,'EXECUTED','8:4fdd834a7c67254a1a43731143eac162','createTable tableName=visit_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-115','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',113,'EXECUTED','8:ce6b0e41d3ec10d08a79be78120cfe19','createTable tableName=visit_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-116','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',114,'EXECUTED','8:785fa875154432f6a14919abbf246e4e','createIndex indexName=Unique_StopWord_Key, tableName=concept_stop_word','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-117','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',115,'EXECUTED','8:b333d65cbce8404063a1a9c49de4448f','createIndex indexName=address_for_person, tableName=person_address','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-118','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',116,'EXECUTED','8:66771b59765b7d074573fc820d0cd3fe','createIndex indexName=alert_creator, tableName=notification_alert','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-119','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',117,'EXECUTED','8:8147fa6e97c11507e1f592303259720e','createIndex indexName=alert_date_to_expire_idx, tableName=notification_alert','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-120','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',118,'EXECUTED','8:3191e265a3b57b0521ac0f34acec54a3','createIndex indexName=alert_read_by_user, tableName=notification_alert_recipient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-121','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',119,'EXECUTED','8:f5fae48654f180b62cf26898cd7f9459','createIndex indexName=allergy_changed_by_fk, tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-122','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',120,'EXECUTED','8:21221492eb4e97daa9035b20a84f906a','createIndex indexName=allergy_coded_allergen_fk, tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-123','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',121,'EXECUTED','8:b1d6b67f44e93f9b02919352b5aacaad','createIndex indexName=allergy_creator_fk, tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-124','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',122,'EXECUTED','8:98f799ab7b42725fa3b2fa321c69c19c','createIndex indexName=allergy_encounter_id_fk, tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-125','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',123,'EXECUTED','8:3802de1996f1d6d8960ff81b4e7a8490','createIndex indexName=allergy_patient_id_fk, tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-126','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',124,'EXECUTED','8:73c8f4df0e4b017640c1dbd16aa29dc7','createIndex indexName=allergy_reaction_allergy_id_fk, tableName=allergy_reaction','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-127','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',125,'EXECUTED','8:0c7772d6190cbac7f586dddedfddc53a','createIndex indexName=allergy_reaction_reaction_concept_id_fk, tableName=allergy_reaction','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-128','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',126,'EXECUTED','8:1c9aac1012a9f0aa9ffd18108fb78c7b','createIndex indexName=allergy_severity_concept_id_fk, tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-129','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',127,'EXECUTED','8:26d5e7fcd5345ba55707d1e45d68f384','createIndex indexName=allergy_voided_by_fk, tableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-130','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',128,'EXECUTED','8:6085d7b477502bb8c100914454572165','createIndex indexName=answer, tableName=concept_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-131','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',129,'EXECUTED','8:6b328688423a699018d015f3de50e7d9','createIndex indexName=answer_answer_drug_fk, tableName=concept_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-132','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',130,'EXECUTED','8:e149c8cd912ab791878266048cdc5f66','createIndex indexName=answer_concept, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-133','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',131,'EXECUTED','8:4cff1dfa775ab85432147fc7a9bc45bb','createIndex indexName=answer_concept_drug, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-134','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',132,'EXECUTED','8:cb0f8b09f946ae6f4de454a9b3c73d14','createIndex indexName=answer_creator, tableName=concept_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-135','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',133,'EXECUTED','8:aafb0f0bbcc38efaea28ef6a0d0279b9','createIndex indexName=answers_for_concept, tableName=concept_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-136','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',134,'EXECUTED','8:b932dbce6343d0824d78fb1ab6bafe63','createIndex indexName=attribute_changer, tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-137','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',135,'EXECUTED','8:5efc1f8e26031e3b01c73abefbd04d97','createIndex indexName=attribute_creator, tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-138','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',136,'EXECUTED','8:9685bc73a05b10f134eb4094de4454fd','createIndex indexName=attribute_is_searchable, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-139','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',137,'EXECUTED','8:8ee4bc666d3f825b7fd1b219483b31c9','createIndex indexName=attribute_type_changer, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-140','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',138,'EXECUTED','8:2d33f303d66728b36c2613fbdbf1aaee','createIndex indexName=attribute_type_creator, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-141','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',139,'EXECUTED','8:cdc02e232fe6637a419142acd22d6931','createIndex indexName=attribute_voider, tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-142','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',140,'EXECUTED','8:58d7b7938a86384a410898cb2af13e8c','createIndex indexName=care_setting_changed_by, tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-143','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',141,'EXECUTED','8:bbe2e850c9553c682b0b81a09d28bea2','createIndex indexName=care_setting_creator, tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-144','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',142,'EXECUTED','8:0c9233681e5faa9dd2536b33b8e763a0','createIndex indexName=care_setting_name, tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-145','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',143,'EXECUTED','8:5cf3a54d398cc896218fbf6e3b298995','createIndex indexName=care_setting_retired_by, tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-146','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',144,'EXECUTED','8:6e0c003dc134e780289f4bbed82aa8ac','createIndex indexName=category_order_set_fk, tableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-147','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',145,'EXECUTED','8:f09e2fb2224ce21fcef36d29b130aa92','createIndex indexName=cohort_creator, tableName=cohort','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-148','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',146,'EXECUTED','8:4029038a2a09d5f0815d7e34592c64be','createIndex indexName=cohort_member_creator, tableName=cohort_member','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-149','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',147,'EXECUTED','8:606356b2cb0496204c4d88883957bd25','createIndex indexName=concept_attribute_attribute_type_id_fk, tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-150','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',148,'EXECUTED','8:1e05001c3a7a9e8a9eb97183fc608c2b','createIndex indexName=concept_attribute_changed_by_fk, tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-151','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',149,'EXECUTED','8:5f96f8798e5b7b9da72c45b3dc81ac9d','createIndex indexName=concept_attribute_concept_fk, tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-152','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',150,'EXECUTED','8:c89c712442a89f2d07ea21f6b60ffc1a','createIndex indexName=concept_attribute_creator_fk, tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-153','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',151,'EXECUTED','8:c10f39874bf68963958a72412fe6bcac','createIndex indexName=concept_attribute_type_changed_by_fk, tableName=concept_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-154','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',152,'EXECUTED','8:28845acfb2c6fa84d47ce725c57f8794','createIndex indexName=concept_attribute_type_creator_fk, tableName=concept_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-155','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',153,'EXECUTED','8:edf32967a9d82155a0ea8dd8eb2ab6e1','createIndex indexName=concept_attribute_type_retired_by_fk, tableName=concept_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-156','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',154,'EXECUTED','8:723ad3f4df5d4e3dfd3a85d5ab9b541e','createIndex indexName=concept_attribute_voided_by_fk, tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-157','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',155,'EXECUTED','8:4074c61d6b4dae4ab5df098dc9546d7b','createIndex indexName=concept_class_changed_by, tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-158','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',156,'EXECUTED','8:d2fe72bcd363d8819fae99cfa833b6bb','createIndex indexName=concept_class_creator, tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-159','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',157,'EXECUTED','8:1ac47021261d5d2b0c17358d7f1f31ee','createIndex indexName=concept_class_id, tableName=order_type_class_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-160','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',158,'EXECUTED','8:6e676415380fef7f1ac0d9f9033aca69','createIndex indexName=concept_class_name_index, tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-161','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',159,'EXECUTED','8:38e8f613e1b7a518ba6ed47e80e6f8ca','createIndex indexName=concept_class_retired_status, tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-162','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',160,'EXECUTED','8:b74346ab17f12599f02e5bea00781e31','createIndex indexName=concept_classes, tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-163','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',161,'EXECUTED','8:f302942598e5281d4edaa8eab7b4ade4','createIndex indexName=concept_creator, tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-164','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',162,'EXECUTED','8:8af66bd30f40514f5663229d3f558a52','createIndex indexName=concept_datatype_creator, tableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-165','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',163,'EXECUTED','8:0eb97161e6a7cea441a2ed63e804feaa','createIndex indexName=concept_datatype_name_index, tableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-166','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',164,'EXECUTED','8:988e10b41ee14ab190aabac3111ecc84','createIndex indexName=concept_datatype_retired_status, tableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-167','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',165,'EXECUTED','8:e9d6fb1e47328e7d59c1ddbf308dea70','createIndex indexName=concept_datatypes, tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-168','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',166,'EXECUTED','8:b245869dccd4b5adbc79240f66334db4','createIndex indexName=concept_for_field, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-169','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',167,'EXECUTED','8:b47fb81e5fd807f6999454f313e4fb88','createIndex indexName=concept_for_proposal, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-170','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',168,'EXECUTED','8:aa653faa994913a1a341733b5f6d534f','createIndex indexName=concept_id, tableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-171','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',169,'EXECUTED','8:9d6067722574739babcf829fd7005083','createIndex indexName=concept_map_type_for_drug_reference_map, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-172','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',170,'EXECUTED','8:ff3e34792a09fd668bcca62343ced16b','createIndex indexName=concept_map_type_name, tableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-173','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',171,'EXECUTED','8:446797c99cbd566e631792db3f446eb0','createIndex indexName=concept_name_changed_by, tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-174','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',172,'EXECUTED','8:fe80f22f46e05ec71478e7ce7c665681','createIndex indexName=concept_name_tag_changed_by, tableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-175','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',173,'EXECUTED','8:97a9fac7c24c6ceb695c173082b6de53','createIndex indexName=concept_reference_source_changed_by, tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-176','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',174,'EXECUTED','8:983653514a7c9e3c9e7cb3f06f845759','createIndex indexName=concept_reference_term_for_drug_reference_map, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-177','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',175,'EXECUTED','8:11669081562535928754bc5bcc11a180','createIndex indexName=concept_source_creator, tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-178','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',176,'EXECUTED','8:87a8f1b58cf4f932acdd05c7e1590ff4','createIndex indexName=concept_triggers_conversion, tableName=concept_state_conversion','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-179','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',177,'EXECUTED','8:7369c064030a14e98be1c1b9dd0680bb','createIndex indexName=condition_changed_by_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-180','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',178,'EXECUTED','8:b3b1a267e2f70b822a9ea1dfe6755b1e','createIndex indexName=condition_condition_coded_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-181','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',179,'EXECUTED','8:babb1074f32abb1122b552ca0eda2abd','createIndex indexName=condition_condition_coded_name_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-182','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',180,'EXECUTED','8:e2bda57c42c5a9bf7c854a67b55bf3c7','createIndex indexName=condition_creator_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-183','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',181,'EXECUTED','8:ee4160d92a93933a9b2d17ff9f6b60c5','createIndex indexName=condition_patient_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-184','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',182,'EXECUTED','8:1aa6149355334eb5629ba66c9f4e8488','createIndex indexName=condition_previous_version_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-185','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',183,'EXECUTED','8:a0f5c5a8139591a6dfaa0ebdcb1abf91','createIndex indexName=condition_voided_by_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-186','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',184,'EXECUTED','8:6caefd25b4504bd5cc1cc4b0d50ac80c','createIndex indexName=conditions_encounter_id_fk, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-187','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',185,'EXECUTED','8:9036796f0a8298a97e6ebbbb44f60269','createIndex indexName=conversion_to_state, tableName=concept_state_conversion','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-188','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',186,'EXECUTED','8:ca5f8b4206708c7a0b265ec52b2036f1','createIndex indexName=defines_attribute_type, tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-189','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',187,'EXECUTED','8:0e8c8a71f1d7106ab0ee2b7a4e9c1a8e','createIndex indexName=defines_identifier_type, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-190','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',188,'EXECUTED','8:21f248120c7e245a62a62a73f0bca3ac','createIndex indexName=description_for_concept, tableName=concept_description','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-191','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',189,'EXECUTED','8:a425094ca65a5c6823ba530b2219a3db','createIndex indexName=diagnosis_attribute_attribute_type_id_fk, tableName=diagnosis_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-192','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',190,'EXECUTED','8:a03ca46bc4a4838ecb353c6c4cfdfd6d','createIndex indexName=diagnosis_attribute_changed_by_fk, tableName=diagnosis_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-193','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',191,'EXECUTED','8:4d0896e9907e1f0a6d2aa6c12add5ada','createIndex indexName=diagnosis_attribute_creator_fk, tableName=diagnosis_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-194','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',192,'EXECUTED','8:5140e33065ca1a07f86c7ad04de2928e','createIndex indexName=diagnosis_attribute_diagnosis_fk, tableName=diagnosis_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-195','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',193,'EXECUTED','8:b79a41a887e2ae08de5f96d39a48a951','createIndex indexName=diagnosis_attribute_type_changed_by_fk, tableName=diagnosis_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-196','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',194,'EXECUTED','8:7a9ce1dc9d1f7c45c09dd7d7949835de','createIndex indexName=diagnosis_attribute_type_creator_fk, tableName=diagnosis_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-197','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',195,'EXECUTED','8:6c9315af11de76cb585f74a3b1b9542e','createIndex indexName=diagnosis_attribute_type_retired_by_fk, tableName=diagnosis_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-198','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',196,'EXECUTED','8:579710cf0110a6a3b1c1de0d4dee217c','createIndex indexName=diagnosis_attribute_voided_by_fk, tableName=diagnosis_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-199','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',197,'EXECUTED','8:3947bfa70f2dd628ef3693ef16865e27','createIndex indexName=discontinued_because, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-200','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',198,'EXECUTED','8:16bae96e0a9575b80f7436b65b3c901f','createIndex indexName=dosage_form_concept, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-201','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',199,'EXECUTED','8:aa2f3c125c218015850faf6902216f15','createIndex indexName=drug_changed_by, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-202','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',200,'EXECUTED','8:7923f05b615200e9a0074c5891d30bf9','createIndex indexName=drug_creator, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-203','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',201,'EXECUTED','8:12c2cab925c834a4e3cfd42d35177396','createIndex indexName=drug_dose_limit_units_fk, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-204','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',202,'EXECUTED','8:5504067b12cf6ae817487a6ca5d52d1d','createIndex indexName=drug_for_drug_reference_map, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-205','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',203,'EXECUTED','8:b3ff030e8d493d861782b7f31a050e0f','createIndex indexName=drug_ingredient_ingredient_id_fk, tableName=drug_ingredient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-206','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',204,'EXECUTED','8:bb3a92181d3b62e7473b880f3d1091c8','createIndex indexName=drug_ingredient_units_fk, tableName=drug_ingredient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-207','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:34',205,'EXECUTED','8:2247879088e4582d25b5375883a3831b','createIndex indexName=drug_order_dose_units, tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-208','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',206,'EXECUTED','8:a120dbdf3e345dca7ed6ad9f16ee5ee2','createIndex indexName=drug_order_duration_units_fk, tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-209','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',207,'EXECUTED','8:3b8d08a476c29d5fd436f49598aeff80','createIndex indexName=drug_order_frequency_fk, tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-210','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',208,'EXECUTED','8:6abde870ce9c60dbf78776b3ab7b0747','createIndex indexName=drug_order_quantity_units, tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-211','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',209,'EXECUTED','8:5962c1ccc35a4201ad511c5972e4d773','createIndex indexName=drug_order_route_fk, tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-212','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',210,'EXECUTED','8:194d1348aad83d4663ff157b6980811b','createIndex indexName=drug_reference_map_creator, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-213','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',211,'EXECUTED','8:7197cb961f010b43e23b0f22fcc8f111','createIndex indexName=drug_retired_by, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-214','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',212,'EXECUTED','8:31c423c71c17e8760f26658c86d6f6e3','createIndex indexName=encounter_changed_by, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-215','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',213,'EXECUTED','8:4afd4079efd3ff663172a71a656cb0af','createIndex indexName=encounter_datetime_idx, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-216','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',214,'EXECUTED','8:dede34974de7f23b44244760df684f5a','createIndex indexName=encounter_diagnosis_changed_by_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-217','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',215,'EXECUTED','8:ce1e96338f2fd38358218a1b00930ecf','createIndex indexName=encounter_diagnosis_coded_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-218','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',216,'EXECUTED','8:4cf956666910b129c0ba36fc14010d11','createIndex indexName=encounter_diagnosis_coded_name_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-219','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',217,'EXECUTED','8:18e31313b0721289a6b9f980c3483ecc','createIndex indexName=encounter_diagnosis_condition_id_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-220','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',218,'EXECUTED','8:558a51844de3da4468a37e556f88550c','createIndex indexName=encounter_diagnosis_creator_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-221','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',219,'EXECUTED','8:ba1b9af8999b961c8bd24b959adda690','createIndex indexName=encounter_diagnosis_encounter_id_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-222','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',220,'EXECUTED','8:4751d44aad47533f7134557462f454d1','createIndex indexName=encounter_diagnosis_patient_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-223','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',221,'EXECUTED','8:517763278ae916a8f65aeb738b9071cb','createIndex indexName=encounter_diagnosis_voided_by_fk, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-224','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',222,'EXECUTED','8:82914cb547a4e291a05eff545a62e372','createIndex indexName=encounter_for_proposal, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-225','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',223,'EXECUTED','8:dfea9f5ed02652ffc5e82180f2b5992e','createIndex indexName=encounter_form, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-226','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',224,'EXECUTED','8:6a5edd213c96575bd6da0b995778673c','createIndex indexName=encounter_ibfk_1, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-227','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',225,'EXECUTED','8:dda7f3b1c8972a907b426fd36ba0602e','createIndex indexName=encounter_id_fk, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-228','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',226,'EXECUTED','8:1b085e5a93b7f65318144bdce78c779a','createIndex indexName=encounter_location, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-229','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',227,'EXECUTED','8:3c53772159fdf4203993da96963deb97','createIndex indexName=encounter_note, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-230','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',228,'EXECUTED','8:4336db4180aeb8bb560bc2d8c97f6a73','createIndex indexName=encounter_observations, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-231','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',229,'EXECUTED','8:c12352d4cf4efd3d17dd66f10d27d583','createIndex indexName=encounter_patient, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-232','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',230,'EXECUTED','8:672cc0e9cd6fea831298ed3ffaf45b10','createIndex indexName=encounter_provider_changed_by, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-233','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',231,'EXECUTED','8:d77e3dc635af12b46d4053e839f9e56a','createIndex indexName=encounter_provider_creator, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-234','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',232,'EXECUTED','8:eda86bd1660dca81f84a0dd9779884f4','createIndex indexName=encounter_provider_voided_by, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-235','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',233,'EXECUTED','8:a0c736f940893bf5c0a56c15d99d7e21','createIndex indexName=encounter_role_changed_by_fk, tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-236','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',234,'EXECUTED','8:3b49284874dfe876e87273e8f2688bed','createIndex indexName=encounter_role_creator_fk, tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-237','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',235,'EXECUTED','8:a4a01769a58e5cd2dbd901d9b72c6dac','createIndex indexName=encounter_role_id_fk, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-238','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',236,'EXECUTED','8:3ab01680a3b2e784e3616c4b25600974','createIndex indexName=encounter_role_name, tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-239','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',237,'EXECUTED','8:4d72025c8c4bac3cd6be806e49d67b7c','createIndex indexName=encounter_role_retired_by_fk, tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-240','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',238,'EXECUTED','8:9d45a4c24d04a5d65220c7f89899c24a','createIndex indexName=encounter_type_changed_by, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-241','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',239,'EXECUTED','8:c5a4bab1f539bd4ced495c005c1a6f7f','createIndex indexName=encounter_type_id, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-242','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',240,'EXECUTED','8:1cbf454f90f0af4bc73ee5fb11dd05ee','createIndex indexName=encounter_type_name, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-243','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',241,'EXECUTED','8:06769795c33557f4889394fc30179450','createIndex indexName=encounter_type_retired_status, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-244','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',242,'EXECUTED','8:1a50f14ad002e48115436ef3665f4c43','createIndex indexName=encounter_visit_id_fk, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-245','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',243,'EXECUTED','8:d37207a166aaca60d194ee966bc7e120','createIndex indexName=family_name2, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-246','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',244,'EXECUTED','8:d4aa398734c837519572e6fcb143f355','createIndex indexName=field_answer_concept, tableName=field_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-247','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',245,'EXECUTED','8:5dd4c8d34f9c8e04b99ccb7c2d12598f','createIndex indexName=field_retired_status, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-248','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',246,'EXECUTED','8:3b5c94c711a49af9fb71d08971423bb8','createIndex indexName=field_within_form, tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-249','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',247,'EXECUTED','8:38c45e1e9f4030e9e93022f6dc4ed350','createIndex indexName=first_name, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-250','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',248,'EXECUTED','8:5fcbc0fefca2b7bb36b0d15a0eb99e8f','createIndex indexName=fk_orderer_provider, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-251','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',249,'EXECUTED','8:92329d4c3a194f8543b93d67fccecd19','createIndex indexName=form_containing_field, tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-252','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',250,'EXECUTED','8:a8c8db5b52aa3122f0d65888b7700398','createIndex indexName=form_encounter_type, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-253','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',251,'EXECUTED','8:5fd7daba08733913b3e5dcf044c4a82a','createIndex indexName=form_field_hierarchy, tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-254','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',252,'EXECUTED','8:c8a1b0aeda085a3c343812e6e2daa18d','createIndex indexName=form_published_and_retired_index, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-255','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',253,'EXECUTED','8:024161f440c8e3fbbf85b098b439ae3f','createIndex indexName=form_published_index, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-256','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',254,'EXECUTED','8:4c6571095e0adeb94f37286e43b5b9d2','createIndex indexName=form_resource_changed_by, tableName=form_resource','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-257','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',255,'EXECUTED','8:023c3e2259f8bb56b2a1cb4a8c35a867','createIndex indexName=form_retired_index, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-258','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',256,'EXECUTED','8:077ed81c7d1f405c4ac4eac7e218fa43','createIndex indexName=global_property_changed_by, tableName=global_property','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-259','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',257,'EXECUTED','8:43a5867d586a7bb3465aa89d95198815','createIndex indexName=has_a, tableName=concept_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-260','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',258,'EXECUTED','8:3d9ea542a05ba93fc2ccbdf08ccaa447','createIndex indexName=hl7_code, tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-261','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',259,'EXECUTED','8:117418151837815057613901205a080b','createIndex indexName=hl7_in_archive_message_state_idx, tableName=hl7_in_archive','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-262','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',260,'EXECUTED','8:6d3741d2da44192d82d16931f1ac6ac3','createIndex indexName=hl7_source_with_queue, tableName=hl7_in_queue','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-263','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',261,'EXECUTED','8:d95090902ac2b5d451f06d171c4cbf17','createIndex indexName=identifier_creator, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-264','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',262,'EXECUTED','8:5f75e8eaf134aa378d6abed32e7ffef2','createIndex indexName=identifier_name, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-265','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',263,'EXECUTED','8:27c4e5b6e65968bc70a736d1c002ee4f','createIndex indexName=identifier_voider, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-266','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',264,'EXECUTED','8:85e6b78d6cd2d2b38288c093ed45a50c','createIndex indexName=identifies_person, tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-267','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',265,'EXECUTED','8:d2ad7f39efadf4ae6e8cc21e028d3ae2','createIndex indexName=idx_code_concept_reference_term, tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-268','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',266,'EXECUTED','8:604f6d74b4b564008d52f4e4724025e0','createIndex indexName=idx_concept_set_concept, tableName=concept_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-269','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',267,'EXECUTED','8:073d2de556dcb0d7069d64fc1ac853ac','createIndex indexName=idx_patient_identifier_patient, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-270','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',268,'EXECUTED','8:a7118f2301f4b24a6342ef5b45ff9310','createIndex indexName=inherited_role, tableName=role_role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-271','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',269,'EXECUTED','8:adebe6e37634bbab5e47e52c6f001b6e','createIndex indexName=inventory_item, tableName=drug_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-272','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',270,'EXECUTED','8:9ab129f6cd9a2eb318aa9b6188e10a05','createIndex indexName=last_name, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-273','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',271,'EXECUTED','8:7116fd0aa0d86192591b5b05562321f7','createIndex indexName=location_attribute_attribute_type_id_fk, tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-274','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',272,'EXECUTED','8:55a0e386a302774eb39a51199113f194','createIndex indexName=location_attribute_changed_by_fk, tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-275','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',273,'EXECUTED','8:73ff410750d5bd6be3065cf8872da7ad','createIndex indexName=location_attribute_creator_fk, tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-276','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',274,'EXECUTED','8:49f63762203223d15f120ef9e10e0d37','createIndex indexName=location_attribute_location_fk, tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-277','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',275,'EXECUTED','8:9f081da2d2b5de2bde1c4fffa582c2ff','createIndex indexName=location_attribute_type_changed_by_fk, tableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-278','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',276,'EXECUTED','8:dc08777f447f2daa7b3882f86c1bc70e','createIndex indexName=location_attribute_type_creator_fk, tableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-279','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',277,'EXECUTED','8:20c35381d445f3c1114a4efda23dbc3e','createIndex indexName=location_attribute_type_name, tableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-280','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',278,'EXECUTED','8:08cd9b26671410fa217fcbda4a00df29','createIndex indexName=location_attribute_type_retired_by_fk, tableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-281','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',279,'EXECUTED','8:a8104ab3f954e872c1e57069d16b49ba','createIndex indexName=location_attribute_voided_by_fk, tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-282','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',280,'EXECUTED','8:f7bfde79464fa0eef95da29ea2ce350e','createIndex indexName=location_changed_by, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-283','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',281,'EXECUTED','8:da61c541398842b05fb3fdc5a2d44825','createIndex indexName=location_retired_status, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-284','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',282,'EXECUTED','8:a76d70c3f03f3db902cc9fb070564974','createIndex indexName=location_tag_changed_by, tableName=location_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-285','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',283,'EXECUTED','8:c1e4a003467d3a7586507788c79d6632','createIndex indexName=location_tag_creator, tableName=location_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-286','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',284,'EXECUTED','8:40cceeab104549fd7756a182c7b31fce','createIndex indexName=location_tag_map_tag, tableName=location_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-287','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',285,'EXECUTED','8:af9d4ce6100a5afe145c856afc419037','createIndex indexName=location_tag_retired_by, tableName=location_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-288','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',286,'EXECUTED','8:0ce389e14f1744242918e699babedf9b','createIndex indexName=location_type_fk, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-289','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',287,'EXECUTED','8:b88c90f1f923cde2f9e5b575bc897d24','createIndex indexName=map_creator, tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-290','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',288,'EXECUTED','8:aaec6329d2d4888261e450178f7169e0','createIndex indexName=map_for_concept, tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-291','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',289,'EXECUTED','8:30d99738aea457ba6229a7b6f5e20cbd','createIndex indexName=mapped_concept_map_type, tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-292','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',290,'EXECUTED','8:10e3e6f6299728e62066649948857d9a','createIndex indexName=mapped_concept_map_type_ref_term_map, tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-293','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',291,'EXECUTED','8:51f56c62985729cfea11d79bef2c188b','createIndex indexName=mapped_concept_name, tableName=concept_name_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-294','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',292,'EXECUTED','8:d83c81bd65c2723b6e2db5d44a0c99b1','createIndex indexName=mapped_concept_name_tag, tableName=concept_name_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-295','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',293,'EXECUTED','8:b0c3a30b6d83eba35cc271b97bf2b88f','createIndex indexName=mapped_concept_proposal, tableName=concept_proposal_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-296','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',294,'EXECUTED','8:a3c3c7616093618de7f90a3bac2d27bc','createIndex indexName=mapped_concept_proposal_tag, tableName=concept_proposal_tag_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-297','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',295,'EXECUTED','8:17a6d1ea0726018afcd68f60d77159f4','createIndex indexName=mapped_concept_reference_term, tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-298','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',296,'EXECUTED','8:9a0d34b3c6b7086cbc977a8be7baaff0','createIndex indexName=mapped_concept_source, tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-299','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',297,'EXECUTED','8:b550cb7b0906479412351e273260ad7f','createIndex indexName=mapped_term_a, tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-300','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',298,'EXECUTED','8:6e11a8dd0c3441dadbe67e7c9c68bf42','createIndex indexName=mapped_term_b, tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-301','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',299,'EXECUTED','8:76d42b1c3a55e113b21aa47ccf41907e','createIndex indexName=mapped_user_changed, tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-302','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',300,'EXECUTED','8:19b3d065a2f192cbe7698cdfdfec4eb0','createIndex indexName=mapped_user_changed_concept_map_type, tableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-303','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',301,'EXECUTED','8:46f0c2de79abb084376f3d0dd760bbd5','createIndex indexName=mapped_user_changed_ref_term, tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-304','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',302,'EXECUTED','8:a2cf7b4567d193532eaefe2ce29aff62','createIndex indexName=mapped_user_changed_ref_term_map, tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-305','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',303,'EXECUTED','8:64ea4ebae8ea496a009d1f525367370a','createIndex indexName=mapped_user_creator, tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-306','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',304,'EXECUTED','8:b3435d6dd1b7880e93aa5edcc74846c7','createIndex indexName=mapped_user_creator_concept_map_type, tableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-307','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',305,'EXECUTED','8:3dc54f80ef612321c91462eccd3a55ec','createIndex indexName=mapped_user_creator_ref_term_map, tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-308','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',306,'EXECUTED','8:0a3503af05a7bd7897d95be10c3b3688','createIndex indexName=mapped_user_retired, tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-309','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',307,'EXECUTED','8:31d72025fbec4406f9f1cb7c1f033cdb','createIndex indexName=mapped_user_retired_concept_map_type, tableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-310','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',308,'EXECUTED','8:aad8af5f9b037c94eac525a386375b06','createIndex indexName=member_patient, tableName=cohort_member','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-311','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',309,'EXECUTED','8:bc24e713e119554b17147a3f6a2dfb93','createIndex indexName=middle_name, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-312','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',310,'EXECUTED','8:bfe1ca8b8a5e3d68f95a62511813e18b','createIndex indexName=diagnosis_attribute_type_name, tableName=diagnosis_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-313','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',311,'EXECUTED','8:d8b9d7efd2834bf2f3a5914f33bfb4ae','createIndex indexName=order_attribute_type_name, tableName=order_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-314','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',312,'EXECUTED','8:4ee87d8b126bd60cc9275e7f68c8015d','createIndex indexName=name_for_concept, tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-315','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',313,'EXECUTED','8:b9d77080d671f9befc731734d1082d2c','createIndex indexName=name_for_person, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-316','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',314,'EXECUTED','8:e1cf3551a6093982be076e202c877bea','createIndex indexName=name_of_attribute, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-317','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',315,'EXECUTED','8:f20bfdf41671ea836878754bf85cefaf','createIndex indexName=name_of_concept, tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-318','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',316,'EXECUTED','8:6ab3606ad0d1274f0f855eb01769c936','createIndex indexName=name_of_location, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-319','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',317,'EXECUTED','8:04a2e1643f8c67411cae4177290f22b5','createIndex indexName=note_hierarchy, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-320','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',318,'EXECUTED','8:502b5e5c51859e2eec91b875bc9abcb0','createIndex indexName=obs_concept, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-321','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',319,'EXECUTED','8:722b0dba4b61dcab30b52c887cf9e0b8','createIndex indexName=obs_datetime_idx, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-322','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',320,'EXECUTED','8:dce7042a64be9d3a55a5cbcf432e7bf5','createIndex indexName=obs_enterer, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-323','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',321,'EXECUTED','8:2c33da9c17d2eaeaee93fc90591b033d','createIndex indexName=obs_grouping_id, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-324','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',322,'EXECUTED','8:d84954ea35e785f43984207b617ed02f','createIndex indexName=obs_location, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-325','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',323,'EXECUTED','8:b5a33a8491f791f267384902aa798522','createIndex indexName=obs_name_of_coded_value, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-326','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',324,'EXECUTED','8:83f5de226100f7e8c6ee067396be02a7','createIndex indexName=obs_note, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-327','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',325,'EXECUTED','8:a192ac50b1fd406192f9924692d4b796','createIndex indexName=obs_order, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-328','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',326,'EXECUTED','8:ca77b396969e50349f2e953bcd59f282','createIndex indexName=order_attribute_attribute_type_id_fk, tableName=order_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-329','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',327,'EXECUTED','8:ce5748bd9044197665d63a2d57050b7b','createIndex indexName=order_attribute_changed_by_fk, tableName=order_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-330','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',328,'EXECUTED','8:97f700145ada01d59d72281531a67376','createIndex indexName=order_attribute_creator_fk, tableName=order_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-331','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',329,'EXECUTED','8:895562bfd364ff58a87efd0dd3c9fa49','createIndex indexName=order_attribute_order_fk, tableName=order_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-332','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',330,'EXECUTED','8:f7ede3dc9dee8303b0a87cc753cd8485','createIndex indexName=order_attribute_type_changed_by_fk, tableName=order_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-333','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',331,'EXECUTED','8:6bbd72d40fe3d5fae98ad93584c3b4f0','createIndex indexName=order_attribute_type_creator_fk, tableName=order_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-334','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',332,'EXECUTED','8:227383b43cce9fe1b2eb98d75b84847e','createIndex indexName=order_attribute_type_retired_by_fk, tableName=order_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-335','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',333,'EXECUTED','8:d6be404eecdbb20446a1f6088d461b36','createIndex indexName=order_attribute_voided_by_fk, tableName=order_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-336','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',334,'EXECUTED','8:0c18f787da491494b46284585cd17820','createIndex indexName=order_creator, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-337','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',335,'EXECUTED','8:1667c90052e388e32df606174648c13a','createIndex indexName=order_for_patient, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-338','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',336,'EXECUTED','8:c7b2f400292b0f1b2b3371c9c64e46f1','createIndex indexName=order_frequency_changed_by_fk, tableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-339','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',337,'EXECUTED','8:9dce146c0bdb56e141c57d01f0a3d7a6','createIndex indexName=order_frequency_creator_fk, tableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-340','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',338,'EXECUTED','8:87f2104877646004907ff88af1d0f110','createIndex indexName=order_frequency_retired_by_fk, tableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-341','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:35',339,'EXECUTED','8:5fe67bd43afa9b0317903ee93bc5f08d','createIndex indexName=order_group_attribute_attribute_type_id_fk, tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-342','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',340,'EXECUTED','8:ecf11fb07af574de5372262eb6503ac2','createIndex indexName=order_group_attribute_changed_by_fk, tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-343','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',341,'EXECUTED','8:e45b69b5bcf322d09c0d1278309eb948','createIndex indexName=order_group_attribute_creator_fk, tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-344','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',342,'EXECUTED','8:43f137218504b56e701f37bc7c0befe1','createIndex indexName=order_group_attribute_order_group_fk, tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-345','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',343,'EXECUTED','8:88eae2ffeaa73ece1e7e4197292b6d94','createIndex indexName=order_group_attribute_type_changed_by_fk, tableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-346','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',344,'EXECUTED','8:cdd83fa85b9bc40873ef92d41b140a11','createIndex indexName=order_group_attribute_type_creator_fk, tableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-347','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',345,'EXECUTED','8:70071e4c97e8754c6ecb480470a699ee','createIndex indexName=order_group_attribute_type_name, tableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-348','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',346,'EXECUTED','8:ef1db190e7105a37eec6a5591bb201e6','createIndex indexName=order_group_attribute_type_retired_by_fk, tableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-349','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',347,'EXECUTED','8:172db383f51e7e0059132c1b9fcdf18c','createIndex indexName=order_group_attribute_voided_by_fk, tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-350','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',348,'EXECUTED','8:a6d743bf0f9e2a56558455fa5a1d87fa','createIndex indexName=order_group_changed_by_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-351','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',349,'EXECUTED','8:a1aafa6be68646d6461ea2ffbf1a8eaa','createIndex indexName=order_group_creator_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-352','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',350,'EXECUTED','8:1fd62bd470c97ec9131e196ac9cb2653','createIndex indexName=order_group_encounter_id_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-353','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',351,'EXECUTED','8:e23d69bd9fbbf155956ff2d406713005','createIndex indexName=order_group_order_group_reason_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-354','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',352,'EXECUTED','8:8404f1bc8833e8609f3bd5cb0bdd818a','createIndex indexName=order_group_parent_order_group_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-355','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',353,'EXECUTED','8:b5d584dae229bb43b3d5dfac96ff4baa','createIndex indexName=order_group_patient_id_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-356','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',354,'EXECUTED','8:7d42fc3b2f07fc51dc70ad09ab1f8534','createIndex indexName=order_group_previous_order_group_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-357','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',355,'EXECUTED','8:3f6b7823a7e57010dc4b96a25697e015','createIndex indexName=order_group_set_id_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-358','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',356,'EXECUTED','8:a2b48e2bbe4c1757ae3a99938d81eac5','createIndex indexName=order_group_voided_by_fk, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-359','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',357,'EXECUTED','8:6aab691f5f0bfd66f72c75285215c0e7','createIndex indexName=order_set_attribute_attribute_type_id_fk, tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-360','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',358,'EXECUTED','8:f2eeaa7c0f8b667588f39b99a35fd08e','createIndex indexName=order_set_attribute_changed_by_fk, tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-361','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',359,'EXECUTED','8:4dd3631c767fb038eadbc0ffaceb3f86','createIndex indexName=order_set_attribute_creator_fk, tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-362','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',360,'EXECUTED','8:f0f868a21d97bccc759154377933ace0','createIndex indexName=order_set_attribute_order_set_fk, tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-363','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',361,'EXECUTED','8:9b714141c53fee3517b11f78f3b7dfbc','createIndex indexName=order_set_attribute_type_changed_by_fk, tableName=order_set_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-364','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',362,'EXECUTED','8:e89fed18056235fbe9886fb152a2ba02','createIndex indexName=order_set_attribute_type_creator_fk, tableName=order_set_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-365','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',363,'EXECUTED','8:7c57da9ce702aef6a97782d52ee58f7e','createIndex indexName=order_set_attribute_type_retired_by_fk, tableName=order_set_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-366','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',364,'EXECUTED','8:468a4c6b240eb183db2abeaac785224f','createIndex indexName=order_set_attribute_voided_by_fk, tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-367','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',365,'EXECUTED','8:a5912184a23044851e8ef42a1b48a7e4','createIndex indexName=order_set_changed_by_fk, tableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-368','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',366,'EXECUTED','8:fb89530b109069ba8be1c3bb717c2121','createIndex indexName=order_set_creator_fk, tableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-369','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',367,'EXECUTED','8:a9f9b14070164cc2407c05f8d78b0aa2','createIndex indexName=order_set_member_changed_by_fk, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-370','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',368,'EXECUTED','8:9e380798192199cc558dcf76b86ee383','createIndex indexName=order_set_member_concept_id_fk, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-371','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',369,'EXECUTED','8:ab47cf75996ecd79d4f3e7d51add4f85','createIndex indexName=order_set_member_creator_fk, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-372','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',370,'EXECUTED','8:a4486ddcf3e2bfe7c2af98f689f999f0','createIndex indexName=order_set_member_order_set_id_fk, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-373','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',371,'EXECUTED','8:c33913fec57162ef1d536665aefaa1c4','createIndex indexName=order_set_member_order_type_fk, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-374','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',372,'EXECUTED','8:2038b70c44473ef106edbf302edd26a4','createIndex indexName=order_set_member_retired_by_fk, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-375','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',373,'EXECUTED','8:e6296cd5b7a15e29ffbcde45a4cd0268','createIndex indexName=order_set_retired_by_fk, tableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-376','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',374,'EXECUTED','8:8cd746d20e2d3f0cfb478b8fbb3b7976','createIndex indexName=order_type_changed_by, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-377','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',375,'EXECUTED','8:0231ecea82c82cda827ab3f001b62328','createIndex indexName=order_type_name, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-378','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',376,'EXECUTED','8:f2dc37c519d6795fd871c18435980f70','createIndex indexName=order_type_parent_order_type, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-379','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',377,'EXECUTED','8:644ef92f3d47d365532eff9953db9793','createIndex indexName=order_type_retired_status, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-380','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',378,'EXECUTED','8:ebab8d61ca65d04b73dbed42acebbe6e','createIndex indexName=orders_accession_number, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-381','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',379,'EXECUTED','8:ecd6c3abe3ba7670d05bb4f66036e2d0','createIndex indexName=orders_care_setting, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-382','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',380,'EXECUTED','8:d43c7046b4cc96fb2bab5c015230ea82','createIndex indexName=orders_in_encounter, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-383','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',381,'EXECUTED','8:411231af101070ffc908d7c86640b5de','createIndex indexName=orders_order_group_id_fk, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-384','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',382,'EXECUTED','8:b19d78a89af7cbbc1146acfdc3bc9ee5','createIndex indexName=orders_order_number, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-385','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',383,'EXECUTED','8:b015ed6aaed23eda9c8e04dad894a859','createIndex indexName=parent_cohort, tableName=cohort_member','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-386','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',384,'EXECUTED','8:7bd03f793dfec28e378b4c7b5f193b83','createIndex indexName=parent_location, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-387','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',385,'EXECUTED','8:e9b88deaeac31b0432fbd4d4d483691e','createIndex indexName=patient_address_creator, tableName=person_address','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-388','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',386,'EXECUTED','8:4afe3b113bc16965b318ebd554a09bc8','createIndex indexName=patient_address_void, tableName=person_address','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-389','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',387,'EXECUTED','8:ace943334658479bf77701411c6be732','createIndex indexName=patient_identifier_changed_by, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-390','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',388,'EXECUTED','8:6d42130aab478c32de45a24a4bac2a24','createIndex indexName=patient_identifier_ibfk_2, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-391','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',389,'EXECUTED','8:95435cb6c7ff578eb26059b6a7321f72','createIndex indexName=patient_identifier_type_changed_by, tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-392','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',390,'EXECUTED','8:38fdd8b1bdea75698ea31430790b340e','createIndex indexName=patient_identifier_type_retired_status, tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-393','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',391,'EXECUTED','8:dcfc5706248f1defc7ea124dbef8ec14','createIndex indexName=patient_in_program, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-394','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',392,'EXECUTED','8:0ef5c8042260eb5634b1665a3f970dc8','createIndex indexName=patient_note, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-395','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',393,'EXECUTED','8:f6981472e24eb69ed6a696561588fd0e','createIndex indexName=patient_program_attribute_attributetype_fk, tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-396','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',394,'EXECUTED','8:f449c5e93c69745dc202a964eb95b43f','createIndex indexName=patient_program_attribute_changed_by_fk, tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-397','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',395,'EXECUTED','8:2beba5b0be222efca6a121d118a4ab09','createIndex indexName=patient_program_attribute_creator_fk, tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-398','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',396,'EXECUTED','8:958d8644d04dcbe96aad90bb7ca4eed6','createIndex indexName=patient_program_attribute_programid_fk, tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-399','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',397,'EXECUTED','8:4b4e07cc31c923fa37f0caaa8bf323df','createIndex indexName=patient_program_attribute_voided_by_fk, tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-400','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',398,'EXECUTED','8:ef920ed5a34363bd9ece75f6eb9071d7','createIndex indexName=patient_program_creator, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-401','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',399,'EXECUTED','8:2ce117f1b4c309dd58b48915890bba97','createIndex indexName=patient_program_for_state, tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-402','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',400,'EXECUTED','8:5e6d0c02499f229cf931498b8c6ae898','createIndex indexName=patient_program_location_id, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-403','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',401,'EXECUTED','8:e28c5452f1d034a0ad4f3cf5a0f1df95','createIndex indexName=patient_program_outcome_concept_id_fk, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-404','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',402,'EXECUTED','8:e1cab1a84a4e9594b95d9eb6e9643932','createIndex indexName=patient_state_changer, tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-405','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',403,'EXECUTED','8:59094c6a8698fa471c9b14945960111a','createIndex indexName=patient_state_creator, tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-406','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',404,'EXECUTED','8:50062302dd49f3d1517953e3a721bb43','createIndex indexName=patient_state_encounter_id_fk, tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-407','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',405,'EXECUTED','8:f61e718deb4467f134397c85a65cd48c','createIndex indexName=patient_state_voider, tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-408','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',406,'EXECUTED','8:c4934fbf7bce4899a852b7e2ac7a0c62','createIndex indexName=person_a_is_person, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-409','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',407,'EXECUTED','8:b66c5278a9277f8055bf6e303d4276e8','createIndex indexName=person_address_changed_by, tableName=person_address','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-410','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',408,'EXECUTED','8:f3ec96539a9c6fa4a8e93f2387d65b25','createIndex indexName=person_attribute_type_retired_status, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-411','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',409,'EXECUTED','8:32c296ff73a60f3b9f44519f4e05ada3','createIndex indexName=person_b_is_person, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-412','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',410,'EXECUTED','8:0ffc099ac596e6a1e0623448a12d32b7','createIndex indexName=person_birthdate, tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-413','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',411,'EXECUTED','8:5d335cd48c9c3622ecdbc9398fba5a49','createIndex indexName=person_death_date, tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-414','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',412,'EXECUTED','8:fa9376ea58b6f400fd7b37c634f8439e','createIndex indexName=person_died_because, tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-415','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',413,'EXECUTED','8:af83ca3362c3219a38a424b25e6cb9a0','createIndex indexName=person_id_for_user, tableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-416','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',414,'EXECUTED','8:10e7a7009c8a68479c8b5d3a3bd723e1','createIndex indexName=person_merge_log_changed_by_fk, tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-417','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',415,'EXECUTED','8:aeba6374ce79692cc6c4afdbcbdae230','createIndex indexName=person_merge_log_creator, tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-418','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',416,'EXECUTED','8:44d3661ba03f1283fd7ad325dac42206','createIndex indexName=person_merge_log_loser, tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-419','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',417,'EXECUTED','8:c85e6c020114981ada1c3e475de966c4','createIndex indexName=person_merge_log_voided_by_fk, tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-420','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',418,'EXECUTED','8:03ad1a480aa9c0448d3fd383e831beff','createIndex indexName=person_merge_log_winner, tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-421','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',419,'EXECUTED','8:f7b462971b2248a9a77bd476ce5591dc','createIndex indexName=person_obs, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-422','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',420,'EXECUTED','8:87b5dc4984cd482af28b829d4a3411e5','createIndex indexName=previous_order_id_order_id, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-423','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',421,'EXECUTED','8:481f14ae80e10723e8cdd4d957a32b18','createIndex indexName=previous_version, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-424','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',422,'EXECUTED','8:8b988fc20cd05cce87dfca918a67a853','createIndex indexName=primary_drug_concept, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-425','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',423,'EXECUTED','8:8035e065236d99f2120a0e0ae7e00e19','createIndex indexName=privilege_definitions, tableName=role_privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-426','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',424,'EXECUTED','8:569e556b5127fb85bbd2f762b8abcb0d','createIndex indexName=privilege_which_can_edit, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-427','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',425,'EXECUTED','8:b2d66237cf16273ca43a279787abd289','createIndex indexName=privilege_which_can_edit_encounter_type, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-428','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',426,'EXECUTED','8:0bad7afd4996f4532aabecc55c05c6f9','createIndex indexName=privilege_which_can_view_encounter_type, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-429','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',427,'EXECUTED','8:1b95d72f022d870a00e7a1306eaed6af','createIndex indexName=program_attribute_type_changed_by_fk, tableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-430','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',428,'EXECUTED','8:0666dfeda180082f4a8ce20d4672aabb','createIndex indexName=program_attribute_type_creator_fk, tableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-431','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',429,'EXECUTED','8:f7c7ce994adfdd0282914a56626c9397','createIndex indexName=program_attribute_type_name, tableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-432','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',430,'EXECUTED','8:cb4803b0dcbc70a13a2bf54dd7b04c93','createIndex indexName=program_attribute_type_retired_by_fk, tableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-433','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',431,'EXECUTED','8:4fceeddc65527f6aff2db27629515acc','createIndex indexName=program_concept, tableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-434','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',432,'EXECUTED','8:c860ac227d0114f62112a04072c5d8b3','createIndex indexName=program_creator, tableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-435','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',433,'EXECUTED','8:fc67efa5176469047ee0366b3cf61606','createIndex indexName=program_for_patient, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-436','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',434,'EXECUTED','8:802d71f2abb2ed1c9596adaefa80f25a','createIndex indexName=program_for_workflow, tableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-437','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',435,'EXECUTED','8:6de848917d554d43146c6d60ce7c1654','createIndex indexName=program_outcomes_concept_id_fk, tableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-438','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',436,'EXECUTED','8:a40909e40fd6c37a4c77d0db07af0c35','createIndex indexName=proposal_obs_concept_id, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-439','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',437,'EXECUTED','8:97f396dababa47ad282f35d66387010f','createIndex indexName=proposal_obs_id, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-440','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',438,'EXECUTED','8:7878b52a263400367ae2f19ee2ffea2c','createIndex indexName=provider_attribute_attribute_type_id_fk, tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-441','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',439,'EXECUTED','8:b1e716f65494df103352dce39165d467','createIndex indexName=provider_attribute_changed_by_fk, tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-442','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',440,'EXECUTED','8:1a3991173a1a6c1cbedcfd8f38b23d46','createIndex indexName=provider_attribute_creator_fk, tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-443','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',441,'EXECUTED','8:2f926bc27f17c6b5761c743e12bc4704','createIndex indexName=provider_attribute_provider_fk, tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-444','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',442,'EXECUTED','8:c82699b775351d2324ca4f3797bd2aa0','createIndex indexName=provider_attribute_type_changed_by_fk, tableName=provider_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-445','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',443,'EXECUTED','8:c8769a761efe89c1327d1f45c3e6804b','createIndex indexName=provider_attribute_type_creator_fk, tableName=provider_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-446','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',444,'EXECUTED','8:5a015c038de4e1bfb150845cc8dcd1fd','createIndex indexName=provider_attribute_type_retired_by_fk, tableName=provider_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-447','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',445,'EXECUTED','8:071b3b36b034dde1fb3eac03d76dc3c2','createIndex indexName=provider_attribute_voided_by_fk, tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-448','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',446,'EXECUTED','8:28391123e016ecedc9e92aaa7002a9a5','createIndex indexName=provider_changed_by_fk, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-449','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',447,'EXECUTED','8:6150efa0ff3b5fd0ac858125c1a470ce','createIndex indexName=provider_creator_fk, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-450','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',448,'EXECUTED','8:14b0b358db728ed4432e8de5dc0909e4','createIndex indexName=provider_id_fk, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-451','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',449,'EXECUTED','8:60671017da9f7c68ceeb70574da92295','createIndex indexName=provider_person_id_fk, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-452','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',450,'EXECUTED','8:d1b4399b5599817e9b1159107eefe938','createIndex indexName=provider_retired_by_fk, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-453','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',451,'EXECUTED','8:0faeedc4e3cb554ffcfb7d184a9b8ca5','createIndex indexName=provider_role_id_fk, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-454','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',452,'EXECUTED','8:3b20f44b1dbc5ad8f3bd825b45e89984','createIndex indexName=provider_speciality_id_fk, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-455','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',453,'EXECUTED','8:290c3353cb2c99ee5ddd2eff635a17f7','createIndex indexName=referral_order_frequency_index, tableName=referral_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-456','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',454,'EXECUTED','8:4b26048ce45036416476e498b50f3501','createIndex indexName=referral_order_location_fk, tableName=referral_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-457','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',455,'EXECUTED','8:13e25ed0bb786c46433e9d0adf368317','createIndex indexName=referral_order_specimen_source_index, tableName=referral_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-458','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',456,'EXECUTED','8:c1d0126a6bf573cae8fcb664270333c7','createIndex indexName=relation_creator, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-459','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',457,'EXECUTED','8:727751987bccbb0bd271dcb57a46e2f3','createIndex indexName=relation_voider, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-460','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',458,'EXECUTED','8:02c1b06ef9c6be308a00d4df8da55eb6','createIndex indexName=relationship_changed_by, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-461','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',459,'EXECUTED','8:975371d15b7b0a5e52af044c7d3f6a28','createIndex indexName=relationship_type_changed_by, tableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-462','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',460,'EXECUTED','8:2f66bc5bb41570ae45a5ab255469fcd4','createIndex indexName=relationship_type_id, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-463','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',461,'EXECUTED','8:60c176b60dcff5580d0f22a012633b25','createIndex indexName=report_object_creator, tableName=report_object','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-464','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',462,'EXECUTED','8:7c638e546bd699a279d62b0d36489c57','createIndex indexName=role_definitions, tableName=user_role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-465','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',463,'EXECUTED','8:c89862f1966d03c0884d245fdaf4a88f','createIndex indexName=role_privilege_to_role, tableName=role_privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-466','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:36',464,'EXECUTED','8:58a56ac393d2f6c48126a8adba8a9a66','createIndex indexName=route_concept, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-467','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',465,'EXECUTED','8:345907215bab087d0035d000afa9a514','createIndex indexName=scheduler_changer, tableName=scheduler_task_config','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-468','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',466,'EXECUTED','8:8ba6f3cf03f4489d612f25773dcad889','createIndex indexName=scheduler_creator, tableName=scheduler_task_config','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-469','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',467,'EXECUTED','8:99d9e2f3721be928d0c43ca256e12728','createIndex indexName=serialized_object_changed_by, tableName=serialized_object','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-470','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',468,'EXECUTED','8:dc694719e402b67c9639afee8af3b75c','createIndex indexName=serialized_object_creator, tableName=serialized_object','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-471','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',469,'EXECUTED','8:7437d93c8757fb55aa919bf21b456a0f','createIndex indexName=serialized_object_retired_by, tableName=serialized_object','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-472','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',470,'EXECUTED','8:e8e2d575a45d7de72c502acc8d6c006c','createIndex indexName=state_changed_by, tableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-473','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',471,'EXECUTED','8:3f7b636456460832ee260a18eb162a6c','createIndex indexName=state_concept, tableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-474','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',472,'EXECUTED','8:81fc984d8bff19515079254bdfce4802','createIndex indexName=state_creator, tableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-475','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',473,'EXECUTED','8:f2c40d95494570530e077694d500fbba','createIndex indexName=state_for_patient, tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-476','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',474,'EXECUTED','8:c2825227c10c514167c74e537c88a771','createIndex indexName=tag, tableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-477','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',475,'EXECUTED','8:11c673d5fe793dde11022be10f69d93f','createIndex indexName=task_config_for_property, tableName=scheduler_task_config_property','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-478','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',476,'EXECUTED','8:0b3d1d47a36a661b8367b2d90e21e362','createIndex indexName=test_order_frequency_fk, tableName=test_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-479','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',477,'EXECUTED','8:bb84a8f02441d31422134f617b52b49c','createIndex indexName=test_order_location_fk, tableName=test_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-480','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',478,'EXECUTED','8:cee34c547f13180783fb9f02e79a28e0','createIndex indexName=test_order_specimen_source_fk, tableName=test_order','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-481','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',479,'EXECUTED','8:30777b65a5778c2830ab812ea875d684','createIndex indexName=type_created_by, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-482','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',480,'EXECUTED','8:4f750963d8d22945764a40f9349c9bb9','createIndex indexName=type_creator, tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-483','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',481,'EXECUTED','8:72c791b5132cf0715a6f75e6339c2780','createIndex indexName=type_of_field, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-484','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',482,'EXECUTED','8:2635f2461e75c0f71a74049ee425d379','createIndex indexName=type_of_order, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-485','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',483,'EXECUTED','8:188adf59d09e6066878534296f4fa4ac','createIndex indexName=unique_form_and_name, tableName=form_resource','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-486','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',484,'EXECUTED','8:e55261322ad9928cfeab76281217b010','createIndex indexName=unique_id, tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-487','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',485,'EXECUTED','8:016d4ffa55c6a43fa90835b06cd452ef','createIndex indexName=unique_workflow_concept_in_conversion, tableName=concept_state_conversion','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-488','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',486,'EXECUTED','8:98a1414be57317ab27d544e18c5ff1a8','createIndex indexName=user_creator, tableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-489','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',487,'EXECUTED','8:6a79b0e1a0cf53e5fe69cd7e9fdb1a23','createIndex indexName=user_role_to_users, tableName=user_role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-490','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',488,'EXECUTED','8:4916c9528fc492d433ad57cef358518d','createIndex indexName=user_who_changed, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-491','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',489,'EXECUTED','8:2dcbd1f067b5fe3c83c2613efd2d1ce3','createIndex indexName=user_who_changed_alert, tableName=notification_alert','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-492','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',490,'EXECUTED','8:c22cb21d67fe46c765e31421cbdbba78','createIndex indexName=user_who_changed_cohort, tableName=cohort','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-493','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',491,'EXECUTED','8:12e40bf834814c64bd67441cc66450b8','createIndex indexName=user_who_changed_concept, tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-494','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',492,'EXECUTED','8:97f4d48da9b98a9797f11bd2e121e3bb','createIndex indexName=user_who_changed_description, tableName=concept_description','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-495','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',493,'EXECUTED','8:31934f744d48a9d20bbd99af206f3666','createIndex indexName=user_who_changed_drug_reference_map, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-496','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',494,'EXECUTED','8:1b013a4f078f3b18bba0c97bd807be90','createIndex indexName=user_who_changed_field, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-497','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',495,'EXECUTED','8:84e66526131ba5b2f75ef7b0a475cb97','createIndex indexName=user_who_changed_note, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-498','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',496,'EXECUTED','8:3b0867d2fa7c5bbfa2e0151ff998c0a9','createIndex indexName=user_who_changed_pat, tableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-499','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',497,'EXECUTED','8:0122464ab2e25489a251a835254933b2','createIndex indexName=user_who_changed_person, tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-500','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',498,'EXECUTED','8:295451a8413d60bf3a126b7c0a377dec','createIndex indexName=user_who_changed_program, tableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-501','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',499,'EXECUTED','8:2a9363c78f4846cf41045c6f203573b8','createIndex indexName=user_who_changed_proposal, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-502','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',500,'EXECUTED','8:80eaf4b1de0dda76c24bf41ada886534','createIndex indexName=user_who_changed_report_object, tableName=report_object','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-503','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',501,'EXECUTED','8:d08c61d242968617b703fc25d88d2c97','createIndex indexName=user_who_changed_user, tableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-504','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',502,'EXECUTED','8:e9b1959ae3773a7b5381225a307b25a7','createIndex indexName=user_who_created, tableName=concept_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-505','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',503,'EXECUTED','8:dec84946e9ef350fee9bf9e564565ae7','createIndex indexName=user_who_created_description, tableName=concept_description','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-506','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',504,'EXECUTED','8:2267a0e2ef7c5c12e1136d8fc58ee817','createIndex indexName=user_who_created_field, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-507','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',505,'EXECUTED','8:cc980da2d5334af135b31baab720a8b7','createIndex indexName=user_who_created_field_answer, tableName=field_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-508','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',506,'EXECUTED','8:b4e1b4ef31eed23b036e69759ed6d362','createIndex indexName=user_who_created_field_type, tableName=field_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-509','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',507,'EXECUTED','8:4c9ab7f6f116fb6d180165aa4314c4f9','createIndex indexName=user_who_created_form, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-510','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',508,'EXECUTED','8:d1435b63968d4ac2ff7656d2f26c596d','createIndex indexName=user_who_created_form_field, tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-511','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',509,'EXECUTED','8:d60b12848c9dbbf1c87a640ba1f1464e','createIndex indexName=user_who_created_hl7_source, tableName=hl7_source','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-512','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',510,'EXECUTED','8:77e3f63aae12ba9d898a35ab6fe95592','createIndex indexName=user_who_created_location, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-513','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',511,'EXECUTED','8:e5b1e2c2a208fc667cab964139db2a5b','createIndex indexName=user_who_created_name, tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-514','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',512,'EXECUTED','8:482b94f4c8a17498c0ea738a069e3b64','createIndex indexName=user_who_created_name_tag, tableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-515','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',513,'EXECUTED','8:b7ac3e14096bec7ec7488c25b0f3a459','createIndex indexName=user_who_created_note, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-516','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',514,'EXECUTED','8:831f45000783dbdccbd088d9f1fe2b94','createIndex indexName=user_who_created_patient, tableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-517','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',515,'EXECUTED','8:cf226ccbf7971a8a9ca0d248d495ba02','createIndex indexName=user_who_created_person, tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-518','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',516,'EXECUTED','8:35887a2b53f4cdfc1d6bfbdf1f08c935','createIndex indexName=user_who_created_proposal, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-519','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',517,'EXECUTED','8:4f2fdf3f432374182921e1e7549ec23c','createIndex indexName=user_who_created_rel, tableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-520','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',518,'EXECUTED','8:2e9acebbfe25234f77fb27d8073d54a3','createIndex indexName=user_who_created_type, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-521','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',519,'EXECUTED','8:efc6dd67eaa7c5c4d10a3ec3a31b6ffb','createIndex indexName=user_who_last_changed_form, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-522','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',520,'EXECUTED','8:3626af26aefa67355dbdc1443c425dc9','createIndex indexName=user_who_last_changed_form_field, tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-523','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',521,'EXECUTED','8:dba003f29a0c1a018bb4d6b19bb8ea82','createIndex indexName=user_who_made_name, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-524','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',522,'EXECUTED','8:596d63992b7500d2857ff9b591a5867a','createIndex indexName=user_who_retired_concept, tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-525','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',523,'EXECUTED','8:22c383f0c05fdab71781e7a590e548e6','createIndex indexName=user_who_retired_concept_class, tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-526','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',524,'EXECUTED','8:38342c5f7d175b5085fe7779daed6a25','createIndex indexName=user_who_retired_concept_datatype, tableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-527','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',525,'EXECUTED','8:6b4329d504bbeef8d05f63c829474fa5','createIndex indexName=user_who_retired_concept_source, tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-528','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',526,'EXECUTED','8:be03b0886a60a4801fd01afe63879796','createIndex indexName=user_who_retired_drug_reference_map, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-529','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',527,'EXECUTED','8:e16cbf2b5aaefb822e1077ad4752fcd7','createIndex indexName=user_who_retired_encounter_type, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-530','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',528,'EXECUTED','8:cc548a866306eedbab3ba3ccd03d87fa','createIndex indexName=user_who_retired_field, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-531','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',529,'EXECUTED','8:81ce0f64dd3f0a7e91dcdbdeefd2eabf','createIndex indexName=user_who_retired_form, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-532','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',530,'EXECUTED','8:b71173f28e535bf7348e6a3c7557dd7b','createIndex indexName=user_who_retired_location, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-533','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',531,'EXECUTED','8:6372653c2c49f52453fbf46af792d8a8','createIndex indexName=user_who_retired_order_type, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-534','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',532,'EXECUTED','8:d3e96ecd89da6c8fe015b2574708eca0','createIndex indexName=user_who_retired_patient_identifier_type, tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-535','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',533,'EXECUTED','8:d833b27872a872339fc8028162d0cdc8','createIndex indexName=user_who_retired_person_attribute_type, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-536','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',534,'EXECUTED','8:7fd5577476509af77746aecc8b078ab6','createIndex indexName=user_who_retired_relationship_type, tableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-537','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',535,'EXECUTED','8:1d661628f98b448fce38d7aa3e0bede7','createIndex indexName=user_who_retired_this_user, tableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-538','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',536,'EXECUTED','8:5852f80d0e0ef4359873f542c2747a63','createIndex indexName=user_who_voided_cohort, tableName=cohort','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-539','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',537,'EXECUTED','8:ab96b9937398cd4e1449dec3506e528e','createIndex indexName=user_who_voided_encounter, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-540','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',538,'EXECUTED','8:2bf627a24b3a4101ad2cf42aebc6f170','createIndex indexName=user_who_voided_name, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-541','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',539,'EXECUTED','8:988da7b1d6f2c4056885ecfcf0f9a57d','createIndex indexName=user_who_voided_name_tag, tableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-542','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',540,'EXECUTED','8:2219bbfb05637be221bef4bc9a853477','createIndex indexName=user_who_voided_obs, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-543','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',541,'EXECUTED','8:9e19e62c8650a5aefb0e6c2602764586','createIndex indexName=user_who_voided_order, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-544','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',542,'EXECUTED','8:6e61ed99aa6f483b97bd3d01301171f9','createIndex indexName=user_who_voided_patient, tableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-545','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',543,'EXECUTED','8:35bb5780c5059d400cac524f0301abd0','createIndex indexName=user_who_voided_patient_program, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-546','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',544,'EXECUTED','8:714686779f3d31877568eccb91700375','createIndex indexName=user_who_voided_person, tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-547','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',545,'EXECUTED','8:9eeded0707368de50c3ee0add3c2e8b2','createIndex indexName=user_who_voided_report_object, tableName=report_object','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-548','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',546,'EXECUTED','8:3f9860528ccc3a3ae04d7e8aca6f44e1','createIndex indexName=user_who_voided_this_name, tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-549','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',547,'EXECUTED','8:803eaa77c10c4a16e5ecdd194d3dfec1','createIndex indexName=uuid_diagnosis_attribute, tableName=diagnosis_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-550','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',548,'EXECUTED','8:80542bcf2b3a3c33666aabef37cc6f2a','createIndex indexName=uuid_diagnosis_attribute_type, tableName=diagnosis_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-551','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',549,'EXECUTED','8:09f16dcfb203df405d448188c9f5dd7e','createIndex indexName=uuid_order_attribute, tableName=order_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-552','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',550,'EXECUTED','8:9d0100dcf899c720ad37638154ba8693','createIndex indexName=uuid_order_attribute_type, tableName=order_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-553','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',551,'EXECUTED','8:615d8d1c4cc4debb2a34f436262f565c','createIndex indexName=uuid_care_setting, tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-554','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',552,'EXECUTED','8:e886faf65c185292efbfa6abec09e27b','createIndex indexName=uuid_clob_datatype_storage, tableName=clob_datatype_storage','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-555','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',553,'EXECUTED','8:2fcb13bb42ebfe0c32d1cad882519d6c','createIndex indexName=uuid_cohort, tableName=cohort','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-556','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',554,'EXECUTED','8:282138d38a297b81dcecb202d3be291f','createIndex indexName=uuid_cohort_member, tableName=cohort_member','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-557','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',555,'EXECUTED','8:8c034623a2dc5d702102ef1820c03054','createIndex indexName=uuid_concept, tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-558','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',556,'EXECUTED','8:6126ac43911b68657b65ea896c1174c7','createIndex indexName=uuid_concept_answer, tableName=concept_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-559','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',557,'EXECUTED','8:633f1aec7dd087d7e0dbf6d4c372870d','createIndex indexName=uuid_concept_attribute, tableName=concept_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-560','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',558,'EXECUTED','8:4376f445a25c5b5ff11b587c0639301c','createIndex indexName=uuid_concept_attribute_type, tableName=concept_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-561','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',559,'EXECUTED','8:44c0b31fe18bf32850d632db20b2ade0','createIndex indexName=uuid_concept_class, tableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-562','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',560,'EXECUTED','8:d806509f2caf2316e474c4fbe9f15e5b','createIndex indexName=uuid_concept_datatype, tableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-563','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',561,'EXECUTED','8:0444f1ba5fc9a14a35c1581e511e5209','createIndex indexName=uuid_concept_description, tableName=concept_description','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-564','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',562,'EXECUTED','8:06e83d1b5eebbab38d2c5c24c6b9eace','createIndex indexName=uuid_concept_map_type, tableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-565','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',563,'EXECUTED','8:fb9b7c16781ff1fb3986e9f0424c3858','createIndex indexName=uuid_concept_name, tableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-566','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',564,'EXECUTED','8:828e36a31197ecd94b5032a65bd3d51a','createIndex indexName=uuid_concept_name_tag, tableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-567','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',565,'EXECUTED','8:d5182d65882c5bf52efde0c54710448f','createIndex indexName=uuid_concept_proposal, tableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-568','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',566,'EXECUTED','8:4afef575d799fd530d64e0d646609de0','createIndex indexName=uuid_concept_reference_map, tableName=concept_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-569','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',567,'EXECUTED','8:f2b14357b8d781f0430b1af95329a30b','createIndex indexName=uuid_concept_reference_source, tableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-570','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',568,'EXECUTED','8:7be4c5c7012ac8250595eed163936017','createIndex indexName=uuid_concept_reference_term, tableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-571','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',569,'EXECUTED','8:9a569d16757ec4224843e48c43c120d8','createIndex indexName=uuid_concept_reference_term_map, tableName=concept_reference_term_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-572','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',570,'EXECUTED','8:52706fb6763b7d2323047446a66a94ac','createIndex indexName=uuid_concept_set, tableName=concept_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-573','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',571,'EXECUTED','8:cfe4f16b4a2bfee696d853b5c3393e61','createIndex indexName=uuid_concept_state_conversion, tableName=concept_state_conversion','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-574','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',572,'EXECUTED','8:097eee9bab0e09f85e3b189ae35fa2e9','createIndex indexName=uuid_conditions, tableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-575','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',573,'EXECUTED','8:9e1b2910d3b78c7b4a58f9fcbfe738a8','createIndex indexName=uuid_drug, tableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-576','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',574,'EXECUTED','8:4694e6de93f21a4de4ce4c2a1018962d','createIndex indexName=uuid_drug_ingredient, tableName=drug_ingredient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-577','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',575,'EXECUTED','8:2639385f325b1d03008df17fbc53cd1d','createIndex indexName=uuid_drug_reference_map, tableName=drug_reference_map','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-578','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',576,'EXECUTED','8:8d8faeabcc242bca40ac00a86c6ef13a','createIndex indexName=uuid_encounter, tableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-579','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',577,'EXECUTED','8:406c4da214dc0a5d96b56445825a45d9','createIndex indexName=uuid_encounter_diagnosis, tableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-580','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',578,'EXECUTED','8:32e1441fda83b3f09b08495f54224097','createIndex indexName=uuid_encounter_provider, tableName=encounter_provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-581','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',579,'EXECUTED','8:223e7ccad5d92ac9ac6ce466e78149b3','createIndex indexName=uuid_encounter_role, tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-582','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',580,'EXECUTED','8:283bfe503fece369904d99289348dcb7','createIndex indexName=uuid_encounter_type, tableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-583','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',581,'EXECUTED','8:5147b6dd155cbfac471071e43e5ab63d','createIndex indexName=uuid_field, tableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-584','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',582,'EXECUTED','8:7aa933f48b1567b874cbed32b7e7facc','createIndex indexName=uuid_field_answer, tableName=field_answer','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-585','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:37',583,'EXECUTED','8:3de5f3270a63bb332ae84384e4891f3f','createIndex indexName=uuid_field_type, tableName=field_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-586','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',584,'EXECUTED','8:b6b3ae0801cbfbabd935198c84d6f5a5','createIndex indexName=uuid_form, tableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-587','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',585,'EXECUTED','8:d44c4274900c82f6e726df0c832051a7','createIndex indexName=uuid_form_field, tableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-588','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',586,'EXECUTED','8:1ca9493ffc823394b1ff763bdc53359f','createIndex indexName=uuid_form_resource, tableName=form_resource','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-589','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',587,'EXECUTED','8:13eed271d4b2a4ff087afd2b93210e23','createIndex indexName=uuid_global_property, tableName=global_property','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-590','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',588,'EXECUTED','8:f9e55637df872bcb714a21eeb7c2fa01','createIndex indexName=uuid_hl7_in_archive, tableName=hl7_in_archive','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-591','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',589,'EXECUTED','8:0bdca7f6359e1fc2140882724278b182','createIndex indexName=uuid_hl7_in_error, tableName=hl7_in_error','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-592','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',590,'EXECUTED','8:99f1bad1b56e258bf640f07506df8199','createIndex indexName=uuid_hl7_in_queue, tableName=hl7_in_queue','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-593','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',591,'EXECUTED','8:cde9ea9ca79aded8c2ed6ece5c5e415e','createIndex indexName=uuid_hl7_source, tableName=hl7_source','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-594','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',592,'EXECUTED','8:ce86134810c67a0de9164e3ff75b4a91','createIndex indexName=uuid_location, tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-595','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',593,'EXECUTED','8:16ab88c13f7f2ba53cc24f8887ce3c47','createIndex indexName=uuid_location_attribute, tableName=location_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-596','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',594,'EXECUTED','8:81b6e85f9ee792d53b08461d311d88f3','createIndex indexName=uuid_location_attribute_type, tableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-597','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',595,'EXECUTED','8:c806c2eb61f0d4c7024e5062a83f33a1','createIndex indexName=uuid_location_tag, tableName=location_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-598','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',596,'EXECUTED','8:10d17c582a837dc1a1d2dc8d81998689','createIndex indexName=uuid_note, tableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-599','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',597,'EXECUTED','8:b03da5af51f83613980593b82670ca5c','createIndex indexName=uuid_notification_alert, tableName=notification_alert','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-600','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',598,'EXECUTED','8:ab622a82083bbe1ae3c558b8140ac73f','createIndex indexName=uuid_notification_template, tableName=notification_template','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-601','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',599,'EXECUTED','8:8cd659def795315703eee48c881cbe29','createIndex indexName=uuid_obs, tableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-602','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',600,'EXECUTED','8:938c4c7000ae719c2c1f33f179b5edf2','createIndex indexName=uuid_order_frequency, tableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-603','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',601,'EXECUTED','8:e1b3011dd209c587856156f278e3d8f9','createIndex indexName=uuid_order_group, tableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-604','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',602,'EXECUTED','8:26d0f9c7362107a4578ba50eee4e3a7c','createIndex indexName=uuid_order_group_attribute, tableName=order_group_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-605','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',603,'EXECUTED','8:a9e412747b7532594d672fad0dcaf898','createIndex indexName=uuid_order_group_attribute_type, tableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-606','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',604,'EXECUTED','8:09712dcc508a5a17f393a3b3374aeeca','createIndex indexName=uuid_order_set, tableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-607','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',605,'EXECUTED','8:c578ca58e004c53375d33d4dd68d530e','createIndex indexName=uuid_order_set_attribute, tableName=order_set_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-608','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',606,'EXECUTED','8:f52c57bbeb4dd99bf182fec61e4561dd','createIndex indexName=uuid_order_set_member, tableName=order_set_member','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-609','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',607,'EXECUTED','8:ce3300ba331719ed25bb81957cabb9ff','createIndex indexName=uuid_order_type, tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-610','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',608,'EXECUTED','8:9ffb56c1902c6f157ff1697eb195d7c9','createIndex indexName=uuid_orders, tableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-611','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',609,'EXECUTED','8:6c4cd3ef03c3e4115254ba26d3e36211','createIndex indexName=uuid_patient_identifier, tableName=patient_identifier','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-612','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',610,'EXECUTED','8:83200954f6c9d5e6dc0f788a780e3357','createIndex indexName=uuid_patient_identifier_type, tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-613','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',611,'EXECUTED','8:d7a55d0d66b744db62080bf4ecacf39f','createIndex indexName=uuid_patient_program, tableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-614','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',612,'EXECUTED','8:4efbfe8c2d21976fd8381551738b0c70','createIndex indexName=uuid_patient_program_attribute, tableName=patient_program_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-615','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',613,'EXECUTED','8:bd319e6e9ac85db273b802d1d733be5c','createIndex indexName=uuid_patient_state, tableName=patient_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-616','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',614,'EXECUTED','8:8dc72b4ca819c1e7de5aa3ed4b56d67f','createIndex indexName=uuid_person_address, tableName=person_address','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-617','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',615,'EXECUTED','8:da82b5ac5839028d75855498c9a97664','createIndex indexName=uuid_person_attribute, tableName=person_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-618','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',616,'EXECUTED','8:30f723c741fe8fc98dfb9f1a884b5a4b','createIndex indexName=uuid_person_attribute_type, tableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-619','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',617,'EXECUTED','8:fc61ad2574d4866a1872721e66175ba9','createIndex indexName=uuid_person_merge_log, tableName=person_merge_log','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-620','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',618,'EXECUTED','8:7596a3600e3806af81e9ccc989c2583d','createIndex indexName=uuid_person_name, tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-621','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',619,'EXECUTED','8:a319718eec1084aca2296db9515fa9bc','createIndex indexName=uuid_privilege, tableName=privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-622','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',620,'EXECUTED','8:1458dae38e1b91ebd601c7bae1daa537','createIndex indexName=uuid_program, tableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-623','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',621,'EXECUTED','8:0fef693257b5679cbe49731457b57ea4','createIndex indexName=uuid_program_attribute_type, tableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-624','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',622,'EXECUTED','8:3a6acfc955da308b21751f939427570b','createIndex indexName=uuid_program_workflow, tableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-625','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',623,'EXECUTED','8:b1d2a9dd8f28151a49829cb2f871d1e5','createIndex indexName=uuid_program_workflow_state, tableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-626','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',624,'EXECUTED','8:ef81758e91bc5011410856fad5df29b6','createIndex indexName=uuid_provider, tableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-627','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',625,'EXECUTED','8:8073556d039836758d2036783163a0c2','createIndex indexName=uuid_provider_attribute, tableName=provider_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-628','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',626,'EXECUTED','8:006f5f7e6e8155fc8007848059a6712b','createIndex indexName=uuid_provider_attribute_type, tableName=provider_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-629','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',627,'EXECUTED','8:a1a9ed615a43ee77323f1acf8ec67716','createIndex indexName=uuid_relationship, tableName=relationship','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-630','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',628,'EXECUTED','8:a08d190fe858505f5aa0002118731f0a','createIndex indexName=uuid_relationship_type, tableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-631','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',629,'EXECUTED','8:897c4c8ce0e87879ee7516180fc7f85b','createIndex indexName=uuid_report_object, tableName=report_object','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-632','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',630,'EXECUTED','8:efbefc03b593d873d7edb241265e61cc','createIndex indexName=uuid_report_schema_xml, tableName=report_schema_xml','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-633','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',631,'EXECUTED','8:ea472a86e036546830cd94d0dd0239e8','createIndex indexName=uuid_role, tableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-634','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',632,'EXECUTED','8:083f19c9950e474e1093dd957671407c','createIndex indexName=uuid_scheduler_task_config, tableName=scheduler_task_config','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-635','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',633,'EXECUTED','8:1372ff83779a8b1c4fe50a848d87b190','createIndex indexName=uuid_serialized_object, tableName=serialized_object','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-636','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',634,'EXECUTED','8:332254ada48e98ee35b2ed2cea9f43d6','createIndex indexName=uuid_visit, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-637','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',635,'EXECUTED','8:0fa6d73b7ea70add4c675f5b53cb00f7','createIndex indexName=uuid_visit_attribute, tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-638','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',636,'EXECUTED','8:5265350241f2bb2b7efd70407931fdff','createIndex indexName=uuid_visit_attribute_type, tableName=visit_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-639','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',637,'EXECUTED','8:592c2f0f72a977e7d68b7f471eedea8a','createIndex indexName=uuid_visit_type, tableName=visit_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-640','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',638,'EXECUTED','8:b9a65b7ab309bdd46ba4f2c449115ff7','createIndex indexName=visit_attribute_attribute_type_id_fk, tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-641','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',639,'EXECUTED','8:abb19811e24e0f8e496c1fa04d44c32e','createIndex indexName=visit_attribute_changed_by_fk, tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-642','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',640,'EXECUTED','8:a2626b27d70186539932878526685946','createIndex indexName=visit_attribute_creator_fk, tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-643','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',641,'EXECUTED','8:303466c6d7bf9f3da0b0d284d6ceb254','createIndex indexName=visit_attribute_type_changed_by_fk, tableName=visit_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-644','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',642,'EXECUTED','8:9e79bc736ad1da58baa5e7e6a7865b63','createIndex indexName=visit_attribute_type_creator_fk, tableName=visit_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-645','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',643,'EXECUTED','8:d7b6dd39ec5c3dbd6ede2a3c125c2109','createIndex indexName=visit_attribute_type_retired_by_fk, tableName=visit_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-646','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',644,'EXECUTED','8:7abf34988c450d665e0cdeb647f80119','createIndex indexName=visit_attribute_visit_fk, tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-647','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',645,'EXECUTED','8:a988a83b44131d10905757c01b41d39e','createIndex indexName=visit_attribute_voided_by_fk, tableName=visit_attribute','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-648','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',646,'EXECUTED','8:ed5026748c07dc69aa88ee02efc0d531','createIndex indexName=visit_changed_by_fk, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-649','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',647,'EXECUTED','8:821728f2ac5511a778103702e2758d66','createIndex indexName=visit_creator_fk, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-650','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',648,'EXECUTED','8:3c6b43e507d00b5bfb092c11eb16b0ec','createIndex indexName=visit_indication_concept_fk, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-651','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',649,'EXECUTED','8:026070962492dcdc88e33096b8f34690','createIndex indexName=visit_location_fk, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-652','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',650,'EXECUTED','8:fe8454bcd5df128318c029e22ad9e9ea','createIndex indexName=visit_patient_index, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-653','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',651,'EXECUTED','8:422143f23d4dd075db333a41c242fe94','createIndex indexName=visit_type_changed_by, tableName=visit_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-654','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',652,'EXECUTED','8:d334a839fdff81402550989673440de6','createIndex indexName=visit_type_creator, tableName=visit_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-655','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',653,'EXECUTED','8:42aae037b40bfe2fae6e57dba78e4381','createIndex indexName=visit_type_fk, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-656','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',654,'EXECUTED','8:f947d1867f95025430145d95911af633','createIndex indexName=visit_type_retired_by, tableName=visit_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-657','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',655,'EXECUTED','8:c4045e420af7ca5b14cf317fd211cda1','createIndex indexName=visit_voided_by_fk, tableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-658','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',656,'EXECUTED','8:3d95ff0d3142092fa058978762d39599','createIndex indexName=workflow_changed_by, tableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-659','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',657,'EXECUTED','8:3000c224f29f0dae2ab9005d36b178a0','createIndex indexName=workflow_concept, tableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-660','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',658,'EXECUTED','8:6bcb3a192f7a5cf3c1ace2f3bd41cd77','createIndex indexName=workflow_creator, tableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-661','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',659,'EXECUTED','8:eb75a8f45e6abce58d20c2215b7e4f14','createIndex indexName=workflow_for_state, tableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-662','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',660,'EXECUTED','8:21e4444dccbbb9ffcf79985eae831653','addForeignKeyConstraint baseTableName=person_address, constraintName=address_for_person, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-663','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',661,'EXECUTED','8:f8fbb4d31ed3a0336a6023a0726ab8bf','addForeignKeyConstraint baseTableName=notification_alert, constraintName=alert_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-664','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',662,'EXECUTED','8:6aed63a4d3250cd19e0bf7b77d868b4a','addForeignKeyConstraint baseTableName=notification_alert_recipient, constraintName=alert_read_by_user, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-665','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',663,'EXECUTED','8:62093fa49246930f05a852cad0cf9c94','addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-666','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',664,'EXECUTED','8:627b4123afb4b8a0ad0a87f6d39b0019','addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_coded_allergen_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-667','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',665,'EXECUTED','8:709cd85f99c0f717ecb0f323c49e61b2','addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-668','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',666,'EXECUTED','8:c62cb02af2dc596c23f6f9b4bef5537a','addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_encounter_id_fk, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-669','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',667,'EXECUTED','8:3dce98137a7ed3eb6c76f0e175dc0355','addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_patient_id_fk, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-670','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',668,'EXECUTED','8:cd83523b718ea8201962039359abe096','addForeignKeyConstraint baseTableName=allergy_reaction, constraintName=allergy_reaction_allergy_id_fk, referencedTableName=allergy','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-671','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',669,'EXECUTED','8:7a3a4545172d21601bdbb19a02f6029b','addForeignKeyConstraint baseTableName=allergy_reaction, constraintName=allergy_reaction_reaction_concept_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-672','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',670,'EXECUTED','8:99f3abb29249a480cbc3186d7a85e822','addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_severity_concept_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-673','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',671,'EXECUTED','8:2114890a2aced1cd87422ee331fd20aa','addForeignKeyConstraint baseTableName=allergy, constraintName=allergy_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-674','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',672,'EXECUTED','8:51fc5239565a46a290d349bc6d262d7b','addForeignKeyConstraint baseTableName=concept_answer, constraintName=answer, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-675','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',673,'EXECUTED','8:179aae55c847f4334da18bd4ef85e810','addForeignKeyConstraint baseTableName=concept_answer, constraintName=answer_answer_drug_fk, referencedTableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-676','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',674,'EXECUTED','8:6e0b5cf940460d64f3dc6fdda6f4ca2d','addForeignKeyConstraint baseTableName=obs, constraintName=answer_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-677','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',675,'EXECUTED','8:97f4e978fd1db04d340dc1f145926cfb','addForeignKeyConstraint baseTableName=obs, constraintName=answer_concept_drug, referencedTableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-678','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',676,'EXECUTED','8:2e2acc511f4c99af3398acb5cf0e7a78','addForeignKeyConstraint baseTableName=concept_answer, constraintName=answer_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-679','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',677,'EXECUTED','8:ce6986a76f4a90bc8bf4bf4e6621495d','addForeignKeyConstraint baseTableName=concept_answer, constraintName=answers_for_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-680','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:38',678,'EXECUTED','8:18e8a645f41407e4f42649245d6e43e7','addForeignKeyConstraint baseTableName=field_answer, constraintName=answers_for_field, referencedTableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-681','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',679,'EXECUTED','8:438477689b2f1ba483c25a65ab64e029','addForeignKeyConstraint baseTableName=person_attribute, constraintName=attribute_changer, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-682','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',680,'EXECUTED','8:ae6f8de598106d7531bc0ba121154c51','addForeignKeyConstraint baseTableName=person_attribute, constraintName=attribute_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-683','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',681,'EXECUTED','8:e3b34c754c33527d3a459fa20e2b94e3','addForeignKeyConstraint baseTableName=person_attribute_type, constraintName=attribute_type_changer, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-684','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',682,'EXECUTED','8:4b6ef3923376e7e0721ccaf9020ede72','addForeignKeyConstraint baseTableName=person_attribute_type, constraintName=attribute_type_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-685','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',683,'EXECUTED','8:ac0fb330641770b623f338d5f4ff5358','addForeignKeyConstraint baseTableName=person_attribute, constraintName=attribute_voider, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-686','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',684,'EXECUTED','8:1ff320a83539608ff8a193844a60f03c','addForeignKeyConstraint baseTableName=care_setting, constraintName=care_setting_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-687','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',685,'EXECUTED','8:d0b918b4fc771f07c0e566fb21e9b8b6','addForeignKeyConstraint baseTableName=care_setting, constraintName=care_setting_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-688','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',686,'EXECUTED','8:ee5680d2bc5dced96ab96cddc844b809','addForeignKeyConstraint baseTableName=care_setting, constraintName=care_setting_retired_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-689','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',687,'EXECUTED','8:6079cb6c0682f1f6ecb2bd93c25e1598','addForeignKeyConstraint baseTableName=order_set, constraintName=category_order_set_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-690','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',688,'EXECUTED','8:0db1d53e9acc90451fc92d3e1e1e7be4','addForeignKeyConstraint baseTableName=cohort, constraintName=cohort_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-691','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',689,'EXECUTED','8:e75a549692dcc556ebf39c3d9667c79d','addForeignKeyConstraint baseTableName=cohort_member, constraintName=cohort_member_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-692','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',690,'EXECUTED','8:021e09eccc220735850c69397d9b6ab0','addForeignKeyConstraint baseTableName=concept_attribute, constraintName=concept_attribute_attribute_type_id_fk, referencedTableName=concept_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-693','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',691,'EXECUTED','8:7385e0991344ccfc5d8c806821095c49','addForeignKeyConstraint baseTableName=concept_attribute, constraintName=concept_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-694','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',692,'EXECUTED','8:bbf31552bcea02b8cc050ea1714d55be','addForeignKeyConstraint baseTableName=concept_attribute, constraintName=concept_attribute_concept_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-695','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',693,'EXECUTED','8:cd37bd06d0fbfe8017ac3cb391ee4323','addForeignKeyConstraint baseTableName=concept_attribute, constraintName=concept_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-696','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',694,'EXECUTED','8:eefaeefbf3c76760769aa5f0ad3839b6','addForeignKeyConstraint baseTableName=concept_attribute_type, constraintName=concept_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-697','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',695,'EXECUTED','8:00bbf1b5ec53c61fcd779a11812408ae','addForeignKeyConstraint baseTableName=concept_attribute_type, constraintName=concept_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-698','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',696,'EXECUTED','8:58897ca4daed63fdbba0e2453d299491','addForeignKeyConstraint baseTableName=concept_attribute_type, constraintName=concept_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-699','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',697,'EXECUTED','8:48c2223b9c175371d6b9ba53823e4973','addForeignKeyConstraint baseTableName=concept_attribute, constraintName=concept_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-700','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',698,'EXECUTED','8:f929b0bfd0dd87fab0c89251957e167d','addForeignKeyConstraint baseTableName=concept_complex, constraintName=concept_attributes, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-701','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',699,'EXECUTED','8:41ab10429622c3a5953e9d817dca547e','addForeignKeyConstraint baseTableName=concept_class, constraintName=concept_class_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-702','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',700,'EXECUTED','8:cb54d714296e4dcb6f9f4f230f9f25fb','addForeignKeyConstraint baseTableName=concept_class, constraintName=concept_class_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-703','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',701,'EXECUTED','8:eba8bd5ca8f41ca5e456cb53a50006a0','addForeignKeyConstraint baseTableName=concept, constraintName=concept_classes, referencedTableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-704','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',702,'EXECUTED','8:6ff8679ef26b961f0a07b668389dbf8a','addForeignKeyConstraint baseTableName=concept, constraintName=concept_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-705','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',703,'EXECUTED','8:22d195ad33f1d77935c59c9193cfd907','addForeignKeyConstraint baseTableName=concept_datatype, constraintName=concept_datatype_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-706','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',704,'EXECUTED','8:b2cf520e20f19628418b52023909d00d','addForeignKeyConstraint baseTableName=concept, constraintName=concept_datatypes, referencedTableName=concept_datatype','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-707','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',705,'EXECUTED','8:ab770f280406848f7b36728484b25804','addForeignKeyConstraint baseTableName=field, constraintName=concept_for_field, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-708','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',706,'EXECUTED','8:e99530b4a995dc6cea349702e50406a6','addForeignKeyConstraint baseTableName=concept_proposal, constraintName=concept_for_proposal, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-709','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',707,'EXECUTED','8:8183e49821e72eb0289753b8f011a664','addForeignKeyConstraint baseTableName=drug_reference_map, constraintName=concept_map_type_for_drug_reference_map, referencedTableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-710','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',708,'EXECUTED','8:cf86f465e88e378e1d3503ebfafa044c','addForeignKeyConstraint baseTableName=concept_name, constraintName=concept_name_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-711','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',709,'EXECUTED','8:fd9c6937014abfbdf61aee886f2acd22','addForeignKeyConstraint baseTableName=concept_name_tag, constraintName=concept_name_tag_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-712','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',710,'EXECUTED','8:d019d1f383bc872c8591a7fde598483c','addForeignKeyConstraint baseTableName=concept_reference_source, constraintName=concept_reference_source_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-713','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',711,'EXECUTED','8:459dfafc34ee923a41535c728aea8141','addForeignKeyConstraint baseTableName=drug_reference_map, constraintName=concept_reference_term_for_drug_reference_map, referencedTableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-714','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',712,'EXECUTED','8:4232ef29a4b143201b39d56b11832ebb','addForeignKeyConstraint baseTableName=concept_reference_source, constraintName=concept_source_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-715','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',713,'EXECUTED','8:55b15bb8e884a146a2505f119672f34b','addForeignKeyConstraint baseTableName=concept_state_conversion, constraintName=concept_triggers_conversion, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-716','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',714,'EXECUTED','8:f54ec9638bf157480a0aa11e34ade225','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-717','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',715,'EXECUTED','8:b2f10c190910bddb9b90fe972f7e536b','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_condition_coded_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-718','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',716,'EXECUTED','8:704a8e3c95d42a350bc1be82cb9231db','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_condition_coded_name_fk, referencedTableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-719','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',717,'EXECUTED','8:d63bc9fbedc29df5c79dd2025637fbaa','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-720','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',718,'EXECUTED','8:7aefaa5685c2bc534197737aa38b2d03','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_patient_fk, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-721','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',719,'EXECUTED','8:ebaa5d6c4bf4324fcde5ff97f53b621c','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_previous_version_fk, referencedTableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-722','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',720,'EXECUTED','8:f2ec27dbee2e559dc3c27b4e28aeed88','addForeignKeyConstraint baseTableName=conditions, constraintName=condition_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-723','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:39',721,'EXECUTED','8:2b29be399565099eb0ecae1a232ed977','addForeignKeyConstraint baseTableName=conditions, constraintName=conditions_encounter_id_fk, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-724','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',722,'EXECUTED','8:d621b28037111ad0e3f38f4758442799','addForeignKeyConstraint baseTableName=concept_state_conversion, constraintName=conversion_involves_workflow, referencedTableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-725','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',723,'EXECUTED','8:46c431397c29ca016df9ff6ec59ebeef','addForeignKeyConstraint baseTableName=concept_state_conversion, constraintName=conversion_to_state, referencedTableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-726','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',724,'EXECUTED','8:6865a1067e1322b6983e863a6423abce','addForeignKeyConstraint baseTableName=person_attribute, constraintName=defines_attribute_type, referencedTableName=person_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-727','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',725,'EXECUTED','8:5de385975ca54cf445dca515599dce4a','addForeignKeyConstraint baseTableName=patient_identifier, constraintName=defines_identifier_type, referencedTableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-728','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',726,'EXECUTED','8:ca3d804bc69117d7196ba07535c55616','addForeignKeyConstraint baseTableName=concept_description, constraintName=description_for_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-729','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',727,'EXECUTED','8:a884a57c5435f9dbc3e59daa75bdb6bc','addForeignKeyConstraint baseTableName=diagnosis_attribute, constraintName=diagnosis_attribute_attribute_type_id_fk, referencedTableName=diagnosis_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-730','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',728,'EXECUTED','8:957c9d2742cd13ee6ed8e28f2a94867d','addForeignKeyConstraint baseTableName=diagnosis_attribute, constraintName=diagnosis_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-731','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',729,'EXECUTED','8:ca091b6dc2e5d8614d2be42126807aef','addForeignKeyConstraint baseTableName=diagnosis_attribute, constraintName=diagnosis_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-732','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',730,'EXECUTED','8:d8cdd85a700af117dda6f15c0aef4e97','addForeignKeyConstraint baseTableName=diagnosis_attribute, constraintName=diagnosis_attribute_diagnosis_fk, referencedTableName=encounter_diagnosis','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-733','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',731,'EXECUTED','8:a8428eac725320b11e86f6069a37c531','addForeignKeyConstraint baseTableName=diagnosis_attribute_type, constraintName=diagnosis_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-734','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',732,'EXECUTED','8:df572dc29c4c2bf188b505bacc55186f','addForeignKeyConstraint baseTableName=diagnosis_attribute_type, constraintName=diagnosis_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-735','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',733,'EXECUTED','8:027fb12875450a8858f528f5230a988f','addForeignKeyConstraint baseTableName=diagnosis_attribute_type, constraintName=diagnosis_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-736','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',734,'EXECUTED','8:f35ae225dfbc4e92cebd672866769b03','addForeignKeyConstraint baseTableName=diagnosis_attribute, constraintName=diagnosis_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-737','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',735,'EXECUTED','8:d4c32666dbaad85b2f9d1205cb009379','addForeignKeyConstraint baseTableName=orders, constraintName=discontinued_because, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-738','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',736,'EXECUTED','8:552e8c193ed0293ab6784cd2c0240347','addForeignKeyConstraint baseTableName=drug, constraintName=dosage_form_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-739','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',737,'EXECUTED','8:ea226408f1b91fe254ead838c4bcfb1c','addForeignKeyConstraint baseTableName=drug, constraintName=drug_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-740','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',738,'EXECUTED','8:19d1ad53b74d0fa79f7c53d0b285e782','addForeignKeyConstraint baseTableName=drug, constraintName=drug_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-741','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',739,'EXECUTED','8:92254e82b2a826e1abed02bfe818ef94','addForeignKeyConstraint baseTableName=drug, constraintName=drug_dose_limit_units_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-742','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',740,'EXECUTED','8:550473a898fb2adf90ec003baed81e18','addForeignKeyConstraint baseTableName=drug_reference_map, constraintName=drug_for_drug_reference_map, referencedTableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-743','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',741,'EXECUTED','8:d3b694d9711ca75ad013a7b2c43a37f1','addForeignKeyConstraint baseTableName=drug_ingredient, constraintName=drug_ingredient_drug_id_fk, referencedTableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-744','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',742,'EXECUTED','8:89cf7a97bcc99e3fbd9a730b13862250','addForeignKeyConstraint baseTableName=drug_ingredient, constraintName=drug_ingredient_ingredient_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-745','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',743,'EXECUTED','8:1e1453c761a933bb931ed579bcf2fbd9','addForeignKeyConstraint baseTableName=drug_ingredient, constraintName=drug_ingredient_units_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-746','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',744,'EXECUTED','8:834c46f1296b4b879c5255b9d5711efc','addForeignKeyConstraint baseTableName=drug_order, constraintName=drug_order_dose_units, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-747','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',745,'EXECUTED','8:45c24a24e91405ebcbc399245e6aa9fc','addForeignKeyConstraint baseTableName=drug_order, constraintName=drug_order_duration_units_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-748','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',746,'EXECUTED','8:6acf2370b8ca38a3a0bd45ba93af17c0','addForeignKeyConstraint baseTableName=drug_order, constraintName=drug_order_frequency_fk, referencedTableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-749','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',747,'EXECUTED','8:d29ffc460e25957deb024daf835959ff','addForeignKeyConstraint baseTableName=drug_order, constraintName=drug_order_quantity_units, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-750','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',748,'EXECUTED','8:e9a593d247aa42e0c3312971e70a0a83','addForeignKeyConstraint baseTableName=drug_order, constraintName=drug_order_route_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-751','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',749,'EXECUTED','8:18e8f99fe4dc5b41eef2749d7e8c4378','addForeignKeyConstraint baseTableName=drug_reference_map, constraintName=drug_reference_map_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-752','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',750,'EXECUTED','8:62239dfc46c6261b819cfc325689b72d','addForeignKeyConstraint baseTableName=drug, constraintName=drug_retired_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-753','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',751,'EXECUTED','8:46d37cc722f5aaf21abffde567de9490','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-754','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',752,'EXECUTED','8:d181df09c383122a23ab7292f91fb8ab','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-755','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',753,'EXECUTED','8:3b54116dc075d8f76db6b03d0c20297b','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_coded_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-756','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',754,'EXECUTED','8:124aadd3d48288d84f0f25a1910f23d7','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_coded_name_fk, referencedTableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-757','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',755,'EXECUTED','8:db06bdbe85cdc65e7a1c302f2cc0cb40','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_condition_id_fk, referencedTableName=conditions','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-758','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',756,'EXECUTED','8:f1b5042e0b19475fe81463316ecdb305','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-759','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',757,'EXECUTED','8:07320bedaca7821ea6a42d768066ae08','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_encounter_id_fk, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-760','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:40',758,'EXECUTED','8:df53582256da26d4215450e651aeb611','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_patient_fk, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-761','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',759,'EXECUTED','8:ba41a6241d972c59e6fab8183b8903ed','addForeignKeyConstraint baseTableName=encounter_diagnosis, constraintName=encounter_diagnosis_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-762','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',760,'EXECUTED','8:9095e3455d0d74c8eb236a089e891f30','addForeignKeyConstraint baseTableName=concept_proposal, constraintName=encounter_for_proposal, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-763','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',761,'EXECUTED','8:9d9148db86066d8accf65a3573e9225f','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_form, referencedTableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-764','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',762,'EXECUTED','8:44688bbda008838d9cff13981bd0e618','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_ibfk_1, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-765','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',763,'EXECUTED','8:de938cb3f56e4006356bab461f48613b','addForeignKeyConstraint baseTableName=encounter_provider, constraintName=encounter_id_fk, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-766','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',764,'EXECUTED','8:3887c054e5cc31b74eb9da3f18ad6f1d','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_location, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-767','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',765,'EXECUTED','8:301f43698f7f418a04b5bb46303c37b7','addForeignKeyConstraint baseTableName=note, constraintName=encounter_note, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-768','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',766,'EXECUTED','8:1533bc819393e19d5cd6b685f12e1dfd','addForeignKeyConstraint baseTableName=obs, constraintName=encounter_observations, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-769','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',767,'EXECUTED','8:54860a8c7870abf426eecabf87d2bbb4','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_patient, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-770','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',768,'EXECUTED','8:8059e13020d9b3b831c0e84fdd9f35f3','addForeignKeyConstraint baseTableName=encounter_provider, constraintName=encounter_provider_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-771','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',769,'EXECUTED','8:a2de6b3cd36e31fa2479ba4a8f419976','addForeignKeyConstraint baseTableName=encounter_provider, constraintName=encounter_provider_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-772','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',770,'EXECUTED','8:49fc50403bd138f9540de67acdd56ba5','addForeignKeyConstraint baseTableName=encounter_provider, constraintName=encounter_provider_voided_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-773','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',771,'EXECUTED','8:1c04c7b8260bb52579894dfd41bc7ae8','addForeignKeyConstraint baseTableName=encounter_role, constraintName=encounter_role_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-774','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',772,'EXECUTED','8:b4cdcc63e08f474c8fb7b7c3f11bbecc','addForeignKeyConstraint baseTableName=encounter_role, constraintName=encounter_role_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-775','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',773,'EXECUTED','8:a6284ec6b62d4c67c648284299ed6c2c','addForeignKeyConstraint baseTableName=encounter_provider, constraintName=encounter_role_id_fk, referencedTableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-776','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',774,'EXECUTED','8:6ede99ec449cc4ad1810a85937f8fb1a','addForeignKeyConstraint baseTableName=encounter_role, constraintName=encounter_role_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-777','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',775,'EXECUTED','8:7e90744d284d34c8c24c3ca40d17ee46','addForeignKeyConstraint baseTableName=encounter_type, constraintName=encounter_type_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-778','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',776,'EXECUTED','8:31f1cc3a5ef337a9e2e7d925d968b40f','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_type_id, referencedTableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-779','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',777,'EXECUTED','8:96900e1f9722443e27fc0f4fd8fc9c94','addForeignKeyConstraint baseTableName=encounter, constraintName=encounter_visit_id_fk, referencedTableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-780','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',778,'EXECUTED','8:ec809e1d1fb0bb80d27772c601a316fd','addForeignKeyConstraint baseTableName=drug_order, constraintName=extends_order, referencedTableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-781','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',779,'EXECUTED','8:a7ddedb1f7d304cd0c1fa4fc3614e801','addForeignKeyConstraint baseTableName=field_answer, constraintName=field_answer_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-782','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',780,'EXECUTED','8:462867755db1686fa0e869f702a7a607','addForeignKeyConstraint baseTableName=form_field, constraintName=field_within_form, referencedTableName=field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-783','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',781,'EXECUTED','8:7ddfef12f5dd33eb33fa790d08703557','addForeignKeyConstraint baseTableName=order_type_class_map, constraintName=fk_order_type_class_map_concept_class_concept_class_id, referencedTableName=concept_class','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-784','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',782,'EXECUTED','8:dc0e0afa49e37712f28c6a46a8aad2fe','addForeignKeyConstraint baseTableName=order_type_class_map, constraintName=fk_order_type_order_type_id, referencedTableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-785','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',783,'EXECUTED','8:13446e40e44b0402e59022507c21e455','addForeignKeyConstraint baseTableName=orders, constraintName=fk_orderer_provider, referencedTableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-786','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',784,'EXECUTED','8:092613b9ee888f657c30543538f39260','addForeignKeyConstraint baseTableName=patient_identifier, constraintName=fk_patient_id_patient_identifier, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-787','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',785,'EXECUTED','8:81b88efda66069fed3e8efd4949bb5c9','addForeignKeyConstraint baseTableName=form_field, constraintName=form_containing_field, referencedTableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-788','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',786,'EXECUTED','8:581804e4c331f9e9d120460cb3d911f1','addForeignKeyConstraint baseTableName=form, constraintName=form_encounter_type, referencedTableName=encounter_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-789','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',787,'EXECUTED','8:d548b5df5ade6c41909d20883f6d3603','addForeignKeyConstraint baseTableName=form_field, constraintName=form_field_hierarchy, referencedTableName=form_field','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-790','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',788,'EXECUTED','8:5c8a2b583f468b001c99f42240fef8fb','addForeignKeyConstraint baseTableName=form_resource, constraintName=form_resource_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-791','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',789,'EXECUTED','8:634fd2b4c9e9353d4fc0217e02f9f536','addForeignKeyConstraint baseTableName=form_resource, constraintName=form_resource_form_fk, referencedTableName=form','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-792','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',790,'EXECUTED','8:d01943188cc9a7de6d477ce03d9b75d8','addForeignKeyConstraint baseTableName=global_property, constraintName=global_property_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-793','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',791,'EXECUTED','8:554f3ecd038c24ad03c11f90a9eed5b2','addForeignKeyConstraint baseTableName=concept_set, constraintName=has_a, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-794','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',792,'EXECUTED','8:34d1f304af0097cc11d84a3a197c0888','addForeignKeyConstraint baseTableName=hl7_in_queue, constraintName=hl7_source_with_queue, referencedTableName=hl7_source','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-795','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',793,'EXECUTED','8:c80769ca20a15737c5324380cfde8fa5','addForeignKeyConstraint baseTableName=notification_alert_recipient, constraintName=id_of_alert, referencedTableName=notification_alert','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-796','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:41',794,'EXECUTED','8:4575acff7e7864408a6b80f6593468fc','addForeignKeyConstraint baseTableName=patient_identifier, constraintName=identifier_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-797','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',795,'EXECUTED','8:305c970aafd1bc1938958c621a91ebce','addForeignKeyConstraint baseTableName=patient_identifier, constraintName=identifier_voider, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-798','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',796,'EXECUTED','8:f9a9e3a536c17c2c156d53692358a3dd','addForeignKeyConstraint baseTableName=person_attribute, constraintName=identifies_person, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-799','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',797,'EXECUTED','8:24c34b76746ec045dee8a349aa56ac9c','addForeignKeyConstraint baseTableName=role_role, constraintName=inherited_role, referencedTableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-800','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',798,'EXECUTED','8:f549f14e485daa46df216d897b16112d','addForeignKeyConstraint baseTableName=drug_order, constraintName=inventory_item, referencedTableName=drug','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-801','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',799,'EXECUTED','8:33d706edd195109d4a6340ca29863bec','addForeignKeyConstraint baseTableName=location_attribute, constraintName=location_attribute_attribute_type_id_fk, referencedTableName=location_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-802','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',800,'EXECUTED','8:b4f4a7aded7b47acd2f19d3ea5ed9c38','addForeignKeyConstraint baseTableName=location_attribute, constraintName=location_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-803','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',801,'EXECUTED','8:557b52f502c3d90d97069fb071661a0b','addForeignKeyConstraint baseTableName=location_attribute, constraintName=location_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-804','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',802,'EXECUTED','8:770021fdec02992fa3681d043eec83eb','addForeignKeyConstraint baseTableName=location_attribute, constraintName=location_attribute_location_fk, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-805','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',803,'EXECUTED','8:8b4f373bae9db1fd844a9864e5cdddba','addForeignKeyConstraint baseTableName=location_attribute_type, constraintName=location_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-806','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',804,'EXECUTED','8:d84f3918d536bbf5307d923d7f3020be','addForeignKeyConstraint baseTableName=location_attribute_type, constraintName=location_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-807','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',805,'EXECUTED','8:87923b1e7201befca237a14aeec0a9fa','addForeignKeyConstraint baseTableName=location_attribute_type, constraintName=location_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-808','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',806,'EXECUTED','8:b0f81097e2d6e80b7e9a55e1e6a6588b','addForeignKeyConstraint baseTableName=location_attribute, constraintName=location_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-809','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',807,'EXECUTED','8:0ebfc2ccea985d43830b059418c925c6','addForeignKeyConstraint baseTableName=location, constraintName=location_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-810','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',808,'EXECUTED','8:b9659675bcdb5b15be5d3e04d1b306bd','addForeignKeyConstraint baseTableName=location_tag, constraintName=location_tag_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-811','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',809,'EXECUTED','8:1a6596d2d7d328fb949405e3cf585e16','addForeignKeyConstraint baseTableName=location_tag, constraintName=location_tag_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-812','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',810,'EXECUTED','8:a0ae313e026a7f6f9cee4278859e1dcc','addForeignKeyConstraint baseTableName=location_tag_map, constraintName=location_tag_map_location, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-813','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',811,'EXECUTED','8:c51245b6e4d91a95ada1eba7bf8bcb0c','addForeignKeyConstraint baseTableName=location_tag_map, constraintName=location_tag_map_tag, referencedTableName=location_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-814','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',812,'EXECUTED','8:d51a90d2c4de90a286cc7bf3c10f57b7','addForeignKeyConstraint baseTableName=location_tag, constraintName=location_tag_retired_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-815','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',813,'EXECUTED','8:108c8816e041e2f64a8a4378259789a8','addForeignKeyConstraint baseTableName=location, constraintName=location_type_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-816','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',814,'EXECUTED','8:1f608428ac7658d92103ea3d69e6ae9f','addForeignKeyConstraint baseTableName=concept_reference_map, constraintName=map_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-817','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',815,'EXECUTED','8:be49584e8688d0d3b6f878d8dca29f5a','addForeignKeyConstraint baseTableName=concept_reference_map, constraintName=map_for_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-818','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',816,'EXECUTED','8:d416fa1984701b2cebe9e1e2b6c3b8f2','addForeignKeyConstraint baseTableName=concept_reference_map, constraintName=mapped_concept_map_type, referencedTableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-819','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',817,'EXECUTED','8:1e0439821c666f1c716ba26c80ef7701','addForeignKeyConstraint baseTableName=concept_reference_term_map, constraintName=mapped_concept_map_type_ref_term_map, referencedTableName=concept_map_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-820','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',818,'EXECUTED','8:b2dddec16d2679c0990c2035f9e78e00','addForeignKeyConstraint baseTableName=concept_name_tag_map, constraintName=mapped_concept_name, referencedTableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-821','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',819,'EXECUTED','8:12c7cf8c691aa926199519434ad03413','addForeignKeyConstraint baseTableName=concept_name_tag_map, constraintName=mapped_concept_name_tag, referencedTableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-822','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',820,'EXECUTED','8:0bec568cbeb81dbe2518cff57be788e3','addForeignKeyConstraint baseTableName=concept_proposal_tag_map, constraintName=mapped_concept_proposal, referencedTableName=concept_proposal','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-823','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',821,'EXECUTED','8:8e6a0691fc1f926acd3a4d26d41fb010','addForeignKeyConstraint baseTableName=concept_proposal_tag_map, constraintName=mapped_concept_proposal_tag, referencedTableName=concept_name_tag','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-824','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:42',822,'EXECUTED','8:788154a99a273faaf0d4e352793b5f1c','addForeignKeyConstraint baseTableName=concept_reference_map, constraintName=mapped_concept_reference_term, referencedTableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-825','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',823,'EXECUTED','8:9a18311294e0ef7d6019959277730b19','addForeignKeyConstraint baseTableName=concept_reference_term, constraintName=mapped_concept_source, referencedTableName=concept_reference_source','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-826','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',824,'EXECUTED','8:8a681c7d2ae4d9334fd0602144b878fd','addForeignKeyConstraint baseTableName=concept_reference_term_map, constraintName=mapped_term_a, referencedTableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-827','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',825,'EXECUTED','8:212689ff4e3937f4e8ccafde1e9c33ca','addForeignKeyConstraint baseTableName=concept_reference_term_map, constraintName=mapped_term_b, referencedTableName=concept_reference_term','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-828','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',826,'EXECUTED','8:574be9614f17cc39f647467658459bb5','addForeignKeyConstraint baseTableName=concept_reference_term, constraintName=mapped_user_changed, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-829','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',827,'EXECUTED','8:d604b0c1a59f8371919d17eca4fd9fb4','addForeignKeyConstraint baseTableName=concept_map_type, constraintName=mapped_user_changed_concept_map_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-830','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',828,'EXECUTED','8:9fad410381b49fff0e9845bd52b01979','addForeignKeyConstraint baseTableName=concept_reference_map, constraintName=mapped_user_changed_ref_term, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-831','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',829,'EXECUTED','8:a3fe5cfe7315c7f199ea9b6cb8f14cc2','addForeignKeyConstraint baseTableName=concept_reference_term_map, constraintName=mapped_user_changed_ref_term_map, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-832','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',830,'EXECUTED','8:bf67dbdd2a18d66991ac19afe6613cb7','addForeignKeyConstraint baseTableName=concept_reference_term, constraintName=mapped_user_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-833','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',831,'EXECUTED','8:7af1ad5eb936878b328315c223486a1f','addForeignKeyConstraint baseTableName=concept_map_type, constraintName=mapped_user_creator_concept_map_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-834','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',832,'EXECUTED','8:d15a3809e6bde80dfb09b890cd7dc7da','addForeignKeyConstraint baseTableName=concept_reference_term_map, constraintName=mapped_user_creator_ref_term_map, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-835','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',833,'EXECUTED','8:d0ed2f4f2c62732f945530fe5260e2f1','addForeignKeyConstraint baseTableName=concept_reference_term, constraintName=mapped_user_retired, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-836','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',834,'EXECUTED','8:384de9582bfb3bf713164815d6eb3ef8','addForeignKeyConstraint baseTableName=concept_map_type, constraintName=mapped_user_retired_concept_map_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-837','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',835,'EXECUTED','8:6e45c7a91e88c35f8647eee56578b655','addForeignKeyConstraint baseTableName=cohort_member, constraintName=member_patient, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-838','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',836,'EXECUTED','8:e2bb60d7a69cc6866ce15cd7c01e81db','addForeignKeyConstraint baseTableName=concept_name, constraintName=name_for_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-839','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',837,'EXECUTED','8:8fcf4f6343d695755508cc42b4a4bd96','addForeignKeyConstraint baseTableName=person_name, constraintName=name_for_person, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-840','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',838,'EXECUTED','8:1f635a1b5b931f3cf99f23b42375cc6f','addForeignKeyConstraint baseTableName=note, constraintName=note_hierarchy, referencedTableName=note','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-841','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',839,'EXECUTED','8:3505f76084af733f6cccbf76e5c38d83','addForeignKeyConstraint baseTableName=concept_numeric, constraintName=numeric_attributes, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-842','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',840,'EXECUTED','8:c9732c3472d8ea7432d7f81f6f5a364d','addForeignKeyConstraint baseTableName=obs, constraintName=obs_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-843','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',841,'EXECUTED','8:8268277839315e6c98c9628043e7ff7a','addForeignKeyConstraint baseTableName=obs, constraintName=obs_enterer, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-844','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',842,'EXECUTED','8:978d54886f99fefb9e7ad0dc6b22039a','addForeignKeyConstraint baseTableName=obs, constraintName=obs_grouping_id, referencedTableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-845','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',843,'EXECUTED','8:17401ff295cbe4915de4c9525aa78d5b','addForeignKeyConstraint baseTableName=obs, constraintName=obs_location, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-846','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',844,'EXECUTED','8:3a81fc6dbab540a5c54aff400e275a1f','addForeignKeyConstraint baseTableName=obs, constraintName=obs_name_of_coded_value, referencedTableName=concept_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-847','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',845,'EXECUTED','8:fdd335669cdfbf9d32c61431f90a33c2','addForeignKeyConstraint baseTableName=note, constraintName=obs_note, referencedTableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-848','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',846,'EXECUTED','8:432c70cfc7e56e006716bcb9b2081d5e','addForeignKeyConstraint baseTableName=obs, constraintName=obs_order, referencedTableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-849','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',847,'EXECUTED','8:678cccaed6cd2e13315efa19775f8f26','addForeignKeyConstraint baseTableName=order_attribute, constraintName=order_attribute_attribute_type_id_fk, referencedTableName=order_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-850','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',848,'EXECUTED','8:1a442dfab10d54dc14f691d215538420','addForeignKeyConstraint baseTableName=order_attribute, constraintName=order_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-851','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',849,'EXECUTED','8:6df6d314e4d83317dbcfb1407c3e810c','addForeignKeyConstraint baseTableName=order_attribute, constraintName=order_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-852','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',850,'EXECUTED','8:46112c273e0a43fbc863f1752cf33146','addForeignKeyConstraint baseTableName=order_attribute, constraintName=order_attribute_order_fk, referencedTableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-853','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',851,'EXECUTED','8:d0be8fbbb23772a62b8f476a21a1625b','addForeignKeyConstraint baseTableName=order_attribute_type, constraintName=order_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-854','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',852,'EXECUTED','8:f466922e57459d80de08a68bef09631b','addForeignKeyConstraint baseTableName=order_attribute_type, constraintName=order_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-855','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',853,'EXECUTED','8:c040e0fd5fc272fd33d490e1a2070d76','addForeignKeyConstraint baseTableName=order_attribute_type, constraintName=order_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-856','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',854,'EXECUTED','8:697702508b60442475466e386c94e06e','addForeignKeyConstraint baseTableName=order_attribute, constraintName=order_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-857','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',855,'EXECUTED','8:c0cc8be50e42088cf80d11f4e163ea7d','addForeignKeyConstraint baseTableName=orders, constraintName=order_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-858','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',856,'EXECUTED','8:f685e35a4e9b7be9d77310c3ee7e6650','addForeignKeyConstraint baseTableName=orders, constraintName=order_for_patient, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-859','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',857,'EXECUTED','8:35ee5d376901b289c588811974e87a7d','addForeignKeyConstraint baseTableName=order_frequency, constraintName=order_frequency_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-860','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',858,'EXECUTED','8:ed8b876f5f143f0cba8e6cbe34736fb1','addForeignKeyConstraint baseTableName=order_frequency, constraintName=order_frequency_concept_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-861','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:43',859,'EXECUTED','8:fa666fcbaf13dda689b99cc7cf461f73','addForeignKeyConstraint baseTableName=order_frequency, constraintName=order_frequency_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-862','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',860,'EXECUTED','8:a42da69630cf17b47154a3f920f11945','addForeignKeyConstraint baseTableName=order_frequency, constraintName=order_frequency_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-863','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',861,'EXECUTED','8:b6d8666b5a284fe45a0a99bdec3ae05f','addForeignKeyConstraint baseTableName=order_group_attribute, constraintName=order_group_attribute_attribute_type_id_fk, referencedTableName=order_group_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-864','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',862,'EXECUTED','8:d5b3a8130e70af6e1bad1d45af868161','addForeignKeyConstraint baseTableName=order_group_attribute, constraintName=order_group_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-865','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',863,'EXECUTED','8:2d97855468cd1eb3a913e0619be17cc5','addForeignKeyConstraint baseTableName=order_group_attribute, constraintName=order_group_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-866','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',864,'EXECUTED','8:134e39fdd57e873f854fe6330f152131','addForeignKeyConstraint baseTableName=order_group_attribute, constraintName=order_group_attribute_order_group_fk, referencedTableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-867','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',865,'EXECUTED','8:41eed75e834cd501df0635e629086869','addForeignKeyConstraint baseTableName=order_group_attribute_type, constraintName=order_group_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-868','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',866,'EXECUTED','8:af578c3200ea4e8d6a7ee67830e2dee3','addForeignKeyConstraint baseTableName=order_group_attribute_type, constraintName=order_group_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-869','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',867,'EXECUTED','8:93a543e85718bc84800ff2ef6f1d56be','addForeignKeyConstraint baseTableName=order_group_attribute_type, constraintName=order_group_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-870','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',868,'EXECUTED','8:237fe0e6b320c65d53b09adbce550815','addForeignKeyConstraint baseTableName=order_group_attribute, constraintName=order_group_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-871','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',869,'EXECUTED','8:9afd39bfd39b523493df6ca7a18959ad','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-872','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',870,'EXECUTED','8:165a8e6fd586ec7b9aaba466a0e72b72','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-873','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',871,'EXECUTED','8:62befe3cbb631d4cce795c3bd0ffce33','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_encounter_id_fk, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-874','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',872,'EXECUTED','8:434d6baf570210d69bf2cd1563523c3a','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_order_group_reason_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-875','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',873,'EXECUTED','8:1077139c52eafdb43b82685cdda42094','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_parent_order_group_fk, referencedTableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-876','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',874,'EXECUTED','8:cfba1c3e93a86bf7f0d5cbad7e6fb11a','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_patient_id_fk, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-877','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',875,'EXECUTED','8:9254a443cd8cb8563080beab50178b80','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_previous_order_group_fk, referencedTableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-878','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',876,'EXECUTED','8:adf9e68f34363f7ab5225c34619aeb72','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_set_id_fk, referencedTableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-879','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',877,'EXECUTED','8:a3831c1d7876bf02b28394b18241d456','addForeignKeyConstraint baseTableName=order_group, constraintName=order_group_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-880','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',878,'EXECUTED','8:c5798f9d76b0410ee0751458ad63ce10','addForeignKeyConstraint baseTableName=order_set_attribute, constraintName=order_set_attribute_attribute_type_id_fk, referencedTableName=order_set_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-881','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',879,'EXECUTED','8:10f8a8f94c59c1ca08f3d92af41d6177','addForeignKeyConstraint baseTableName=order_set_attribute, constraintName=order_set_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-882','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',880,'EXECUTED','8:8effc2d76e6e2db156d78ba477612b8d','addForeignKeyConstraint baseTableName=order_set_attribute, constraintName=order_set_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-883','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',881,'EXECUTED','8:6ba2f04b606798319b688fbc3ec66da8','addForeignKeyConstraint baseTableName=order_set_attribute, constraintName=order_set_attribute_order_set_fk, referencedTableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-884','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',882,'EXECUTED','8:8c138d212ce61dfe85d4cc94152b0214','addForeignKeyConstraint baseTableName=order_set_attribute_type, constraintName=order_set_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-885','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',883,'EXECUTED','8:1ffd97c016e06a9364eafe58d7bb9c10','addForeignKeyConstraint baseTableName=order_set_attribute_type, constraintName=order_set_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-886','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',884,'EXECUTED','8:840fd6d949b76467eca0a45df50b5455','addForeignKeyConstraint baseTableName=order_set_attribute_type, constraintName=order_set_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-887','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',885,'EXECUTED','8:5be1cea2506a9b4bf28e88f03d2ac435','addForeignKeyConstraint baseTableName=order_set_attribute, constraintName=order_set_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-888','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',886,'EXECUTED','8:d6a5dd977622b9a45104b639fea27057','addForeignKeyConstraint baseTableName=order_set, constraintName=order_set_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-889','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',887,'EXECUTED','8:a33e570f46675760d7f918cafe09f470','addForeignKeyConstraint baseTableName=order_set, constraintName=order_set_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-890','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',888,'EXECUTED','8:7fc0b8b57c306844f3f967e9ea88ba95','addForeignKeyConstraint baseTableName=order_set_member, constraintName=order_set_member_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-891','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',889,'EXECUTED','8:f5dbac0b9db31f6d17e7d9e4b4ecc377','addForeignKeyConstraint baseTableName=order_set_member, constraintName=order_set_member_concept_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-892','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',890,'EXECUTED','8:38c854821271ac80902335e4ee69711b','addForeignKeyConstraint baseTableName=order_set_member, constraintName=order_set_member_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-893','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',891,'EXECUTED','8:2d496e3f4c55684c462f610904dfc668','addForeignKeyConstraint baseTableName=order_set_member, constraintName=order_set_member_order_set_id_fk, referencedTableName=order_set','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-894','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',892,'EXECUTED','8:7eeb953070b3e000bf2c8588532a1ce2','addForeignKeyConstraint baseTableName=order_set_member, constraintName=order_set_member_order_type_fk, referencedTableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-895','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',893,'EXECUTED','8:0526053665c948124eea436de5222a65','addForeignKeyConstraint baseTableName=order_set_member, constraintName=order_set_member_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-896','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',894,'EXECUTED','8:732454ce0f5ed67b04c53e5b9aa3e9c6','addForeignKeyConstraint baseTableName=order_set, constraintName=order_set_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-897','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',895,'EXECUTED','8:435a1e100712f1e93b3b65b003a86ac3','addForeignKeyConstraint baseTableName=order_type, constraintName=order_type_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-898','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',896,'EXECUTED','8:d34a6c90298741958538167827a6b3dd','addForeignKeyConstraint baseTableName=order_type, constraintName=order_type_parent_order_type, referencedTableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-899','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',897,'EXECUTED','8:05590ce6d45e9d21de5f2d145d0b7695','addForeignKeyConstraint baseTableName=orders, constraintName=orders_care_setting, referencedTableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-900','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',898,'EXECUTED','8:35723632c7575cb3d80b418598356632','addForeignKeyConstraint baseTableName=orders, constraintName=orders_in_encounter, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-901','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',899,'EXECUTED','8:b916b89186b29d6ddb09be718e528973','addForeignKeyConstraint baseTableName=orders, constraintName=orders_order_group_id_fk, referencedTableName=order_group','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-902','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:44',900,'EXECUTED','8:50107b202250e00a620f9f972a017fc9','addForeignKeyConstraint baseTableName=cohort_member, constraintName=parent_cohort, referencedTableName=cohort','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-903','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',901,'EXECUTED','8:36d997ad0f295fd015f873b1d130013b','addForeignKeyConstraint baseTableName=location, constraintName=parent_location, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-904','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',902,'EXECUTED','8:b5aa149236a07ecdd72a57127f4a75d2','addForeignKeyConstraint baseTableName=role_role, constraintName=parent_role, referencedTableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-905','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',903,'EXECUTED','8:5d087e8d6ad7c8e2d7c1fb3228468d27','addForeignKeyConstraint baseTableName=person_address, constraintName=patient_address_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-906','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',904,'EXECUTED','8:90a22152501f9631b0014a93179f965a','addForeignKeyConstraint baseTableName=person_address, constraintName=patient_address_void, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-907','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',905,'EXECUTED','8:020f0a732ee5a6f8661f3cafc9fc55b4','addForeignKeyConstraint baseTableName=patient_identifier, constraintName=patient_identifier_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-908','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',906,'EXECUTED','8:6fadd0b9f398428c180326f1473c39de','addForeignKeyConstraint baseTableName=patient_identifier, constraintName=patient_identifier_ibfk_2, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-909','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',907,'EXECUTED','8:43aaee8b48063350605399fc915d314c','addForeignKeyConstraint baseTableName=patient_identifier_type, constraintName=patient_identifier_type_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-910','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',908,'EXECUTED','8:23b0efcc933d062f0118f49b8058cad5','addForeignKeyConstraint baseTableName=patient_program, constraintName=patient_in_program, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-911','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',909,'EXECUTED','8:76e9bc625c11684e3edc195ef8b3b1e9','addForeignKeyConstraint baseTableName=note, constraintName=patient_note, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-912','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',910,'EXECUTED','8:32b8ebd8d130b8f175c87684d8bc2d1c','addForeignKeyConstraint baseTableName=patient_program_attribute, constraintName=patient_program_attribute_attributetype_fk, referencedTableName=program_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-913','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',911,'EXECUTED','8:72fea5869b6141a1cf80603a5de3558d','addForeignKeyConstraint baseTableName=patient_program_attribute, constraintName=patient_program_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-914','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',912,'EXECUTED','8:851e0d7e2d1dbe924624ea405fd6cc94','addForeignKeyConstraint baseTableName=patient_program_attribute, constraintName=patient_program_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-915','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',913,'EXECUTED','8:02385045d5711d24d92f78dc108d99ca','addForeignKeyConstraint baseTableName=patient_program_attribute, constraintName=patient_program_attribute_programid_fk, referencedTableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-916','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',914,'EXECUTED','8:f73483a028df661a2c26d1323f3c8451','addForeignKeyConstraint baseTableName=patient_program_attribute, constraintName=patient_program_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-917','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',915,'EXECUTED','8:a1fe4a7f940aa20f1a6651c1964577c2','addForeignKeyConstraint baseTableName=patient_program, constraintName=patient_program_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-918','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',916,'EXECUTED','8:751d160f00ab311740538463c7cfb69b','addForeignKeyConstraint baseTableName=patient_state, constraintName=patient_program_for_state, referencedTableName=patient_program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-919','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',917,'EXECUTED','8:8b152de3747c8b27f678c4e9387535cf','addForeignKeyConstraint baseTableName=patient_program, constraintName=patient_program_location_id, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-920','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',918,'EXECUTED','8:ca216dc9b815b69e18ab3771a0772427','addForeignKeyConstraint baseTableName=patient_program, constraintName=patient_program_outcome_concept_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-921','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',919,'EXECUTED','8:91f1a39c3b6007783100753e95b429a5','addForeignKeyConstraint baseTableName=patient_state, constraintName=patient_state_changer, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-922','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',920,'EXECUTED','8:12838849537a52d96e46e3745a8997dd','addForeignKeyConstraint baseTableName=patient_state, constraintName=patient_state_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-923','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',921,'EXECUTED','8:684fcf64779002fcb71dca0ccf6e11c4','addForeignKeyConstraint baseTableName=patient_state, constraintName=patient_state_encounter_id_fk, referencedTableName=encounter','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-924','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',922,'EXECUTED','8:598555dff114215f0080878d92cd6a2b','addForeignKeyConstraint baseTableName=patient_state, constraintName=patient_state_voider, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-925','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',923,'EXECUTED','8:ee8e441bcba80d23fc6415d1c9307d5f','addForeignKeyConstraint baseTableName=relationship, constraintName=person_a_is_person, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-926','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',924,'EXECUTED','8:0d06092ed078138eebcbe296d50db88e','addForeignKeyConstraint baseTableName=person_address, constraintName=person_address_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-927','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',925,'EXECUTED','8:c620d324af31116f91d22f7ac9e60440','addForeignKeyConstraint baseTableName=relationship, constraintName=person_b_is_person, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-928','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',926,'EXECUTED','8:9cadafea81dd484831240e834e846934','addForeignKeyConstraint baseTableName=person, constraintName=person_died_because, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-929','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',927,'EXECUTED','8:112516eb2ddcdb1b30ad14897fabd706','addForeignKeyConstraint baseTableName=patient, constraintName=person_id_for_patient, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-930','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',928,'EXECUTED','8:d308c4c8ed3fbf658a0310dc458d6636','addForeignKeyConstraint baseTableName=users, constraintName=person_id_for_user, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-931','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:45',929,'EXECUTED','8:7da7e954b405e74e5686454131f25f6e','addForeignKeyConstraint baseTableName=person_merge_log, constraintName=person_merge_log_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-932','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',930,'EXECUTED','8:cea7193597e608255e8b4d22ba7c8e8e','addForeignKeyConstraint baseTableName=person_merge_log, constraintName=person_merge_log_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-933','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',931,'EXECUTED','8:38c7fea990ed555a7924ac3176269f36','addForeignKeyConstraint baseTableName=person_merge_log, constraintName=person_merge_log_loser, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-934','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',932,'EXECUTED','8:3a6ec5b7592017c6eed57644ba25e332','addForeignKeyConstraint baseTableName=person_merge_log, constraintName=person_merge_log_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-935','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',933,'EXECUTED','8:705eb5a5b5aca27e84dfa405f77431ee','addForeignKeyConstraint baseTableName=person_merge_log, constraintName=person_merge_log_winner, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-936','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',934,'EXECUTED','8:bc7705fb1e89f96b987bd301022e3914','addForeignKeyConstraint baseTableName=obs, constraintName=person_obs, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-937','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',935,'EXECUTED','8:94ab85718e0cb640ea5758c113fdeced','addForeignKeyConstraint baseTableName=orders, constraintName=previous_order_id_order_id, referencedTableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-938','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',936,'EXECUTED','8:ba54d4d21cf2e60c43f3381ade3930cd','addForeignKeyConstraint baseTableName=obs, constraintName=previous_version, referencedTableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-939','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',937,'EXECUTED','8:864c68b497d770d4cfc3106f936987a6','addForeignKeyConstraint baseTableName=drug, constraintName=primary_drug_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-940','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',938,'EXECUTED','8:368d2c6558d4ca7fc3eb5955e1c313b2','addForeignKeyConstraint baseTableName=role_privilege, constraintName=privilege_definitions, referencedTableName=privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-941','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',939,'EXECUTED','8:bec8d14fbcd98750801f07d810eebfd5','addForeignKeyConstraint baseTableName=person_attribute_type, constraintName=privilege_which_can_edit, referencedTableName=privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-942','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',940,'EXECUTED','8:8f5da1efd8164891c226627748b5ae9c','addForeignKeyConstraint baseTableName=encounter_type, constraintName=privilege_which_can_edit_encounter_type, referencedTableName=privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-943','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',941,'EXECUTED','8:28e365841cb092772641e81f39e26c77','addForeignKeyConstraint baseTableName=encounter_type, constraintName=privilege_which_can_view_encounter_type, referencedTableName=privilege','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-944','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',942,'EXECUTED','8:2888047b26688496274be6d6c7ccdbb0','addForeignKeyConstraint baseTableName=program_attribute_type, constraintName=program_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-945','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',943,'EXECUTED','8:b78050728415dfdc0642be01e09f28cd','addForeignKeyConstraint baseTableName=program_attribute_type, constraintName=program_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-946','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',944,'EXECUTED','8:0aaa34b309311e9006bc7b4f98b1893e','addForeignKeyConstraint baseTableName=program_attribute_type, constraintName=program_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-947','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',945,'EXECUTED','8:c757b39c031a0b4f7a1ef80801f234f6','addForeignKeyConstraint baseTableName=program, constraintName=program_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-948','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',946,'EXECUTED','8:85de92efbfe6c6ec80f41a48a0bfaed7','addForeignKeyConstraint baseTableName=program, constraintName=program_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-949','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',947,'EXECUTED','8:8b1f049920652db516dbca511076a9f4','addForeignKeyConstraint baseTableName=patient_program, constraintName=program_for_patient, referencedTableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-950','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',948,'EXECUTED','8:4740603fdc07139d43e29af6daf3cb10','addForeignKeyConstraint baseTableName=program_workflow, constraintName=program_for_workflow, referencedTableName=program','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-951','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',949,'EXECUTED','8:48891931556beb2fc6590306e055d8d8','addForeignKeyConstraint baseTableName=program, constraintName=program_outcomes_concept_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-952','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',950,'EXECUTED','8:873823d50904b587afe3aec452a52329','addForeignKeyConstraint baseTableName=concept_proposal, constraintName=proposal_obs_concept_id, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-953','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',951,'EXECUTED','8:30fd438046736007e73c9c833eeea60b','addForeignKeyConstraint baseTableName=concept_proposal, constraintName=proposal_obs_id, referencedTableName=obs','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-954','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',952,'EXECUTED','8:3877465c4a9799b19b60fa3af3dfbc65','addForeignKeyConstraint baseTableName=provider_attribute, constraintName=provider_attribute_attribute_type_id_fk, referencedTableName=provider_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-955','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',953,'EXECUTED','8:03799fef9c0f5ae043962de95b0dc202','addForeignKeyConstraint baseTableName=provider_attribute, constraintName=provider_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-956','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',954,'EXECUTED','8:1c771ae73e6480bb861a645fe909a131','addForeignKeyConstraint baseTableName=provider_attribute, constraintName=provider_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-957','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',955,'EXECUTED','8:03c94a2f727c43f67d929a4214879a91','addForeignKeyConstraint baseTableName=provider_attribute, constraintName=provider_attribute_provider_fk, referencedTableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-958','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',956,'EXECUTED','8:d227c9a4d2485d4be77681aec1467350','addForeignKeyConstraint baseTableName=provider_attribute_type, constraintName=provider_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-959','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',957,'EXECUTED','8:f0cb284e7145d21ea42788e7ce776bf1','addForeignKeyConstraint baseTableName=provider_attribute_type, constraintName=provider_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-960','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',958,'EXECUTED','8:f1975c9f9588a0f1436bffa9df32b972','addForeignKeyConstraint baseTableName=provider_attribute_type, constraintName=provider_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-961','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',959,'EXECUTED','8:30f3c93397432465637261f6185a9b65','addForeignKeyConstraint baseTableName=provider_attribute, constraintName=provider_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-962','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',960,'EXECUTED','8:aee4d6d3b99903a1245a01bb28d72d9b','addForeignKeyConstraint baseTableName=provider, constraintName=provider_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-963','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',961,'EXECUTED','8:130425bf5258dfe55545e05d7df34595','addForeignKeyConstraint baseTableName=provider, constraintName=provider_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-964','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',962,'EXECUTED','8:e62bd1b601fe168319dbf0a34de6784e','addForeignKeyConstraint baseTableName=encounter_provider, constraintName=provider_id_fk, referencedTableName=provider','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-965','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',963,'EXECUTED','8:e18b12fbe82ba34a22a38958bc5a351b','addForeignKeyConstraint baseTableName=provider, constraintName=provider_person_id_fk, referencedTableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-966','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',964,'EXECUTED','8:0cd186d7421cc9da75a66a01d1a1e5ff','addForeignKeyConstraint baseTableName=provider, constraintName=provider_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-967','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',965,'EXECUTED','8:ef8fb54896552fc0f061ee4d1af8314a','addForeignKeyConstraint baseTableName=provider, constraintName=provider_role_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-968','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',966,'EXECUTED','8:61fb202a141d970ce143baa009dfecb7','addForeignKeyConstraint baseTableName=provider, constraintName=provider_speciality_id_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-969','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:46',967,'EXECUTED','8:fd8476171996a0cee3f0d0f1583e584e','addForeignKeyConstraint baseTableName=referral_order, constraintName=referral_order_frequency_fk, referencedTableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-970','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',968,'EXECUTED','8:fc7a16628919821337d5207e96cc8722','addForeignKeyConstraint baseTableName=referral_order, constraintName=referral_order_location_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-971','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',969,'EXECUTED','8:485189fa2a7650fc52de0840da6bcfc3','addForeignKeyConstraint baseTableName=referral_order, constraintName=referral_order_order_id_fk, referencedTableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-972','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',970,'EXECUTED','8:6889bee4509a8a0891c022d3a096c237','addForeignKeyConstraint baseTableName=referral_order, constraintName=referral_order_specimen_source_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-973','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',971,'EXECUTED','8:ff8404b9b7aeb224be5b903cda3801ba','addForeignKeyConstraint baseTableName=relationship, constraintName=relation_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-974','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',972,'EXECUTED','8:b7535ef52fdec7216712b89bc8617624','addForeignKeyConstraint baseTableName=relationship, constraintName=relation_voider, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-975','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',973,'EXECUTED','8:b17a6b362473678c91ccaa4027b0209a','addForeignKeyConstraint baseTableName=relationship, constraintName=relationship_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-976','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',974,'EXECUTED','8:8f89a8d5c2a956aff04700656c9396e5','addForeignKeyConstraint baseTableName=relationship_type, constraintName=relationship_type_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-977','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',975,'EXECUTED','8:78087e61efe77335148fc92b95840fae','addForeignKeyConstraint baseTableName=relationship, constraintName=relationship_type_id, referencedTableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-978','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',976,'EXECUTED','8:ae0fa4466b4f779155f24ea6e9e825f3','addForeignKeyConstraint baseTableName=report_object, constraintName=report_object_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-979','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',977,'EXECUTED','8:b0297e8d8bc0a4adf11037fbc9af3431','addForeignKeyConstraint baseTableName=user_role, constraintName=role_definitions, referencedTableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-980','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',978,'EXECUTED','8:ed8f5077d539f345d0810adc39aab70b','addForeignKeyConstraint baseTableName=role_privilege, constraintName=role_privilege_to_role, referencedTableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-981','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',979,'EXECUTED','8:6f387fc05df49edb56c2164d482f47f9','addForeignKeyConstraint baseTableName=drug, constraintName=route_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-982','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',980,'EXECUTED','8:4652f64b910edf57a1380bbdf33205b9','addForeignKeyConstraint baseTableName=scheduler_task_config, constraintName=scheduler_changer, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-983','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',981,'EXECUTED','8:bb9e5e2975c8294c6be76b283668c275','addForeignKeyConstraint baseTableName=scheduler_task_config, constraintName=scheduler_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-984','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',982,'EXECUTED','8:ebf77f5dcca8d4f309b902359ee3cc63','addForeignKeyConstraint baseTableName=serialized_object, constraintName=serialized_object_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-985','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',983,'EXECUTED','8:c03789a6b3105e78e8b04beb23db60d4','addForeignKeyConstraint baseTableName=serialized_object, constraintName=serialized_object_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-986','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',984,'EXECUTED','8:ab33850c20390d9a94667bdefb2c84fc','addForeignKeyConstraint baseTableName=serialized_object, constraintName=serialized_object_retired_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-987','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',985,'EXECUTED','8:afc394554a225f746e7e180b5f8fed42','addForeignKeyConstraint baseTableName=program_workflow_state, constraintName=state_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-988','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',986,'EXECUTED','8:43d77120664e32253c6946c918fd24b8','addForeignKeyConstraint baseTableName=program_workflow_state, constraintName=state_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-989','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',987,'EXECUTED','8:ce3e47a7b841fc73bf96504bf7db8749','addForeignKeyConstraint baseTableName=program_workflow_state, constraintName=state_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-990','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',988,'EXECUTED','8:db6e8528879c9f79296e24af5ca075be','addForeignKeyConstraint baseTableName=patient_state, constraintName=state_for_patient, referencedTableName=program_workflow_state','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-991','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',989,'EXECUTED','8:20b95ac353b4e626dc6018013b5663d5','addForeignKeyConstraint baseTableName=scheduler_task_config_property, constraintName=task_config_for_property, referencedTableName=scheduler_task_config','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-992','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',990,'EXECUTED','8:7efca8cad4f197b4e9a393c0ccd6df10','addForeignKeyConstraint baseTableName=test_order, constraintName=test_order_frequency_fk, referencedTableName=order_frequency','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-993','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',991,'EXECUTED','8:699fedb10aefb74af0d177aeac247f5a','addForeignKeyConstraint baseTableName=test_order, constraintName=test_order_location_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-994','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',992,'EXECUTED','8:d15937ce9e41099d41ef0fafed335109','addForeignKeyConstraint baseTableName=test_order, constraintName=test_order_order_id_fk, referencedTableName=orders','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-995','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',993,'EXECUTED','8:96eb9f492204d23790dda339e5c9a7d7','addForeignKeyConstraint baseTableName=test_order, constraintName=test_order_specimen_source_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-996','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',994,'EXECUTED','8:e777121d9258621b5e3e221ada4b50ee','addForeignKeyConstraint baseTableName=order_type, constraintName=type_created_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-997','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',995,'EXECUTED','8:b6e18134ff3b5765f88fa3a6b9d2292c','addForeignKeyConstraint baseTableName=patient_identifier_type, constraintName=type_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-998','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',996,'EXECUTED','8:95bdd7fc9a1d53ee4e557767ad15a255','addForeignKeyConstraint baseTableName=field, constraintName=type_of_field, referencedTableName=field_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-999','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',997,'EXECUTED','8:106718fd2565c4e74c81f51608a81e2f','addForeignKeyConstraint baseTableName=orders, constraintName=type_of_order, referencedTableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1000','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',998,'EXECUTED','8:77ff24d236ce4e83e4b139ee0a2b7857','addForeignKeyConstraint baseTableName=users, constraintName=user_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1001','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',999,'EXECUTED','8:36496add7c8e546ef289e5dbcccc4f53','addForeignKeyConstraint baseTableName=user_property, constraintName=user_property_to_users, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1002','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',1000,'EXECUTED','8:59966c2252770ba8f91be47557324da7','addForeignKeyConstraint baseTableName=user_role, constraintName=user_role_to_users, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1003','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',1001,'EXECUTED','8:8602f3a50d36ad38b3923fbdf2e65f80','addForeignKeyConstraint baseTableName=patient_program, constraintName=user_who_changed, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1004','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',1002,'EXECUTED','8:2411670a2f9854c2dc182090bafdb6e2','addForeignKeyConstraint baseTableName=notification_alert, constraintName=user_who_changed_alert, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1005','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',1003,'EXECUTED','8:b7c5cf87111b45ecc5eb4957c895d551','addForeignKeyConstraint baseTableName=cohort, constraintName=user_who_changed_cohort, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1006','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',1004,'EXECUTED','8:a38338459266c6758781e42e267282e9','addForeignKeyConstraint baseTableName=concept, constraintName=user_who_changed_concept, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1007','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',1005,'EXECUTED','8:517ba79b2e58821cfbb633c7a432257f','addForeignKeyConstraint baseTableName=concept_description, constraintName=user_who_changed_description, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1008','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',1006,'EXECUTED','8:1218284e419acdc1c2c9158fc505760f','addForeignKeyConstraint baseTableName=drug_reference_map, constraintName=user_who_changed_drug_reference_map, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1009','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:47',1007,'EXECUTED','8:9b1303dc86a4937b111ff0924ca58816','addForeignKeyConstraint baseTableName=field, constraintName=user_who_changed_field, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1010','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1008,'EXECUTED','8:f101e27d192a0eb9dec0517ea8af6267','addForeignKeyConstraint baseTableName=note, constraintName=user_who_changed_note, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1011','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1009,'EXECUTED','8:7b004f293df92626e6d28c7e7f0f2b0d','addForeignKeyConstraint baseTableName=patient, constraintName=user_who_changed_pat, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1012','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1010,'EXECUTED','8:27366629e9d449b737e4a7e1caa52826','addForeignKeyConstraint baseTableName=person, constraintName=user_who_changed_person, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1013','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1011,'EXECUTED','8:51f5fa0d3c3f1178d78eca763ecbf657','addForeignKeyConstraint baseTableName=program, constraintName=user_who_changed_program, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1014','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1012,'EXECUTED','8:95bfdeb0859dc01f919e413e1c3e44de','addForeignKeyConstraint baseTableName=concept_proposal, constraintName=user_who_changed_proposal, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1015','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1013,'EXECUTED','8:7ce2c902ec94326aaee23059c4389aff','addForeignKeyConstraint baseTableName=report_object, constraintName=user_who_changed_report_object, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1016','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1014,'EXECUTED','8:f0e86572f9011a1d9bb0123ae9ccf843','addForeignKeyConstraint baseTableName=users, constraintName=user_who_changed_user, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1017','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1015,'EXECUTED','8:f47fdd4ea85c023d3b66d0550667a7f9','addForeignKeyConstraint baseTableName=concept_set, constraintName=user_who_created, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1018','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1016,'EXECUTED','8:b8fcf2ac41670a9793e894f08b071d15','addForeignKeyConstraint baseTableName=concept_description, constraintName=user_who_created_description, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1019','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1017,'EXECUTED','8:8a7d6bbae90553077ddf45dcbe5a51e2','addForeignKeyConstraint baseTableName=field, constraintName=user_who_created_field, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1020','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1018,'EXECUTED','8:b009990ac5bd183936b4f522f0052c08','addForeignKeyConstraint baseTableName=field_answer, constraintName=user_who_created_field_answer, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1021','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1019,'EXECUTED','8:dcafbee6699d224b99e0938948016001','addForeignKeyConstraint baseTableName=field_type, constraintName=user_who_created_field_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1022','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1020,'EXECUTED','8:7088c7abae9366bcada546e8d38c2fc7','addForeignKeyConstraint baseTableName=form, constraintName=user_who_created_form, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1023','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1021,'EXECUTED','8:076b36f0dfe98f1aecf50fb60319b2b3','addForeignKeyConstraint baseTableName=form_field, constraintName=user_who_created_form_field, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1024','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1022,'EXECUTED','8:2ffd8fd5073b127999b7c77524e26568','addForeignKeyConstraint baseTableName=hl7_source, constraintName=user_who_created_hl7_source, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1025','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1023,'EXECUTED','8:e29ec5f0b4cd7b0d7b5e16b3561c9f86','addForeignKeyConstraint baseTableName=location, constraintName=user_who_created_location, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1026','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1024,'EXECUTED','8:8c201b88de87df5ac321b202cd46c87a','addForeignKeyConstraint baseTableName=concept_name, constraintName=user_who_created_name, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1027','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1025,'EXECUTED','8:f306d1317e3adcf71faf8a5253a2804a','addForeignKeyConstraint baseTableName=note, constraintName=user_who_created_note, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1028','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1026,'EXECUTED','8:36a4cb95348cdb42c1faa5fff6e0a2f0','addForeignKeyConstraint baseTableName=patient, constraintName=user_who_created_patient, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1029','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1027,'EXECUTED','8:3d51e7e09960c2f167d3ccfd325b684b','addForeignKeyConstraint baseTableName=person, constraintName=user_who_created_person, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1030','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1028,'EXECUTED','8:9889d4ba6279c6c46bf99fa4b44a497a','addForeignKeyConstraint baseTableName=concept_proposal, constraintName=user_who_created_proposal, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1031','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1029,'EXECUTED','8:631b9e2ca82364f3851f43e515d030c8','addForeignKeyConstraint baseTableName=relationship_type, constraintName=user_who_created_rel, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1032','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1030,'EXECUTED','8:99ba86b3b1ed8eca70b86a80c60e9ea2','addForeignKeyConstraint baseTableName=encounter_type, constraintName=user_who_created_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1033','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1031,'EXECUTED','8:e263f26377e04c5eaf990141c659426f','addForeignKeyConstraint baseTableName=form, constraintName=user_who_last_changed_form, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1034','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1032,'EXECUTED','8:69baf6d56760f324322ea154cd7b6e9d','addForeignKeyConstraint baseTableName=form_field, constraintName=user_who_last_changed_form_field, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1035','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1033,'EXECUTED','8:33378ea2cc2d531fa6896c1f7ffe89ce','addForeignKeyConstraint baseTableName=person_name, constraintName=user_who_made_name, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1036','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1034,'EXECUTED','8:085f124f4e2470919cc13dc948677744','addForeignKeyConstraint baseTableName=concept, constraintName=user_who_retired_concept, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1037','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1035,'EXECUTED','8:7a76f208d58c03138ba1121debe02887','addForeignKeyConstraint baseTableName=concept_class, constraintName=user_who_retired_concept_class, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1038','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1036,'EXECUTED','8:5f94b99552467c24aee8f94e6647c083','addForeignKeyConstraint baseTableName=concept_datatype, constraintName=user_who_retired_concept_datatype, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1039','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1037,'EXECUTED','8:e071e0bbc8f0b2e7d9393743df7bb572','addForeignKeyConstraint baseTableName=concept_reference_source, constraintName=user_who_retired_concept_source, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1040','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1038,'EXECUTED','8:b6845ac52006a6db339f0bba8b284294','addForeignKeyConstraint baseTableName=drug_reference_map, constraintName=user_who_retired_drug_reference_map, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1041','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1039,'EXECUTED','8:357083bc1fba4415682131a64ebd9878','addForeignKeyConstraint baseTableName=encounter_type, constraintName=user_who_retired_encounter_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1042','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1040,'EXECUTED','8:280f08a02dde8204ebb872fd1af22cd9','addForeignKeyConstraint baseTableName=field, constraintName=user_who_retired_field, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1043','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1041,'EXECUTED','8:43d0cfcfb7702147a9022dd9e65e80cc','addForeignKeyConstraint baseTableName=form, constraintName=user_who_retired_form, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1044','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1042,'EXECUTED','8:1304d05f67d6ce15e45c7cbb1a6dc007','addForeignKeyConstraint baseTableName=location, constraintName=user_who_retired_location, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1045','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1043,'EXECUTED','8:c974d9f1a5794c6e465876321c62baa8','addForeignKeyConstraint baseTableName=order_type, constraintName=user_who_retired_order_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1046','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1044,'EXECUTED','8:21ced5e3d600731dc773eb3799397f26','addForeignKeyConstraint baseTableName=patient_identifier_type, constraintName=user_who_retired_patient_identifier_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1047','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1045,'EXECUTED','8:a16ec0f5694584f4d391beab8c1b024d','addForeignKeyConstraint baseTableName=person_attribute_type, constraintName=user_who_retired_person_attribute_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1048','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1046,'EXECUTED','8:24cf680ad246c318a513b8f933761418','addForeignKeyConstraint baseTableName=relationship_type, constraintName=user_who_retired_relationship_type, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1049','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1047,'EXECUTED','8:2e729344b24c164838b036860da3071d','addForeignKeyConstraint baseTableName=users, constraintName=user_who_retired_this_user, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1050','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1048,'EXECUTED','8:c3b47f085ee1c554733559c865c20b14','addForeignKeyConstraint baseTableName=cohort, constraintName=user_who_voided_cohort, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1051','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1049,'EXECUTED','8:53cd75514719543e6b43ff55de401797','addForeignKeyConstraint baseTableName=encounter, constraintName=user_who_voided_encounter, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1052','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1050,'EXECUTED','8:c57620a91d3f54cae590929f3dcad33e','addForeignKeyConstraint baseTableName=person_name, constraintName=user_who_voided_name, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1053','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:48',1051,'EXECUTED','8:372a49dc1af12aebc3461dfd3849ffa1','addForeignKeyConstraint baseTableName=obs, constraintName=user_who_voided_obs, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1054','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1052,'EXECUTED','8:8a9f7042da846cc80057cc02efd2e510','addForeignKeyConstraint baseTableName=orders, constraintName=user_who_voided_order, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1055','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1053,'EXECUTED','8:e2a1ef8d5bc27124c509a3a1333fbd3d','addForeignKeyConstraint baseTableName=patient, constraintName=user_who_voided_patient, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1056','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1054,'EXECUTED','8:3ed24fd80fa06dac2c49d40903af9e97','addForeignKeyConstraint baseTableName=patient_program, constraintName=user_who_voided_patient_program, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1057','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1055,'EXECUTED','8:cf72bfec4f97d3d83163191c5db5e287','addForeignKeyConstraint baseTableName=person, constraintName=user_who_voided_person, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1058','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1056,'EXECUTED','8:d7105e8d63af1e2d234a22b3081e8ed3','addForeignKeyConstraint baseTableName=report_object, constraintName=user_who_voided_report_object, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1059','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1057,'EXECUTED','8:1bbb32c6cfc1794a5db7952ef5f0f1e9','addForeignKeyConstraint baseTableName=concept_name, constraintName=user_who_voided_this_name, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1060','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1058,'EXECUTED','8:9a3d956b0d13cf53387fb83083b64a14','addForeignKeyConstraint baseTableName=visit_attribute, constraintName=visit_attribute_attribute_type_id_fk, referencedTableName=visit_attribute_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1061','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1059,'EXECUTED','8:741755734c210fa644166e6ee82b1b01','addForeignKeyConstraint baseTableName=visit_attribute, constraintName=visit_attribute_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1062','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1060,'EXECUTED','8:a019be47cce0d75a13cd8d36c3644ab5','addForeignKeyConstraint baseTableName=visit_attribute, constraintName=visit_attribute_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1063','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1061,'EXECUTED','8:968b89848268b8f160b6f7ae0f121ef3','addForeignKeyConstraint baseTableName=visit_attribute_type, constraintName=visit_attribute_type_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1064','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1062,'EXECUTED','8:bef70d726c43ccae39ed7f4da3d879a1','addForeignKeyConstraint baseTableName=visit_attribute_type, constraintName=visit_attribute_type_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1065','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1063,'EXECUTED','8:4046bcdbac255468446a94061c1c6d05','addForeignKeyConstraint baseTableName=visit_attribute_type, constraintName=visit_attribute_type_retired_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1066','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1064,'EXECUTED','8:86fe77ed791220c2e07e48d2194c1bcc','addForeignKeyConstraint baseTableName=visit_attribute, constraintName=visit_attribute_visit_fk, referencedTableName=visit','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1067','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1065,'EXECUTED','8:1f20c1b226184660b36f681f46ae1dea','addForeignKeyConstraint baseTableName=visit_attribute, constraintName=visit_attribute_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1068','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1066,'EXECUTED','8:48ef11adc19c3774bf7c9ee3bc901dd0','addForeignKeyConstraint baseTableName=visit, constraintName=visit_changed_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1069','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1067,'EXECUTED','8:70d01b1643c8c1ba0a8c99d2762bfec2','addForeignKeyConstraint baseTableName=visit, constraintName=visit_creator_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1070','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1068,'EXECUTED','8:c363012eb8513835980abf5c690f32c9','addForeignKeyConstraint baseTableName=visit, constraintName=visit_indication_concept_fk, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1071','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1069,'EXECUTED','8:8991b8fdf04c1910e9e864a7bdd1d620','addForeignKeyConstraint baseTableName=visit, constraintName=visit_location_fk, referencedTableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1072','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1070,'EXECUTED','8:37d6df6fce98e57fbacfa56264e0bfc3','addForeignKeyConstraint baseTableName=visit, constraintName=visit_patient_fk, referencedTableName=patient','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1073','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1071,'EXECUTED','8:663a07b153a05ad3e4f32d6544c20e1e','addForeignKeyConstraint baseTableName=visit_type, constraintName=visit_type_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1074','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1072,'EXECUTED','8:9f8b48b4cc01c4ffb044e3b4ddf00341','addForeignKeyConstraint baseTableName=visit_type, constraintName=visit_type_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1075','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1073,'EXECUTED','8:417a8b642747bbff10ed4ecdf095da74','addForeignKeyConstraint baseTableName=visit, constraintName=visit_type_fk, referencedTableName=visit_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1076','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1074,'EXECUTED','8:53ac855d10a18def31c32ef717582e1a','addForeignKeyConstraint baseTableName=visit_type, constraintName=visit_type_retired_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1077','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1075,'EXECUTED','8:99eb27815589c6c9e80d8ac4bb8d6a6a','addForeignKeyConstraint baseTableName=visit, constraintName=visit_voided_by_fk, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1078','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1076,'EXECUTED','8:eec82397d9dfa7cf4d937440ae0f0405','addForeignKeyConstraint baseTableName=program_workflow, constraintName=workflow_changed_by, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1079','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1077,'EXECUTED','8:56783b7bfc7556dcd750925352aca137','addForeignKeyConstraint baseTableName=program_workflow, constraintName=workflow_concept, referencedTableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1080','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1078,'EXECUTED','8:f7f4682878845e23d811d83940fa0a2d','addForeignKeyConstraint baseTableName=program_workflow, constraintName=workflow_creator, referencedTableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357630219-1081','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.5.x.xml','2022-09-01 13:17:49',1079,'EXECUTED','8:d977b0f4353d94104dce1117148d438b','addForeignKeyConstraint baseTableName=program_workflow_state, constraintName=workflow_for_state, referencedTableName=program_workflow','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-17','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:49',1080,'EXECUTED','8:9deaeac785ac922da287ba26ba28eb00','insert tableName=person','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-27','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:49',1081,'EXECUTED','8:d55c78b0aa979676e79af22dd21ef490','insert tableName=users; insert tableName=users','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-1','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:49',1082,'EXECUTED','8:78248bfd4fa70706049cf833181fd562','insert tableName=care_setting; insert tableName=care_setting','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-3','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:49',1083,'EXECUTED','8:7e03c0f867cbb788a1ed6698663b507a','insert tableName=concept_class; insert tableName=concept_class; insert tableName=concept_class; insert tableName=concept_class; insert tableName=concept_class; insert tableName=concept_class; insert tableName=concept_class; insert tableName=concep...','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-4','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1084,'EXECUTED','8:1a2264e9d014b4ab9420bbf886ce389c','insert tableName=concept_datatype; insert tableName=concept_datatype; insert tableName=concept_datatype; insert tableName=concept_datatype; insert tableName=concept_datatype; insert tableName=concept_datatype; insert tableName=concept_datatype; in...','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-2','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1085,'EXECUTED','8:b476eb139d9c2d0019b22e0406edce71','insert tableName=concept; insert tableName=concept','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-5','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1086,'EXECUTED','8:c9c0f57b80f38a6337f965c97f01778d','insert tableName=concept_map_type; insert tableName=concept_map_type; insert tableName=concept_map_type; insert tableName=concept_map_type; insert tableName=concept_map_type; insert tableName=concept_map_type; insert tableName=concept_map_type; in...','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-6','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1087,'EXECUTED','8:58de2c5dcbc1bc8afaa03cf83c5d8bb3','insert tableName=concept_name; insert tableName=concept_name; insert tableName=concept_name; insert tableName=concept_name; insert tableName=concept_name; insert tableName=concept_name; insert tableName=concept_name; insert tableName=concept_name;...','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-7','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1088,'EXECUTED','8:f20b5d7db739e72d0254bcc5badd9393','insert tableName=concept_stop_word; insert tableName=concept_stop_word; insert tableName=concept_stop_word; insert tableName=concept_stop_word; insert tableName=concept_stop_word; insert tableName=concept_stop_word; insert tableName=concept_stop_w...','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-8','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1089,'EXECUTED','8:157ab8bd3f2b4df2166ed867fd0c20e3','insert tableName=encounter_role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-9','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1090,'EXECUTED','8:7d7b82a60c99724ff4bdac9ba04e4e72','insert tableName=field_type; insert tableName=field_type; insert tableName=field_type; insert tableName=field_type; insert tableName=field_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-10','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1091,'EXECUTED','8:faafdcb2c1b2772a9e4b7d43b0eecbc6','insert tableName=global_property; insert tableName=global_property; insert tableName=global_property; insert tableName=global_property; insert tableName=global_property; insert tableName=global_property; insert tableName=global_property; insert ta...','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-11','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1092,'EXECUTED','8:1c8270fc058ae357426167c11c86cda7','insert tableName=hl7_source','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-14','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1093,'EXECUTED','8:eb00371748c0c26308cb003e39f5909e','insert tableName=location','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-15','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1094,'EXECUTED','8:bcef322e3a2f1f304e83b9201fae3e80','insert tableName=order_type; insert tableName=order_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-16','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1095,'EXECUTED','8:87939b3ed1ec7f77ca2f37ae0f74f18b','insert tableName=patient_identifier_type; insert tableName=patient_identifier_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-18','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1096,'EXECUTED','8:3230c80eadaba64bcb4091b10e4cf3bb','insert tableName=person_attribute_type; insert tableName=person_attribute_type; insert tableName=person_attribute_type; insert tableName=person_attribute_type; insert tableName=person_attribute_type; insert tableName=person_attribute_type; insert ...','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-19','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1097,'EXECUTED','8:484bb93da0cd3c11c533df5ab56bf59d','insert tableName=person_name','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-20','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1098,'EXECUTED','8:14f0d8fa4ecdd6633fadce1944e27cd2','insert tableName=privilege; insert tableName=privilege; insert tableName=privilege; insert tableName=privilege; insert tableName=privilege; insert tableName=privilege; insert tableName=privilege; insert tableName=privilege; insert tableName=privil...','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-21','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1099,'EXECUTED','8:15c9b7deeca206ce09ae4f22cd0a4fe8','insert tableName=relationship_type; insert tableName=relationship_type; insert tableName=relationship_type; insert tableName=relationship_type','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-22','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1100,'EXECUTED','8:3e059f57b088f635d2c19d2e3cbb488e','insert tableName=role; insert tableName=role; insert tableName=role; insert tableName=role','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-23','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1101,'EXECUTED','8:21180f55b63fb7d74dc53d35b827746b','insert tableName=role_privilege; insert tableName=role_privilege; insert tableName=role_privilege; insert tableName=role_privilege; insert tableName=role_privilege; insert tableName=role_privilege; insert tableName=role_privilege; insert tableName...','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-24','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1102,'EXECUTED','8:dfc2f9a352adc0327feb7de3ef50a47a','insert tableName=scheduler_task_config','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-25','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1103,'EXECUTED','8:78b09b530a915c9ed205b1950a622f66','insert tableName=user_property; insert tableName=user_property; insert tableName=user_property','',NULL,'4.4.3',NULL,NULL,NULL),('1644357753728-26','danielkayiwa (generated)','org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.5.x.xml','2022-09-01 13:17:50',1104,'EXECUTED','8:c2366d8db8ef9d8a623360430d7a1115','insert tableName=user_role; insert tableName=user_role','',NULL,'4.4.3',NULL,NULL,NULL),('TRUNK-6070-2022-03-10','slubwama','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.6.x.xml','2022-09-01 13:17:50',1105,'EXECUTED','8:8679e5f0d918804ba26997c9b8ea7854','addColumn tableName=patient_identifier; addForeignKeyConstraint baseTableName=patient_identifier, constraintName=patient_identifier_program_id_fk, referencedTableName=patient_program','Adding \'patient_program_id\' column to \'patient_identifier\' table',NULL,'4.4.3',NULL,NULL,NULL),('TRUNK-6071-20220406','PIH','org/openmrs/liquibase/updates/liquibase-update-to-latest-2.6.x.xml','2022-09-01 13:17:50',1106,'EXECUTED','8:1d3c92f4442c6eef1a6c4c4433592632','createTable tableName=medication_dispense; addForeignKeyConstraint baseTableName=medication_dispense, constraintName=medication_dispense_patient_fk, referencedTableName=patient; addForeignKeyConstraint baseTableName=medication_dispense, constraint...','Creating medication_dispense table',NULL,'4.4.3',NULL,NULL,NULL);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  KEY `location_type_fk` (`location_type_concept_id`),
  KEY `name_of_location` (`name`),
  KEY `parent_location` (`parent_location`),
  KEY `user_who_created_location` (`creator`),
  KEY `user_who_retired_location` (`retired_by`),
  CONSTRAINT `location_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `location_type_fk` FOREIGN KEY (`location_type_concept_id`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `parent_location` FOREIGN KEY (`parent_location`) REFERENCES `location` (`location_id`),
  CONSTRAINT `user_who_created_location` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_who_retired_location` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location_tag_map`
--

LOCK TABLES `location_tag_map` WRITE;
/*!40000 ALTER TABLE `location_tag_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `location_tag_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medication_dispense`
--

DROP TABLE IF EXISTS `medication_dispense`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `medication_dispense` (
  `medication_dispense_id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` char(38) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `encounter_id` int(11) DEFAULT NULL,
  `concept` int(11) NOT NULL,
  `drug_id` int(11) DEFAULT NULL,
  `location_id` int(11) DEFAULT NULL,
  `dispenser` int(11) DEFAULT NULL,
  `drug_order_id` int(11) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `status_reason` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `quantity` double DEFAULT NULL,
  `quantity_units` int(11) DEFAULT NULL,
  `dose` double DEFAULT NULL,
  `dose_units` int(11) DEFAULT NULL,
  `route` int(11) DEFAULT NULL,
  `frequency` int(11) DEFAULT NULL,
  `as_needed` tinyint(1) DEFAULT NULL,
  `dosing_instructions` text DEFAULT NULL,
  `date_prepared` datetime DEFAULT NULL,
  `date_handed_over` datetime DEFAULT NULL,
  `was_substituted` tinyint(1) DEFAULT NULL,
  `substitution_type` int(11) DEFAULT NULL,
  `substitution_reason` int(11) DEFAULT NULL,
  `form_namespace_and_path` varchar(255) DEFAULT NULL,
  `creator` int(11) NOT NULL,
  `date_created` datetime NOT NULL,
  `changed_by` int(11) DEFAULT NULL,
  `date_changed` datetime DEFAULT NULL,
  `voided` tinyint(1) NOT NULL DEFAULT 0,
  `voided_by` int(11) DEFAULT NULL,
  `date_voided` datetime DEFAULT NULL,
  `void_reason` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`medication_dispense_id`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `medication_dispense_patient_fk` (`patient_id`),
  KEY `medication_dispense_encounter_fk` (`encounter_id`),
  KEY `medication_dispense_concept_fk` (`concept`),
  KEY `medication_dispense_drug_fk` (`drug_id`),
  KEY `medication_dispense_location_fk` (`location_id`),
  KEY `medication_dispense_dispenser_fk` (`dispenser`),
  KEY `medication_dispense_drug_order_fk` (`drug_order_id`),
  KEY `medication_dispense_status_fk` (`status`),
  KEY `medication_dispense_status_reason_fk` (`status_reason`),
  KEY `medication_dispense_type_fk` (`type`),
  KEY `medication_dispense_quantity_units_fk` (`quantity_units`),
  KEY `medication_dispense_dose_units_fk` (`dose_units`),
  KEY `medication_dispense_route_fk` (`route`),
  KEY `medication_dispense_frequency_fk` (`frequency`),
  KEY `medication_dispense_substitution_type_fk` (`substitution_type`),
  KEY `medication_dispense_substitution_reason_fk` (`substitution_reason`),
  KEY `medication_dispense_creator_fk` (`creator`),
  KEY `medication_dispense_changed_by_fk` (`changed_by`),
  KEY `medication_dispense_voided_by_fk` (`voided_by`),
  CONSTRAINT `medication_dispense_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `medication_dispense_concept_fk` FOREIGN KEY (`concept`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `medication_dispense_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `medication_dispense_dispenser_fk` FOREIGN KEY (`dispenser`) REFERENCES `provider` (`provider_id`),
  CONSTRAINT `medication_dispense_dose_units_fk` FOREIGN KEY (`dose_units`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `medication_dispense_drug_fk` FOREIGN KEY (`drug_id`) REFERENCES `drug` (`drug_id`),
  CONSTRAINT `medication_dispense_drug_order_fk` FOREIGN KEY (`drug_order_id`) REFERENCES `drug_order` (`order_id`),
  CONSTRAINT `medication_dispense_encounter_fk` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
  CONSTRAINT `medication_dispense_frequency_fk` FOREIGN KEY (`frequency`) REFERENCES `order_frequency` (`order_frequency_id`),
  CONSTRAINT `medication_dispense_location_fk` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`),
  CONSTRAINT `medication_dispense_patient_fk` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `medication_dispense_quantity_units_fk` FOREIGN KEY (`quantity_units`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `medication_dispense_route_fk` FOREIGN KEY (`route`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `medication_dispense_status_fk` FOREIGN KEY (`status`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `medication_dispense_status_reason_fk` FOREIGN KEY (`status_reason`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `medication_dispense_substitution_reason_fk` FOREIGN KEY (`substitution_reason`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `medication_dispense_substitution_type_fk` FOREIGN KEY (`substitution_type`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `medication_dispense_type_fk` FOREIGN KEY (`type`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `medication_dispense_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medication_dispense`
--

LOCK TABLES `medication_dispense` WRITE;
/*!40000 ALTER TABLE `medication_dispense` DISABLE KEYS */;
/*!40000 ALTER TABLE `medication_dispense` ENABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  UNIQUE KEY `uuid_order_attribute` (`uuid`),
  KEY `order_attribute_attribute_type_id_fk` (`attribute_type_id`),
  KEY `order_attribute_changed_by_fk` (`changed_by`),
  KEY `order_attribute_creator_fk` (`creator`),
  KEY `order_attribute_order_fk` (`order_id`),
  KEY `order_attribute_voided_by_fk` (`voided_by`),
  CONSTRAINT `order_attribute_attribute_type_id_fk` FOREIGN KEY (`attribute_type_id`) REFERENCES `order_attribute_type` (`order_attribute_type_id`),
  CONSTRAINT `order_attribute_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `order_attribute_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `order_attribute_order_fk` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `order_attribute_voided_by_fk` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  UNIQUE KEY `order_attribute_type_name` (`name`),
  UNIQUE KEY `uuid_order_attribute_type` (`uuid`),
  KEY `order_attribute_type_changed_by_fk` (`changed_by`),
  KEY `order_attribute_type_creator_fk` (`creator`),
  KEY `order_attribute_type_retired_by_fk` (`retired_by`),
  CONSTRAINT `order_attribute_type_changed_by_fk` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `order_attribute_type_creator_fk` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `order_attribute_type_retired_by_fk` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  `patient_program_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`patient_identifier_id`),
  UNIQUE KEY `uuid_patient_identifier` (`uuid`),
  KEY `defines_identifier_type` (`identifier_type`),
  KEY `identifier_creator` (`creator`),
  KEY `identifier_name` (`identifier`),
  KEY `identifier_voider` (`voided_by`),
  KEY `idx_patient_identifier_patient` (`patient_id`),
  KEY `patient_identifier_changed_by` (`changed_by`),
  KEY `patient_identifier_ibfk_2` (`location_id`),
  KEY `patient_identifier_program_id_fk` (`patient_program_id`),
  CONSTRAINT `defines_identifier_type` FOREIGN KEY (`identifier_type`) REFERENCES `patient_identifier_type` (`patient_identifier_type_id`),
  CONSTRAINT `fk_patient_id_patient_identifier` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
  CONSTRAINT `identifier_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `identifier_voider` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `patient_identifier_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `patient_identifier_ibfk_2` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`),
  CONSTRAINT `patient_identifier_program_id_fk` FOREIGN KEY (`patient_program_id`) REFERENCES `patient_program` (`patient_program_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  KEY `patient_state_encounter_id_fk` (`encounter_id`),
  KEY `patient_state_voider` (`voided_by`),
  KEY `state_for_patient` (`state`),
  CONSTRAINT `patient_program_for_state` FOREIGN KEY (`patient_program_id`) REFERENCES `patient_program` (`patient_program_id`),
  CONSTRAINT `patient_state_changer` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `patient_state_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `patient_state_encounter_id_fk` FOREIGN KEY (`encounter_id`) REFERENCES `encounter` (`encounter_id`),
  CONSTRAINT `patient_state_voider` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `state_for_patient` FOREIGN KEY (`state`) REFERENCES `program_workflow_state` (`program_workflow_state_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `privilege`
--

LOCK TABLES `privilege` WRITE;
/*!40000 ALTER TABLE `privilege` DISABLE KEYS */;
INSERT INTO `privilege` VALUES ('Add Allergies','Add allergies','8d04fc61-45de-4d16-a2f7-39ccbda88739'),('Add Cohorts','Able to add a cohort to the system','5f8a2896-6814-11e8-923f-e9a88dcb533f'),('Add Concept Proposals','Able to add concept proposals to the system','5f8a2a1c-6814-11e8-923f-e9a88dcb533f'),('Add Encounters','Able to add patient encounters','5f8a2a8a-6814-11e8-923f-e9a88dcb533f'),('Add HL7 Inbound Archive','Able to add an HL7 archive item','3192b2c9-3844-42f7-8e8e-f8964768ca26'),('Add HL7 Inbound Exception','Able to add an HL7 error item','e41c644c-1866-4572-a78d-6bfe36dd6a4b'),('Add HL7 Inbound Queue','Able to add an HL7 Queue item','b20ee658-9faa-4c6a-bc95-b5a9a4bfa4c2'),('Add HL7 Source','Able to add an HL7 Source','b8a2625f-3130-4b15-a3ba-907600d1bb03'),('Add Observations','Able to add patient observations','5f8a2ada-6814-11e8-923f-e9a88dcb533f'),('Add Orders','Able to add orders','5f8a2b7a-6814-11e8-923f-e9a88dcb533f'),('Add Patient Identifiers','Able to add patient identifiers','5f8a2bc0-6814-11e8-923f-e9a88dcb533f'),('Add Patient Programs','Able to add patients to programs','5f8a2c10-6814-11e8-923f-e9a88dcb533f'),('Add Patients','Able to add patients','5f8a2c56-6814-11e8-923f-e9a88dcb533f'),('Add People','Able to add person objects','5f8a2c92-6814-11e8-923f-e9a88dcb533f'),('Add Problems','Add problems','2f6d256a-1f73-4776-ad5b-f94c80f7beea'),('Add Relationships','Able to add relationships','5f8a2cce-6814-11e8-923f-e9a88dcb533f'),('Add Report Objects','Able to add report objects','5f8a2cf6-6814-11e8-923f-e9a88dcb533f'),('Add Reports','Able to add reports','5f8a2d32-6814-11e8-923f-e9a88dcb533f'),('Add Users','Able to add users to OpenMRS','5f8a2d5a-6814-11e8-923f-e9a88dcb533f'),('Add Visits','Able to add visits','65d14b28-3989-11e6-899a-a4d646d86a8a'),('Assign System Developer Role','Able to assign System Developer role','09cbaabc-ae63-44ed-9d4d-34ec6610e0a5'),('Configure Visits','Able to choose encounter visit handler and enable/disable encounter visits','d7899df6-68d7-49f1-bc3a-a1a64be157d6'),('Delete Cohorts','Able to add a cohort to the system','5f8a2d8c-6814-11e8-923f-e9a88dcb533f'),('Delete Concept Proposals','Able to delete concept proposals from the system','5f8a2dbe-6814-11e8-923f-e9a88dcb533f'),('Delete Conditions','Able to delete conditions','ef725ffa-2d14-41d8-b655-2d2430963b21'),('Delete Diagnoses','Able to delete diagnoses','a7882352-41b4-4da7-b422-84470ede7052'),('Delete Encounters','Able to delete patient encounters','5f8a2de6-6814-11e8-923f-e9a88dcb533f'),('Delete HL7 Inbound Archive','Able to delete/retire an HL7 archive item','7cd1f479-4e6a-422d-8b4d-7a7e5758d253'),('Delete HL7 Inbound Exception','Able to delete an HL7 archive item','25109fd5-edd1-4d11-84fe-a50b4001b50b'),('Delete HL7 Inbound Queue','Able to delete an HL7 Queue item','0e39bf44-a08b-4d61-9d20-08d2cc0ab51e'),('Delete Medication Dispense','Able to delete Medication Dispenses','e3461e96-393b-47ce-9672-025d031e2268'),('Delete Notes','Able to delete patient notes','ff39bade-4ea2-463b-8a66-ff4b26f85f25'),('Delete Observations','Able to delete patient observations','5f8a2e18-6814-11e8-923f-e9a88dcb533f'),('Delete Orders','Able to delete orders','5f8a2e40-6814-11e8-923f-e9a88dcb533f'),('Delete Patient Identifiers','Able to delete patient identifiers','5f8a2e72-6814-11e8-923f-e9a88dcb533f'),('Delete Patient Programs','Able to delete patients from programs','5f8a2ea4-6814-11e8-923f-e9a88dcb533f'),('Delete Patients','Able to delete patients','5f8a2ecc-6814-11e8-923f-e9a88dcb533f'),('Delete People','Able to delete objects','5f8a2efe-6814-11e8-923f-e9a88dcb533f'),('Delete Relationships','Able to delete relationships','5f8a2f26-6814-11e8-923f-e9a88dcb533f'),('Delete Report Objects','Able to delete report objects','5f8a2f58-6814-11e8-923f-e9a88dcb533f'),('Delete Reports','Able to delete reports','5f8a2f8a-6814-11e8-923f-e9a88dcb533f'),('Delete Users','Able to delete users in OpenMRS','5f8a2fb2-6814-11e8-923f-e9a88dcb533f'),('Delete Visits','Able to delete visits','02d02051-931b-4325-88d9-bf1a6cc85f5d'),('Edit Allergies','Able to edit allergies','507e9f66-ee25-4113-a55a-4b050022f55a'),('Edit Cohorts','Able to add a cohort to the system','5f8a2fe4-6814-11e8-923f-e9a88dcb533f'),('Edit Concept Proposals','Able to edit concept proposals in the system','5f8a300c-6814-11e8-923f-e9a88dcb533f'),('Edit Conditions','Able to edit conditions','910ec2ec-a87b-4528-ad5c-02c500068bf0'),('Edit Diagnoses','Able to edit diagnoses','107f5169-77a2-48d3-9f69-9f2ca9b419ca'),('Edit Encounters','Able to edit patient encounters','5f8a303e-6814-11e8-923f-e9a88dcb533f'),('Edit Medication Dispense','Able to edit Medication Dispenses','3eb234fe-3415-49e2-8988-d80b7695b41a'),('Edit Notes','Able to edit patient notes','888fe070-549c-429a-9784-edbaf42e3229'),('Edit Observations','Able to edit patient observations','5f8a3066-6814-11e8-923f-e9a88dcb533f'),('Edit Orders','Able to edit orders','5f8a308e-6814-11e8-923f-e9a88dcb533f'),('Edit Patient Identifiers','Able to edit patient identifiers','5f8a30c0-6814-11e8-923f-e9a88dcb533f'),('Edit Patient Programs','Able to edit patients in programs','5f8a30e8-6814-11e8-923f-e9a88dcb533f'),('Edit Patients','Able to edit patients','5f8a311a-6814-11e8-923f-e9a88dcb533f'),('Edit People','Able to edit person objects','5f8a3142-6814-11e8-923f-e9a88dcb533f'),('Edit Problems','Able to edit problems','5131daf7-2c4d-4c2b-8f6e-bf7cf8290444'),('Edit Relationships','Able to edit relationships','5f8a3174-6814-11e8-923f-e9a88dcb533f'),('Edit Report Objects','Able to edit report objects','5f8a319c-6814-11e8-923f-e9a88dcb533f'),('Edit Reports','Able to edit reports','5f8a31ce-6814-11e8-923f-e9a88dcb533f'),('Edit User Passwords','Able to change the passwords of users in OpenMRS','5f8a31f6-6814-11e8-923f-e9a88dcb533f'),('Edit Users','Able to edit users in OpenMRS','5f8a3228-6814-11e8-923f-e9a88dcb533f'),('Edit Visits','Able to edit visits','14f4da3c-d1e1-4571-b379-0b533496f5b7'),('Form Entry','Allows user to access Form Entry pages/functions','5f8a3250-6814-11e8-923f-e9a88dcb533f'),('Get Allergies','Able to get allergies','d05118c6-2490-4d78-a41a-390e3596a220'),('Get Care Settings','Able to get Care Settings','1ab26030-a207-4a22-be52-b40be3e401dd'),('Get Concept Attribute Types','Able to get concept attribute types','498317ea-e55f-4488-9e87-0db1349c3d11'),('Get Concept Classes','Able to get concept classes','d05118c6-2490-4d78-a41a-390e3596a238'),('Get Concept Datatypes','Able to get concept datatypes','d05118c6-2490-4d78-a41a-390e3596a237'),('Get Concept Map Types','Able to get concept map types','d05118c6-2490-4d78-a41a-390e3596a230'),('Get Concept Proposals','Able to get concept proposals to the system','d05118c6-2490-4d78-a41a-390e3596a250'),('Get Concept Reference Terms','Able to get concept reference terms','d05118c6-2490-4d78-a41a-390e3596a229'),('Get Concept Sources','Able to get concept sources','d05118c6-2490-4d78-a41a-390e3596a231'),('Get Concepts','Able to get concept entries','d05118c6-2490-4d78-a41a-390e3596a251'),('Get Conditions','Able to get conditions','6b470c18-04e8-42c5-bc34-0ac871a0beb6'),('Get Database Changes','Able to get database changes from the admin screen','d05118c6-2490-4d78-a41a-390e3596a222'),('Get Diagnoses','Able to get diagnoses','77b0b402-c928-4811-b084-f0ef7087a49c'),('Get Diagnoses Attribute Types','Able to get diagnoses attribute types','e2b4b047-e595-4456-8ae1-8c16c4f23adc'),('Get Encounter Roles','Able to get encounter roles','d05118c6-2490-4d78-a41a-390e3596a210'),('Get Encounter Types','Able to get encounter types','d05118c6-2490-4d78-a41a-390e3596a247'),('Get Encounters','Able to get patient encounters','d05118c6-2490-4d78-a41a-390e3596a248'),('Get Field Types','Able to get field types','d05118c6-2490-4d78-a41a-390e3596a234'),('Get Forms','Able to get forms','d05118c6-2490-4d78-a41a-390e3596a240'),('Get Global Properties','Able to get global properties on the administration screen','d05118c6-2490-4d78-a41a-390e3596a226'),('Get HL7 Inbound Archive','Able to get an HL7 archive item','d05118c6-2490-4d78-a41a-390e3596a217'),('Get HL7 Inbound Exception','Able to get an HL7 error item','d05118c6-2490-4d78-a41a-390e3596a216'),('Get HL7 Inbound Queue','Able to get an HL7 Queue item','d05118c6-2490-4d78-a41a-390e3596a218'),('Get HL7 Source','Able to get an HL7 Source','d05118c6-2490-4d78-a41a-390e3596a219'),('Get Identifier Types','Able to get patient identifier types','d05118c6-2490-4d78-a41a-390e3596a239'),('Get Location Attribute Types','Able to get location attribute types','d05118c6-2490-4d78-a41a-390e3596a212'),('Get Locations','Able to get locations','d05118c6-2490-4d78-a41a-390e3596a246'),('Get Medication Dispense','Able to get Medication Dispenses','923f059d-0b29-4903-8846-d037bb2b446a'),('Get Notes','Able to get patient notes','dcd11774-e77c-48d5-8f34-685741dec359'),('Get Observations','Able to get patient observations','d05118c6-2490-4d78-a41a-390e3596a245'),('Get Order Frequencies','Able to get Order Frequencies','c78007dd-c641-400b-9aac-04420aecc5b6'),('Get Order Set Attribute Types','Able to get order set attribute types','bfc2eca6-fa0a-4700-b5ba-3bbe5a44413d'),('Get Order Sets','Able to get order sets','e52af909-2baf-4ab6-9862-8a6848448ec0'),('Get Order Types','Able to get order types','d05118c6-2490-4d78-a41a-390e3596a233'),('Get Orders','Able to get orders','d05118c6-2490-4d78-a41a-390e3596a241'),('Get Patient Cohorts','Able to get patient cohorts','d05118c6-2490-4d78-a41a-390e3596a242'),('Get Patient Identifiers','Able to get patient identifiers','d05118c6-2490-4d78-a41a-390e3596a243'),('Get Patient Programs','Able to get which programs that patients are in','d05118c6-2490-4d78-a41a-390e3596a227'),('Get Patients','Able to get patients','d05118c6-2490-4d78-a41a-390e3596a244'),('Get People','Able to get person objects','d05118c6-2490-4d78-a41a-390e3596a224'),('Get Person Attribute Types','Able to get person attribute types','d05118c6-2490-4d78-a41a-390e3596a225'),('Get Privileges','Able to get user privileges','d05118c6-2490-4d78-a41a-390e3596a236'),('Get Problems','Able to get problems','d05118c6-2490-4d78-a41a-390e3596a221'),('Get Programs','Able to get patient programs','d05118c6-2490-4d78-a41a-390e3596a228'),('Get Providers','Able to get Providers','d05118c6-2490-4d78-a41a-390e3596a211'),('Get Relationship Types','Able to get relationship types','d05118c6-2490-4d78-a41a-390e3596a232'),('Get Relationships','Able to get relationships','d05118c6-2490-4d78-a41a-390e3596a223'),('Get Roles','Able to get user roles','d05118c6-2490-4d78-a41a-390e3596a235'),('Get Users','Able to get users in OpenMRS','d05118c6-2490-4d78-a41a-390e3596a249'),('Get Visit Attribute Types','Able to get visit attribute types','d05118c6-2490-4d78-a41a-390e3596a213'),('Get Visit Types','Able to get visit types','d05118c6-2490-4d78-a41a-390e3596a215'),('Get Visits','Able to get visits','d05118c6-2490-4d78-a41a-390e3596a214'),('Manage Address Templates','Able to add/edit/delete address templates','fb21b9ee-fd8a-47b6-8656-bd3a68a44925'),('Manage Alerts','Able to add/edit/delete user alerts','5f8a3282-6814-11e8-923f-e9a88dcb533f'),('Manage Concept Attribute Types','Able to add/edit/retire concept attribute types','7550fbcb-cb61-4a9f-94d5-eb376afc727f'),('Manage Concept Classes','Able to add/edit/retire concept classes','5f8a32aa-6814-11e8-923f-e9a88dcb533f'),('Manage Concept Datatypes','Able to add/edit/retire concept datatypes','5f8a32dc-6814-11e8-923f-e9a88dcb533f'),('Manage Concept Map Types','Able to add/edit/retire concept map types','385096c5-6492-41f3-8304-c0144e8fb2e6'),('Manage Concept Name tags','Able to add/edit/delete concept name tags','401cc09a-84c6-4d9e-bf93-4659837edbde'),('Manage Concept Reference Terms','Able to add/edit/retire reference terms','80e45896-d4cd-48a1-b35e-30709bab36b6'),('Manage Concept Sources','Able to add/edit/delete concept sources','5f8a3304-6814-11e8-923f-e9a88dcb533f'),('Manage Concept Stop Words','Able to view/add/remove the concept stop words','47b5b16e-0ff5-4a4a-ae26-947af73c88ca'),('Manage Concepts','Able to add/edit/delete concept entries','5f8a3336-6814-11e8-923f-e9a88dcb533f'),('Manage Encounter Roles','Able to add/edit/retire encounter roles','0fbaddea-b053-4841-a1f1-2ca93fd71d9a'),('Manage Encounter Types','Able to add/edit/delete encounter types','5f8a3368-6814-11e8-923f-e9a88dcb533f'),('Manage Field Types','Able to add/edit/retire field types','5f8a3390-6814-11e8-923f-e9a88dcb533f'),('Manage FormEntry XSN','Allows user to upload and edit the xsns stored on the server','5f8a33b8-6814-11e8-923f-e9a88dcb533f'),('Manage Forms','Able to add/edit/delete forms','5f8a33ea-6814-11e8-923f-e9a88dcb533f'),('Manage Global Properties','Able to add/edit global properties','5f8a341c-6814-11e8-923f-e9a88dcb533f'),('Manage HL7 Messages','Able to add/edit/delete HL7 messages','e43b8830-7c95-42e6-8259-538fec951c66'),('Manage Identifier Types','Able to add/edit/delete patient identifier types','5f8a3444-6814-11e8-923f-e9a88dcb533f'),('Manage Implementation Id','Able to view/add/edit the implementation id for the system','20e102e1-74b4-4198-905e-9b598c8474f1'),('Manage Location Attribute Types','Able to add/edit/retire location attribute types','5c266720-20c2-4fa2-89d3-56886176d63a'),('Manage Location Tags','Able to add/edit/delete location tags','d6d83d7f-9241-42e6-9689-393920a4f733'),('Manage Locations','Able to add/edit/delete locations','5f8a3476-6814-11e8-923f-e9a88dcb533f'),('Manage Modules','Able to add/remove modules to the system','5f8a349e-6814-11e8-923f-e9a88dcb533f'),('Manage Order Frequencies','Able to add/edit/retire Order Frequencies','e3a5205d-ab12-40ca-9160-9ba02198e389'),('Manage Order Set Attribute Types','Able to add/edit/retire order set attribute types','35360a98-eebf-4cc0-812d-80b6025cfea3'),('Manage Order Sets','Able to manage order sets','059955e6-014f-4e50-b198-24e179784025'),('Manage Order Types','Able to add/edit/retire order types','5f8a34c6-6814-11e8-923f-e9a88dcb533f'),('Manage Person Attribute Types','Able to add/edit/delete person attribute types','5f8a34f8-6814-11e8-923f-e9a88dcb533f'),('Manage Privileges','Able to add/edit/delete privileges','5f8a3520-6814-11e8-923f-e9a88dcb533f'),('Manage Programs','Able to add/view/delete patient programs','5f8a3552-6814-11e8-923f-e9a88dcb533f'),('Manage Providers','Able to edit Provider','b0b6fe18-9940-42b4-80fc-b05e6ee546f5'),('Manage Relationship Types','Able to add/edit/retire relationship types','5f8a357a-6814-11e8-923f-e9a88dcb533f'),('Manage Relationships','Able to add/edit/delete relationships','5f8a35ac-6814-11e8-923f-e9a88dcb533f'),('Manage Roles','Able to add/edit/delete user roles','5f8a35d4-6814-11e8-923f-e9a88dcb533f'),('Manage Scheduler','Able to add/edit/remove scheduled tasks','5f8a3606-6814-11e8-923f-e9a88dcb533f'),('Manage Search Index','Able to manage the search index','657bfbbb-a008-43f3-85ac-3163d201b389'),('Manage Visit Attribute Types','Able to add/edit/retire visit attribute types','d0e19ed6-a299-4435-ab8f-2ea76e22fcc4'),('Manage Visit Types','Able to add/edit/delete visit types','f9196849-164f-4522-80d8-488d36faeec2'),('Patient Dashboard - View Demographics Section','Able to view the \'Demographics\' tab on the patient dashboard','5f8a362e-6814-11e8-923f-e9a88dcb533f'),('Patient Dashboard - View Encounters Section','Able to view the \'Encounters\' tab on the patient dashboard','5f8a3660-6814-11e8-923f-e9a88dcb533f'),('Patient Dashboard - View Forms Section','Allows user to view the Forms tab on the patient dashboard','5f8a3692-6814-11e8-923f-e9a88dcb533f'),('Patient Dashboard - View Graphs Section','Able to view the \'Graphs\' tab on the patient dashboard','5f8a36ce-6814-11e8-923f-e9a88dcb533f'),('Patient Dashboard - View Overview Section','Able to view the \'Overview\' tab on the patient dashboard','5f8a3700-6814-11e8-923f-e9a88dcb533f'),('Patient Dashboard - View Patient Summary','Able to view the \'Summary\' tab on the patient dashboard','5f8a3732-6814-11e8-923f-e9a88dcb533f'),('Patient Dashboard - View Regimen Section','Able to view the \'Regimen\' tab on the patient dashboard','5f8a375a-6814-11e8-923f-e9a88dcb533f'),('Patient Overview - View Allergies','Able to view the Allergies portlet on the patient overview tab','d05118c6-2490-4d78-a41a-390e3596a261'),('Patient Overview - View Patient Actions','Able to view the Patient Actions portlet on the patient overview tab','d05118c6-2490-4d78-a41a-390e3596a264'),('Patient Overview - View Problem List','Able to view the Problem List portlet on the patient overview tab','d05118c6-2490-4d78-a41a-390e3596a260'),('Patient Overview - View Programs','Able to view the Programs portlet on the patient overview tab','d05118c6-2490-4d78-a41a-390e3596a263'),('Patient Overview - View Relationships','Able to view the Relationships portlet on the patient overview tab','d05118c6-2490-4d78-a41a-390e3596a262'),('Purge Field Types','Able to purge field types','5f8a3796-6814-11e8-923f-e9a88dcb533f'),('Remove Allergies','Remove allergies','dbdea8f5-9fca-4527-9ef6-5fa03eebf2bd'),('Remove Problems','Remove problems','28ea7e84-e21a-4031-9f9f-2d3eda9d1200'),('Task: Modify Allergies','Able to add, edit, delete allergies','eeb9108e-6905-4712-a13c-b9435db4abcb'),('Update HL7 Inbound Archive','Able to update an HL7 archive item','5dd8306f-eeb4-430e-b5da-a6e02bcafb79'),('Update HL7 Inbound Exception','Able to update an HL7 archive item','e2614f6d-a730-4292-9c10-baf6d912500f'),('Update HL7 Inbound Queue','Able to update an HL7 Queue item','f82b7723-110e-47d3-a35b-4552ea1bff9e'),('Update HL7 Source','Able to update an HL7 Source','6e0abfb7-e95c-4efd-82a0-00a02b0e8335'),('Upload XSN','Allows user to upload/overwrite the XSNs defined for forms','5f8a37c8-6814-11e8-923f-e9a88dcb533f'),('View Administration Functions','Able to view the \'Administration\' link in the navigation bar','5f8a37fa-6814-11e8-923f-e9a88dcb533f'),('View Allergies','Able to view allergies in OpenMRS','5f8a382c-6814-11e8-923f-e9a88dcb533f'),('View Concept Classes','Able to view concept classes','5f8a3958-6814-11e8-923f-e9a88dcb533f'),('View Concept Datatypes','Able to view concept datatypes','5f8a398a-6814-11e8-923f-e9a88dcb533f'),('View Concept Proposals','Able to view concept proposals to the system','5f8a39bc-6814-11e8-923f-e9a88dcb533f'),('View Concept Sources','Able to view concept sources','5f8a39ee-6814-11e8-923f-e9a88dcb533f'),('View Concepts','Able to view concept entries','5f8a3a16-6814-11e8-923f-e9a88dcb533f'),('View Data Entry Statistics','Able to view data entry statistics from the admin screen','5f8a3a48-6814-11e8-923f-e9a88dcb533f'),('View Encounter Types','Able to view encounter types','5f8a3a7a-6814-11e8-923f-e9a88dcb533f'),('View Encounters','Able to view patient encounters','5f8a3aa2-6814-11e8-923f-e9a88dcb533f'),('View Field Types','Able to view field types','5f8a3ad4-6814-11e8-923f-e9a88dcb533f'),('View Forms','Able to view forms','5f8a3afc-6814-11e8-923f-e9a88dcb533f'),('View Global Properties','Able to view global properties on the administration screen','5f8a3b2e-6814-11e8-923f-e9a88dcb533f'),('View Identifier Types','Able to view patient identifier types','5f8a3b60-6814-11e8-923f-e9a88dcb533f'),('View Locations','Able to view locations','5f8a3b88-6814-11e8-923f-e9a88dcb533f'),('View Navigation Menu','Ability to see the navigation menu','5f8a3bba-6814-11e8-923f-e9a88dcb533f'),('View Observations','Able to view patient observations','5f8a3be2-6814-11e8-923f-e9a88dcb533f'),('View Order Types','Able to view order types','5f8a3c14-6814-11e8-923f-e9a88dcb533f'),('View Orders','Able to view orders','5f8a3c46-6814-11e8-923f-e9a88dcb533f'),('View Patient Cohorts','Able to view patient cohorts','5f8a3c6e-6814-11e8-923f-e9a88dcb533f'),('View Patient Identifiers','Able to view patient identifiers','5f8a3ca0-6814-11e8-923f-e9a88dcb533f'),('View Patient Programs','Able to see which programs that patients are in','5f8a3cd2-6814-11e8-923f-e9a88dcb533f'),('View Patients','Able to view patients','5f8a3cfa-6814-11e8-923f-e9a88dcb533f'),('View People','Able to view person objects','5f8a3d2c-6814-11e8-923f-e9a88dcb533f'),('View Person Attribute Types','Able to view person attribute types','5f8a3d5e-6814-11e8-923f-e9a88dcb533f'),('View Privileges','Able to view user privileges','5f8a3d86-6814-11e8-923f-e9a88dcb533f'),('View Problems','Able to view problems in OpenMRS','5f8a3db8-6814-11e8-923f-e9a88dcb533f'),('View Programs','Able to view patient programs','5f8a3de0-6814-11e8-923f-e9a88dcb533f'),('View Relationship Types','Able to view relationship types','5f8a3e12-6814-11e8-923f-e9a88dcb533f'),('View Relationships','Able to view relationships','5f8a3e44-6814-11e8-923f-e9a88dcb533f'),('View Report Objects','Able to view report objects','5f8a3e6c-6814-11e8-923f-e9a88dcb533f'),('View Reports','Able to view reports','5f8a3e9e-6814-11e8-923f-e9a88dcb533f'),('View Roles','Able to view user roles','5f8a3ec6-6814-11e8-923f-e9a88dcb533f'),('View Unpublished Forms','Able to view and fill out unpublished forms','5f8a3ef8-6814-11e8-923f-e9a88dcb533f'),('View Users','Able to view users in OpenMRS','5f8a3f2a-6814-11e8-923f-e9a88dcb533f');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  KEY `referral_order_frequency_index` (`frequency`),
  KEY `referral_order_location_fk` (`location`),
  KEY `referral_order_specimen_source_index` (`specimen_source`),
  CONSTRAINT `referral_order_frequency_fk` FOREIGN KEY (`frequency`) REFERENCES `order_frequency` (`order_frequency_id`),
  CONSTRAINT `referral_order_location_fk` FOREIGN KEY (`location`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `referral_order_order_id_fk` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `referral_order_specimen_source_fk` FOREIGN KEY (`specimen_source`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  KEY `test_order_location_fk` (`location`),
  KEY `test_order_specimen_source_fk` (`specimen_source`),
  CONSTRAINT `test_order_frequency_fk` FOREIGN KEY (`frequency`) REFERENCES `order_frequency` (`order_frequency_id`),
  CONSTRAINT `test_order_location_fk` FOREIGN KEY (`location`) REFERENCES `concept` (`concept_id`),
  CONSTRAINT `test_order_order_id_fk` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `test_order_specimen_source_fk` FOREIGN KEY (`specimen_source`) REFERENCES `concept` (`concept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','','e57803f05704936892416eeb894a261aa7bb0f3327b81d51bb1c1dc8e229a98c4515de3e9de652d045b8e004fbfb4cfaa458374a6e65ca3658d97e92059e5f89','bdf469e93449e50a829ed46e9c9f39e926f3483acd8f0b79718d6d48cd72ba5f8cb8d4006127e51c5588663ba77114c566fd94d5a64d9e3e9c85ef8525e66b07',NULL,NULL,1,'2005-01-01 00:00:00',1,'2022-09-01 13:17:57',1,0,NULL,NULL,NULL,'82f18b44-6814-11e8-923f-e9a88dcb533f',NULL,NULL),(2,'daemon','daemon',NULL,NULL,NULL,NULL,1,'2010-04-26 13:25:00',NULL,NULL,1,0,NULL,NULL,NULL,'A4F30A1B-5EB9-11DF-A648-37A07F9C90FB',NULL,NULL);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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

-- Dump completed on 2022-09-01 13:23:55
