@PHYSICAL_INVENTORY
Feature: Physical Inventory

  Scenario: Do physical inventory, and check the stock on hand quantity.
    Given I try to log in with "physical_inventory" "password1"
    Then I wait up to 30 seconds for "Initial Inventory" to appear
    Then I wait for 3 seconds

    When I search product by fnm "08S42B" and select this item
    When I search product by fnm "08S18Y" and select this item
    When I search product by fnm "08S40Z" and select this item

    Then I wait for "Complete" to appear
    And I press "Complete"
    Then I wait for "Home Page" to appear

    And I press "Do Monthly Inventory"
    And I wait for "inventory" to appear

    And I do physical inventory with "100" by fnm "08S42B"
    And I do physical inventory with "200" by fnm "08S18Y"
    And I do physical inventory with "300" by fnm "08S40Z"
    And I go back
    And I press "Complete"
    And I sign with "sign"

    Then I wait for "Home Page" to appear
    When I press "Stock Card Overview"
    Then I wait for "Stock Overview" to appear
    Then I should see text containing "100"
    Then I should see text containing "200"
    Then I should see text containing "300"

  Scenario: Save physical inventory, and check the stock on hand quantity that have been saved.
    Given I try to log in with "physical_inventory" "password1"
    Then I wait for "Home Page" to appear

    And I press "Do Monthly Inventory"
    And I wait for "inventory" to appear
    And I do physical inventory with "2015" by fnm "08S42B"
    And I go back
    And I press "Save"

    And I wait for "Home Page" to appear
    And I press "Do Monthly Inventory"
    And I wait for "inventory" to appear
    Then I should see text containing "2015"


    


