#@bulk
#Feature: Log in and initialize Inventory with Basic Products
#
#  Scenario: User should be able to log in and add lots Bulk Initial Inventory Activity
#
#    Given I try to log in with "core" "password1"
#    And I wait up to 180 seconds for "Initial Inventory" to appear
#    When I press the menu key
#    And I select new drug enter requested "123" by product name "Digoxina 0,25mg Comp"
#    Then I press "Add New Lot"
#    Then I wait for 2 seconds
#    Then I write in "Lot number" field the text "AAA"
#    Then I write in "Amount" field the text "100"
#    Then I press the button with text "Complete"
#    Then I see "AAA"
#    Then I see "100"
#    Then I press "Add New Lot"
#    Then I wait for 2 seconds
#    Then I write in "Lot number" field the text "BBB"
#    Then I write in "Amount" field the text "50"
#    Then I press the button with text "Complete"
#    Then I see "BBB"
#    Then I see "50"
#    Then I see "150"
#    Then I press delete button
#    Then I see "50"
#
#  Scenario: User should be able to check an element as no stock
#
#    Given I try to log in with "core" "password1"
#    And I wait up to 180 seconds for "Initial Inventory" to appear
#    When I press the menu key
#    And  I select new drug enter requested "123" by product name "Digoxina 0,25mg Comp"
#    Then I press "No stock"
#    Then I see "0"
#    Then I do not see field with id "btn_no_stock"
#    Then I press save button
#    Then I wait for 3 seconds
#    Then I see "Successfully Saved"