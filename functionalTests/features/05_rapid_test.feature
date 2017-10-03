@core @rapid
Feature: Rapid Test

  Scenario: Create Rapid Test Report and sync
    Given today is "20160218.140000"
    And I log in for the first time
    And I see the initial inventory screen
    And I press "Rapid Test"
    And I should see text containing "No rapid test report has been created."
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