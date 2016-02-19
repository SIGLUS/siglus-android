@regression
Feature: User re-selects movement reason after already entering numbers

  Scenario: Movement fields should be cleared if user changes movement reason
    Given I try to log in with "superuser" "password1"
    And I wait up to 30 seconds for "Initial Inventory" to appear
    When I search product by fnm "08S36" and select this item with quantity "123"
    And I wait for "Complete" to appear
    And I press "Complete"
    Then I wait for "STOCK CARD OVERVIEW" to appear

    When I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I select stock card code called "[01A01]"
    And I wait for "Stock Card" to appear
    And I rotate the page to "landscape"
    And I wait for "Stock Card" to appear
    And I select a reason "Positive Adjustments" "Donations to Deposit"
    And I wait for 1 second
    And I enter "888" into documentNo
    And I wait for 1 second
    And I enter positive adjustment number "41"
    And I wait for 1 second
    And I select a reason "Negative Adjustments" "Damaged on arrival"
    And I wait for 1 second
    Then I should not see "888"
    And I should not see "41"