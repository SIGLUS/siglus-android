@MMIA
Feature: stock movement Page

    Background: Navigate to Home Page
        Given I am logged in

    Scenario: Initial a MMIA
            Given I have initialized inventory
            And I press "Stock Card"
            Then I wait for the "StockCardListActivity" screen to appear
            Then I wait for 1 second
            And I make a movement "[08S42]" "Issues" "PAV" "issued" "2"
            Then I wait for 1 second
            Then I go back
            Then I wait for the "HomeActivity" screen to appear
            And I press "Create a MMIA"
            Then I wait for the "MMIAActivity" screen to appear
            Then I wait for 1 second
            Then I should see text containing "Zidovudina/Lamivudina/Nevirapi; 300mg+150mg+200mg 60Comp; Embalagem"
            Then I swipe right
            Then I should see issued movement "2"
            Then I should see inventory "121"
            Then I swipe left
            Then I scroll to "Complete"
            Then I wait for 1 second
            And I enter regimen totals
            Then I press "Save"
            Then I wait for 1 second
            And I press "Create a MMIA"
            And I wait for the "MMIAActivity" screen to appear
            Then I scroll to "Complete"
            And I enter patient totals
            Then I press "Complete"
            Then I should see text containing "Your MMIA form has been successfully saved,"
            Then I wait for the "HomeActivity" screen to appear

    Scenario: after editing if I go back without saving I should see pop up, if I say yes then go back without saving, else staying at mmia page
            And I press "Stock Card"
            Then I wait for the "StockCardListActivity" screen to appear
            Then I wait for 1 second
            And I make a movement "[08S42]" "Issues" "PAV" "issued" "2"
            Then I wait for 1 second
            Then I go back
            Then I wait for the "HomeActivity" screen to appear
            And I press "Create a MMIA"
            Then I wait for the "MMIAActivity" screen to appear
            Then I wait for 1 second
            Then I should see text containing "Zidovudina/Lamivudina/Nevirapi; 300mg+150mg+200mg 60Comp; Embalagem"
            Then I scroll to "Complete"
            Then I wait for 1 second
            And I enter patient totals
            Then I go back
            Then I should see text containing "Are you sure you want to quit without saving your work?"
            Then I press "No"
            Then I wait for the "MMIAActivity" screen to appear
            Then I wait for 1 second
            Then I should see text containing "Complete"
            Then I go back
            Then I should see text containing "Are you sure you want to quit without saving your work?"
            Then I press "Yes"
            Then I wait for the "HomeActivity" screen to appear
            Then I wait for 1 second
            And I press "Create a MMIA"
            Then I wait for the "MMIAActivity" screen to appear
            Then I wait for 1 second
            Then I should see text containing "Zidovudina/Lamivudina/Nevirapi; 300mg+150mg+200mg 60Comp; Embalagem"
            Then I scroll to "Complete"
            And I should see empty patient total