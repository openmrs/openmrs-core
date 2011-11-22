GivenStories: org/openmrs/stories/go_to_admin_page.story

Given I choose to manage roles
When I choose to add role
And I mention the role name, description and privileges as newrole, newrole description, Add Allergies respectively
And save
Then the role should be saved