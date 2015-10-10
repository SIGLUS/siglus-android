Feature: initial_inventory

    @initial_inventory
    Scenario: initial_inventory
            Given I am logged in
            Then I wait for the "InventoryActivity" screen to appear
            Then I wait for 1 second
            When I Select initial inventory
            And I press "Done"
            Then I wait for the "HomeActivity" screen to appear
            And I press "STOCK MOVEMENT CARD"
            Then I wait for the "StockCardListActivity" screen to appear
            And I press "Sort alphabetically: A to Z"
            And I press "Sort by quantity: High to Low"
            Then I see "20"

