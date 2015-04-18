Feature: Test log in and log out functions

  @login_test
  Scenario: Test Login
    Given I wait for the Sign in button to appear
    Then I wait for the Mendeley oauth login page
    Then I enter "pdrolourenco@gmail.com" into the "username" input field
    Then I enter "000000" into the "password" input field
    Then I touch "Authorize"
    Then I validate if it's the right activity - MainMenuActivity


  @logOut_test
  Scenario: As a logged in user I can perform a logout
    Given I wait for the Sign in button to appear
    Then I wait for the Mendeley oauth login page
    Then I enter "pdrolourenco@gmail.com" into the "username" input field
    Then I enter "000000" into the "password" input field
    Then I touch "Authorize"
    Then I validate if it's the right activity - MainMenuActivity
    Then I see Menu list
    Then I touch on "Logout" button
    Then I confirm that i want to logout






