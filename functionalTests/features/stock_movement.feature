@StockMovement
Feature: stock movement Page

    Scenario: Navigate to Home Page
        Given I am logged in
        Given I am Initialized Inventory

    Scenario: Bottom Btn Logic
        Given I am logged in
        And I press "Stock on Hand"
        Then I wait for the "StockCardListActivity" screen to appear
        Then I see "Stock Card"
        Then I wait for 1 second
        Then I select stock card called "Lamivudina 150mg/Zidovudina 300mg/Nevirapina 200mg [08S42]"
        Then I wait for the "StockMovementActivity" screen to appear
        Then I wait for 1 second
        Then I don't see "Save"
        Then I don't see "Cancel"
        Then I select a reason "Entries" "Normal requisition/reinforcement"
        Then I should see "Save"
        Then I should see "Cancel"
        And I press "Cancel"
        Then I wait for 1 second
        Then I don't see "Save"
        Then I don't see "Cancel"
        Then I don't see "Normal requisition/reinforcement"



    Scenario: Add A Receive Movement
        Given I am logged in
        And I press "Stock on Hand"
        Then I wait for the "StockCardListActivity" screen to appear
        Then I wait for 1 second
        Then I select stock card called "Lamivudina 150mg/Zidovudina 300mg/Nevirapina 200mg [08S42]"
        Then I wait for the "StockMovementActivity" screen to appear
        Then I wait for 1 second
        And I select a reason "Entries" "Normal requisition/reinforcement"
        Then I wait for 1 second
        And I enter received number "2"
        And I press "Save"
        Then I see "125"
        Then I go back
        Then I wait for 1 second
        Then I go back
        Then I wait for the "HomeActivity" screen to appear

    Scenario: Add A Negative Adjustment
        Given I am logged in
        And I press "Stock on Hand"
        Then I wait for the "StockCardListActivity" screen to appear
        Then I wait for 1 second
        Then I select stock card called "Acyclovir, tablet 400mg [P2]"
        Then I wait for the "StockMovementActivity" screen to appear
        Then I wait for 1 second
        And I select a reason "Negative Adjustments" "Damaged on arrival"
        Then I wait for 1 second
        And I enter negative adjustment number "2"
        And I press "Save"
        Then I see "121"
        Then I go back
        Then I wait for 1 second
        Then I go back
        Then I wait for the "HomeActivity" screen to appear

    Scenario: Add A Positive Adjustment
        Given I am logged in
        And I press "Stock on Hand"
        Then I wait for the "StockCardListActivity" screen to appear
        Then I wait for 1 second
        Then I select stock card called "Atenolol 50mg tab [P5]"
        Then I wait for the "StockMovementActivity" screen to appear
        Then I wait for 1 second
        And I select a reason "Positive Adjustments" "Donations to Deposit"
        Then I swipe right
        Then I wait for 1 second
        And I enter positive adjustment number "2"
        And I press "Save"
        Then I see "125"
        Then I go back
        Then I wait for 1 second
        Then I go back
        Then I wait for the "HomeActivity" screen to appear

    Scenario: Add A Issued Movement
        Given I am logged in
        And I press "Stock on Hand"
        Then I wait for the "StockCardListActivity" screen to appear
        Then I wait for 1 second
        Then I select stock card called "Tenofovir 300mg/Lamivudina 300mg/Efavirenze 600mg [08S18Y]"
        Then I wait for the "StockMovementActivity" screen to appear
        Then I wait for 1 second
        And I select a reason "Issues" "Issues from customers requests"
        Then I wait for 1 second
        Then I swipe right
        And I enter issued number "2"
        And I press "Save"
        Then I see "121"
        Then I go back
        Then I wait for 1 second
        Then I go back
        Then I wait for the "HomeActivity" screen to appear


