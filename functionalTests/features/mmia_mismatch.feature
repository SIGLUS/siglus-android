@MMIA_mismatch
Feature: MMIA Page total mismatch

  Scenario: When I fill a field, if the regime total and patient total are different, I will see pop up if I press complete without filling comments.
    Given I am logged in
    And I have initialized inventory
    Then I wait for the "HomeActivity" screen to appear
    When I press "Create a MMIA"
    And I wait for the "MMIAActivity" screen to appear
    And I scroll to "Complete"
    And I enter regimen totals
    And I should see text containing "Add reason for regime & patient mismatch"
    When I enter patient total different from regime total
    And I press "Complete"
    Then I should see text containing "Totals do not match please check figures that you"
    When I press "OK"
    And I enter "just because!" in "Observations"
    Then I press "Complete"
    And I sign with "superuser"
    Then I press "Sign"
    Then I should see text containing "Your MMIA form has been successfully saved,"
    Then I wait for the "HomeActivity" screen to appear
