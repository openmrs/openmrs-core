#--------------------------------------
# USE:
# part of sync model updates: add guids to all tables
# in the *current* schema
#--------------------------------------

#--------------------------------------
# Maros Cunderlik 30 Jul 2007 10:00AM
# script does the following changes to the DB:
# TODO: merge into xxxx-latest-mysqldiff.sql.
#--------------------------------------

DROP PROCEDURE IF EXISTS add_guids;

delimiter //


CREATE PROCEDURE add_guids ()
 BEGIN
  DECLARE table_name varchar(64) default null;
	
	DECLARE done INT DEFAULT 0;									
	
	#get all the tables in the current schema that do not have a guid column
  DECLARE cur_tabs CURSOR FOR 
		SELECT tabs.table_name
		FROM INFORMATION_SCHEMA.TABLES tabs
		WHERE tabs.table_schema = schema()
		 AND NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS cols
									WHERE cols.table_schema = schema() 
										and cols.COLUMN_NAME = 'guid' 
										and tabs.table_name = cols.table_name);								
	
	DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done = 1;
	
	
  OPEN cur_tabs;

  REPEAT
    FETCH cur_tabs INTO table_name;
    IF NOT done THEN
				# prepare stmt to alter table
				select concat('Altering ',table_name) from dual;
				SET @sql_text := concat('ALTER TABLE `',table_name,'` ADD COLUMN `guid` CHAR(36) DEFAULT NULL;');
				PREPARE stmt from @sql_text;
				EXECUTE stmt;
				DEALLOCATE PREPARE stmt;
				
				#prepare stmt to populate added column
				select concat('Populating  ',table_name) from dual;
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
		
 END;
//

delimiter ;


#-----------------------------------
# Clean up - Keep this section at the very bottom of diff script
#-----------------------------------

#DROP PROCEDURE IF EXISTS add_guids;