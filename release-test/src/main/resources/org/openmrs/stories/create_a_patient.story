GivenStories: org/openmrs/stories/login_to_website.story

Given I am on Home screen
When I click on the Find/Create Patient link
Then take me to Find/Create Patient Page with Find Patient as title

Given I am on the Find/Create Patient Page
When I enter Robert as Name
And I enter 32 as Age
And I select Male as Gender
And I click on the createPerson button
Then take me to the Create Patient Page


Given I am on the Create Patient Page
When I enter the Clive as the family name
And I enter 123456 as Identifier Code
And I select Old Identification Number as Identifier Type with index 1
And I select Unknown Location as location with index 1
And I enter address as address
And I enter India as country
And I click on Save button
Then take me to Patient dashboard page with title Patient Dashboard




