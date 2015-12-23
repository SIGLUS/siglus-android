@ADD_NEW_DRUG
Feature:add new drug

  Scenario: add one new drug with empty SOH
    Given I try to log in with "initial_inventory" "password1"
    Then I wait for "Initial Inventory" to appear
    Then I wait for 1 second
    Given I initialize inventory
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    Given I press the menu key
    And I wait for "Add new product" to appear
    Then I press "Add new product"
    And I wait for "Add new product" to appear
    When I search drug by fnm "08S01ZY"
    When I select the item called "08S01ZY"
    And I press "Complete"
    Then I should see text containing "Quantity cannot be left blank!"

  Scenario: add one new drug,and check stock on hand quantity
    Given I try to log in with "initial_inventory" "password1"
    And I wait for "Home Page" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    Given I press the menu key
    And I wait for "Add new product" to appear
    Then I press "Add new product"
    And I wait for "Add new product" to appear
    When I search product by fnm "08S01ZY" and select this item with quantity "2008"
    And I press "Complete"
    And I wait for "Stock Overview" to appear
    When I search drug by fnm "08S01ZY"
    Then I should see text containing "2008"


