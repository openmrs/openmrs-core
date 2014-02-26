Scenario: retire unretire concept drug

GivenStories: org/openmrs/stories/login_to_website.story

Given I login to the openmrs application
Given I navigate to the the administration page
When I click on the Manage Concept Drugs link
And I edit a concept drug
And I provide a retire reason such as Retire
And I retire the concept drug
Then the concept drug should get retired
When I click on the Toggle Retired link
And I click on the d4T-30 link
And I click on the button Unretire this Drug

