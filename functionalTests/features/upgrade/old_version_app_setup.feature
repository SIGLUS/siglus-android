@upgrade_setup
Feature: Old version set up before upgrade

  Scenario: Install an older version of the app on tablet and have data available from the old version
    When I try to log in with "mali" "password1"
    And I wait up to 120 seconds for "Stock Card Overview" to appear
    # to run this in a physical device, we need to wait longer, IO is low on physical devices
    Then I press "Stock Card Overview"