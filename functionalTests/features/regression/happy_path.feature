@regression
Feature: Log in and initialize Inventory

  Scenario: User should be able to log in, initialize inventory and navigate to stock overview page

    # Initialize inventory and check stock card overview
    Given I try to log in with "superuser" "password1"
    And I wait up to 30 seconds for "Initial Inventory" to appear
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
    Then I see the text "Donations to Deposit"
    And I see "125"
    And I see "super" in signature field

    # Sign out
    When I navigate back
    And I navigate back
    And I press the menu key
    And I sign out
    Then I wait for the "LoginActivity" screen to appear