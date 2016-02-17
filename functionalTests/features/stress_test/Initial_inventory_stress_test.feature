@STRESS_TEST
Feature: initial inventory

  Scenario: Initial inventory
    Given I try to log in with "initial_inventory" "password1"
    Then I wait up to 30 seconds for "Initial Inventory" to appear
    And I initialize "1264" products
    And I press "Complete"

    And I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I press "Sort alphabetically: A to Z"
    And I press "Sort by quantity: High to Low"
    And I wait for 1 second

    Then I can see stock on hand "1264" in position "1"
    Then I should see total:"1264" on stock list page
