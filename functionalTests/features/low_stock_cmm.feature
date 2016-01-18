@LOW_STOCK_CMM
Feature: low stock

  Scenario: Calculate low stock and cmm without stock out in 3 months
    Given Server updates stock_movements data
    And I wait for 2 seconds

    Given I try to log in with "low_stock" "password1"
    And I wait for "Home Page" to appear
    When I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I wait for 1 second
    Then I should see SOH of "01A01" is "400"


    Then I select stock card code called "[01A01]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second

    Then I should see CMM "200"

    And I select a reason "Issues" "Public pharmacy"
    Then I wait for 1 second
    Then I swipe right
    And I enter issued number "390"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "low_stock"
    Then I see "10"
    Then I see "low_s" in signature field
    And I navigate back
    And I wait for 3 seconds
    Then I should see lowStock "10" and warning on product "01A01"

    Then I select stock card code called "[01A01]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    And I select a reason "Positive Adjustments" "Donations to Deposit"
    Then I swipe right
    Then I wait for 1 second
    And I enter positive adjustment number "1"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I sign with "superuser"
    Then I see "11"
    Then I navigate back
    Then I wait for 1 second
    Then I shouldn't see lowStock "11" and warning on product "01A01"
    Then I wait for 1 second

