Feature: Home Page

    Scenario: Navigate to Home Page
            Given I am logged in
            Given I am Initialized Inventory

    Scenario: Entry Inventory Page
            Given I am logged in
            And I press "Inventory"
            Then I wait for the "InventoryActivity" screen to appear
