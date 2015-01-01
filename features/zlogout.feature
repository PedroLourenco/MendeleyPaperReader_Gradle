Feature: LogOut test
@logOut_test
Scenario: As a logged in user I can perform a logout
Given I see logout button
Then I touch on "Logout" button
Then I confirm that i want to logout
