Scenario: Edit patient demographics short form

GivenStories: org/openmrs/stories/verify_patient_dashboard.story

Given I edit the patient in the short form

And I click on the button Save
Then the information should be saved 