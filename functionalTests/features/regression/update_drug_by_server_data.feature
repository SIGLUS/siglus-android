@regression
Feature: Archive drug

  Scenario: update product as server(update product & deactivated product & add new product)
    Given I try to log in with "initial_inventory" "password1"
    And I wait up to 120 seconds for "Initial Inventory" to appear
    And I search product by primary name "Estavudina/Lamivudina; 6mg+30mg, 60 Comp (Baby); Embalagem" and select this item with quantity "123"
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
    When I search drug by fnm "25D03"
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
    And I select a reason "Negative Adjustments" "Damaged on arrival"
    Then I wait for 1 second
    Then I swipe right
    And I enter negative adjustment number "123"
    Then I wait for "Complete" to appear
    And I press "Complete"
    And I wait for "Enter your initials" to appear
    And I sign with "superuser"
    And I press the menu key
    Then I see "Archive drugs"
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
    When I search drug by fnm "99X99"
    Then I see "New Drug"

    #primary name of 25D03 is "Updated Drug"
    When I clean search bar
    And I search drug by fnm "25D03"
    Then I see "Updated Drug"

    #can not see 12D03Z
    When I clean search bar
    Then I shouldn't see product "12D03Z" in this page