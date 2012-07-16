Scenario: Edit user password

GivenStories: org/openmrs/stories/create_a_user.story

Given I navigate to the the administration page
When I click on the Manage Users
And I search for user john
And I chose to edit the user
And I changed the Openmr5tw, Openmr5tw
And I save the user
Then the user's password should be changed
