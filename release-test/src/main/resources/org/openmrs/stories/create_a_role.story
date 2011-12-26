GivenStories: org/openmrs/stories/go_to_admin_page.story

When I choose to Manage Roles
When I choose to Add Role
And I mention the role name, description and privileges as newrole, newrole description, Add Allergies respectively
And save
Then the role should be saved