@unpack_kit_dev @dev
Feature: Unpack Kit

  Scenario: Unpack US kit and verify kit products SOH
    Given I change device date to "20160216.130000"
    And I try to log in with "kit" "password1"
    And I wait up to 90 seconds for "Initial Inventory" to appear
    Then I wait for "Initial inventory" to appear
    When I search lot product by fnm "02E02" and select this item with quantity "888" and lot number "testA"
    When I search lot product by fnm "15C0ZY" and select this item with quantity "2" and lot number "testB"
    Then I press "Complete"

    Then I wait for "MMIA" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    Then I select stock card code called "[15C0ZY]"
    Then I wait for "Stock Card" to appear
    Then I press "NEW MOVEMENT"
    Then I select a new movement reason "Negative Adjustments" "Damaged on arrival"
    Then I wait for 1 second
    Then I select movement date
    Then I press "Done"
    Then I wait for 1 second
    Then I enter signature "super"
    When I enter quantity "2" for the last lot
    And I press "Complete"

    And I wait for 1 second
    And I press the menu key
    And I press "Archive drugs"
    And I wait for "Stock Overview" to appear
    Then I navigate back
    Then I wait for "MMIA" to appear

    Then I press "KITS"
    Then I wait for "Kit Overview" to appear
    Then I press "KITS  (DE PME US)"
    Then I wait for "[SCOD10]" to appear

    Then I press "NEW MOVEMENT"
    Then I select a new movement reason "Entries" "District( DDM)"
    Then I wait for 1 second
    Then I select movement date
    Then I press "Done"
    Then I wait for 1 second
    Then I enter quantity number "3"
    Then I enter signature "super"
    And I press "Complete"

    Then I wait for "Unpack Kit" to appear
    Then I see "1"
    Then I swipe right
    Then I wait for 1 second
    Then I swipe right
    Then I wait for 1 second
    Then I see "super" in signature field

    And I press "Unpack Kit"
    Then I wait for "Next" to appear
    When I press "3"
    And I press "Next"
    Then I wait for "02E02" to appear
    And I should see "Unpacking"

    And I enter document number for unpack kit

    #enter quantity of lot
    And I enter quantity for lots of all products in kit

    #signature
    And I wait for "Please enter your initials to confirm the amounts entered" to appear
    And I sign with "test"

    Then I wait for "[SCOD10]" to appear
    And I should not see "Unpack Kit"
    And I swipe right
    Then I see "test" in signature field

    Then I navigate back
    And I wait for 1 second
    Then I navigate back

    Then I wait for "MMIA" to appear
    And I press "Stock Card Overview"
    Then I should see "Total:44"
    When I search drug by fnm "02E02"
    Then I should see "889"
    And I clean search bar
    When I search drug by fnm "15C0ZY"
    Then I should see "[15C0ZY]"
    Then I select stock card code called "[15C0ZY]"
    Then I should see "District( DDM)"
    And I swipe right
    And I swipe right
    Then I see "test" in signature field
    Then I navigate back
    And I wait for 1 second
    Then I navigate back
    And I wait for 1 second

    Given I change device date to "20160218.140000"
    And I navigate back
    And I wait for 2 seconds
    And I try to log in with "kit" "password1"

    Then I wait for "MMIA" to appear
    And I press "Requisitions"
    Then I wait for "Requisitions" to appear
    Then I should see text containing "No Requisition Balancete has been created."

    Then I press "Complete Inventory"
    And I wait for "inventory" to appear

    And I wait for 1 second
    When I search drug by fnm "15C0ZY"
    Then I should see "[15C0ZY]"
    Then I navigate back
    And I wait for 1 second
#    Then I do physical inventory for all items
    Then I make lots adjustment of physical inventory for all items

    Then I wait for "Requisitions" to appear
    Then I should see text containing "Create Requisition Balancete"

    And I press "Create Requisition Balancete"
    Then I should see "Select inventory to close period"
    And I press "Thursday"
    And I press "Next"
    Then I should see "to 18 Feb"
    Then I should see kit receive number and open number is "3"

    Then I enter consultationsNub "2015"
    Then I wait for 1 second
    Then I navigate back
    Then I wait to see "Are you sure you want to quit without saving your work?"
    Then I press "Yes"
    Then I wait for "Requisitions" to appear

    And I press "Create Requisition Balancete"
    Then I should see "Select inventory to close period"
    And I press "Thursday"
    And I press "Next"
    Then I should see "to 18 Feb"
    Then I should see kit receive number and open number is "3"

    And I should see empty consultations number
    Then I enter consultationsNub "888"
    Then I swipe to the left in via requisition form
    Then I should see "889" on index "2" of "tx_theoretical" field
    Then I swipe right
    Then I swipe right
    Then I enter QuantityRequested "345"
    Then I wait for 1 second
    Then I press "Save"
    Then I wait for "Requisitions" to appear

    And I press "Continue Working on Requisition Balancete"
    And I rotate the page to "landscape"
    Then I swipe right
    Then I should see "345" in the requisition form

    And I press "Submit for Approval"
    And I sign requisition with "superuser" "testUser" and complete
    Then I wait for "Requisitions" to appear

    Then I navigate back
    Then I wait for "Stock Card Overview" to appear
    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"

    And I wait up to 180 seconds for "0 minutes since last sync" to appear
    And I click the last sync banner
    Then I see "Requisition last synced 0 minutes ago"
    Then I go back

    And I press "Requisitions"
    Then I wait for "Requisitions" to appear
    Then I should see text containing "View Requisition Balancete"