@MMIA @dev @change_date
Feature: MMIA

  Background: Navigate to Home Page
    Given I try to log in with "mmia" "password1"

  Scenario: Initial a MMIA
    Given I have initialized inventory with MMIA user
    And I change device date to "20160121.130000"
    And I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    And I make a movement "[08S42B]" "Issues" "PAV" "issued" "2"
    Then I wait for "Stock Overview" to appear
    Then I wait for 1 second
    Then I navigate back
    Given I change device date to "20160117.130000"
    And I wait for 2 seconds

    And I press "MMIA"
    Then I wait for "MMIA" to appear
    Then I should see text containing "You will be able to create a requisition on the 18th of Jan"
    And I navigate back

    Given I change device date to "20160121.130000"
    And I wait for 2 seconds
    And I press "MMIA"
    And I try to log in with "mmia" "password1"
    And I wait for "Stock Card Overview" to appear

    And I press "MMIA"
    Then I wait for "MMIA" to appear
    Then I should see text containing "No MMIA has been created."
    Then I press "Complete Inventory"
    And I wait for "inventory" to appear
    Then I do physical inventory for mmia items

    Then I wait for "MMIA" to appear
    Then I should see text containing "Create MMIA"

    Then I press "Create MMIA"
    Then I should see "Select inventory to close period"
    And I press "Thursday"
    And I press "Next"
    Then I wait for "MMIA -" to appear
    Then I wait for 1 second
    Then I scroll down until I see the "Zidovudina/Lamivudina/Nevirapi; 60mg+30mg+50mg"
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
    And I press "Thursday"
    And I press "Next"
    Then I wait for "MMIA -" to appear
    Then I wait for 1 second
    Then I scroll "scrollView" down to "Submit for Approval"
    And I should see empty patient total

    And I enter regimen totals
    Then I navigate back
    Then I should see text containing "Are you sure you want to quit without saving your work?"
    Then I press "No"
    Then I wait for "MMIA -" to appear
    Then I wait for 1 second
    
    Then I scroll "scrollView" down to "Submit for Approval"
    Then I press "Submit for Approval"
    Then I should see "Invalid Input"
    
    Then I press "Save"
    Then I wait for 1 second
    Then I should see text containing "Continue Working on MMIA"

    And I press "Continue Working on MMIA"
    And I wait for "MMIA -" to appear
    Then I scroll "scrollView" down to "Submit for Approval"
    And I enter patient totals
    Then I press "Submit for Approval"
    And I sign requisition with "superuser" "testUser" and complete

    Then I should see text containing "Your MMIA form has been successfully saved,"
    Then I wait for "MMIA" to appear

    Then I navigate back
    Then I wait for "Stock Card Overview" to appear
    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"

    And I wait for "0 minutes since last sync" to appear
    And I press "0 minutes since last sync"
    Then I see "Requisition last synced 0 minutes ago"
    Then I go back

    And I press "MMIA"
    Then I wait for "MMIA" to appear
    Then I should see text containing "View MMIA"

    Then I should see text containing "You will be able to create a requisition on the 18th of"

    When I press the pop menu for delete rnr form
    And I press "Remove from tablet"
    And I press "Delete"
    Then I see "Create MMIA"