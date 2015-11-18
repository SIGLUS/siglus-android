@Requisition
Feature: Requisition

  Scenario: Go to requisition page and display all VIA products
    Given I am logged in
    Given I have initialized inventory

    And I press "Create a Via Classica Requisition"
    Then I wait for the "RequisitionActivity" screen to appear
    Then I should see text containing "Digoxina 0,25mg Comp"
    Then I should see "4" products

  Scenario: Pop up alert
    Given I am logged in
    And I press "Create a Via Classica Requisition"
    Then I wait for the "RequisitionActivity" screen to appear
    Then I enter consultationsNub "2015"
    Then I wait for 1 second
    Then I go back
    Then I wait to see "Are you sure you want to quit without saving your work?"
    Then I press "Yes"
    Then I wait for the "HomeActivity" screen to appear

  Scenario: Save requisition draft and complete
    Given I am logged in
    And I press "Create a Via Classica Requisition"
    When I enter consultationsNub "888"
    Then I swipe right
    Then I swipe right
    Then I swipe right
    Then I enter QuantityRequested "345"
    And I press "Save"

    And I wait for the "HomeActivity" screen to appear
    When I press view with id "btn_requisition"
    Then I wait for the "RequisitionActivity" screen to appear
    Then I swipe right
    Then I swipe right
    Then I should see "345"
    Then I press "Submit for Approval"
    And I sign via with "superuser" "testUser" and complete
    Then I wait for the "RequisitionActivity" screen to appear

Scenario: Add A Issued Movement on VIA product,then the quantity should change
    Given I am logged in
    And I press "Stock Card"
    Then I wait for the "StockCardListActivity" screen to appear
    Then I wait for 1 second
    And I make a movement "[01A01]" "Issues" "PAV" "issued" "10"
    Then I wait for 1 second
    Then I go back
    Then I wait for the "HomeActivity" screen to appear

    When I press view with id "btn_requisition"
    Then I swipe right
    Then I should see "113" on index "1" of "tx_theoretical" field

