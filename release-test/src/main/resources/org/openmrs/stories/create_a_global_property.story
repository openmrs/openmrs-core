Scenario: Create a global property

GivenStories: org/openmrs/stories/go_to_admin_page.story

Given I am on Admin page
When I click on the Advanced Settings link
Then take me to Advanced Settings Page with Advanced Settings as heading

When I press input button Add Property
When I type Test1 as name
When I type Test2 as value

And I click on Save button
Then display message Global properties saved
