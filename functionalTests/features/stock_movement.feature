@StockMovement
Feature: stock movement Page

  Scenario: Navigate to Home Page
    Given I am logged in
    Given I have initialized inventory

  Scenario: Bottom Btn Logic
    Given I am logged in
    And I press "Stock Card Overview"
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
    Given I am logged in
    And I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I select stock card code called "[08S42B]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    And I select a reason "Entries" "District( DDM)"
    Then I wait for 1 second
    And I enter received number "2"
    And I press "Complete"
    And I sign stock movement with "superuser"
    Then I see "125"
    Then I see "superuser" in signature field
    Then I navigate back
    Then I wait for 1 second
    Then I navigate back
    Then I wait for "Home Page" to appear

  Scenario: Add A Negative Adjustment
    Given I am logged in
    And I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I select stock card code called "[01A02]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    And I select a reason "Negative Adjustments" "Damaged on arrival"
    Then I wait for 1 second
    And I enter negative adjustment number "2"
    And I press "Complete"
    And I sign stock movement with "superuser"
    Then I see "121"
    Then I navigate back
    Then I wait for 1 second
    Then I navigate back
    Then I wait for "Home Page" to appear

  Scenario: Add A Positive Adjustment
    Given I am logged in
    And I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I select stock card code called "[01A05]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    And I select a reason "Positive Adjustments" "Donations to Deposit"
    Then I swipe right
    Then I wait for 1 second
    And I enter positive adjustment number "2"
    And I press "Complete"
    And I sign stock movement with "superuser"
    Then I see "125"
    Then I navigate back
    Then I wait for 1 second
    Then I navigate back
    Then I wait for "Home Page" to appear

  Scenario: Add A Issued Movement
    Given I am logged in
    And I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I select stock card code called "[08S18Y]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    And I select a reason "Issues" "PAV"
    Then I wait for 1 second
    Then I swipe right
    And I enter issued number "2"
    And I press "Complete"
    And I sign stock movement with "superuser"
    Then I see "121"
    Then I see "superuser" in signature field
    Then I navigate back
    Then I wait for 1 second
    Then I navigate back
    Then I wait for "Home Page" to appear

  Scenario: Add all movements for one drug
    Given I am logged in
    And I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I make all movements for "08S18Y"
    Then I wait for 1 second


