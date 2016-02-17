@STRESS_TEST
Feature: add new drug

  Scenario: Add new drugs
    Given I try to log in with "initial_inventory" "password1"
    And I wait up to 30 seconds for "Initial Inventory" to appear
    And I press "Complete"
    And I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    And I press the menu key
    And I wait for "Add new product" to appear
    Then I press "Add new product"
    And I wait for "Add new product" to appear

    And I initialize "1264" products
    Then I press "Complete"
    Then I wait for "Stock Overview" to appear
    Then I should see total:"1264" on stock list page