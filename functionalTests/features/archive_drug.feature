@ARCHIVE_DRUG
Feature: Archive drug

  Scenario: Archive one drug
    Given I try to log in with "initial_inventory" "password1"
    Then I wait for "Initial Inventory" to appear
    Then I wait for 1 second
    Given I have initialized inventory
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    Then I select stock card code called "[01A01]"
    Then I wait for "Stock Card" to appear
    And I select a reason "Issues" "PAV"
    Then I wait for 1 second
    Then I swipe right
    And I enter issued number "123"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"

    Then I see "0"
    Then I see "super" in signature field
    And I press the menu key
    Then I see "Archive drugs"
    And I press "Archive drugs"
    And I wait for "Stock Overview" to appear
    Then I should see total:"9" on stock list page
    Then I don't see the text "[01A01]"

  Scenario: View stock history page via Archived drugs page
    Given I try to log in with "initial_inventory" "password1"
    And I wait for "Home Page" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I press the menu key
    And I press "Archived drugs"
    And I wait for "Archived drugs" to appear
    And I press "View movement history"
    And I wait for the "StockMovementHistoryActivity" screen to appear
    Then I see the text "Inventory"
    Then I see the text "PAV"

  Scenario: Unarchive one drug
    Given I try to log in with "initial_inventory" "password1"
    And I wait for "Home Page" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I press the menu key
    And I press "Archived drugs"
    And I wait for "Archived drugs" to appear
    Then I see the text "[01A01]"
    And I press "Add drug to stock overview"
    Then I see the text "There are no archived drugs"
    And I navigate back
    And I wait for "Stock Overview" to appear
    Then I should see total:"10" on stock list page
    Then I see the text "[01A01]"

