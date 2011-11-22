GivenStories: org/openmrs/stories/go_to_admin_page.story

Given I am on the manage users page
When I choose to add a user
And I choose to create a new person
And I enter John, F, Kennedy, M, john, Openmr5tw, Openmr5tw
And I save the user
Then the user should be saved/created
