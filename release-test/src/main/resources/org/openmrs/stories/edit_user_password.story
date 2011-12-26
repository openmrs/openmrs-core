GivenStories: org/openmrs/stories/login_to_website.story,org/openmrs/stories/go_to_admin_page.story,org/openmrs/stories/create_a_user.story,org/openmrs/stories/logout_of_website.story

Given I login to the openmrs application with username admin and password Admin123
Given I navigate to the the administration page
When I click on the Manage Users
And I search for user john
And I chose to edit the user
And I changed the Openmr5tw, Openmr5tw
And I save the user
Then the user's password should be changed
