Scenario: View encounters on the patient dashboard

GivenStories: org/openmrs/stories/create_an_encounter.story,org/openmrs/stories/verify_patient_dashboard.story

Given I am on the patient dashboard for Horatio
When I select the Encounters tab
Then I should see the encounters associated to the patient