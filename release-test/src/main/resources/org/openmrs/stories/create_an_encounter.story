Scenario: Create an encounter

GivenStories: org/openmrs/stories/go_to_admin_page.story

When I click on the Manage Providers link
And I click on the Add Provider link
And I enter identifier, Attending Nurse
And I click on the button Save
Then the provider should be saved

When I click on the Administration link
And I click on the Manage Encounters link
And I click on the Add Encounter link
And I enter Horatio L Hornblower, Attending Nurse, Unknown Location, 06/15/2012 00:00, Unknown
And I save the encounter
Then the encounter should be saved

When I click on the Admin link
And I click on the Manage Providers link
And I enter Attending Nurse as provider name
When I select identifier from provider search results
And I enter retiring the provider as retired reason
And I click on the button Retire
Then the provider should be retired

