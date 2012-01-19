GivenStories: org/openmrs/stories/go_to_admin_page.story

When I click on the Manage Providers link
And I click on the Add Provider link
And I enter identifier, Attending Doctor
And I click on the button Save
Then the provider should be saved

When I select identifier from provider search results
And I enter retiring the provider as retired reason
And I click on the button Retire
Then the provider should be retired

