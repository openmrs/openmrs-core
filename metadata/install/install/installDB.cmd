@ECHO OFF
ECHO **************************************
ECHO DATBASE INSTALL SCRIPT BEING PROCESSED
ECHO **************************************

ECHO Taken from http://openmrs.org/wiki/Step-by-Step_Installation_for_Developers
cd $INSTALL_PATH\model
ECHO **************************************
ECHO Creating Database
"$MySQL_Install_Directory\bin\mysql.exe" -u$MySQL_Admin_User -p$MySQL_Admin_Password < "$INSTALL_PATH\model\openmrs_createdb-mysql.sql"
ECHO **************************************
ECHO Installing Datamodel 1.0
"$MySQL_Install_Directory\bin\mysql.exe" -u$MySQL_Admin_User -p$MySQL_Admin_Password -D$Database_Name < "$INSTALL_PATH\model\openmrs_1.0.0-mysql.sql"
ECHO **************************************
ECHO Adding Data
"$MySQL_Install_Directory\bin\mysql.exe" -u$MySQL_Admin_User -p$MySQL_Admin_Password -D$Database_Name < "$INSTALL_PATH\model\openmrs_1.0.0-data-mysql.sql"
ECHO **************************************
ECHO Adding Patient Data
"$MySQL_Install_Directory\bin\mysql.exe" -u$MySQL_Admin_User -p$MySQL_Admin_Password -D$Database_Name < "$INSTALL_PATH\model\openmrs_1.0.0-patient-data-mysql.sql"
ECHO **************************************
ECHO Upgrading to Latest Datamodel - This will take some time...
"$MySQL_Install_Directory\bin\mysql.exe" -u$MySQL_Admin_User -p$MySQL_Admin_Password -D$Database_Name < "$INSTALL_PATH\model\openmrs_1.0.0-to-latest-mysqldiff.sql"
REM ECHO NOT DONE - UPGRADE FAILS WITH DATA

ECHO ******************
ECHO STARTING UP TOMCAT
ECHO ******************
NET START "apache tomcat"


ECHO ***********************
ECHO DEPLOYING WAR TO TOMCAT
ECHO ***********************
java -classpath "$INSTALL_PATH\bin" org.openmrs.installer.DeployTomcat $Tomcat_Admin_User $Tomcat_Admin_Password "http://localhost:8080/manager/deploy?path=/openmrs&war=$INSTALL_PATH\openmrs.war"
ECHO You can now login to OpenMRS with Username: admin Password: test
start http://localhost:8080/openmrs
