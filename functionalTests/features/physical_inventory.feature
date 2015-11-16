Feature: physical_inventory

    @physical_inventory
    Scenario: physical_inventory
            Given I am logged in
            Given I have initialized inventory
            Then I press "Do Monthly Inventory"
            And I press "+ Expiry Date"
            And I set Date
            And I press "Done"
            And I enter text "1111" into field with id "tx_quantity"
            Then I press "Save"
            Then I press "Do Monthly Inventory"
            And I see "1111"
            And I see "2016"


