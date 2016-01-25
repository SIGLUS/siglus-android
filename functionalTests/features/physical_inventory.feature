@PHYSICAL_INVENTORY @dev
Feature: Physical Inventory

  #Should check scroll up and down very quickly
  #Should check click next button very quickly
  #Should check load more than 300 products in this page
  #Should check physical Inventory thant can't rotate or the data will lost
  Scenario: Do physical inventory, and check the stock on hand quantity.
    Given I try to log in with "physical_inventory" "password1"
    Then I wait up to 30 seconds for "Initial Inventory" to appear
    Then I wait for 3 seconds

    When I search product by fnm "08S42B" and select this item with quantity "10"
    When I search product by fnm "08S18Y" and select this item with quantity "20"
    When I search product by fnm "08S40Z" and select this item with quantity "30"

    Then I wait for "Complete" to appear
    And I press "Complete"
    Then I wait for "STOCK CARD OVERVIEW" to appear

    And I press "Do Monthly Inventory"
    And I wait for "inventory" to appear
    And I press "Complete"
    Then I should see text containing "Quantity cannot be left blank!"

    And I do physical inventory with "100" by fnm "08S42B"
    And I do physical inventory with "200" by fnm "08S18Y"
    And I do physical inventory with "300" by fnm "08S40Z"

    Then I can see "300" physical quantity in position "1"
    Then I can see "200" physical quantity in position "2"
    Then I can see "100" physical quantity in position "3"

    And I press "Complete"
    And I sign with "sign"

    Then I wait for "STOCK CARD OVERVIEW" to appear
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear

    Then I should see SOH of "08S42B" is "100"
    Then I should see SOH of "08S18Y" is "200"
    Then I should see SOH of "08S40Z" is "300"

  Scenario: Save physical inventory, and check the stock on hand quantity that have been saved.
    Given I try to log in with "physical_inventory" "password1"
    Then I wait for "STOCK CARD OVERVIEW" to appear

    And I press "Do Monthly Inventory"
    And I wait for "inventory" to appear
    And I do physical inventory with "2015" by fnm "08S42B"
    And I press "Save"

    And I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Do Monthly Inventory"
    And I wait for "inventory" to appear
    Then I should see text containing "2015"







