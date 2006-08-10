#------------------------------------------------------
# USE:
#  The diffs are ordered by datamodel version number.
#  Find your datamodel version and run all sections
#  that follow it.
#------------------------------------------------------

#--------------------------------------
# OpenMRS Datamodel version 1.0.10
# Paul Biondich Apr 11 2006 12:50 PM
# Reporting table added
#--------------------------------------

CREATE TABLE IF NOT EXISTS `report_object` (
  `report_object_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `description` varchar(1000) default NULL,
  `type` varchar(255) NOT NULL,
  `sub_type` varchar(255) NOT NULL,
  `xml_data` text default NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='';

update `global_property` set property_value='1.0.10' where property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.11
# Ben Wolfe Apr 19 2006 5:00 PM
# Alert Functionality added
#-----------------------------------

CREATE TABLE IF NOT EXISTS `alert` (
  `alert_id` int(11) NOT NULL auto_increment,
  `user_id` int(11) default NULL,
  `role` varchar(50) default NULL,
  `text` varchar(512) NOT NULL,
  `date_to_expire` datetime default NULL,
  `creator` int(11) NOT NULL,
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,

  PRIMARY KEY (`alert_id`),

  KEY `alert_creator` (`creator`),
  KEY `alert_assigned_to_user` (`user_id`),
  KEY `alert_assigned_to_role` (`role`),
  KEY `user_who_changed_alert` (`changed_by`),

  CONSTRAINT `alert_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `alert_assigned_to_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `alert_assigned_to_role` FOREIGN KEY (`role`) REFERENCES `role` (`role`),
  CONSTRAINT `user_who_changed_alert` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='';

CREATE TABLE IF NOT EXISTS `alert_read` (
  `alert_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `date_read` timestamp NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,

  PRIMARY KEY (`alert_id`, `user_id`),

  CONSTRAINT `alert_read` FOREIGN KEY (`alert_id`) REFERENCES `alert` (`alert_id`),
  CONSTRAINT `alert_read_by_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='';

update `global_property` set property_value='1.0.11' where property = 'database_version';


#------------------------------------
# OpenMRS Datamodel version 1.0.12
# Paul Biondich Apr 21 2006 11:29 AM
# Drug autoincrement added
#------------------------------------

ALTER TABLE `drug` MODIFY COLUMN `drug_id` int(11) NOT NULL auto_increment;
update `global_property` set property_value='1.0.12' where property = 'database_version';


#---------------------------------------
# OpenMRS Datamodel version 1.0.13
# Darius Jazayeri Apr 21 2006 5:40 PM
# report_object.type keyword resolution
#---------------------------------------

ALTER TABLE `report_object` CHANGE COLUMN `type` `report_object_type` varchar(255) NOT NULL;
ALTER TABLE `report_object` CHANGE COLUMN `sub_type` `report_object_sub_type` varchar(255) NOT NULL;
update `global_property` set property_value='1.0.13' where property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.14
# Ben Wolfe    Apr 24 2006 5:40 PM
# Added obs.value_drug
#-----------------------------------

ALTER TABLE `obs` ADD COLUMN `value_drug` int(11) default NULL AFTER `value_coded`;
ALTER TABLE `obs` ADD INDEX `answer_concept_drug` (`value_drug`);
ALTER TABLE `obs` ADD CONSTRAINT `answer_concept_drug` FOREIGN KEY (`value_drug`) REFERENCES `drug` (`drug_id`);
update `global_property` set property_value='1.0.14' where property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.15
# Burke Mamlin  Apr 25 2006 5:47 AM
# Added form.template
#-----------------------------------

ALTER TABLE `form` ADD COLUMN `template` mediumtext default NULL AFTER `schema_namespace`;
update `global_property` set property_value='1.0.15' where property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.16
# Ben Wolfe    May 1 2006 9:15 AM
# Added database indexes (Directed towards patient merging)
#-----------------------------------

ALTER TABLE `patient_name` ADD INDEX `first_name` (`given_name`);
ALTER TABLE `patient_name` ADD INDEX  `middle_name` (`middle_name`);
ALTER TABLE `patient_name` ADD INDEX  `last_name` (`family_name`);
ALTER TABLE `patient` ADD INDEX `birthdate` (`birthdate`);
ALTER TABLE `patient_identifier` ADD INDEX `identifier_name` (`identifier`);
update `global_property` set property_value='1.0.16' where property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.17
# Justin Miranda    May 1 2006 5:02 PM
# Added scheduler_task_config,
# schedule_task_config_properties,
# and notification_template files.
#-----------------------------------

CREATE TABLE `notification_template` ( 
  `template_id` int(11) NOT NULL auto_increment,
  `name` varchar(50),
  `template` text,
  `subject` varchar(100) default NULL,
  `sender` varchar(255) default NULL,
  `recipients` varchar(512) default NULL,
  `ordinal` int(11) default 0,
  primary key (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `scheduler_task_config` (
  `task_config_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `description` varchar(1024) DEFAULT NULL,
  `schedulable_class` text DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `start_time_pattern` varchar(50) DEFAULT NULL,  
  `repeat_interval` int(11) NOT NULL default '0',
  `start_on_startup` int(1) NOT NULL default '0',
  `started` int(1) NOT NULL default '0',
  `created_by` int(11) default '0',
  `date_created` datetime default '2005-01-01 00:00:00',
  `changed_by` int(11) default NULL,
  `date_changed` datetime default NULL,
  PRIMARY KEY (`task_config_id`),
  KEY `schedule_creator` (`created_by`),
  KEY `schedule_changer` (`changed_by`),
  CONSTRAINT `scheduler_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `scheduler_changer` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `scheduler_task_config_property` (
   `task_config_property_id`  int(11) NOT NULL auto_increment,
   `name`                  varchar(255) NOT NULL,
   `value`                  text DEFAULT NULL,
   `task_config_id`      int(11),
   PRIMARY KEY (`task_config_property_id`),
   KEY `task_config` (`task_config_id`),
   CONSTRAINT `task_config_for_property` FOREIGN KEY (`task_config_id`) REFERENCES `scheduler_task_config` (`task_config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# Sample data (including the form entry and HL7 processor tasks)
insert into scheduler_task_config (task_config_id,name,description,schedulable_class,start_time,repeat_interval,start_on_startup,started,created_by,date_created,changed_by,date_changed) values (1,'Process Form Entry Queue','Processes form entry queue.','org.openmrs.scheduler.tasks.ProcessFormEntryQueueTask','2006-04-24 00:00:00',30,0,0,1,'2006-04-24 00:00:00',null,null);
insert into scheduler_task_config (task_config_id,name,description,schedulable_class,start_time,repeat_interval,start_on_startup,started,created_by,date_created,changed_by,date_changed) values (2,'Process HL7 Task','Processes HL7 messages.','org.openmrs.scheduler.tasks.ProcessHL7InQueueTask','2006-04-24 00:00:00',30,0,0,1,'2006-04-24 00:00:00',null,null);
insert into scheduler_task_config (task_config_id,name,description,schedulable_class,start_time,repeat_interval,start_on_startup,started,created_by,date_created,changed_by,date_changed) values (3,'Alert Reminder Task','Sends email to users who have not checked their alerts.  Set to run every ten minutes.','org.openmrs.scheduler.tasks.AlertReminderTask','2006-04-24 00:00:00',600,0,0,1,'2006-04-24 00:00:00',null,null);
insert into scheduler_task_config (task_config_id,name,description,schedulable_class,start_time,repeat_interval,start_on_startup,started,created_by,date_created,changed_by,date_changed) values (4,'Send Email Task','Doesn''t do anything yet.','org.openmrs.scheduler.tasks.SendEmailTask','2006-04-24 00:00:00',600,0,0,1,'2006-04-24 00:00:00',null,null);
insert into scheduler_task_config (task_config_id,name,description,schedulable_class,start_time,repeat_interval,start_on_startup,started,created_by,date_created,changed_by,date_changed) values (5,'Hello World Task','Writes ''hello world'' to log.  Demonstrates problem caused by spawning a thread from a timer task.','org.openmrs.scheduler.tasks.HelloWorldTask','2006-04-24 00:00:00',600,0,0,1,'2006-04-24 00:00:00',null,null);
insert into scheduler_task_config (task_config_id,name,description,schedulable_class,start_time,repeat_interval,start_on_startup,started,created_by,date_created,changed_by,date_changed) values (6,'Check Internet Connectivity Task','Checks the external internet connection every ten minutes.  This is a trivial task that checks the connection to Google over port 80.  If the connection fails, we assume the internet is done and raise an alert.','org.openmrs.scheduler.tasks.CheckInternetConnectivityTask','2006-04-24 00:00:00',60,0,0,1,'2006-04-24 00:00:00',null,null);

update `global_property` set property_value='1.0.17' where property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.18
# Ben Wolfe     May 8 2006 8:30 AM
# Modified alert tables
#-----------------------------------

ALTER TABLE `alert_read` DROP FOREIGN KEY `alert_read`;

ALTER TABLE `alert` RENAME TO `notification_alert`;
ALTER TABLE `notification_alert` DROP FOREIGN KEY `alert_assigned_to_role`;
ALTER TABLE `notification_alert` DROP INDEX `alert_assigned_to_role`;
ALTER TABLE `notification_alert` DROP COLUMN `role`;
ALTER TABLE `notification_alert` ADD COLUMN `satisfied_by_any` int(1) NOT NULL default '0' AFTER `text`;
ALTER TABLE `notification_alert` ADD COLUMN `alert_read` int(1) NOT NULL default '0' AFTER `satisfied_by_any`;

ALTER TABLE `alert_read` RENAME TO `notification_alert_recipient`;
ALTER TABLE `notification_alert_recipient` ADD COLUMN `alert_read` int(1) NOT NULL default '0' AFTER `user_id`;
ALTER TABLE `notification_alert_recipient` CHANGE COLUMN `date_read` `date_changed` timestamp NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP;
ALTER TABLE `notification_alert_recipient` ADD INDEX `id_of_alert` (`alert_id`);
ALTER TABLE `notification_alert_recipient` ADD CONSTRAINT `id_of_alert` FOREIGN KEY (`alert_id`) REFERENCES `notification_alert` (`alert_id`);

UPDATE `notification_alert_recipient` SET alert_read = 1;

UPDATE `global_property` SET property_value='1.0.18' WHERE property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.21
# Ben Wolfe    May 25 2006 9:40 AM
# Added patient.dead
#-----------------------------------

ALTER TABLE `patient` ADD COLUMN `dead` int(1) NOT NULL default '0' AFTER `civil_status`;

UPDATE `patient` SET `dead` = 1 WHERE `death_date` IS NOT NULL;
UPDATE `patient` p SET `dead` = 1 WHERE `cause_of_death` IS NOT NULL AND `cause_of_death` <> '' AND NOT EXISTS (SELECT e.encounter_id FROM encounter e WHERE e.patient_id = p.patient_id);
UPDATE `global_property` SET property_value='1.0.21' WHERE property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.22
# Ben Wolfe   May 26 2006 10:00 AM
# Moved concept_class.is_set to concept.is_set
#-----------------------------------

ALTER TABLE `concept` ADD COLUMN `is_set` tinyint(1) NOT NULL default '0' AFTER `class_id`;
UPDATE `concept` c, `concept_class` class SET c.`is_set` = class.`is_set` WHERE class.`concept_class_id` = c.`class_id`;
ALTER TABLE `concept_class` DROP COLUMN `is_set`;

UPDATE `global_property` SET property_value='1.0.22' WHERE property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.23
# Ben Wolfe   June 19 2006 8:45 AM
# Make encounters voidable
#-----------------------------------

ALTER TABLE `encounter` ADD COLUMN `voided` tinyint(1) NOT NULL default '0';
ALTER TABLE `encounter` ADD COLUMN `voided_by` int(11) default NULL;
ALTER TABLE `encounter` ADD COLUMN `date_voided` datetime default NULL;
ALTER TABLE `encounter` ADD COLUMN `void_reason` varchar(255) default NULL;
ALTER TABLE `encounter` ADD INDEX `user_who_voided_encounter` (`voided_by`);
ALTER TABLE `encounter` ADD CONSTRAINT `user_who_voided_encounter` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`);

UPDATE `global_property` SET property_value='1.0.23' WHERE property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.26
# Ben Wolfe   July 20 2006 8:45 PM
# Add form_field.sort_weight
#-----------------------------------

ALTER TABLE `form_field` ADD COLUMN `sort_weight` float(11,5) default NULL;

SET @new_weight=0;
UPDATE form_field SET sort_weight = (select @new_weight := @new_weight + 10 from dual) ORDER BY form_id, parent_form_field, field_number, field_part, (select name from field where field_id = form_field.field_id);

UPDATE `global_property` SET property_value='1.0.26' WHERE property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.27
# Ben Wolfe     Aug 2 2006 9:55 AM
# Removed form.infopath_solution_version
#-----------------------------------

ALTER TABLE form DROP COLUMN infopath_solution_version;

UPDATE `global_property` SET property_value='1.0.27' WHERE property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.28
# Darius Jazayeri August 02 2006 6:28 PM
# Initial pass at Programs
#-----------------------------------

CREATE TABLE `program` (
 `program_id` int(11) NOT NULL auto_increment,
 `concept_id` int(11) NOT NULL default '0',
 `creator` int(11) NOT NULL default '0',
 `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
 PRIMARY KEY  (`program_id`),
 KEY `program_concept` (`concept_id`),
 KEY `program_creator` (`creator`),
 CONSTRAINT `program_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
 CONSTRAINT `program_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
 PRIMARY KEY (`patient_program_id`),
 KEY `patient_in_program` (`patient_id`),
 KEY `program_for_patient` (`program_id`),
 KEY `patient_program_creator` (`creator`),
 KEY `user_who_changed` (`changed_by`),
 CONSTRAINT `patient_in_program` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`),
 CONSTRAINT `program_for_patient` FOREIGN KEY (`program_id`) REFERENCES `program` (`program_id`),
 CONSTRAINT `patient_program_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
 CONSTRAINT `user_who_changed` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

UPDATE `global_property` SET property_value='1.0.28' WHERE property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.29
# Burke Mamlin     Aug 2 2006 11:07 AM
# Removed form.schema_namespace and form.uri
# and cleaned out artifacts from concept table
#-----------------------------------

ALTER TABLE form DROP COLUMN schema_namespace;
ALTER TABLE form DROP COLUMN uri;

ALTER TABLE concept DROP COLUMN name;
ALTER TABLE concept DROP COLUMN icd10;
ALTER TABLE concept DROP COLUMN loinc;
ALTER TABLE concept DROP COLUMN form_location;
ALTER TABLE concept DROP COLUMN units;
ALTER TABLE concept DROP COLUMN view_count;

UPDATE `global_property` SET property_value='1.0.29' WHERE property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.30
# Darius Jazayeri     Aug 7 2006 3:00 PM
# Populate person table
#-----------------------------------

INSERT INTO person (person_id, patient_id) (SELECT null, patient_id FROM patient);
INSERT INTO person (person_id, user_id) (SELECT null, user_id FROM users);

UPDATE `global_property` SET property_value='1.0.30' WHERE property = 'database_version';


#-----------------------------------
# OpenMRS Datamodel version 1.0.31
# Darius Jazayeri     Aug 8 2006 11:59 PM
# Major cleanup of drug and drug_order tables
#-----------------------------------

# This is destructive to the route and dosage_form columns, so ensure you're not using those before running this script
ALTER TABLE `drug` DROP COLUMN `dosage_form`;
ALTER TABLE `drug` ADD COLUMN `dosage_form` int(11) default NULL AFTER `combination`;
ALTER TABLE `drug` ADD INDEX `dosage_form_concept` (`dosage_form`);
ALTER TABLE `drug` ADD CONSTRAINT `dosage_form_concept` FOREIGN KEY (`dosage_form`) REFERENCES `concept` (`concept_id`);
ALTER TABLE `drug` DROP COLUMN `route`;
ALTER TABLE `drug` ADD COLUMN `route` int(11) default NULL AFTER `minimum_dose`;
ALTER TABLE `drug` ADD INDEX `route_concept` (`route`);
ALTER TABLE `drug` ADD CONSTRAINT `route_concept` FOREIGN KEY (`route`) REFERENCES `concept` (`concept_id`);
ALTER TABLE `drug` DROP COLUMN `therapy_class`;
ALTER TABLE `drug` DROP COLUMN `shelf_life`;
ALTER TABLE `drug` DROP COLUMN `inn`;
ALTER TABLE `drug` DROP COLUMN `daily_mg_per_kg`;
ALTER TABLE `drug` ADD COLUMN `maximum_daily_dose` double AFTER `maximum_dose`;
ALTER TABLE `drug` ADD COLUMN `minimum_daily_dose` double AFTER `minimum_dose`;
UPDATE `drug` SET maximum_daily_dose = maximum_dose;
UPDATE `drug` SET minimum_daily_dose = minimum_dose;
ALTER TABLE `drug` DROP COLUMN `maximum_dose`;
ALTER TABLE `drug` DROP COLUMN `minimum_dose`;
ALTER TABLE `drug` ADD COLUMN `voided` tinyint(1) NOT NULL default '0' AFTER `date_created`;
ALTER TABLE `drug` ADD COLUMN `voided_by` int(11) default NULL AFTER `voided`;
ALTER TABLE `drug` ADD INDEX `user_who_voided_drug` (`voided_by`);
ALTER TABLE `drug` ADD CONSTRAINT `user_who_voided_drug` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`);
ALTER TABLE `drug` ADD COLUMN `date_voided` datetime default NULL AFTER `voided_by`;
ALTER TABLE `drug` ADD COLUMN `void_reason` varchar(255) default NULL AFTER `date_voided`;

ALTER TABLE `drug_order` ADD COLUMN `dose_to_delete` int(11) default null AFTER `dose`;
UPDATE drug_order SET dose_to_delete = dose;
ALTER TABLE `drug_order` DROP COLUMN `dose`;
ALTER TABLE `drug_order` ADD COLUMN `dose` double default NULL AFTER `dose_to_delete`;
UPDATE drug_order SET dose = dose_to_delete;
ALTER TABLE `drug_order` DROP COLUMN `dose_to_delete`;
ALTER TABLE `drug_order` ADD COLUMN `equivalent_daily_dose` double default NULL AFTER `dose`;

UPDATE `global_property` SET property_value='1.0.31' WHERE property = 'database_version';

#-----------------------------------
# OpenMRS Datamodel version 1.0.32
# Burke Mamlin     Aug 10 2006 9:40 AM
# Fix to global_property and update of XSLT for starter form
#-----------------------------------

ALTER TABLE `global_property` ADD PRIMARY KEY (`property`);
UPDATE `form` SET xslt='<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\r\n<!--\r\nOpenMRS FormEntry Form HL7 Translation\r\n\r\nThis XSLT is used to translate OpenMRS forms from XML into HL7 2.5 format\r\n\r\n@author Burke Mamlin, MD\r\n@author Ben Wolfe\r\n@version 1.8\r\n-->\r\n\r\n<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" xmlns:xdt=\"http://www.w3.org/2005/xpath-datatypes\">\r\n	<xsl:output method=\"text\" version=\"1.0\" encoding=\"UTF-8\" indent=\"no\"/>\r\n\r\n<xsl:variable name=\"SENDING-APPLICATION\">FORMENTRY</xsl:variable>\r\n<xsl:variable name=\"SENDING-FACILITY\">AMRS</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-APPLICATION\">HL7LISTENER</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-FACILITY\">AMRS</xsl:variable>\r\n<xsl:variable name=\"PATIENT-AUTHORITY\">AMRS-ELDORET&amp;openmrs.org&amp;DNS</xsl:variable>\r\n<xsl:variable name=\"FORM-AUTHORITY\">AMRS-ELDORET^http://schema.openmrs.org/2006/FormEntry/formId^URI</xsl:variable>\r\n\r\n<xsl:template match=\"/\">\r\n	<xsl:apply-templates />\r\n</xsl:template>\r\n\r\n<!-- Form template -->\r\n<xsl:template match=\"form\">\r\n	<!-- MSH Header -->\r\n	<xsl:text>MSH|^~\\&amp;</xsl:text>   <!-- Message header, field separator, and encoding characters -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-3 Sending application -->\r\n	<xsl:value-of select=\"$SENDING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-4 Sending facility -->\r\n	<xsl:value-of select=\"$SENDING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-5 Receiving application -->\r\n	<xsl:value-of select=\"$RECEIVING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-6 Receiving facility -->\r\n	<xsl:value-of select=\"$RECEIVING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-7 Date/time message sent -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- MSH-8 Security -->\r\n	<xsl:text>|ORU^R01</xsl:text>       <!-- MSH-9 Message type ^ Event type (observation report unsolicited) -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-10 Message control ID -->\r\n	<xsl:text>formentry-</xsl:text>\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|P</xsl:text>             <!-- MSH-11 Processing ID -->\r\n	<xsl:text>|2.5</xsl:text>           <!-- MSH-12 HL7 version -->\r\n	<xsl:text>|1</xsl:text>             <!-- MSH-13 Message sequence number -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-14 Continuation Pointer -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-15 Accept Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-16 Application Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-17 Country Code -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-18 Character Set -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-19 Principal Language of Message -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-20 Alternate Character Set Handling Scheme -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-21 Message Profile Identifier -->\r\n	<xsl:value-of select=\"@id\" />\r\n	<xsl:text>^</xsl:text>\r\n	<xsl:value-of select=\"$FORM-AUTHORITY\" />\r\n	<xsl:text>&#x000d;</xsl:text>\r\n		\r\n	<!-- PID header -->\r\n	<xsl:text>PID</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-1 Set ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-2 (deprecated) Patient ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-3 Patient Identifier List -->\r\n	<xsl:call-template name=\"patient_id\">\r\n		<xsl:with-param name=\"pid\" select=\"patient/patient.patient_id\" />\r\n		<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n		<xsl:with-param name=\"type\" select=\"L\" />\r\n	</xsl:call-template>\r\n	<xsl:if test=\"patient/patient.previous_mrn and string-length(patient/patient.previous_mrn) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.previous_mrn\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"PRIOR\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<!-- Additional patient identifiers -->\r\n	<!-- This example is for an MTCT-PLUS identifier used in the AMPATH project in Kenya (skipped if not present) -->\r\n	<xsl:if test=\"patient/patient.mtctplus_id and string-length(patient/patient.mtctplus_id) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.mtctplus_id\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"MTCTPLUS\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PID-4 (deprecated) Alternate patient ID -->\r\n	<!-- PID-5 Patient name -->\r\n	<xsl:text>|</xsl:text>              <!-- Family name -->\r\n	<xsl:value-of select=\"patient/patient.family_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Given name -->\r\n	<xsl:value-of select=\"patient/patient.given_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Middle name -->\r\n	<xsl:value-of select=\"patient/patient.middle_name\" />\r\n	<xsl:text>|</xsl:text>              <!-- PID-6 Mother\'s maiden name -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-7 Date/Time of Birth -->\r\n	<xsl:value-of select=\"patient/patient.date_of_birth\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n	<!-- PV1 header -->\r\n	<xsl:text>PV1</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-1 Sub ID -->\r\n	<xsl:text>|O</xsl:text>             <!-- PV1-2 Patient class (O = outpatient) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-3 Patient location -->\r\n	<xsl:value-of select=\"encounter/encounter.location_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-4 Admission type (2 = return) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-5 Pre-Admin Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-6 Prior Patient Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-7 Attending Doctor -->\r\n	<xsl:value-of select=\"encounter/encounter.provider_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-8 Referring Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-9 Consulting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-10 Hospital Service -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-11 Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-12 Preadmin Test Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-13 Re-adminssion Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-14 Admit Source -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-15 Ambulatory Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-16 VIP Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-17 Admitting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-18 Patient Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-19 Visit Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-20 Financial Class -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-21 Charge Price Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-22 Courtesy Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-23 Credit Rating -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-24 Contract Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-25 Contract Effective Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-26 Contract Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-27 Contract Period -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-28 Interest Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-29 Transfer to Bad Debt Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-30 Transfer to Bad Debt Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Agency Code -->\r\n  <xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Transfer Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-33 Bad Debt Recovery Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-34 Delete Account Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-35 Delete Account Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-36 Discharge Disposition -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-37 Discharge To Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-38 Diet Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-39 Servicing Facility -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-40 Bed Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-41 Account Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-42 Pending Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-43 Prior Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-44 Admit Date/Time -->\r\n	<xsl:call-template name=\"hl7Date\">\r\n		<xsl:with-param name=\"date\" select=\"xs:date(encounter/encounter.encounter_datetime)\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-45 Discharge Date/Time -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-46 Current Patient Balance -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-47 Total Charges -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-48 Total Adjustments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-49 Total Payments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-50 Alternate Visit ID -->\r\n	<xsl:text>|V</xsl:text>             <!-- PV1-51 Visit Indicator -->\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- We use encounter date as the timestamp for each observation -->\r\n	<xsl:variable name=\"encounterTimestamp\">\r\n		<xsl:call-template name=\"hl7Date\">\r\n			<xsl:with-param name=\"date\" select=\"xs:date(encounter/encounter.encounter_datetime)\" />\r\n		</xsl:call-template>\r\n	</xsl:variable>\r\n	\r\n	<!-- ORC Common Order Segment -->\r\n	<xsl:text>ORC</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|RE</xsl:text>            <!-- ORC-1 Order Control (RE = obs to follow) -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-2 Placer Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-3 Filler Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-4 Placer Group Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-5 Order Status -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-6 Response Flag -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-7 Quantity/Timing -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-8 Parent -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-9 Date/Time of Transaction -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"xs:dateTime(header/date_entered)\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- ORC-10 Entered By -->\r\n	<xsl:value-of select=\"header/enterer\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- Observation(s) -->\r\n	<xsl:variable name=\"obsList\" select=\"obs/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\" />\r\n	<xsl:variable name=\"obsListCount\" select=\"count($obsList)\" as=\"xs:integer\" />\r\n	<!-- Observation OBR -->\r\n	<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n	<xsl:text>1</xsl:text>\r\n	<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n	<xsl:value-of select=\"obs/@openmrs_concept\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- observation OBXs -->\r\n	<xsl:for-each select=\"$obsList\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"value\">\r\n				<xsl:call-template name=\"obsObx\">\r\n					<xsl:with-param name=\"setId\" select=\"position()\" />\r\n					<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n					<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n					<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n					<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n					<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n					<xsl:with-param name=\"value\" select=\"value\" />\r\n					<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n				<xsl:variable name=\"setId\" select=\"position()\" />\r\n				<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n						<xsl:with-param name=\"subId\" select=\"position()\" />\r\n						<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:for-each>\r\n			</xsl:otherwise>\r\n		</xsl:choose>\r\n	</xsl:for-each>\r\n	\r\n	<!-- Grouped observation(s) -->\r\n	<xsl:variable name=\"obsGroupList\" select=\"obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]\" />\r\n	<xsl:variable name=\"obsGroupListCount\" select=\"count($obsGroupList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$obsGroupList\">\r\n		<!-- Observation OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + position()\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n		\r\n		<!-- Generate OBXs -->\r\n		<xsl:for-each select=\"*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"position()\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>\r\n	</xsl:for-each>\r\n\r\n	<!-- Problem list(s) -->\r\n	<xsl:variable name=\"problemList\" select=\"problem_list/*[value[text() != \'\']]\" />\r\n	<xsl:variable name=\"problemListCount\" select=\"count($problemList)\" as=\"xs:integer\" />\r\n	<xsl:if test=\"$problemList\">\r\n		<!-- Problem list OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"problem_list/@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n		<!-- Problem list OBXs -->\r\n		<xsl:for-each select=\"$problemList\">\r\n			<xsl:call-template name=\"obsObx\">\r\n				<xsl:with-param name=\"setId\" select=\"position()\" />\r\n				<xsl:with-param name=\"datatype\" select=\"\'CWE\'\" />\r\n				<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n				<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n				<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n				<xsl:with-param name=\"value\" select=\"value\" />\r\n				<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n			</xsl:call-template>		\r\n		</xsl:for-each>\r\n	</xsl:if>\r\n	\r\n	<!-- Orders -->\r\n	<xsl:variable name=\"orderList\" select=\"orders/*[*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]]\" />\r\n	<xsl:variable name=\"orderListCount\" select=\"count($orderList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$orderList\">\r\n		<!-- Order section OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + $problemListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n		<!-- Order OBXs -->\r\n		<xsl:for-each select=\"*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"position()\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>	\r\n	</xsl:for-each>\r\n	\r\n</xsl:template>\r\n\r\n<!-- Patient Identifier (CX) generator -->\r\n<xsl:template name=\"patient_id\">\r\n	<xsl:param name=\"pid\" />\r\n	<xsl:param name=\"auth\" />\r\n	<xsl:param name=\"type\" />\r\n	<xsl:value-of select=\"$pid\" />\r\n	<xsl:text>^</xsl:text>              <!-- Check digit -->\r\n	<xsl:text>^</xsl:text>              <!-- Check Digit Scheme -->\r\n	<xsl:text>^</xsl:text>              <!-- Assigning Authority -->\r\n	<xsl:value-of select=\"$auth\" />\r\n	<xsl:text>^</xsl:text>              <!-- Identifier Type -->\r\n	<xsl:value-of select=\"$type\" />\r\n</xsl:template>\r\n\r\n<!-- OBX Generator -->\r\n<xsl:template name=\"obsObx\">\r\n	<xsl:param name=\"setId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"subId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"datatype\" required=\"yes\" />\r\n	<xsl:param name=\"concept\" required=\"yes\" />\r\n	<xsl:param name=\"date\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"time\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"value\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"units\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"encounterTimestamp\" required=\"yes\" />\r\n	<xsl:text>OBX</xsl:text>                     <!-- Message type -->\r\n	<xsl:text>|</xsl:text>                       <!-- Set ID -->\r\n	<xsl:value-of select=\"$setId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Observation datatype -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:text>NM</xsl:text>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$datatype\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Concept (what was observed -->\r\n	<xsl:value-of select=\"$concept\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Sub-ID -->\r\n	<xsl:value-of select=\"$subId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Value -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'TS\'\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'DT\'\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'TM\'\">\r\n			<xsl:call-template name=\"hl7Time\">\r\n				<xsl:with-param name=\"time\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"$value = \'0\' or upper-case($value) = \'FALSE\'\">0</xsl:when>\r\n				<xsl:otherwise>1</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$value\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Units -->\r\n	<xsl:value-of select=\"$units\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Reference range -->\r\n	<xsl:text>|</xsl:text>                       <!-- Abnormal flags -->\r\n	<xsl:text>|</xsl:text>                       <!-- Probability -->\r\n	<xsl:text>|</xsl:text>                       <!-- Nature of abnormal test -->\r\n	<xsl:text>|</xsl:text>                       <!-- Observation result status -->\r\n	<xsl:text>|</xsl:text>                       <!-- Effective date -->\r\n	<xsl:text>|</xsl:text>                       <!-- User defined access checks -->\r\n	<xsl:text>|</xsl:text>                       <!-- Date time of observation -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$date and $time\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"dateTime($date,$time)\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$date\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$date\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$encounterTimestamp\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>&#x000d;</xsl:text>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted timestamp -->\r\n<xsl:template name=\"hl7Timestamp\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:value-of select=\"concat(year-from-dateTime($date),format-number(month-from-dateTime($date),\'00\'),format-number(day-from-dateTime($date),\'00\'),format-number(hours-from-dateTime($date),\'00\'),format-number(minutes-from-dateTime($date),\'00\'),format-number(seconds-from-dateTime($date),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted date -->\r\n<xsl:template name=\"hl7Date\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"contains(string($date),\'T\')\">\r\n				<xsl:call-template name=\"hl7Date\">\r\n					<xsl:with-param name=\"date\" select=\"xs:date(substring-before($date,\'T\'))\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n					<xsl:value-of select=\"concat(year-from-date($date),format-number(month-from-date($date),\'00\'),format-number(day-from-date($date),\'00\'))\" />\r\n			</xsl:otherwise>\r\n		</xsl:choose>				\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted time -->\r\n<xsl:template name=\"hl7Time\">\r\n	<xsl:param name=\"time\" />\r\n	<xsl:if test=\"$time != \'\'\">\r\n		<xsl:value-of select=\"concat(format-number(hours-from-time($time),\'00\'),format-number(minutes-from-time($time),\'00\'),format-number(seconds-from-time($time),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n</xsl:stylesheet>' WHERE form_id = 1;

UPDATE `global_property` SET property_value='1.0.32' WHERE property = 'database_version';
