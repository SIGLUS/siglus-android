@regression
Feature: Happy path existing user flow

  Scenario: User should be able to log in and navigate to stock overview page

    When I try to log in with "mali" "password1"
    And I press "Stock Card Overview"

