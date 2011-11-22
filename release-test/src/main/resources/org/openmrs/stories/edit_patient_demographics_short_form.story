GivenStories: org/openmrs/stories/verify_patient_dashboard.story

Given I choose to edit the patient in the short form
When I mention Address, Address2, City as addressOne, addressTwo, Bangalore
And save the demographics
Then the information should be saved 