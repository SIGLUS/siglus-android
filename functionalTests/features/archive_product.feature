@ARCHIVE_PRODUCT @dev

Feature: archive product

  Scenario: user should sync back product archived status when init app
    Given I try to log in with "superuser" "password1"
    Given I have initialized inventory
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    And I select stock card code called "[01A01]"
    And I wait for "Stock Card" to appear
    And I select a reason "Issues" "Maternity"
    And I wait for 1 second
    And I swipe right
    And I enter issued number "123"
    And I wait for "Complete" to appear
    And I press "Complete"
    And I wait for "Enter your initials" to appear
    And I sign with "superuser"
    And I wait for 1 second
    And I press the menu key
    Then I see "Archive drugs"
    And I press "Archive drugs"
    And I wait for "Stock Overview" to appear
    And I navigate back
    And I wait for "Stock Card Overview" to appear
    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"
    And I wait for 3 seconds
    #clean app data
    Given I clean app data

    Scenario: user should sync back product archived status when init app
    Given I try to log in with "superuser" "password1"
    And I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    And I press the menu key
    Then I see "Archived drugs"
    And I press "Archived drugs"
    Then I should see "[01A01]"


