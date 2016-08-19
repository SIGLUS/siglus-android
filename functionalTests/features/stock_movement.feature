@STOCK_MOVEMENT @dev
Feature: stock movement Page

  Background: Navigate to Home Page
    Given I try to log in with "stock_card" "password1"

  Scenario: deactivated product show notify banner
    Given I have initialized inventory with lot
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I select stock card code called "[01A04Z]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    Then I should not see "This product has been deactivated and is not available to reorder"

    Then I press "NEW MOVEMENT"
    Then I select a new movement reason "Negative Adjustments" "Damaged on arrival"
    Then I wait for 1 second
    Then I select movement date
    Then I press "Done"
    Then I wait for 1 second
    Then I enter quantity number "123"
    Then I enter signature "super"
    And I press "Complete"
    Then I swipe right
    Then I swipe right

    Then I see "123"
    Then I see "super" in signature field
    Then I navigate back
    And I wait for "Stock Overview" to appear
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

    Then I press "NEW MOVEMENT"
    Then I select a new movement reason "Issues" "PAV"
    Then I wait for 1 second
    And I enter quantity number "123"
    And I enter requested quantity "456"
    Then I select movement date
    Then I press "Done"
    Then I wait for 1 second
    Then I enter signature "super"
    And I press "Complete"
    Then I swipe right
    Then I swipe right

    Then I see "123"
    Then I see "456"
    Then I see "super" in signature field
    Then I navigate back
    Then I should see "has been deactivated and removed"

    #Return to DDM stock movement
    Then I select stock card code called "[01A01]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second

    Then I press "NEW MOVEMENT"
    Then I select a new movement reason "Negative Adjustments" "Return to DDM"
    Then I wait for 1 second
    And I enter quantity number "123"
    Then I select movement date
    Then I press "Done"
    Then I wait for 1 second
    Then I enter signature "super"
    And I press "Complete"
    Then I swipe right
    Then I swipe right

    Then I see "123"
    Then I see "super" in signature field

    When I press "NEW MOVEMENT"
    And I select a new movement reason "Entries" "District( DDM)"
    And I wait for 1 second
    And I select movement date
    And I press "Done"
    And I wait for 1 second
    And I enter signature "super"
    And I press "+ Add New Lot"
    And I wait for "lot number" to appear
    And I enter lot number "TEST-123" on add lot page
    And I press "Complete"
    Then I should see "TEST-123"
    And I wait for 1 second
    And I scroll down
    When I press "Complete"
    Then I should see "Quantity cannot be left blank or zero!"
    When I enter quantity "100" for the last lot
    And I press "Complete"
    And I wait for 1 second
    Then I should see "TEST-123"
    And I swipe right
    And I swipe right
    Then I should see "100"
    When I rotate the page to "landscape"
    And I should see "super"
    Then I rotate the page to "portrait"
    And I wait for 1 second

    Then I navigate back
    And I wait for 1 second
    And I wait for "Stock Overview" to appear

    #clear warning banner
    And I clear banner message
    Then I should not see "has been deactivated and removed"


