Scenario: Find a patient

GivenStories: org/openmrs/stories/login_to_website.story

Given I am on Home screen
When I click on the Find/Create Patient link
Then take me to Find/Create Patient Page with Find Patient as title
