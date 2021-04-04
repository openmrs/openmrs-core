# Creating Liquibase snapshots for OpenMRS
This document explains why Liquibase snapshots are introduced to OpenMRS and how versioning of database changes is 
affected by snapshots. It also contains instructions for creating new Liquibase snapshots, running them from the 
console and adding them to the OpenMRS code base.

## Why Liquibase snapshots?
OpenMRS uses [Liquibase](http://www.liquibase.org/index.html) to evolve its database model. The respective change sets 
have grown in the course of time and contain unneeded changes. E.g. there are tables or columns that are created, 
modified and eventually deleted as they are no longer used.

Large and partly outdated Liquibase change sets affect OpenMRS in two ways:

* Unneeded changes slow down the initialisation of OpenMRS
* Liquibase change sets are hard to read and understand as they contain unused code.

An alternative model is based on snapshots where historic change sets are consolidated into smaller change sets.

## Folder structure and naming conventions
This section describes the folder structure and naming conventions used for Liquibase snapshots and updates.

**Before** the introduction of snapshots, all Liquibase change log files were located in the resources folder 
of `openmrs-api`:

* `openmrs-core/api/src/main/resources`

**Since** the introduction of snapshots, the respective change log files are no longer stored in 
`openmrs-core/api/src/main/resources` 
but in the following subfolders:

*  `openmrs-core/api/src/main/resources/org/openmrs/liquibase/snapshots` contains all Liquibase snapshot files.

*  `openmrs-core/api/src/main/resources/org/openmrs/liquibase/updates` contains all Liquibase update files.

Version numbers are part of the change log file name: 

* `liquibase-update-to-latest-2.1.x.xml`.

## Versioning of database changes with snapshots
This section compares Liquibase files before and after the introduction of Liquibase snapshots.

### Change sets in [OpenMRS 2.1.x](https://github.com/openmrs/openmrs-core/tree/2.1.x/api/src/main/resources)
OpenMRS 2.1.x is the **last version that contains the full history** of database changes. The respective Liquibase 
files are:

* `org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-1.9.x.xml` defines the OpenMRS schema

* `org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-1.9.x.xml` defines core data for OpenMRS

* `org/openmrs/liquibase/updates/liquibase-update-to-latest-1.9.x/.xml` is an empty change log that was added so that 
change log version also comes with an update file

* `org/openmrs/liquibase/updates/liquibase-update-to-latest-2.0.x/.xml` contains all database changes introduced 
**until** OpenMRS 2.0, some changes go back to 2009

* `org/openmrs/liquibase/updates/liquibase-update-to-latest-2.1.x.xml` contains all database changes introduced 
**since** OpenMRS 2.0

### Change sets in OpenMRS 2.2.x
OpenMRS 2.2.x is the **first version using Liquibase snapshots**. Please note that this version did not exist at the 
time of writing this document.

* `org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.1.x.xml` defines the OpenMRS schema. This file 
is a **snapshot** generated from OpenMRS 2.1.x.

* `org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.1.x.xml` defines core data. Again, this file is 
a **snapshot** generated from OpenMRS 2.1.x.

* `org/openmrs/liquibase/updates/liquibase-update-to-latest-2.2.x.xml` contains database changes introduced by 
OpenMRS 2.2.x

### Change sets in (hypothetic) OpenMRS 4.8.x
Looking forward to a (hypothetic) version 4.8.x of OpenMRS, the respective change sets are:

* `org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-4.7.x.xml` defines the OpenMRS schema. This file 
is a **snapshot** generated from OpenMRS 4.7.x.

* `org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-4.7.x.xml` defines core data. Again, this file is 
a **snapshot** generated from OpenMRS 4.7.x.

* `org/openmrs/liquibase/updates/liquibase-update-to-latest-4.8.x.xml` contains database changes introduced by 
OpenMRS 4.8.x

### Further Liquibase files
The folder `openmrs-core/api/src/main/resources` contains further Liquibase files: 

* `liquibase-schema-only.xml` references the latest snapshot file for creating the OpenMRS schema and continues to
be used by other OpenMRS projects, e.g. openmrs-standalone.   

* `liquibase-core-data.xml` references the latest snapshot file for OpenMRS data and continues to
be used by other OpenMRS projects, e.g. openmrs-standalone.   

* `liquibase-update-to-latest.xml` references the latest update file for the OpenMRS database and continues to
be used by other OpenMRS projects, e.g. openmrs-standalone.   

* `liquibase-update-to-latest-from-1.9.x.xml` is used by integration tests and includes references to 
multiple `org/openmrs/liquibase/updates/liquibase-update-to-latest-a.b.x.xml` files.

* `liquibase-empty-changelog.xml` is used as a default Liquibase file by the org.openmrs.util.DatabaseUpdater class.

* `liquibase-update-to-latest-template.xml` is a template for creating new update files.

## When to generate Liquibase shapshots
Liquibase snapshots need to be created...

1. when a **new minor or major version** of OpenMRS is created (such as 2.3.x or 3.0.x), new snapshot files need to be 
generated for the **previous** versions. The new snapshot files are added to
 
    * `org/openmrs/liquibase/snapshots/schema-only` and 
    * `org/openmrs/liquibase/snapshots/core-data` 

   in the OpenMRS **master branch**. Do not forget to include the version number in the change log filenames.
  
   Liquibase updates introduced with the new 
version are added to

    * `org/openmrs/liquibase/updates`
   
   The examples for the (hypothetic) OpemMRS version 4.8.x further above illustrates the different version numbers to 
   use for the new change log files.

2. when a **database change is added to an existing minor or major version**, the snapshot files of later versions 
need to be updated so that they include the change.

## Generating and applying Liquibase snapshots
The pom file of the openmrs-liquibase module contains a template for generating Liquibase snapshots from an existing 
database and applying snapshots to an OpenMRS database.

### How to generate Liquibase snapshots
#### Step 1 - Drop your local OpenMRS schema
E.g. by running the script `openmrs-core/liquibase/scripts/drop_openmrs_schema.sql`:

	mysql -u root -p < openmrs-core/liquibase/scripts/drop_openmrs_schema.sql
	
Take care **NOT** to run this script on a production database.

#### Step 2 - Build and initialise OpenMRS
	cd <some root folder>/openmrs-core
	mvn clean install
	cd webapp
	rm -r ~/.OpenMRS
	mvn jetty:run

Open [http://localhost:8080/openmrs/initialsetup](http://localhost:8080/openmrs/initialsetup) and choose the following 
options:

* **simple installation** in step 2 of the installation wizard

* **not to add demo data** in step 3 of the installation wizard

#### Step 3 - Create snapshots of the OpenMRS database

Run the following commands to generate the Liquibase snapshots where `username` and `password` refer to a MySQL user:

	cd <some root folder>/openmrs-core/liquibase
	. scripts/create_liquibase_snapshots.sh <username> <password>
	
The following snapshot files are created:

* `openmrs-core/liquibase/snapshots/liquibase-schema-only-SNAPSHOT.xml`
* `openmrs-core/liquibase/snapshots/liquibase-core-data-SNAPSHOT.xml`

As an alternative to using the shell script, the snapshots can be created as follows:

	cd <some root folder>/openmrs-core/liquibase
	
	mvn \
	  -DoutputChangelogfile=liquibase-schema-only-SNAPSHOT.xml \
	  -Dusername=<database user> \
	  -Dpassword=<database password> \
	  liquibase:generateChangeLog
	  
	mvn \
	  -DdiffTypes=data \
	  -DoutputChangelogfile=liquibase-core-data-SNAPSHOT.xml \
	  -Dusername=<database user> \
	  -Dpassword=<database password> \
	  liquibase:generateChangeLog
	
#### Step 4 - Apply corrections to generated snapshot files

The generated Liquibase snapshot files need to be corrected. The appendix of this document contains a detailed description of the changes applied to the generated files.

Also, the OpenMRS license header needs to be added to both files.

This can be accomplished by running another utility script:

	cd <some root folder>/openmrs-core/liquibase
	. scripts/fix_liquibase_snapshots.sh

Alternatively, the corrections can be applied by running these commands:

	cd <some root folder>/openmrs-core/liquibase
	java -jar ./target/openmrs-liquibase-2.4.0-SNAPSHOT-jar-with-dependencies.jar
	
Please note that the jar file needs to be created *before* generating the Liquibase snapshots as the build process will detect that the generated files do not (yet) contain the OpenMRS license header.

### How to test Liquibase snapshots

Testing the (corrected) Liquibase snapshots comprises three steps:

* Drop the OpenMRS database
* Create an empty OpenMRS database
* Apply the (corrected) Liquibase snapshots to the new OpenMRS database

Take care **NOT** to execute these steps on a production database as the OpenMRS database is dropped.

#### Testing Liquibase snapshots with a utility script

All three steps can be accomplished by running a third utility script where `username` and `password` refer to a MySQL user:

	cd <some root folder>/openmrs-core/liquibase
	. scripts/test_liquibase_snapshots.sh <username> <password>

#### Testing Liquibase snapshots manually

As an alternative to using the utility script, apply the steps described below.

##### Step 1 - Drop your local OpenMRS schema
E.g. by running the script `drop_openmrs_schema.sql`:

	mysql -u root -p < openmrs-core/liquibase/scripts/drop_openmrs_schema.sql
	
Again, take care **NOT** to run this script on a production database.

##### Step 2 - Create an empty OpenMRS database
E.g. by running the script `create_openmrs_database.sql`:

	mysql -u root -p < openmrs-core/liquibase/scripts/create_openmrs_database.sql
	
The script creates the openmrs database and the tables `liquibasechangelog` and `liquibasechangeloglock`.
	
##### Step 3 - Use the snapshots to update the OpenMRS database 

Execute the following commands:

	cd <some root folder>/openmrs-core/liquibase

	mvn \
	  -Dchangelogfile=liquibase-schema-only-UPDATED-SNAPSHOT.xml  \
	  -Dusername=<database user> \
	  -Dpassword=<database password> \
	  liquibase:update

	mvn \
	  -Dchangelogfile=liquibase-core-data-UPDATED-SNAPSHOT.xml  \
	  -Dusername=<database user> \
	  -Dpassword=<database password> \
	  liquibase:update

One more time, take care **NOT** to run these commands on a production database.

### How to add snapshots to OpenMRS master
The new snapshots files are now ready to be added to the OpenMRS master branch. 

#### Step 1 - Add the new snapshot files 
Move the file `liquibase-schema-only-UPDATED-SNAPSHOT.xml` to `org/openmrs/liquibase/snapshots/schema-only` and 
rename it to `liquibase-schema-only-<major.minor>.x.xml`.

Similarly. move the file `liquibase-core-data-UPDATED-SNAPSHOT.xml` to `org/openmrs/liquibase/snapshots/core-data` and 
rename it to `liquibase-core-data-<major.minor>.x.xml`.

For example, when creating snapshots for version 2.2.x of OpenMRS, the resulting files are:

* `org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.2.x.xml`
* `org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.2.x.xml`
	
#### Step 2 - Create a new liquibase update file
In the folder `org/openmrs/liquibase/updates`, create an **empty** Liquibase change log file 
called `liquibase-update-to-latest-<major.minor+1>.x.xml`. 

Use `resources/liquibase-update-to-latest-template.xml` as a template for creating the new file.

For example, when adding snapshots from OpenMRS 2.2.x, the resulting file is:

* `org/openmrs/liquibase/updates/liquibase-updates-2.3.x.xml`

The minor version number of the new update change log is increased by one as this file contains all Liquibase change 
sets that are introduced 

* *after* OpenMRS version 2.2 was created 
* and *before* OpenMRS version 2.3 will be created

Include the new file in `resources/liquibase-update-to-latest-from-1.9.x.xml`, it is used by integration tests (as mentioned above).

#### Step 3 - Update references in legacy Liquibase change log files
The following files in `openmrs-core/api/src/main/resources` contain references to the latest Liquibase snapshot and 
update change logs and need to be updated after the new change log files were added:
* `liquibase-schema-only.xml`
* `liquibase-core-data.xml`
* `liquibase-update-to-latest.xml`

#### Step 4 - Make OpenMRS aware of the new versions
New snapshot and update versions need to be added to the `org.openmrs.liquibase.ChangeLogVersions` class. 

After adding the new change log files and updating the `ChangeLogVersions` class, run the 
test `org.openmrs.liquibase.ChangeLogVersionsTest` to ensure that the definition of change log versions and the actual 
change log files are in sync. The test fails if either versions are missing in the `ChangeLogVersions` class or if 
change log files are missing in the resource folder.

#### Step 5 - Validate Hibernate mappings

Run `org.openmrs.util.databasechange.ValidateHibernateMappingsDatabaseIT` to check whether the data types in the new 
liquibase files are compatible with the data types specified in the Hibernate mappings. 

The test can be run in two ways:

* By running `mvn clean test -Pskip-default-test -Pintegration-test -Dtest=ValidateHibernateMappingsIT2` in the console
* Alternatively, by running the test in IntelliJ or another IDE 

#### Step 6 - Build and initialise OpenMRS with the new snapshot and update files
Drop your local OpenMRS database and build and initialise OpenMRS as described in the section "How to generate 
Liquibase snapshots".

## How to run manual integration tests with older versions of OpenMRS

When introducing Liquibase snapshots, the integration of snapshots with older versions of OpenMRS had to be tested. 
This section describes how these tests were conducted, so that they can be repeated as and when needed.

#### Step 1 - Drop your local OpenMRS schema
E.g. by running the script `drop_openmrs_schema.sql`:

	mysql -u root -p < openmrs-core/liquibase/scripts/drop_openmrs_schema.sql
	
Take care **NOT** to run this script on a production database.

#### Step 2 - Delete the OpenMRS folder in your profile
	rm -r ~/.OpenMRS

Take care **NOT** to do that on a production environment.

#### Step 3 - Build, initialise and run an older OpenMRS version, e.g. 2.2.x
	cd <some root folder>/openmrs-core
	git checkout 2.2.x
	mvn clean install
	cd webapp
	mvn jetty:run

Open [http://localhost:8080/openmrs/initialsetup](http://localhost:8080/openmrs/initialsetup) and choose the following 
options:

* **simple installation** in step 2 of the installation wizard

* **not to add demo data** in step 3 of the installation wizard

#### Step 4 - Stop OpenMRS after the initialisation

#### Step 5 - Build, initialise and run a version of OpenMRS that uses Liquibase snapshots, e.g. master
	cd <some root folder>/openmrs-core
	git checkout master
	mvn clean install
	cd webapp
	mvn jetty:run

Open [http://localhost:8080/openmrs](http://localhost:8080/openmrs) and do the following steps:

* log in as administrator
* review the list of Liquibase change sets that need to be run to update the OpenMRS database
* run the pending change sets

#### Step 6 - Check the OpenMRS log 
The OpenMRS log file lists the change sets that were run. Validate that the expected change sets were executed.

## References
[https://issues.openmrs.org/browse/TRUNK-4830](https://issues.openmrs.org/browse/TRUNK-4830)

[http://www.liquibase.org/documentation/maven](http://www.liquibase.org/documentation/maven)

[https://dev.mysql.com/doc/refman/8.0/en/mysql-batch-commands.html](https://dev.mysql.com/doc/refman/8.0/en/mysql-batch-commands.html)

## Appendix

#### Corrections needed for `liquibase-schema-only-SNAPSHOT.xml`

After generating the file `liquibase-schema-only-SNAPSHOT.xml`, a few changes need to be applied to that file.

Change sets referring to **`liquibasechangelog`** and **`liquibasechangeloglock`** need to be removed.

The attribute **`value`** of the table **`clob_datatype_storage `** must be of type `CLOB` (and **not** `LONGTEXT`):

    <changeSet ...>
        <createTable tableName="clob_datatype_storage">
            ...
            <column name="value" type="CLOB">
            ...
        </createTable>
    </changeSet>

When creating snapshots, Liquibase version 3.x  uses the types `BIT` or `BIT(1)` for boolean attributes in a MySQL 
database. These types need to be changed to `BOOLEAN`:

    <changeSet ...>
        <createTable tableName="...">
            ...
            <column name="..." ... type="BOOLEAN">
            ...
        </createTable>
    </changeSet>

#### Corrections needed for `liquibase-core-data-SNAPSHOT.xml`

After generating the file `liquibase-core-data-SNAPSHOT.xml`, a few changes need to be applied to that file.

Change sets referring to **`liquibasechangelog`** and **`liquibasechangeloglock`** need to be removed.

The **order of change sets** in `liquibase-core-data-SNAPSHOT.xml` needs to be as follows:

1. `<databaseChangeLog ... \>`
2. ... `<insert tableName="person">` ...
3. ... `<insert tableName="users">` ...
4. ... `<insert tableName="care_setting">` ...
5. ... `<insert tableName="concept_class">` ...
6. ... `<insert tableName="concept_datatype">` ...
7. ... `<insert tableName="concept">` ...
8. ... followed by all other change sets as generated by liquibase

The **username and password** of the user with `name="system_id"` and  `value="admin"` need to have the same values as in
`org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.2.x.xml`:

	<changeSet author= ... >
		<insert tableName="users">
			<column name="user_id" valueNumeric="1"/>
			<column name="system_id" value="admin"/>
			<column name="username" value=""/>
			<column name="password" value="4a1750c8607dfa237de36c6305715c223415189"/>
			<column name="salt" value="c788c6ad82a157b712392ca695dfcf2eed193d7f"/>
            ...
		</insert>
        ...
	</changeSet>
