GivenStories: org/openmrs/stories/go_to_admin_page.story

When I click on the Manage Providers link
And I click on the Add Provider link
And I enter identifier, Attending Doctor
And I click on the button Save
Then the provider should be saved

When I click on the Administration link
And I click on the Manage Encounters link
And I click on the Add Encounter link
And I enter Horatio L Hornblower, Attending Doctor, Unknown Location, 15/06/2011 00:00, Unknown
And I save the encounter
Then the encounter should be saved

