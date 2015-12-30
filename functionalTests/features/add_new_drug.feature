@ADD_NEW_DRUG
Feature:add new drug

  Scenario: If is STRESS TEST,add all drugs with quantity 300, else try adding drug without SOH, then try adding drug with SOH
    Given I try to log in with "initial_inventory" "password1"
    Then I wait for "Initial Inventory" to appear
    Given I have initialized inventory
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

    When I unselect the item called "08S01ZY"
    Given I have added new drugs
    Then I press "Complete"
    Then I wait for "Stock Overview" to appear
    Then I check new drug quantity