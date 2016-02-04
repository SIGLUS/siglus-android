@STRESS_TEST
Feature: Stock Card Overview page

  Background: Navigate to Home Page
    Given I try to log in with "initial_inventory" "password1"

  Scenario: Initial inventory
    Then I wait up to 30 seconds for "Initial Inventory" to appear
    And I initialize "300" products
    And I press "Complete"

    And I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I press "Sort alphabetically: A to Z"
    And I press "Sort by quantity: High to Low"
    And I wait for 1 second

    Then I can see stock on hand "300" in position "1"
    Then I should see total:"10" on stock list page

  Scenario: Add new drugs
    And I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    And I press the menu key
    And I wait for "Add new product" to appear
    Then I press "Add new product"
    And I wait for "Add new product" to appear

    And I add "300" new products
    Then I press "Complete"
    Then I wait for "Stock Overview" to appear
    Then I should see total:"600" on stock list page

  Scenario: Add all movements for one drug
    And I wait for "STOCK CARD OVERVIEW" to appear
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I make all movements for "08S01" when stress test
    Then I wait for 1 second
    Then I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear