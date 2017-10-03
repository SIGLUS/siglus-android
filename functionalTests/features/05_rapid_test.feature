@rapid @dev
Feature: Rapid Test

  Scenario: Create Rapid Test Report and sync
    Given today is "20160218.140000"
    And I log in for the first time
    And I press "Rapid Test"
    And I should see text containing "No rapid test report has been created"
    And I press "CREATE RAPID TEST REPORT"
    And I enter quantity for Rapid Test Report
    And I save rapid test report
    And I should see text containing "Rapid Test Reports is incomplete."
    And I press "Continue Working on Rapid Test Reports"
    And I submit rapid test report
    And I sign using "TWUIO" as initials
    And I press continue to finish approval
    And I press "Complete"
    And I sign using "TWUIO" as initials
    When I wait up to 30 seconds to see "successfully submitted"
    Then I should see text containing "Rapid Test Reports successfully submitted"