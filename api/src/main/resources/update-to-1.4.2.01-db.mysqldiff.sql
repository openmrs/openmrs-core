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



#---------------------------------------------------------------------------------
#--   OpenMRS 1.2           ------------------------------------------------------
#---------------------------------------------------------------------------------



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

DROP PROCEDURE IF EXISTS diff_procedure;



#----------------------------------------
# OpenMRS Datamodel version 1.2.01
# Ben Wolfe                 Feb 20th 2008
# Adding obs grouping rows to obs table and foreign
# keying obs.obs_group_id to obs.obs_id
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	SELECT 'Adding index to obs_group_id. **This could take a while on large databases.**\n\n  Cancelling during this step and restarting later will not harm anything.' AS 'Current step (1/8):' FROM dual;
	ALTER TABLE `obs` ADD INDEX `obs_grouping_id` (`obs_group_id`);
	
	-- temporary concept id to use if we can't guess the right obs grouper concept
	SET @OTHER_CONCEPT_ID = (SELECT `concept_id` FROM `concept_name` where name = 'MEDICAL RECORD OBSERVATIONS' LIMIT 1);
	
	-- This creates rows in obs for parent obs groupers. It temporarily gives the obs grouping rows an obs_group_id
	-- Tries to use the concept_set concept that the obs is in if there is only one.  If there is more than one, take from form_field parent 
	SELECT 'Creating rows in obs table representing obs groups from current data' as 'Current step (2/8):' FROM dual;
	INSERT INTO `obs` 
		(obs_group_id,
		 concept_id,
		 person_id,
		 obs_datetime,
		 encounter_id,
		 location_id,
		 creator,
		 date_created)
			SELECT 
				obs_group_id,
				COALESCE(
					(SELECT
						concept_set
					 FROM
						concept_set
					 WHERE
						concept_id = obs.concept_id
					 GROUP BY 
						concept_id
					 HAVING
						count(*) = 1
					),
					(SELECT
						COALESCE(f2.concept_id, @OTHER_CONCEPT_ID ) as 'concept_id'
					 FROM
						`field` f, `field` f2, `form_field` ff, `form_field` ff2, `form`, `encounter`
					 WHERE
						encounter.encounter_id = obs.encounter_id AND
						encounter.form_id = form.form_id AND
						form.form_id = ff.form_id AND
						ff.field_id = f.field_id AND
						f.concept_id = obs.concept_id AND
						ff.parent_form_field = ff2.form_field_id AND
						ff2.field_id = f2.field_id
					 LIMIT 1
					)
				) as 'concept_id',
				person_id,
				obs_datetime,
				encounter_id,
				location_id,
				creator, 
				date_created 
			FROM
				`obs`
			WHERE
				obs_group_id IS NOT null
			GROUP BY
				obs_group_id;
	
	-- This creates a temp table mapping from grouper obs to its obs_group_id
	SELECT 'Fixing obs_group_id values on all obs that were grouped' as 'Current step: (3/8)' FROM dual;
	DROP TABLE IF EXISTS `new_obs_groups_mapping`;
	CREATE TABLE `new_obs_groups_mapping` (
		`grouper_obs_id` int(11) NOT NULL,
		`obs_group_id` int(11) default NULL,
		PRIMARY KEY  (`grouper_obs_id`),
		KEY `grouper` (`grouper_obs_id`),
		KEY `the_group_id` (`obs_group_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
	-- This populates the previous table
	INSERT INTO `new_obs_groups_mapping`
		(grouper_obs_id, 
		 obs_group_id)
		SELECT
			max(obs_id) as grouper_obs_id,
			obs_group_id
		FROM 
			`obs` o2
		WHERE
			o2.obs_group_id IS NOT NULL
		GROUP BY
			o2.obs_group_id;
	
	-- This changes the obs_group_ids on the obs table to point at the obs_id of the grouper obs
	SELECT 'Applying temporary table values to obs table' as 'Current step: (3.5/8)' FROM dual;
	UPDATE 
		`obs` o left join new_obs_groups_mapping mapping on o.obs_group_id = mapping.obs_group_id
	SET
		o.obs_group_id = mapping.grouper_obs_id
	WHERE
		o.obs_group_id IS NOT NULL;

	-- Get rid of our temp table
	DROP TABLE IF EXISTS new_obs_groups_mapping;
	
	-- Remove the obs_group_id from the obs groupers
	SELECT 'Fixing newly added obs grouper rows that were temporarily given an obs_group_id' as 'Current step: (4/8)' FROM dual;
	UPDATE
		`obs` o
	SET
		obs_group_id = null
	WHERE
		obs_group_id IS NOT NULL AND
		obs_group_id = obs_id;
	
	-- Sanity check...we shouldn't really have any obs groupers with concept_id = MEDICAL RECORD OBSERVATIONS
	IF (SELECT COUNT(*)<>'0' FROM obs o WHERE concept_id = @OTHER_CONCEPT_ID AND EXISTS (SELECT * FROM obs o2 WHERE o2.obs_group_id = o.obs_id)) THEN
		SELECT 'These obs rows pertaining to obs_groups have the been given a generic concept_id. You should find and correct with their right grouping concept_id' AS '########## WARNING! #############' FROM DUAL;
		SELECT * FROM obs WHERE concept_id = @OTHER_CONCEPT_ID; 
	END IF;

	-- remove all bad obs grouping by setting obs_group_id to null for any obs in a solitary group and its grouper concept is not a set 
	SELECT 'Cleaning up the obs that think they are in an obs_group but really are not.' AS 'Current step (5/8):' FROM dual;
	DROP TABLE IF EXISTS `single_member_obs_groups`;
	CREATE TABLE `single_member_obs_groups` (
	  `obs_id` int(11) NOT NULL,
      `obs_group_id` int(11) NOT NULL,
	  PRIMARY KEY  (`obs_id`),
	  KEY `the_group_id` (`obs_group_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	INSERT INTO `single_member_obs_groups`
		(obs_id,
		 obs_group_id)
		SELECT
			obs.obs_id,
			obs.obs_group_id
		FROM
			obs join obs obsparent on obs.obs_group_id = obsparent.obs_id
			join concept c on obsparent.concept_id = c.concept_id
		WHERE
			c.is_set = 0
		GROUP BY
			obs.obs_group_id
		HAVING
			count(obs.obs_group_id) = 1;
	
	-- The actual ungrouping of the singluarly grouped obs rows
	UPDATE
		obs
	SET
		obs_group_id = null
	WHERE
		obs_id in (select obs_id from single_member_obs_groups);

	-- delete the obs grouper that was added for all those poser obs groups
	DELETE FROM
		obs
	WHERE
		obs_id IN (select distinct obs_group_id from `single_member_obs_groups`);

	-- cleanup the temp table.
	DROP TABLE IF EXISTS `single_member_obs_groups`;

	-- obs_group_id should be restricted to obs_ids
	SELECT 'Adding foreign key constraint from obs.obs_group_id to obs.obs_id' as 'Current step: (6/8)' FROM dual;
	ALTER TABLE `obs` ADD CONSTRAINT `obs_grouping_id` FOREIGN KEY (`obs_group_id`) REFERENCES `obs` (`obs_id`);
	
	-- set the voided bit on obs rows that are obs groupings if all children obs are voided
	SELECT 'Voiding those obs groupers that are in all-voided groups' as 'Current step: (7/8)' FROM dual;
	-- create a temp table to hold the obs_id of obs groupers that need to be voided
	DROP TABLE IF EXISTS `obs_groupers_needing_voided`;
	CREATE TABLE `obs_groupers_needing_voided` (
		`obs_id` int(11) NOT NULL,
		`voided_by` int(11) default NULL,
		`date_voided` datetime default NULL,
		`void_reason` varchar(255) default NULL,
	  PRIMARY KEY  (`obs_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	-- This populates the previous table
	INSERT INTO `obs_groupers_needing_voided`
		(obs_id, 
		 voided_by,
		 date_voided,
		 void_reason)
		SELECT
			obs_group_id,
			voided_by,
			date_voided,
			void_reason
		FROM 
			obs
		WHERE
			obs_group_id is NOT NULL AND			
			voided = 1
		GROUP BY 
			obs_group_id, voided
		HAVING
			count(*) = (SELECT count(*) FROM obs o2 WHERE o2.obs_group_id = obs.obs_group_id);
	-- doing the actual change in the obs table by voiding any rows that have all voided groupedobs
	UPDATE
		obs o1 join obs_groupers_needing_voided tovoid on o1.obs_id = tovoid.obs_id
	SET
		o1.voided = 1,
		o1.voided_by = tovoid.voided_by,
		o1.date_voided = tovoid.date_voided,
		o1.void_reason = tovoid.void_reason;
	DROP TABLE IF EXISTS `obs_groupers_needing_voided`;

	SELECT 'Updating database version global property' as 'Current step (8/8):' FROM dual;
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.2.01');

#----------------------------------------
# OpenMRS Datamodel version 1.2.02
# Burke Mamlin              April 6, 2008
# Adding concept_map.concept_id as FK back
# to concept (ticket #216)
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	ALTER TABLE `concept_map` ADD COLUMN `concept_id` int(11) NOT null default '0';
	ALTER TABLE `concept_map` ADD CONSTRAINT `map_for_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`);
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.2.02');

#----------------------------------------
# OpenMRS Datamodel version 1.2.03
# Justin Miranda          April 11, 2008
# Made scheduler task start time nullable 
# (ticket #667).
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	ALTER TABLE `scheduler_task_config` MODIFY COLUMN `start_time` DATETIME default NULL;
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.2.03');


#----------------------------------------
# OpenMRS Datamodel version 1.2.04
# Burke Mamlin          April 14, 2008
# Upgrade default from XSLT to 1.9.5 
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	-- Upgrade all instances of XSLT 1.9.4 to 1.9.5
	UPDATE `form`
	SET xslt = '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\r\n<!--\r\nOpenMRS FormEntry Form HL7 Translation\r\n\r\nThis XSLT is used to translate OpenMRS forms from XML into HL7 2.5 format\r\n\r\n@author Burke Mamlin, MD\r\n@author Ben Wolfe\r\n@version 1.9.5\r\n\r\n1.9.5 - allow for organizing sections under \"obs\" section\r\n1.9.4 - add support for message uid (as HL7 control id) and transform of patient.health_center to Discharge to Location (PV1-37)\r\n1.9.3 - fixed rounding error on timestamp (tenths of seconds getting rounded up, causing \"60\" seconds in some cases) \r\n1.9.2 - first generally useful version\r\n-->\r\n\r\n<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" xmlns:xdt=\"http://www.w3.org/2005/xpath-datatypes\">\r\n	<xsl:output method=\"text\" version=\"1.0\" encoding=\"UTF-8\" indent=\"no\"/>\r\n\r\n<xsl:variable name=\"SENDING-APPLICATION\">FORMENTRY</xsl:variable>\r\n<xsl:variable name=\"SENDING-FACILITY\">AMRS.ELD</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-APPLICATION\">HL7LISTENER</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-FACILITY\">AMRS.ELD</xsl:variable>\r\n<xsl:variable name=\"PATIENT-AUTHORITY\"></xsl:variable> <!-- leave blank for internal id, max 20 characters -->\r\n                                                       <!-- for now, must match patient_identifier_type.name -->\r\n<xsl:variable name=\"FORM-AUTHORITY\">AMRS.ELD.FORMID</xsl:variable> <!-- max 20 characters -->\r\n\r\n<xsl:template match=\"/\">\r\n	<xsl:apply-templates />\r\n</xsl:template>\r\n\r\n<!-- Form template -->\r\n<xsl:template match=\"form\">\r\n	<!-- MSH Header -->\r\n	<xsl:text>MSH|^~\\&amp;</xsl:text>   <!-- Message header, field separator, and encoding characters -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-3 Sending application -->\r\n	<xsl:value-of select=\"$SENDING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-4 Sending facility -->\r\n	<xsl:value-of select=\"$SENDING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-5 Receiving application -->\r\n	<xsl:value-of select=\"$RECEIVING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-6 Receiving facility -->\r\n	<xsl:value-of select=\"$RECEIVING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-7 Date/time message sent -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- MSH-8 Security -->\r\n	<xsl:text>|ORU^R01</xsl:text>       <!-- MSH-9 Message type ^ Event type (observation report unsolicited) -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-10 Message control ID -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"header/uid\">\r\n			<xsl:value-of select=\"header/uid\" />\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"patient/patient.patient_id\" />\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n			</xsl:call-template>\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|P</xsl:text>             <!-- MSH-11 Processing ID -->\r\n	<xsl:text>|2.5</xsl:text>           <!-- MSH-12 HL7 version -->\r\n	<xsl:text>|1</xsl:text>             <!-- MSH-13 Message sequence number -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-14 Continuation Pointer -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-15 Accept Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-16 Application Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-17 Country Code -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-18 Character Set -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-19 Principal Language of Message -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-20 Alternate Character Set Handling Scheme -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-21 Message Profile Identifier -->\r\n	<xsl:value-of select=\"@id\" />\r\n	<xsl:text>^</xsl:text>\r\n	<xsl:value-of select=\"$FORM-AUTHORITY\" />\r\n	<xsl:text>&#x000d;</xsl:text>\r\n\r\n	<!-- PID header -->\r\n	<xsl:text>PID</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-1 Set ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-2 (deprecated) Patient ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-3 Patient Identifier List -->\r\n	<xsl:call-template name=\"patient_id\">\r\n		<xsl:with-param name=\"pid\" select=\"patient/patient.patient_id\" />\r\n		<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n		<xsl:with-param name=\"type\" select=\"L\" />\r\n	</xsl:call-template>\r\n	<xsl:if test=\"patient/patient.previous_mrn and string-length(patient/patient.previous_mrn) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.previous_mrn\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"PRIOR\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<!-- Additional patient identifiers -->\r\n	<!-- This example is for an MTCT-PLUS identifier used in the AMPATH project in Kenya (skipped if not present) -->\r\n	<xsl:if test=\"patient/patient.mtctplus_id and string-length(patient/patient.mtctplus_id) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.mtctplus_id\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"MTCTPLUS\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PID-4 (deprecated) Alternate patient ID -->\r\n	<!-- PID-5 Patient name -->\r\n	<xsl:text>|</xsl:text>              <!-- Family name -->\r\n	<xsl:value-of select=\"patient/patient.family_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Given name -->\r\n	<xsl:value-of select=\"patient/patient.given_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Middle name -->\r\n	<xsl:value-of select=\"patient/patient.middle_name\" />\r\n	<xsl:text>|</xsl:text>              <!-- PID-6 Mother\'s maiden name -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-7 Date/Time of Birth -->\r\n	<xsl:value-of select=\"patient/patient.date_of_birth\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n	<!-- PV1 header -->\r\n	<xsl:text>PV1</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-1 Sub ID -->\r\n	<xsl:text>|O</xsl:text>             <!-- PV1-2 Patient class (O = outpatient) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-3 Patient location -->\r\n	<xsl:value-of select=\"encounter/encounter.location_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-4 Admission type (2 = return) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-5 Pre-Admin Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-6 Prior Patient Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-7 Attending Doctor -->\r\n	<xsl:value-of select=\"encounter/encounter.provider_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-8 Referring Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-9 Consulting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-10 Hospital Service -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-11 Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-12 Preadmin Test Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-13 Re-adminssion Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-14 Admit Source -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-15 Ambulatory Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-16 VIP Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-17 Admitting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-18 Patient Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-19 Visit Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-20 Financial Class -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-21 Charge Price Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-22 Courtesy Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-23 Credit Rating -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-24 Contract Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-25 Contract Effective Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-26 Contract Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-27 Contract Period -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-28 Interest Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-29 Transfer to Bad Debt Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-30 Transfer to Bad Debt Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Agency Code -->\r\n  <xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Transfer Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-33 Bad Debt Recovery Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-34 Delete Account Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-35 Delete Account Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-36 Discharge Disposition -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-37 Discharge To Location -->\r\n	<xsl:if test=\"patient/patient.health_center\">\r\n		<xsl:value-of select=\"replace(patient/patient.health_center,\'\\^\',\'&amp;\')\" />\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-38 Diet Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-39 Servicing Facility -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-40 Bed Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-41 Account Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-42 Pending Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-43 Prior Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-44 Admit Date/Time -->\r\n	<xsl:call-template name=\"hl7Date\">\r\n		<xsl:with-param name=\"date\" select=\"encounter/encounter.encounter_datetime\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-45 Discharge Date/Time -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-46 Current Patient Balance -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-47 Total Charges -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-48 Total Adjustments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-49 Total Payments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-50 Alternate Visit ID -->\r\n	<xsl:text>|V</xsl:text>             <!-- PV1-51 Visit Indicator -->\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- We use encounter date as the timestamp for each observation -->\r\n	<xsl:variable name=\"encounterTimestamp\">\r\n		<xsl:call-template name=\"hl7Date\">\r\n			<xsl:with-param name=\"date\" select=\"encounter/encounter.encounter_datetime\" />\r\n		</xsl:call-template>\r\n	</xsl:variable>\r\n	\r\n	<!-- ORC Common Order Segment -->\r\n	<xsl:text>ORC</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|RE</xsl:text>            <!-- ORC-1 Order Control (RE = obs to follow) -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-2 Placer Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-3 Filler Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-4 Placer Group Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-5 Order Status -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-6 Response Flag -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-7 Quantity/Timing -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-8 Parent -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-9 Date/Time of Transaction -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"xs:dateTime(header/date_entered)\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- ORC-10 Entered By -->\r\n	<xsl:value-of select=\"header/enterer\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- Observation(s) -->\r\n	<!-- <xsl:variable name=\"obsList\" select=\"obs/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\" /> -->\r\n	<xsl:variable name=\"obsList\" select=\"obs/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]|obs/*[not(@openmrs_concept)]/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\" />\r\n	<xsl:variable name=\"obsListCount\" select=\"count($obsList)\" as=\"xs:integer\" />\r\n	<!-- Observation OBR -->\r\n	<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n	<xsl:text>1</xsl:text>\r\n	<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n	<xsl:value-of select=\"obs/@openmrs_concept\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- observation OBXs -->\r\n	<xsl:for-each select=\"$obsList\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"value\">\r\n				<xsl:call-template name=\"obsObx\">\r\n					<xsl:with-param name=\"setId\" select=\"position()\" />\r\n					<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n					<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n					<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n					<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n					<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n					<xsl:with-param name=\"value\" select=\"value\" />\r\n					<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n				<xsl:variable name=\"setId\" select=\"position()\" />\r\n				<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n						<xsl:with-param name=\"subId\" select=\"concat($setId,position())\" />\r\n						<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:for-each>\r\n			</xsl:otherwise>\r\n		</xsl:choose>\r\n	</xsl:for-each>\r\n	\r\n	<!-- Grouped observation(s) -->\r\n	<!-- <xsl:variable name=\"obsGroupList\" select=\"obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]\" /> -->\r\n	<xsl:variable name=\"obsGroupList\" select=\"obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]|obs/*[not(@openmrs_concept)]/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]\" />\r\n	<xsl:variable name=\"obsGroupListCount\" select=\"count($obsGroupList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$obsGroupList\">\r\n		<!-- Observation OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + position()\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n		\r\n		<!-- Generate OBXs -->\r\n		<xsl:for-each select=\"*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"subId\" select=\"1\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"concat(\'1.\',position())\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>\r\n	</xsl:for-each>\r\n\r\n	<!-- Problem list(s) -->\r\n	<xsl:variable name=\"problemList\" select=\"problem_list/*[value[text() != \'\']]\" />\r\n	<xsl:variable name=\"problemListCount\" select=\"count($problemList)\" as=\"xs:integer\" />\r\n	<xsl:if test=\"$problemList\">\r\n		<!-- Problem list OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"problem_list/@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n		<!-- Problem list OBXs -->\r\n		<xsl:for-each select=\"$problemList\">\r\n			<xsl:call-template name=\"obsObx\">\r\n				<xsl:with-param name=\"setId\" select=\"position()\" />\r\n				<xsl:with-param name=\"datatype\" select=\"\'CWE\'\" />\r\n				<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n				<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n				<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n				<xsl:with-param name=\"value\" select=\"value\" />\r\n				<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n			</xsl:call-template>		\r\n		</xsl:for-each>\r\n	</xsl:if>\r\n	\r\n	<!-- Orders -->\r\n	<xsl:variable name=\"orderList\" select=\"orders/*[*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]]\" />\r\n	<xsl:variable name=\"orderListCount\" select=\"count($orderList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$orderList\">\r\n		<!-- Order section OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + $problemListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n		<!-- Order OBXs -->\r\n		<xsl:for-each select=\"*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"position()\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>	\r\n	</xsl:for-each>\r\n	\r\n</xsl:template>\r\n\r\n<!-- Patient Identifier (CX) generator -->\r\n<xsl:template name=\"patient_id\">\r\n	<xsl:param name=\"pid\" />\r\n	<xsl:param name=\"auth\" />\r\n	<xsl:param name=\"type\" />\r\n	<xsl:value-of select=\"$pid\" />\r\n	<xsl:text>^</xsl:text>              <!-- Check digit -->\r\n	<xsl:text>^</xsl:text>              <!-- Check Digit Scheme -->\r\n	<xsl:text>^</xsl:text>              <!-- Assigning Authority -->\r\n	<xsl:value-of select=\"$auth\" />\r\n	<xsl:text>^</xsl:text>              <!-- Identifier Type -->\r\n	<xsl:value-of select=\"$type\" />\r\n</xsl:template>\r\n\r\n<!-- OBX Generator -->\r\n<xsl:template name=\"obsObx\">\r\n	<xsl:param name=\"setId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"subId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"datatype\" required=\"yes\" />\r\n	<xsl:param name=\"concept\" required=\"yes\" />\r\n	<xsl:param name=\"date\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"time\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"value\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"units\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"encounterTimestamp\" required=\"yes\" />\r\n	<xsl:text>OBX</xsl:text>                     <!-- Message type -->\r\n	<xsl:text>|</xsl:text>                       <!-- Set ID -->\r\n	<xsl:value-of select=\"$setId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Observation datatype -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:text>NM</xsl:text>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$datatype\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Concept (what was observed -->\r\n	<xsl:value-of select=\"$concept\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Sub-ID -->\r\n	<xsl:value-of select=\"$subId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Value -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'TS\'\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'DT\'\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'TM\'\">\r\n			<xsl:call-template name=\"hl7Time\">\r\n				<xsl:with-param name=\"time\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"$value = \'0\' or upper-case($value) = \'FALSE\'\">0</xsl:when>\r\n				<xsl:otherwise>1</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$value\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Units -->\r\n	<xsl:value-of select=\"$units\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Reference range -->\r\n	<xsl:text>|</xsl:text>                       <!-- Abnormal flags -->\r\n	<xsl:text>|</xsl:text>                       <!-- Probability -->\r\n	<xsl:text>|</xsl:text>                       <!-- Nature of abnormal test -->\r\n	<xsl:text>|</xsl:text>                       <!-- Observation result status -->\r\n	<xsl:text>|</xsl:text>                       <!-- Effective date -->\r\n	<xsl:text>|</xsl:text>                       <!-- User defined access checks -->\r\n	<xsl:text>|</xsl:text>                       <!-- Date time of observation -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$date and $time\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"dateTime($date,$time)\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$date\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$date\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$encounterTimestamp\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>&#x000d;</xsl:text>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted timestamp -->\r\n<xsl:template name=\"hl7Timestamp\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:value-of select=\"concat(year-from-dateTime($date),format-number(month-from-dateTime($date),\'00\'),format-number(day-from-dateTime($date),\'00\'),format-number(hours-from-dateTime($date),\'00\'),format-number(minutes-from-dateTime($date),\'00\'),format-number(floor(seconds-from-dateTime($date)),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted date -->\r\n<xsl:template name=\"hl7Date\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"contains(string($date),\'T\')\">\r\n				<xsl:call-template name=\"hl7Date\">\r\n					<xsl:with-param name=\"date\" select=\"xs:date(substring-before($date,\'T\'))\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n					<xsl:value-of select=\"concat(year-from-date($date),format-number(month-from-date($date),\'00\'),format-number(day-from-date($date),\'00\'))\" />\r\n			</xsl:otherwise>\r\n		</xsl:choose>				\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted time -->\r\n<xsl:template name=\"hl7Time\">\r\n	<xsl:param name=\"time\" />\r\n	<xsl:if test=\"$time != \'\'\">\r\n		<xsl:value-of select=\"concat(format-number(hours-from-time($time),\'00\'),format-number(minutes-from-time($time),\'00\'),format-number(floor(seconds-from-time($time)),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n</xsl:stylesheet>'
	WHERE xslt = '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\r\n<!--\r\nOpenMRS FormEntry Form HL7 Translation\r\n\r\nThis XSLT is used to translate OpenMRS forms from XML into HL7 2.5 format\r\n\r\n@author Burke Mamlin, MD\r\n@author Ben Wolfe\r\n@version 1.9.4\r\n\r\n1.9.4 - add support for message uid (as HL7 control id) and transform of patient.health_center to Discharge to Location (PV1-37)\r\n1.9.3 - fixed rounding error on timestamp (tenths of seconds getting rounded up, causing \"60\" seconds in some cases) \r\n1.9.2 - first generally useful version\r\n-->\r\n\r\n<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" xmlns:xdt=\"http://www.w3.org/2005/xpath-datatypes\">\r\n	<xsl:output method=\"text\" version=\"1.0\" encoding=\"UTF-8\" indent=\"no\"/>\r\n\r\n<xsl:variable name=\"SENDING-APPLICATION\">FORMENTRY</xsl:variable>\r\n<xsl:variable name=\"SENDING-FACILITY\">AMRS.ELD</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-APPLICATION\">HL7LISTENER</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-FACILITY\">AMRS.ELD</xsl:variable>\r\n<xsl:variable name=\"PATIENT-AUTHORITY\"></xsl:variable> <!-- leave blank for internal id, max 20 characters -->\r\n                                                       <!-- for now, must match patient_identifier_type.name -->\r\n<xsl:variable name=\"FORM-AUTHORITY\">AMRS.ELD.FORMID</xsl:variable> <!-- max 20 characters -->\r\n\r\n<xsl:template match=\"/\">\r\n	<xsl:apply-templates />\r\n</xsl:template>\r\n\r\n<!-- Form template -->\r\n<xsl:template match=\"form\">\r\n	<!-- MSH Header -->\r\n	<xsl:text>MSH|^~\\&amp;</xsl:text>   <!-- Message header, field separator, and encoding characters -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-3 Sending application -->\r\n	<xsl:value-of select=\"$SENDING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-4 Sending facility -->\r\n	<xsl:value-of select=\"$SENDING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-5 Receiving application -->\r\n	<xsl:value-of select=\"$RECEIVING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-6 Receiving facility -->\r\n	<xsl:value-of select=\"$RECEIVING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-7 Date/time message sent -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- MSH-8 Security -->\r\n	<xsl:text>|ORU^R01</xsl:text>       <!-- MSH-9 Message type ^ Event type (observation report unsolicited) -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-10 Message control ID -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"header/uid\">\r\n			<xsl:value-of select=\"header/uid\" />\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"patient/patient.patient_id\" />\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n			</xsl:call-template>\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|P</xsl:text>             <!-- MSH-11 Processing ID -->\r\n	<xsl:text>|2.5</xsl:text>           <!-- MSH-12 HL7 version -->\r\n	<xsl:text>|1</xsl:text>             <!-- MSH-13 Message sequence number -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-14 Continuation Pointer -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-15 Accept Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-16 Application Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-17 Country Code -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-18 Character Set -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-19 Principal Language of Message -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-20 Alternate Character Set Handling Scheme -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-21 Message Profile Identifier -->\r\n	<xsl:value-of select=\"@id\" />\r\n	<xsl:text>^</xsl:text>\r\n	<xsl:value-of select=\"$FORM-AUTHORITY\" />\r\n	<xsl:text>&#x000d;</xsl:text>\r\n\r\n	<!-- PID header -->\r\n	<xsl:text>PID</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-1 Set ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-2 (deprecated) Patient ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-3 Patient Identifier List -->\r\n	<xsl:call-template name=\"patient_id\">\r\n		<xsl:with-param name=\"pid\" select=\"patient/patient.patient_id\" />\r\n		<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n		<xsl:with-param name=\"type\" select=\"L\" />\r\n	</xsl:call-template>\r\n	<xsl:if test=\"patient/patient.previous_mrn and string-length(patient/patient.previous_mrn) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.previous_mrn\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"PRIOR\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<!-- Additional patient identifiers -->\r\n	<!-- This example is for an MTCT-PLUS identifier used in the AMPATH project in Kenya (skipped if not present) -->\r\n	<xsl:if test=\"patient/patient.mtctplus_id and string-length(patient/patient.mtctplus_id) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.mtctplus_id\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"MTCTPLUS\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PID-4 (deprecated) Alternate patient ID -->\r\n	<!-- PID-5 Patient name -->\r\n	<xsl:text>|</xsl:text>              <!-- Family name -->\r\n	<xsl:value-of select=\"patient/patient.family_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Given name -->\r\n	<xsl:value-of select=\"patient/patient.given_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Middle name -->\r\n	<xsl:value-of select=\"patient/patient.middle_name\" />\r\n	<xsl:text>|</xsl:text>              <!-- PID-6 Mother\'s maiden name -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-7 Date/Time of Birth -->\r\n	<xsl:value-of select=\"patient/patient.date_of_birth\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n	<!-- PV1 header -->\r\n	<xsl:text>PV1</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-1 Sub ID -->\r\n	<xsl:text>|O</xsl:text>             <!-- PV1-2 Patient class (O = outpatient) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-3 Patient location -->\r\n	<xsl:value-of select=\"encounter/encounter.location_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-4 Admission type (2 = return) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-5 Pre-Admin Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-6 Prior Patient Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-7 Attending Doctor -->\r\n	<xsl:value-of select=\"encounter/encounter.provider_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-8 Referring Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-9 Consulting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-10 Hospital Service -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-11 Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-12 Preadmin Test Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-13 Re-adminssion Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-14 Admit Source -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-15 Ambulatory Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-16 VIP Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-17 Admitting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-18 Patient Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-19 Visit Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-20 Financial Class -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-21 Charge Price Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-22 Courtesy Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-23 Credit Rating -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-24 Contract Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-25 Contract Effective Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-26 Contract Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-27 Contract Period -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-28 Interest Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-29 Transfer to Bad Debt Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-30 Transfer to Bad Debt Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Agency Code -->\r\n  <xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Transfer Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-33 Bad Debt Recovery Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-34 Delete Account Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-35 Delete Account Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-36 Discharge Disposition -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-37 Discharge To Location -->\r\n	<xsl:if test=\"patient/patient.health_center\">\r\n		<xsl:value-of select=\"replace(patient/patient.health_center,\'\\^\',\'&amp;\')\" />\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-38 Diet Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-39 Servicing Facility -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-40 Bed Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-41 Account Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-42 Pending Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-43 Prior Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-44 Admit Date/Time -->\r\n	<xsl:call-template name=\"hl7Date\">\r\n		<xsl:with-param name=\"date\" select=\"encounter/encounter.encounter_datetime\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-45 Discharge Date/Time -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-46 Current Patient Balance -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-47 Total Charges -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-48 Total Adjustments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-49 Total Payments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-50 Alternate Visit ID -->\r\n	<xsl:text>|V</xsl:text>             <!-- PV1-51 Visit Indicator -->\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- We use encounter date as the timestamp for each observation -->\r\n	<xsl:variable name=\"encounterTimestamp\">\r\n		<xsl:call-template name=\"hl7Date\">\r\n			<xsl:with-param name=\"date\" select=\"encounter/encounter.encounter_datetime\" />\r\n		</xsl:call-template>\r\n	</xsl:variable>\r\n	\r\n	<!-- ORC Common Order Segment -->\r\n	<xsl:text>ORC</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|RE</xsl:text>            <!-- ORC-1 Order Control (RE = obs to follow) -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-2 Placer Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-3 Filler Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-4 Placer Group Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-5 Order Status -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-6 Response Flag -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-7 Quantity/Timing -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-8 Parent -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-9 Date/Time of Transaction -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"xs:dateTime(header/date_entered)\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- ORC-10 Entered By -->\r\n	<xsl:value-of select=\"header/enterer\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- Observation(s) -->\r\n	<xsl:variable name=\"obsList\" select=\"obs/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\" />\r\n	<xsl:variable name=\"obsListCount\" select=\"count($obsList)\" as=\"xs:integer\" />\r\n	<!-- Observation OBR -->\r\n	<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n	<xsl:text>1</xsl:text>\r\n	<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n	<xsl:value-of select=\"obs/@openmrs_concept\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- observation OBXs -->\r\n	<xsl:for-each select=\"$obsList\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"value\">\r\n				<xsl:call-template name=\"obsObx\">\r\n					<xsl:with-param name=\"setId\" select=\"position()\" />\r\n					<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n					<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n					<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n					<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n					<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n					<xsl:with-param name=\"value\" select=\"value\" />\r\n					<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n				<xsl:variable name=\"setId\" select=\"position()\" />\r\n				<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n						<xsl:with-param name=\"subId\" select=\"concat($setId,position())\" />\r\n						<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:for-each>\r\n			</xsl:otherwise>\r\n		</xsl:choose>\r\n	</xsl:for-each>\r\n	\r\n	<!-- Grouped observation(s) -->\r\n	<xsl:variable name=\"obsGroupList\" select=\"obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]\" />\r\n	<xsl:variable name=\"obsGroupListCount\" select=\"count($obsGroupList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$obsGroupList\">\r\n		<!-- Observation OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + position()\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n		\r\n		<!-- Generate OBXs -->\r\n		<xsl:for-each select=\"*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"subId\" select=\"1\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"concat(\'1.\',position())\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>\r\n	</xsl:for-each>\r\n\r\n	<!-- Problem list(s) -->\r\n	<xsl:variable name=\"problemList\" select=\"problem_list/*[value[text() != \'\']]\" />\r\n	<xsl:variable name=\"problemListCount\" select=\"count($problemList)\" as=\"xs:integer\" />\r\n	<xsl:if test=\"$problemList\">\r\n		<!-- Problem list OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"problem_list/@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n		<!-- Problem list OBXs -->\r\n		<xsl:for-each select=\"$problemList\">\r\n			<xsl:call-template name=\"obsObx\">\r\n				<xsl:with-param name=\"setId\" select=\"position()\" />\r\n				<xsl:with-param name=\"datatype\" select=\"\'CWE\'\" />\r\n				<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n				<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n				<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n				<xsl:with-param name=\"value\" select=\"value\" />\r\n				<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n			</xsl:call-template>		\r\n		</xsl:for-each>\r\n	</xsl:if>\r\n	\r\n	<!-- Orders -->\r\n	<xsl:variable name=\"orderList\" select=\"orders/*[*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]]\" />\r\n	<xsl:variable name=\"orderListCount\" select=\"count($orderList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$orderList\">\r\n		<!-- Order section OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + $problemListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n		<!-- Order OBXs -->\r\n		<xsl:for-each select=\"*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"position()\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>	\r\n	</xsl:for-each>\r\n	\r\n</xsl:template>\r\n\r\n<!-- Patient Identifier (CX) generator -->\r\n<xsl:template name=\"patient_id\">\r\n	<xsl:param name=\"pid\" />\r\n	<xsl:param name=\"auth\" />\r\n	<xsl:param name=\"type\" />\r\n	<xsl:value-of select=\"$pid\" />\r\n	<xsl:text>^</xsl:text>              <!-- Check digit -->\r\n	<xsl:text>^</xsl:text>              <!-- Check Digit Scheme -->\r\n	<xsl:text>^</xsl:text>              <!-- Assigning Authority -->\r\n	<xsl:value-of select=\"$auth\" />\r\n	<xsl:text>^</xsl:text>              <!-- Identifier Type -->\r\n	<xsl:value-of select=\"$type\" />\r\n</xsl:template>\r\n\r\n<!-- OBX Generator -->\r\n<xsl:template name=\"obsObx\">\r\n	<xsl:param name=\"setId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"subId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"datatype\" required=\"yes\" />\r\n	<xsl:param name=\"concept\" required=\"yes\" />\r\n	<xsl:param name=\"date\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"time\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"value\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"units\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"encounterTimestamp\" required=\"yes\" />\r\n	<xsl:text>OBX</xsl:text>                     <!-- Message type -->\r\n	<xsl:text>|</xsl:text>                       <!-- Set ID -->\r\n	<xsl:value-of select=\"$setId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Observation datatype -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:text>NM</xsl:text>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$datatype\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Concept (what was observed -->\r\n	<xsl:value-of select=\"$concept\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Sub-ID -->\r\n	<xsl:value-of select=\"$subId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Value -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'TS\'\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'DT\'\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'TM\'\">\r\n			<xsl:call-template name=\"hl7Time\">\r\n				<xsl:with-param name=\"time\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"$value = \'0\' or upper-case($value) = \'FALSE\'\">0</xsl:when>\r\n				<xsl:otherwise>1</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$value\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Units -->\r\n	<xsl:value-of select=\"$units\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Reference range -->\r\n	<xsl:text>|</xsl:text>                       <!-- Abnormal flags -->\r\n	<xsl:text>|</xsl:text>                       <!-- Probability -->\r\n	<xsl:text>|</xsl:text>                       <!-- Nature of abnormal test -->\r\n	<xsl:text>|</xsl:text>                       <!-- Observation result status -->\r\n	<xsl:text>|</xsl:text>                       <!-- Effective date -->\r\n	<xsl:text>|</xsl:text>                       <!-- User defined access checks -->\r\n	<xsl:text>|</xsl:text>                       <!-- Date time of observation -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$date and $time\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"dateTime($date,$time)\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$date\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$date\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$encounterTimestamp\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>&#x000d;</xsl:text>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted timestamp -->\r\n<xsl:template name=\"hl7Timestamp\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:value-of select=\"concat(year-from-dateTime($date),format-number(month-from-dateTime($date),\'00\'),format-number(day-from-dateTime($date),\'00\'),format-number(hours-from-dateTime($date),\'00\'),format-number(minutes-from-dateTime($date),\'00\'),format-number(floor(seconds-from-dateTime($date)),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted date -->\r\n<xsl:template name=\"hl7Date\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"contains(string($date),\'T\')\">\r\n				<xsl:call-template name=\"hl7Date\">\r\n					<xsl:with-param name=\"date\" select=\"xs:date(substring-before($date,\'T\'))\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n					<xsl:value-of select=\"concat(year-from-date($date),format-number(month-from-date($date),\'00\'),format-number(day-from-date($date),\'00\'))\" />\r\n			</xsl:otherwise>\r\n		</xsl:choose>				\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted time -->\r\n<xsl:template name=\"hl7Time\">\r\n	<xsl:param name=\"time\" />\r\n	<xsl:if test=\"$time != \'\'\">\r\n		<xsl:value-of select=\"concat(format-number(hours-from-time($time),\'00\'),format-number(minutes-from-time($time),\'00\'),format-number(floor(seconds-from-time($time)),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n</xsl:stylesheet>';
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.2.04');

#----------------------------------------
# OpenMRS Datamodel version 1.2.05
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
call diff_procedure('1.2.05');

#---------------------------------------
# OpenMRS Datamodel version 1.2.06
# Brian McKown      Mar 06 2008
# Alter report_schema table
# Modify xml_data to MEDIUMTEXT
#-------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

    ALTER TABLE `report_schema_xml` 
        MODIFY COLUMN `xml_data` MEDIUMTEXT CHARACTER SET utf8 NOT NULL;

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.2.06');

#---------------------------------------
# OpenMRS Datamodel version 1.2.07
# Brian McKown      Mar 06 2008
# Alter global_property table
# Modify property_value to MEDIUMTEXT
#---------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

    ALTER TABLE `global_property` 
        MODIFY COLUMN `property_value` MEDIUMTEXT CHARACTER SET utf8 DEFAULT NULL;

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.2.07');

#---------------------------------------
# OpenMRS Datamodel version 1.2.08
# Darius Jazayeri      Mar 29 2008
# Drop the REPORT table which has never been used
#---------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	DROP TABLE `report`;

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.2.08');


#---------------------------------------------------------------------------------
#--   OpenMRS 1.3.0           ------------------------------------------------------
#---------------------------------------------------------------------------------

#---------------------------------------
# OpenMRS Datamodel version 1.3.0.00
# Ben Wolfe      May 9 2008
# Upgrading to the 1.3.0
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
call diff_procedure('1.3.0.00');

#---------------------------------------
# OpenMRS Datamodel version 1.3.0.01
# Chase Yarbrough      May 19 2008
# Adding infrastructure for pluggable patient identifier validators.
#---------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

    ALTER TABLE `patient_identifier_type` ADD COLUMN `validator` VARCHAR(200);
    UPDATE `patient_identifier_type` SET `validator`="org.openmrs.patient.impl.LuhnIdentifierValidator" WHERE check_digit=true;

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.01');

#----------------------------------------
# OpenMRS Datamodel version 1.3.0.02
# Darius Jazayeri               March 13, 2008
# Adding modified* columns to Cohort
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	ALTER TABLE `cohort` ADD COLUMN `changed_by` int(11) default NULL;
	ALTER TABLE `cohort` ADD COLUMN `date_changed` datetime default NULL;
	ALTER TABLE `cohort` ADD KEY `user_who_changed_cohort` (`changed_by`);
	ALTER TABLE `cohort` ADD CONSTRAINT `user_who_changed_cohort` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`);
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.02');

#----------------------------------------
# OpenMRS Datamodel version 1.3.0.03
# Mike Seaton         March 31, 2008
# API-Refactoring of Program tables
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	ALTER TABLE `program` ADD COLUMN `name` varchar(50);
	UPDATE program p SET p.name = (SELECT n.name FROM concept_name n WHERE n.concept_id = p.concept_id LIMIT 1);
	ALTER TABLE `program` MODIFY `name` varchar(50) NOT NULL;
	ALTER TABLE `program` ADD COLUMN `description` varchar(500);
	ALTER TABLE `program` CHANGE `voided` `retired` tinyint(1) NOT NULL default '0';
	ALTER TABLE `program` DROP FOREIGN KEY `user_who_voided_program`;
	ALTER TABLE `program` DROP COLUMN `voided_by`;
	ALTER TABLE `program` DROP COLUMN `date_voided`;
	ALTER TABLE `program` DROP COLUMN `void_reason`;

	ALTER TABLE `program_workflow` CHANGE COLUMN `voided` `retired` tinyint(1) NOT NULL default '0';
	ALTER TABLE `program_workflow` DROP FOREIGN KEY `workflow_voided_by`;
	ALTER TABLE `program_workflow` CHANGE COLUMN `voided_by` `changed_by` int(11) default NULL;
	ALTER TABLE `program_workflow` ADD CONSTRAINT `workflow_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`);
	ALTER TABLE `program_workflow` CHANGE COLUMN `date_voided` `date_changed` datetime default NULL;
	ALTER TABLE `program_workflow` DROP COLUMN `void_reason`;
	
	ALTER TABLE `program_workflow_state` CHANGE COLUMN `voided` `retired` tinyint(1) NOT NULL default '0';
	ALTER TABLE `program_workflow_state` DROP FOREIGN KEY `state_voided_by`;
	ALTER TABLE `program_workflow_state` CHANGE COLUMN `voided_by` `changed_by` int(11) default NULL;
	ALTER TABLE `program_workflow_state` ADD CONSTRAINT `state_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`);
	ALTER TABLE `program_workflow_state` CHANGE COLUMN `date_voided` `date_changed` datetime default NULL;
	ALTER TABLE `program_workflow_state` DROP COLUMN `void_reason`;

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.03');


#----------------------------------------
# OpenMRS Datamodel version 1.3.0.04
# Ben Wolfe               April 1st, 2008
# Adding retired* columns to Order Type
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	ALTER TABLE `order_type` ADD COLUMN `retired` tinyint(1) NOT NULL default 0;
	ALTER TABLE `order_type` ADD COLUMN `retired_by` int(11) default NULL;
	ALTER TABLE `order_type` ADD COLUMN `date_retired` datetime default NULL;
	ALTER TABLE `order_type` ADD COLUMN `retire_reason` varchar(255) default NULL;
	ALTER TABLE `order_type` ADD KEY `user_who_retired_order_type` (`retired_by`);
	ALTER TABLE `order_type` ADD CONSTRAINT `user_who_retired_order_type` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`);
	ALTER TABLE `order_type` ADD INDEX `retired_status` (`retired`);

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.04');

#----------------------------------------
# OpenMRS Datamodel version 1.3.0.05
# Brian McKown               April 4, 2008
# Adding retired* columns to Encounter Type
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

    ALTER TABLE `encounter_type` ADD COLUMN `retired` tinyint(1) NOT NULL default 0;
    ALTER TABLE `encounter_type` ADD COLUMN `retired_by` int(11) default NULL;
    ALTER TABLE `encounter_type` ADD COLUMN `date_retired` datetime default NULL;
    ALTER TABLE `encounter_type` ADD KEY `user_who_retired_encounter_type` (`retired_by`);
    ALTER TABLE `encounter_type` ADD CONSTRAINT `user_who_retired_encounter_type` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`);
    ALTER TABLE `encounter_type` ADD INDEX `encounter_type_retired_status` (`retired`);

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.05');

#----------------------------------------
# OpenMRS Datamodel version 1.3.0.06
# Ben Wolfe               April 8th, 2008
# Adding retired* columns to PatientIdentifierType
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	ALTER TABLE `patient_identifier_type` ADD COLUMN `retired` tinyint(1) NOT NULL default 0;
	ALTER TABLE `patient_identifier_type` ADD COLUMN `retired_by` int(11) default NULL;
	ALTER TABLE `patient_identifier_type` ADD COLUMN `date_retired` datetime default NULL;
	ALTER TABLE `patient_identifier_type` ADD COLUMN `retire_reason` varchar(255) default NULL;
	ALTER TABLE `patient_identifier_type` ADD KEY `user_who_retired_patient_identifier_type` (`retired_by`);
	ALTER TABLE `patient_identifier_type` ADD CONSTRAINT `user_who_retired_patient_identifier_type` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`);
	ALTER TABLE `patient_identifier_type` ADD INDEX `retired_status` (`retired`);

	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.06');

#----------------------------------------
# OpenMRS Datamodel version 1.3.0.07
# Brian McKown               April 8, 2008
# Adding retired* columns to Location
# Added retire_reason col to EncounterType
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

    ALTER TABLE `encounter_type` ADD COLUMN `retire_reason` varchar(255) default NULL;
    ALTER TABLE `encounter_type` DROP INDEX `encounter_type_retired_status`, 
    ADD INDEX `retired_status` (`retired`);

    ALTER TABLE `location` ADD COLUMN `retired` tinyint(1) NOT NULL default 0;
    ALTER TABLE `location` ADD COLUMN `retired_by` int(11) default NULL;
    ALTER TABLE `location` ADD COLUMN `date_retired` datetime default NULL;
    ALTER TABLE `location` ADD COLUMN `retire_reason` varchar(255) default NULL;
    ALTER TABLE `location` ADD KEY `user_who_retired_location` (`retired_by`);
    ALTER TABLE `location` ADD CONSTRAINT `user_who_retired_location` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`);
    ALTER TABLE `location` ADD INDEX `retired_status` (`retired`);

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.07');

#----------------------------------------
# OpenMRS Datamodel version 1.3.0.08
# Ben Wolfe               May 16th, 2008
# Adding retired* columns to Concept

#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

    ALTER TABLE `concept` ADD COLUMN `retired_by` int(11) default NULL;
    ALTER TABLE `concept` ADD COLUMN `date_retired` datetime default NULL;
    ALTER TABLE `concept` ADD COLUMN `retire_reason` varchar(255) default NULL;
    ALTER TABLE `concept` ADD KEY `user_who_retired_concept` (`retired_by`);
    ALTER TABLE `concept` ADD CONSTRAINT `user_who_retired_concept` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`);
	
	ALTER TABLE `concept_datatype` ADD COLUMN `retired` tinyint(1) NOT NULL default 0;
    ALTER TABLE `concept_datatype` ADD COLUMN `retired_by` int(11) default NULL;
    ALTER TABLE `concept_datatype` ADD COLUMN `date_retired` datetime default NULL;
    ALTER TABLE `concept_datatype` ADD COLUMN `retire_reason` varchar(255) default NULL;
    ALTER TABLE `concept_datatype` ADD KEY `user_who_retired_concept_datatype` (`retired_by`);
    ALTER TABLE `concept_datatype` ADD CONSTRAINT `user_who_retired_concept_datatype` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`);
	ALTER TABLE `concept_datatype` ADD INDEX `concept_datatype_retired_status` (`retired`);

	ALTER TABLE `concept_class` ADD COLUMN `retired` tinyint(1) NOT NULL default 0;
    ALTER TABLE `concept_class` ADD COLUMN `retired_by` int(11) default NULL;
    ALTER TABLE `concept_class` ADD COLUMN `date_retired` datetime default NULL;
    ALTER TABLE `concept_class` ADD COLUMN `retire_reason` varchar(255) default NULL;
    ALTER TABLE `concept_class` ADD KEY `user_who_retired_concept_class` (`retired_by`);
    ALTER TABLE `concept_class` ADD CONSTRAINT `user_who_retired_concept_class` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`);
	ALTER TABLE `concept_class` ADD INDEX `concept_class_retired_status` (`retired`);
	
	ALTER TABLE `drug` CHANGE COLUMN `voided` `retired` tinyint(1) NOT NULL default '0';
	ALTER TABLE `drug` DROP FOREIGN KEY `user_who_voided_drug`;
	ALTER TABLE `drug` CHANGE COLUMN `voided_by` `retired_by` int(11) default NULL;
	ALTER TABLE `drug` ADD CONSTRAINT `drug_retired_by` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`);
	ALTER TABLE `drug` CHANGE COLUMN `date_voided` `date_retired` datetime default NULL;
	ALTER TABLE `drug` CHANGE COLUMN `void_reason` `retire_reason` datetime default NULL;
	
	ALTER TABLE `concept_name` ADD COLUMN `concept_name_id` int(11) UNIQUE KEY NOT NULL AUTO_INCREMENT;
	ALTER TABLE `concept_name` ADD INDEX `unique_concept_name_id` (`concept_id`);
	ALTER TABLE `concept_name` DROP PRIMARY KEY, ADD PRIMARY KEY (`concept_name_id`);
	
    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.08');


#----------------------------------------
# OpenMRS Datamodel version 1.3.0.09
# Darius Jazayeri               May 4, 2008
# Adding retired column to Field
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

    ALTER TABLE `field` ADD COLUMN `retired` tinyint(1) NOT NULL default 0;
    ALTER TABLE `field` ADD COLUMN `retired_by` int(11) default NULL;
    ALTER TABLE `field` ADD COLUMN `date_retired` datetime default NULL;
    ALTER TABLE `field` ADD COLUMN `retire_reason` varchar(255) default NULL;
    ALTER TABLE `field` ADD KEY `user_who_retired_field` (`retired_by`);
    ALTER TABLE `field` ADD CONSTRAINT `user_who_retired_field` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`);
	ALTER TABLE `field` ADD INDEX `field_retired_status` (`retired`);

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.09');


#----------------------------------------
# OpenMRS Datamodel version 1.3.0.10
# Ben Wolfe               May 24, 2008
# Adding retired column to PersonAttributeType
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

    ALTER TABLE `person_attribute_type` ADD COLUMN `retired` tinyint(1) NOT NULL default 0;
    ALTER TABLE `person_attribute_type` ADD COLUMN `retired_by` int(11) default NULL;
    ALTER TABLE `person_attribute_type` ADD COLUMN `date_retired` datetime default NULL;
    ALTER TABLE `person_attribute_type` ADD COLUMN `retire_reason` varchar(255) default NULL;
    ALTER TABLE `person_attribute_type` ADD KEY `user_who_retired_person_attribute_type` (`retired_by`);
    ALTER TABLE `person_attribute_type` ADD CONSTRAINT `user_who_retired_person_attribute_type` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`);
	ALTER TABLE `person_attribute_type` ADD INDEX `person_attribute_type_retired_status` (`retired`);

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.10');

#----------------------------------------
# OpenMRS Datamodel version 1.3.0.11
# Ben Wolfe               May 27, 2008
# Modifying concept_name table for hibernate insert quirk
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

    ALTER TABLE `concept_name` MODIFY COLUMN `concept_id` int(11) default NULL;

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.11');

#----------------------------------------
# OpenMRS Datamodel version 1.3.0.12
# Ben Wolfe               June 25, 2008
# Giving 1.3 modified privileges to roles
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	set FOREIGN_KEY_CHECKS = 0;
	
    insert into role_privilege (role, privilege) select distinct role, 'View Patient Programs' from role_privilege rp where privilege = 'Manage Patient Programs' and not exists (select * from role_privilege where role = rp.role and privilege = 'View Patient Programs');
	insert into role_privilege (role, privilege) select distinct role, 'Edit Patient Programs' from role_privilege rp where privilege = 'Manage Patient Programs' and not exists (select * from role_privilege where role = rp.role and privilege = 'Edit Patient Programs');
	update role_privilege set privilege = 'Delete Patient Programs' where privilege = 'Manage Patient Programs';
	
	update role_privilege set privilege = 'Edit Concepts' where privilege = 'Manage Concepts';
	delete from role_privilege where privilege = 'Add Concepts';

	update role_privilege set privilege = 'Edit Forms' where privilege = 'Manage Forms';
	delete from role_privilege where privilege = 'Add Forms';

	set FOREIGN_KEY_CHECKS = 1;

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.12');

#----------------------------------------
# OpenMRS Datamodel version 1.3.0.13
# Ben Wolfe               July 3, 2008
# Changing person.dead and person_attribute_type.searchable to tinyint
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	ALTER TABLE person CHANGE COLUMN dead dead tinyint(1) NOT NULL DEFAULT '0';
	ALTER TABLE person_attribute_type CHANGE COLUMN searchable searchable tinyint(1) NOT NULL DEFAULT '0';

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.13');

#----------------------------------------
# OpenMRS Datamodel version 1.3.0.14
# Ben Wolfe               July 24th, 2008
# Giving 1.3 modified privileges to roles
#----------------------------------------

#-- temporary procedure to check and add a privilege to the Authenticated role
DROP PROCEDURE IF EXISTS insert_authenticated_privilege;
delimiter //
CREATE PROCEDURE insert_authenticated_privilege (IN priv VARCHAR(50))
 BEGIN
	IF NOT EXISTS (SELECT * FROM role_privilege where role = 'Authenticated' and privilege = priv) THEN
		insert into role_privilege (role, privilege) values ('Authenticated', priv);
	END IF;
 END;
//
delimiter ;

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	set FOREIGN_KEY_CHECKS = 0;
	
    insert into role_privilege (role, privilege) select distinct role, 'View Patient Programs' from role_privilege rp where privilege = 'Manage Patient Programs' and not exists (select * from role_privilege where role = rp.role and privilege = 'View Patient Programs');
	insert into role_privilege (role, privilege) select distinct role, 'Edit Patient Programs' from role_privilege rp where privilege = 'Manage Patient Programs' and not exists (select * from role_privilege where role = rp.role and privilege = 'Edit Patient Programs');
	insert into role_privilege (role, privilege) select distinct role, 'Delete Patient Programs' from role_privilege rp where privilege = 'Manage Patient Programs' and not exists (select * from role_privilege where role = rp.role and privilege = 'Delete Patient Programs');
	delete from role_privilege where privilege = 'Manage Patient Programs';
	
	#-- the 1.3.0.12 update didn't do this change correctly
	update role_privilege set privilege = 'Manage Concepts' where privilege = 'Edit Concepts';
	update role_privilege set privilege = 'Manage Forms' where privilege = 'Edit Forms';
	
	#-- The concept proposal privilege changed slightly
	insert into role_privilege (role, privilege) select distinct role, 'Add Concept Proposals' from role_privilege rp where privilege = 'Add Concept Proposal' and not exists (select * from role_privilege where role = rp.role and privilege = 'Add Concept Proposals');
	insert into role_privilege (role, privilege) select distinct role, 'Edit Concept Proposals' from role_privilege rp where privilege = 'Edit Concept Proposal' and not exists (select * from role_privilege where role = rp.role and privilege = 'Edit Concept Proposals');
	
	#-- These objects/methods now require an explicit privilege.  Previously they only required authentication
	call insert_authenticated_privilege('View Encounter Types');
	call insert_authenticated_privilege('View Locations');
	call insert_authenticated_privilege('View Mime Types');
	call insert_authenticated_privilege('View Identifier Types');
	call insert_authenticated_privilege('View Concept Classes');
	call insert_authenticated_privilege('View Concept Datatypes');
	call insert_authenticated_privilege('View Privileges');
	call insert_authenticated_privilege('View Roles');
	call insert_authenticated_privilege('View Field Types');
	call insert_authenticated_privilege('View Order Types');
	call insert_authenticated_privilege('View Relationship Types');
	call insert_authenticated_privilege('View Global Properties');
	call insert_authenticated_privilege('View Person Attribute Types');
	call insert_authenticated_privilege('View Relationships');
	call insert_authenticated_privilege('View Tribes');
	
	#-- If a role can View Patients...add the new View Patient Identifiers privilege to that role
	insert into role_privilege (role, privilege) select distinct role, 'View Patient Identifiers' from role_privilege rp where privilege = 'View Patients' and not exists (select * from role_privilege where role = rp.role and privilege = 'View Patient Identifiers');	
	
	# THIS IS INCORRECT, BUT DOES NOT GET FIXED UNTIL 1.3.3.0 and 1.4.0.20
	#-- Convert Manage Encounter Types
	insert into role_privilege (role, privilege) select distinct role, 'Add Encounter Types' from role_privilege rp where privilege = 'Manage Encounter Types' and not exists (select * from role_privilege where role = rp.role and privilege = 'Add Encounter Types');
	insert into role_privilege (role, privilege) select distinct role, 'Edit Encounter Types' from role_privilege rp where privilege = 'Manage Encounter Types' and not exists (select * from role_privilege where role = rp.role and privilege = 'Edit Encounter Types');
	insert into role_privilege (role, privilege) select distinct role, 'Delete Encounter Types' from role_privilege rp where privilege = 'Manage Encounter Types' and not exists (select * from role_privilege where role = rp.role and privilege = 'Delete Encounter Types');
	delete from role_privilege where privilege = 'Manage Encounter Types';
	
	#-- Convert Manage Relationships privilege
	insert into role_privilege (role, privilege) select distinct role, 'Add Relationships' from role_privilege rp where privilege = 'Manage Relationships' and not exists (select * from role_privilege where role = rp.role and privilege = 'Add Relationships');
	insert into role_privilege (role, privilege) select distinct role, 'Edit Relationships' from role_privilege rp where privilege = 'Manage Relationships' and not exists (select * from role_privilege where role = rp.role and privilege = 'Edit Relationships');
	insert into role_privilege (role, privilege) select distinct role, 'Delete Relationships' from role_privilege rp where privilege = 'Manage Relationships' and not exists (select * from role_privilege where role = rp.role and privilege = 'Delete Relationships');
	delete from role_privilege where privilege = 'Manage Relationships';
	
	set FOREIGN_KEY_CHECKS = 1;

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.14');

DROP PROCEDURE IF EXISTS insert_authenticated_privilege;

#----------------------------------------
# OpenMRS Datamodel version 1.3.0.15
# Ben Wolfe               Aug 1st, 2008
# Fixing 1.3.0.14 modified authenticated role
#----------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	# Fix the incorrect privilege name from the privious update
	update role_privilege set privilege = 'View Relationship Types' where privilege = 'View RelationshipTypes';

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.15');

#----------------------------------------
# OpenMRS Datamodel version 1.3.0.16
# Ben Wolfe               Oct 6th, 2008
# Moving hl7_in_error items to hl7_in_queue that 
# were misprocessed due to a concept proposed bug
#----------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	insert into hl7_in_queue (hl7_source, hl7_source_key, hl7_data, date_created)
	(select 1, hl7_source_key, hl7_data, date_created from hl7_in_error where error_details = 'object references an unsaved transient instance - save the transient instance before flushing: org.openmrs.Encounter; nested exception is org.hibernate.TransientObjectException: object references an unsaved transient instance - save the transient instance before flushing: org.openmrs.Encounter');
	
	delete from hl7_in_error where error_details = 'object references an unsaved transient instance - save the transient instance before flushing: org.openmrs.Encounter; nested exception is org.hibernate.TransientObjectException: object references an unsaved transient instance - save the transient instance before flushing: org.openmrs.Encounter';

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.3.0.16');

#-----------------------------------------------------------
# OpenMRS Datamodel version 1.3.3.0
# Darius Jazayeri     Oct 17th, 2008
#
# Convert from Add/Edit/Delete Encounter Types to Manage Encounter Types
# (this was done incorrectly in 1.3.0.14)
# This sqldiff is identical to 1.4.0.20, but is here as a backport
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		#-- Convert Manage Encounter Types
		insert into role_privilege (role, privilege) select distinct role, 'Manage Encounter Types' from role_privilege rp where privilege = 'Add Encounter Types' and not exists (select * from role_privilege where role = rp.role and privilege = 'Manage Encounter Types');
		insert into role_privilege (role, privilege) select distinct role, 'Manage Encounter Types' from role_privilege rp where privilege = 'Edit Encounter Types' and not exists (select * from role_privilege where role = rp.role and privilege = 'Manage Encounter Types');
		insert into role_privilege (role, privilege) select distinct role, 'Manage Encounter Types' from role_privilege rp where privilege = 'Delete Encounter Types' and not exists (select * from role_privilege where role = rp.role and privilege = 'Manage Encounter Types');
		delete from role_privilege where privilege = 'Add Encounter Types';
		delete from role_privilege where privilege = 'Edit Encounter Types';
		delete from role_privilege where privilege = 'Delete Encounter Types';
			
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.3.3.0');


#----------------------------------------
# OpenMRS Datamodel version 1.4.0.01
# Mike Seaton          July 28, 2008
# Adding accession_number to orders
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
    IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
    SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;
	
	ALTER TABLE `orders` ADD COLUMN `accession_number` varchar(255) default NULL;

    UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
    
    END IF;
 END;
//

delimiter ;
call diff_procedure('1.4.0.01');


#----------------------------------------
# OpenMRS Datamodel version 1.4.0.02
# Ben Wolfe                Sep 2nd, 2008
# Upgrade default from XSLT to 1.9.6 
# to enable appending obs to an encounter
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	-- Upgrade all instances of XSLT 1.9.5 to 1.9.6
	UPDATE `form`
	SET xslt = '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\r\n<!--\r\nOpenMRS FormEntry Form HL7 Translation\r\n\r\nThis XSLT is used to translate OpenMRS forms from XML into HL7 2.5 format\r\n\r\n@author Burke Mamlin, MD\r\n@author Ben Wolfe\r\n@version 1.9.6\r\n\r\n1.9.6 - added encounter/encounter.encounter_id to PV1-1\r\n1.9.5 - allow for organizing sections under \"obs\" section\r\n1.9.4 - add support for message uid (as HL7 control id) and transform of patient.health_center to Discharge to Location (PV1-37)\r\n1.9.3 - fixed rounding error on timestamp (tenths of seconds getting rounded up, causing \"60\" seconds in some cases) \r\n1.9.2 - first generally useful version\r\n-->\r\n\r\n<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" xmlns:xdt=\"http://www.w3.org/2005/xpath-datatypes\">\r\n	<xsl:output method=\"text\" version=\"1.0\" encoding=\"UTF-8\" indent=\"no\"/>\r\n\r\n<xsl:variable name=\"SENDING-APPLICATION\">FORMENTRY</xsl:variable>\r\n<xsl:variable name=\"SENDING-FACILITY\">AMRS.ELD</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-APPLICATION\">HL7LISTENER</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-FACILITY\">AMRS.ELD</xsl:variable>\r\n<xsl:variable name=\"PATIENT-AUTHORITY\"></xsl:variable> <!-- leave blank for internal id, max 20 characters -->\r\n                                                       <!-- for now, must match patient_identifier_type.name -->\r\n<xsl:variable name=\"FORM-AUTHORITY\">AMRS.ELD.FORMID</xsl:variable> <!-- max 20 characters -->\r\n\r\n<xsl:template match=\"/\">\r\n	<xsl:apply-templates />\r\n</xsl:template>\r\n\r\n<!-- Form template -->\r\n<xsl:template match=\"form\">\r\n	<!-- MSH Header -->\r\n	<xsl:text>MSH|^~\\&amp;</xsl:text>   <!-- Message header, field separator, and encoding characters -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-3 Sending application -->\r\n	<xsl:value-of select=\"$SENDING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-4 Sending facility -->\r\n	<xsl:value-of select=\"$SENDING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-5 Receiving application -->\r\n	<xsl:value-of select=\"$RECEIVING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-6 Receiving facility -->\r\n	<xsl:value-of select=\"$RECEIVING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-7 Date/time message sent -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- MSH-8 Security -->\r\n	<xsl:text>|ORU^R01</xsl:text>       <!-- MSH-9 Message type ^ Event type (observation report unsolicited) -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-10 Message control ID -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"header/uid\">\r\n			<xsl:value-of select=\"header/uid\" />\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"patient/patient.patient_id\" />\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n			</xsl:call-template>\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|P</xsl:text>             <!-- MSH-11 Processing ID -->\r\n	<xsl:text>|2.5</xsl:text>           <!-- MSH-12 HL7 version -->\r\n	<xsl:text>|1</xsl:text>             <!-- MSH-13 Message sequence number -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-14 Continuation Pointer -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-15 Accept Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-16 Application Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-17 Country Code -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-18 Character Set -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-19 Principal Language of Message -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-20 Alternate Character Set Handling Scheme -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-21 Message Profile Identifier -->\r\n	<xsl:value-of select=\"@id\" />\r\n	<xsl:text>^</xsl:text>\r\n	<xsl:value-of select=\"$FORM-AUTHORITY\" />\r\n	<xsl:text>&#x000d;</xsl:text>\r\n\r\n	<!-- PID header -->\r\n	<xsl:text>PID</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-1 Set ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-2 (deprecated) Patient ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-3 Patient Identifier List -->\r\n	<xsl:call-template name=\"patient_id\">\r\n		<xsl:with-param name=\"pid\" select=\"patient/patient.patient_id\" />\r\n		<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n		<xsl:with-param name=\"type\" select=\"L\" />\r\n	</xsl:call-template>\r\n	<xsl:if test=\"patient/patient.previous_mrn and string-length(patient/patient.previous_mrn) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.previous_mrn\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"PRIOR\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<!-- Additional patient identifiers -->\r\n	<!-- This example is for an MTCT-PLUS identifier used in the AMPATH project in Kenya (skipped if not present) -->\r\n	<xsl:if test=\"patient/patient.mtctplus_id and string-length(patient/patient.mtctplus_id) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.mtctplus_id\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"MTCTPLUS\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PID-4 (deprecated) Alternate patient ID -->\r\n	<!-- PID-5 Patient name -->\r\n	<xsl:text>|</xsl:text>              <!-- Family name -->\r\n	<xsl:value-of select=\"patient/patient.family_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Given name -->\r\n	<xsl:value-of select=\"patient/patient.given_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Middle name -->\r\n	<xsl:value-of select=\"patient/patient.middle_name\" />\r\n	<xsl:text>|</xsl:text>              <!-- PID-6 Mother\'s maiden name -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-7 Date/Time of Birth -->\r\n	<xsl:value-of select=\"patient/patient.date_of_birth\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n	<!-- PV1 header -->\r\n	<xsl:text>PV1</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-1 Sub ID -->\r\n	<xsl:value-of select="encounter/encounter.encounter_id" />\r\n	<xsl:text>|O</xsl:text>             <!-- PV1-2 Patient class (O = outpatient) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-3 Patient location -->\r\n	<xsl:value-of select=\"encounter/encounter.location_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-4 Admission type (2 = return) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-5 Pre-Admin Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-6 Prior Patient Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-7 Attending Doctor -->\r\n	<xsl:value-of select=\"encounter/encounter.provider_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-8 Referring Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-9 Consulting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-10 Hospital Service -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-11 Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-12 Preadmin Test Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-13 Re-adminssion Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-14 Admit Source -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-15 Ambulatory Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-16 VIP Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-17 Admitting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-18 Patient Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-19 Visit Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-20 Financial Class -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-21 Charge Price Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-22 Courtesy Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-23 Credit Rating -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-24 Contract Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-25 Contract Effective Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-26 Contract Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-27 Contract Period -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-28 Interest Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-29 Transfer to Bad Debt Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-30 Transfer to Bad Debt Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Agency Code -->\r\n  <xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Transfer Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-33 Bad Debt Recovery Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-34 Delete Account Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-35 Delete Account Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-36 Discharge Disposition -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-37 Discharge To Location -->\r\n	<xsl:if test=\"patient/patient.health_center\">\r\n		<xsl:value-of select=\"replace(patient/patient.health_center,\'\\^\',\'&amp;\')\" />\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-38 Diet Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-39 Servicing Facility -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-40 Bed Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-41 Account Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-42 Pending Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-43 Prior Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-44 Admit Date/Time -->\r\n	<xsl:call-template name=\"hl7Date\">\r\n		<xsl:with-param name=\"date\" select=\"encounter/encounter.encounter_datetime\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-45 Discharge Date/Time -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-46 Current Patient Balance -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-47 Total Charges -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-48 Total Adjustments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-49 Total Payments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-50 Alternate Visit ID -->\r\n	<xsl:text>|V</xsl:text>             <!-- PV1-51 Visit Indicator -->\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- We use encounter date as the timestamp for each observation -->\r\n	<xsl:variable name=\"encounterTimestamp\">\r\n		<xsl:call-template name=\"hl7Date\">\r\n			<xsl:with-param name=\"date\" select=\"encounter/encounter.encounter_datetime\" />\r\n		</xsl:call-template>\r\n	</xsl:variable>\r\n	\r\n	<!-- ORC Common Order Segment -->\r\n	<xsl:text>ORC</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|RE</xsl:text>            <!-- ORC-1 Order Control (RE = obs to follow) -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-2 Placer Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-3 Filler Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-4 Placer Group Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-5 Order Status -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-6 Response Flag -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-7 Quantity/Timing -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-8 Parent -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-9 Date/Time of Transaction -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"xs:dateTime(header/date_entered)\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- ORC-10 Entered By -->\r\n	<xsl:value-of select=\"header/enterer\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- Observation(s) -->\r\n	<!-- <xsl:variable name=\"obsList\" select=\"obs/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\" /> -->\r\n	<xsl:variable name=\"obsList\" select=\"obs/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]|obs/*[not(@openmrs_concept)]/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\" />\r\n	<xsl:variable name=\"obsListCount\" select=\"count($obsList)\" as=\"xs:integer\" />\r\n	<!-- Observation OBR -->\r\n	<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n	<xsl:text>1</xsl:text>\r\n	<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n	<xsl:value-of select=\"obs/@openmrs_concept\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- observation OBXs -->\r\n	<xsl:for-each select=\"$obsList\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"value\">\r\n				<xsl:call-template name=\"obsObx\">\r\n					<xsl:with-param name=\"setId\" select=\"position()\" />\r\n					<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n					<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n					<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n					<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n					<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n					<xsl:with-param name=\"value\" select=\"value\" />\r\n					<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n				<xsl:variable name=\"setId\" select=\"position()\" />\r\n				<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n						<xsl:with-param name=\"subId\" select=\"concat($setId,position())\" />\r\n						<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:for-each>\r\n			</xsl:otherwise>\r\n		</xsl:choose>\r\n	</xsl:for-each>\r\n	\r\n	<!-- Grouped observation(s) -->\r\n	<!-- <xsl:variable name=\"obsGroupList\" select=\"obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]\" /> -->\r\n	<xsl:variable name=\"obsGroupList\" select=\"obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]|obs/*[not(@openmrs_concept)]/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]\" />\r\n	<xsl:variable name=\"obsGroupListCount\" select=\"count($obsGroupList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$obsGroupList\">\r\n		<!-- Observation OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + position()\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n		\r\n		<!-- Generate OBXs -->\r\n		<xsl:for-each select=\"*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"subId\" select=\"1\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"concat(\'1.\',position())\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>\r\n	</xsl:for-each>\r\n\r\n	<!-- Problem list(s) -->\r\n	<xsl:variable name=\"problemList\" select=\"problem_list/*[value[text() != \'\']]\" />\r\n	<xsl:variable name=\"problemListCount\" select=\"count($problemList)\" as=\"xs:integer\" />\r\n	<xsl:if test=\"$problemList\">\r\n		<!-- Problem list OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"problem_list/@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n		<!-- Problem list OBXs -->\r\n		<xsl:for-each select=\"$problemList\">\r\n			<xsl:call-template name=\"obsObx\">\r\n				<xsl:with-param name=\"setId\" select=\"position()\" />\r\n				<xsl:with-param name=\"datatype\" select=\"\'CWE\'\" />\r\n				<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n				<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n				<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n				<xsl:with-param name=\"value\" select=\"value\" />\r\n				<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n			</xsl:call-template>		\r\n		</xsl:for-each>\r\n	</xsl:if>\r\n	\r\n	<!-- Orders -->\r\n	<xsl:variable name=\"orderList\" select=\"orders/*[*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]]\" />\r\n	<xsl:variable name=\"orderListCount\" select=\"count($orderList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$orderList\">\r\n		<!-- Order section OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + $problemListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n		<!-- Order OBXs -->\r\n		<xsl:for-each select=\"*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"position()\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>	\r\n	</xsl:for-each>\r\n	\r\n</xsl:template>\r\n\r\n<!-- Patient Identifier (CX) generator -->\r\n<xsl:template name=\"patient_id\">\r\n	<xsl:param name=\"pid\" />\r\n	<xsl:param name=\"auth\" />\r\n	<xsl:param name=\"type\" />\r\n	<xsl:value-of select=\"$pid\" />\r\n	<xsl:text>^</xsl:text>              <!-- Check digit -->\r\n	<xsl:text>^</xsl:text>              <!-- Check Digit Scheme -->\r\n	<xsl:text>^</xsl:text>              <!-- Assigning Authority -->\r\n	<xsl:value-of select=\"$auth\" />\r\n	<xsl:text>^</xsl:text>              <!-- Identifier Type -->\r\n	<xsl:value-of select=\"$type\" />\r\n</xsl:template>\r\n\r\n<!-- OBX Generator -->\r\n<xsl:template name=\"obsObx\">\r\n	<xsl:param name=\"setId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"subId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"datatype\" required=\"yes\" />\r\n	<xsl:param name=\"concept\" required=\"yes\" />\r\n	<xsl:param name=\"date\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"time\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"value\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"units\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"encounterTimestamp\" required=\"yes\" />\r\n	<xsl:text>OBX</xsl:text>                     <!-- Message type -->\r\n	<xsl:text>|</xsl:text>                       <!-- Set ID -->\r\n	<xsl:value-of select=\"$setId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Observation datatype -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:text>NM</xsl:text>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$datatype\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Concept (what was observed -->\r\n	<xsl:value-of select=\"$concept\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Sub-ID -->\r\n	<xsl:value-of select=\"$subId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Value -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'TS\'\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'DT\'\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'TM\'\">\r\n			<xsl:call-template name=\"hl7Time\">\r\n				<xsl:with-param name=\"time\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"$value = \'0\' or upper-case($value) = \'FALSE\'\">0</xsl:when>\r\n				<xsl:otherwise>1</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$value\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Units -->\r\n	<xsl:value-of select=\"$units\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Reference range -->\r\n	<xsl:text>|</xsl:text>                       <!-- Abnormal flags -->\r\n	<xsl:text>|</xsl:text>                       <!-- Probability -->\r\n	<xsl:text>|</xsl:text>                       <!-- Nature of abnormal test -->\r\n	<xsl:text>|</xsl:text>                       <!-- Observation result status -->\r\n	<xsl:text>|</xsl:text>                       <!-- Effective date -->\r\n	<xsl:text>|</xsl:text>                       <!-- User defined access checks -->\r\n	<xsl:text>|</xsl:text>                       <!-- Date time of observation -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$date and $time\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"dateTime($date,$time)\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$date\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$date\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$encounterTimestamp\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>&#x000d;</xsl:text>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted timestamp -->\r\n<xsl:template name=\"hl7Timestamp\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:value-of select=\"concat(year-from-dateTime($date),format-number(month-from-dateTime($date),\'00\'),format-number(day-from-dateTime($date),\'00\'),format-number(hours-from-dateTime($date),\'00\'),format-number(minutes-from-dateTime($date),\'00\'),format-number(floor(seconds-from-dateTime($date)),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted date -->\r\n<xsl:template name=\"hl7Date\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"contains(string($date),\'T\')\">\r\n				<xsl:call-template name=\"hl7Date\">\r\n					<xsl:with-param name=\"date\" select=\"xs:date(substring-before($date,\'T\'))\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n					<xsl:value-of select=\"concat(year-from-date($date),format-number(month-from-date($date),\'00\'),format-number(day-from-date($date),\'00\'))\" />\r\n			</xsl:otherwise>\r\n		</xsl:choose>				\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted time -->\r\n<xsl:template name=\"hl7Time\">\r\n	<xsl:param name=\"time\" />\r\n	<xsl:if test=\"$time != \'\'\">\r\n		<xsl:value-of select=\"concat(format-number(hours-from-time($time),\'00\'),format-number(minutes-from-time($time),\'00\'),format-number(floor(seconds-from-time($time)),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n</xsl:stylesheet>'
	where xslt = '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\r\n<!--\r\nOpenMRS FormEntry Form HL7 Translation\r\n\r\nThis XSLT is used to translate OpenMRS forms from XML into HL7 2.5 format\r\n\r\n@author Burke Mamlin, MD\r\n@author Ben Wolfe\r\n@version 1.9.5\r\n\r\n1.9.5 - allow for organizing sections under \"obs\" section\r\n1.9.4 - add support for message uid (as HL7 control id) and transform of patient.health_center to Discharge to Location (PV1-37)\r\n1.9.3 - fixed rounding error on timestamp (tenths of seconds getting rounded up, causing \"60\" seconds in some cases) \r\n1.9.2 - first generally useful version\r\n-->\r\n\r\n<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" xmlns:xdt=\"http://www.w3.org/2005/xpath-datatypes\">\r\n	<xsl:output method=\"text\" version=\"1.0\" encoding=\"UTF-8\" indent=\"no\"/>\r\n\r\n<xsl:variable name=\"SENDING-APPLICATION\">FORMENTRY</xsl:variable>\r\n<xsl:variable name=\"SENDING-FACILITY\">AMRS.ELD</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-APPLICATION\">HL7LISTENER</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-FACILITY\">AMRS.ELD</xsl:variable>\r\n<xsl:variable name=\"PATIENT-AUTHORITY\"></xsl:variable> <!-- leave blank for internal id, max 20 characters -->\r\n                                                       <!-- for now, must match patient_identifier_type.name -->\r\n<xsl:variable name=\"FORM-AUTHORITY\">AMRS.ELD.FORMID</xsl:variable> <!-- max 20 characters -->\r\n\r\n<xsl:template match=\"/\">\r\n	<xsl:apply-templates />\r\n</xsl:template>\r\n\r\n<!-- Form template -->\r\n<xsl:template match=\"form\">\r\n	<!-- MSH Header -->\r\n	<xsl:text>MSH|^~\\&amp;</xsl:text>   <!-- Message header, field separator, and encoding characters -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-3 Sending application -->\r\n	<xsl:value-of select=\"$SENDING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-4 Sending facility -->\r\n	<xsl:value-of select=\"$SENDING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-5 Receiving application -->\r\n	<xsl:value-of select=\"$RECEIVING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-6 Receiving facility -->\r\n	<xsl:value-of select=\"$RECEIVING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-7 Date/time message sent -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- MSH-8 Security -->\r\n	<xsl:text>|ORU^R01</xsl:text>       <!-- MSH-9 Message type ^ Event type (observation report unsolicited) -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-10 Message control ID -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"header/uid\">\r\n			<xsl:value-of select=\"header/uid\" />\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"patient/patient.patient_id\" />\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n			</xsl:call-template>\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|P</xsl:text>             <!-- MSH-11 Processing ID -->\r\n	<xsl:text>|2.5</xsl:text>           <!-- MSH-12 HL7 version -->\r\n	<xsl:text>|1</xsl:text>             <!-- MSH-13 Message sequence number -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-14 Continuation Pointer -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-15 Accept Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-16 Application Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-17 Country Code -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-18 Character Set -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-19 Principal Language of Message -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-20 Alternate Character Set Handling Scheme -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-21 Message Profile Identifier -->\r\n	<xsl:value-of select=\"@id\" />\r\n	<xsl:text>^</xsl:text>\r\n	<xsl:value-of select=\"$FORM-AUTHORITY\" />\r\n	<xsl:text>&#x000d;</xsl:text>\r\n\r\n	<!-- PID header -->\r\n	<xsl:text>PID</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-1 Set ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-2 (deprecated) Patient ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-3 Patient Identifier List -->\r\n	<xsl:call-template name=\"patient_id\">\r\n		<xsl:with-param name=\"pid\" select=\"patient/patient.patient_id\" />\r\n		<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n		<xsl:with-param name=\"type\" select=\"L\" />\r\n	</xsl:call-template>\r\n	<xsl:if test=\"patient/patient.previous_mrn and string-length(patient/patient.previous_mrn) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.previous_mrn\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"PRIOR\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<!-- Additional patient identifiers -->\r\n	<!-- This example is for an MTCT-PLUS identifier used in the AMPATH project in Kenya (skipped if not present) -->\r\n	<xsl:if test=\"patient/patient.mtctplus_id and string-length(patient/patient.mtctplus_id) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.mtctplus_id\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"MTCTPLUS\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PID-4 (deprecated) Alternate patient ID -->\r\n	<!-- PID-5 Patient name -->\r\n	<xsl:text>|</xsl:text>              <!-- Family name -->\r\n	<xsl:value-of select=\"patient/patient.family_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Given name -->\r\n	<xsl:value-of select=\"patient/patient.given_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Middle name -->\r\n	<xsl:value-of select=\"patient/patient.middle_name\" />\r\n	<xsl:text>|</xsl:text>              <!-- PID-6 Mother\'s maiden name -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-7 Date/Time of Birth -->\r\n	<xsl:value-of select=\"patient/patient.date_of_birth\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n	<!-- PV1 header -->\r\n	<xsl:text>PV1</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-1 Sub ID -->\r\n	<xsl:text>|O</xsl:text>             <!-- PV1-2 Patient class (O = outpatient) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-3 Patient location -->\r\n	<xsl:value-of select=\"encounter/encounter.location_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-4 Admission type (2 = return) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-5 Pre-Admin Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-6 Prior Patient Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-7 Attending Doctor -->\r\n	<xsl:value-of select=\"encounter/encounter.provider_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-8 Referring Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-9 Consulting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-10 Hospital Service -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-11 Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-12 Preadmin Test Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-13 Re-adminssion Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-14 Admit Source -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-15 Ambulatory Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-16 VIP Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-17 Admitting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-18 Patient Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-19 Visit Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-20 Financial Class -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-21 Charge Price Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-22 Courtesy Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-23 Credit Rating -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-24 Contract Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-25 Contract Effective Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-26 Contract Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-27 Contract Period -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-28 Interest Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-29 Transfer to Bad Debt Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-30 Transfer to Bad Debt Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Agency Code -->\r\n  <xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Transfer Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-33 Bad Debt Recovery Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-34 Delete Account Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-35 Delete Account Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-36 Discharge Disposition -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-37 Discharge To Location -->\r\n	<xsl:if test=\"patient/patient.health_center\">\r\n		<xsl:value-of select=\"replace(patient/patient.health_center,\'\\^\',\'&amp;\')\" />\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-38 Diet Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-39 Servicing Facility -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-40 Bed Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-41 Account Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-42 Pending Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-43 Prior Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-44 Admit Date/Time -->\r\n	<xsl:call-template name=\"hl7Date\">\r\n		<xsl:with-param name=\"date\" select=\"encounter/encounter.encounter_datetime\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-45 Discharge Date/Time -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-46 Current Patient Balance -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-47 Total Charges -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-48 Total Adjustments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-49 Total Payments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-50 Alternate Visit ID -->\r\n	<xsl:text>|V</xsl:text>             <!-- PV1-51 Visit Indicator -->\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- We use encounter date as the timestamp for each observation -->\r\n	<xsl:variable name=\"encounterTimestamp\">\r\n		<xsl:call-template name=\"hl7Date\">\r\n			<xsl:with-param name=\"date\" select=\"encounter/encounter.encounter_datetime\" />\r\n		</xsl:call-template>\r\n	</xsl:variable>\r\n	\r\n	<!-- ORC Common Order Segment -->\r\n	<xsl:text>ORC</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|RE</xsl:text>            <!-- ORC-1 Order Control (RE = obs to follow) -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-2 Placer Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-3 Filler Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-4 Placer Group Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-5 Order Status -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-6 Response Flag -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-7 Quantity/Timing -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-8 Parent -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-9 Date/Time of Transaction -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"xs:dateTime(header/date_entered)\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- ORC-10 Entered By -->\r\n	<xsl:value-of select=\"header/enterer\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- Observation(s) -->\r\n	<!-- <xsl:variable name=\"obsList\" select=\"obs/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\" /> -->\r\n	<xsl:variable name=\"obsList\" select=\"obs/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]|obs/*[not(@openmrs_concept)]/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\" />\r\n	<xsl:variable name=\"obsListCount\" select=\"count($obsList)\" as=\"xs:integer\" />\r\n	<!-- Observation OBR -->\r\n	<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n	<xsl:text>1</xsl:text>\r\n	<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n	<xsl:value-of select=\"obs/@openmrs_concept\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- observation OBXs -->\r\n	<xsl:for-each select=\"$obsList\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"value\">\r\n				<xsl:call-template name=\"obsObx\">\r\n					<xsl:with-param name=\"setId\" select=\"position()\" />\r\n					<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n					<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n					<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n					<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n					<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n					<xsl:with-param name=\"value\" select=\"value\" />\r\n					<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n				<xsl:variable name=\"setId\" select=\"position()\" />\r\n				<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n						<xsl:with-param name=\"subId\" select=\"concat($setId,position())\" />\r\n						<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:for-each>\r\n			</xsl:otherwise>\r\n		</xsl:choose>\r\n	</xsl:for-each>\r\n	\r\n	<!-- Grouped observation(s) -->\r\n	<!-- <xsl:variable name=\"obsGroupList\" select=\"obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]\" /> -->\r\n	<xsl:variable name=\"obsGroupList\" select=\"obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]|obs/*[not(@openmrs_concept)]/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]\" />\r\n	<xsl:variable name=\"obsGroupListCount\" select=\"count($obsGroupList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$obsGroupList\">\r\n		<!-- Observation OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + position()\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n		\r\n		<!-- Generate OBXs -->\r\n		<xsl:for-each select=\"*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"subId\" select=\"1\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"concat(\'1.\',position())\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>\r\n	</xsl:for-each>\r\n\r\n	<!-- Problem list(s) -->\r\n	<xsl:variable name=\"problemList\" select=\"problem_list/*[value[text() != \'\']]\" />\r\n	<xsl:variable name=\"problemListCount\" select=\"count($problemList)\" as=\"xs:integer\" />\r\n	<xsl:if test=\"$problemList\">\r\n		<!-- Problem list OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"problem_list/@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n		<!-- Problem list OBXs -->\r\n		<xsl:for-each select=\"$problemList\">\r\n			<xsl:call-template name=\"obsObx\">\r\n				<xsl:with-param name=\"setId\" select=\"position()\" />\r\n				<xsl:with-param name=\"datatype\" select=\"\'CWE\'\" />\r\n				<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n				<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n				<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n				<xsl:with-param name=\"value\" select=\"value\" />\r\n				<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n			</xsl:call-template>		\r\n		</xsl:for-each>\r\n	</xsl:if>\r\n	\r\n	<!-- Orders -->\r\n	<xsl:variable name=\"orderList\" select=\"orders/*[*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]]\" />\r\n	<xsl:variable name=\"orderListCount\" select=\"count($orderList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$orderList\">\r\n		<!-- Order section OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + $problemListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n		<!-- Order OBXs -->\r\n		<xsl:for-each select=\"*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"position()\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>	\r\n	</xsl:for-each>\r\n	\r\n</xsl:template>\r\n\r\n<!-- Patient Identifier (CX) generator -->\r\n<xsl:template name=\"patient_id\">\r\n	<xsl:param name=\"pid\" />\r\n	<xsl:param name=\"auth\" />\r\n	<xsl:param name=\"type\" />\r\n	<xsl:value-of select=\"$pid\" />\r\n	<xsl:text>^</xsl:text>              <!-- Check digit -->\r\n	<xsl:text>^</xsl:text>              <!-- Check Digit Scheme -->\r\n	<xsl:text>^</xsl:text>              <!-- Assigning Authority -->\r\n	<xsl:value-of select=\"$auth\" />\r\n	<xsl:text>^</xsl:text>              <!-- Identifier Type -->\r\n	<xsl:value-of select=\"$type\" />\r\n</xsl:template>\r\n\r\n<!-- OBX Generator -->\r\n<xsl:template name=\"obsObx\">\r\n	<xsl:param name=\"setId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"subId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"datatype\" required=\"yes\" />\r\n	<xsl:param name=\"concept\" required=\"yes\" />\r\n	<xsl:param name=\"date\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"time\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"value\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"units\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"encounterTimestamp\" required=\"yes\" />\r\n	<xsl:text>OBX</xsl:text>                     <!-- Message type -->\r\n	<xsl:text>|</xsl:text>                       <!-- Set ID -->\r\n	<xsl:value-of select=\"$setId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Observation datatype -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:text>NM</xsl:text>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$datatype\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Concept (what was observed -->\r\n	<xsl:value-of select=\"$concept\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Sub-ID -->\r\n	<xsl:value-of select=\"$subId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Value -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'TS\'\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'DT\'\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'TM\'\">\r\n			<xsl:call-template name=\"hl7Time\">\r\n				<xsl:with-param name=\"time\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"$value = \'0\' or upper-case($value) = \'FALSE\'\">0</xsl:when>\r\n				<xsl:otherwise>1</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$value\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Units -->\r\n	<xsl:value-of select=\"$units\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Reference range -->\r\n	<xsl:text>|</xsl:text>                       <!-- Abnormal flags -->\r\n	<xsl:text>|</xsl:text>                       <!-- Probability -->\r\n	<xsl:text>|</xsl:text>                       <!-- Nature of abnormal test -->\r\n	<xsl:text>|</xsl:text>                       <!-- Observation result status -->\r\n	<xsl:text>|</xsl:text>                       <!-- Effective date -->\r\n	<xsl:text>|</xsl:text>                       <!-- User defined access checks -->\r\n	<xsl:text>|</xsl:text>                       <!-- Date time of observation -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$date and $time\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"dateTime($date,$time)\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$date\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$date\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$encounterTimestamp\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>&#x000d;</xsl:text>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted timestamp -->\r\n<xsl:template name=\"hl7Timestamp\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:value-of select=\"concat(year-from-dateTime($date),format-number(month-from-dateTime($date),\'00\'),format-number(day-from-dateTime($date),\'00\'),format-number(hours-from-dateTime($date),\'00\'),format-number(minutes-from-dateTime($date),\'00\'),format-number(floor(seconds-from-dateTime($date)),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted date -->\r\n<xsl:template name=\"hl7Date\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"contains(string($date),\'T\')\">\r\n				<xsl:call-template name=\"hl7Date\">\r\n					<xsl:with-param name=\"date\" select=\"xs:date(substring-before($date,\'T\'))\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n					<xsl:value-of select=\"concat(year-from-date($date),format-number(month-from-date($date),\'00\'),format-number(day-from-date($date),\'00\'))\" />\r\n			</xsl:otherwise>\r\n		</xsl:choose>				\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted time -->\r\n<xsl:template name=\"hl7Time\">\r\n	<xsl:param name=\"time\" />\r\n	<xsl:if test=\"$time != \'\'\">\r\n		<xsl:value-of select=\"concat(format-number(hours-from-time($time),\'00\'),format-number(minutes-from-time($time),\'00\'),format-number(floor(seconds-from-time($time)),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n</xsl:stylesheet>';
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.4.0.02');

#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.03
# Andreas Kollegger   Sep 26th, 2008
#
# create concept description - creates a table to hold
# localizable concept descriptions. This does NOT remove
# concept_name.description, which is needed for data
# migrations.
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	select 'Create table concept_description then migrate' AS '*** Step: ***', new_db_version from dual;
	CREATE TABLE `concept_description` (
	  `concept_description_id` int(11) NOT NULL AUTO_INCREMENT,
	  `concept_id` int(11) NOT NULL default '0',
	  `description` text NOT NULL,
	  `locale` varchar(50) NOT NULL default '',
	  `creator` int(11) NOT NULL default '0',
	  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
	  `changed_by` int(11) default NULL,
	  `date_changed` datetime default NULL,
	  PRIMARY KEY  (`concept_description_id`),
	  KEY `concept_being_described` (`concept_id`),
	  KEY `user_who_created_description` (`creator`),
	  KEY `user_who_changed_description` (`changed_by`),
	  CONSTRAINT `description_for_concept` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`),
	  CONSTRAINT `user_who_created_description` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
	  CONSTRAINT `user_who_changed_description` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	INSERT INTO `concept_description` (concept_id, description, locale, creator, date_created)
		SELECT concept_id, description, locale, creator, date_created from `concept_name` WHERE description<>'';
	select '***' AS '...done' from dual;
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
END;
//

delimiter ;
call diff_procedure('1.4.0.03');

#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.04
# Andreas Kollegger   Sep 26th, 2008
#
# create concept_name_tag table - used to "tag" concept names
#-----------------------------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		select 'Create concept_name_tag table' AS '*** Step: ***', new_db_version from dual;
		CREATE TABLE `concept_name_tag` (
		  `concept_name_tag_id` int(11) NOT NULL auto_increment,
		  `tag` VARCHAR(50) NOT NULL,
		  `description` text NOT NULL,
		  `creator` int(11) NOT NULL default '0',
		  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
		  `voided` tinyint(1) NOT NULL default '0',
		  `voided_by` int(11) default NULL,
		  `date_voided` datetime default NULL,
		  `void_reason` varchar(255) default NULL,
		  PRIMARY KEY  (`concept_name_tag_id`),
		  KEY `user_who_created_name_tag` (`creator`),
		  KEY `user_who_voided_name_tag` (`voided_by`)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8;
		

		# add some "well-known" tags
		#
		# NOTE: common practice also employs a "preferred_<2-letter-country-code>"
		# to indicate that a name is preferred in a particular country. The locale
		# of the name is expected to be compatible. For instance the tag "preferred_GB"
		# could be attached to either the "en_GB" locale or just the "en" locale to
		# indicate a name which is preferred by english speakers in Great Britain.
		IF (SELECT COUNT(tag)='0' FROM concept_name_tag) THEN
			select 'insert well-known concept_name_tag tags' AS '*** Step: ***' from dual;
			ALTER TABLE `concept_name_tag` MODIFY COLUMN `concept_name_tag_id` int(11) UNIQUE KEY NOT NULL AUTO_INCREMENT;
			insert into `concept_name_tag`  (`tag`, `description`, `creator`, `date_created`)
				VALUES
				('default', 'name to use when nothing else is available', 1, '2007-05-01 00:00:00'),
				('short', 'preferred short name for a concept', 1, '2007-05-01 00:00:00'),
				('synonym', 'a different word with similar meaning', 1, '2007-05-01 00:00:00'),
				('preferred', 'preferred name in English', 1, '2007-05-01 00:00:00'),
				('preferred_KE', 'preferred name in Kenya', 1, '2007-06-20'),
				('preferred_LS', 'preferred name in Lesotho', 1, '2007-06-20'),
				('preferred_MW', 'preferred name in Malawi', 1, '2007-06-20'),
				('preferred_RW', 'preferred name in Rwanda', 1, '2007-06-20'),
				('preferred_SA', 'preferred name in South Africa', 1, '2007-06-20'),
				('preferred_TZ', 'preferred name in Tanzania', 1, '2007-06-20'),
				('preferred_UG', 'preferred name in Uganda', 1, '2007-06-20'),
				('preferred_UK', 'preferred name in the United Kingdom', 1, '2007-06-20'),
				('preferred_US', 'preferred name in the United States', 1, '2007-06-20'),
				('preferred_ZM', 'preferred name in Zambia', 1, '2007-06-20'),
				('preferred_ZW', 'preferred name in Zimbabwe', 1, '2007-06-20');

		END IF;
		select '***' AS '...done' from dual;


		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//

delimiter ;
call diff_procedure('1.4.0.04');


#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.05
# Andreas Kollegger   Sep 26th, 2008
#
# update concept_name -
#
# add fields to concept_name table, preparing for data migrations.
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		select 'Update concept_name table' AS '*** Step: ***', new_db_version from dual;
#		ALTER TABLE `concept_name` ADD COLUMN `concept_name_id` int(11) UNIQUE KEY NOT NULL AUTO_INCREMENT;
#		ALTER TABLE `concept_name` ADD INDEX (`concept_id`);
#		ALTER TABLE `concept_name` DROP PRIMARY KEY, ADD PRIMARY KEY (`concept_name_id`);
		ALTER TABLE `concept_name` ADD COLUMN `voided` tinyint(1) NOT NULL default '0';
		ALTER TABLE `concept_name` ADD COLUMN `voided_by` int(11) default NULL;
		ALTER TABLE `concept_name` ADD COLUMN `date_voided` datetime default NULL;
		ALTER TABLE `concept_name` ADD COLUMN `void_reason` varchar(255) default NULL;
		ALTER TABLE `concept_name` ADD INDEX user_who_voided_name (`voided_by`);
		ALTER TABLE `concept_name` ADD CONSTRAINT `user_who_voided_this_name` FOREIGN KEY (`voided_by`) REFERENCES `users` (`user_id`);
		select '***' AS '...done' from dual;
		
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//

delimiter ;
call diff_procedure('1.4.0.05');

#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.06
# Andreas Kollegger   Sep 26th, 2008
#
# create name_tag_map - allows many-to-many mapping from
# concept_name to concept_name_tag
# 
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		select 'Create concept_name_tag_map' AS '*** Step: ***', new_db_version from dual;
		CREATE TABLE `concept_name_tag_map` (
	 	  `concept_name_id` int(11) NOT NULL,
		  `concept_name_tag_id` int(11) NOT NULL,
		  KEY `map_name` (`concept_name_id`),
		  KEY `map_name_tag` (`concept_name_tag_id`),
		  CONSTRAINT `mapped_concept_name` FOREIGN KEY (`concept_name_id`) REFERENCES `concept_name` (`concept_name_id`),
		  CONSTRAINT `mapped_concept_name_tag` FOREIGN KEY (`concept_name_tag_id`) REFERENCES `concept_name_tag` (`concept_name_tag_id`)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8;
		select '***' AS '...done' from dual;
	
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//

delimiter ;
call diff_procedure('1.4.0.06');

#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.07
# Andreas Kollegger   Sep 26th, 2008
#
# tag preferred language -
#
# Tag existing concept names as preferred in language by
# using an unqualified "preferred" tag.
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
		select 'Tag existing names as preferred' AS '*** Step: ***', new_db_version from dual;

		# tag all concept_name entries with a non-null, non-blank locale as "preferred language"
		SET @NAME_TAG_ID = (SELECT `concept_name_tag_id` FROM `concept_name_tag` where tag='preferred');
		INSERT INTO `concept_name_tag_map` (`concept_name_id`, `concept_name_tag_id`)
			SELECT `concept_name_id`, @NAME_TAG_ID FROM `concept_name`
			WHERE locale is not null and locale<>'';
		select '***' AS '...done' from dual;
	
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//

delimiter ;
call diff_procedure('1.4.0.07');


#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.08
# Andreas Kollegger   Sep 26th, 2008
#
# migrate short names -
#
# Migrates concept_name.short_name fields to individual 
# concept_name entries, tagged as short.
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		select 'Migrate concept_name.short_names.' AS '*** Step: ***', new_db_version from dual;
	
		SET @NAME_TAG_ID = (SELECT `concept_name_tag_id` FROM `concept_name_tag` where tag='short');
		# make new concept_names for the short_names.
		select 'create concept_names' AS '*** sub-step 1' from dual;
		INSERT INTO `concept_name` (short_name, description, concept_id, name, locale, creator, date_created)
			SELECT 'MVP-SHORT', 'deprecated', concept_id, short_name, locale, creator, date_created 
			FROM `concept_name` 
			LEFT JOIN `concept_name_tag_map` cntm ON cntm.concept_name_id=concept_name.concept_name_id
			WHERE short_name<>'' AND cntm.concept_name_tag_id<>@NAME_TAG_ID;
		# tag the newly created short_name entries
		select 'tagging new concept_names' AS 'sub-step 2' from dual;
		INSERT INTO `concept_name_tag_map` (`concept_name_id`, `concept_name_tag_id`)
			SELECT `concept_name_id`, @NAME_TAG_ID FROM `concept_name` WHERE `short_name`='MVP-SHORT';

		select '***' AS '...done' from dual;
	
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.08');

#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.09
# Andreas Kollegger   Sep 26th, 2008
#
# migrate synonyms
#
# Move concept_synonyms to new concept_name entries, 
# tagged as 'synonym'.
#
# NOTE: requires concept_name.short_name column to
# exist in order to perform migration.
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		select 'Migrate synonyms.' AS '*** Step: ***', new_db_version from dual;
		# make new concept_names for the synonym
		INSERT INTO `concept_name` (short_name, description, concept_id, name, locale, creator, date_created)
            SELECT 'MVP-SYNONYM', '', cs.concept_id, cs.synonym, cs.locale,
                cs.creator, cs.date_created
                FROM `concept_synonym` cs
                INNER JOIN `concept` c on c.concept_id=cs.concept_id 
                WHERE cs.synonym<>'';

		# tag the newly created synonym entries
		SET @NAME_TAG_ID = (SELECT `concept_name_tag_id` FROM `concept_name_tag` where tag='synonym');
		INSERT INTO `concept_name_tag_map` (`concept_name_id`, `concept_name_tag_id`)
			SELECT `concept_name_id`, @NAME_TAG_ID FROM `concept_name` WHERE `short_name`='MVP-SYNONYM';
	
		DROP table concept_synonym;
		select '***' AS '...done' from dual;
		
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.09');


#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.10
# Andreas Kollegger   Sep 26th, 2008
#
# drop deprecated -
#
# drops deprecated tables and columns
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		select 'Drop concept_name.short_name and concept_name.description' AS '*** Step: ***', new_db_version from dual;
		ALTER TABLE `concept_name` DROP COLUMN `short_name`;

		ALTER TABLE `concept_name` DROP COLUMN `description`;
		select '***' AS '...done' from dual;

		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.10');


#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.11
# Andreas Kollegger   Sep 26th, 2008
#
# upgrade concept proposal -
#
# upgrades concept_proposal table to add localization.
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		select 'add concept_proposal.locale' AS '*** Step: ***', new_db_version from dual;
		ALTER TABLE `concept_proposal` ADD COLUMN `locale` varchar(50) NOT NULL default '';
		select '***' AS '...done' from dual;
		
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.11');


#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.12
# Andreas Kollegger   Sep 26th, 2008
#
# create concept_proposal_tag_map -
#
# Adds a new concept_proposal_tag_map table
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		select 'Creating concept_proposal_tag_map table' AS '*** Step: ***', new_db_version from dual;
		CREATE TABLE `concept_proposal_tag_map` (
	  		`concept_proposal_id` int(11) NOT NULL,
	  		`concept_name_tag_id` int(11) NOT NULL,
	  		KEY `map_proposal` (`concept_proposal_id`),
	  		KEY `map_name_tag` (`concept_name_tag_id`),
	  		CONSTRAINT `mapped_concept_proposal` FOREIGN KEY (`concept_proposal_id`) REFERENCES `concept_proposal` (`concept_proposal_id`),
	  		CONSTRAINT `mapped_concept_proposal_tag` FOREIGN KEY (`concept_name_tag_id`) REFERENCES `concept_name_tag` (`concept_name_tag_id`)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8;
		select '***' AS '...done' from dual;
		
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.12');

#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.13
# Andreas Kollegger   Sep 26th, 2008
#
# update obs table -
#
# add concept_name_id to observation answers
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		select 'Updating obs (this may take a while)' AS '*** Step: ***', new_db_version from dual;

		ALTER TABLE `obs` ADD COLUMN `value_coded_name_id` int(11) AFTER `value_coded`;

		select '***' AS '...done' from dual;
	
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.13');

#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.14
# Andreas Kollegger   Sep 26th, 2008
#
# constrain obs table -
#
# adds foreign key constraints for concept_name additions
#
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		select 'Constraining obs (this may take a while)' AS '*** Step: ***', new_db_version from dual;
		
		ALTER TABLE `obs` ADD CONSTRAINT `obs_name_of_coded_value` FOREIGN KEY (`value_coded_name_id`) 
			REFERENCES `concept_name` (`concept_name_id`);

		select '***' AS '...done' from dual;
	
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.14');

#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.15
# Andreas Kollegger   Sep 26th, 2008
#
# update concept_word table with concept_name_ids
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		select 'Adding concept_word table columns' AS '*** Step: ***', new_db_version from dual;
		
		# delete all orphan concept_names and concept_words
		delete from concept_name_tag_map where exists (select * from concept_name where concept_name.concept_name_id = concept_name_tag_map.concept_name_id and not exists (select * from concept where concept.concept_id = concept_name.concept_id));
		delete from concept_name where not exists (select * from concept where concept.concept_id = concept_name.concept_id);
		
		# This is made "NOT NULL" in the next changeset
		ALTER TABLE `concept_word` ADD COLUMN `concept_name_id` INTEGER DEFAULT NULL AFTER `locale`;

		select '***' AS '...done' from dual;

		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.15');

#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.15.1
# Ben Wolfe   Feb 27th, 2009
#
# update concept_word table with concept_name_ids
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		select 'Updating data in concept_word table columns' AS '*** Step: ***', new_db_version from dual;
		
		# Update all non-synonym words
		update 
		 concept_word
		set
		  concept_name_id = 
		    (select 
		      min(cn.concept_name_id)
		     from
		      concept_name cn,
		      concept_name_tag_map map
		     where
		      cn.concept_id = concept_word.concept_id
		      and
		      locate(word, cn.name) > 0
		      and
		      map.concept_name_id = cn.concept_name_id
		      and
		      map.concept_name_tag_id = 4)
		where synonym = '';
		
		# update all synonym words
		update 
		 concept_word
		set
		  concept_name_id = 
		    (select 
		      cn.concept_name_id
		     from
		      concept_name cn,
		      concept_name_tag_map map
		     where
		      cn.concept_id = concept_word.concept_id
		      and
		      cn.name = synonym
		      and
		      map.concept_name_id = cn.concept_name_id
		      and
		      map.concept_name_tag_id = 3)
		where synonym <> '';
		
		# clean up any synonyms that were in the word table but werent really ever synonyms
		delete from concept_word where concept_name_id = 0 or concept_name_id is null;
		
		ALTER TABLE concept_word CHANGE concept_name_id concept_name_id INTEGER NOT NULL; 
		
		# add the contraint from concept_word.concept_name_id to concept_name.concept_name_id
		ALTER TABLE `concept_word` ADD CONSTRAINT `word_for_name` FOREIGN KEY `word_for_name` (`concept_name_id`)
		    REFERENCES `concept_name` (`concept_name_id`);

		select '***' AS '...done' from dual;

		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.15.1');

#
# update concept source table
#
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		select 'Updating concept_source table' AS '*** Step: ***', new_db_version from dual;
		# Truncating data here?? First drop column and then re-add it maybe? -bwolfe (TODO)
		#ALTER TABLE `concept_source` CHANGE COLUMN `voided` `voided` tinyint(1) NOT NULL default 0;
		ALTER TABLE `concept_source` ADD INDEX `unique_hl7_code` (`hl7_code`, `voided`);
		select '***' AS '...done' from dual;

		select 'Updating concept_map table' AS '*** Step: ***' from dual;
		ALTER TABLE `concept_map` DROP COLUMN `source_id`;
		ALTER TABLE `concept_map` ADD COLUMN `source_code` varchar(255) default NULL AFTER `source`;
#		ALTER TABLE `concept_map` ADD COLUMN `concept_id` int(11) NOT NULL AFTER `concept_map_id`;
#		ALTER TABLE `concept_map` ADD CONSTRAINT `concept_using_this_mapping` FOREIGN KEY (`concept_id`) REFERENCES `concept` (`concept_id`);
		select '***' AS '...done' from dual;

		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.16');

#
# update concept_name_tag to be auto increment table
#
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		ALTER TABLE `concept_name_tag` MODIFY COLUMN `concept_name_tag_id` int(11) UNIQUE KEY NOT NULL AUTO_INCREMENT;

		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.17');


#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.18
# Andreas Kollegger   Oct 16th, 2008
#
# drop concept_name_id from obs
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

        # update the db version no matter what happens so that this procedure is skipped during the next run
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
		
		select 'Dropping concept_name_id from obs table (this may fail, which is ok. If it does fail, run this file again.)' AS '*** Step: ***', new_db_version from dual;
		
		IF (select count(*) from information_schema.columns where table_schema = schema() and table_name = 'obs' and column_name = 'concept_name_id') THEN
			ALTER TABLE `obs` DROP FOREIGN KEY `concept_name_id`;
			
			ALTER TABLE `obs` DROP COLUMN `concept_name_id`;
		END IF;
		
		select '***' AS '...done' from dual;
		
	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.18');


#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.19
# Andreas Kollegger   Oct 16th, 2008
#
# a dummy update to ensure that the db version bumps up
# after the previous call possibly failed
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.19');


#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.20
# Darius Jazayeri     Oct 17th, 2008
#
# Convert from Add/Edit/Delete Encounter Types to Manage Encounter Types
# (this was done incorrectly in 1.3.0.14)
# This is identical to 1.3.3.0 which is there as a backport
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		#-- Convert Manage Encounter Types
		insert into role_privilege (role, privilege) select distinct role, 'Manage Encounter Types' from role_privilege rp where privilege = 'Add Encounter Types' and not exists (select * from role_privilege where role = rp.role and privilege = 'Manage Encounter Types');
		insert into role_privilege (role, privilege) select distinct role, 'Manage Encounter Types' from role_privilege rp where privilege = 'Edit Encounter Types' and not exists (select * from role_privilege where role = rp.role and privilege = 'Manage Encounter Types');
		insert into role_privilege (role, privilege) select distinct role, 'Manage Encounter Types' from role_privilege rp where privilege = 'Delete Encounter Types' and not exists (select * from role_privilege where role = rp.role and privilege = 'Manage Encounter Types');
		delete from role_privilege where privilege = 'Add Encounter Types';
		delete from role_privilege where privilege = 'Edit Encounter Types';
		delete from role_privilege where privilege = 'Delete Encounter Types';
			
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.20');

#----------------------------------------
# OpenMRS Datamodel version 1.4.0.21
# Ben Wolfe                Jan 21, 2008
# Upgrade default from XSLT to 1.9.7 
# to enable fix appending obs to an encounter to use PV1-19 now
#----------------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;

delimiter //

CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
 BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	SELECT CONCAT('Updating to ', new_db_version) AS 'Datamodel Update:' FROM dual;

	-- Upgrade all instances of XSLT 1.9.6 to 1.9.7
	UPDATE `form`
	SET xslt = '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\r\n<!--\r\nOpenMRS FormEntry Form HL7 Translation\r\n\r\nThis XSLT is used to translate OpenMRS forms from XML into HL7 2.5 format\r\n\r\n@author Burke Mamlin, MD\r\n@author Ben Wolfe\r\n@version 1.9.7\r\n\r\n1.9.7 - moved encounter/encounter.encounter_id to use PV1-19 instead\r\n1.9.6 - added encounter/encounter.encounter_id to PV1-1\r\n1.9.5 - allow for organizing sections under \"obs\" section\r\n1.9.4 - add support for message uid (as HL7 control id) and transform of patient.health_center to Discharge to Location (PV1-37)\r\n1.9.3 - fixed rounding error on timestamp (tenths of seconds getting rounded up, causing \"60\" seconds in some cases) \r\n1.9.2 - first generally useful version\r\n-->\r\n\r\n<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" xmlns:xdt=\"http://www.w3.org/2005/xpath-datatypes\">\r\n	<xsl:output method=\"text\" version=\"1.0\" encoding=\"UTF-8\" indent=\"no\"/>\r\n\r\n<xsl:variable name=\"SENDING-APPLICATION\">FORMENTRY</xsl:variable>\r\n<xsl:variable name=\"SENDING-FACILITY\">AMRS.ELD</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-APPLICATION\">HL7LISTENER</xsl:variable>\r\n<xsl:variable name=\"RECEIVING-FACILITY\">AMRS.ELD</xsl:variable>\r\n<xsl:variable name=\"PATIENT-AUTHORITY\"></xsl:variable> <!-- leave blank for internal id, max 20 characters -->\r\n                                                       <!-- for now, must match patient_identifier_type.name -->\r\n<xsl:variable name=\"FORM-AUTHORITY\">AMRS.ELD.FORMID</xsl:variable> <!-- max 20 characters -->\r\n\r\n<xsl:template match=\"/\">\r\n	<xsl:apply-templates />\r\n</xsl:template>\r\n\r\n<!-- Form template -->\r\n<xsl:template match=\"form\">\r\n	<!-- MSH Header -->\r\n	<xsl:text>MSH|^~\\&amp;</xsl:text>   <!-- Message header, field separator, and encoding characters -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-3 Sending application -->\r\n	<xsl:value-of select=\"$SENDING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-4 Sending facility -->\r\n	<xsl:value-of select=\"$SENDING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-5 Receiving application -->\r\n	<xsl:value-of select=\"$RECEIVING-APPLICATION\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-6 Receiving facility -->\r\n	<xsl:value-of select=\"$RECEIVING-FACILITY\" />\r\n	<xsl:text>|</xsl:text>              <!-- MSH-7 Date/time message sent -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- MSH-8 Security -->\r\n	<xsl:text>|ORU^R01</xsl:text>       <!-- MSH-9 Message type ^ Event type (observation report unsolicited) -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-10 Message control ID -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"header/uid\">\r\n			<xsl:value-of select=\"header/uid\" />\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"patient/patient.patient_id\" />\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"current-dateTime()\" />\r\n			</xsl:call-template>\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|P</xsl:text>             <!-- MSH-11 Processing ID -->\r\n	<xsl:text>|2.5</xsl:text>           <!-- MSH-12 HL7 version -->\r\n	<xsl:text>|1</xsl:text>             <!-- MSH-13 Message sequence number -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-14 Continuation Pointer -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-15 Accept Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-16 Application Acknowledgement Type -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-17 Country Code -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-18 Character Set -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-19 Principal Language of Message -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-20 Alternate Character Set Handling Scheme -->\r\n	<xsl:text>|</xsl:text>              <!-- MSH-21 Message Profile Identifier -->\r\n	<xsl:value-of select=\"@id\" />\r\n	<xsl:text>^</xsl:text>\r\n	<xsl:value-of select=\"$FORM-AUTHORITY\" />\r\n	<xsl:text>&#x000d;</xsl:text>\r\n\r\n	<!-- PID header -->\r\n	<xsl:text>PID</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-1 Set ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-2 (deprecated) Patient ID -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-3 Patient Identifier List -->\r\n	<xsl:call-template name=\"patient_id\">\r\n		<xsl:with-param name=\"pid\" select=\"patient/patient.patient_id\" />\r\n		<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n		<xsl:with-param name=\"type\" select=\"L\" />\r\n	</xsl:call-template>\r\n	<xsl:if test=\"patient/patient.previous_mrn and string-length(patient/patient.previous_mrn) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.previous_mrn\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"PRIOR\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<!-- Additional patient identifiers -->\r\n	<!-- This example is for an MTCT-PLUS identifier used in the AMPATH project in Kenya (skipped if not present) -->\r\n	<xsl:if test=\"patient/patient.mtctplus_id and string-length(patient/patient.mtctplus_id) > 0\">\r\n		<xsl:text>~</xsl:text>\r\n		<xsl:call-template name=\"patient_id\">\r\n			<xsl:with-param name=\"pid\" select=\"patient/patient.mtctplus_id\" />\r\n			<xsl:with-param name=\"auth\" select=\"$PATIENT-AUTHORITY\" />\r\n			<xsl:with-param name=\"type\" select=\"MTCTPLUS\" />\r\n		</xsl:call-template>\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PID-4 (deprecated) Alternate patient ID -->\r\n	<!-- PID-5 Patient name -->\r\n	<xsl:text>|</xsl:text>              <!-- Family name -->\r\n	<xsl:value-of select=\"patient/patient.family_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Given name -->\r\n	<xsl:value-of select=\"patient/patient.given_name\" />\r\n	<xsl:text>^</xsl:text>              <!-- Middle name -->\r\n	<xsl:value-of select=\"patient/patient.middle_name\" />\r\n	<xsl:text>|</xsl:text>              <!-- PID-6 Mother\'s maiden name -->\r\n	<xsl:text>|</xsl:text>              <!-- PID-7 Date/Time of Birth -->\r\n	<xsl:value-of select=\"patient/patient.date_of_birth\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n	<!-- PV1 header -->\r\n	<xsl:text>PV1</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-1 Sub ID -->\r\n	<xsl:text>|O</xsl:text>             <!-- PV1-2 Patient class (O = outpatient) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-3 Patient location -->\r\n	<xsl:value-of select=\"encounter/encounter.location_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-4 Admission type (2 = return) -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-5 Pre-Admin Number -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-6 Prior Patient Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-7 Attending Doctor -->\r\n	<xsl:value-of select=\"encounter/encounter.provider_id\" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-8 Referring Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-9 Consulting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-10 Hospital Service -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-11 Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-12 Preadmin Test Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-13 Re-adminssion Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-14 Admit Source -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-15 Ambulatory Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-16 VIP Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-17 Admitting Doctor -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-18 Patient Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-19 Visit Number -->\r\n	<xsl:value-of select="encounter/encounter.encounter_id" />\r\n	<xsl:text>|</xsl:text>              <!-- PV1-20 Financial Class -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-21 Charge Price Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-22 Courtesy Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-23 Credit Rating -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-24 Contract Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-25 Contract Effective Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-26 Contract Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-27 Contract Period -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-28 Interest Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-29 Transfer to Bad Debt Code -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-30 Transfer to Bad Debt Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Agency Code -->\r\n  <xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Transfer Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-33 Bad Debt Recovery Amount -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-34 Delete Account Indicator -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-35 Delete Account Date -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-36 Discharge Disposition -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-37 Discharge To Location -->\r\n	<xsl:if test=\"patient/patient.health_center\">\r\n		<xsl:value-of select=\"replace(patient/patient.health_center,\'\\^\',\'&amp;\')\" />\r\n	</xsl:if>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-38 Diet Type -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-39 Servicing Facility -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-40 Bed Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-41 Account Status -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-42 Pending Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-43 Prior Temporary Location -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-44 Admit Date/Time -->\r\n	<xsl:call-template name=\"hl7Date\">\r\n		<xsl:with-param name=\"date\" select=\"encounter/encounter.encounter_datetime\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- PV1-45 Discharge Date/Time -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-46 Current Patient Balance -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-47 Total Charges -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-48 Total Adjustments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-49 Total Payments -->\r\n	<xsl:text>|</xsl:text>              <!-- PV1-50 Alternate Visit ID -->\r\n	<xsl:text>|V</xsl:text>             <!-- PV1-51 Visit Indicator -->\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- We use encounter date as the timestamp for each observation -->\r\n	<xsl:variable name=\"encounterTimestamp\">\r\n		<xsl:call-template name=\"hl7Date\">\r\n			<xsl:with-param name=\"date\" select=\"encounter/encounter.encounter_datetime\" />\r\n		</xsl:call-template>\r\n	</xsl:variable>\r\n	\r\n	<!-- ORC Common Order Segment -->\r\n	<xsl:text>ORC</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|RE</xsl:text>            <!-- ORC-1 Order Control (RE = obs to follow) -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-2 Placer Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-3 Filler Order Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-4 Placer Group Number -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-5 Order Status -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-6 Response Flag -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-7 Quantity/Timing -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-8 Parent -->\r\n	<xsl:text>|</xsl:text>              <!-- ORC-9 Date/Time of Transaction -->\r\n	<xsl:call-template name=\"hl7Timestamp\">\r\n		<xsl:with-param name=\"date\" select=\"xs:dateTime(header/date_entered)\" />\r\n	</xsl:call-template>\r\n	<xsl:text>|</xsl:text>              <!-- ORC-10 Entered By -->\r\n	<xsl:value-of select=\"header/enterer\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- Observation(s) -->\r\n	<!-- <xsl:variable name=\"obsList\" select=\"obs/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\" /> -->\r\n	<xsl:variable name=\"obsList\" select=\"obs/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]|obs/*[not(@openmrs_concept)]/*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\" />\r\n	<xsl:variable name=\"obsListCount\" select=\"count($obsList)\" as=\"xs:integer\" />\r\n	<!-- Observation OBR -->\r\n	<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n	<xsl:text>1</xsl:text>\r\n	<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n	<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n	<xsl:value-of select=\"obs/@openmrs_concept\" />\r\n	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n	<!-- observation OBXs -->\r\n	<xsl:for-each select=\"$obsList\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"value\">\r\n				<xsl:call-template name=\"obsObx\">\r\n					<xsl:with-param name=\"setId\" select=\"position()\" />\r\n					<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n					<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n					<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n					<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n					<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n					<xsl:with-param name=\"value\" select=\"value\" />\r\n					<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n				<xsl:variable name=\"setId\" select=\"position()\" />\r\n				<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n						<xsl:with-param name=\"subId\" select=\"concat($setId,position())\" />\r\n						<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:for-each>\r\n			</xsl:otherwise>\r\n		</xsl:choose>\r\n	</xsl:for-each>\r\n	\r\n	<!-- Grouped observation(s) -->\r\n	<!-- <xsl:variable name=\"obsGroupList\" select=\"obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]\" /> -->\r\n	<xsl:variable name=\"obsGroupList\" select=\"obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]|obs/*[not(@openmrs_concept)]/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]]\" />\r\n	<xsl:variable name=\"obsGroupListCount\" select=\"count($obsGroupList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$obsGroupList\">\r\n		<!-- Observation OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + position()\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n		\r\n		<!-- Generate OBXs -->\r\n		<xsl:for-each select=\"*[(@openmrs_concept and value and value/text() != \'\') or *[@openmrs_concept and text()=\'true\']]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"subId\" select=\"1\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"concat(\'1.\',position())\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>\r\n	</xsl:for-each>\r\n\r\n	<!-- Problem list(s) -->\r\n	<xsl:variable name=\"problemList\" select=\"problem_list/*[value[text() != \'\']]\" />\r\n	<xsl:variable name=\"problemListCount\" select=\"count($problemList)\" as=\"xs:integer\" />\r\n	<xsl:if test=\"$problemList\">\r\n		<!-- Problem list OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"problem_list/@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n\r\n		<!-- Problem list OBXs -->\r\n		<xsl:for-each select=\"$problemList\">\r\n			<xsl:call-template name=\"obsObx\">\r\n				<xsl:with-param name=\"setId\" select=\"position()\" />\r\n				<xsl:with-param name=\"datatype\" select=\"\'CWE\'\" />\r\n				<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n				<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n				<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n				<xsl:with-param name=\"value\" select=\"value\" />\r\n				<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n			</xsl:call-template>		\r\n		</xsl:for-each>\r\n	</xsl:if>\r\n	\r\n	<!-- Orders -->\r\n	<xsl:variable name=\"orderList\" select=\"orders/*[*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]]\" />\r\n	<xsl:variable name=\"orderListCount\" select=\"count($orderList)\" as=\"xs:integer\" />\r\n	<xsl:for-each select=\"$orderList\">\r\n		<!-- Order section OBR -->\r\n		<xsl:text>OBR</xsl:text>            <!-- Message type -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->\r\n		<xsl:value-of select=\"$obsListCount + $obsGroupListCount + $problemListCount + 1\" />\r\n		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->\r\n		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->\r\n		<xsl:value-of select=\"@openmrs_concept\" />\r\n		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->\r\n	\r\n		<!-- Order OBXs -->\r\n		<xsl:for-each select=\"*[@openmrs_concept and ((value and value/text() != \'\') or *[@openmrs_concept and text() = \'true\'])]\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"value\">\r\n					<xsl:call-template name=\"obsObx\">\r\n						<xsl:with-param name=\"setId\" select=\"position()\" />\r\n						<xsl:with-param name=\"datatype\" select=\"@openmrs_datatype\" />\r\n						<xsl:with-param name=\"units\" select=\"@openmrs_units\" />\r\n						<xsl:with-param name=\"concept\" select=\"@openmrs_concept\" />\r\n						<xsl:with-param name=\"date\" select=\"date/text()\" />\r\n						<xsl:with-param name=\"time\" select=\"time/text()\" />\r\n						<xsl:with-param name=\"value\" select=\"value\" />\r\n						<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n					</xsl:call-template>\r\n				</xsl:when>\r\n				<xsl:otherwise>\r\n					<xsl:variable name=\"setId\" select=\"position()\" />\r\n					<xsl:for-each select=\"*[@openmrs_concept and text() = \'true\']\">\r\n						<xsl:call-template name=\"obsObx\">\r\n							<xsl:with-param name=\"setId\" select=\"$setId\" />\r\n							<xsl:with-param name=\"subId\" select=\"position()\" />\r\n							<xsl:with-param name=\"datatype\" select=\"../@openmrs_datatype\" />\r\n							<xsl:with-param name=\"units\" select=\"../@openmrs_units\" />\r\n							<xsl:with-param name=\"concept\" select=\"../@openmrs_concept\" />\r\n							<xsl:with-param name=\"date\" select=\"../date/text()\" />\r\n							<xsl:with-param name=\"time\" select=\"../time/text()\" />\r\n							<xsl:with-param name=\"value\" select=\"@openmrs_concept\" />\r\n							<xsl:with-param name=\"encounterTimestamp\" select=\"$encounterTimestamp\" />\r\n						</xsl:call-template>\r\n					</xsl:for-each>\r\n				</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:for-each>	\r\n	</xsl:for-each>\r\n	\r\n</xsl:template>\r\n\r\n<!-- Patient Identifier (CX) generator -->\r\n<xsl:template name=\"patient_id\">\r\n	<xsl:param name=\"pid\" />\r\n	<xsl:param name=\"auth\" />\r\n	<xsl:param name=\"type\" />\r\n	<xsl:value-of select=\"$pid\" />\r\n	<xsl:text>^</xsl:text>              <!-- Check digit -->\r\n	<xsl:text>^</xsl:text>              <!-- Check Digit Scheme -->\r\n	<xsl:text>^</xsl:text>              <!-- Assigning Authority -->\r\n	<xsl:value-of select=\"$auth\" />\r\n	<xsl:text>^</xsl:text>              <!-- Identifier Type -->\r\n	<xsl:value-of select=\"$type\" />\r\n</xsl:template>\r\n\r\n<!-- OBX Generator -->\r\n<xsl:template name=\"obsObx\">\r\n	<xsl:param name=\"setId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"subId\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"datatype\" required=\"yes\" />\r\n	<xsl:param name=\"concept\" required=\"yes\" />\r\n	<xsl:param name=\"date\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"time\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"value\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"units\" required=\"no\"></xsl:param>\r\n	<xsl:param name=\"encounterTimestamp\" required=\"yes\" />\r\n	<xsl:text>OBX</xsl:text>                     <!-- Message type -->\r\n	<xsl:text>|</xsl:text>                       <!-- Set ID -->\r\n	<xsl:value-of select=\"$setId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Observation datatype -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:text>NM</xsl:text>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$datatype\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Concept (what was observed -->\r\n	<xsl:value-of select=\"$concept\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Sub-ID -->\r\n	<xsl:value-of select=\"$subId\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Value -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$datatype = \'TS\'\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'DT\'\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'TM\'\">\r\n			<xsl:call-template name=\"hl7Time\">\r\n				<xsl:with-param name=\"time\" select=\"$value\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$datatype = \'BIT\'\">\r\n			<xsl:choose>\r\n				<xsl:when test=\"$value = \'0\' or upper-case($value) = \'FALSE\'\">0</xsl:when>\r\n				<xsl:otherwise>1</xsl:otherwise>\r\n			</xsl:choose>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$value\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>|</xsl:text>                       <!-- Units -->\r\n	<xsl:value-of select=\"$units\" />\r\n	<xsl:text>|</xsl:text>                       <!-- Reference range -->\r\n	<xsl:text>|</xsl:text>                       <!-- Abnormal flags -->\r\n	<xsl:text>|</xsl:text>                       <!-- Probability -->\r\n	<xsl:text>|</xsl:text>                       <!-- Nature of abnormal test -->\r\n	<xsl:text>|</xsl:text>                       <!-- Observation result status -->\r\n	<xsl:text>|</xsl:text>                       <!-- Effective date -->\r\n	<xsl:text>|</xsl:text>                       <!-- User defined access checks -->\r\n	<xsl:text>|</xsl:text>                       <!-- Date time of observation -->\r\n	<xsl:choose>\r\n		<xsl:when test=\"$date and $time\">\r\n			<xsl:call-template name=\"hl7Timestamp\">\r\n				<xsl:with-param name=\"date\" select=\"dateTime($date,$time)\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:when test=\"$date\">\r\n			<xsl:call-template name=\"hl7Date\">\r\n				<xsl:with-param name=\"date\" select=\"$date\" />\r\n			</xsl:call-template>\r\n		</xsl:when>\r\n		<xsl:otherwise>\r\n			<xsl:value-of select=\"$encounterTimestamp\" />\r\n		</xsl:otherwise>\r\n	</xsl:choose>\r\n	<xsl:text>&#x000d;</xsl:text>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted timestamp -->\r\n<xsl:template name=\"hl7Timestamp\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:value-of select=\"concat(year-from-dateTime($date),format-number(month-from-dateTime($date),\'00\'),format-number(day-from-dateTime($date),\'00\'),format-number(hours-from-dateTime($date),\'00\'),format-number(minutes-from-dateTime($date),\'00\'),format-number(floor(seconds-from-dateTime($date)),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted date -->\r\n<xsl:template name=\"hl7Date\">\r\n	<xsl:param name=\"date\" />\r\n	<xsl:if test=\"string($date) != \'\'\">\r\n		<xsl:choose>\r\n			<xsl:when test=\"contains(string($date),\'T\')\">\r\n				<xsl:call-template name=\"hl7Date\">\r\n					<xsl:with-param name=\"date\" select=\"xs:date(substring-before($date,\'T\'))\" />\r\n				</xsl:call-template>\r\n			</xsl:when>\r\n			<xsl:otherwise>\r\n					<xsl:value-of select=\"concat(year-from-date($date),format-number(month-from-date($date),\'00\'),format-number(day-from-date($date),\'00\'))\" />\r\n			</xsl:otherwise>\r\n		</xsl:choose>				\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n<!-- Generate HL7-formatted time -->\r\n<xsl:template name=\"hl7Time\">\r\n	<xsl:param name=\"time\" />\r\n	<xsl:if test=\"$time != \'\'\">\r\n		<xsl:value-of select=\"concat(format-number(hours-from-time($time),\'00\'),format-number(minutes-from-time($time),\'00\'),format-number(floor(seconds-from-time($time)),\'00\'))\" />\r\n	</xsl:if>\r\n</xsl:template>\r\n\r\n</xsl:stylesheet>'
	WHERE xslt LIKE '%\r\n@version 1.9.6\r\n%';
	
	UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';
	
	END IF;
 END;
//

delimiter ;
call diff_procedure('1.4.0.21');


#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.22
# Ben Wolfe 			Jan 22nd 2009
#
# Remove duplicate concept name tags
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	
		UPDATE 
				concept_name_tag_map map 
			SET 
				concept_name_tag_id = (SELECT 
										 min(concept_name_tag_id)
									   FROM concept_name_tag
									   WHERE 
									     tag = (select tag from concept_name_tag where concept_name_tag_id = map.concept_name_tag_id)
									  );
									  
		CREATE TABLE `tmp_concept_name_tag_duplicates` (
			`concept_name_tag_id` int(11) NOT NULL,
		PRIMARY KEY (`concept_name_tag_id`)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8;
		
		INSERT INTO tmp_concept_name_tag_duplicates (concept_name_tag_id) (select concept_name_tag_id from concept_name_tag where tag in (select tag from concept_name_tag group by tag having count(*) > 1));
		
		DELETE FROM concept_name_tag
		WHERE
			concept_name_tag_id in (select concept_name_tag_id from tmp_concept_name_tag_duplicates)
			and
			concept_name_tag_id not in (select distinct(concept_name_tag_id) from concept_name_tag_map);
		
		DROP TABLE tmp_concept_name_tag_duplicates;
		
		ALTER TABLE `concept_name_tag` ADD CONSTRAINT concept_name_tag_unique_tags UNIQUE (`tag`);

		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.22');


#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.0.23
# Ben Wolfe 			Feb 27th 2009
#
# Fixed the description for name layout global property
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN
	
		UPDATE `global_property` SET description='Format in which to display the person names.  Valid values are short, long' WHERE property = 'layout.name.format';

		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.0.23');


#-----------------------------------------------------------
# OpenMRS Datamodel version 1.4.2.01
# Ben Wolfe            May 7th 2009
#
# Changed the primary key for concept words
#-----------------------------------------------------------
DROP PROCEDURE IF EXISTS diff_procedure;
delimiter //
CREATE PROCEDURE diff_procedure (IN new_db_version VARCHAR(10))
BEGIN
	IF (SELECT REPLACE(property_value, '.', '0') < REPLACE(new_db_version, '.', '0') FROM global_property WHERE property = 'database_version') THEN

		CREATE INDEX concept_word_concept_idx on concept_word (concept_id); 
		
		ALTER TABLE `concept_word` DROP PRIMARY KEY;

		ALTER TABLE `concept_word` ADD PRIMARY KEY (`concept_name_id`, `word`, `locale`);
		
		UPDATE `global_property` SET property_value=new_db_version WHERE property = 'database_version';

	END IF;
END;
//
delimiter ;
call diff_procedure('1.4.2.01');


#-----------------------------------
# Clean up - Keep this section at the very bottom of diff script
#-----------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;
