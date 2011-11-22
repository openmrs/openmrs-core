GivenStories: org/openmrs/stories/login_to_website.story

Given I login to the openmrs application
Given I navigate to the the administration page
When I choose to click Manage Encounter Types
And I choose to add a new encounter type
And I mention name YOUTHINITIAL and description Outpatient Youth initial visit
When I save the encounter type
Then the new encounter type should be saved
