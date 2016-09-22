@regression @archive
Feature: Archive drug

  Scenario: update product as server(update product & deactivated product & add new product)
    Given I see "ESMS"
    Given server deactivates products 12D03 and 07L01
    Given I try to log in with "initial_inventory" "password1"
    And I wait up to 180 seconds for "Initial Inventory" to appear
    Then I shouldn't see product "Alfa tocoferol (Vitamina E); 100mg; Cápsulas" in this page
    And I shouldn't see product "Baclofeno; 10mg; Comprimidos" in this page

    And I search lot product by primary name "Estavudina/Lamivudina; 6mg+30mg, 60 Comp (Baby); Embalagem" and select this item with quantity "123" and lot number "kkk"
    And I wait for "Complete" to appear
    And I press "Complete"
    Then I wait for "STOCK CARD OVERVIEW" to appear
    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    #can not see 99X99 & can see 12D03
    And I press the menu key
    And I wait for "Add new product" to appear
    And I press "Add new product"
    And I wait for "Add new product" to appear

    #primary name of 25D03 is "Manual de procedimentos  do Deposito Distital de Medicamentos Sem Dosagem Papel"
    When I search "25D03"
    Then I see "Manual de procedimentos  do Deposito Distital de Medicamentos Sem Dosagem Papel"

    #can not see 99X99
    When I clean search bar
    Then I shouldn't see product "99X99" in this page

    #can see 12D03Z
    Then I should see product "12D03Z" in this page
    When I navigate back
    And I wait for 1 second
    And I navigate back
    And I wait for "Stock Overview" to appear

    #Archived 08S32Z
    Then I select stock card code called "[08S32Z]"
    Then I wait for "Stock Card" to appear

    Then I press "NEW MOVEMENT"
    Then I select a new movement reason "Negative Adjustments" "Damaged on arrival"
    Then I wait for 1 second
    Then I select movement date
    Then I wait for 1 second
    Then I enter quantity "123" for the last lot
    Then I enter signature "super"
    And I press "Complete"

    And I wait for "Stock Card" to appear
    Then I press the menu key
    Then I should see "Archive drugs"
    And I press "Archive drugs"
    And I wait for "Stock Overview" to appear
    Then I don't see the text "[08S32Z]"

    # Server updates drugs
    Given Server updates drug data
    When I navigate back
    And I wait for "STOCK CARD OVERVIEW" to appear

    And I press the menu key
    Then I see "Sync Data"
    And I press "Sync Data"

    And I press "Stock Card Overview"
    And I wait for "Stock Overview" to appear

    # Archived drugs should stay as archived after server update
    Then I shouldn't see product "08S32Z" in this page

    And I press the menu key
    And I wait for "Add new product" to appear
    And I press "Add new product"
    And I wait for "Add new product" to appear

    #can see 99X99
    And I wait for 5 seconds
    When I search "99X99"
    Then I see "New Drug"

    #primary name of 25D03 is "Updated Drug"
    When I clean search bar
    And I search "25D03"
    Then I see "Updated Drug"

    #can not see 12D03Z
    When I clean search bar
    Then I shouldn't see product "12D03Z" in this page