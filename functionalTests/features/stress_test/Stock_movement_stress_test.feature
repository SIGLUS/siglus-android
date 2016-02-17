@STRESS_TEST
Feature: all stock movements

  Scenario: Add all movements for one drug
    Given I try to log in with "initial_inventory" "password1"
    Then I wait up to 30 seconds for "Initial Inventory" to appear
    When I search product by primary name "Abacavir sulfato (ABC) 300mg,60comp Embalagem" and select this item with quantity "123"
    And I press "Complete"

    And I wait for "STOCK CARD OVERVIEW" to appear
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I make all movements for "08S01" when stress test
    Then I wait for 1 second
    Then I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear