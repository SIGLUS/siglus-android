@bulk @dev @reinstall
Feature: Log in and initialize Inventory with Basic Products

  Scenario: 01 - Submit initial inventory
    Given today is "20160216.130000"
    And I log in for the first time
    And I see the initial inventory screen
    And I search for "Hidralazina; 25mg/5mL; Injectável" to declare no stock
    And I save initial inventory
    And I see "Successfully saved"
    And I navigate back
    And I login back into the application
    And the initial inventory list should not contains product "Hidralazina; 25mg/5mL; Injectável"
    And I search for "Dobutamina; 250mg/5mL; Injectável" to add a new lot
    And I add a new lot without lot number and amount 123 and expiration date next year
    And I select to autogenerate lot number
    And I search for "Digoxina 0,25mg Comp" to add a new lot
    And I add a new lot with number "AAA", amount 110 and expiration date next year
    And I search for "Digoxina 0,25mg Comp" to add a new lot
    And I add a new lot with number "BBB", amount 50 and expiration date next year
    And I see total stock on hand for visible product to be equals to 160
    And I delete lot added in position 2
    And I see total stock on hand for visible product to be equals to 110
    And I search for "Abacavir sulfato (ABC) 300mg,60comp Embalagem" to add a new lot
    And I add a new lot with number "CCC", amount 120 and expiration date next year
    And I search for "Estavudina+Lamivudina+Nevirapi; 6mg + 30mg +50mg, 60 Cps (Baby; Embalagem" to add a new lot
    And I add a new lot with number "DDD", amount 130 and expiration date next year
    And I add non basic products
    And I select "Zidovudina/Lamivudina; 300mg+150mg 60Comp; Embalagem" to be added
    And I select "Nevirapina (NVP); 200mg 60Comp; Embalagem" to be added
    And I select "Nevirapina (NVP); 50mg/5mL 240mL; Suspensão" to be added
    And I select "Zidovudina (AZT); 300mg 60Comp; Embalagem" to be added
    And I select "Zidovudina (AZT); 50mg/5mL 240mL; Solução" to be added
    And I select "Trastuzumab; 150mg" to be added
    And I add selected products
    And I submit the initial inventory
    And I should see text containing "All the basic products have to be set up in order to continue"
    And I search for "Zidovudina/Lamivudina; 300mg+150mg 60Comp; Embalagem" to add a new lot
    And I add a new lot with number "EEE", amount 350 and expiration date next year
    And I search for "Nevirapina (NVP); 200mg 60Comp; Embalagem" to add a new lot
    And I add a new lot with number "FFF", amount 400 and expiration date next year
    And I search for "Nevirapina (NVP); 50mg/5mL 240mL; Suspensão" to add a new lot
    And I add a new lot with number "GGG", amount 500 and expiration date next year
    And I search for "Zidovudina (AZT); 300mg 60Comp; Embalagem" to add a new lot
    And I add a new lot with number "HHH", amount 500 and expiration date next year
    And I search for "Zidovudina (AZT); 50mg/5mL 240mL; Solução" to add a new lot
    And I add a new lot with number "III", amount 500 and expiration date next year
    And I search for "Trastuzumab; 150mg" to add a new lot
    And I add a new lot with number "JJJ", amount 300 and expiration date next year
    And I search for "Trastuzumab; 150mg" to delete it
    And the initial inventory list should not contains product "Trastuzumab; 150mg"
    When I submit the initial inventory
    Then I should see the application main menu screen