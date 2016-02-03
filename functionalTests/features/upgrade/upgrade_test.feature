@upgrade
Feature: After version upgrade, data integrity should not be broken

  Scenario: After version upgrade, user should still be able to use all pages with proper data
    When I try to log in with "Michafutene" "password1"
    And I wait up to 120 seconds for "Stock Card Overview" to appear
    # to run this in a physical device, we need to wait longer, IO is slow on physical devices

    #after upgrade user should still be able to see stock cards and stock movements history
    Then I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    Then I select drug number 1
    And I wait for "Stock Card" to appear
    Then I should see more than 0 movements in stock card page
    Then I press "Movement History"
    And I wait for "Date of Movement" to appear
    Then I should see more than 0 movements in movement history page