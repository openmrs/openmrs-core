GivenStories: org/openmrs/stories/go_to_admin_page.story

Given I choose to manage encounters
When I choose to add an encounter
And I enter Horatio L Hornblower, Super User, Unknown Location, 15/06/2011
And I save the encounter
Then the encounter should be saved

