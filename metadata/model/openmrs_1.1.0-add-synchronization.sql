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
) ENGINE=InnoDB DEFAULT CHARSET=utf8
