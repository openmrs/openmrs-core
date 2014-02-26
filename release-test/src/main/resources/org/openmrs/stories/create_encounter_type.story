Scenario: Create an encounter type

GivenStories: org/openmrs/stories/login_to_website.story

Given I login to the openmrs application
Given I navigate to the the administration page
When I choose to Manage Encounter Types
And I choose to Add Encounter Type
And I mention name YOUTHINITIAL and description Outpatient Youth initial visit
When I save the encounter type
Then the new encounter type should be saved
