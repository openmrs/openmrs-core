GivenStories: org/openmrs/stories/login_to_website.story

When I navigate to the Dictionary page
When I search for a concept by typing aspirin and wait for the search hits
When I select ASPIRIN from the hits
Then Take me to the viewing concept page
When I choose to Edit
And I change the fully specified name to ASPIRIN
And I edit the synonym
And I click on Add Search Term
And I edit the index term name
And I edit the short name
And I change the concept class to Test
And I check/uncheck is set
And I change the datatype to Text
And I click Save Concept button
Then The concept should get saved with a success message


