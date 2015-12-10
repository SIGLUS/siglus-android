@PHYSICAL_INVENTORY
Feature: Physical Inventory

  Scenario: physical_inventory
    Given I try to log in with "physical_inventory" "password1"
    Given I have initialized inventory
    Then I press "Do Monthly Inventory"
    And I press "+ Expiry Date"



