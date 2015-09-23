Feature: initial_inventory

    @initial_inventory
    Scenario: initial_inventory
            Given I am logged in
            Then I wait for the "InventoryActivity" screen to appear
            Then I wait for 1 second
            When I Select initial inventory
            And I press "Done"
            Then I wait for the "HomeActivity" screen to appear

