GivenStories: org/openmrs/stories/login_to_website.story


Given I login to the openmrs application
Given I navigate to the the administration page
When I click on the Manage Locations link
And I edit a location
And I mention name Unknown Location and description description
And I save the location
Then the new location name should get saved
