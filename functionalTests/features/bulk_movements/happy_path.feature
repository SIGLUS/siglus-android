#@bulk @happy_path
#Feature: Log in and initialize Inventory with Basic Products
#
#  Scenario: User should be able to log in and add lots Bulk Initial Inventory Activity
#
#    # Initialize inventory and check stock card overview
#    Given I try to log in with "core" "password1"
#    And I wait up to 180 seconds for "Initial Inventory" to appear
#    # to run this in a physical device, we need to wait longer, IO is slow on physical devices
#
#    When I press the menu key
#    And I select new drug enter requested "123" by product name "Digoxina 0,25mg Comp"
#    Then I press "Add New Lot"
#    Then I wait for 5 seconds
#    Then I enter "AAA" into input field number 1
#    Then I enter "100" into input field number 2
#    Then I press "Complete"
#    Then I see "AAA"
#    Then I see "100"
#    Then I press "Add New Lot"
#    Then I wait for 5 seconds
#    Then I enter "BBB" into input field number 1
#    Then I enter "50" into input field number 2
#    Then I press "Complete"
#    Then I see "BBB"
#    Then I see "50"
#    Then text should be "150" in edit text "tv_sho_amount"
#    Then I press image button with id "btn_delete_lot"
#    Then I wait for 5 seconds
#    Then I press the menu key
#    Then I select new drug enter requested "123" by product name "Digoxina 0,25mg Comp"
#    Then text should be "50" in edit text "tv_sho_amount"
#
#  Scenario: User should be able to check an element as no stock
#
#  # Initialize inventory and check stock card overview
#    Given I try to log in with "core" "password1"
#    And I wait up to 180 seconds for "Initial Inventory" to appear
#  # to run this in a physical device, we need to wait longer, IO is slow on physical devices
#
#    When I press the menu key
#    And  I select new drug enter requested "123" by product name "Digoxina 0,25mg Comp"
#    Then I press "No stock"
#    Then text should be "0" in edit text "tv_sho_amount"
#    Then I do not see field with id "btn_no_stock"