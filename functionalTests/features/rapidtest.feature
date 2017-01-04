@core @dev @rapid
Feature: Rapid Test

  Scenario: Create Rapid Test Report and sync
    Given I change device date to "20160216.130000"
    Given I try to log in with "core" "password1"
    Given I have initialized inventory with VIA user with lot
    And I wait for "Stock Card Overview" to appear

    Given I change device date to "20160218.140000"
    And I wait for 2 seconds
    And I press "Stock Card Overview"
    And I try to log in with "core" "password1"
    And I wait for "Stock Card Overview" to appear

    And I press "Rapid Test"
    Then I wait for "Rapid Test Reports" to appear
    And I wait for 1 second
    And I press "CREATE RAPID TEST REPORT"
    And I wait for "Rapid Test Report" to appear
    Then I enter quantity for Rapid Test Report
    And I wait for 1 second
    Then I press "Submit for Approval"
    And I sign requisition with "superuser" "testUser" and complete

    Then I wait for 3 seconds
    Then I navigate back

    Then I wait for "Stock Card Overview" to appear
    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"
    And I wait for 3 seconds
    And I press "Rapid Test"
    Then I should see text containing "Rapid Test Reports successfully submitted"