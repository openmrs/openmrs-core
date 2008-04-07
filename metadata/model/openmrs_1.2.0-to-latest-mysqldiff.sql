#--------------------------------------
# USE:
#  The diffs are ordered by datamodel version number.
#--------------------------------------

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
	CREATE TEMPORARY TABLE `new_obs_groups_mapping` (
	  `grouper_obs_id` int(11) NOT NULL,
	  `obs_group_id` int(11) default NULL,
	  PRIMARY KEY  (`grouper_obs_id`)
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
		SELECT * FROM obs_group WHERE concept_id = @OTHER_CONCEPT_ID; 
	END IF;

	-- remove all bad obs grouping by setting obs_group_id to null for any obs in a solitary group and its grouper concept is not a set 
	SELECT 'Cleaning up the obs that think they are in an obs_group but really are not.' AS 'Current step (5/8):' FROM dual;
	DROP TABLE IF EXISTS `single_member_obs_groups`;
	CREATE TEMPORARY TABLE `single_member_obs_groups` (
	  `obs_id` int(11) NOT NULL,
      `obs_group_id` int(11) NOT NULL,
	  PRIMARY KEY  (`obs_id`)
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
	CREATE TEMPORARY TABLE `obs_groupers_needing_voided` (
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

#-----------------------------------
# Clean up - Keep this section at the very bottom of diff script
#-----------------------------------

DROP PROCEDURE IF EXISTS diff_procedure;