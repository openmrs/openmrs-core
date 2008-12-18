#!/bin/sh

# Unix based shell script to run the update-to-latest.sql
# script against each of the .sql files in /metadata/model
#
# WARNING: This will drop and recreate the mysql database named 'openmrs'
#

# user defined variables
export dbuser=root
export dbname=openmrs

# Ask for the current version of the files
echo -n "Current version of the files (e.g: 1.3.2): "
read currentversion

# Ask for the current version of the files
echo -n "New version of the files (e.g: 1.3.3): "
read newversion

# Ask for the user's database password
echo -n "MySQL $dbuser password: "
stty -echo
read dbpass
stty echo

# Add a break after the user input
echo ""

# Do the createdb script first so that the '$dbname' database is dropped and recreated
echo Creating createdb sql file
mysql -u$dbuser -p$dbpass -e"drop database if exists $dbname; create database openmrs default character set utf8" -Dopenmrs
mysql -u$dbuser -p$dbpass -e"source $currentversion-createdb-from-scratch-with-demo-data.sql"
mysql -u$dbuser -p$dbpass -e"source update-to-latest-db.mysqldiff.sql" -D$dbname
echo Running it again
mysql -u$dbuser -p$dbpass -e"source update-to-latest-db.mysqldiff.sql" -D$dbname
mysqldump -u$dbuser -p$dbpass -e -q --add-drop-database --skip-add-locks --skip-add-drop-table -N -r"./$newversion-createdb-from-scratch-with-demo-data.sql" --databases $dbname

# Note that this mysqldump has the -d flag to ignore the rows (because this file is table schema only)
echo Creating schema only sql file
mysql -u$dbuser -p$dbpass -e"drop database if exists $dbname; create database openmrs default character set utf8" -Dopenmrs
mysql -u$dbuser -p$dbpass -e"source $currentversion-schema-only.sql" -D$dbname
mysql -u$dbuser -p$dbpass -e"source update-to-latest-db.mysqldiff.sql" -D$dbname
mysql -u$dbuser -p$dbpass -e"source update-to-latest-db.mysqldiff.sql" -D$dbname
mysqldump -u$dbuser -p$dbpass -e -q -d --add-drop-table --skip-add-locks -N -r"./$newversion-schema-only.sql" $dbname

echo Creating schema with core and demo data sql file
mysql -u$dbuser -p$dbpass -e"drop database if exists $dbname; create database openmrs default character set utf8" -Dopenmrs
mysql -u$dbuser -p$dbpass -e"source $currentversion-schema-with-core-and-demo-data.sql" -D$dbname
mysql -u$dbuser -p$dbpass -e"source update-to-latest-db.mysqldiff.sql" -D$dbname
mysql -u$dbuser -p$dbpass -e"source update-to-latest-db.mysqldiff.sql" -D$dbname
mysqldump -u$dbuser -p$dbpass -e -q --add-drop-table --skip-add-locks -N -r"./$newversion-schema-with-core-and-demo-data.sql" $dbname

echo Creating schema with core data only sql file
mysql -u$dbuser -p$dbpass -e"drop database if exists $dbname; create database openmrs default character set utf8" -Dopenmrs
mysql -u$dbuser -p$dbpass -e"source $currentversion-schema-with-core-data.sql" -D$dbname
mysql -u$dbuser -p$dbpass -e"source update-to-latest-db.mysqldiff.sql" -D$dbname
mysql -u$dbuser -p$dbpass -e"source update-to-latest-db.mysqldiff.sql" -D$dbname
mysqldump -u$dbuser -p$dbpass -e -q --add-drop-table --skip-add-locks -N -r"./$newversion-schema-with-core-data.sql" $dbname

#drop database if exists $dbname;
#create database $dbname default charset utf8;
#use $dbname;
echo Done.

echo ""
echo ""
echo WARNING!!!
echo WARNING!!!
echo WARNING!!!
echo Change the \"create database\" line to: \"create database $dbname default character set utf8\;\"
echo You will also have to put the following lines into the createdb-from-scratch after that \"create database...\" statement
echo DELETE FROM mysql.user WHERE User=\'test\'\;
echo flush privileges\;
echo CREATE USER \'test\'@\'localhost\' IDENTIFIED BY \'test\'\;
echo GRANT ALL ON $dbname.* TO test\;
echo ""
echo Also change the \"schema only\" file to have : \"INSERT INTO global_property VALUES \(\'database_version\',\'1.x.x.xx\',NULL\)\;\" with the __latest_version__

echo ""
