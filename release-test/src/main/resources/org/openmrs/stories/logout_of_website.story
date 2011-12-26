GivenStories: org/openmrs/stories/login_to_website.story,org/openmrs/stories/go_to_admin_page.story,org/openmrs/stories/create_a_user.story

Given I am already logged into openmrs
When I click on link Log out
Then I must navigate to login page
