@REQUISITION
Feature: Requisition

  Scenario: Save requisition draft
    Given I try to log in with "via" "password1"
    Given I have initialized inventory with VIA user
    And I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    And I make a movement "[01A01]" "Issues" "PAV" "issued" "10"
    Then I wait for 1 second
    Then I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear


    And I press "Via Classica Requisitions"
    Then I wait for "Historic Requisitions" to appear
    Then I should see text containing "No Via Classica Requisition has been created."

    Then I press "Complete Inventory"
    And I wait for "inventory" to appear
    Then I do physical inventory for via items

    And I press "Via Classica Requisitions"
    Then I wait for "Historic Requisitions" to appear
    Then I should see text containing "Create Via Classica Requisition"

    And I press "Create Via Classica Requisition"
    Then I enter consultationsNub "2015"
    Then I wait for 1 second
    Then I navigate back
    Then I wait to see "Are you sure you want to quit without saving your work?"
    Then I press "Yes"
    Then I wait for "Historic Requisitions" to appear

    And I press "Create Via Classica Requisition"
    And I should see empty consultations number
    Then I enter consultationsNub "888"
    Then I swipe right
    Then I should see "113" on index "1" of "tx_theoretical" field
    Then I swipe right
    Then I swipe right
    Then I enter QuantityRequested "345"
    Then I wait for 1 second
    Then I press "Save"
    Then I wait for "Historic Requisitions" to appear

    And I press "Continue Working on Via Classica Requisition"
    And I wait for "Requisition -" to appear
    And I rotate the page to "landscape"
    Then I swipe right
    Then I should see "345" in the requisition form

    When I enter kit values
    And I press "Submit for Approval"
    And I sign requisition with "superuser" "testUser" and complete
    Then I wait for "Historic Requisitions" to appear

    Then I navigate back
    Then I wait for "Stock Card Overview" to appear
    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"

    And I wait for "0 minutes since last sync" to appear
    And I press "0 minutes since last sync"
    Then I see "Requisition last synced 0 minutes ago"
    Then I go back

    And I press "Via Classica Requisitions"
    Then I wait for "Historic Requisitions" to appear
    Then I should see text containing "View Via Classica Requisition"
