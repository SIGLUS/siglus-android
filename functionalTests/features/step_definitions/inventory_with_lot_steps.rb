require 'calabash-android/calabash_steps'
require 'pry'

index = 0


Given(/^I have initialized inventory with lot$/) do
  steps %Q{
        Then I wait up to 60 seconds for "Initial Inventory" to appear
        Then I wait for "Initial inventory" to appear
        When I Select VIA Item with lot
        When I Select MMIA Item with lot
        Then I wait for "Complete" to appear
        And I press "Complete"
        Then I wait for "STOCK CARD OVERVIEW" to appear
	}
end

Given(/^I have initialized inventory with MMIA user with lot$/) do
  steps %Q{
        Then I wait up to 60 seconds for "Initial Inventory" to appear
        Then I wait for "Initial inventory" to appear
        When I Select MMIA Item with lot
        Then I wait for "Complete" to appear
        And I press "Complete"
        Then I wait for "STOCK CARD OVERVIEW" to appear
	}
end

Given(/^I have initialized inventory with VIA user with lot$/) do
  steps %Q{
        Then I wait up to 60 seconds for "Initial Inventory" to appear
        Then I wait for "Initial inventory" to appear
        When I Select VIA Item with lot
        Then I wait for "Complete" to appear
        And I press "Complete"
        Then I wait for "STOCK CARD OVERVIEW" to appear
	}
end

When(/^I Select MMIA Item with lot$/) do
  steps %Q{
    When I search lot product by primary name "Zidovudina/Lamivudina/Nevirapi; 60mg+30mg+50mg 60 Comprimidos; Embalagem" and select this item with quantity "123" and lot number "AAA"
    When I search lot product by primary name "Tenofovir/Lamivudina/Efavirenz; 300mg + 300mg + 600mg 30Comp; Embalagem" and select this item with quantity "123" and lot number "BBB"
    When I search lot product by primary name "Lamivudina+Zidovudina; 30mg+60mg, 60 Comprimidos; Embalagem" and select this item with quantity "123" and lot number "CCC"
    When I search lot product by primary name "Estavudina/Lamivudina/Nevirapi; 200mg+150mg+30mg 60Comp; Embalagem" and select this item with quantity "123" and lot number "DDD"
    When I search lot product by primary name "Estavudina/Lamivudina; 6mg+30mg, 60 Comp (Baby); Embalagem" and select this item with quantity "123" and lot number "EEE"
  }
end

When(/^I Select VIA Item with lot$/) do
  steps %Q{
    When I search lot product by primary name "Digoxina 0,25mg Comp" and select this item with quantity "123" and lot number "FFF"
    When I search lot product by primary name "Digoxina; 2,5mg/50ml; Gotas Orais" and select this item with quantity "123" and lot number "GGG"
    When I search lot product by primary name "Digoxina; 0,25mg/2mL; Inject" and select this item with quantity "123" and lot number "HHH"
    When I search lot product by primary name "Dobutamina; 250mg/20mL; Inject" and select this item with quantity "123" and lot number "III"
    When I search lot product by primary name "Dopamina HCL; 200mg/5mL; Inject" and select this item with quantity "123" and lot number "JJJ"
  }
end


