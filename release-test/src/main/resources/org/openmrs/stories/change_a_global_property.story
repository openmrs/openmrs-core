GivenStories: org/openmrs/stories/go_to_admin_page.story

Given I am on Admin page
When I click on the Manage Global Properties link
Then take me to Global Properties Management Page with Global Properties as heading

When I type Test1 as name
When I type Test2 as value

And I click on Save button
Then display message Global properties saved