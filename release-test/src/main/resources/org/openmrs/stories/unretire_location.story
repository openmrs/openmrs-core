GivenStories: org/openmrs/stories/login_to_website.story,org/openmrs/stories/retire_location.story

Given The location was retired
When I want to unretire the retired location
And I chose to edit the retired location
And I unretire the location
Then the location should get unretired
