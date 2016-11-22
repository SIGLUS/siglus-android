@MMIA @weekly @change_date
Feature: MMIA

  Scenario: Initial a MMIA
    Given I change device date to "20160216.130000"
    And I try to log in with "mmia" "password1"

    Given I have initialized inventory with MMIA user with lot
    And I press "MMIA"
    Then I wait for "MMIA" to appear
    Then I should see text containing "You will be able to create a MMIA on the 18th of Feb"

    And I change device date to "20160221.140000"
    And I navigate back
    And I wait for 2 seconds
    And I try to log in with "mmia" "password1"
    And I wait for "Stock Card Overview" to appear
    And I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear

    Then I wait for 1 second
    And I make a new movement "[08S18Y]" "Issues" "PAV" "2"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I navigate back

    And I press "MMIA"
    Then I wait for "MMIA" to appear
    Then I should see text containing "No MMIA has been created."
    Then I press "Complete Inventory"
    And I wait for "inventory" to appear
    Then I do physical inventory with lot for mmia items

    Then I wait for "MMIA" to appear
    Then I should see text containing "Create MMIA"

    Then I press "Create MMIA"
    Then I should see "Select inventory to close period"
    And I press "Sunday"
    And I press "Next"
    Then I wait for "MMIA -" to appear
    Then I wait for 1 second
    Then I should see text containing "Tenofovir/Lamivudina/Efavirenz; 300mg + 300mg + 600mg 30Comp; Embalagem"
    Then I swipe right
    Then I wait for 1 second
    Then I should see issued movement "2"
    Then I should see inventory "121"
    Then I swipe left
    
    Then I scroll "scrollView" down to "Submit for Approval"
    Then I press "Submit for Approval"
    Then I should see "Invalid Input"
    
    And I enter patient totals

    Then I navigate back
    Then I should see text containing "Are you sure you want to quit without saving your work?"
    Then I press "Yes"
    Then I wait for 1 second

    Then I press "Create MMIA"
    And I press "Sunday"
    And I press "Next"
    Then I wait for "MMIA -" to appear
    Then I wait for 1 second
    Then I scroll "scrollView" down to "Submit for Approval"

    And I enter patient totals
    Then I navigate back
    Then I should see text containing "Are you sure you want to quit without saving your work?"
    Then I press "No"
    Then I press "Save"
    Then I wait for 1 second
    Then I should see text containing "Continue Working on MMIA"
    And I press "Continue Working on MMIA"
    Then I wait for "MMIA -" to appear
    Then I wait for 1 second
    Then I scroll "scrollView" down to "Therapeutic Regime"
    And I enter regimen totals
    And I add custom regimens and enter total
    And I scroll "scrollView" down to "Submit for Approval"

    Then I press "Submit for Approval"
    And I sign requisition with "superuser" "testUser" and complete

    Then I should see text containing "Your MMIA form has been successfully saved,"
    Then I wait for "MMIA" to appear

#   TODO remove unnecessary manual sync
    Then I navigate back
    Then I wait for "Stock Card Overview" to appear
    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"

    And I wait up to 60 seconds for "0 minutes since last sync" to appear
    And I press "0 minutes since last sync"
    Then I see "Requisition last synced 0 minutes ago"
    Then I go back

    And I press "MMIA"
    Then I wait for "MMIA" to appear
    Then I should see text containing "View MMIA"

    Then I should see text containing "You will be able to create a MMIA on the 18th of"

    When I press delete icon
    And I press "Remove from tablet"
    And I press "Delete"
    Then I see "Create MMIA"