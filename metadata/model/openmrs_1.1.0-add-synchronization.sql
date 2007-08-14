#--------------------------------------
# USE:
# part of synchronization branch model updates: add table for Synchronization records
#--------------------------------------

#--------------------------------------
# Anders Gjendem 14 Aug 2007 10:00AM
#--------------------------------------

CREATE TABLE `synchronization_journal` (
  `guid` char(36) NOT NULL,
  `timestamp` datetime default NULL,
  `retry_count` int(11) default NULL,
  `state` varchar(20) default NULL,
  `payload` longtext,
  PRIMARY KEY  (`guid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8

#-----------------------------------
# Clean up - Keep this section at the very bottom of diff script
#-----------------------------------
DROP TABLE IF EXISTS synchronization;