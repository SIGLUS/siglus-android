@REQUISITION @weekly @change_date
Feature: Requisition

  Scenario: Save requisition draft
    Given I change device date to "20160128.130000"
    Given I try to log in with "via" "password1"
    Given I have initialized inventory with VIA user with lot
    And I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    And I make a new movement "[01A01]" "Issues" "PAV" "10"
    Then I wait for 1 second

     # Archive VIA drug
    And I select stock card code called "[01A02]"
    And I wait for "Stock Card" to appear

    Then I press "NEW MOVEMENT"
    Then I select a new movement reason "Issues" "Maternity"
    Then I wait for 1 second
    Then I select movement date
    Then I wait for 1 second
    Then I enter signature "super"
    When I enter quantity "123" for the last lot
    And I press "Complete"
    Then I swipe right
    Then I swipe right

    Then I see "0"
    And I press the menu key
    Then I see "Archive drugs"
    And I press "Archive drugs"
    And I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear

    And I press "Requisitions"
    Then I wait for "Requisitions" to appear
    Then I should see text containing "You will be able to create a requisition on the 18th of Feb"
    And I navigate back

    Given I change device date to "20160218.140000"
    And I wait for 2 seconds
    And I press "Requisitions"
    And I try to log in with "via" "password1"
    And I wait for "Stock Card Overview" to appear

    And I press "Requisitions"
    Then I wait for "Requisitions" to appear
    Then I should see text containing "No Requisition Balancete has been created."

    Then I press "Complete Inventory"
    And I wait for "inventory" to appear
    Then I do physical inventory for via items without archived drugs

    Then I wait for "Requisitions" to appear
    And I should see text containing "Create Requisition Balancete"

    And I press "Create Requisition Balancete"
    Then I should see "Select inventory to close period"
    And I press "Thursday"
    And I press "Next"
    Then I should see "to 18 Feb"
    Then I enter consultationsNub "2015"
    Then I wait for 1 second
    Then I navigate back
    Then I wait to see "Are you sure you want to quit without saving your work?"
    Then I press "Yes"
    Then I wait for "Requisitions" to appear

    And I press "Create Requisition Balancete"
    And I press "Thursday"
    And I press "Next"
    And I should see empty consultations number
    Then I enter consultationsNub "888"
    Then I swipe to the left in via requisition form
    Then I should see "113" on index "1" of "tx_theoretical" field

    #add not archive and archive product to via form
    When I press the menu key
    And I wait for "Add Products" to appear
    And I press "Add Products"
    And I wait for "Add Products" to appear
    And I select new drug enter requested "123" by product name "Digoxina; 0,5mg/2mL; Inject"
    And I select new drug enter requested "123" by product name "Digoxina; 2,5mg/50mL; Gotas Orais"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I should see "Digoxina; 0,5mg/2mL; Inject"
    And I should see "Digoxina; 2,5mg/50mL; Gotas Orais"

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

    Then I navigate back
    Then I wait for "Stock Card Overview" to appear
    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"

    And I wait up to 60 seconds for "0 minutes since last sync" to appear
    And I press "0 minutes since last sync"
    Then I see "Requisition last synced 0 minutes ago"
    Then I go back

    And I press "Requisitions"
    Then I wait for "Requisitions" to appear
    Then I should see text containing "View Requisition Balancete"
    Then I should see text containing "You will be able to create a requisition on the 18th of"


