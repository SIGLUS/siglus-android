@INITIAL_INVENTORY
Feature: Initial Inventory
  Scenario: User should be able to initial inventory
    Given I try to log in with "initial_inventory" "password1"
    Given I initialize inventory
    And I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    And I press "Sort alphabetically: A to Z"
    And I press "Sort by quantity: High to Low"
    Then I check the initial result quantity on stock overview page
    Then I navigate back
    And I sign out
    Then I wait for the "LoginActivity" screen to appear


