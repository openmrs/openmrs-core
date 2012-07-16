Scenario: Create and edit a concept

GivenStories: org/openmrs/stories/login_to_website.story

Given I am on Home screen
When I click on the Dictionary link
Then Take me to the Dictionary page

When I choose to Add new Concept
Then Take me to the Creating New Concept form
When I enter test as a fully specified name
And I click add Synonym button for en locale
And Type syn1 as the synonym name
And I click add Search term button for en locale
And Type term1 as the index term name
And I enter short1 as the short name
And I select Question as the concept class
And I check is set
And I select Boolean as the datatype
And I click Save Concept button
Then The concept should get created with a success message

When I click on the Dictionary link
When I search for a concept by typing aspirin and wait for the search hits
When I select ASPIRIN from the hits
When I choose to Edit
And I change the fully specified name to ASPIRIN
And I edit the synonym

And I edit the short name
And I change the concept class to Test
And I check/uncheck is set
And I change the datatype to Text
And I click Save Concept button
Then The concept should get saved with a success message