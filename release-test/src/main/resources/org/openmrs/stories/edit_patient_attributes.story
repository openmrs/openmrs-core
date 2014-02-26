Scenario: Edit patient attributes

GivenStories: org/openmrs/stories/go_to_admin_page.story

Given I am on the manage patient page
When I search for a  patient horatio
And choose the patient
And enter birthplace, citizenship, health district and race as Kenya, Kenyan, Nairobi and human respectively
And save
Then the patient attributes should be updated