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
 

