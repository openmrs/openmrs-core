#Run once prior to other scripts to create
#the OpenMRS database.  Alternatively, if
#a database and user already exists, this step may
#be skipped.

drop database if exists openmrs;
create database openmrs default charset utf8;
use openmrs;

create user test identified by 'test';
grant all on openmrs.* to test;