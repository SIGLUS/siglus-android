@INITIAL_INVENTORY
Feature: Initial Inventory
  Scenario: User should be able to initial inventory
    Given I am logged in
    Then I wait for "Initial Inventory" to appear
    Then I wait for 1 second
    Given I initialize inventory
    And I press "stock cards & overview"
    Then I wait for "Stock overview" to appear
    And I press "Sort alphabetically: A to Z"
    And I press "Sort by quantity: High to Low"
    Then I see "20"

