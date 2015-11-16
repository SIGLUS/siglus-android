Feature: initial_inventory

    @initial_inventory
    Scenario: initial_inventory
            Given I am logged in
            Then I wait for the "InventoryActivity" screen to appear
            Then I wait for 1 second
            Given I initialize inventory
            And I press "Stock Card"
            Then I wait for the "StockCardListActivity" screen to appear
            And I press "Sort alphabetically: A to Z"
            And I press "Sort by quantity: High to Low"
            Then I see "20"

