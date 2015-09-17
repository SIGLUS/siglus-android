@StockMovement
Feature: stock movement Page

    Scenario: Add A Positive Movement
            Given I am logged in
            Given I am Initialized Inventory
            And I press "Stock on Hand"
            Then I wait for the "StockCardListActivity" screen to appear
            Then I wait for 1 second
            Then I select stock card called "Lamivudina 150mg/Zidovudina 300mg/Nevirapina 200mg [08S42]"
            Then I wait for the "StockMovementActivity" screen to appear
            Then I wait for 1 second
            And I select a reason "Receive" "Normal requisition/reinforcement"
            Then I wait for 1 second
            And I enter received number "2"
            And I press "Save"
            Then I go back
            Then I wait for 1 second
            Then I go back
            Then I wait for the "HomeActivity" screen to appear

    Scenario: Add A Negative Movement
        Given I am logged in
        And I press "Stock on Hand"
        Then I wait for the "StockCardListActivity" screen to appear
        Then I wait for 1 second
        Then I select stock card called "Tenofovir 300mg/Lamivudina 300mg/Efavirenze 600mg [08S18Y]"
        Then I wait for the "StockMovementActivity" screen to appear
        Then I wait for 1 second
        And I select a reason "Issue" "Issues from customers requests"
        Then I wait for 1 second
        Then I swipe right
        And I enter issued number "2"
        And I press "Save"
        Then I go back
        Then I wait for 1 second
        Then I go back
        Then I wait for the "HomeActivity" screen to appear


