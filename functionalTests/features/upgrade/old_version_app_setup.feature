@upgrade_setup
Feature: Old version set up before upgrade

  Scenario: Install an older version of the app on tablet and have data available from the old version
    When I try to log in with "mali" "password1"
    Then I press "Stock Card Overview"