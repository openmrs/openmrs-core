#--------------------------------------
# USE:
# Contains ddl sql diffs for data synchronization feature.
# The diffs are ordered by datamodel version number.
# The update scheme is modeled after update-to-latest-db.mysqldiff.sql file.
#--------------------------------------


#----------------------------------------
# synchronization  version 1.0.0
# Initial setup
#----------------------------------------

DROP PROCEDURE IF EXISTS sync_diff_procedure;

delimiter //

CREATE PROCEDURE sync_diff_procedure (IN new_sync_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_sync_version, '.', '0') FROM global_property WHERE property = 'synchronization.version') THEN
	SELECT CONCAT('Installing initial synchronization version: ', new_sync_version) AS 'Synchronization Datamodel Update:' FROM dual;
	SELECT CONCAT('This is initial synchronization setup. All synchronization data will reloaded. ', new_sync_version) AS 'Synchronization Datamodel Update:' FROM dual;

	#Drop tables with FK relationships first
	DROP TABLE IF EXISTS synchronization_server_class;
	DROP TABLE IF EXISTS synchronization_server_record;

	#synchronization_journal
	DROP TABLE IF EXISTS synchronization_journal;
	CREATE TABLE `synchronization_journal` (
	  `record_id` int(11) NOT NULL auto_increment,
	  `guid` char(36) NOT NULL,
	  `creator` char(36) default NULL,
	  `database_version` char(8) default NULL,
	  `timestamp` datetime default NULL,
	  `retry_count` int(11) default NULL,
	  `state` varchar(20) default NULL,
	  `payload` longtext,
	  `contained_classes` varchar(1000) default '',
	  `original_guid` varchar(36) NOT NULL,	  
	  PRIMARY KEY  (`record_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	#synchronization_import
	DROP TABLE IF EXISTS synchronization_import;
	CREATE TABLE `synchronization_import` (
	  `record_id` int(11) NOT NULL auto_increment,
	  `guid` char(36) NOT NULL,
	  `creator` char(36) default NULL,
	  `database_version` char(8) default NULL,
	  `timestamp` datetime default NULL,
	  `retry_count` int(11) default NULL,
	  `state` varchar(20) default NULL,
	  `payload` longtext,
	  `error_message` varchar(255) default NULL,
	  PRIMARY KEY  (`record_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	#synchronization_server
	DROP TABLE IF EXISTS synchronization_server;
	CREATE TABLE `synchronization_server` (
	  `server_id` int(11) NOT NULL auto_increment,
	  `nickname` varchar(255) default NULL,
	  `address` varchar(255) NOT NULL,
	  `server_type` varchar(20) NOT NULL,
	  `username` varchar(255) default NULL,
	  `password` varchar(255) default NULL,
	  `guid` char(36) default NULL,
	  `last_sync` datetime default NULL,
	  `disabled` tinyint(1) NOT NULL,
	  `child_username` varchar(50) default '',
	  PRIMARY KEY  (`server_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	#synchronization_class
	DROP TABLE IF EXISTS synchronization_class;
	CREATE TABLE `synchronization_class` (
	  `class_id` int(11) NOT NULL auto_increment,
	  `name` varchar(255) NOT NULL,
	  `type` varchar(255) NOT NULL,
	  `default_to` tinyint(1) NOT NULL,
	  `default_from` tinyint(1) NOT NULL,
	  PRIMARY KEY  (`class_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;	
	INSERT INTO `synchronization_class` (`name`,`type`,`default_to`,`default_from`) values
	('Cohort', 'PATIENT', 1, 1),
	('ComplexObs', 'PATIENT', 1, 1),
	('Concept', 'DICTIONARY', 1, 1),
	('ConceptAnswer', 'DICTIONARY', 1, 1),
	('ConceptClass', 'DICTIONARY', 1, 1),
	('ConceptDatatype', 'DICTIONARY', 1, 1),
	('ConceptDerived', 'DICTIONARY', 1, 1),
	('ConceptMap', 'DICTIONARY', 1, 1),
	('ConceptName', 'DICTIONARY', 1, 1),
	('ConceptNumeric', 'DICTIONARY', 1, 1),
	('ConceptProposal', 'DICTIONARY', 1, 1),
	('ConceptSet', 'DICTIONARY', 1, 1),
	('ConceptSetDerived', 'DICTIONARY', 1, 1),
	('ConceptSource', 'DICTIONARY', 1, 1),
	('ConceptStateConversion', 'PATIENT', 1, 1),
	('ConceptWord', 'DICTIONARY', 1, 1),
	('Drug', 'DICTIONARY', 1, 1),
	('DrugIngredient', 'DICTIONARY', 1, 1),
	('DrugOrder', 'PATIENT', 1, 1),
	('Encounter', 'PATIENT', 1, 1),
	('EncounterType', 'PATIENT', 1, 1),
	('Field', 'FORM', 1, 1),
	('FieldAnswer', 'FORM', 1, 1),
	('FieldType', 'FORM', 1, 1),
	('Form', 'FORM', 1, 1),
	('FormField', 'FORM', 1, 1),
	('GlobalProperty', 'MISC', 0, 0),
	('Location', 'PATIENT', 1, 1),
	('LoginCredential', 'REQUIRED', 1, 1),
	('MimeType', 'DICTIONARY', 1, 1),
	('Obs', 'PATIENT', 1, 1),
	('Order', 'PATIENT', 1, 1),
	('OrderType', 'PATIENT', 1, 1),
	('Patient', 'PATIENT', 1, 1),
	('PatientIdentifier', 'PATIENT', 1, 1),
	('PatientIdentifierType', 'PATIENT', 1, 1),
	('PatientProgram', 'PATIENT', 1, 1),
	('PatientState', 'PATIENT', 1, 1),
	('Person', 'REQUIRED', 1, 1),
	('PersonAddress', 'PATIENT', 1, 1),
	('PersonAttribute', 'PATIENT', 1, 1),
	('PersonAttributeType', 'PATIENT', 1, 1),
	('PersonName', 'PATIENT', 1, 1),
	('Privilege', 'REQUIRED', 1, 1),
	('Program', 'PATIENT', 1, 1),
	('ProgramWorkflow', 'PATIENT', 1, 1),
	('ProgramWorkflowState', 'PATIENT', 1, 1),
	('Relationship', 'PATIENT', 1, 1),
	('RelationshipType', 'PATIENT', 1, 1),
	('Role', 'REQUIRED', 1, 1),
	('Tribe', 'PATIENT', 1, 1),
	('User', 'REQUIRED', 1, 1);

	#synchronization_server_class
	CREATE TABLE `synchronization_server_class` (
		`server_class_id` int(11) NOT NULL auto_increment,
		`class_id` int(11) NOT NULL,
		`server_id` int(11) NOT NULL,
		`send_to` tinyint(1) NOT NULL,
		`receive_from` tinyint(1) NOT NULL,
		PRIMARY KEY  (`server_class_id`),
		KEY `server_class_class` (`class_id`),
		KEY `server_class_server` (`server_id`),
		CONSTRAINT `server_class_class` FOREIGN KEY (`class_id`) REFERENCES `synchronization_class` (`class_id`),
		CONSTRAINT `server_class_server` FOREIGN KEY (`server_id`) REFERENCES `synchronization_server` (`server_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	#synchronization_server_record
	CREATE TABLE `synchronization_server_record` (
		`server_record_id` int(11) NOT NULL auto_increment,
		`server_id` int(11) NOT NULL,
		`record_id` int(11) NOT NULL,
		`state` varchar(20) default NULL,
		`retry_count` int(11) default NULL,
		PRIMARY KEY  (`server_record_id`),
		KEY `server_record_server` (`server_id`),
		KEY `server_record_record` (`record_id`),
		CONSTRAINT `server_record_server` FOREIGN KEY (`server_id`) REFERENCES `synchronization_server` (`server_id`),
		CONSTRAINT `server_record_record` FOREIGN KEY (`record_id`) REFERENCES `synchronization_journal` (`record_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	# -------------------------------------------------------------------------------
	# Setup common global properties: server guid, server name, admin email
	# sync role, compression settings, max retries, max records
	# -------------------------------------------------------------------------------
	
	# Create/fill-in server guid global property
	if (SELECT count(*) FROM global_property where property = 'synchronization.server_guid') > 0  then
		#row there, clear it out
		update global_property set property_value = '' where property = 'synchronization.server_guid';
	 else
	    insert into global_property (property, property_value, description, guid)
	    values ('synchronization.server_guid',
	           '',
	           'Globally unique server id used to identify a given data source in synchronization.', uuid());
	 end if;

	 # Create/fill-in server name global property
	 if (SELECT count(*) FROM global_property where property = 'synchronization.server_name') > 0  then
       select 'server name already assigned, no change made' from dual;
	 else
	    insert into global_property (property, property_value, description, guid)
	    values ('synchronization.server_name', '', 'Display name for this server, to distinguish it from other servers.', uuid());
	 end if;

	 # Create/fill-in admin email global property
	 if (SELECT count(*) FROM global_property where property = 'synchronization.admin_email') > 0  then
       select 'administrator email already assigned, no change made' from dual;
	 else
	    insert into global_property (property, property_value, description, guid)
	    values ('synchronization.admin_email', '', 'Email address for administrator responsible for this server.', uuid());
	 end if;

	#fill out the default role for sync
	IF( SELECT count(*) > 0 FROM global_property WHERE property = 'synchronization.default_role' ) THEN
		UPDATE global_property SET property_value='System Developer' where property='synchronization.default_role';
	ELSE
		INSERT INTO global_property (property, property_value, description, guid) values
		('synchronization.default_role', 'System Developer', 'Server role for the synchronization scheduled task login.', uuid());	
	END IF;

	#fill out the default compression settings for sync
	IF( SELECT count(*) > 0 FROM global_property WHERE property = 'synchronization.enable_compression' ) THEN
		UPDATE global_property SET property_value='true' where property='synchronization.enable_compression';
	ELSE
		INSERT INTO global_property (property, property_value, description, guid) values
		('synchronization.enable_compression', 'true', 'Whether or not OpenMRS should compress data that it sends (recommend that you set this to true).', uuid());	
	END IF;

	#fill out the default retry settings for sync (3)
	IF( SELECT count(*) > 0 FROM global_property WHERE property = 'synchronization.max_retry_count' ) THEN
		UPDATE global_property SET property_value='3' where property='synchronization.max_retry_count';
	ELSE
		INSERT INTO global_property (property, property_value, description, guid) values
		('synchronization.max_retry_count', '3', 'Number of times server attempts to apply received synchronization changes before giving up.', uuid());	
	END IF;
	
	#fill out the default max records settings for sync (500)
	IF( SELECT count(*) > 0 FROM global_property WHERE property = 'synchronization.max_records' ) THEN
		UPDATE global_property SET property_value='500' where property='synchronization.max_records';
	ELSE
		INSERT INTO global_property (property, property_value, description, guid) values
		('synchronization.max_records', '500', 'Maximum number of change records that will be send to the server at one time.', uuid());	
	END IF;

	# -------------------------------------------------------------------------------
	# Add primary keys to dependent concept tables (concept_name) 
	# -------------------------------------------------------------------------------
	ALTER TABLE `concept_name` ADD INDEX (`concept_id`);
	ALTER TABLE `concept_name` DROP PRIMARY KEY, ADD PRIMARY KEY (`concept_name_id`);
	ALTER TABLE `concept_set` ADD COLUMN `concept_set_id` int(11) UNIQUE KEY NOT NULL AUTO_INCREMENT FIRST;
	ALTER TABLE `concept_set` ADD INDEX (`concept_id`);
	ALTER TABLE `concept_set` DROP PRIMARY KEY, ADD PRIMARY KEY (`concept_set_id`);
	ALTER TABLE `concept_word` ADD COLUMN `concept_word_id` int(11) UNIQUE KEY NOT NULL AUTO_INCREMENT FIRST;
	ALTER TABLE `concept_word` ADD INDEX (`concept_id`);
	ALTER TABLE `concept_word` DROP PRIMARY KEY, ADD PRIMARY KEY (`concept_word_id`);

	# -------------------------------------------------------------------------------
	# Add user_guid column to users table to use with LoginCredential sync'ing
	# -------------------------------------------------------------------------------
	ALTER TABLE `users` ADD COLUMN `user_guid` char(36) DEFAULT NULL;

	
	#-----------------------------------------------------------------------------
	# Adding a primary key for the patient_identifier table
	#-----------------------------------------------------------------------------
	ALTER TABLE `patient_identifier` ADD COLUMN `patient_identifier_id` int(11) UNIQUE KEY NOT NULL AUTO_INCREMENT FIRST;
	ALTER TABLE `patient_identifier` ADD KEY `identifies_patient` (`patient_id`);
	ALTER TABLE `patient_identifier` DROP PRIMARY KEY, ADD PRIMARY KEY (`patient_identifier_id`);
	
	#create initial version number for sync if needed
	IF( SELECT count(*) > 0 FROM global_property WHERE property = 'synchronization.version' ) THEN
		UPDATE `global_property` SET property_value=new_sync_version WHERE property = 'synchronization.version';
	ELSE
		INSERT INTO global_property (property, property_value, description, guid) values
			('synchronization.version', new_sync_version, 'Synchronization version number. Do not edit, it is populated automatically during database upgrade process.', uuid());	
	END IF;
  END IF;
 END;
//

delimiter ;
call sync_diff_procedure('1.0.0');

#----------------------------------------
# OpenMRS Datamodel version 1.0.1
# removing user_guid from users table.
#----------------------------------------

DROP PROCEDURE IF EXISTS sync_diff_procedure;

delimiter //

CREATE PROCEDURE sync_diff_procedure (IN new_sync_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_sync_version, '.', '0') FROM global_property WHERE property = 'synchronization.version') THEN
	SELECT CONCAT('Updating synchronization to ', new_sync_version) AS 'Synchronization Datamodel Update:' FROM dual;

	# -------------------------------------------------------------------------------
	# user_guid was removed from Users table, not longer needed; make sure it is removed, if present
	# -------------------------------------------------------------------------------
	IF ( SELECT count(*) > 0 FROM INFORMATION_SCHEMA.Columns WHERE table_schema = schema() AND table_name = 'Users' AND column_name = 'user_guid' ) THEN
		ALTER TABLE `users` DROP COLUMN `user_guid`;
	END IF;

	# -------------------------------------------------------------------------------
	# add concept description & nametag from concept branch
	# -------------------------------------------------------------------------------
	INSERT INTO `synchronization_class` (`name`,`type`,`default_to`,`default_from`) values
	('ConceptDescription', 'DICTIONARY', 1, 1),
	('ConceptNameTag', 'DICTIONARY', 1, 1);
	INSERT INTO synchronization_server_class (class_id,server_id,send_to,receive_from)
  	select (select class_id from synchronization_class where name = 'ConceptDescription' ), server_id,1,1
  	from synchronization_server;
	INSERT INTO synchronization_server_class (class_id,server_id,send_to,receive_from)
  	select (select class_id from synchronization_class where name = 'ConceptNameTag' ), server_id,1,1
  	from synchronization_server;
	
	UPDATE `global_property` SET property_value=new_sync_version WHERE property = 'synchronization.version';	
	
	END IF;
 END;
//

delimiter ;
call sync_diff_procedure('1.0.1');


#----------------------------------------
# OpenMRS Datamodel version 1.0.2
# removing guid from concept_word table.
#----------------------------------------

DROP PROCEDURE IF EXISTS sync_diff_procedure;

delimiter //

CREATE PROCEDURE sync_diff_procedure (IN new_sync_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_sync_version, '.', '0') FROM global_property WHERE property = 'synchronization.version') THEN
	SELECT CONCAT('Updating synchronization to ', new_sync_version) AS 'Synchronization Datamodel Update:' FROM dual;

	# ----------------------------------------------------------------------------------------
	# Remove guid from concept_word; there is no need for it; concept_word is local table only
	# ----------------------------------------------------------------------------------------
	IF ( SELECT count(*) > 0 FROM INFORMATION_SCHEMA.Columns WHERE table_schema = schema() AND table_name = 'concept_word' AND column_name = 'guid' ) THEN
		ALTER TABLE `concept_word` DROP COLUMN `guid`;
		DELETE ssc, sc FROM synchronization_server_class ssc inner join synchronization_class sc
		where ssc.class_id = sc.class_id
		and sc.name = 'ConceptWord';
	END IF;
	
	UPDATE `global_property` SET property_value=new_sync_version WHERE property = 'synchronization.version';	
	
	END IF;
 END;
//

delimiter ;
call sync_diff_procedure('1.0.2');


#-----------------------------------
# Clean up - Keep this section at the very bottom of diff script
#-----------------------------------
DROP PROCEDURE IF EXISTS sync_diff_procedure;
