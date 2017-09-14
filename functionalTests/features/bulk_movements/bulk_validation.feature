#@bulk_validations
#Feature: Log in and initialize Inventory with Basic Products
#
#  Scenario: User should be able to log in and add lots Bulk Initial Inventory Activity and validate amount field
#
#    # Initialize inventory and check stock card overview
#    Given I try to log in with "core" "password1"
#    And I wait up to 180 seconds for "Initial Inventory" to appear
#    When I press the menu key
#    And I select new drug enter requested "123" by product name "Digoxina 0,25mg Comp"
#    Then I press "ADD LOT"
#    Then I wait for 1 seconds
#    Then I write in "Amount" field the text "100"
#    Then I press the button with text "Complete"
#    Then I wait for 5 seconds
#    Then I press the button with text "AUTO GENERATE LOT NUMBER"
#
#  Scenario: User should be able to log in and add lots Bulk Initial Inventory Activity
#
## Initialize inventory and check stock card overview
#    Given I try to log in with "core" "password1"
#    And I wait up to 180 seconds for "Initial Inventory" to appear
#    When I press the menu key
#    And I select new drug enter requested "123" by product name "Digoxina 0,25mg Comp"
#    Then I press "ADD LOT"
#    Then I wait for 2 seconds
#    Then I write in "Lot number" field the text "AAA"
#    Then I press the button with text "Complete"
#    Then I wait for 5 seconds
#    Then I see "The amount field cannot be empty"
#    Then I wait for 3 seconds
