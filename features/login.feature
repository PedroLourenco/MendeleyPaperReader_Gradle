Feature: Test log in and log out functions

  @login_test
  Scenario: As a logged in user I can add a file
    Given I wait for the Sign in button to appear
    Then I wait for the Mendeley oauth login page
    Then I wait too see "Authorize"
    Then I enter "pdrolourenco@gmail.com" into the "username" input field
    Then I enter "000000" into the "password" input field
    Then I touch "Authorize"
    Then I wait for the "Contacting Mendeley ..." progress dialog to close
    Then I wait for the "Sync data..." progress dialog to close
    Then Validate if you are in the right activity


  @logOut_test
  Scenario: As a logged in user I can perform a logout
    Given I see logout button
    Then I touch on "Logout" button
    Then I confirm that i want to logout


