[![Build Status](https://secure.travis-ci.org/openmrs/openmrs-core.png?branch=master)](https://travis-ci.org/openmrs/openmrs-core)


OpenMRS is an patient-based medical record system focusing on giving providers a free customizable EMR.

Read more at http://openmrs.org/about

Find documentation on our wiki: http://wiki.openmrs.org

The project tree is set up as follows:

<table>
 <tr>
  <td>.settings</td>
  <td>Eclipse specific settings. Useful for character encoding and formatting (for devs using eclipse anyway)</td>
 </tr>
 <tr>
  <td>api/</td>
  <td>java and resource files for building the java api jar file.</td>
 </tr>
 <tr>
  <td>release-test</td>
  <td>Cucumber/selenium integration tests. Run daily against a running webapp. See https://wiki.openmrs.org/display/docs/Testing+Releases</td>
 </tr>
 <tr>
  <td>test</td>
  <td>TBD</td>
 </tr>
 <tr>
  <td>tools</td>
  <td>Meta code used during compiling and testing. Does not go into any released binary (like doclets)</td>
 </tr>
 <tr>
  <td>web/</td>
  <td>java and resource files that are used in the webapp/war file.</td>
 </tr>
 <tr>
  <td>webapp/</td>
  <td>jsp files used in building the war file.</td>
 </tr>
 <tr> 
  <td>build.properties</td>
  <td>(deprecated) default properties used by the deprecated ANT build.xml file</td>
 </tr>
 <tr>
  <td>build.xml</td>
  <td>(deprecated) ANT build file containing convenience methods into the maven build</td>
 </tr>
 <tr>
  <td>liquibase.build.xml</td>
  <td>ANT build file containing convenience methods to run liquibase actions</td>
 </tr>
 <tr>
  <td>openmrs-checkstyle.properties</td>
  <td>properties for the checkstyle library</td>
 </tr>
 <tr>
  <td>pom.xml</td>
  <td>The main maven file used to build and package OpenMRS</td>
 </tr>  
</table>