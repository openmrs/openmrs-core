metadata/model folder

Contains model-specific information, including SQL scripts,
model image, version information, etc.

x.y.0-createdb-from-scratch-with-demo-data.sql	Drops the current "openmrs" database and creates a new one with demo data
x.y.0-schema-only.sql							Uses currently selected database to create the tables
x.y.0-schema-with-core-and-demo-data.sql		Uses currently selected database to create tables and demo data
x.y.0-schema-with-core-data.sql					Uses currently selected database to create tables and the very base data
readme.txt										This file
test-data-conventions.sql						Utility script to test some expected conventions in your database
update-to-latest-db.mysqldiff.sql				Updates any openmrs version to the latest version

--------

Install the database in 2 easy steps:

	1. Choose one of the x.y.0________.sql files according to your
	   situation.  See descriptions above.
	   
	   1a) If x.y.0-createdb____.sql, type this at the command line:
	   		mysql -uroot -p -e"source x.y.0-createdb-from-scratch-with-demo-data.sql"
	   		
	   1b) If x.y.0-schema___.sql, type these lines at the command line:
	   		mysql -uroot -p
	   		create database custom_openmrs_db default character set utf8;
	   		use custom_openmrs_db;
	   		source x.y.0-schema_____.sql
	   		
	2. Run update-to-latest-db.mysqldiff.sql (MUST BE DONE LAST)
			mysql -uroot -p -e"source update-to-latest-db.mysqldiff.sql" -Dopenmrs

--------

NOTE: Paul Biondich is in charge of this section