@regression @change_date @happy_path
Feature: Log in and initialize Inventory with Basic Products

  @avoid
  Scenario: User should be able to log in, initialize inventory and navigate to stock overview page

    Given I change device date to "20160216.130000"
    #Unauthenrised account shouldn't login to the app
    Given I try to log in with "testlogin" "password1"
    And I should see "Username or Password is incorrect."
    Then I wait for 1 second

    # Initialize inventory and check stock card overview
    Given I try to log in with "core" "password1"
    And I wait up to 180 seconds for "Initial Inventory" to appear
    # to run this in a physical device, we need to wait longer, IO is slow on physical devices

    #When I press the menu key
    #And I select new drug enter requested "123" by product name "Digoxina 0,25mg Comp"
    #Then I press "Add New Lot"
    #And add a new lot with lot number "AAA" and quantity "1"
    #And I press "Complete"
