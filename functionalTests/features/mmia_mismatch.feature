@MMIA_MISMATCH @dev @change_date
Feature: MMIA Page total mismatch

  Scenario: When I fill a field, if the regime total and patient total are different, I will see pop up if I press complete without filling comments.
    Given I change device date to "20160116.130000"
    Given I try to log in with "mmia_mismatch" "password1"
    Given I have initialized inventory with MMIA user
    Then I wait for "Stock Card Overview" to appear

    Given I change device date to "20160121.140000"
    Then I press "MMIA"
    And I try to log in with "mmia_mismatch" "password1"
    And I press "MMIA"
    Then I wait for "MMIA" to appear
    Then I should see text containing "No MMIA has been created."

    Then I press "Complete Inventory"
    And I wait for "inventory" to appear
    Then I do physical inventory for mmia items

    Then I wait for "MMIA" to appear
    Then I should see text containing "Create MMIA"

    Then I press "Create MMIA"
    Then I should see "Select inventory to close period"
    And I press "Thursday"
    And I press "Next"
    And I wait for "MMIA -" to appear
    And I scroll "ScrollView" down to "Submit for Approval"
    And I enter regimen totals
    Then I wait for 1 second
    When I enter patient total different from regime total
    And I should see text containing "Add reason for mismatch between regime & patient totals"
    And I press "Submit for Approval"
    Then I should see text containing "The regime and patient totals do not match. Please correct your figures or add a comment that explains why the totals differ"
    When I press "OK"
    And I enter "just because!" in "Observations"
    Then I wait for 1 second
    Then I should not see "Add reason for mismatch between regime & patient totals"
    Then I wait for 1 second
    Then I press "Submit for Approval"
    And I sign requisition with "superuser" "testUser" and complete
    Then I should see text containing "Your MMIA form has been successfully saved,"
    Then I wait for "MMIA" to appear
