GivenStories: org/openmrs/stories/login_to_website.story

Given I login to the openmrs application
Given I navigate to the the administration page
When I click on the Manage Locations link
And I chose to edit a location
And I mention the retired reason as retireReason
And I retire the location
Then the location should get retired
