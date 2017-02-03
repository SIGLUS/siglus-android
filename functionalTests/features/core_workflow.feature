@core @dev @change_date
Feature: Core Workflow

  Scenario: Initial inventory, movements, physical inventory, mmia and via
    Given I change device date to "20160216.130000"
    Given I try to log in with "core" "password1"
    Given I have initialized inventory with MMIA and VIA products with lot

    And I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    And I make a new movement "[01A01]" "Issues" "PAV" "10"
    And I make a new movement "[08S18Y]" "Issues" "PAV" "2"
    And I make a new movement "[01A04Z]" "Entries" "District( DDM)" "2"
    And I make a new movement "[08S36]" "Positive Adjustments" "Loans received at the health facility deposit" "2"
    And I make a new movement "[01A05]" "Negative Adjustments" "Return to DDM" "2"

    Given I change device date to "20160218.140000"
    And I wait for 2 seconds
    And I press "Stock Overview"
    And I try to log in with "core" "password1"
    And I wait for "Stock Card Overview" to appear

    And I press "Requisitions"
    Then I wait for "Requisitions" to appear
    Then I should see text containing "No Requisition Balancete has been created."

    Then I press "Complete Inventory"
    And I wait for "inventory" to appear

    Then I do physical inventory for via and mmia items

    Then I wait for "Requisitions" to appear
    And I press "Create Requisition Balancete"
    And I press "Thursday"
    Then I wait for 1 second
    And I press "Next"

    Then I enter consultationsNub "888"
    Then I swipe to the left in via requisition form
    Then I should see "113" on index "1" of "tx_theoretical" field

    When I press the menu key
    And I wait for "Add Products" to appear
    And I press "Add Products"
    And I wait for "Add Products" to appear
    And I select new drug enter requested "123" by product name "Digoxina; 0,5mg/2mL; Inject"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I should see "Digoxina; 0,5mg/2mL; Inject"

    Then I swipe right
    Then I swipe right
    Then I should see "123" in the requisition form
    Then I enter QuantityRequested "345"
    Then I wait for 1 second
    Then I press "Save"
    Then I wait for "Requisitions" to appear

    And I press "Continue Working on Requisition Balancete"
    And I wait for "Requisition -" to appear
    And I rotate the page to "landscape"
    Then I swipe right
    Then I swipe right
    Then I should see "345" in the requisition form

    And I press "Submit for Approval"
    And I sign requisition with "superuser" "testUser" and complete
    Then I wait for "Requisitions" to appear

    Then I wait up to 120 seconds to see "successfully submitted"

    Then I wait for 1 second
    Then I navigate back
    Then I wait for "Stock Card Overview" to appear

    And I press "MMIA"
    Then I wait for "MMIA" to appear
    Then I should see text containing "No MMIA has been created."

    Then I wait for "MMIA" to appear
    Then I should see text containing "Create MMIA"

    Then I press "Create MMIA"
    Then I should see "Select inventory to close period"
    And I press "Thursday"
    And I press "Next"
    Then I wait for "MMIA -" to appear
    Then I wait for 1 second
    Then I should see text containing "Tenofovir/Lamivudina/Efavirenz; 300mg + 300mg + 600mg 30Comp; Embalagem"
    Then I swipe right
    Then I wait for 1 second
    Then I should see issued movement "2"
    Then I should see inventory "121"
    Then I swipe left

    Then I scroll "scrollView" down to "Submit for Approval"
    Then I press "Submit for Approval"
    Then I should see "Invalid Input"

    And I enter patient totals

    Then I navigate back
    Then I should see text containing "Are you sure you want to quit without saving your work?"
    Then I press "No"
    Then I press "Save"
    Then I wait for 1 second
    Then I should see text containing "Continue Working on MMIA"
    And I press "Continue Working on MMIA"
    Then I wait for "MMIA -" to appear
    Then I wait for 1 second
    Then I scroll "scrollView" down to "Therapeutic Regime"
    And I enter regimen totals
    And I add custom regimens and enter total
    And I scroll "scrollView" down to "Submit for Approval"

    Then I press "Submit for Approval"
    And I sign requisition with "superuser" "testUser" and complete

    Then I should see text containing "Your MMIA form has been successfully saved,"
    Then I wait for "MMIA" to appear

    Then I wait up to 120 seconds to see "successfully submitted"
    Then I should see text containing "You will be able to create a MMIA on the 18th of"

