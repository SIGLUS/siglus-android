@regression @change_date
Feature: Log in and initialize Inventory

  Scenario: User should be able to log in, initialize inventory and navigate to stock overview page

    Given I change device date to "20160216.130000"
    #Unauthenrised account shouldn't login to the app
    Given I try to log in with "testlogin" "password1"
    And I should see "Username or Password is incorrect."
    Then I wait for 1 second

    # Initialize inventory and check stock card overview
    Given I try to log in with "superuser" "password1"
    And I wait up to 180 seconds for "Initial Inventory" to appear
    # to run this in a physical device, we need to wait longer, IO is slow on physical devices

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
    And I wait for 1 second
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

    # Attempt to make a stock movement which make its soh negative
    And I wait for 1 second
    And I select a reason "Negative Adjustments" "Damaged on arrival"
    And I wait for 1 second
    And I swipe right
    And I enter negative adjustment number "123456789098"
    And I wait for "Complete" to appear
    And I press "Complete"
    Then I should see text containing "Invalid Input"
    And I wait for 2 seconds
    And I press "CANCEL"

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
    And I wait for "Enter your initials" to appear
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
    And I select a reason "Positive Adjustments" "Loans received at the health facility deposit"
    Then I swipe right
    Then I wait for 1 second
    And I enter positive adjustment number "2"
    And I rotate the page to "landscape"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I wait for "Enter your initials" to appear
    And I sign with "superuser"
    # And I wait for 1 second
    # And I swipe left
    # And I swipe left
    # And I wait for 1 second
    # Then I see the text "Donations to Deposit"
    # And I swipe right
    # And I swipe right
    # And I wait for 1 second
    # And I see "125"
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
    And I wait for "Enter your initials" to appear
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
    And I wait for "Enter your initials" to appear
    And I sign with "superuser"
    Then I see "0"
    When I press the menu key
    Then I see "Archive drugs"
    When I press "Archive drugs"
    And I wait for "Stock Overview" to appear
    Then I should see total:"9" on stock list page
    And I don't see the text "[08S32Z]"

    # Archived drugs don't appear in monthly inventory
    Given I change device date to "20160218.140000"
    And I navigate back
    And I wait for 2 seconds
    And I try to log in with "superuser" "password1"
    And I wait up to 180 seconds for "STOCK CARD OVERVIEW" to appear

    And I press "Inventory"
    And I wait for "Inventory" to appear
    Then I shouldn't see product "01A01" in this page
    And I shouldn't see product "08S32Z" in this page

    # Physical inventory cannot include blank quantities
    And I search drug by fnm "08S01ZY"
    When I press "Complete"
    Then I should see text containing "Quantity cannot be left blank!"

    # Do physical inventory and SOH should be adjusted
    When I do physical inventory with "100" by fnm "08S42B"
    And I do physical inventory with "100" by fnm "08S18Y"
    And I do physical inventory with "100" by fnm "08S40Z"
    #And I do physical inventory with "100" by fnm "01A01"
    And I do physical inventory with "100" by fnm "01A03Z"
    And I do physical inventory with "100" by fnm "01A02"
    And I do physical inventory with "100" by fnm "01A04Z"
    And I do physical inventory with "100" by fnm "01A05"
    And I do physical inventory with "100" by fnm "08S36"
    And I do physical inventory with "100" by fnm "08S01ZY"

    And I search drug by fnm "08S01ZY"
    And I press "Complete"
    And I wait for "Enter your initials" to appear
    And I sign with "sign"
    Then I wait for "STOCK CARD OVERVIEW" to appear
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I should see SOH of "08S42B" is "100"
    Then I should see SOH of "08S18Y" is "100"
    Then I should see SOH of "08S40Z" is "100"

    #Archived drugs don't appear in via
    When I navigate back
    And I wait for 1 second
    And I navigate back
    And I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Requisitions"

    Then I wait for "Requisitions" to appear
    Then I should see text containing "Create Requisition Balancete"

    When I press "Create Requisition Balancete"
    Then I should see "Select inventory to close period"
    And I press "Thursday"
    And I press "Next"
    Then I should see "to 18 Feb"

    Then I should not see "01A01"

    #columns of Archived drugs are 0 in mmia
    When I navigate back
    Then I wait to see "Are you sure you want to quit without saving your work?"
    When I press "Yes"
    Then I wait for "Requisitions" to appear
    When I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear
    When I press "MMIA"
    Then I should see text containing "Create MMIA"
    When I press "Create MMIA"
    Then I should see "Select inventory to close period"
    And I press "Thursday"
    And I press "Next"
    Then I wait for "MMIA -" to appear
    Then I should see "to 18 Feb"

    Then I swipe right
    Then I wait for 1 second
    And I should see inventory "0"


    # Archived drugs screen
    When I navigate back
    Then I wait to see "Are you sure you want to quit without saving your work?"
    When I press "Yes"
    Then I wait for "Create MMIA" to appear
    When I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I press the menu key
    And I press "Archived drugs"
    And I wait for "Archived drugs" to appear
    Then I see the text "[01A01]"

    # Stock movement history screen
    When I press "View movement history"
    Then I see the text "Inventory"
    Then I see the text "Maternity"

    # Unarchive a drug from archived page
    When I navigate back
    And I press "Add drug to stock overview"
    And I navigate back
    And I wait for "Stock Overview" to appear
    Then I should see total:"10" on stock list page
    Then I see the text "[01A01]"

    #Unarchive a drug from add new drug page
    When I press the menu key
    And I wait for "Add new product" to appear
    And I press "Add new product"
    And I wait for "Add new product" to appear
    And I select new drug "08S32Z"
    Then I wait for "Complete" to appear
    And I press "Complete"

    # Unarchived drug shows up in monthly inventory
    Then I wait for "Stock Overview" to appear
    When I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Inventory"
    And I wait for "Inventory" to appear
    Then I should see product "01A01" in this page
    And I clean search bar
    Then I should see product "08S32Z" in this page

    #Unarchived drugs appear in via
    And I navigate back
    And I wait for 1 second
    And I navigate back
    And I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Requisitions"
    Then I wait for "Requisitions" to appear
    Then I should see text containing "Create Requisition Balancete"

    When I press "Create Requisition Balancete"
    Then I should see "Select inventory to close period"
    And I press "Thursday"
    And I press "Next"

    Then I should see "01A01"

    # Sign out
    When I navigate back
    Then I wait to see "Are you sure you want to quit without saving your work?"
    When I press "Yes"
    Then I wait for "Requisitions" to appear
    When I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear
    And I press the menu key
    And I wait for "Sign Out" to appear
    # note: sometimes the wait for sign out to appear fails, reason unknown
    And I press "Sign Out"
    Then I wait for the "LoginActivity" screen to appear

