# Creating Liquibase snapshots for OpenMRS
This document explains why Liquibase snapshots are introduced to OpenMRS and how versioning of database changes is affected by snapshots. It also contains instructions for creating new Liquibase snapshots, running them from the console and adding them to the OpenMRS code base.

## Why Liquibase snapshots?
OpenMRS uses [Liquibase](http://www.liquibase.org/index.html) to evolve its database model. The respective change sets have grown in the course of time and contain unneeded changes. E.g. there are tables or columns that are created, modified and eventually deleted as they are no longer used.

Large and partly outdated Liquibase change sets affect OpenMRS in two ways:

* Unneeded changes slow down the initialisation of OpenMRS
* Liquibase change sets are hard to read and understand as they contain unused code.

An alternative model is based on snapshots where historic change sets are consolidated into smaller change sets.

## Versioning of database changes with snapshots
This section compares Liquibase files before and after the introduction of Liquibase snapshots.

### Change sets in [OpenMRS 2.1.x](https://github.com/openmrs/openmrs-core/tree/2.1.x/api/src/main/resources)
OpenMRS 2.1.x is the **last version that contains the full history** of database changes. The respective Liquibase files are:

* `liquibase-schema-only.xml` defines the OpenMRS schema
* `liquibase-core-data.xml` defines core data
* `liquibase-update-to-latest.xml` includes the next two files
* `liquibase-update-to-2.0.xml` contains all database changes introduced **until** OpenMRS 2.0, some changes go back to 2009
* `liquibase-update-to-2.1.xml` contains all database changes introduced **since** OpenMRS 2.0

### Change sets in OpenMRS 2.2.x
OpenMRS 2.2.x is the **first version using Liquibase snapshots**. Please note that this version did not exist at the time of writing this document.

* `liquibase-schema-only-2.1.xml` defines the OpenMRS schema. This file is a **snapshot** generated from OpenMRS 2.1.x.
* `liquibase-core-data-2.1.xml` defines core data. Again, this file is a **snapshot** generated from OpenMRS 2.1.x.
* `liquibase-update-to-latest.xml` contains database changes introduced by OpenMRS 2.2.x

### Change sets in (hypothetic) OpenMRS 2.7.x
Looking forward to a (hypothetic) version 2.7.x of OpenMRS, the respective change sets are:

* `liquibase-schema-only-2.6.xml` defines the OpenMRS schema. This file is a **snapshot** generated from OpenMRS 2.6.x.
* `liquibase-core-data-2.6.xml` defines core data. Again, this file is a **snapshot** generated from OpenMRS 2.6.x.
* `liquibase-update-to-latest.xml` contains database changes introduced by OpenMRS 2.7.x

## When to generate Liquibase shapshots
Liquibase snapshots need to be created...

1. when a **new minor or major version** of OpenMRS is created (such as 2.3.x or 3.0.x), new snapshot files need to be generated from these versions. The new snapshot files replace existing snapshot file in the OpenMRS **master branch**.
2. when a **database change is added to an existing minor or major version**, the snapshot files of later versions need to be updated so that they include the change.

## Generating and applying Liquibase snapshots
The pom file of the openmrs-liquibase module contains a template for generating Liquibase snapshots from an existing database and applying snapshots to an OpenMRS database.

### How to generate Liquibase snapshots
#### Step 1 - Drop your local OpenMRS schema
E.g. by running this script:

	openmrs-core/liquibase/scripts/drop_openmrs_schema.sql
	
Take care **not** to run this script on a production database.

#### Step 2 - Build and initialise OpenMRS
	cd <some root folder>/openmrs-core
	mvn clean install
	cd webapp
	rm -r ~/.OpenMRS
	mvn jetty:run

Open [http://localhost:8080/openmrs/initialsetup](http://localhost:8080/openmrs/initialsetup) and choose the following options:

* **simple installation** in step 2 of the installation wizard

* **not to add demo data** in step 3 of the installation wizard

#### Step 3 - Create a snapshot of the OpenMRS schema
	mvn \
	  -DoutputChangelogfile=liquibase-schema-only-SNAPSHOT.xml \
	  -Dusername=<database user> \
	  -Dpassword=<database password> \
	  liquibase:generateChangeLog
	
The file `liquibase-schema-only-SNAPSHOT.xml` is created in the folder `<some root folder>/openmrs-core/api/src/main/resources`

#### Step 4 - Create a snapshot of the OpenMRS core data
	mvn \
	  -DdiffTypes=data \ 
	  -DoutputChangelogfile=liquibase-core-data-SNAPSHOT.xml \
	  -Dusername=<database user> \
	  -Dpassword=<database password> \
	  liquibase:generateChangeLog
	
The file `liquibase-core-data-SNAPSHOT.xml` is also created in the folder `<some root folder>/openmrs-core/api/src/main/resources`

#### Step 5 - Remove references to liquibase tables
In both files, search for `liquibasechangelog` and `liquibasechangeloglock` and remove the respective change sets.

#### Step 6 - Add the OpenMRS license header to new files
The header can be copied from an existing file.

### How to test Liquibase snapshots
#### Step 1 - Drop your local OpenMRS schema
E.g. by running this script:

	openmrs-core/liquibase/scripts/drop_openmrs_schema.sql
	
Again, take care **not** to run this script on a production database.

#### Step 2 - Create an empty OpenMRS database
E.g. by running this script:

	openmrs-core/liquibase/scripts/create_openmrs_database.sql
	
#### Step 3 - Use the snapshots to update the OpenMRS database 
Execute

	mvn \
	  -Dchangelogfile=liquibase-schema-only-SNAPSHOT.xml  \
	  -Dusername=<database user> \
	  -Dpassword=<database password> \
	  liquibase:update

and 

	mvn \
	  -Dchangelogfile=liquibase-core-data-SNAPSHOT.xml  \
	  -Dusername=<database user> \
	  -Dpassword=<database password> \
	  liquibase:update

### How to add snapshots to OpenMRS master
The new snapshots files are now ready to be added to the OpenMRS master branch. 

#### Step 1 - Update snapshot files 
Copy the files to the master branch and rename them so that their names reflect the OpenMRS version the snapshots were generated from.  

For example, when generating snapshots from OpenMRS 2.1.x, files are renamed to `liquibase-schema-only-2.1.xml` and `liquibase-core-data-2.1.xml`. 

Remove previous Liquibase snapshots from the master branch.

#### Step 2 - Update initialization filter
Update the[`org.openmrs.web.filter.initialization.InitializationFilter`](https://github.com/openmrs/openmrs-core/blob/13ddf0df1350d16f93085950cfe2a9f35532ba8e/web/src/main/java/org/openmrs/web/filter/initialization/InitializationFilter.java) class so that it references the new snapshot files. E.g. when adding snapshots for OpenMRS 2.1.x to master, the class needs to be changed as follows:

	...
	public class InitializationFilter extends StartupFilter {
		...
		private static final String LIQUIBASE_SCHEMA_DATA = "liquibase-schema-only-2.1.xml";	
		private static final String LIQUIBASE_CORE_DATA = "liquibase-core-data-2.1.xml";
		...
	}
	
#### Step 3 - Build and initialise OpenMRS
Drop your local OpenMRS database and build and initialise OpenMRS as described in the section "How to generate Liquibase snapshots".


## References
[https://issues.openmrs.org/browse/TRUNK-4830](https://issues.openmrs.org/browse/TRUNK-4830)

[http://www.liquibase.org/documentation/maven](http://www.liquibase.org/documentation/maven)

[https://dev.mysql.com/doc/refman/8.0/en/mysql-batch-commands.html](https://dev.mysql.com/doc/refman/8.0/en/mysql-batch-commands.html)
