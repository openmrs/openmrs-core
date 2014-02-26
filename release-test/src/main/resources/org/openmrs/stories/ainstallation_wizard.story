Scenario: Installation Wizard

Given I am on the OpenMRS Installation Wizard
When I select English as the language I prefer
And click on Continue
When I select the Advanced option
And click on Continue
Then take me to Step 1 of 5 of the installation wizard

Given I am on Step 1 of 5 of the installation wizard
When I enter a database url and mention the database name, username, password, and port as stored in system properties as database_name, mysql_username, mysql_password, and mysql_port
And click on Continue
Then take me to Step 2 of 5 of the installation wizard

Given I am on Step 2 of 5 of the installation wizard
When I mention username and password for the user with CREATE USER privileges as stored in system properties as mysql_username and mysql_password
And click on Continue
Then take me to Step 3 of 5 of the installation wizard

Given I am on Step 3 of 5 of the installation wizard
When I Continue
Then take me to Step 4 of 5 of the installation wizard

Given I am on Step 4 of 5 of the installation wizard
When I type Admin123 as the password confirm the same
And click on Continue
Then take me to Step 5 of 5 of the installation wizard

Given I am on Step 5 of 5 of the installation wizard
When I Continue
Then take me to Review of the installation wizard

Given I am on Review of the installation wizard
When I Continue
Then take me to login Page