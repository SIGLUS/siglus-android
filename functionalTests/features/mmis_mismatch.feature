@MMIA_MISMATCH
Feature: MMIA Page total mismatch

  Scenario: When I fill a field, if the regime total and patient total are different, I will see pop up if I press complete without filling comments.
    Given I try to log in with "mmia_mismatch" "password1"
    Given I have initialized inventory
    When I press "Create a MMIA"
    And I wait for "MMIA -" to appear
    And I scroll to "Submit for Approval"
    And I enter regimen totals
    And I should see text containing "Add reason for mismatch between regime & patient totals"
    When I enter patient total different from regime total
    And I press "Submit for Approval"
    Then I should see text containing "Totals do not match please check figures that you"
    When I press "OK"
    And I enter "just because!" in "Observations"
    Then I wait for 1 second
    Then I press "Submit for Approval"
    And I sign requisition with "superuser" "testUser" and complete
    Then I should see text containing "Your MMIA form has been successfully saved,"
    Then I wait for "Home Page" to appear