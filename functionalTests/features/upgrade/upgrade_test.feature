@upgrade
Feature: After version upgrade, data integrity should not be broken

  Scenario: After version upgrade, user should still be able to use all pages with proper data
    When I try to log in with "Michafutene" "password1"
    And I wait up to 120 seconds for "Stock Card Overview" to appear
    # to run this in a physical device, we need to wait longer, IO is slow on physical devices

    #after upgrade, user should still be able to see stock cards and stock movements history
    Then I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    Then I select drug number 1
    And I wait for "Stock Card" to appear
    Then I should see more than 0 movements in stock card page
    Then I press "Movement History"
    And I wait for "Date of Movement" to appear
    Then I should see more than 0 movements in movement history page

    #after upgrade, user should still be able to create new stock card
    When I navigate back
    And I wait for "Stock Card" to appear
    And I navigate back
    And I wait for "Stock Overview" to appear
    When I press the menu key
    Then I see "Add new product"
    When I press "Add new product"
    And I wait for "Add new product" to appear
    Then I add new drug number 1 with SOH 12345 quantity, then I see the added drug in stock overview

    #after upgrade, user should still be able to sync success
    And I navigate back
    And I wait for "Stock Card Overview" to appear
    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"
    Then I see "0 minutes since last sync"

    #after upgrade, user should still be able to use the rnr form draft that are created on the old version
    And I press "Via Classica Requisitions"
    Then I wait for "Via Classica Requisitions" to appear
    And I press "Continue Working on Via Classica Requisition"
    And I wait for "Requisition -" to appear
    Then I should see consultations number is "888"


