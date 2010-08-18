#!/bin/bash 

# mavenize.sh
# Bash script to convert OpenMRS trunk to a Maven multi-module project
# Author: Matthew Blanchette

########## WEBAPP MODULE ##########

echo "Creating webapp module directory structure"
svn mkdir webapp/src/main/resources --parents

echo "Copying web directory to webapp module"
svn copy web webapp/src/main/webapp

echo "Copying web resource to webapp module"
svn copy web/WEB-INF/themes webapp/src/main/resources

echo "Removing moved web resource in webapp module"
svn delete webapp/src/main/webapp/WEB-INF/themes

echo "Moving metadata files to webapp module"
svn move metadata/model/liquibase-data.zip webapp/src/main/resources
svn move metadata/api/log4j/velocity.properties webapp/src/main/resources
svn move metadata/api/log4j/log4j.xml webapp/src/main/resources

########## WEB MODULE ##########

echo "Creating web module directory structure"
svn mkdir web/src/main/javadoc --parents
svn mkdir web/src/test

echo "Copying src and test web to web module resources"
svn copy src/web web/src/main/resources
svn copy test/web web/src/test/resources

echo "Removing java and html files from web module resources"
for f in `find web/src/main/resources -not \( -name .svn -prune \) -type f \( -name "*.java" -or -name "*.html" \)`; do
  svn delete $f
done
for f in `find web/src/test/resources -not \( -name .svn -prune \) -type f \( -name "*.java" -or -name "*.html" \)`; do
  svn delete $f
done

echo "Moving src and test web to web module java"
svn move src/web web/src/main/java
svn move test/web web/src/test/java

echo "Removing non-java and non-html files from web module java"
for f in `find web/src/main/java -not \( -name .svn -prune \) -type f -not \( -name "*.java" -or -name "*.html" \)`; do
  svn delete $f
done
for f in `find web/src/test/java -not \( -name .svn -prune \) -type f -not \( -name "*.java" -or -name "*.html" \)`; do
  svn delete $f
done

echo "Copying web resource to web module"
svn copy web/WEB-INF/openmrs-servlet.xml web/src/main/resources

echo "Removing moved web resource in webapp module"
svn delete webapp/src/main/webapp/WEB-INF/openmrs-servlet.xml

echo "Copying metadata file to web module"
svn copy metadata/images web/src/main/javadoc/resources

echo "Removing empty src directories from web module"
for f in `find web/src -type d -name ".svn"`; do
 parent=$f/..
 childrencount=`find $parent -not \( -name .svn -prune \) -type f | wc -l`
 if [ "$childrencount" == "0" ]; then
  echo "Removing empty directory $parent"
  svn delete $parent
 fi
done

########## API MODULE ##########

echo "Creating api module directory structure"
svn mkdir api/src/main/javadoc --parents
svn mkdir api/src/main/antlr
svn mkdir api/src/test

echo "Copying resource to antlr in api module"
svn copy src/api/org/openmrs/arden/ArdenRecognizer.g api/src/main/antlr

echo "Copying src and test api to api module resources"
svn copy src/api api/src/main/resources
svn copy test/api api/src/test/resources

echo "Removing java and html files from api module resources"
for f in `find api/src/main/resources -not \( -name .svn -prune \) -type f \( -name "*.java" -or -name "*.html" \)`; do
  svn delete $f
done
for f in `find api/src/test/resources -not \( -name .svn -prune \) -type f \( -name "*.java" -or -name "*.html" \)`; do
  svn delete $f
done

echo "Copying test resource from test api java sources"
svn copy test/api/org/openmrs/test/TestingApplicationContext.xml api/src/test/resources

echo "Removing moved test resource in api module"
svn delete api/src/test/resources/org/openmrs/test/TestingApplicationContext.xml

echo "Moving src and test api to api module java"
svn move src/api api/src/main/java
svn move test/api api/src/test/java

echo "Removing non-java and non-html files from api module java"
for f in `find api/src/main/java -not \( -name .svn -prune \) -type f -not \( -name "*.java" -or -name "*.html" \)`; do
  svn delete $f
done
for f in `find api/src/test/java -not \( -name .svn -prune \) -type f -not \( -name "*.java" -or -name "*.html" \)`; do
  svn delete $f
done

echo "Moving metadata files to api module"
# Move hibernate files, keeping packaging directories for hbm.xml mapping files
hibernatedir=metadata/api/hibernate
for f in `find $hibernatedir -not \( -name .svn -prune \) -type f`; do 
 svn move $f api/src/main/resources${f/$hibernatedir/}
done
svn move metadata/api/spring/applicationContext-service.xml api/src/main/resources
svn move metadata/model/liquibase-update-to-latest.xml api/src/main/resources
svn move metadata/model/archive/update-to-1.4.2.01-db.mysqldiff.sql api/src/main/resources
svn move metadata/api/log4j/junit/log4j.xml api/src/test/resources
svn move metadata/images api/src/main/javadoc/resources

echo "Removing empty src directories from api module"
for f in `find api/src -type d -name ".svn"`; do
 parent=$f/..
 childrencount=`find $parent -not \( -name .svn -prune \) -type f | wc -l`
 if [ "$childrencount" == "0" ]; then
  echo "Removing empty directory $parent"
  svn delete $parent
 fi
done

########## CLEANUP ##########

echo "Removing original trunk directories and files"
svn delete .settings
svn delete .classpath
svn delete .project
svn delete lib
# Currently not removing metadata dir for checkstyle and sql files
#svn delete metadata
svn delete src
svn delete test

# Removing all files and dirs from /web except /web/src
# Web directory repurposed into web module
for f in web/*; do
 if [ $f != "web/src" ]; then
  echo "Removing from web directory $f"
  svn delete $f 
 fi
done

########## SVN PROPS ##########

echo "Updating svn ignore properties in modules"

echo "Ignoring Maven target and eclipse files in webapp module"
svn propset svn:ignore "target
.settings
.classpath
.project
" webapp

echo "Ignoring Maven target and eclipse files in web module"
svn propset svn:ignore "target
.settings
.classpath
.project
" web

echo "Ignoring Maven target, eclipse files, and velocity log in api module"
svn propset svn:ignore "target
.settings
.classpath
.project
velocity.log
" api

echo "Ignoring Maven target in parent dir"
# Get existing ignore properties
# Remove carriage returns for consistent line style
props=`svn propget svn:ignore . | tr -d "\r"`
svn propset svn:ignore "$props
target
" .

########## EOF ##########