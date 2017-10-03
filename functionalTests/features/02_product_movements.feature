@movements @dev
Feature: Log in and make a requisition for mmia products

  Scenario: 02 - Product movements
    Given I log in into the application
    And I press "Stock Card Overview"
    And I search for "Digoxina 0,25mg Comp"
    And I add a new movement
    And I select issue movement for product found
    And I add details in destination "PAV" amount 10 and signed by "TWUIO"
    And I add a new movement
    And I select entry movement for product found
    And I add details in origin "District( DDM)" amount 20 and signed by "TWUIO"
    And I navigate back
    And I search for "Dobutamina; 250mg/5mL; Injectável"
    And I add a new movement
    And I select positive adjustment for product found
    And I add details in origin "Loans received at the health facility deposit" amount 30 and signed by "TWUIO"
    And I add a new movement
    And I select negative adjustment for product found
    And I add details in origin "Return to DDM" amount 40 and signed by "TWUIO"
    And I navigate back
    And I navigate back
    When I navigate back
    Then I should see the application main menu screen