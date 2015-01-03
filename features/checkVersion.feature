Feature: Check version off APP


  @check_app_version
  Scenario: Validate the correct version of AboutActivity
    Given I wait for the Sign in button to appear
    Then I wait for the Mendeley oauth login page
    Then I enter "pdrolourenco@gmail.com" into the "username" input field
    Then I enter "000000" into the "password" input field
    Then I touch "Authorize"
    Then I wait for the "Contacting Mendeley ..." progress dialog to close
    Then I wait for the "Sync data..." progress dialog to close
    Then I validate if it's the right activity - MainMenuActivity
    Then I see Menu list
    Then I touch on "About" button
    Then I validate if it's the right activity - AboutActivity
    Then I search for "Version 0.5.0"

