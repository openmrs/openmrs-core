Scenario: Change role privileges

GivenStories: org/openmrs/stories/go_to_admin_page.story

When I choose to Manage Roles
When I choose to Add Role
And I mention the role name, description and privileges as newrole, newrole description, Add Allergies respectively
And save
Then the role should be saved


Given I am viewing the list of roles
When I edit a role with the name newrole
And change the privilege from Add Allergies to Add Cohorts
And save the role
Then the role newrole should be saved and it should not have Add Allergies privilege but should have Add Cohorts privilege