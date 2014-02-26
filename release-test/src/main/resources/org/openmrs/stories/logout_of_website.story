Scenario: Log out of website

GivenStories: org/openmrs/stories/go_to_admin_page.story

Given I am already logged into openmrs
When I click on link Log out
Then I must navigate to login page
