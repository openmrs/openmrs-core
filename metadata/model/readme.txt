The metadata/model folder contains database model specific information.

liquibase-data.zip...................Liquibase xml files to create a database schema, core data, and demo data
                                     Used by the OpenMRS InitializationFilter to create a database if one does
                                     not exist and the user has no openmrs runtime properties file defined.
liquibase-update-to-latest.xml.......Run by openmrs at startup to keep the user's database up to date with the
									 latest code installed.  This file is packaged with the openmrs api jar 
readme.txt...........................The file you're reading right now
util/................................Utility scripts to work on the database

--------

The scripts in liquibase-data.zip are not usually used alone.  Install the war file into tomcat and a wizard 
will walk you through creating and populating a database.  
(as long as you don't have a runtime properties file defined)

--------

The liquibase-update-to-latest.xml file is run on the connected database everytime the API and/or the webapp is 
started.

--------

Do not modify the schema in the liquibase-data.zip files unless you are preparing a new release.  The files
should contain the same schema through a development lifecycle and are only changed right after a release
by the release manager.

--------  

To create a .sql file, use the ant build.xml file:
  "ant liquibase-create-sql-diff"
  ...
  (TODO: enumerate the liquibase ant targets here)
--------

NOTE: Paul Biondich is in charge of this section