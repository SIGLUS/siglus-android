@regression
Feature: Log in and initialize Inventory

  Scenario: User should be able to log in, initialize inventory and navigate to stock overview page

    # Initialize inventory and check stock card overview
    Given I try to log in with "superuser" "password1"
    And I wait up to 120 seconds for "Initial Inventory" to appear
    # to run this in a physical device, we need to wait longer, IO is low on physical devices

    # 01A01, 01A02, 01A03Z, 01A04Z, 01A05
    When I Select VIA Item
    # 08S42B, 08S18Y, 08S40Z, 08S36, 08S32Z
    And I Select MMIA Item
    And I wait for "Complete" to appear
    And I press "Complete"
    Then I wait for "STOCK CARD OVERVIEW" to appear

    When I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I press "Sort alphabetically: A to Z"
    And I press "Sort by quantity: High to Low"
    Then I should see SOH of "01A01" is "123"
    And I should see SOH of "08S42B" is "123"

    # Add new product to stock cards
    When I press the menu key
    And I wait for "Add new product" to appear
    And I press "Add new product"
    And I wait for "Add new product" to appear

    When I search drug by fnm "25D03"
    Then I see "Manual de"
    When I clean search bar
    And I select the inventory item called "08S01ZY"
    And I press "Complete"
    Then I should see text containing "Quantity cannot be left blank!"

    When I unselect the inventory item
    And I have added new drugs
    And I press "Complete"
    Then I wait for "Stock Overview" to appear
    And I check new drug quantity

    # Attempt to make a stock movement and cancel
    Given I navigate back
    And I wait for 1 second
    And I select stock card code called "[08S42B]"
    And I wait for "Stock Card" to appear
    And I wait for 1 second
    And I don't see "Complete"
    And I don't see "Cancel"
    When I select a reason "Entries" "District( DDM)"
    And I should see "Complete"
    And I should see "CANCEL"
    And I press "CANCEL"
    And I wait for 1 second
    Then I don't see "Complete"
    And I don't see "CANCEL"

    # Make a stock movement and save
    When I navigate back
    And I wait for 1 second
    And I select stock card code called "[01A04Z]"
    And I wait for "Stock Card" to appear
    And I wait for 1 second
    And I select a reason "Negative Adjustments" "Damaged on arrival"
    And I wait for 1 second
    And I swipe right
    And I enter negative adjustment number "123"
    And I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"
    Then I see "123"
    And I swipe right
    #physical device portrait view is too narrow for signature to show, need to swipe right
    And I see "super" in signature field

    # Make stock movements with different movement types
    When I navigate back
    And I wait for 1 second
    When I make all movements for "08S18Y"
    And I wait for 1 second
    Then I should see SOH of "08S18Y" is "125"

    # Make stock movements in landscape mode
    When I select stock card code called "[01A01]"
    And I wait for "Stock Card" to appear
    And I select a reason "Positive Adjustments" "Donations to Deposit"
    Then I swipe right
    Then I wait for 1 second
    And I enter positive adjustment number "2"
    And I rotate the page to "landscape"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"
    And I wait for 1 second
    Then I see the text "Donations to Deposit"
    And I see "125"
    And I rotate the page to "portrait"

    # Archive VIA drug
    When I navigate back
    And I wait for 1 second
    And I select stock card code called "[01A01]"
    And I wait for "Stock Card" to appear
    And I select a reason "Issues" "Maternity"
    And I wait for 1 second
    And I swipe right
    And I enter issued number "125"
    And I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"
    Then I see "0"
    And I press the menu key
    Then I see "Archive drugs"
    And I press "Archive drugs"
    And I navigate back
    And I wait for "Stock Overview" to appear
    Then I should see total:"10" on stock list page
    And I don't see the text "[01A01]"

    # Archive MMIA drug
    When I select stock card code called "[08S32Z]"
    And I wait for "Stock Card" to appear
    And I select a reason "Negative Adjustments" "Damaged on arrival"
    And I wait for 1 second
    And I swipe right
    And I enter negative adjustment number "123"
    And I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"
    Then I see "0"
    When I press the menu key
    Then I see "Archive drugs"
    When I press "Archive drugs"
    And I wait for "Stock Overview" to appear
    Then I should see total:"9" on stock list page
    And I don't see the text "[08S32Z]"

    # Archived drugs don't appear in monthly inventory
    When I navigate back
    And I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Do Monthly Inventory"
    And I wait for "Inventory" to appear
    Then I shouldn't see product "01A01" in this page
    And I shouldn't see product "08S32Z" in this page

    # Archived drugs screen
    When I navigate back
    And I wait for 1 second
    And I navigate back
    And I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I press the menu key
    And I press "Archived drugs"
    And I wait for "Archived drugs" to appear
    Then I see the text "[01A01]"

    # Stock movement history screen
    When I press "View movement history"
    And I wait for the "StockMovementHistoryActivity" screen to appear
    Then I see the text "Inventory"
    Then I see the text "PAV"

    # Unarchive a drug
    When I navigate back
    And I press "Add drug to stock overview"
    And I navigate back
    And I wait for "Stock Overview" to appear
    Then I should see total:"10" on stock list page
    Then I see the text "[01A01]"

    # Unarchived drug shows up in monthly inventory
    When I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Do Monthly Inventory"
    Then I wait for "Inventory" to appear
    Then I should see product "01A01" in this page

    # Physical inventory cannot include blank quantities
    When I press "Complete"
    Then I should see text containing "Quantity cannot be left blank!"

    # Do physical inventory and SOH should be adjusted
    When I do physical inventory with "100" by fnm "08S42B"
    And I do physical inventory with "100" by fnm "08S18Y"
    And I do physical inventory with "100" by fnm "08S40Z"
    And I do physical inventory with "100" by fnm "01A01"
    And I do physical inventory with "100" by fnm "01A03Z"
    And I do physical inventory with "100" by fnm "01A02"
    And I do physical inventory with "100" by fnm "01A04Z"
    And I do physical inventory with "100" by fnm "01A05"
    And I do physical inventory with "100" by fnm "08S36"
    And I do physical inventory with "100" by fnm "08S01ZY"

    And I search drug by fnm "08S01ZY"
    And I press "Complete"
    And I sign with "sign"
    # the line above just does not work on physical devices ......
    Then I wait for "STOCK CARD OVERVIEW" to appear
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I should see SOH of "08S42B" is "100"
    Then I should see SOH of "08S18Y" is "100"
    Then I should see SOH of "08S40Z" is "100"

    # Sign out
    When I navigate back
    And I navigate back
    And I wait for 1 second
    And I press the menu key
    And I wait for "Sign Out" to appear
    # note: sometimes the wait for sign out to appear fails, reason unknown
    And I press "Sign Out"
    Then I wait for the "LoginActivity" screen to appear
