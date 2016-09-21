@save_physical_inventory @regression
Feature: Save physical inventory

  Scenario: Save physical inventory, and check the stock on hand quantity that have been saved.
    Given I try to log in with "superuser" "password1"
    And I wait up to 60 seconds for "Initial Inventory" to appear
    When I search lot product by fnm "08S42B" and select this item with quantity "123" and lot number "testA"
    And I wait for "Complete" to appear
    And I press "Complete"
    Then I wait for "STOCK CARD OVERVIEW" to appear

    And I press "Inventory"
    And I wait for "inventory" to appear
    And I do physical inventory with lots with "2015" by fnm "08S42B"
    And I press "Save"

    And I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Inventory"
    And I wait for "inventory" to appear
    Then I should see text containing "2015"