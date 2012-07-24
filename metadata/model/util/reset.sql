drop database if exists openmrs;
create database openmrs default charset utf8;
use openmrs;

grant all on openmrs.* to test;

source openmrs_1.2.0-mysql.sql;

source openmrs_1.2.0-data-mysql.sql;
source openmrs_1.2.0-patient-data-mysql.sql;

source openmrs_1.2.0-to-latest-mysqldiff.sql;
