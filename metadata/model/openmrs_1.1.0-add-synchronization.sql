#--------------------------------------
# USE:
# part of sync model updates
#--------------------------------------

#--------------------------------------
# Maros Cunderlik 17 Aug 2007 1:00PM
# TODO: merge into xxxx-latest-mysqldiff.sql.
#--------------------------------------

DROP PROCEDURE IF EXISTS sync_setup_procedure;

delimiter //

CREATE PROCEDURE sync_setup_procedure()
 BEGIN

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
	('ConceptSynonym', 'DICTIONARY', 1, 1),
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

  
	 # Create/fill-in server guid global property
	 if (SELECT count(*) FROM global_property where property = 'synchronization.server_guid') > 0  then
	    if (SELECT count(*) FROM global_property
	               where property = 'synchronization.server_guid'
	               and (property_value is null or property_value = '') ) = 0  then
	       #already there do nothing
	       select 'server guid already assigned, no change made' from dual;
	    else
	       #row there but empty, assign new
	       update global_property set property_value = uuid() where property = 'synchronization.server_guid';
	    end if;
	 else
	    insert into global_property (property, property_value, description)
	    values ('synchronization.server_guid',
	           uuid(),
	           'Globally unique server id used to identify a given data source in synchronization.');
	 end if;

	#fill out the default role for sync
	IF( SELECT count(*) > 0 FROM global_property WHERE property = 'synchronization.default_role' ) THEN
		UPDATE global_property SET property_value='System Developer' where property='synchronization.default_role';
	ELSE
		INSERT INTO global_property (property, property_value, description) values
		('synchronization.default_role', 'System Developer', 'Server role for the synchronization scheduled task login.');	
	END IF;

	# -------------------------------------------------------------------------------
	# Add primary keys to dependent concept tables (concept_name, concept_synonym) 
	# -------------------------------------------------------------------------------
	ALTER TABLE `concept_name` ADD COLUMN `concept_name_id` int(11) UNIQUE KEY NOT NULL AUTO_INCREMENT FIRST;
	ALTER TABLE `concept_name` ADD INDEX (`concept_id`);
	ALTER TABLE `concept_name` DROP PRIMARY KEY, ADD PRIMARY KEY (`concept_name_id`);
	ALTER TABLE `concept_synonym` ADD COLUMN `concept_synonym_id` int(11) UNIQUE KEY NOT NULL AUTO_INCREMENT FIRST;
	ALTER TABLE `concept_synonym` ADD INDEX (`concept_id`);
	ALTER TABLE `concept_synonym` DROP PRIMARY KEY, ADD PRIMARY KEY (`concept_synonym_id`);
	
 END;
//
delimiter ;
select 'Executing sync_setup_procedure..' as ' ';
call sync_setup_procedure();
select 'Sync_setup_procedure completed.' as '';
drop procedure sync_setup_procedure;