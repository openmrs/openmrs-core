#--------------------------------------
# USE:
#  The diffs are ordered by datamodel version number.
#--------------------------------------

DROP PROCEDURE IF EXISTS update_user_password;
DROP PROCEDURE IF EXISTS insert_patient_stub;
DROP PROCEDURE IF EXISTS insert_user_stub;


#----------------------------------------
# OpenMRS Datamodel version 1.1.10
# Ben Wolfe                 May 31st 2007
# Adding township_division, region,  and 
# subregion attributes to patient_address 
# and location tables
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	ALTER TABLE `person_address` ADD COLUMN `region` varchar(50) default NULL;
	ALTER TABLE `person_address` ADD COLUMN `subregion` varchar(50) default NULL;
	ALTER TABLE `person_address` ADD COLUMN `township_division` varchar(50) default NULL;
	
	ALTER TABLE `location` ADD COLUMN `region` varchar(50) default NULL;
	ALTER TABLE `location` ADD COLUMN `subregion` varchar(50) default NULL;
	ALTER TABLE `location` ADD COLUMN `township_division` varchar(50) default NULL;
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.1.10');

#----------------------------------------
# OpenMRS Datamodel version 1.1.11
# Ben Wolfe                 Dec 21st 2007
# Removing the unneeded auto increment values
# on patient_id and user_id.
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	ALTER TABLE `patient` MODIFY COLUMN `patient_id` int(11) NOT NULL;
	ALTER TABLE `users` MODIFY COLUMN `user_id` int(11) NOT NULL;
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.1.11');


#----------------------------------------
# OpenMRS Datamodel version 1.1.12
# Ben Wolfe                 Dec 27th 2007
# Adding report_schema_xml table
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	CREATE TABLE `report_schema_xml` (
	  `report_schema_id` int(11) NOT NULL auto_increment,
	  `name` varchar(255) NOT NULL,
	  `description` text NOT NULL,
	  `xml_data` text NOT NULL,
	  PRIMARY KEY  (`report_schema_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.1.12');

#---------------------------------------
# Update OpenMRS to 1.2.0
#---------------------------------------
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
call diff_procedure('1.2.0');


#-----------------------------------
# Clean up - Keep this section at the very bottom of diff script
#-----------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;
