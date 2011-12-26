GivenStories: org/openmrs/stories/create_a_role.story

Given I am viewing the list of roles
When I delete a role with the name newrole
Then the role newrole should be deleted