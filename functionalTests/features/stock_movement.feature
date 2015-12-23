@StockMovement
Feature: stock movement Page

  Background: Navigate to Home Page
    Given I try to log in with "stock_card" "password1"

  Scenario: Navigate to Home Page
    Given I have initialized inventory

  Scenario: Bottom Btn Logic
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
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

  Scenario: Add A Receive Movement
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I select stock card code called "[01A05]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    And I select a reason "Entries" "District( DDM)"
    Then I wait for 1 second
    And I enter received number "2"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"
    Then I see "125"
    Then I see "super" in signature field
    Then I navigate back
    Then I wait for 1 second
    Then I navigate back
    Then I wait for "Home Page" to appear


  Scenario: Add A Issued Movement
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I select stock card code called "[01A05]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    And I select a reason "Issues" "PAV"
    Then I wait for 1 second
    Then I swipe right
    And I enter issued number "2"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"
    Then I see "123"
    Then I see "super" in signature field
    Then I navigate back
    Then I wait for 1 second
    Then I navigate back
    Then I wait for "Home Page" to appear

  Scenario: Add A Negative Adjustment
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I select stock card code called "[01A02]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    And I select a reason "Negative Adjustments" "Damaged on arrival"
    Then I wait for 1 second
    And I enter negative adjustment number "2"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"
    Then I see "121"
    Then I navigate back
    Then I wait for 1 second
    Then I navigate back
    Then I wait for "Home Page" to appear

  Scenario: Add A Positive Adjustment
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I select stock card code called "[01A02]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    And I select a reason "Positive Adjustments" "Donations to Deposit"
    Then I swipe right
    Then I wait for 1 second
    And I enter positive adjustment number "2"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"
    Then I see "123"
    Then I navigate back
    Then I wait for 1 second
    Then I navigate back
    Then I wait for "Home Page" to appear

  Scenario: Add all movements for one drug
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I make all movements for "08S18Y"
    Then I wait for 1 second
    Then I navigate back
    Then I wait for "Home Page" to appear



#  TODO swipe left does not work
#  Scenario: ReSelect Adjust Reason
#    When I press "Stock Card Overview"
#    Then I wait for "Stock Overview" to appear
#    Then I select stock card code called "08S36"
#    Then I wait for "Stock Card" to appear
#    And I select a reason "Positive Adjustments" "Donations to Deposit"
#    Then I wait for 1 second
#    And I enter "888" into documentNo
#    Then I swipe right
#    Then I wait for 1 second
#    And I enter positive adjustment number "41"
#    Then I wait for 2 seconds
#    Then I swipe left
#    Then I wait for 1 second
#    And I select a reason "Positive Adjustments" "Donations to Deposit"
#    Then I should not see "888"
#    Then I swipe right
#    Then I should not see "41"



