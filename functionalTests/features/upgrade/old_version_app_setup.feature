@upgrade_setup @change_date
Feature: Old version set up before upgrade

  Scenario: Install an older version of the app on tablet and have data available from the old version
    When I try to log in with "marracuene" "password1"
    And I wait up to 180 seconds for "Stock Card Overview" to appear
    # to run this in a physical device, we need to wait longer, IO is slow on physical devices

    # Given I press "Create a Via Classica Requisition"
    # And I wait for "Requisition -" to appear
    # And I enter consultationsNub "888"
    # And I press "Save"
    # Then I wait for "Home Page" to appear

    # Given I press "Create a MMIA"
    # And I wait for "MMIA -" to appear
    # Then I scroll "scrollView" down to "Submit for Approval"
    # And I enter patient totals
    # And I press "Save"
    # Then I wait for "Home Page" to appear




