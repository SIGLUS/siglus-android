@core @dev @weitest
Feature: Rapidtest

  Scenario: Create rapidtest and sync it


    Given I try to log in with "training" "password1"
    And I wait for "Stock Card Overview" to appear

    And I press "Rapid Test"
    Then I wait for "Rapid Test Reports" to appear
    And I wait for 1 second
    And I press CREATE RAPID TEST REPORT
    And I wait for "Rapid Test Report" to appear
    Then I enter quantity for Rapid Test Report
    And I wait for 1 seconds
    Then I press "Submit for Approval"
    And I sign requisition with "superuser" "testUser" and complete
    Then I navigate back
    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"
    And I wait for 3 seconds
    And I press "Rapid Test"
    Then I should see text containing "Rapid Test Reports successfully submitted"