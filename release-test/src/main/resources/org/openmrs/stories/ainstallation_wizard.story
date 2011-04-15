Scenario: 

Given I am on the OpenMRS Installation Wizard
When I select the Advanced option
And Continue
Then take me to Step 1 of 5 of the installation wizard

Given I am on Step 1 of 5 of the installation wizard
When I enter a database url and mention openmrs as the database, openmrs as the user name and test as the password
And Continue
Then take me to Step 2 of 5 of the installation wizard

Given I am on Step 2 of 5 of the installation wizard
When I mention openmrs as user name and test as password for the user with CREATE USER privileges
And Continue
Then take me to Step 3 of 5 of the installation wizard

Given I am on Step 3 of 5 of the installation wizard
When I Continue
Then take me to Step 4 of 5 of the installation wizard

Given I am on Step 4 of 5 of the installation wizard
When I type Admin123 as the password confirm the same
And Continue
Then take me to Step 5 of 5 of the installation wizard

Given I am on Step 5 of 5 of the installation wizard
When I Continue
Then take me to Review of the installation wizard

Given I am on Review of the installation wizard
When I Finish
Then take me to login Page