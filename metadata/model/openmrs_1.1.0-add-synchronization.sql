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

	#--------------------------------------
	# Anders Gjendem 14 Aug 2007 9:00PM
	# Add table for Synchronization Journal
	#--------------------------------------
	DROP TABLE IF EXISTS synchronization_journal;

	CREATE TABLE `synchronization_journal` (
	  `guid` char(36) NOT NULL,
	  `timestamp` datetime default NULL,
	  `retry_count` int(11) default NULL,
	  `state` varchar(20) default NULL,
	  `payload` longtext,
	  PRIMARY KEY  (`guid`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

 END;
//
delimiter ;
select 'Executing sync_static_data_procedure completed..' as ' ';
call sync_setup_procedure();
select 'Sync_setup_procedure completed.';
drop procedure sync_setup_procedure;