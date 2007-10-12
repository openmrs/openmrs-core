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
	  `record_id` int(11) NOT NULL auto_increment,
	  `guid` char(36) NOT NULL,
	  `creator` char(36) default NULL,
	  `database_version` char(8) default NULL,
	  `timestamp` datetime default NULL,
	  `retry_count` int(11) default NULL,
	  `state` varchar(20) default NULL,
	  `payload` longtext,
	  PRIMARY KEY  (`record_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
	  `resulting_record_guid` char(36) default NULL,
	  PRIMARY KEY  (`record_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	DROP TABLE IF EXISTS synchronization_server;
	CREATE TABLE `synchronization_server` (
	  `server_id` int(11) NOT NULL auto_increment,
	  `nickname` varchar(255) default NULL,
	  `address` varchar(255) NOT NULL,
	  `server_type` varchar(20) NOT NULL,
	  `username` varchar(255) default NULL,
	  `password` varchar(255) default NULL,
	  PRIMARY KEY  (`server_id`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;


 END;
//
delimiter ;
select 'Executing sync_static_data_procedure completed..' as ' ';
call sync_setup_procedure();
select 'Sync_setup_procedure completed.';
drop procedure sync_setup_procedure;