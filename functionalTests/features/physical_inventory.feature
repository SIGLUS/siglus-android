@PHYSICAL_INVENTORY
Feature: Physical Inventory

  Scenario: physical_inventory
    Given I am logged in
    Given I have initialized inventory
    Then I press "Do Monthly Inventory"
    And I press "+ Expiry Date"



