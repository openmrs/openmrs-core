Scenario: Create and edit a concept drug

GivenStories: org/openmrs/stories/go_to_admin_page.story

When I choose to Manage Concept Drugs
And I choose to Add Concept Drug
And I mention Triomune-100, triomune, 1.5, mg, 1.0 and 0.5
When I save the concept drug
Then the new drug should get saved

When I click on the Manage Concept Drugs
When I click on the edit option
And I change Triomune-100, triomune, 1.5, mg, 1.0 and 0.5
When I save the concept drug
Then the changes to the drug should be saved