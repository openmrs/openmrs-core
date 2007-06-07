#--------------------------------------
# USE:
#  The diffs are ordered by datamodel version number.
#--------------------------------------

#--------------------------------------
# OpenMRS Datamodel version 1.0.10
# Paul Biondich Apr 11 2006 12:50 PM
# Reporting table added
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
		select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
		CREATE TABLE `report_object` (
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
		
		update `global_property` set property_value=new_db_version where property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.10');

#--------------------------------------
# OpenMRS Datamodel version 1.0.11
# Ben Wolfe Apr 19 2006 5:00 PM
# Alert Functionality added
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	CREATE TABLE `alert` (
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
	
	CREATE TABLE `alert_read` (
	  `alert_id` int(11) NOT NULL,
	  `user_id` int(11) NOT NULL,
	  `date_read` timestamp NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
	
	  PRIMARY KEY (`alert_id`, `user_id`),
	
	  CONSTRAINT `alert_read` FOREIGN KEY (`alert_id`) REFERENCES `alert` (`alert_id`),
	  CONSTRAINT `alert_read_by_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='';
	
	update `global_property` set property_value=new_db_version where property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.11');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.12
# Paul Biondich Apr 21 2006 11:29 AM
# Drug autoincrement added
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	ALTER TABLE `drug` MODIFY COLUMN `drug_id` int(11) NOT NULL auto_increment;
	update `global_property` set property_value=new_db_version where property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.12');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.13
# Darius Jazayeri Apr 21 2006 5:40 PM
# report_object.type keyword resolution
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	ALTER TABLE `report_object` CHANGE COLUMN `type` `report_object_type` varchar(255) NOT NULL;
	ALTER TABLE `report_object` CHANGE COLUMN `sub_type` `report_object_sub_type` varchar(255) NOT NULL;
	update `global_property` set property_value=new_db_version where property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.13');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.14
# Ben Wolfe    Apr 24 2006 5:40 PM
# Added obs.value_drug
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	ALTER TABLE `obs` ADD COLUMN `value_drug` int(11) default NULL AFTER `value_coded`;
	ALTER TABLE `obs` ADD INDEX `answer_concept_drug` (`value_drug`);
	ALTER TABLE `obs` ADD CONSTRAINT `answer_concept_drug` FOREIGN KEY (`value_drug`) REFERENCES `drug` (`drug_id`);
	update `global_property` set property_value=new_db_version where property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.14');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.15
# Burke Mamlin  Apr 25 2006 5:47 AM
# Added form.template
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	ALTER TABLE `form` ADD COLUMN `template` mediumtext default NULL AFTER `schema_namespace`;
	update `global_property` set property_value=new_db_version where property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.15');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.16
# Ben Wolfe    May 1 2006 9:15 AM
# Added database indexes (Directed towards patient merging)
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	ALTER TABLE `patient_name` ADD INDEX `first_name` (`given_name`);
	ALTER TABLE `patient_name` ADD INDEX  `middle_name` (`middle_name`);
	ALTER TABLE `patient_name` ADD INDEX  `last_name` (`family_name`);
	ALTER TABLE `patient` ADD INDEX `birthdate` (`birthdate`);
	ALTER TABLE `patient_identifier` ADD INDEX `identifier_name` (`identifier`);
	update `global_property` set property_value=new_db_version where property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.16');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.17
# Justin Miranda    May 1 2006 5:02 PM
# Added scheduler_task_config,
# schedule_task_config_properties,
# and notification_template files.
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
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
	insert into scheduler_task_config (`task_config_id`,`name`,`description`,`schedulable_class`,`start_time`,`repeat_interval`,`start_on_startup`,`started`,`created_by`,`date_created`,`changed_by`,`date_changed`) values (1,'Process Form Entry Queue','Processes form entry queue.','org.openmrs.scheduler.tasks.ProcessFormEntryQueueTask','2006-04-24 00:00:00',30,0,0,1,'2006-04-24 00:00:00',null,null);
	insert into scheduler_task_config (`task_config_id`,`name`,`description`,`schedulable_class`,`start_time`,`repeat_interval`,`start_on_startup`,`started`,`created_by`,`date_created`,`changed_by`,`date_changed`) values (2,'Process HL7 Task','Processes HL7 messages.','org.openmrs.scheduler.tasks.ProcessHL7InQueueTask','2006-04-24 00:00:00',30,0,0,1,'2006-04-24 00:00:00',null,null);
	insert into scheduler_task_config (`task_config_id`,`name`,`description`,`schedulable_class`,`start_time`,`repeat_interval`,`start_on_startup`,`started`,`created_by`,`date_created`,`changed_by`,`date_changed`) values (3,'Alert Reminder Task','Sends email to users who have not checked their alerts.  Set to run every ten minutes.','org.openmrs.scheduler.tasks.AlertReminderTask','2006-04-24 00:00:00',600,0,0,1,'2006-04-24 00:00:00',null,null);
	insert into scheduler_task_config (`task_config_id`,`name`,`description`,`schedulable_class`,`start_time`,`repeat_interval`,`start_on_startup`,`started`,`created_by`,`date_created`,`changed_by`,`date_changed`) values (4,'Send Email Task','Doesn''t do anything yet.','org.openmrs.scheduler.tasks.SendEmailTask','2006-04-24 00:00:00',600,0,0,1,'2006-04-24 00:00:00',null,null);
	insert into scheduler_task_config (`task_config_id`,`name`,`description`,`schedulable_class`,`start_time`,`repeat_interval`,`start_on_startup`,`started`,`created_by`,`date_created`,`changed_by`,`date_changed`) values (5,'Hello World Task','Writes ''hello world'' to log.  Demonstrates problem caused by spawning a thread from a timer task.','org.openmrs.scheduler.tasks.HelloWorldTask','2006-04-24 00:00:00',600,0,0,1,'2006-04-24 00:00:00',null,null);
	insert into scheduler_task_config (`task_config_id`,`name`,`description`,`schedulable_class`,`start_time`,`repeat_interval`,`start_on_startup`,`started`,`created_by`,`date_created`,`changed_by`,`date_changed`) values (6,'Check Internet Connectivity Task','Checks the external internet connection every ten minutes.  This is a trivial task that checks the connection to Google over port 80.  If the connection fails, we assume the internet is done and raise an alert.','org.openmrs.scheduler.tasks.CheckInternetConnectivityTask','2006-04-24 00:00:00',60,0,0,1,'2006-04-24 00:00:00',null,null);
	
	update `global_property` set property_value=new_db_version where property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.17');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.18
# Ben Wolfe     May 8 2006 8:30 AM
# Modified alert tables
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
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
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.18');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.21
# Ben Wolfe    May 25 2006 9:40 AM
# Added patient.dead
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	ALTER TABLE `patient` ADD COLUMN `dead` int(1) NOT NULL default '0' AFTER `civil_status`;
	
	UPDATE `patient` SET `dead` = 1 WHERE `death_date` IS NOT NULL;
	UPDATE `patient` p SET `dead` = 1 WHERE `cause_of_death` IS NOT NULL AND `cause_of_death` <> '' AND NOT EXISTS (SELECT e.encounter_id FROM encounter e WHERE e.patient_id = p.patient_id);
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.21');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.22
# Ben Wolfe   May 26 2006 10:00 AM
# Moved concept_class.is_set to concept.is_set
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	ALTER TABLE `concept` ADD COLUMN `is_set` tinyint(1) NOT NULL default '0' AFTER `class_id`;
	UPDATE `concept` c, `concept_class` class SET c.`is_set` = class.`is_set` WHERE class.`concept_class_id` = c.`class_id`;
	ALTER TABLE `concept_class` DROP COLUMN `is_set`;
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.22');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.23
# Ben Wolfe   June 19 2006 8:45 AM
# Make encounters voidable
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	ALTER TABLE `encounter` ADD COLUMN `voided` tinyint(1) NOT NULL default '0';
	ALTER TABLE `encounter` ADD COLUMN `voided_by` int(11) default NULL;
	ALTER TABLE `encounter` ADD COLUMN `date_voided` datetime default NULL;
	ALTER TABLE `encounter` ADD COLUMN `void_reason` varchar(255) default NULL;
	ALTER TABLE `encounter` ADD INDEX `user_who_voided_encounter` (`voided_by`);
	ALTER TABLE `encounter` ADD CONSTRAINT `user_who_voided_encounter` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`);
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.23');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.26
# Ben Wolfe   July 20 2006 8:45 PM
# Add form_field.sort_weight
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	ALTER TABLE `form_field` ADD COLUMN `sort_weight` float(11,5) default NULL;
	
	SET @new_weight=0;
	UPDATE form_field SET sort_weight = (select @new_weight := @new_weight + 10 from dual) ORDER BY form_id, parent_form_field, field_number, field_part, (select name from field where field_id = form_field.field_id);
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.26');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.27
# Ben Wolfe     Aug 2 2006 9:55 AM
# Removed form.infopath_solution_version
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	ALTER TABLE form DROP COLUMN infopath_solution_version;
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.27');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.28
# Darius Jazayeri August 02 2006 6:28 PM
# Initial pass at Programs
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
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
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.28');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.29
# Burke Mamlin     Aug 2 2006 11:07 AM
# Removed form.schema_namespace and form.uri
# and cleaned out artifacts from concept table
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	ALTER TABLE form DROP COLUMN schema_namespace;
	ALTER TABLE form DROP COLUMN uri;
	
	ALTER TABLE concept DROP COLUMN name;
	ALTER TABLE concept DROP COLUMN icd10;
	ALTER TABLE concept DROP COLUMN loinc;
	ALTER TABLE concept DROP COLUMN form_location;
	ALTER TABLE concept DROP COLUMN units;
	ALTER TABLE concept DROP COLUMN view_count;
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.29');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.30
# Darius Jazayeri     Aug 7 2006 3:00 PM
# Populate person table
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	INSERT INTO person (person_id, patient_id) (SELECT null, patient_id FROM patient);
	INSERT INTO person (person_id, user_id) (SELECT null, user_id FROM users);
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.30');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.31
# Darius Jazayeri     Aug 8 2006 11:59 PM
# Major cleanup of drug and drug_order tables
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
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
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.31');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.32
# Burke Mamlin     Aug 10 2006 9:40 AM
# Fix to global_property and update of XSLT for starter form
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	ALTER TABLE `global_property` ADD PRIMARY KEY (`property`);
	UPDATE `form` SET xslt='<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\r\n<!--\r\nOpenMRS FormEntry Form HL7 Translation\r\n\r\nThis XSLT is used to translate OpenMRS forms from XML into HL7 2.5 format\r\n\r\n@author Burke Mamlin, MD\r\n@author Ben Wolfe\r\n@version 1.8\r\n-->\r\n\r\n<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" xmlns:xdt=\"http://www.w3.org/2005/xpath-datatypes\">\r\n	<xsl:output method=\"text\" version=\"1.0\" encoding=\"UTF-8\" indent=\"no\"/>\r\n\r\n<xsl:variable name=\"SENDING-APPLICATION\">FORMENTRY</xsl:variable>\r\n<xsl:variable name=\"SENDING-FACILITY\">AMRS</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-APPLICATION\">HL7LISTENER</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-FACILITY\">AMRS</xsl:variable>\r\n<xsl:variable name=\"PATIENT-AUTHORITY\">AMRS-ELDORET&amp;openmrs.org&amp;DNS</xsl:variable>\r\n<xsl:variable name=\"FORM-AUTHORITY\">AMRS-ELDORET^http://schema.openmrs.org/2006/FormEntry/formId^URI</xsl:variable>\r\n\r\n<xsl:template match=\"/\">\r\n	<xsl:apply-templates />\r\n</xsl:template>\r\n\r\n<!-- Form template -->\r\n<xsl:template match=\"form\">\r\n	<!-- MSH Header -->\r\n	<xsl:text>MSH|^~\\&amp;</xsl:text>   <!-- Message header, field separator, and encoding characters -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-3 Sending application -->\r\n	<xsl:value-of select=\"$SENDING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-4 Sending facility -->\r\n	<xsl:value-of select=\"$SENDING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-5 Receiving application -->\r\n	<xsl:value-of select=\"$RECEIVING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-6 Receiving facility -->\r\n	<xsl:value-of select=\"$RECEIVING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-7 Date/time message sent -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- MSH-8 Security -->\r\n	<xsl:text>|ORU^R01</xsl:text>       <!-- MSH-9 Message type ^ Event type (observation report unsolicited) -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-10 Message control ID -->\r\n	<xsl:text>formentry-</xsl:text>\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|P</xsl:text>             <!-- MSH-11 Processing ID -->\r\n	<xsl:text>|2.5</xsl:text>           <!-- MSH-12 HL7 version -->\r\n	<xsl:text>|1</xsl:text>             <!-- MSH-13 Message sequence number -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-14 Continuation Pointer -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-15 Accept Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-16 Application Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-17 Country Code -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-18 Character Set -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-19 Principal Language of Message -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-20 Alternate Character Set Handling Scheme -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-21 Message Profile Identifier -->\r\n	<xsl:value-of select=\"@id\" />\r\n	<xsl:text>^</xsl:text>\r\n	<xsl:value-of select=\"$FORM-AUTHORITY\" />\r\n	<xsl:text>&#x000d;</xsl:text>\r\n		\r\n	<!-- PID header -->\r\n	<xsl:text>PID</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-1 Set ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-2 (deprecated) Patient ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-3 Patient Identifier List -->\r\n	<xsl:call-template name=\"patient_id\">\r\n		<xsl:with-param name=\"pid\" select=\"patient/patient.patient_id\" />\r\n		<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n		<xsl:with-param name=\"type\" select=\"L\" />\r\n	</xsl:call-template>\r\n	<xsl:if test=\"patient/patient.previous_mrn and string-length(patient/patient.previous_mrn) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.previous_mrn\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"PRIOR\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<!-- Additional patient identifiers -->\r\n	<!-- This example is for an MTCT-PLUS identifier used in the AMPATH project in Kenya (skipped if not present) -->\r\n	<xsl:if test=\"patient/patient.mtctplus_id and string-length(patient/patient.mtctplus_id) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.mtctplus_id\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"MTCTPLUS\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PID-4 (deprecated) Alternate patient ID -->\r\n	<!-- PID-5 Patient name -->\r\n	<xsl:text>|</xsl:text>              <!-- Family name -->\r\n	<xsl:value-of select=\"patient/patient.family_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Given name -->\r\n	<xsl:value-of select=\"patient/patient.given_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Middle name -->\r\n	<xsl:value-of select=\"patient/patient.middle_name\" />\r\n	<xsl:text>|</xsl:text>              <!-- PID-6 Mother\'s maiden name -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-7 Date/Time of Birth -->\r\n	<xsl:value-of select=\"patient/patient.date_of_birth\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n	<!-- PV1 header -->\r\n	<xsl:text>PV1</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-1 Sub ID -->\r\n	<xsl:text>|O</xsl:text>             <!-- PV1-2 Patient class (O = outpatient) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-3 Patient location -->\r\n	<xsl:value-of select=\"encounter/encounter.location_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-4 Admission type (2 = return) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-5 Pre-Admin Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-6 Prior Patient Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-7 Attending Doctor -->\r\n	<xsl:value-of select=\"encounter/encounter.provider_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-8 Referring Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-9 Consulting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-10 Hospital Service -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-11 Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-12 Preadmin Test Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-13 Re-adminssion Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-14 Admit Source -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-15 Ambulatory Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-16 VIP Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-17 Admitting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-18 Patient Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-19 Visit Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-20 Financial Class -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-21 Charge Price Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-22 Courtesy Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-23 Credit Rating -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-24 Contract Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-25 Contract Effective Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-26 Contract Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-27 Contract Period -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-28 Interest Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-29 Transfer to Bad Debt Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-30 Transfer to Bad Debt Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Agency Code -->\r\n  <xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Transfer Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-33 Bad Debt Recovery Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-34 Delete Account Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-35 Delete Account Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-36 Discharge Disposition -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-37 Discharge To Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-38 Diet Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-39 Servicing Facility -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-40 Bed Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-41 Account Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-42 Pending Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-43 Prior Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-44 Admit Date/Time -->\r\n	<xsl:call-template name=\"hl7Date\">\r\n		<xsl:with-param name=\"date\" select=\"xs:date(encounter/encounter.encounter_datetime)\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-45 Discharge Date/Time -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-46 Current Patient Balance -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-47 Total Charges -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-48 Total Adjustments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-49 Total Payments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-50 Alternate Visit ID -->\r\n	<xsl:text>|V</xsl:text>             <!-- PV1-51 Visit Indicator -->\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- We use encounter date as the timestamp for each observation -->\r\n	<xsl:variable name=\"encounterTimestamp\">\r\n		<xsl:call-template name=\"hl7Date\">\r\n			<xsl:with-param name=\"date\" select=\"xs:date(encounter/encounter.encounter_datetime)\" />\r\n		</xsl:call-template>\r\n	</xsl:variable>\r\n	\r\n	<!-- ORC Common Order Segment -->\r\n	<xsl:text>ORC</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|RE</xsl:text>            <!-- ORC-1 Order Control (RE = obs to follow) -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-2 Placer Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-3 Filler Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-4 Placer Group Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-5 Order Status -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-6 Response Flag -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-7 Quantity/Timing -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-8 Parent -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-9 Date/Time of Transaction -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"xs:dateTime(header/date_entered)\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- ORC-10 Entered By -->\r\n	<xsl:value-of select=\"header/enterer\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- Observation(s) -->\r\n	<xsl:variable name=\"obsList\" select=\"obs/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\" />\r\n	<xsl:variable name=\"obsListCount\" select=\"count($obsList)\" as=\"xs:integer\" />\r\n	<!-- Observation OBR -->\r\n	<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n	<xsl:text>1</xsl:text>\r\n	<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n	<xsl:value-of select=\"obs/@openmrs_concept\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- observation OBXs -->\r\n	<xsl:for-each select=\"$obsList\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"value\">\r\n				<xsl:call-template name=\"obsObx\">\r\n					<xsl:with-param name=\"setId\" select=\"position()\" />\r\n					<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n					<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n					<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n					<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n					<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n					<xsl:with-param name=\"value\" select=\"value\" />\r\n					<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n				<xsl:variable name=\"setId\" select=\"position()\" />\r\n				<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n						<xsl:with-param name=\"subId\" select=\"position()\" />\r\n						<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:for-each>\r\n			</xsl:otherwise>\r\n		</xsl:choose>\r\n	</xsl:for-each>\r\n	\r\n	<!-- Grouped observation(s) -->\r\n	<xsl:variable name=\"obsGroupList\" select=\"obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]\" />\r\n	<xsl:variable name=\"obsGroupListCount\" select=\"count($obsGroupList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$obsGroupList\">\r\n		<!-- Observation OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + position()\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n		\r\n		<!-- Generate OBXs -->\r\n		<xsl:for-each select=\"*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"position()\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>\r\n	</xsl:for-each>\r\n\r\n	<!-- Problem list(s) -->\r\n	<xsl:variable name=\"problemList\" select=\"problem_list/*[value[text() != \'\']]\" />\r\n	<xsl:variable name=\"problemListCount\" select=\"count($problemList)\" as=\"xs:integer\" />\r\n	<xsl:if test=\"$problemList\">\r\n		<!-- Problem list OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"problem_list/@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n		<!-- Problem list OBXs -->\r\n		<xsl:for-each select=\"$problemList\">\r\n			<xsl:call-template name=\"obsObx\">\r\n				<xsl:with-param name=\"setId\" select=\"position()\" />\r\n				<xsl:with-param name=\"datatype\" select=\"\'CWE\'\" />\r\n				<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n				<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n				<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n				<xsl:with-param name=\"value\" select=\"value\" />\r\n				<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n			</xsl:call-template>		\r\n		</xsl:for-each>\r\n	</xsl:if>\r\n	\r\n	<!-- Orders -->\r\n	<xsl:variable name=\"orderList\" select=\"orders/*[*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]]\" />\r\n	<xsl:variable name=\"orderListCount\" select=\"count($orderList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$orderList\">\r\n		<!-- Order section OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + $problemListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n		<!-- Order OBXs -->\r\n		<xsl:for-each select=\"*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"position()\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>	\r\n	</xsl:for-each>\r\n	\r\n</xsl:template>\r\n\r\n<!-- Patient Identifier (CX) generator -->\r\n<xsl:template name=\"patient_id\">\r\n	<xsl:param name=\"pid\" />\r\n	<xsl:param name=\"auth\" />\r\n	<xsl:param name=\"type\" />\r\n	<xsl:value-of select=\"$pid\" />\r\n	<xsl:text>^</xsl:text>              <!-- Check digit -->\r\n	<xsl:text>^</xsl:text>              <!-- Check Digit Scheme -->\r\n	<xsl:text>^</xsl:text>              <!-- Assigning Authority -->\r\n	<xsl:value-of select=\"$auth\" />\r\n	<xsl:text>^</xsl:text>              <!-- Identifier Type -->\r\n	<xsl:value-of select=\"$type\" />\r\n</xsl:template>\r\n\r\n<!-- OBX Generator -->\r\n<xsl:template name=\"obsObx\">\r\n	<xsl:param name=\"setId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"subId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"datatype\" required=\"yes\" />\r\n	<xsl:param name=\"concept\" required=\"yes\" />\r\n	<xsl:param name=\"date\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"time\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"value\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"units\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"encounterTimestamp\" required=\"yes\" />\r\n	<xsl:text>OBX</xsl:text>                     <!-- Message type -->\r\n	<xsl:text>|</xsl:text>                       <!-- Set ID -->\r\n	<xsl:value-of select=\"$setId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Observation datatype -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:text>NM</xsl:text>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$datatype\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Concept (what was observed -->\r\n	<xsl:value-of select=\"$concept\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Sub-ID -->\r\n	<xsl:value-of select=\"$subId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Value -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'TS\'\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'DT\'\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'TM\'\">\r\n			<xsl:call-template name=\"hl7Time\">\r\n				<xsl:with-param name=\"time\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"$value = \'0\' or upper-case($value) = \'FALSE\'\">0</xsl:when>\r\n				<xsl:otherwise>1</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$value\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Units -->\r\n	<xsl:value-of select=\"$units\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Reference range -->\r\n	<xsl:text>|</xsl:text>                       <!-- Abnormal flags -->\r\n	<xsl:text>|</xsl:text>                       <!-- Probability -->\r\n	<xsl:text>|</xsl:text>                       <!-- Nature of abnormal test -->\r\n	<xsl:text>|</xsl:text>                       <!-- Observation result status -->\r\n	<xsl:text>|</xsl:text>                       <!-- Effective date -->\r\n	<xsl:text>|</xsl:text>                       <!-- User defined access checks -->\r\n	<xsl:text>|</xsl:text>                       <!-- Date time of observation -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$date and $time\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"dateTime($date,$time)\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$date\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$date\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$encounterTimestamp\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>&#x000d;</xsl:text>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted timestamp -->\r\n<xsl:template name=\"hl7Timestamp\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:value-of select=\"concat(year-from-dateTime($date),format-number(month-from-dateTime($date),\'00\'),format-number(day-from-dateTime($date),\'00\'),format-number(hours-from-dateTime($date),\'00\'),format-number(minutes-from-dateTime($date),\'00\'),format-number(seconds-from-dateTime($date),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted date -->\r\n<xsl:template name=\"hl7Date\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"contains(string($date),\'T\')\">\r\n				<xsl:call-template name=\"hl7Date\">\r\n					<xsl:with-param name=\"date\" select=\"xs:date(substring-before($date,\'T\'))\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n					<xsl:value-of select=\"concat(year-from-date($date),format-number(month-from-date($date),\'00\'),format-number(day-from-date($date),\'00\'))\" />\r\n			</xsl:otherwise>\r\n		</xsl:choose>				\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted time -->\r\n<xsl:template name=\"hl7Time\">\r\n	<xsl:param name=\"time\" />\r\n	<xsl:if test=\"$time != \'\'\">\r\n		<xsl:value-of select=\"concat(format-number(hours-from-time($time),\'00\'),format-number(minutes-from-time($time),\'00\'),format-number(seconds-from-time($time),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n</xsl:stylesheet>' WHERE form_id = 1;
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.32');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.33
# Darius Jazayeri     Aug 12 2006 7:07 PM
# Create program_workflow_state table
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	 select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	 ALTER TABLE `program` ADD COLUMN `changed_by` int(11) default NULL;
	 ALTER TABLE `program` ADD COLUMN `date_changed` datetime default NULL;
	 ALTER TABLE `program` ADD COLUMN `voided` tinyint(1) NOT NULL default '0';
	 ALTER TABLE `program` ADD COLUMN `voided_by` int(11) default NULL;
	 ALTER TABLE `program` ADD COLUMN `date_voided` datetime default NULL;
	 ALTER TABLE `program` ADD COLUMN `void_reason` varchar(255) default NULL;
	 ALTER TABLE `program` ADD INDEX `user_who_changed_program` (`changed_by`);
	 ALTER TABLE `program` ADD INDEX `user_who_voided_program` (`voided_by`);
	 ALTER TABLE `program` ADD CONSTRAINT `user_who_changed_program` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`);
	 ALTER TABLE `program` ADD CONSTRAINT `user_who_voided_program` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`);
	 
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
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.33');
	
#--------------------------------------
# OpenMRS Datamodel version 1.0.34
# Darius Jazayeri     Aug 14 2006 12:09 AM
# Added voided columns to patient_program
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' from dual;
	
	ALTER TABLE `patient_program` ADD COLUMN `voided` tinyint(1) NOT NULL default '0';
	ALTER TABLE `patient_program` ADD COLUMN `voided_by` int(11) default NULL;
	ALTER TABLE `patient_program` ADD COLUMN `date_voided` datetime default NULL;
	ALTER TABLE `patient_program` ADD COLUMN `void_reason` varchar(255) default NULL;
	ALTER TABLE `patient_program` ADD INDEX `user_who_voided_patient_program` (`voided_by`);
	ALTER TABLE `patient_program` ADD CONSTRAINT `user_who_voided_patient_program` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`);
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.34');

#--------------------------------------
# OpenMRS Datamodel version 1.0.35
# Darius Jazayeri     Aug 15 2006 2:00 PM
# Create patient_state table
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
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

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.35');

#--------------------------------------
# OpenMRS Datamodel version 1.0.36
# Ben Wolfe    Sept 1 2006 3:00 PM
# Update default xslt and hl7_source
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	IF (SELECT count(*) < 1 from hl7_source) THEN
		INSERT INTO `hl7_source` VALUES ('1', 'LOCAL', '', '1', '2006-09-01 09:00:00');
	END IF;
	UPDATE form set xslt = '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\r\n<!--\r\nOpenMRS FormEntry Form HL7 Translation\r\n\r\nThis XSLT is used to translate OpenMRS forms from XML into HL7 2.5 format\r\n\r\n@author Burke Mamlin, MD\r\n@author Ben Wolfe\r\n@version 1.9.2\r\n-->\r\n\r\n<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" xmlns:xdt=\"http://www.w3.org/2005/xpath-datatypes\">\r\n	<xsl:output method=\"text\" version=\"1.0\" encoding=\"UTF-8\" indent=\"no\"/>\r\n\r\n<xsl:variable name=\"SENDING-APPLICATION\">FORMENTRY</xsl:variable>\r\n<xsl:variable name=\"SENDING-FACILITY\">AMRS.ELD</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-APPLICATION\">HL7LISTENER</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-FACILITY\">AMRS.ELD</xsl:variable>\r\n<xsl:variable name=\"PATIENT-AUTHORITY\"></xsl:variable> <!-- leave blank for internal id, max 20 characters -->\r\n                                                       <!-- for now, must match patient_identifier_type.name -->\r\n<xsl:variable name=\"FORM-AUTHORITY\">AMRS.ELD.FORMID</xsl:variable> <!-- max 20 characters -->\r\n\r\n<xsl:template match=\"/\">\r\n	<xsl:apply-templates />\r\n</xsl:template>\r\n\r\n<!-- Form template -->\r\n<xsl:template match=\"form\">\r\n	<!-- MSH Header -->\r\n	<xsl:text>MSH|^~\\&amp;</xsl:text>   <!-- Message header, field separator, and encoding characters -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-3 Sending application -->\r\n	<xsl:value-of select=\"$SENDING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-4 Sending facility -->\r\n	<xsl:value-of select=\"$SENDING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-5 Receiving application -->\r\n	<xsl:value-of select=\"$RECEIVING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-6 Receiving facility -->\r\n	<xsl:value-of select=\"$RECEIVING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-7 Date/time message sent -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- MSH-8 Security -->\r\n	<xsl:text>|ORU^R01</xsl:text>       <!-- MSH-9 Message type ^ Event type (observation report unsolicited) -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-10 Message control ID -->\r\n	<xsl:text>formentry-</xsl:text>\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|P</xsl:text>             <!-- MSH-11 Processing ID -->\r\n	<xsl:text>|2.5</xsl:text>           <!-- MSH-12 HL7 version -->\r\n	<xsl:text>|1</xsl:text>             <!-- MSH-13 Message sequence number -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-14 Continuation Pointer -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-15 Accept Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-16 Application Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-17 Country Code -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-18 Character Set -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-19 Principal Language of Message -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-20 Alternate Character Set Handling Scheme -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-21 Message Profile Identifier -->\r\n	<xsl:value-of select=\"@id\" />\r\n	<xsl:text>^</xsl:text>\r\n	<xsl:value-of select=\"$FORM-AUTHORITY\" />\r\n	<xsl:text>&#x000d;</xsl:text>\r\n\r\n	<!-- PID header -->\r\n	<xsl:text>PID</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-1 Set ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-2 (deprecated) Patient ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-3 Patient Identifier List -->\r\n	<xsl:call-template name=\"patient_id\">\r\n		<xsl:with-param name=\"pid\" select=\"patient/patient.patient_id\" />\r\n		<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n		<xsl:with-param name=\"type\" select=\"L\" />\r\n	</xsl:call-template>\r\n	<xsl:if test=\"patient/patient.previous_mrn and string-length(patient/patient.previous_mrn) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.previous_mrn\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"PRIOR\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<!-- Additional patient identifiers -->\r\n	<!-- This example is for an MTCT-PLUS identifier used in the AMPATH project in Kenya (skipped if not present) -->\r\n	<xsl:if test=\"patient/patient.mtctplus_id and string-length(patient/patient.mtctplus_id) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.mtctplus_id\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"MTCTPLUS\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PID-4 (deprecated) Alternate patient ID -->\r\n	<!-- PID-5 Patient name -->\r\n	<xsl:text>|</xsl:text>              <!-- Family name -->\r\n	<xsl:value-of select=\"patient/patient.family_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Given name -->\r\n	<xsl:value-of select=\"patient/patient.given_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Middle name -->\r\n	<xsl:value-of select=\"patient/patient.middle_name\" />\r\n	<xsl:text>|</xsl:text>              <!-- PID-6 Mother\'s maiden name -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-7 Date/Time of Birth -->\r\n	<xsl:value-of select=\"patient/patient.date_of_birth\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n	<!-- PV1 header -->\r\n	<xsl:text>PV1</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-1 Sub ID -->\r\n	<xsl:text>|O</xsl:text>             <!-- PV1-2 Patient class (O = outpatient) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-3 Patient location -->\r\n	<xsl:value-of select=\"encounter/encounter.location_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-4 Admission type (2 = return) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-5 Pre-Admin Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-6 Prior Patient Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-7 Attending Doctor -->\r\n	<xsl:value-of select=\"encounter/encounter.provider_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-8 Referring Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-9 Consulting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-10 Hospital Service -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-11 Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-12 Preadmin Test Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-13 Re-adminssion Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-14 Admit Source -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-15 Ambulatory Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-16 VIP Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-17 Admitting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-18 Patient Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-19 Visit Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-20 Financial Class -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-21 Charge Price Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-22 Courtesy Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-23 Credit Rating -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-24 Contract Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-25 Contract Effective Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-26 Contract Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-27 Contract Period -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-28 Interest Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-29 Transfer to Bad Debt Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-30 Transfer to Bad Debt Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Agency Code -->\r\n  <xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Transfer Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-33 Bad Debt Recovery Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-34 Delete Account Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-35 Delete Account Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-36 Discharge Disposition -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-37 Discharge To Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-38 Diet Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-39 Servicing Facility -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-40 Bed Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-41 Account Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-42 Pending Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-43 Prior Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-44 Admit Date/Time -->\r\n	<xsl:call-template name=\"hl7Date\">\r\n		<xsl:with-param name=\"date\" select=\"xs:date(encounter/encounter.encounter_datetime)\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-45 Discharge Date/Time -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-46 Current Patient Balance -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-47 Total Charges -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-48 Total Adjustments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-49 Total Payments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-50 Alternate Visit ID -->\r\n	<xsl:text>|V</xsl:text>             <!-- PV1-51 Visit Indicator -->\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- We use encounter date as the timestamp for each observation -->\r\n	<xsl:variable name=\"encounterTimestamp\">\r\n		<xsl:call-template name=\"hl7Date\">\r\n			<xsl:with-param name=\"date\" select=\"xs:date(encounter/encounter.encounter_datetime)\" />\r\n		</xsl:call-template>\r\n	</xsl:variable>\r\n	\r\n	<!-- ORC Common Order Segment -->\r\n	<xsl:text>ORC</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|RE</xsl:text>            <!-- ORC-1 Order Control (RE = obs to follow) -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-2 Placer Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-3 Filler Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-4 Placer Group Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-5 Order Status -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-6 Response Flag -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-7 Quantity/Timing -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-8 Parent -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-9 Date/Time of Transaction -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"xs:dateTime(header/date_entered)\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- ORC-10 Entered By -->\r\n	<xsl:value-of select=\"header/enterer\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- Observation(s) -->\r\n	<xsl:variable name=\"obsList\" select=\"obs/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\" />\r\n	<xsl:variable name=\"obsListCount\" select=\"count($obsList)\" as=\"xs:integer\" />\r\n	<!-- Observation OBR -->\r\n	<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n	<xsl:text>1</xsl:text>\r\n	<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n	<xsl:value-of select=\"obs/@openmrs_concept\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- observation OBXs -->\r\n	<xsl:for-each select=\"$obsList\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"value\">\r\n				<xsl:call-template name=\"obsObx\">\r\n					<xsl:with-param name=\"setId\" select=\"position()\" />\r\n					<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n					<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n					<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n					<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n					<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n					<xsl:with-param name=\"value\" select=\"value\" />\r\n					<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n				<xsl:variable name=\"setId\" select=\"position()\" />\r\n				<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n						<xsl:with-param name=\"subId\" select=\"concat($setId,position())\" />\r\n						<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:for-each>\r\n			</xsl:otherwise>\r\n		</xsl:choose>\r\n	</xsl:for-each>\r\n	\r\n	<!-- Grouped observation(s) -->\r\n	<xsl:variable name=\"obsGroupList\" select=\"obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]\" />\r\n	<xsl:variable name=\"obsGroupListCount\" select=\"count($obsGroupList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$obsGroupList\">\r\n		<!-- Observation OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + position()\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n		\r\n		<!-- Generate OBXs -->\r\n		<xsl:for-each select=\"*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"subId\" select=\"1\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"concat(\'1.\',position())\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>\r\n	</xsl:for-each>\r\n\r\n	<!-- Problem list(s) -->\r\n	<xsl:variable name=\"problemList\" select=\"problem_list/*[value[text() != \'\']]\" />\r\n	<xsl:variable name=\"problemListCount\" select=\"count($problemList)\" as=\"xs:integer\" />\r\n	<xsl:if test=\"$problemList\">\r\n		<!-- Problem list OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"problem_list/@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n		<!-- Problem list OBXs -->\r\n		<xsl:for-each select=\"$problemList\">\r\n			<xsl:call-template name=\"obsObx\">\r\n				<xsl:with-param name=\"setId\" select=\"position()\" />\r\n				<xsl:with-param name=\"datatype\" select=\"\'CWE\'\" />\r\n				<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n				<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n				<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n				<xsl:with-param name=\"value\" select=\"value\" />\r\n				<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n			</xsl:call-template>		\r\n		</xsl:for-each>\r\n	</xsl:if>\r\n	\r\n	<!-- Orders -->\r\n	<xsl:variable name=\"orderList\" select=\"orders/*[*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]]\" />\r\n	<xsl:variable name=\"orderListCount\" select=\"count($orderList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$orderList\">\r\n		<!-- Order section OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + $problemListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n		<!-- Order OBXs -->\r\n		<xsl:for-each select=\"*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"position()\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>	\r\n	</xsl:for-each>\r\n	\r\n</xsl:template>\r\n\r\n<!-- Patient Identifier (CX) generator -->\r\n<xsl:template name=\"patient_id\">\r\n	<xsl:param name=\"pid\" />\r\n	<xsl:param name=\"auth\" />\r\n	<xsl:param name=\"type\" />\r\n	<xsl:value-of select=\"$pid\" />\r\n	<xsl:text>^</xsl:text>              <!-- Check digit -->\r\n	<xsl:text>^</xsl:text>              <!-- Check Digit Scheme -->\r\n	<xsl:text>^</xsl:text>              <!-- Assigning Authority -->\r\n	<xsl:value-of select=\"$auth\" />\r\n	<xsl:text>^</xsl:text>              <!-- Identifier Type -->\r\n	<xsl:value-of select=\"$type\" />\r\n</xsl:template>\r\n\r\n<!-- OBX Generator -->\r\n<xsl:template name=\"obsObx\">\r\n	<xsl:param name=\"setId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"subId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"datatype\" required=\"yes\" />\r\n	<xsl:param name=\"concept\" required=\"yes\" />\r\n	<xsl:param name=\"date\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"time\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"value\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"units\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"encounterTimestamp\" required=\"yes\" />\r\n	<xsl:text>OBX</xsl:text>                     <!-- Message type -->\r\n	<xsl:text>|</xsl:text>                       <!-- Set ID -->\r\n	<xsl:value-of select=\"$setId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Observation datatype -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:text>NM</xsl:text>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$datatype\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Concept (what was observed -->\r\n	<xsl:value-of select=\"$concept\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Sub-ID -->\r\n	<xsl:value-of select=\"$subId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Value -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'TS\'\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'DT\'\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'TM\'\">\r\n			<xsl:call-template name=\"hl7Time\">\r\n				<xsl:with-param name=\"time\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"$value = \'0\' or upper-case($value) = \'FALSE\'\">0</xsl:when>\r\n				<xsl:otherwise>1</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$value\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Units -->\r\n	<xsl:value-of select=\"$units\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Reference range -->\r\n	<xsl:text>|</xsl:text>                       <!-- Abnormal flags -->\r\n	<xsl:text>|</xsl:text>                       <!-- Probability -->\r\n	<xsl:text>|</xsl:text>                       <!-- Nature of abnormal test -->\r\n	<xsl:text>|</xsl:text>                       <!-- Observation result status -->\r\n	<xsl:text>|</xsl:text>                       <!-- Effective date -->\r\n	<xsl:text>|</xsl:text>                       <!-- User defined access checks -->\r\n	<xsl:text>|</xsl:text>                       <!-- Date time of observation -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$date and $time\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"dateTime($date,$time)\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$date\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$date\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$encounterTimestamp\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>&#x000d;</xsl:text>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted timestamp -->\r\n<xsl:template name=\"hl7Timestamp\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:value-of select=\"concat(year-from-dateTime($date),format-number(month-from-dateTime($date),\'00\'),format-number(day-from-dateTime($date),\'00\'),format-number(hours-from-dateTime($date),\'00\'),format-number(minutes-from-dateTime($date),\'00\'),format-number(seconds-from-dateTime($date),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted date -->\r\n<xsl:template name=\"hl7Date\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"contains(string($date),\'T\')\">\r\n				<xsl:call-template name=\"hl7Date\">\r\n					<xsl:with-param name=\"date\" select=\"xs:date(substring-before($date,\'T\'))\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n					<xsl:value-of select=\"concat(year-from-date($date),format-number(month-from-date($date),\'00\'),format-number(day-from-date($date),\'00\'))\" />\r\n			</xsl:otherwise>\r\n		</xsl:choose>				\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted time -->\r\n<xsl:template name=\"hl7Time\">\r\n	<xsl:param name=\"time\" />\r\n	<xsl:if test=\"$time != \'\'\">\r\n		<xsl:value-of select=\"concat(format-number(hours-from-time($time),\'00\'),format-number(minutes-from-time($time),\'00\'),format-number(seconds-from-time($time),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n</xsl:stylesheet>';

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.36');

#--------------------------------------
# OpenMRS Datamodel version 1.0.39
# Ben Wolfe    Sept 6 2006 8:07 AM
# Fixing default address velocity script
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	UPDATE field SET default_value = '$!{patient.getPatientAddress().getAddress1()}' WHERE default_value = '$!{patient.getAddresses().iterator().next().getAddress1()}';
	UPDATE field SET default_value = '$!{patient.getPatientAddress().getAddress2()}' WHERE default_value = '$!{patient.getAddresses().iterator().next().getAddress2()}';
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.39');


#--------------------------------------
# OpenMRS Datamodel version 1.0.40
# Christian Allen 	Sept 8 2006 10:27 AM
# Adding county_district and neighborhood_cell attributes
#  to patient_address table
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	ALTER TABLE `patient_address` ADD COLUMN `county_district` varchar(50) default NULL;
	ALTER TABLE `patient_address` ADD COLUMN `neighborhood_cell` varchar(50) default NULL;
	
	ALTER TABLE `location` ADD COLUMN `county_district` varchar(50) default NULL;
	ALTER TABLE `location` ADD COLUMN `neighborhood_cell` varchar(50) default NULL;

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.40');

#--------------------------------------
# OpenMRS Datamodel version 1.0.41
# Burke Mamlin 	Sept 10, 2006 8:10 PM
# Adding concept_derived
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	CREATE TABLE `concept_derived` (
	  `concept_id` int(11) NOT NULL DEFAULT '0',
	  `rule` mediumtext DEFAULT NULL,
	  `compile_date` datetime DEFAULT NULL,
	  `compile_status` varchar(255) DEFAULT NULL,
	  `class_name` varchar(1024) DEFAULT NULL,
	  PRIMARY KEY  (`concept_id`),
	  CONSTRAINT `derived_attributes` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	INSERT INTO `concept_datatype` (`concept_datatype_id`, `name`, `hl7_abbreviation`, `description`, `creator`, `date_created`) values (11, 'Rule', 'ZZ', 'Value derived from other data', 1, '2006-09-11 13:22:00');

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.41');


#--------------------------------------
# OpenMRS Datamodel version 42
# Ben Wolfe		Sept 13, 2006 9:25 AM
# Changing database versioning system
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
 	#
 	#
 	# DO NOT COPY THIS PROCEDURE'S CODE. THE 'IF STATEMENT' IS NOT FORMATTED CORRECTLY.
	#               COPY/PASTE THE PREVIOUS DATABASE UPDATE PROCEDURE.
	#
	# DO CHANGE THE VERSIONING NUMBER FROM 1.0.xx TO JUST xx
	#
	#
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(CONCAT('1.0.', new_db_version), '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('42');


#--------------------------------------
# OpenMRS Datamodel version 1.0.43
# Ben Wolfe 	Sept 22, 2006 12:00 PM
# Reverting database versioning system 
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
 	#
 	#
 	# DO NOT COPY THIS PROCEDURE'S CODE. THE 'IF STATEMENT' IS NOT FORMATTED CORRECTLY.
	#               COPY/PASTE THE '1.0.41' DATABASE UPDATE PROCEDURE.
	#
	# VERSIONING IS REVERTING BACK TO x.x.xx (instead of the newly created xx)
	#
	#
	IF (SELECT property_value = '42' FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.43');


#--------------------------------------
# OpenMRS Datamodel version 1.0.44
# Christian Allen 	Oct 22, 2006 12:00 PM
# Changing patient table - cause_of_death now foreign keys to a concept
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	DECLARE _cause_id int(11);
	DECLARE _none_id int(11);
	DECLARE _other_id int(11);
	DECLARE _user_id int(11);
	DECLARE _location_id int(11);

	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

#-- START YOUR QUERY(IES) HERE --#

	SET _cause_id = 0;
	SET _none_id = 0;
	SET _other_id = 0;
	SET _user_id = 0;
	SET _location_id = 1;

	# get a user for us to make inserts with
	SET _user_id = (SELECT user_id FROM users LIMIT 1);

	# try to get unknown location - otherwise stick with 1
	IF ( SELECT count(*) > 0 FROM location WHERE lower(name) LIKE '%unknown%' ) THEN
		SET _location_id = (SELECT location_id FROM location WHERE lower(name) LIKE '%unknown%' LIMIT 1);
	END IF;

	# get concept_id for CAUSE_OF_DEATH - if it's not here, then create it
	IF( SELECT count(*) > 0 FROM concept_name WHERE lower(name) = 'cause of death' ) THEN
		SET _cause_id = (SELECT concept_id FROM concept_name WHERE lower(name) = 'cause of death' LIMIT 1);
	ELSE
		INSERT INTO concept(retired, short_name, description, form_text, datatype_id, class_id, is_set, creator, date_created, default_charge, version, changed_by, date_changed)
			VALUES(0, '', '', null, 2, 7, 0, _user_id, now(), null, '', _user_id, now());
		SET _cause_id = LAST_INSERT_ID();
		INSERT INTO concept_name(concept_id, name, short_name, description, locale, creator, date_created)
			VALUES(_cause_id, 'CAUSE OF DEATH', '', 'Describes a cause of death for a patient.  Coded answer.', 'en', _user_id, now());
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_cause_id, 'CAUSE', '', 'en');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_cause_id, 'DEATH', '', 'en');
	END IF;
	
	# get concept_id for coded answer OTHER NON-CODED - if it's not here, then create it
	IF( SELECT count(*) > 0 FROM concept_name WHERE lower(name) = 'other non-coded' ) THEN
		SET _other_id = (SELECT concept_id FROM concept_name WHERE lower(name) = 'other non-coded' LIMIT 1);
	ELSE
		INSERT INTO concept(retired, short_name, description, form_text, datatype_id, class_id, is_set, creator, date_created, default_charge, version, changed_by, date_changed)
			VALUES(0, '', '', null, 4, 11, 0, _user_id, now(), null, '', _user_id, now());
		SET _other_id = LAST_INSERT_ID();
		INSERT INTO concept_name(concept_id, name, short_name, description, locale, creator, date_created)
			VALUES(_other_id, 'OTHER NON-CODED', '', 'Non-coded answer to a coded question - allows other as a coded answer.', 'en', _user_id, now());
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_other_id, 'AUTRES', '', 'fr');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_other_id, 'AUTRE', '', 'fr');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_other_id, 'OTHER', '', 'en');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_other_id, 'CODED', '', 'en');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_other_id, 'NON', '', 'en');
	END IF;

	# get concept_id for NONE - if it's not here, then create it
	IF( SELECT count(*) > 0 FROM concept_name WHERE lower(name) = 'none' ) THEN
		SET _none_id = (SELECT concept_id FROM concept_name WHERE lower(name) = 'none' LIMIT 1);
	ELSE
		INSERT INTO concept(retired, short_name, description, form_text, datatype_id, class_id, is_set, creator, date_created, default_charge, version, changed_by, date_changed)
			VALUES(0, '', '', null, 4, 11, 0, _user_id, now(), null, '', _user_id, now());
		SET _none_id = LAST_INSERT_ID();
		INSERT INTO concept_name(concept_id, name, short_name, description, locale, creator, date_created)
			VALUES(_none_id, 'NONE', '', 'Generic descriptive answer.', 'en', _user_id, now());
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_none_id, 'AUCUN', '', 'fr');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_none_id, 'AUCUN', 'AUCUN', 'en');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_none_id, 'OTHER', '', 'en');
	END IF;

	# (for debugging) SELECT _cause_id AS 'cause_id', _none_id AS 'none_id', _other_id AS 'other_id', _user_id AS 'user_id', _location_id AS 'location_id';
			
	# make sure that OTHER NON-CODE and NONE are answers to CAUSE OF DEATH
	IF( SELECT count(*) = 0 FROM concept_answer WHERE concept_id = _cause_id and answer_concept = _other_id ) THEN
		INSERT INTO concept_answer(concept_id, answer_concept, answer_drug, creator, date_created)
			VALUES(_cause_id, _other_id, null, _user_id, now());
	END IF;
	
	IF( SELECT count(*) = 0 FROM concept_answer WHERE concept_id = _cause_id and answer_concept = _none_id ) THEN
		INSERT INTO concept_answer(concept_id, answer_concept, answer_drug, creator, date_created)
			VALUES(_cause_id, _none_id, null, _user_id, now());
	END IF;

	# ensure that patients who are alive have cause_of_death set to NULL
	UPDATE patient SET cause_of_death = null WHERE dead = 0;

	# create a table to hold patient_id, encounter_id, and encounter_datetime for the last encounter of each patient with a non-null cause_of_death
	# warning: assumes that every patient with a non-null cause_of_death has at least one encounter.  if not, encounter_id will be 0
	DROP TABLE IF EXISTS last_encounters_temp;

	CREATE TABLE `last_encounters_temp` (
		`patient_id` int(11) DEFAULT '0',
		`encounter_id` int(11) DEFAULT '0',
		`encounter_datetime` datetime,
		`cause_of_death` varchar(255) DEFAULT ''
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	INSERT INTO last_encounters_temp (patient_id, encounter_datetime)
	SELECT e.patient_id, max(e.encounter_datetime)
	FROM encounter e
	INNER JOIN patient p
	ON e.patient_id = p.patient_id
	WHERE p.dead = 1
	GROUP BY e.patient_id;
	
	UPDATE last_encounters_temp t, encounter e
	SET t.encounter_id = e.encounter_id
	WHERE t.patient_id = e.patient_id
	AND t.encounter_datetime = e.encounter_datetime;

	INSERT INTO last_encounters_temp (patient_id, encounter_id)
	SELECT patient_id, 0
	FROM patient
	WHERE dead = 1
	AND patient_id NOT IN (SELECT patient_id FROM last_encounters_temp);

	UPDATE last_encounters_temp t, patient p
	SET t.cause_of_death = p.cause_of_death
	WHERE t.patient_id = p.patient_id;

	# create an observation for each of the above patients on their last encounter, with value_coded as NONE if cause_of_death was empty string, OTHER if not
	INSERT INTO obs(patient_id, concept_id, encounter_id, order_id, obs_datetime, location_id, obs_group_id, accession_number, value_group_id, value_boolean,
			value_coded, value_drug, value_datetime, value_numeric, value_modifier, value_text, date_started, date_stopped, comments, creator, date_created,
			voided, voided_by, date_voided, void_reason)
	SELECT t.patient_id, _cause_id, t.encounter_id, null, IF(p.death_date IS NOT NULL, p.death_date, t.encounter_datetime), _location_id, null, null, null, null,
			IF(p.cause_of_death IS NOT NULL AND LENGTH(p.cause_of_death) > 0, _other_id, _none_id), null, null, null, null, IF(p.cause_of_death IS NOT NULL AND LENGTH(p.cause_of_death) > 0, p.cause_of_death, null), null, null, null, _user_id, now(),
			0, null, null, null
	FROM last_encounters_temp t
	INNER JOIN patient p
	ON t.patient_id = p.patient_id
	WHERE t.encounter_id > 0;

	# create an observation for each of the above patients on their last encounter, with value_coded as NONE if cause_of_death was empty string, OTHER if not
	INSERT INTO obs(patient_id, concept_id, encounter_id, order_id, obs_datetime, location_id, obs_group_id, accession_number, value_group_id, value_boolean,
			value_coded, value_drug, value_datetime, value_numeric, value_modifier, value_text, date_started, date_stopped, comments, creator, date_created,
			voided, voided_by, date_voided, void_reason)
	SELECT t.patient_id, _cause_id, null, null, IF(p.death_date IS NOT NULL, p.death_date, now()), _location_id, null, null, null, null,
			IF(p.cause_of_death IS NOT NULL AND LENGTH(p.cause_of_death) > 0, _other_id, _none_id), null, null, null, null, IF(p.cause_of_death IS NOT NULL AND LENGTH(p.cause_of_death) > 0, p.cause_of_death, null), null, null, null, _user_id, now(),
			0, null, null, null
	FROM last_encounters_temp t
	INNER JOIN patient p
	ON t.patient_id = p.patient_id
	WHERE t.encounter_id = 0;

	# alter tables so that they now accept an int(11) instead of varchar(255)
	UPDATE patient SET cause_of_death = null;

	ALTER TABLE `patient` MODIFY COLUMN `cause_of_death` int(11) default null;
	ALTER TABLE `patient` ADD CONSTRAINT `died_because` FOREIGN KEY (`cause_of_death`) REFERENCES `concept` (`concept_id`);
	
	# use info from last_encounters_temp table to fill in the corrent concept_id in the patient.cause_of_death field
	UPDATE patient p, last_encounters_temp t
	SET p.cause_of_death = _other_id
	WHERE p.patient_id = t.patient_id
	AND t.cause_of_death IS NOT NULL
	AND LENGTH(t.cause_of_death) > 0;

	UPDATE patient p, last_encounters_temp t
	SET p.cause_of_death = _none_id
	WHERE p.patient_id = t.patient_id
	AND p.cause_of_death IS NULL;

	# insert a global property to indicate cause_of_death concept (if it doesn't exist already)
	IF( SELECT count(*) = 0 FROM global_property WHERE lower(property) = 'concept.causeofdeath' ) THEN
		INSERT INTO global_property(property, property_value) VALUES ('concept.causeOfDeath', CONCAT(_cause_id));
	END IF;

	# cleanup
	DROP TABLE last_encounters_temp;

#-- END YOUR QUERY(IES) HERE --#

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.44');


#--------------------------------------
# OpenMRS Datamodel version 1.0.45
# Christian Allen 	Oct 24, 2006 10:00 PM
# Changing orders table - discontinued_reason now foreign keys to a concept
#
# ALTERNATE TOOL FOR REPLACING ORDERS - if you already have orders with discontinued_reason in varchar format, the following script will at least copy those
#		to a separate table so that you can later resolve what concept they should be (uncomment and execute in SQL client tool).  If your data set is small,
#		and you want to control which concepts each variation of these fields is transitioned to, you might want to do something like this instead of running
#		the script below.
#
#	CREATE TABLE `orders_discontinued_reason_temp` (
#		`order_id` int(11) NOT NULL DEFAULT '0',
#	  	`discontinued_reason` varchar(255) default NULL
#	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
#
#	INSERT INTO orders_discontinued_reason_temp
#	SELECT order_id, discontinued_reason
#	FROM orders
#	WHERE discontinued = 1;
#
#	UPDATE orders set discontinued_reason = null;
#	 
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	DECLARE _reason_id int(11);
	DECLARE _none_id int(11);
	DECLARE _other_id int(11);
	DECLARE _user_id int(11);
	DECLARE _location_id int(11);

	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	SET _reason_id = 0;
	SET _none_id = 0;
	SET _other_id = 0;
	SET _user_id = 0;
	SET _location_id = 1;

	# get a user for us to make inserts with
	SET _user_id = (SELECT user_id FROM users LIMIT 1);

	# try to get unknown location - otherwise stick with 1
	IF ( SELECT count(*) > 0 FROM location WHERE lower(name) LIKE '%unknown%' ) THEN
		SET _location_id = (SELECT location_id FROM location WHERE lower(name) LIKE '%unknown%' LIMIT 1);
	END IF;

	# get concept_id for REASON ORDER STOPPED - if it's not here, then create it
	IF( SELECT count(*) > 0 FROM concept_name WHERE lower(name) = 'reason order stopped' ) THEN
		SET _reason_id = (SELECT concept_id FROM concept_name WHERE lower(name) = 'reason order stopped' LIMIT 1);
	ELSE
		INSERT INTO concept(retired, short_name, description, form_text, datatype_id, class_id, is_set, creator, date_created, default_charge, version, changed_by, date_changed)
			VALUES(0, '', '', null, 2, 7, 0, _user_id, now(), null, '', _user_id, now());
		SET _reason_id = LAST_INSERT_ID();
		INSERT INTO concept_name(concept_id, name, short_name, description, locale, creator, date_created)
			VALUES(_reason_id, 'REASON ORDER STOPPED', '', 'Describes a reason for stopping an order.  Coded answer.', 'en', _user_id, now());
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_reason_id, 'REASON', '', 'en');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_reason_id, 'STOP', '', 'en');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_reason_id, 'STOPPED', '', 'en');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_reason_id, 'DRUG', '', 'en');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_reason_id, 'DISCONTINUE', '', 'en');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_reason_id, 'DISCONTINUED', '', 'en');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_reason_id, 'ORDER', '', 'en');
	END IF;
	
	# get concept_id for coded answer OTHER NON-CODED - if it's not here, then create it
	IF( SELECT count(*) > 0 FROM concept_name WHERE lower(name) = 'other non-coded' ) THEN
		SET _other_id = (SELECT concept_id FROM concept_name WHERE lower(name) = 'other non-coded' LIMIT 1);
	ELSE
		INSERT INTO concept(retired, short_name, description, form_text, datatype_id, class_id, is_set, creator, date_created, default_charge, version, changed_by, date_changed)
			VALUES(0, '', '', null, 4, 11, 0, _user_id, now(), null, '', _user_id, now());
		SET _other_id = LAST_INSERT_ID();
		INSERT INTO concept_name(concept_id, name, short_name, description, locale, creator, date_created)
			VALUES(_other_id, 'OTHER NON-CODED', '', 'Non-coded answer to a coded question - allows other as a coded answer.', 'en', _user_id, now());
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_other_id, 'AUTRES', '', 'fr');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_other_id, 'AUTRE', '', 'fr');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_other_id, 'OTHER', '', 'en');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_other_id, 'CODED', '', 'en');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_other_id, 'NON', '', 'en');
	END IF;

	# get concept_id for NONE - if it's not here, then create it
	IF( SELECT count(*) > 0 FROM concept_name WHERE lower(name) = 'none' ) THEN
		SET _none_id = (SELECT concept_id FROM concept_name WHERE lower(name) = 'none' LIMIT 1);
	ELSE
		INSERT INTO concept(retired, short_name, description, form_text, datatype_id, class_id, is_set, creator, date_created, default_charge, version, changed_by, date_changed)
			VALUES(0, '', '', null, 4, 11, 0, _user_id, now(), null, '', _user_id, now());
		SET _none_id = LAST_INSERT_ID();
		INSERT INTO concept_name(concept_id, name, short_name, description, locale, creator, date_created)
			VALUES(_none_id, 'NONE', '', 'Generic descriptive answer.', 'en', _user_id, now());
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_none_id, 'AUCUN', '', 'fr');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_none_id, 'AUCUN', 'AUCUN', 'en');
		INSERT INTO concept_word(concept_id, word, synonym, locale) VALUES(_none_id, 'OTHER', '', 'en');
	END IF;

	# (for debugging) SELECT _reason_id AS 'cause_id', _none_id AS 'none_id', _other_id AS 'other_id', _user_id AS 'user_id', _location_id AS 'location_id';
			
	# make sure that OTHER NON-CODE and NONE are answers to CAUSE OF DEATH
	IF( SELECT count(*) = 0 FROM concept_answer WHERE concept_id = _reason_id and answer_concept = _other_id ) THEN
		INSERT INTO concept_answer(concept_id, answer_concept, answer_drug, creator, date_created)
			VALUES(_reason_id, _other_id, null, _user_id, now());
	END IF;
	
	IF( SELECT count(*) = 0 FROM concept_answer WHERE concept_id = _reason_id and answer_concept = _none_id ) THEN
		INSERT INTO concept_answer(concept_id, answer_concept, answer_drug, creator, date_created)
			VALUES(_reason_id, _none_id, null, _user_id, now());
	END IF;

	# ensure that patients who are alive have cause_of_death set to NULL
	UPDATE orders SET discontinued_reason = null WHERE discontinued = 0;

	# create temp table to store reasons so we can later put the right concepts into place in the orders table
	DROP TABLE IF EXISTS `orders_discontinued_reasons_temp`;
	CREATE TABLE `orders_discontinued_reasons_temp` (
		`order_id` int(11) NOT NULL DEFAULT '0',
	  	`discontinued_reason` varchar(255) default NULL
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	INSERT INTO orders_discontinued_reasons_temp
	SELECT order_id, discontinued_reason
	FROM orders
	WHERE discontinued = 1;

	# create an observation for each of the above orders, with value_coded as NONE if discontinued_reason was empty string, OTHER if not
	INSERT INTO obs(patient_id, concept_id, encounter_id, order_id, obs_datetime, location_id, obs_group_id, accession_number, value_group_id, value_boolean,
			value_coded, value_drug, value_datetime, value_numeric, value_modifier, value_text, date_started, date_stopped, comments, creator, date_created,
			voided, voided_by, date_voided, void_reason)
	SELECT e.patient_id, _reason_id, null, null, IF(o.discontinued_date IS NOT NULL, o.discontinued_date, now()), _location_id, null, null, null, null,
			IF(o.discontinued_reason IS NOT NULL AND LENGTH(o.discontinued_reason) > 0, _other_id, _none_id), null, null, null, null, IF(o.discontinued_reason IS NOT NULL AND LENGTH(o.discontinued_reason) > 0, o.discontinued_reason, null), null, null, null, _user_id, now(),
			0, null, null, null
	FROM orders o
	INNER JOIN encounter e
	ON o.encounter_id = e.encounter_id
	WHERE o.discontinued = 1;

	# alter tables so that they now accept an int(11) instead of varchar(255)
	UPDATE orders set discontinued_reason = null;

	ALTER TABLE `orders` MODIFY COLUMN `discontinued_reason` int(11) default null;
	ALTER TABLE `orders` ADD CONSTRAINT `discontinued_because` FOREIGN KEY (`discontinued_reason`) REFERENCES `concept` (`concept_id`);
	
	# use info from temp table to fill in the correct concept_id in the orders.discontinued_reason field
	UPDATE orders o, orders_discontinued_reasons_temp t
	SET o.discontinued_reason = _other_id
	WHERE o.order_id = t.order_id
	AND t.discontinued_reason IS NOT NULL
	AND LENGTH(t.discontinued_reason) > 0;

	UPDATE orders o, orders_discontinued_reasons_temp t
	SET o.discontinued_reason = _none_id
	WHERE o.order_id = t.order_id
	AND o.discontinued_reason IS NULL;

	# insert a global property to indicate reason_order_stopped concept (if it doesn't exist already)
	IF( SELECT count(*) = 0 FROM global_property WHERE lower(property) = 'concept.reasonorderstopped' ) THEN
		INSERT INTO global_property(property, property_value) VALUES ('concept.reasonOrderStopped', CONCAT(_reason_id));
	END IF;

	# cleanup
	DROP TABLE orders_discontinued_reasons_temp;

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.45');

#--------------------------------------
# OpenMRS Datamodel version 1.0.46
# Ben Wolfe  Nov 11, 2006 11:15 AM
# Fixing obs section in basic form
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	IF (SELECT concept_id = 1238 FROM field WHERE field_id = 4) THEN
		IF (SELECT `name` = 'OBS' FROM field WHERE field_id = 5) THEN
			IF (SELECT field_id = 5 FROM form_field WHERE form_field_id = 5) THEN
				UPDATE `form_field` SET field_id = 4 WHERE form_field_id = 5;
			END IF;
		END IF;
	END IF;
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.46');

#--------------------------------------
# OpenMRS Datamodel version 1.0.47
# Burke Mamlin  Nov 29, 2006 2:53 AM
# Fixing fix to obs section in basic form
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	IF (SELECT `name` = 'OBS' FROM field WHERE field_id = 5) THEN
		IF (SELECT field_id = 4 FROM form_field WHERE form_field_id = 5) THEN
			UPDATE `form_field` SET field_id = 5 WHERE form_field_id = 5;
			UPDATE `field` SET field_type = 1, concept_id = 1238 WHERE field_id = 5;
		END IF;
	END IF;
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.47');


#--------------------------------------
# OpenMRS Datamodel version 1.0.48
# Christian Allen 	Oct 31, 2006 23:28 PM
# Adding table for mapping between Concept and resulting States
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	CREATE TABLE `concept_state_conversion` (
		`concept_state_conversion_id` int(11) NOT NULL auto_increment,
		`concept_id` int(11) DEFAULT '0',
		`program_workflow_id` int(11) DEFAULT '0',
		`program_workflow_state_id` int(11) DEFAULT '0',
		  PRIMARY KEY  (`concept_state_conversion_id`),
		  KEY `triggering_concept` (`concept_id`),
		  KEY `affected_workflow` (`program_workflow_id`),
		  KEY `resulting_state` (`program_workflow_state_id`),
		  CONSTRAINT `concept_triggers_conversion` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
		  CONSTRAINT `conversion_involves_workflow` FOREIGN KEY (`program_workflow_id`) REFERENCES `program_workflow` (`program_workflow_id`),
		  CONSTRAINT `conversion_to_state` FOREIGN KEY (`program_workflow_state_id`) REFERENCES `program_workflow_state` (`program_workflow_state_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.48');

#--------------------------------------
# OpenMRS Datamodel version 1.0.49
# Christian Allen 	Dec 14, 2006 05:47 AM
# Adding unique constraint to concept_state_conversion table
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

#-- START YOUR QUERY(IES) HERE --#

	ALTER TABLE `concept_state_conversion` ADD CONSTRAINT `unique_workflow_concept_in_conversion` UNIQUE KEY (`program_workflow_id`, `concept_id`);

#-- END YOUR QUERY(IES) HERE --#

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.49');


#--------------------------------------
# OpenMRS Datamodel version 1.0.50
# Burke Mamlin 	Dec 14, 2006 23:28 PM
# Fixing bug and updating XSLT for FormEntry to 1.9.3
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	# First fix any cases where format-number might be used on seconds without use of floor() to trim tenths of seconds
	UPDATE `form`
	SET
		`xslt` = replace(
			replace(
				xslt,
				'format-number(seconds-from-time($time)',
				'format-number(floor(seconds-from-time($time))'
			),
			'format-number(seconds-from-dateTime($date)',
			'format-number(floor(seconds-from-dateTime($date))'
		);

	# Next fix casting of encounter_datetime when sending it to date formatting template
	UPDATE `form`
	SET
		`xslt` = replace(
			xslt,
			'xs:date(encounter/encounter.encounter_datetime)',
			'encounter/encounter.encounter_datetime'
		);

	# Lastly, save the XSLT for the basic form (id = 1)
	SET @xslt = (SELECT `xslt` FROM `form` where form_id = 1);
	
	# Finally, update all forms using the basic form XSLT to latest version
	UPDATE `form`
	SET `xslt` = '<?xml version="1.0" encoding="UTF-8"?>\r\n\r\n<!--\r\nOpenMRS FormEntry Form HL7 Translation\r\n\r\nThis XSLT is used to translate OpenMRS forms from XML into HL7 2.5 format\r\n\r\n@author Burke Mamlin, MD\r\n@author Ben Wolfe\r\n@version 1.9.4\r\n\r\n1.9.4 - add support for message uid (as HL7 control id) and transform of patient.health_center to Discharge to Location (PV1-37)\r\n1.9.3 - fixed rounding error on timestamp (tenths of seconds getting rounded up, causing "60" seconds in some cases) \r\n1.9.2 - first generally useful version\r\n-->\r\n\r\n<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:xdt="http://www.w3.org/2005/xpath-datatypes">\r\n	<xsl:output method="text" version="1.0" encoding="UTF-8" indent="no"/>\r\n\r\n<xsl:variable name="SENDING-APPLICATION">FORMENTRY</xsl:variable>\r\n<xsl:variable name="SENDING-FACILITY">AMRS.ELD</xsl:variable>\r\n<xsl:variable name="RECEIVING-APPLICATION">HL7LISTENER</xsl:variable>\r\n<xsl:variable name="RECEIVING-FACILITY">AMRS.ELD</xsl:variable>\r\n<xsl:variable name="PATIENT-AUTHORITY"></xsl:variable> <!-- leave blank for internal id, max 20 characters -->\r\n                                                       <!-- for now, must match patient_identifier_type.name -->\r\n<xsl:variable name="FORM-AUTHORITY">AMRS.ELD.FORMID</xsl:variable> <!-- max 20 characters -->\r\n\r\n<xsl:template match="/">\r\n	<xsl:apply-templates />\r\n</xsl:template>\r\n\r\n<!-- Form template -->\r\n<xsl:template match="form">\r\n	<!-- MSH Header -->\r\n	<xsl:text>MSH|^~\\&amp;</xsl:text>   <!-- Message header, field separator, and encoding characters -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-3 Sending application -->\r\n	<xsl:value-of select="$SENDING-APPLICATION" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-4 Sending facility -->\r\n	<xsl:value-of select="$SENDING-FACILITY" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-5 Receiving application -->\r\n	<xsl:value-of select="$RECEIVING-APPLICATION" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-6 Receiving facility -->\r\n	<xsl:value-of select="$RECEIVING-FACILITY" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-7 Date/time message sent -->\r\n	<xsl:call-template name="hl7Timestamp">\r\n		<xsl:with-param name="date" select="current-dateTime()" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- MSH-8 Security -->\r\n	<xsl:text>|ORU^R01</xsl:text>       <!-- MSH-9 Message type ^ Event type (observation report unsolicited) -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-10 Message control ID -->\r\n	<xsl:choose>\r\n		<xsl:when test="header/uid">\r\n			<xsl:value-of select="header/uid" />\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select="patient/patient.patient_id" />\r\n			<xsl:call-template name="hl7Timestamp">\r\n				<xsl:with-param name="date" select="current-dateTime()" />\r\n			</xsl:call-template>\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|P</xsl:text>             <!-- MSH-11 Processing ID -->\r\n	<xsl:text>|2.5</xsl:text>           <!-- MSH-12 HL7 version -->\r\n	<xsl:text>|1</xsl:text>             <!-- MSH-13 Message sequence number -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-14 Continuation Pointer -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-15 Accept Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-16 Application Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-17 Country Code -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-18 Character Set -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-19 Principal Language of Message -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-20 Alternate Character Set Handling Scheme -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-21 Message Profile Identifier -->\r\n	<xsl:value-of select="@id" />\r\n	<xsl:text>^</xsl:text>\r\n	<xsl:value-of select="$FORM-AUTHORITY" />\r\n	<xsl:text>&#x000d;</xsl:text>\r\n\r\n	<!-- PID header -->\r\n	<xsl:text>PID</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-1 Set ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-2 (deprecated) Patient ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-3 Patient Identifier List -->\r\n	<xsl:call-template name="patient_id">\r\n		<xsl:with-param name="pid" select="patient/patient.patient_id" />\r\n		<xsl:with-param name="auth" select="$PATIENT-AUTHORITY" />\r\n		<xsl:with-param name="type" select="L" />\r\n	</xsl:call-template>\r\n	<xsl:if test="patient/patient.previous_mrn and string-length(patient/patient.previous_mrn) > 0">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name="patient_id">\r\n			<xsl:with-param name="pid" select="patient/patient.previous_mrn" />\r\n			<xsl:with-param name="auth" select="$PATIENT-AUTHORITY" />\r\n			<xsl:with-param name="type" select="PRIOR" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<!-- Additional patient identifiers -->\r\n	<!-- This example is for an MTCT-PLUS identifier used in the AMPATH project in Kenya (skipped if not present) -->\r\n	<xsl:if test="patient/patient.mtctplus_id and string-length(patient/patient.mtctplus_id) > 0">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name="patient_id">\r\n			<xsl:with-param name="pid" select="patient/patient.mtctplus_id" />\r\n			<xsl:with-param name="auth" select="$PATIENT-AUTHORITY" />\r\n			<xsl:with-param name="type" select="MTCTPLUS" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PID-4 (deprecated) Alternate patient ID -->\r\n	<!-- PID-5 Patient name -->\r\n	<xsl:text>|</xsl:text>              <!-- Family name -->\r\n	<xsl:value-of select="patient/patient.family_name" />\r\n	<xsl:text>^</xsl:text>              <!-- Given name -->\r\n	<xsl:value-of select="patient/patient.given_name" />\r\n	<xsl:text>^</xsl:text>              <!-- Middle name -->\r\n	<xsl:value-of select="patient/patient.middle_name" />\r\n	<xsl:text>|</xsl:text>              <!-- PID-6 Mother''s maiden name -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-7 Date/Time of Birth -->\r\n	<xsl:value-of select="patient/patient.date_of_birth" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n	<!-- PV1 header -->\r\n	<xsl:text>PV1</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-1 Sub ID -->\r\n	<xsl:text>|O</xsl:text>             <!-- PV1-2 Patient class (O = outpatient) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-3 Patient location -->\r\n	<xsl:value-of select="encounter/encounter.location_id" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-4 Admission type (2 = return) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-5 Pre-Admin Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-6 Prior Patient Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-7 Attending Doctor -->\r\n	<xsl:value-of select="encounter/encounter.provider_id" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-8 Referring Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-9 Consulting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-10 Hospital Service -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-11 Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-12 Preadmin Test Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-13 Re-adminssion Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-14 Admit Source -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-15 Ambulatory Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-16 VIP Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-17 Admitting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-18 Patient Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-19 Visit Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-20 Financial Class -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-21 Charge Price Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-22 Courtesy Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-23 Credit Rating -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-24 Contract Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-25 Contract Effective Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-26 Contract Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-27 Contract Period -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-28 Interest Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-29 Transfer to Bad Debt Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-30 Transfer to Bad Debt Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Agency Code -->\r\n  <xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Transfer Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-33 Bad Debt Recovery Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-34 Delete Account Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-35 Delete Account Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-36 Discharge Disposition -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-37 Discharge To Location -->\r\n	<xsl:if test="patient/patient.health_center">\r\n		<xsl:value-of select="replace(patient/patient.health_center,''\\^'',''&amp;'')" />\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-38 Diet Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-39 Servicing Facility -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-40 Bed Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-41 Account Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-42 Pending Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-43 Prior Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-44 Admit Date/Time -->\r\n	<xsl:call-template name="hl7Date">\r\n		<xsl:with-param name="date" select="encounter/encounter.encounter_datetime" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-45 Discharge Date/Time -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-46 Current Patient Balance -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-47 Total Charges -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-48 Total Adjustments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-49 Total Payments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-50 Alternate Visit ID -->\r\n	<xsl:text>|V</xsl:text>             <!-- PV1-51 Visit Indicator -->\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- We use encounter date as the timestamp for each observation -->\r\n	<xsl:variable name="encounterTimestamp">\r\n		<xsl:call-template name="hl7Date">\r\n			<xsl:with-param name="date" select="encounter/encounter.encounter_datetime" />\r\n		</xsl:call-template>\r\n	</xsl:variable>\r\n	\r\n	<!-- ORC Common Order Segment -->\r\n	<xsl:text>ORC</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|RE</xsl:text>            <!-- ORC-1 Order Control (RE = obs to follow) -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-2 Placer Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-3 Filler Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-4 Placer Group Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-5 Order Status -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-6 Response Flag -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-7 Quantity/Timing -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-8 Parent -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-9 Date/Time of Transaction -->\r\n	<xsl:call-template name="hl7Timestamp">\r\n		<xsl:with-param name="date" select="xs:dateTime(header/date_entered)" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- ORC-10 Entered By -->\r\n	<xsl:value-of select="header/enterer" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- Observation(s) -->\r\n	<xsl:variable name="obsList" select="obs/*[(@openmrs_concept and value and value/text() != '''') or *[@openmrs_concept and text()=''true'']]" />\r\n	<xsl:variable name="obsListCount" select="count($obsList)" as="xs:integer" />\r\n	<!-- Observation OBR -->\r\n	<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n	<xsl:text>1</xsl:text>\r\n	<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n	<xsl:value-of select="obs/@openmrs_concept" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- observation OBXs -->\r\n	<xsl:for-each select="$obsList">\r\n		<xsl:choose>\r\n			<xsl:when test="value">\r\n				<xsl:call-template name="obsObx">\r\n					<xsl:with-param name="setId" select="position()" />\r\n					<xsl:with-param name="datatype" select="@openmrs_datatype" />\r\n					<xsl:with-param name="units" select="@openmrs_units" />\r\n					<xsl:with-param name="concept" select="@openmrs_concept" />\r\n					<xsl:with-param name="date" select="date/text()" />\r\n					<xsl:with-param name="time" select="time/text()" />\r\n					<xsl:with-param name="value" select="value" />\r\n					<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n				<xsl:variable name="setId" select="position()" />\r\n				<xsl:for-each select="*[@openmrs_concept and text() = ''true'']">\r\n					<xsl:call-template name="obsObx">\r\n						<xsl:with-param name="setId" select="$setId" />\r\n						<xsl:with-param name="subId" select="concat($setId,position())" />\r\n						<xsl:with-param name="datatype" select="../@openmrs_datatype" />\r\n						<xsl:with-param name="units" select="../@openmrs_units" />\r\n						<xsl:with-param name="concept" select="../@openmrs_concept" />\r\n						<xsl:with-param name="date" select="../date/text()" />\r\n						<xsl:with-param name="time" select="../time/text()" />\r\n						<xsl:with-param name="value" select="@openmrs_concept" />\r\n						<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />\r\n					</xsl:call-template>\r\n				</xsl:for-each>\r\n			</xsl:otherwise>\r\n		</xsl:choose>\r\n	</xsl:for-each>\r\n	\r\n	<!-- Grouped observation(s) -->\r\n	<xsl:variable name="obsGroupList" select="obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != '''') or *[@openmrs_concept and text()=''true'']]]" />\r\n	<xsl:variable name="obsGroupListCount" select="count($obsGroupList)" as="xs:integer" />\r\n	<xsl:for-each select="$obsGroupList">\r\n		<!-- Observation OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select="$obsListCount + position()" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select="@openmrs_concept" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n		\r\n		<!-- Generate OBXs -->\r\n		<xsl:for-each select="*[(@openmrs_concept and value and value/text() != '''') or *[@openmrs_concept and text()=''true'']]">\r\n			<xsl:choose>\r\n				<xsl:when test="value">\r\n					<xsl:call-template name="obsObx">\r\n						<xsl:with-param name="setId" select="position()" />\r\n						<xsl:with-param name="subId" select="1" />\r\n						<xsl:with-param name="datatype" select="@openmrs_datatype" />\r\n						<xsl:with-param name="units" select="@openmrs_units" />\r\n						<xsl:with-param name="concept" select="@openmrs_concept" />\r\n						<xsl:with-param name="date" select="date/text()" />\r\n						<xsl:with-param name="time" select="time/text()" />\r\n						<xsl:with-param name="value" select="value" />\r\n						<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name="setId" select="position()" />\r\n					<xsl:for-each select="*[@openmrs_concept and text() = ''true'']">\r\n						<xsl:call-template name="obsObx">\r\n							<xsl:with-param name="setId" select="$setId" />\r\n							<xsl:with-param name="subId" select="concat(''1.'',position())" />\r\n							<xsl:with-param name="datatype" select="../@openmrs_datatype" />\r\n							<xsl:with-param name="units" select="../@openmrs_units" />\r\n							<xsl:with-param name="concept" select="../@openmrs_concept" />\r\n							<xsl:with-param name="date" select="../date/text()" />\r\n							<xsl:with-param name="time" select="../time/text()" />\r\n							<xsl:with-param name="value" select="@openmrs_concept" />\r\n							<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>\r\n	</xsl:for-each>\r\n\r\n	<!-- Problem list(s) -->\r\n	<xsl:variable name="problemList" select="problem_list/*[value[text() != '''']]" />\r\n	<xsl:variable name="problemListCount" select="count($problemList)" as="xs:integer" />\r\n	<xsl:if test="$problemList">\r\n		<!-- Problem list OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select="$obsListCount + $obsGroupListCount + 1" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select="problem_list/@openmrs_concept" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n		<!-- Problem list OBXs -->\r\n		<xsl:for-each select="$problemList">\r\n			<xsl:call-template name="obsObx">\r\n				<xsl:with-param name="setId" select="position()" />\r\n				<xsl:with-param name="datatype" select="''CWE''" />\r\n				<xsl:with-param name="concept" select="@openmrs_concept" />\r\n				<xsl:with-param name="date" select="date/text()" />\r\n				<xsl:with-param name="time" select="time/text()" />\r\n				<xsl:with-param name="value" select="value" />\r\n				<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />\r\n			</xsl:call-template>		\r\n		</xsl:for-each>\r\n	</xsl:if>\r\n	\r\n	<!-- Orders -->\r\n	<xsl:variable name="orderList" select="orders/*[*[@openmrs_concept and ((value and value/text() != '''') or *[@openmrs_concept and text() = ''true''])]]" />\r\n	<xsl:variable name="orderListCount" select="count($orderList)" as="xs:integer" />\r\n	<xsl:for-each select="$orderList">\r\n		<!-- Order section OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select="$obsListCount + $obsGroupListCount + $problemListCount + 1" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select="@openmrs_concept" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n		<!-- Order OBXs -->\r\n		<xsl:for-each select="*[@openmrs_concept and ((value and value/text() != '''') or *[@openmrs_concept and text() = ''true''])]">\r\n			<xsl:choose>\r\n				<xsl:when test="value">\r\n					<xsl:call-template name="obsObx">\r\n						<xsl:with-param name="setId" select="position()" />\r\n						<xsl:with-param name="datatype" select="@openmrs_datatype" />\r\n						<xsl:with-param name="units" select="@openmrs_units" />\r\n						<xsl:with-param name="concept" select="@openmrs_concept" />\r\n						<xsl:with-param name="date" select="date/text()" />\r\n						<xsl:with-param name="time" select="time/text()" />\r\n						<xsl:with-param name="value" select="value" />\r\n						<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name="setId" select="position()" />\r\n					<xsl:for-each select="*[@openmrs_concept and text() = ''true'']">\r\n						<xsl:call-template name="obsObx">\r\n							<xsl:with-param name="setId" select="$setId" />\r\n							<xsl:with-param name="subId" select="position()" />\r\n							<xsl:with-param name="datatype" select="../@openmrs_datatype" />\r\n							<xsl:with-param name="units" select="../@openmrs_units" />\r\n							<xsl:with-param name="concept" select="../@openmrs_concept" />\r\n							<xsl:with-param name="date" select="../date/text()" />\r\n							<xsl:with-param name="time" select="../time/text()" />\r\n							<xsl:with-param name="value" select="@openmrs_concept" />\r\n							<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>	\r\n	</xsl:for-each>\r\n	\r\n</xsl:template>\r\n\r\n<!-- Patient Identifier (CX) generator -->\r\n<xsl:template name="patient_id">\r\n	<xsl:param name="pid" />\r\n	<xsl:param name="auth" />\r\n	<xsl:param name="type" />\r\n	<xsl:value-of select="$pid" />\r\n	<xsl:text>^</xsl:text>              <!-- Check digit -->\r\n	<xsl:text>^</xsl:text>              <!-- Check Digit Scheme -->\r\n	<xsl:text>^</xsl:text>              <!-- Assigning Authority -->\r\n	<xsl:value-of select="$auth" />\r\n	<xsl:text>^</xsl:text>              <!-- Identifier Type -->\r\n	<xsl:value-of select="$type" />\r\n</xsl:template>\r\n\r\n<!-- OBX Generator -->\r\n<xsl:template name="obsObx">\r\n	<xsl:param name="setId" required="no"></xsl:param>\r\n	<xsl:param name="subId" required="no"></xsl:param>\r\n	<xsl:param name="datatype" required="yes" />\r\n	<xsl:param name="concept" required="yes" />\r\n	<xsl:param name="date" required="no"></xsl:param>\r\n	<xsl:param name="time" required="no"></xsl:param>\r\n	<xsl:param name="value" required="no"></xsl:param>\r\n	<xsl:param name="units" required="no"></xsl:param>\r\n	<xsl:param name="encounterTimestamp" required="yes" />\r\n	<xsl:text>OBX</xsl:text>                     <!-- Message type -->\r\n	<xsl:text>|</xsl:text>                       <!-- Set ID -->\r\n	<xsl:value-of select="$setId" />\r\n	<xsl:text>|</xsl:text>                       <!-- Observation datatype -->\r\n	<xsl:choose>\r\n		<xsl:when test="$datatype = ''BIT''">\r\n			<xsl:text>NM</xsl:text>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select="$datatype" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Concept (what was observed -->\r\n	<xsl:value-of select="$concept" />\r\n	<xsl:text>|</xsl:text>                       <!-- Sub-ID -->\r\n	<xsl:value-of select="$subId" />\r\n	<xsl:text>|</xsl:text>                       <!-- Value -->\r\n	<xsl:choose>\r\n		<xsl:when test="$datatype = ''TS''">\r\n			<xsl:call-template name="hl7Timestamp">\r\n				<xsl:with-param name="date" select="$value" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test="$datatype = ''DT''">\r\n			<xsl:call-template name="hl7Date">\r\n				<xsl:with-param name="date" select="$value" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test="$datatype = ''TM''">\r\n			<xsl:call-template name="hl7Time">\r\n				<xsl:with-param name="time" select="$value" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test="$datatype = ''BIT''">\r\n			<xsl:choose>\r\n				<xsl:when test="$value = ''0'' or upper-case($value) = ''FALSE''">0</xsl:when>\r\n				<xsl:otherwise>1</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select="$value" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Units -->\r\n	<xsl:value-of select="$units" />\r\n	<xsl:text>|</xsl:text>                       <!-- Reference range -->\r\n	<xsl:text>|</xsl:text>                       <!-- Abnormal flags -->\r\n	<xsl:text>|</xsl:text>                       <!-- Probability -->\r\n	<xsl:text>|</xsl:text>                       <!-- Nature of abnormal test -->\r\n	<xsl:text>|</xsl:text>                       <!-- Observation result status -->\r\n	<xsl:text>|</xsl:text>                       <!-- Effective date -->\r\n	<xsl:text>|</xsl:text>                       <!-- User defined access checks -->\r\n	<xsl:text>|</xsl:text>                       <!-- Date time of observation -->\r\n	<xsl:choose>\r\n		<xsl:when test="$date and $time">\r\n			<xsl:call-template name="hl7Timestamp">\r\n				<xsl:with-param name="date" select="dateTime($date,$time)" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test="$date">\r\n			<xsl:call-template name="hl7Date">\r\n				<xsl:with-param name="date" select="$date" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select="$encounterTimestamp" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>&#x000d;</xsl:text>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted timestamp -->\r\n<xsl:template name="hl7Timestamp">\r\n	<xsl:param name="date" />\r\n	<xsl:if test="string($date) != ''''">\r\n		<xsl:value-of select="concat(year-from-dateTime($date),format-number(month-from-dateTime($date),''00''),format-number(day-from-dateTime($date),''00''),format-number(hours-from-dateTime($date),''00''),format-number(minutes-from-dateTime($date),''00''),format-number(floor(seconds-from-dateTime($date)),''00''))" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted date -->\r\n<xsl:template name="hl7Date">\r\n	<xsl:param name="date" />\r\n	<xsl:if test="string($date) != ''''">\r\n		<xsl:choose>\r\n			<xsl:when test="contains(string($date),''T'')">\r\n				<xsl:call-template name="hl7Date">\r\n					<xsl:with-param name="date" select="xs:date(substring-before($date,''T''))" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n					<xsl:value-of select="concat(year-from-date($date),format-number(month-from-date($date),''00''),format-number(day-from-date($date),''00''))" />\r\n			</xsl:otherwise>\r\n		</xsl:choose>				\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted time -->\r\n<xsl:template name="hl7Time">\r\n	<xsl:param name="time" />\r\n	<xsl:if test="$time != ''''">\r\n		<xsl:value-of select="concat(format-number(hours-from-time($time),''00''),format-number(minutes-from-time($time),''00''),format-number(floor(seconds-from-time($time)),''00''))" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n</xsl:stylesheet>'
	WHERE `xslt` = @xslt;
	
	# Remove unused attributes
	ALTER TABLE `formentry_queue` DROP COLUMN `status`;
	ALTER TABLE `formentry_queue` DROP COLUMN `date_processed`;
	ALTER TABLE `formentry_queue` DROP COLUMN `error_msg`;

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.50');

#--------------------------------------
# OpenMRS Datamodel version 1.0.51
# Christian Allen 	Dec 21, 2006 08:38 AM
# Adding 2 columns ('required' and 'format_description' to patient_identifier_type table
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

#-- START YOUR QUERY(IES) HERE --#

	ALTER TABLE `patient_identifier_type` ADD COLUMN `required` tinyint(1) NOT NULL default '0';
	ALTER TABLE `patient_identifier_type` ADD COLUMN `format_description` varchar(255) default NULL;

#-- END YOUR QUERY(IES) HERE --#

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.51');


#--------------------------------------
# OpenMRS Datamodel version 1.0.52
# Ben Wolfe            Feb 06 2007
# Adding description column to global property
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

#-- START YOUR QUERY(IES) HERE --#

	ALTER TABLE `global_property` ADD COLUMN `description` text default NULL;

#-- END YOUR QUERY(IES) HERE --#

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.52');


#--------------------------------------
# OpenMRS Datamodel version 1.0.53
# Darius Jazayeri           Feb 15 2007
# Adding cohort and cohort_member
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

#-- START YOUR QUERY(IES) HERE --#

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
	
	CREATE TABLE `cohort_member` (
	  `cohort_id` int(11) NOT NULL default '0',
	  `patient_id` int(11) NOT NULL default '0',
	  PRIMARY KEY  (`cohort_id`, `patient_id`),
	  KEY `cohort` (`cohort_id`),
	  KEY `patient` (`patient_id`),
	  CONSTRAINT `parent_cohort` FOREIGN KEY (`cohort_id`) REFERENCES `cohort` (`cohort_id`),
	  CONSTRAINT `member_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#-- END YOUR QUERY(IES) HERE --#

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.53');


#--------------------------------------
# OpenMRS Datamodel version 1.0.54
# Darius Jazayeri           Feb 26 2007
# Lengthening global_property.property_value
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

#-- START YOUR QUERY(IES) HERE --#

	alter table global_property modify property_value text;

#-- END YOUR QUERY(IES) HERE --#

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.54');


#--------------------------------------
# OpenMRS Datamodel version 1.0.55
# Ben Wolfe            Mar 27 2007
# Adding indexes to concept, concept_word, location tables
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

#-- START YOUR QUERY(IES) HERE --#
	
	CREATE INDEX `name_of_concept` ON concept_name (`name`);
	CREATE INDEX `short_name_of_concept` ON concept_name (`short_name`);
	CREATE INDEX `word_in_concept_name` ON concept_word (`word`);
	CREATE INDEX `name_of_location` ON location (`name`);

#-- END YOUR QUERY(IES) HERE --#

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.55');

#--------------------------------------
# OpenMRS Datamodel version 1.0.56
# Christian Allen            16 Apr 2007
# Adding patient_id column to orders table
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

#-- START YOUR QUERY(IES) HERE --#
	
	ALTER TABLE `orders` ADD COLUMN `patient_id` int(11) default NULL;
	update orders o, encounter e set o.patient_id=e.patient_id where o.encounter_id=e.encounter_id;	
	ALTER TABLE `orders` MODIFY COLUMN `patient_id` int(11) NOT NULL;
	ALTER TABLE `orders` ADD INDEX `order_for_patient` (`patient_id`);
	ALTER TABLE `orders` ADD CONSTRAINT `order_for_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`);

#-- END YOUR QUERY(IES) HERE --#

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.56');


#--------------------------------------
# OpenMRS Datamodel version 1.0.57
# Ben Wolfe  Jan 24, 2007 11:05 AM
# Merging patient/user/persion
#--------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
 	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	select CONCAT('Updating to patient/user/person at: ', now()) as 'Timestamp' from dual;
	
	# /* remove foreign keys so we can change all patient ids to new person ids */
	ALTER TABLE person DROP FOREIGN KEY patients;
	ALTER TABLE person DROP FOREIGN KEY users;
	ALTER TABLE person DROP INDEX patients;
	ALTER TABLE person DROP INDEX users;
	
	# /* make all patient foreign keys auto update ("cascade" when patient.patient_id is updated) */
	select 'Changing foreign keys to ON CASCADE' as 'Action' from dual;
	ALTER TABLE patient_name DROP FOREIGN KEY name_for_patient;
	ALTER TABLE patient_name ADD CONSTRAINT `name_for_patient` FOREIGN KEY name_for_patient (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE;
	ALTER TABLE patient_address DROP FOREIGN KEY patient_addresses;
	ALTER TABLE patient_address ADD CONSTRAINT `patient_addresses` FOREIGN KEY patient_addresses (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE;
	ALTER TABLE patient_identifier DROP FOREIGN KEY identifies_patient;
	ALTER TABLE patient_identifier ADD CONSTRAINT `identifies_patient` FOREIGN KEY identifies_patient (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE;
	select 'Changing encounter foreign keys to ON CASCADE' as 'Action' from dual;
	ALTER TABLE encounter DROP FOREIGN KEY encounter_patient;
	ALTER TABLE encounter ADD CONSTRAINT `encounter_patient` FOREIGN KEY encounter_patient (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE;
	select 'Changing note foreign keys to ON CASCADE' as 'Action' from dual;
	ALTER TABLE note DROP FOREIGN KEY patient_note;
	ALTER TABLE note ADD CONSTRAINT `patient_note` FOREIGN KEY patient_note (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE;
	# /* Modifying the keys on obs is much too slow.  Change patients manually */
	select 'Changing obs foreign keys to ON CASCADE' as 'Action' from dual;
	ALTER TABLE obs DROP FOREIGN KEY patient_obs;
	# /* ALTER TABLE obs ADD CONSTRAINT `patient_obs` FOREIGN KEY patient_obs (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE; */
	select 'Changing program foreign keys to ON CASCADE' as 'Action' from dual;
	ALTER TABLE patient_program DROP FOREIGN KEY `patient_in_program`;
	ALTER TABLE patient_program ADD CONSTRAINT `patient_in_program` FOREIGN KEY `patient_in_program` (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE;
	select 'Changing cohort member foreign keys to ON CASCADE' as 'Action' from dual;
	ALTER TABLE cohort_member DROP FOREIGN KEY `member_patient`;
	ALTER TABLE cohort_member ADD CONSTRAINT `member_patient` FOREIGN KEY `member_patient` (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE;
	select 'Changing order patient foreign keys to ON CASCADE' as 'Action' from dual;
	ALTER TABLE `orders` DROP FOREIGN KEY `order_for_patient`;
	ALTER TABLE `orders` ADD CONSTRAINT `order_for_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON UPDATE CASCADE;
	
	# /* I want to leave all of the user ids the same, so overlapping patient ids need to change to the person id of the  */
	# /* user_id that would be overlapped	 */
	select 'Changing relationship.person_id' as 'Action' from dual;
	UPDATE
		relationship r,
		person p
	SET
		r.person_id = (select person_id from person where user_id = p.patient_id)
	WHERE
		r.person_id = p.person_id
		AND
		EXISTS (select * from users u where u.user_id = p.patient_id);
	
	select 'Changing relationship.relative_id' as 'Action' from dual;
	UPDATE
		relationship r,
		person p
	SET
		r.relative_id = (select person_id from person where user_id = p.patient_id)
	WHERE
		r.relative_id = p.person_id
		AND
		EXISTS (select * from users u where u.user_id = p.patient_id);
	
	/*	
		1)	Create temp table with overlapping patient_ids and their new person_id
			1a) The new person_id is the patient_id unless patient_id is a user_id already
			1b) If it is a user_id, the new person_id is simply added to the end of the list
		2)	Update patient table to new person id (will cascade to all other patient tables)
		3)	Update obs table with new (because it will not cascade)
		4)	update all person_ids in relationship table that correspond with patient_ids 
			that overlap with user_ids, change to the new person_id

	*/
	select @max_patient_id := max(patient_id) from patient;
	select 'Create temporary table holding overlapping patient_ids and new person_ids' as 'Action' from dual;
	DROP TABLE IF EXISTS temp_overlapping_patient_ids;
	CREATE TABLE 
		temp_overlapping_patient_ids
		(
		 patient_id int(11),
		 old_person_id int(11),
		 new_person_id int(11)
		);
	INSERT INTO
		temp_overlapping_patient_ids
	(patient_id, old_person_id, new_person_id)
		SELECT
			patient_id, person_id, @max_patient_id + patient_id
		FROM
			person
		WHERE
			EXISTS (select * from users u where u.user_id = patient_id);
	
	# /* change the patient ids that overlap user ids to that user id's person id */
	select 'Advancing patient.patient_ids that overlap with user_ids' as 'Action' from dual;
	UPDATE 
		patient p,
		temp_overlapping_patient_ids t
	SET
		p.patient_id = t.new_person_id
	WHERE
		p.patient_id = t.patient_id;
	
	# /* update the obs table because we didn't make a 'cascade' foreign key */
	select 'Advancing obs.patient_ids that overlap with user_ids' as 'Action' from dual;
	UPDATE
		obs o,
		temp_overlapping_patient_ids t
	SET
		o.patient_id = t.new_person_id
	WHERE
		o.patient_id = t.patient_id;
	
	/* Disable the foreign key checks while we update the relationship persons */
	SET FOREIGN_KEY_CHECKS=0;
	select 'Matching relationship.person_id with new person_id (which is the current patient_id or user_id)' as 'Action' from dual;
	UPDATE
		relationship r join
		person p on r.person_id = p.person_id left join
		temp_overlapping_patient_ids t on t.old_person_id = r.person_id
	SET
		r.person_id = IFNULL(t.new_person_id, IFNULL(p.patient_id, p.user_id));
			
	select 'Matching relationship.relative_id with new person_id (which is the current patient_id or user_id)' as 'Action' from dual;
	UPDATE
		relationship r join
		person p on r.relative_id = p.person_id left join
		temp_overlapping_patient_ids t on t.old_person_id = r.relative_id
	SET
		r.relative_id = IFNULL(t.new_person_id, IFNULL(p.patient_id, p.user_id));
	
	DROP TABLE IF EXISTS temp_overlapping_patient_ids;
	SET FOREIGN_KEY_CHECKS=1;
	
	# /* Remake the person table */
	select 'Remaking the person table' as 'Action' from dual;
	ALTER TABLE relationship DROP FOREIGN KEY `related_person`;
	ALTER TABLE relationship DROP FOREIGN KEY `related_relative`;
	DROP TABLE `person`;
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
		PRIMARY KEY	(`person_id`),
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
	) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='';
	
	# /* copy patient data to the person table */
	select 'Copy all patients into the person table' as 'Action' from dual;
	INSERT INTO `person` 
		(person_id, 
			gender, birthdate, birthdate_estimated, 
			dead, death_date, cause_of_death, 
			creator, date_created, 
			changed_by, date_changed, 
			voided, voided_by, date_voided, void_reason)
		SELECT 
			patient_id,
				gender, birthdate, birthdate_estimated, 
				dead, death_date, cause_of_death, 
				creator, date_created, 
				changed_by, date_changed, 
				voided, voided_by, date_voided, void_reason
		FROM 
			`patient`;
	
		
	# /* remove the now unecessary patient table columns */
	select 'Remove some of the deprecated patient table columns' as 'Action' from dual;
	ALTER TABLE `patient` DROP INDEX birthdate;
	ALTER TABLE `patient` DROP FOREIGN KEY died_because;
	ALTER TABLE `patient` DROP INDEX died_because;
	ALTER TABLE `patient`
		DROP COLUMN gender,
		DROP COLUMN birthdate,
		DROP COLUMN birthdate_estimated,
		DROP COLUMN dead,
		DROP COLUMN death_date,
		DROP COLUMN cause_of_death;
	
	
	# /* change patient_name table to person_name */
	ALTER TABLE patient_name DROP FOREIGN KEY `name_for_patient`;
	ALTER TABLE patient_name RENAME TO person_name;
	ALTER TABLE person_name CHANGE COLUMN patient_name_id person_name_id int(11) auto_increment;
	ALTER TABLE person_name CHANGE COLUMN patient_id person_id int(11);
	ALTER TABLE person_name ADD CONSTRAINT `name for person` FOREIGN KEY `name_for_person` (`person_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE;
	
	
	# /* change patient_address table to person_address */
	ALTER TABLE patient_address DROP FOREIGN KEY `patient_addresses`;
	ALTER TABLE patient_address RENAME TO person_address;
	ALTER TABLE person_address CHANGE COLUMN patient_address_id person_address_id int(11) auto_increment;
	ALTER TABLE person_address CHANGE COLUMN patient_id person_id int(11);
	ALTER TABLE person_address ADD CONSTRAINT `address_for_person` FOREIGN KEY `address_for_person` (`person_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE;
	
	
	# /* copy user data to the person table */
	select 'Copy user data to the person table' as 'Action' from dual;
	INSERT INTO `person` 
		(person_id, 
			dead,
			creator, date_created, 
			changed_by, date_changed, 
			voided, voided_by, date_voided, void_reason)
		SELECT 
			user_id,
				0,
				creator, date_created, 
				changed_by, date_changed, 
				voided, voided_by, date_voided, void_reason
		FROM 
			`users`;
	INSERT INTO `person_name`
		(person_id,
			preferred, given_name, middle_name, family_name,
			creator, date_created, 
			changed_by, date_changed)
		SELECT
			user_id, 
			1, first_name, middle_name, last_name,
			creator, date_created,
			changed_by, date_changed
		FROM
			`users`;
	
	# /* remove the now unecessary users table columns */
	ALTER TABLE `users`
		DROP COLUMN first_name,
		DROP COLUMN middle_name,
		DROP COLUMN last_name;
	
	# /* add person.person_id as foreign key for patient.patient_id and user.user_id */
	select 'Adding person_id contraint to patient.patient_id and user.user_id' as 'Action' from dual;
	ALTER TABLE patient ADD CONSTRAINT `person_id_for_patient` FOREIGN KEY patient_id (`patient_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE;
	ALTER TABLE users ADD CONSTRAINT `person_id_for_user` FOREIGN KEY user_id (`user_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE;
	
	# /* restructuring relationship table */
	select 'Restructuring relationship table' as 'Action' from dual;
	ALTER TABLE relationship CHANGE COLUMN person_id person_a int(11) NOT NULL;
	ALTER TABLE relationship CHANGE COLUMN relative_id person_b int(11) NOT NULL;
	
	ALTER TABLE relationship ADD CONSTRAINT `person_a` FOREIGN KEY `person_a` (`person_a`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE;
	ALTER TABLE relationship ADD CONSTRAINT `person_b` FOREIGN KEY `person_b` (`person_b`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE;
	
	
	# /* restructuring relationship type table */
	select 'Restructuring relationship type table' as 'Action' from dual;
	ALTER TABLE relationship_type CHANGE COLUMN `relationship_type_id` `relationship_type_id` int(11) NOT NULL auto_increment;
	ALTER TABLE relationship_type CHANGE COLUMN `name` a_is_to_b varchar(50) NOT NULL;
	ALTER TABLE relationship_type ADD COLUMN b_is_to_a varchar(50) NOT NULL AFTER a_is_to_b;
	ALTER TABLE relationship_type ADD COLUMN `preferred` int(1) NOT NULL default '0' AFTER b_is_to_a;
	ALTER TABLE relationship_type ADD COLUMN `weight` int(11) NOT NULL default '0' AFTER preferred;
	
	
	# /* Creating b_is_to_a column values */
	select 'Creating b_is_to_a column values' as 'Action' from dual;
	UPDATE
		relationship_type
	SET
		a_is_to_b = 'Parent',
		b_is_to_a = 'Child'
	WHERE
		a_is_to_b in ('Mother', 'Father');
	
	UPDATE
		relationship_type
	SET
		b_is_to_a = CONCAT('Opposite of ', a_is_to_b)
	WHERE
		a_is_to_b <> 'Parent';
		
	# /* Add in some default relationships if they don't have them */
	IF (SELECT (count(*) < 1) FROM relationship_type WHERE a_is_to_b = 'Doctor' AND b_is_to_a = 'Patient') THEN
		INSERT INTO relationship_type
			(a_is_to_b, b_is_to_a, description, creator, date_created)
		VALUES
			('Doctor', 'Patient', 'Relationship from a primary care provider to the patient', 1, now());
	END IF;
	
	IF (SELECT (count(*) < 1) FROM relationship_type WHERE a_is_to_b = 'Sibling' AND b_is_to_a = 'Sibling') THEN
		INSERT INTO relationship_type
			(a_is_to_b, b_is_to_a, description, creator, date_created)
		VALUES
			('Sibling', 'Sibling', 'Relationship between brother/sister, brother/brother, and sister/sister', 1, now());
	END IF;
	
	IF (SELECT (count(*) < 1) FROM relationship_type WHERE a_is_to_b = 'Parent' AND b_is_to_a = 'Child') THEN
		INSERT INTO relationship_type
			(a_is_to_b, b_is_to_a, description, creator, date_created)
		VALUES
			('Parent', 'Child', 'Relationship from a mother/father to the child', 1, now());
	END IF;
	
	IF (SELECT (count(*) < 1) FROM relationship_type WHERE a_is_to_b = 'Aunt/Uncle' AND b_is_to_a = 'Niece/Nephew') THEN
		INSERT INTO relationship_type
			(a_is_to_b, b_is_to_a, description, creator, date_created)
		VALUES
			('Aunt/Uncle', 'Niece/Nephew', '', 1, now());
	END IF;
	
	
	# /* change obs.patient_id to obs.person_id */
	select 'Changing obs.patient_id to obs.person_id.' as 'Current Action' from dual;
	select 'This WILL take a _LONG TIME_ if you have a large number of observations (measured in hours)' as 'Note:' from dual;
	# /*ALTER TABLE obs DROP FOREIGN KEY patient_obs; */
	ALTER TABLE obs CHANGE COLUMN patient_id person_id int(11) NOT NULL;
	ALTER TABLE obs ADD CONSTRAINT `person_obs` FOREIGN KEY person_obs (`person_id`) REFERENCES `person` (`person_id`) ON UPDATE CASCADE;
	
	select 'Done changing obs.patient_id to obs.person_id.' as 'Current Action' from dual;
	
	# /* create the person attribute type table */
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
		PRIMARY KEY (`person_attribute_type_id`),
		KEY `name_of_attribute` (`name`),
		KEY `type_creator` (`creator`),
		KEY `attribute_type_changer` (`changed_by`),
		KEY `attribute_is_searchable` (`searchable`),
		CONSTRAINT `attribute_type_changer` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
		CONSTRAINT `attribute_type_creator` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='';
	
	
	# /* create the person attribute table */
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
		PRIMARY KEY (`person_attribute_id`),
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
	) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='';
	
	
	# /* create person attribute types for some of the current patient columns */
	select 'Copying patient columns into person_attribute' as 'Action' from dual;
	INSERT INTO `person_attribute_type` (`name`, description, format, creator, date_created) VALUES ('Race', 'Group of persons related by common descent or heredity', 'java.lang.String', 1, now());
	INSERT INTO `person_attribute_type` (`name`, description, format, creator, date_created) VALUES ('Birthplace', 'Location of persons birth', 'java.lang.String', 1, now());
	INSERT INTO `person_attribute_type` (`name`, description, format, creator, date_created) VALUES ('Citizenship', 'Country of which this person is a member', 'java.lang.String', 1, now());
	INSERT INTO `person_attribute_type` (`name`, description, format, creator, date_created) VALUES ('Mother\'s Name', 'First or last name of this person\'s mother', 'java.lang.String', 1, now());
	INSERT INTO `person_attribute_type` (`name`, description, format, creator, date_created) VALUES ('Civil Status', 'Marriage status of this person', 'org.openmrs.Concept', 1, now());
	INSERT INTO `person_attribute_type` (`name`, description, format, creator, date_created) VALUES ('Health District', 'District/region in which this patient\' home health center resides', 'java.lang.String', 1, now());
	INSERT INTO `person_attribute_type` (`name`, description, format, creator, date_created) VALUES ('Health Center', 'Specific Location of this person\'s home health center.', 'org.openmrs.Location', 1, now());
	
	
	# /* copy some patient columns to the person_attribute table */
	select 'Dropping race attribute' as 'Action' from dual;
	INSERT INTO `person_attribute`
		(person_id, `value`, person_attribute_type_id,
			creator, date_created)
		SELECT patient_id, race, (select person_attribute_type_id from person_attribute_type where `name` = 'Race'),
				1, now()
		FROM
			`patient`
		WHERE
			race is not null;
	ALTER TABLE `patient` DROP COLUMN race;
	select 'Dropping birthplace attribute' as 'Action' from dual;
	INSERT INTO `person_attribute`
		(person_id, `value`, person_attribute_type_id,
			creator, date_created)
		SELECT patient_id, birthplace, (select person_attribute_type_id from person_attribute_type where `name` = 'Birthplace'),
				1, now()
		FROM
			`patient`
		WHERE
			birthplace is not null;
	ALTER TABLE `patient` DROP COLUMN birthplace;
	select 'Dropping citizenship attribute' as 'Action' from dual;
	INSERT INTO `person_attribute`
		(person_id, `value`, person_attribute_type_id,
			creator, date_created)
		SELECT patient_id, citizenship, (select person_attribute_type_id from person_attribute_type where `name` = 'Citizenship'),
				1, now()
		FROM
			`patient`
		WHERE
			citizenship is not null;
	ALTER TABLE `patient` DROP COLUMN citizenship;
	select 'Dropping mothers name attribute' as 'Action' from dual;
	INSERT INTO `person_attribute`
		(person_id, `value`, person_attribute_type_id,
			creator, date_created)
		SELECT patient_id, mothers_name, (select person_attribute_type_id from person_attribute_type where `name` = 'Mother\'s Name'),
				1, now()
		FROM
			`patient`
		WHERE
			mothers_name is not null;
	ALTER TABLE `patient` DROP COLUMN mothers_name;
	select 'Dropping civil status attribute' as 'Action' from dual;
	INSERT INTO `person_attribute`
		(person_id, `value`, person_attribute_type_id,
			creator, date_created)
		SELECT patient_id, civil_status, (select person_attribute_type_id from person_attribute_type where `name` = 'Civil Status'),
				1, now()
		FROM
			`patient`
		WHERE
			civil_status is not null;
	ALTER TABLE `patient` DROP COLUMN civil_status;
	select 'Dropping health district attribute' as 'Action' from dual;
	INSERT INTO `person_attribute`
		(person_id, `value`, person_attribute_type_id,
			creator, date_created)
		SELECT patient_id, health_district, (select person_attribute_type_id from person_attribute_type where `name` = 'Health District'),
				1, now()
		FROM
			`patient`
		WHERE
			health_district is not null;
	ALTER TABLE `patient` DROP COLUMN health_district;
	select 'Dropping health center attribute' as 'Action' from dual;
	INSERT INTO `person_attribute`
		(person_id, `value`, person_attribute_type_id,
			creator, date_created)
		SELECT patient_id, health_center, (select person_attribute_type_id from person_attribute_type where `name` = 'Health Center'),
				1, now()
		FROM
			`patient`
		WHERE
			health_center is not null;
	# /*ALTER TABLE patient DROP FOREIGN KEY `health_center_location`; */
	ALTER TABLE `patient` DROP COLUMN health_center;
	
	
	# /* Modify the global properties to match current patient/user attribute setup */
	select 'Modifying the global properties table for patient.displayAttributeTypes' as 'Action' from dual;
	SET @attr1 = '';
	select @attr1 := ',Mother\'s Name' from global_property where property = 'use_patient_attribute.mothersName' and property_value = 'true';
	SET @attr2 = '';
	select @attr2 := ',Health Center' from global_property where property = 'use_patient_attribute.healthCenter' and property_value = 'true';
	INSERT INTO 
		`global_property`
		(property, property_value)
	VALUES (
		'patient.displayAttributeTypes', 
		CONCAT('Birthplace', CONCAT(@attr1, @attr2))
	);
	
	select 'Modifying the global properties table for address layouts' as 'Action' from dual;
	SET @attr3 = '';
	select @attr3 := property_value FROM `global_property` WHERE property = 'address.format';
	IF (SELECT LENGTH(property_value) > 0 FROM `global_property` WHERE property = 'address.format') THEN
		DELETE FROM `global_property` WHERE property = 'layout.address.format';
		INSERT INTO `global_property` (property, property_value) VALUES ('layout.address.format', @attr3);
		DELETE FROM `global_property` WHERE property = 'address.format';
	END IF;

	select 'Modifying the form fields for person' as 'Action' from dual;
	UPDATE
		field
	SET
		default_value = REPLACE(default_value, 'patient.getPatientName()', 'patient')
	WHERE
		default_value like '%patient.getPatientName()%';
		
	UPDATE
		field
	SET
		default_value = REPLACE(default_value, 'patient.getPatientAddress()', 'patient.getPersonAddress()')
	WHERE
		default_value like '%patient.getPatientAddress()%';
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	
	select 'Modifying the data exports from patient to person' as 'Action' from dual;
	UPDATE
		report_object
	SET
		xml_data = REPLACE(xml_data, 'getPatientAttr(&apos;PatientName&apos;', 'getPatientAttr(&apos;PersonName&apos;');
	UPDATE
		report_object
	SET
		xml_data = REPLACE(xml_data, 'getPatientAttr(&apos;PatientAddress&apos;', 'getPatientAttr(&apos;PersonAddress&apos;');
	UPDATE
		report_object
	SET
		xml_data = REPLACE(xml_data, '(&apos;Patient&apos;, &apos;gender&apos;)', '(&apos;Person&apos;, &apos;gender&apos;)');
	UPDATE
		report_object
	SET
		xml_data = REPLACE(xml_data, '(&apos;Patient&apos;, &apos;birthdate&apos;)', '(&apos;Person&apos;, &apos;birthdate&apos;)');
	UPDATE
		report_object
	SET
		xml_data = REPLACE(xml_data, '(&apos;Patient&apos;, &apos;birthdateEstimated&apos;)', '(&apos;Person&apos;, &apos;birthdateEstimated&apos;)');
	UPDATE
		report_object
	SET
		xml_data = REPLACE(xml_data, '(&apos;Patient&apos;, &apos;causeOfDeath&apos;)', '(&apos;Person&apos;, &apos;causeOfDeath&apos;)');
	UPDATE
		report_object
	SET
		xml_data = REPLACE(xml_data, '(&apos;Patient&apos;, &apos;deathDate&apos;)', '(&apos;Person&apos;, &apos;deathDate&apos;)');
	UPDATE
		report_object
	SET
		xml_data = REPLACE(xml_data, 'fn.getPatientAttr(&apos;Patient&apos;, &apos;healthCenter&apos;).getName()', 'fn.getPersonAttribute(&apos;Health Center&apos;, &apos;Location&apos;, &apos;locationId&apos;, &apos;name&apos;, false)');
	UPDATE
		report_object
	SET
		xml_data = REPLACE(xml_data, 'fn.getPatientAttribute(&apos;Patient&apos;, &apos;race&apos;)', 'fn.getPersonAttribute(&apos;Race&apos;)');
		
	
	select CONCAT('Done updating to person at: ', now()) as 'Timestamp' from dual;
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.57');


#--------------------------------------
# OpenMRS Datamodel version 1.0.58
# Ben Wolfe            Apr 26 2007
# This will ALWAYS drop/create the 'update_user_password'
# <strike>Adding</strike>Removing the user password change stored procedure
#--------------------------------------

DROP PROCEDURE IF EXISTS update_user_password;

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
 	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
		SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.0.58');

#--------------------------------------
# OpenMRS Datamodel version 1.1.0
# Ben Wolfe            Apr 26 2007
# <strike>Adding</strike>Removing patient/user create stub procedures
#--------------------------------------


DROP PROCEDURE IF EXISTS insert_patient_stub;
DROP PROCEDURE IF EXISTS insert_user_stub;

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
 	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
		SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//
delimiter ;
call diff_procedure('1.1.0');


#-----------------------------------
# Clean up - Keep this section at the very bottom of diff script
#-----------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;