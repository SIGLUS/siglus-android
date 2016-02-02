@upgrade
Feature: After version upgrade, data integrity should not be broken

  Scenario: Log in
    When I try to log in with "mali" "password1"
    And I wait up to 120 seconds for "Stock Card Overview" to appear