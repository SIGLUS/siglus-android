@monkey
Feature: monkey test

  Scenario: monkey test
    Given I try to log in with "mali" "password1"
    And I wait up to 120 seconds for "STOCK CARD OVERVIEW" to appear

    Then I do monkey test for "1000" times
