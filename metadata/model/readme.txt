metadata/model folder

Contains model-specific information, including SQL scripts,
model image, version information, etc.

openmrs-mysql-createdb.sql		MySQL script to generate OpenMRS database
openmrs-mysql-data.sql			MySQL script to populate tables
openmrs-mysql.sql				MySQL script for generating data model
readme.txt						this readme file
release-notes.txt				notes about model changes
openmrs_data_model_x.xx.png		data model images

--------

To install database:

	1. If this is the first time you're installing the database OR
	   if you have dropped the OpenMRS database, run openmrs-mysql-createdb.sql

	2. Run openmrs-mysql.sql to create tables

	3. Run openmrs-mysql-data.sql to populate tables with data

	The createdb script simply creates an OpenMRS database for you.
	If you want to install into a different database, create the database
	manually, make it the default database (e.g., "use mydb") and then
	run the second two scripts for tables/data.

--------

NOTE: Paul Biondich is in charge of this section