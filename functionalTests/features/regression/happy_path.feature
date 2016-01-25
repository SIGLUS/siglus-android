@regression
Feature: Log in and initialize Inventory

  Scenario: User should be able to log in, initialize inventory and navigate to stock overview page

    # Initial inventory
    Given server deactivates products 12D03 and 07L01
    When I try to log in with "initial_inventory" "password1"
    And I wait up to 30 seconds for "Initial Inventory" to appear
    Then I shouldn't see product "12D03" in this page
    And I shouldn't see product "07L01" in this page

    When I Select VIA Item
    And I Select MMIA Item
    And I wait for "Complete" to appear
    And I press "Complete"
    Then I wait for "STOCK CARD OVERVIEW" to appear

    When I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I press "Sort alphabetically: A to Z"
    And I press "Sort by quantity: High to Low"
    Then I check the initial result quantity on stock overview page

    # Stock Card
    And I press the menu key
    And I wait for "Add new product" to appear
    And I press "Add new product"
    And I wait for "Add new product" to appear
    Then I shouldn't see product "99X99" in this page

    When I search drug by fnm "25D03"
    Then I see "Manual de"
    When I clean search bar
    And I search drug by fnm "12D03"
    Then I should see product "12D03" in this page

    Given Server updates drug data
    And server deactivates products 12D03 and 07L01

    When I navigate back
    And I navigate back
    And I wait for "Stock Overview" to appear
    And I navigate back
    And I wait for "STOCK CARD OVERVIEW" to appear

    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"

    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I press the menu key
    And I wait for "Add new product" to appear
    And I press "Add new product"
    And I wait for "Add new product" to appear

    When I search drug by fnm "99X99"
    Then I see "New Drug"
    When I clean search bar
    And I search drug by fnm "25D03"
    Then I see "Updated Drug"
    When I clean search bar
    And I search drug by fnm "12D03"
    Then I shouldn't see product "12D03" in this page

    And I navigate back
    And I press the menu key
    And I wait for "Add new product" to appear
    Then I press "Add new product"
    And I wait for "Add new product" to appear
    When I search drug by fnm "08S01ZY"
    When I select the inventory item
    And I press "Complete"
    Then I should see text containing "Quantity cannot be left blank!"

    When I unselect the inventory item
    And I have added new drugs
    Then I press "Complete"
    Then I wait for "Stock Overview" to appear
    And I check new drug quantity

    # Stock movements
    Then I select stock card code called "[08S42B]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    Then I don't see "Complete"
    Then I don't see "Cancel"
    Then I select a reason "Entries" "District( DDM)"
    Then I should see "Complete"
    Then I should see "CANCEL"
    And I press "CANCEL"
    Then I wait for 1 second
    Then I don't see "Complete"
    Then I don't see "CANCEL"

    When I navigate back
    Then I select stock card code called "[01A04Z]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    Then I should not see "This product has been deactivated and is not available to reorder"
    And I select a reason "Negative Adjustments" "Damaged on arrival"
    Then I wait for 1 second
    Then I swipe right
    And I enter negative adjustment number "123"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"
    Then I see "123"
    Then I see "super" in signature field
    Then I navigate back
    Then I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear

    When I navigate back
    And I sign out
    Then I wait for the "LoginActivity" screen to appear