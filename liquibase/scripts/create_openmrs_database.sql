--
--    This Source Code Form is subject to the terms of the Mozilla Public License,
--    v. 2.0. If a copy of the MPL was not distributed with this file, You can
--    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
--    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
--
--    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
--    graphic logo is a trademark of OpenMRS Inc.
--
create database openmrs;
show databases;

use openmrs;

DROP TABLE IF EXISTS `liquibasechangelog`;
CREATE TABLE `liquibasechangelog` (
  `ID` varchar(255) NOT NULL,
  `AUTHOR` varchar(255) NOT NULL,
  `FILENAME` varchar(255) NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int(11) NOT NULL,
  `EXECTYPE` varchar(10) NOT NULL,
  `MD5SUM` varchar(35) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `COMMENTS` varchar(255) DEFAULT NULL,
  `TAG` varchar(255) DEFAULT NULL,
  `LIQUIBASE` varchar(20) DEFAULT NULL,
  `CONTEXTS` varchar(255) DEFAULT NULL,
  `LABELS` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `liquibasechangeloglock`;
CREATE TABLE `liquibasechangeloglock` (
  `ID` int(11) NOT NULL,
  `LOCKED` tinyint(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

show tables;
