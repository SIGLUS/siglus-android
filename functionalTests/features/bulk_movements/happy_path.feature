@bulk @happy_path
Feature: Log in and initialize Inventory with Basic Products

  @avoid
  Scenario: User should be able to log in, initialize inventory and navigate to stock overview page

    # Initialize inventory and check stock card overview
    Given I try to log in with "core" "password1"
    And I wait up to 180 seconds for "Initial Inventory" to appear
    # to run this in a physical device, we need to wait longer, IO is slow on physical devices

    #When I press the menu key
    #And I select new drug enter requested "123" by product name "Digoxina 0,25mg Comp"
    #Then I press "Add New Lot"
    #Then I wait for 3 seconds
    #Then I enter "AAA" into input field number 1
    #Then I enter "100" into input field number 2
    #And I press "Complete"
