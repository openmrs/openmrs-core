GivenStories: org/openmrs/stories/login_to_website.story

Given I login to the openmrs application
Given I navigate to the the administration page
When I click on the Manage Locations link
When I click on the Add Location link
And I mention name Test Location and description description
And I save the location
Then the new location name should get saved

When I edit a location with name Test Location
And I enter retiring the location as retire reason
And I click on the button Retire this Location
Then the location should be retired

When I want to unretire the retired location
When I edit a location with name Test Location
And I unretire the location
Then the location should get unretired

When I want to unretire the retired location
When I check on Test Location
And I click on the button Delete Locations
