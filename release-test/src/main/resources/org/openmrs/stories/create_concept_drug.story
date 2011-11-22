GivenStories: org/openmrs/stories/login_to_website.story

Given I login to the openmrs application
Given I navigate to the the administration page
When I choose to manage concept drugs
And I choose to add a new concept drug
And I mention Triomune-100, triomune, 1.5, mg, 1.0 and 0.5
When I save the concept drug
Then the new drug should get saved