#--------------------------------------
# USE:
# part of sync model updates: add guids to all tables
# in the *current* schema
#--------------------------------------

DROP PROCEDURE IF EXISTS add_guids;

delimiter //


CREATE PROCEDURE add_guids ()
 BEGIN

  DECLARE table_name varchar(64) default null;
  DECLARE done INT DEFAULT 0;									
	
	#get all the tables in the current schema that do not have a guid column
	#exceptions:
	# tables supporting derived classes where parent already has guid: 
	#  patient, drug_order, concept_derived, concept_numeric, complex_obs, users
  DECLARE cur_tabs CURSOR FOR 
		SELECT tabs.table_name
		FROM INFORMATION_SCHEMA.TABLES tabs
		WHERE tabs.table_schema = schema()
		 AND tabs.table_name NOT IN ('patient','users','drug_order','concept_numeric','concept_derived','complex_obs')
		 AND tabs.table_name NOT Like '%synchronization_%'
		 AND NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS cols
									WHERE cols.table_schema = schema() 
										AND cols.COLUMN_NAME = 'guid' 
										AND tabs.table_name = cols.table_name);								
	
  DECLARE cur_tabs_populate CURSOR FOR 
		SELECT distinct cols.table_name
		FROM INFORMATION_SCHEMA.COLUMNS cols
		WHERE cols.table_schema = schema() AND cols.COLUMN_NAME = 'guid';

  DECLARE cur_tabs_indx CURSOR FOR 
		SELECT distinct cols.table_name
		FROM INFORMATION_SCHEMA.COLUMNS cols
		WHERE cols.table_schema = schema() AND cols.COLUMN_NAME = 'guid';
	
  DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done = 1;
  #DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN select 'Error occured in this script, contact an administrator. Exiting.' as 'WARNING' from dual; END;
  
  select 'Detecting tables without GUID columns.' as 'Action:' from dual;	
  OPEN cur_tabs;

  REPEAT
    FETCH cur_tabs INTO table_name;
    IF NOT done THEN
				# prepare stmt to alter table
				select concat('Altering ',table_name) as 'Action:' from dual;
				SET @sql_text := concat('ALTER TABLE `',table_name,'` ADD COLUMN `guid` CHAR(36) DEFAULT NULL;');
				PREPARE stmt from @sql_text;
				EXECUTE stmt;
				DEALLOCATE PREPARE stmt;
				
				#prepare stmt to populate added column
				select concat('Populating ',table_name) as 'Action:' from dual;
				SET @sql_text := concat('UPDATE `',table_name,'` SET guid = UUID() WHERE guid is null;');
				PREPARE stmt from @sql_text;
				EXECUTE stmt;
				DEALLOCATE PREPARE stmt;
				
				#comment out for now
				#prepare stmt to alter column to not null
				#SET @sql_text := concat('ALTER TABLE `',table_name,'` MODIFY COLUMN `guid` CHAR(36) NOT NULL;');
				#PREPARE stmt from @sql_text;
				#EXECUTE stmt;
				#DEALLOCATE PREPARE stmt;
								
    END IF;
  UNTIL done END REPEAT;
  CLOSE cur_tabs;
  select 'Schema update for GUIDs complete.' as 'Action:' from dual;

 		  
  ###
  #Now scan tables for empty GUIDs
  #populate all tables that have GUID columns with null (or empty) values
  SET done = 0;
  select 'Detecting tables with empty GUIDs.' as 'Action:' from dual;	
  OPEN cur_tabs_populate;
  REPEAT
    FETCH cur_tabs_populate INTO table_name;
    IF NOT done THEN
				#prepare update stmt
				SET @sql_text := concat('Select count(*) as ''Rows with empty values in ',table_name,':'' FROM `',table_name,'` WHERE guid is null or guid = '''';');
				PREPARE stmt from @sql_text;
				EXECUTE stmt;
				SET @sql_text := concat('UPDATE `',table_name,'` SET guid = UUID() WHERE guid is null or guid = '''';');
				PREPARE stmt from @sql_text;
				EXECUTE stmt;
				DEALLOCATE PREPARE stmt;				
    END IF;
  UNTIL done END REPEAT;
  CLOSE cur_tabs_populate;
  select 'GUID population complete.' as 'Action:' from dual;

  ###
  #Check for missing indecies
  #Create index on every GUID column
  SET done = 0;
  select 'Check for missing indecies on GUID columns.' as 'Action:' from dual;	
  OPEN cur_tabs_indx;
  REPEAT
    FETCH cur_tabs_indx INTO table_name;
    IF NOT done THEN
				#prepare stmt
				select concat('Adding index to ',table_name) as 'Action' from dual;
				SET @sql_text := concat('CREATE INDEX `',table_name,'_guid` USING BTREE ON `',table_name,'` (guid);');
				PREPARE stmt from @sql_text;
				EXECUTE stmt;
				DEALLOCATE PREPARE stmt;				
    END IF;
  UNTIL done END REPEAT;
  CLOSE cur_tabs_indx;
  select 'GUID index update complete.' as 'Action:' from dual;
  
  ###
  #set server guid if not already set
  select 'Verifying synchronization.server_guid value.' as 'Action:' from dual;		  
  update global_property set property_value=UUID() where property = 'synchronization.server_guid';

  select 'Script complete.' as 'Action:' from dual;		  		
 END;
//

delimiter ;
call add_guids();

#-----------------------------------
# Clean up - Keep this section at the very bottom of diff script
#-----------------------------------
DROP PROCEDURE IF EXISTS add_guids;