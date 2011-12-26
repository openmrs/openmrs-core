GivenStories: org/openmrs/stories/login_to_website.story,org/openmrs/stories/retire_concept_drug.story

Given The new concept drug was retired
When I want to unretire the retired drug
And I chose to edit the retired drug
And I unretire the drug
Then the concept drug should get unretired
