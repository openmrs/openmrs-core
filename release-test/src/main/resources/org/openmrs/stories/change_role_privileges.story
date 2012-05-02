GivenStories: org/openmrs/stories/create_a_role.story

Given I am viewing the list of roles
When I edit a role with the name newrole
And change the privilege from Add Allergies to Add Cohorts
And save the role
Then the role newrole should be saved and it should not have Add Allergies privilege but should have Add Cohorts privilege