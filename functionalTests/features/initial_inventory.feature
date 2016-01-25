@INITIAL_INVENTORY @dev
Feature: Log in and initialize Inventory

  Scenario: User should be able to log in, initialize inventory and navigate to stock overview page

    Given server deactivates products 12D03 and 07L01
    When I try to log in with "initial_inventory" "password1"
    And I wait up to 30 seconds for "Initial Inventory" to appear
    Then I shouldn't see product "12D03" in this page
    And I shouldn't see product "07L01" in this page

    When I Select VIA Item
    And I Select MMIA Item
    And I wait for "Complete" to appear
    And I press "Complete"
    Then I wait for "STOCK CARD OVERVIEW" to appear

    When I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I press "Sort alphabetically: A to Z"
    And I press "Sort by quantity: High to Low"
    Then I check the initial result quantity on stock overview page

    When I navigate back
    And I sign out
    Then I wait for the "LoginActivity" screen to appear