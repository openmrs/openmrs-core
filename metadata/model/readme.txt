metadata/model folder

Contains model-specific information, including SQL scripts,
model image, version information, etc.

openmrs_x.yz-data-mysql.sql				MySQL script to populate tables
openmrs_x.yz-mysql.sql					MySQL script for generating data model
openmrs_x.yz-patient-data-mysql.sql		MySQL script for adding a lot of patients to the db
openmrs_x.yz-to-latest-mysqldiff.sql	MySQL script for upgrading any x.yz version to the latest x.y.ZZ version
openmrs_createdb-mysql.sql				MySQL script to drop/generate OpenMRS database
readme.txt							this readme file
release-notes.txt					notes about model changes
openmrs_data_model_x.yz.png			data model image

--------

To install database:

	1. If this is the first time you're installing the database OR
	   if you have dropped the OpenMRS database, run openmrs_createdb-mysql.sql

	2. Run openmrs_x.yz-mysql.sql to create tables

	3. Run openmrs_x.yz-data-mysql.sql to populate tables with data
		a) The first section of the script populates the *required* data
		b) The second section populates the *starter* data.  Comment this 
		   out if you are creating your own concepts/forms/etc

	4. Run openmrs_x.yz-to-latest-mysqldiff.sql to update 
	   the database schema (MUST BE DONE LAST)

	The createdb script simply creates an "openmrs" database for you.
	If you want to install into a different database, create the database
	manually, make it the default database (e.g., "use mydb") and then
	run the last three scripts for tables/data/updates.

--------

NOTE: Paul Biondich is in charge of this section