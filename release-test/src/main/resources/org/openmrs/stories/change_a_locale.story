Scenario: Change a locale

GivenStories: org/openmrs/stories/go_to_admin_page.story

Given I am on Admin page
When I click on the Manage Locales And Themes link
Then take me to Locales And Themes Management Page with Manage Locales And Themes as heading

Given I am on the Locales And Themes Page

When I type en as locale

And I click on Submit button
Then display message Locales And Themes