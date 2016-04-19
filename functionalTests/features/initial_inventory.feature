@INITIAL_INVENTORY @dev
Feature: Log in and initialize Inventory

  Scenario: User should be able to log in, initialize inventory and navigate to stock overview page

    Given server deactivates products 12D03 and 07L01
    When I try to log in with "initial_inventory" "password1"
    And I wait up to 60 seconds for "Initial Inventory" to appear
    Then I shouldn't see product "12D03" in this page
    And I shouldn't see product "07L01" in this page