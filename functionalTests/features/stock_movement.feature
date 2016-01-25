@STOCK_MOVEMENT @dev
Feature: stock movement Page

  Background: Navigate to Home Page
    Given I try to log in with "stock_card" "password1"

  Scenario: Navigate to Home Page
    Given I have initialized inventory

  Scenario: deactivated product show notify banner
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I select stock card code called "[01A04Z]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    Then I should not see "This product has been deactivated and is not available to reorder"
    And I select a reason "Negative Adjustments" "Damaged on arrival"
    Then I wait for 1 second
    Then I swipe right
    And I enter negative adjustment number "123"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"
    Then I see "123"
    Then I see "super" in signature field
    Then I navigate back
    Then I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear

    #deactivate product
    Given server deactivates products has stock movement
    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I should see "has been deactivated and removed"

    # reactive product
    When server reactive products
    Then I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear
    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I should not see "has been deactivated and removed"

    #issued stock movement
    Then I select stock card code called "[01A03Z]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    Then I should see "This product has been deactivated and is not available to reorder"
    Then I wait for 1 second
    And I select a reason "Issues" "PAV"
    Then I wait for 1 second
    Then I swipe right
    And I enter issued number "123"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"
    Then I see "123"
    Then I see "super" in signature field
    Then I navigate back
    Then I should see "has been deactivated and removed"

    #clear warning banner
    And I clear banner message
    Then I should not see "has been deactivated and removed"

  Scenario: Add all movements for one drug when is STRESS TEST
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I make all movements for "08S18Y"
    Then I wait for 1 second
    Then I navigate back
    Then I wait for "STOCK CARD OVERVIEW" to appear


