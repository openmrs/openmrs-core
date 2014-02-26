Scenario: Create a relationship type

GivenStories: org/openmrs/stories/go_to_admin_page.story

When I click on the Manage Relationship Types link
Then take me to Relationship Type Management Page with Relationship Type Management as heading

Given I am on the Relationship Type Management Page
When I click on the Add Relationship Type link
Then take me to Add Relationship Type page with Relationship Type Form as heading and has a button with label Save Relationship Type

Given I am on the Relationship Type Form Page
When I type Test1 as relationship of A to B
When I type Test2 as relationship of B to A
When I type Test3 as description
When I click the Save Relationship Type button
Then display message Relationship type saved