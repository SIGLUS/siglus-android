@mmia @dev
Feature: Log in and make movements with initialized products

  Scenario: 04 - MMIA
    Given I log in into the application
    And I press "MMIA"
    And I wait for mmia page is initialized
    And I press "Create MMIA"
    And I select current MMIA period
    And I press next to continue with the MMIA form
    And I swipe "right" 1 time in MMIA form
    And I check the quantity for "Abacavir sulfato (ABC) 300mg,60comp Embalagem" is equals to 120 in MMIA form
    And I scroll down to the bottom of the MMIA form
    And I press "Submit for Approval"
    And I should see "Invalid Input"
    And I enter regimen totals
    And I enter patient totals
    And I add a "+ Adult regime"
    And I search for product "Lamivudina(3TC); 150mg 60Comp; Embalagem" in MMIA requisition
    And I add a "+ Child regime"
    And I search for product "Lamivudina(3TC); 150mg 60Comp; Embalagem" in MMIA requisition
    And I scroll down to the bottom of the MMIA form
    And I navigate back
    And I should see text containing "Are you sure you want to quit without saving your work?"
    And I press "No"
    And I save MMIA form
    And I should see text containing "Continue Working on MMIA"
    And I press "Continue Working on MMIA"
    And I scroll down to the bottom of the MMIA form
    And I add data to new regimens
    And I submit MMIA form
    And I sign using "TWUIO" as initials
    And I press continue to finish approval
    And I press "Complete"
    When I sign using "TWUIO" as initials
    Then I wait up to 30 seconds to see "successfully submitted"