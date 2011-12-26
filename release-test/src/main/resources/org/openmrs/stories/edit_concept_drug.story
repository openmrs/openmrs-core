GivenStories: org/openmrs/stories/login_to_website.story,org/openmrs/stories/go_to_admin_page.story

When I click on the Manage Concept Drugs
When I click on the edit option
And I change Triomune-100, triomune, 1.5, mg, 1.0 and 0.5
When I save the concept drug
Then the changes to the drug should be saved
