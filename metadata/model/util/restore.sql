drop database if exists openmrs;
create database openmrs default charset utf8;
use openmrs;

grant all on openmrs.* to test;

source openmrs_backup.sql;

