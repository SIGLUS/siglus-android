@STOCK_CARD @dev
Feature: Stock Card Overview page

  Scenario: User should be able to add products to inventory and see product updates from servers

    Given I try to log in with "initial_inventory" "password1"
    Then I wait for "Initial Inventory" to appear
    Then I press "Complete"
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    And I press the menu key
    And I wait for "Add new product" to appear
    And I press "Add new product"
    And I wait for "Add new product" to appear
    Then I shouldn't see product "99X99" in this page

    When I search drug by fnm "25D03"
    Then I see "Manual de"
    When I clean search bar
    And I search drug by fnm "12D03"
    Then I should see product "12D03" in this page

    Given Server updates drug data
    And server deactivates products 12D03 and 07L01

    When I navigate back
    And I navigate back
    And I wait for "Stock Overview" to appear
    And I navigate back
    And I wait for "STOCK CARD OVERVIEW" to appear

    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"

    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear
    And I press the menu key
    And I wait for "Add new product" to appear
    And I press "Add new product"
    And I wait for "Add new product" to appear

    When I search drug by fnm "99X99"
    Then I see "New Drug"
    When I clean search bar
    And I search drug by fnm "25D03"
    Then I see "Updated Drug"
    When I clean search bar
    And I search drug by fnm "12D03"
    Then I shouldn't see product "12D03" in this page

  Scenario: If is STRESS TEST,add all drugs with quantity 300, else try adding drug without SOH, then try adding drug with SOH

    Given I try to log in with "initial_inventory" "password1"
    And I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    And I press the menu key
    And I wait for "Add new product" to appear
    Then I press "Add new product"
    And I wait for "Add new product" to appear
    When I search drug by fnm "08S01ZY"
    When I select the item called "08S01ZY"
    And I press "Complete"
    Then I should see text containing "Quantity cannot be left blank!"

    When I unselect the item called "08S01ZY"
    And I have added new drugs
    Then I press "Complete"
    Then I wait for "Stock Overview" to appear
    And I check new drug quantity