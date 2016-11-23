@weekly
Feature: Log in from local

  Scenario: Log in from local when network disable

    Given I enable wifi
    Given I try to log in with "superuser" "password1"
    And I wait up to 180 seconds for "Initial Inventory" to appear
    Then I wait for "Initial inventory" to appear
    When I search lot product by fnm "08L01X" and select this item with quantity "888" and lot number "ddkk"
    Then I press "Complete"

    Then I wait for "STOCK CARD OVERVIEW" to appear
    And I press the menu key
    And I wait for "Sign Out" to appear
    # note: sometimes the wait for sign out to appear fails, reason unknown
    And I press "Sign Out"
    And I disable wifi
    Then I wait for the "LoginActivity" screen to appear
    And I wait for 1 second
    Then I try to log in with "superuser" "password1"
    And I wait up to 180 seconds for "Stock Card Overview" to appear
    And I enable wifi

    #TODO to be moved to weekly test