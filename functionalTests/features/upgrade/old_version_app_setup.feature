@upgrade_setup @change_date
Feature: Old version set up before upgrade

  Scenario: Install an older version of the app on tablet and have data available from the old version
    When I try to log in with "marracuene" "password1"
    And I wait up to 300 seconds for "Stock Card Overview" to appear

    And I press "Stock Card Overview"
    And I wait for 1 second
#    And I make a positive movement with lot "Positive Adjustments" "Returns from Customers(HF and dependent wards)" "1"
#    And I search stockcard by code "01A01" and select this item
    When I search "01A01" without clearing text
    Then I select stock card code called "[01A01]"
    Then I wait for "Stock Card" to appear
    Then I wait for 1 second
    Then I press "NEW MOVEMENT"
    Then I select a new movement reason "Entries" "District( DDM)"
    Then I wait for 1 second
    Then I select movement date
    Then I wait for 1 second
    Then I enter signature "super"
    Then I add a new lot with lot number "AAA" and quantity "1"
    And I press "Complete"
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




