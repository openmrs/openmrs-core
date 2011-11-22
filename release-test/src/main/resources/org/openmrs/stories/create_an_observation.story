GivenStories: org/openmrs/stories/go_to_admin_page.story

Given I am on Admin page
When I click on the Manage Observations link
Then take me to Observation Management Page with Observation Management as heading

Given I am on the Observation Management Page
When I click on Add Observation  link
Then take me to Add Observation page with Observation as heading and has a button with label Save Observation

Given I am on the Add Observation page
When I type Robert as person
And I select Unknown Location as Location with index 1
And I type 03/08/2011 as Observation Date
And I type temp as Concept Question
And I type 38 as Concept Answer
And I click the Save Observation button
Then display message Observation saved
