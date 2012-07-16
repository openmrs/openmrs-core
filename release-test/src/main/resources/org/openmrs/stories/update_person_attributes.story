Scenario: Update person attributes

GivenStories: org/openmrs/stories/go_to_admin_page.story

When I click on the Manage Person Attribute Types link
And I click on the Birthplace link
And I change the attribute description to Location of persons birth
And I save the attribute type
Then display message Person Attribute Type saved
