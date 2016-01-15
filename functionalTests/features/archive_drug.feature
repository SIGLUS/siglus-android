@ARCHIVE_DRUG
Feature: Archive drug

  #Should test that one drug archived at 18th, and archived drug shouldn't appear at mmia/via/physical inventory at next period
  #Should add check steps in mmia and via form and make sure all data in the form is 0 or disappear


  Scenario: Archive two drugs
    Given I try to log in with "initial_inventory" "password1"
    Then I wait for "Initial Inventory" to appear
    Then I wait for 1 second
    Given I have initialized inventory
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    Then I select stock card code called "[01A01]"
    Then I wait for "Stock Card" to appear
    And I select a reason "Issues" "PAV"
    Then I wait for 1 second
    Then I swipe right
    And I enter issued number "123"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"

    Then I see "0"
    Then I see "super" in signature field
    And I press the menu key
    Then I see "Archive drugs"
    And I press "Archive drugs"
    And I wait for "Stock Overview" to appear
    Then I should see total:"9" on stock list page
    Then I don't see the text "[01A01]"

    Then I select stock card code called "[08S32Z]"
    Then I wait for "Stock Card" to appear
    And I select a reason "Negative Adjustments" "Damaged on arrival"
    Then I wait for 1 second
    Then I swipe right
    And I enter negative adjustment number "123"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"

    Then I see "0"
    Then I see "super" in signature field
    And I press the menu key
    Then I see "Archive drugs"
    And I press "Archive drugs"
    And I wait for "Stock Overview" to appear
    Then I should see total:"8" on stock list page
    Then I don't see the text "[08S32Z]"

    And I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Do Monthly Inventory"
    Then I wait for "Inventory" to appear
    Then I shouldn't see product "01A01" in this page
    Then I shouldn't see product "08S32Z" in this page

    And I navigate back
    And I wait for 1 second
    And I navigate back
    And I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I press the menu key
    And I press "Archived drugs"
    And I wait for "Archived drugs" to appear
    Then I see the text "[01A01]"

    When I press "View movement history"
    And I wait for the "StockMovementHistoryActivity" screen to appear
    Then I see the text "Inventory"
    Then I see the text "PAV"

    When I navigate back
    And I press "Add drug to stock overview"
    And I navigate back
    And I wait for "Stock Overview" to appear
    Then I should see total:"9" on stock list page
    Then I see the text "[01A01]"

    When I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Do Monthly Inventory"
    Then I wait for "Inventory" to appear
    Then I should see product "01A01" in this page

    # Server updates drugs including 08S32Z
    Given Server updates drug data
    When I navigate back
    And I navigate back
    And I wait for "STOCK CARD OVERVIEW" to appear

    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"

    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    # Archived drugs should stay as archived after server update
    Then I shouldn't see product "08S32Z" in this page

  Scenario: Unarchive one drug from Add new product
    Given I try to log in with "initial_inventory" "password1"
    And I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    And I press the menu key
    And I press "Add new product"
    And I wait for "Add new product" to appear
    Then I select new drug "08S32Z" with SOH "100" quantity
    And I press "Complete"

    And I wait for "Stock Overview" to appear
    Then I should see total:"10" on stock list page
    Then I see the text "[08S32Z]"

    And I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Do Monthly Inventory"
    Then I wait for "Inventory" to appear
    Then I should see product "08S32Z" in this page