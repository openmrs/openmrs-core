Scenario: Login to website

Given I am on the login page of OpenMRS
When I enter username and password as stored in system properties as openmrs_username and openmrs_password and click the 'Log In' button
Then take me to the Home screen and display welcome message for user Super
