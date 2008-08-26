#--------------------------------------
# USE:
# sync utility script to determine if there are any tables with missing guids
# in the *current* schema
#--------------------------------------

DROP PROCEDURE IF EXISTS check_guids;

delimiter //


CREATE PROCEDURE check_guids ()
 BEGIN

  DECLARE table_name varchar(64) default null;
  DECLARE done INT DEFAULT 0;									
																					
  #Get all tables that have column named guid
  DECLARE cur_tabs_populate CURSOR FOR 
		SELECT distinct cols.table_name
		FROM INFORMATION_SCHEMA.COLUMNS cols
		WHERE cols.table_schema = schema() AND cols.COLUMN_NAME = 'guid';
	
  DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done = 1;
    	  
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
				#SET @sql_text := concat('UPDATE `',table_name,'` SET guid = UUID() WHERE guid is null or guid = '''';');
				#PREPARE stmt from @sql_text;
				#EXECUTE stmt;
				DEALLOCATE PREPARE stmt;
    END IF;
  UNTIL done END REPEAT;
  CLOSE cur_tabs_populate;
  select 'GUID scan complete.' as 'Action:' from dual;
  
  select 'Script complete.' as 'Action:' from dual;		  		
 END;


//

delimiter ;
call check_guids();

#-----------------------------------
# Clean up - Keep this section at the very bottom of diff script
#-----------------------------------
DROP PROCEDURE IF EXISTS check_guids;