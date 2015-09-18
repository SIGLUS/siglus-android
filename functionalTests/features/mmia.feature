@Mmia
Feature: stock movement Page

    Scenario: Navigate to Home Page
        Given I am logged in
        Given I am Initialized Inventory

    Scenario: Initial a MMIA
            Given I am logged in
            And I press "Stock on Hand"
            Then I wait for the "StockCardListActivity" screen to appear
            Then I wait for 1 second
            And I make a movement "Lamivudina 150mg/Zidovudina 300mg/Nevirapina 200mg [08S42]" "Issue" "Issues from customers requests" "issued" "2"
            Then I wait for 1 second
            Then I go back
            Then I wait for the "HomeActivity" screen to appear
            And I press "MMIA"
            Then I wait for the "MMIAActivity" screen to appear
            Then I wait for 1 second
            Then I should see text containing "Lamivudina 150mg/Zidovudina 300mg/Nevirapina 200mg"
            Then I swipe right
            Then I should see issued movement "2"
            Then I should see inventory "121"
            Then I swipe left
            Then I scroll to "Complete"
            Then I wait for 1 second
            And I enter regime total
            Then I press "Save"
            Then I wait for 1 second
            And I press "MMIA"
            Then I scroll to "Complete"
            And I enter patient total
            Then I press "Complete"
            Then I should see text containing "Thank you for submitting your mmia form."
            Then I wait for the "HomeActivity" screen to appear


