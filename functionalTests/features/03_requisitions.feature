@requisitions @dev
Feature: Log in and make a requisition for basic products

  Scenario: 03 - Requisitions
    Given today is "20160218.130000"
    And I log in into the application
    And I press "Requisitions"
    And I should see text containing "No Requisition Balancete has been created."
    And I press "Complete Inventory"
    And I see the inventory screen
    And I search for "Digoxina 0,25mg Comp" in physical inventory
    And I set the amount to 120
    And I navigate back
    And I search for "Dobutamina; 250mg/5mL; Injectável" in physical inventory
    And I set the amount to 113
    And I navigate back
    And I search for "Abacavir sulfato (ABC) 300mg,60comp Embalagem" in physical inventory
    And I set the amount to 120
    And I navigate back
    And I search for "Estavudina+Lamivudina+Nevirapi; 6mg + 30mg +50mg, 60 Cps (Baby; Embalagem" in physical inventory
    And I set the amount to 130
    And I navigate back
    And I search for "Zidovudina/Lamivudina; 300mg+150mg 60Comp; Embalagem" in physical inventory
    And I set the amount to 200
    And I navigate back
    And I search for "Nevirapina (NVP); 200mg 60Comp; Embalagem" in physical inventory
    And I set the amount to 230
    And I navigate back
    And I search for "Nevirapina (NVP); 50mg/5mL 240mL; Suspensão" in physical inventory
    And I set the amount to 300
    And I navigate back
    And I search for "Zidovudina (AZT); 300mg 60Comp; Embalagem" in physical inventory
    And I set the amount to 500
    And I navigate back
    And I search for "Zidovudina (AZT); 50mg/5mL 240mL; Solução" in physical inventory
    And I set the amount to 500
    And I navigate back
    And I search for "Hidralazina; 25mg/5mL; Injectável" in physical inventory
    And I press "Complete"
    And I approve the physical inventory and sign the report with "TWUIO"
    And I should see text containing "No Requisition Balancete has been created."
    And I press "Create Requisition Balancete"
    And I select current requisition period
    And I press next to continue with the requisition form
    And I enter 20 external consultations performed
    And I swipe left to see the theoretical stock
    And I check the quantity for "Digoxina 0,25mg Comp" is equals to 120 in requisition form
    And I check the quantity for "Dobutamina; 250mg/5mL; Injectável" is equals to 123 in requisition form
    And I check the quantity for "Hidralazina; 25mg/5mL; Injectável" is equals to 0 in requisition form
    And I press menu key to add a new product
    And I search for "Digoxina; 0,5mg/2mL; Inject" in requisition add products
    And I submit product found with amount 150 to requisition report
    And I swipe "right" 2 times in requisition form
    And I add 100 units as quantity requested for each product except the new one added
    And I save requisition form
    And I should see text containing "Requisition Balancete is incomplete."
    And I press "Continue Working on Requisition Balancete"
    And I press submit for approval
    And I sign using "TWUIO" as initials
    And I press continue to finish approval
    And I press "Complete"
    When I sign using "TWUIO" as initials
    Then I wait up to 30 seconds to see "successfully submitted"