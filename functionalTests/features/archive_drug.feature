@ARCHIVE_DRUG @dev
Feature: Archive drug

  Scenario: Archive two drugs
    Given I try to log in with "initial_inventory" "password1"
    Then I wait for "Initial Inventory" to appear
    Then I wait for 1 second
    Given I have initialized inventory
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    Then I select stock card code called "[08S32Z]"
    Then I wait for "Stock Card" to appear
    And I select a reason "Negative Adjustments" "Damaged on arrival"
    Then I wait for 1 second
    Then I swipe right
    And I enter negative adjustment number "123"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I wait for "Enter your initials" to appear
    And I sign with "superuser"

    Then I see "0"
    Then I see "super" in signature field
    And I press the menu key
    Then I see "Archive drugs"
    And I press "Archive drugs"
    And I wait for "Stock Overview" to appear
    Then I don't see the text "[08S32Z]"

    # Server updates drugs including 08S32Z
    Given Server updates drug data
    When I navigate back
    And I wait for "STOCK CARD OVERVIEW" to appear

    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"

    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    # Archived drugs should stay as archived after server update
    Then I shouldn't see product "08S32Z" in this page