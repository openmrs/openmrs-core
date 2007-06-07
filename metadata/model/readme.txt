metadata/model folder

Contains model-specific information, including SQL scripts,
model image, version information, etc.

openmrs_createdb-mysql.sql		MySQL script to drop/generate OpenMRS database
openmrs_x.xx-data-mysql.sql		MySQL script to populate tables
openmrs_x.xx-mysql.sql			MySQL script for generating data model
readme.txt						this readme file
release-notes.txt				notes about model changes
openmrs_data_model_x.xx.png		data model images

--------

To install database:

	1. If this is the first time you're installing the database OR
	   if you have dropped the OpenMRS database, run openmrs_createdb-mysql.sql

	2. Run openmrs_x.xx-mysql.sql to create tables

	3. Run openmrs_x.xx-data-mysql.sql to populate tables with data
		a) The first section of the script populates the *required* data
		b) The second section populates the *starter* data.  Comment this 
		   out if you are creating your own concepts/forms/etc

	4. Run openmrs_x.xx-to-latest-mysqldiff.sql to update 
	   the database schema (MUST BE DONE LAST)

	The createdb script simply creates an "openmrs" database for you.
	If you want to install into a different database, create the database
	manually, make it the default database (e.g., "use mydb") and then
	run the last three scripts for tables/data/updates.

--------

NOTE: Paul Biondich is in charge of this section