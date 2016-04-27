require 'calabash-android/calabash_steps'
require 'pry'

index = 0

When(/^I select the item called "(.*?)"$/) do |name|
  q = query("android.widget.TextView {text CONTAINS '#{name}'}")
  while q.empty?
    scroll("RecyclerView", :down)
    q = query("android.widget.TextView {text CONTAINS '#{name}'}")
   end

  touch(q)

  h = query("android.widget.EditText id:'tx_quantity' text:''")
  while h.empty?
       scroll("RecyclerView", :down)
       h = query("android.widget.EditText id:'tx_quantity' text:''")
  end

  if h.size > 1
     scroll("RecyclerView", :down)
  end
end

When(/^I select the inventory item/) do
  q = query("android.widget.CheckBox id:'checkbox' checked:'false'")

  if !q.empty?
    touch(q)
  end
end

When(/^I unselect the inventory item/) do
 q = query("android.widget.CheckBox id:'checkbox' checked:'true'")

 if !q.empty?
   touch(q)
 end
end



And(/^I enter quantity "(\d+)" on inventory page$/) do |quantity|
    h = query("android.widget.EditText id:'tx_quantity' text:''").first
    touch(h)
    keyboard_enter_text(quantity)
    hide_soft_keyboard
end

When(/^I select the checkbox with quantity "(\d+)"$/) do |quantity|
    wait_for_element_exists("android.widget.CheckBox id:'checkbox' checked:'false'", :timeout => 10)
    checkbox = query("android.widget.CheckBox id:'checkbox' checked:'false'").first
    until checkbox.nil?

      if index == quantity.to_i then
        break
      end

      touch(checkbox)
      h = query("android.widget.EditText id:'tx_quantity' text:''")
      while h.empty?
        scroll("RecyclerView", :down)
        h = query("android.widget.EditText id:'tx_quantity' text:''")
      end
      tx_quantity = h.last
      touch(tx_quantity)
      keyboard_enter_text(index + 1)
      index = index + 1
      hide_soft_keyboard
      checkbox = query("android.widget.CheckBox id:'checkbox' checked:'false'").first
    end
end

Given(/^I have initialized inventory$/) do
  steps %Q{
        Then I wait up to 60 seconds for "Initial Inventory" to appear
        Then I wait for "Initial inventory" to appear
        When I Select VIA Item
        When I Select MMIA Item
        Then I wait for "Complete" to appear
        And I press "Complete"
        Then I wait for "STOCK CARD OVERVIEW" to appear
	}
end

Given(/^I have initialized inventory with MMIA user$/) do
  steps %Q{
        Then I wait up to 60 seconds for "Initial Inventory" to appear
        Then I wait for "Initial inventory" to appear
        When I Select MMIA Item
        Then I wait for "Complete" to appear
        And I press "Complete"
        Then I wait for "STOCK CARD OVERVIEW" to appear
	}
end

Given(/^I have initialized inventory with VIA user$/) do
  steps %Q{
        Then I wait up to 60 seconds for "Initial Inventory" to appear
        Then I wait for "Initial inventory" to appear
        When I Select VIA Item
        Then I wait for "Complete" to appear
        And I press "Complete"
        Then I wait for "STOCK CARD OVERVIEW" to appear
	}
end

When(/^I Select MMIA Item$/) do
  steps %Q{
    When I search product by primary name "Zidovudina/Lamivudina/Nevirapi; 60mg+30mg+50mg 60 Comprimidos; Embalagem" and select this item with quantity "123"
    When I search product by primary name "Tenofovir/Lamivudina/Efavirenz; 300mg + 300mg + 600mg 30Comp; Embalagem" and select this item with quantity "123"
    When I search product by primary name "Lamivudina+Zidovudina; 30mg+60mg, 60 Comprimidos; Embalagem" and select this item with quantity "123"
    When I search product by primary name "Estavudina/Lamivudina/Nevirapi; 200mg+150mg+30mg 60Comp; Embalagem" and select this item with quantity "123"
    When I search product by primary name "Estavudina/Lamivudina; 6mg+30mg, 60 Comp (Baby); Embalagem" and select this item with quantity "123"
  }
end

When(/^I Select VIA Item$/) do
  steps %Q{
    When I search product by primary name "Digoxina 0,25mg Comp" and select this item with quantity "123"
    When I search product by primary name "Digoxina; 2,5mg/50ml; Gotas Orais" and select this item with quantity "123"
    When I search product by primary name "Digoxina; 0,25mg/2mL; Inject" and select this item with quantity "123"
    When I search product by primary name "Dobutamina; 250mg/20mL; Inject" and select this item with quantity "123"
    When I search product by primary name "Dopamina HCL; 200mg/5mL; Inject" and select this item with quantity "123"
  }
end

And(/^I do physical inventory with "(\d+)" by fnm "(.*?)"$/) do |quantity,fnm|
    steps %Q{
        When I search drug by fnm "#{fnm}"
        And I enter quantity "#{quantity}" on inventory page
        And I clean search bar
        And I go back
    }
end

Then(/^I can see "(\d+)" physical quantity in position "(\d+)"$/) do |number, index|
    quantities = query("android.widget.EditText id:'tx_quantity'", :text)
    unless quantities.at(index.to_i - 1) == number
        fail "Text in quantity field in position "#{index}" is incorrect."
    end
end

And(/^I initialize "(\d+)" products/) do |quantity|
    checkBox = query("android.widget.CheckBox id:'checkbox' checked:'false'").first

    while !checkBox.nil?
        if index == quantity.to_i then
            break
        end

        steps %Q{
            When I select the checkbox with quantity "#{quantity}"
        }
        scroll("RecyclerView", :down)
        checkBox = query("android.widget.CheckBox id:'checkbox' checked:'false'").first
    end

    index = 0
end

Then(/^I can see stock on hand "(\d+)" in position "(\d+)"$/) do |number, index|
    quantities = query("android.widget.TextView id:'tv_stock_on_hand'", :text)
    unless quantities.at(index.to_i - 1) == number
        fail "Text in quantity field in position "#{index}" is incorrect."
    end
end


Then(/^I do physical inventory for mmia items$/) do
    steps %Q{
        And I do physical inventory with "123" by fnm "08S42B"
        And I do physical inventory with "121" by fnm "08S18Y"
        And I do physical inventory with "123" by fnm "08S40Z"
        And I do physical inventory with "123" by fnm "08S36"
        And I do physical inventory with "123" by fnm "08S32Z"
        Then I scroll "recyclerView" down to "Complete"
        And I wait for 1 second
        And I press "Complete"
        And I wait for "Enter your initials" to appear
        And I sign with "sign"
    }
end

Then(/^I do physical inventory for via items$/) do
    steps %Q{
        And I do physical inventory with "113" by fnm "01A01"
        And I do physical inventory with "123" by fnm "01A02"
        And I do physical inventory with "123" by fnm "01A03Z"
        And I do physical inventory with "123" by fnm "01A04Z"
        And I do physical inventory with "123" by fnm "01A05"
        Then I scroll "recyclerView" down to "Complete"
        And I wait for 1 second
        And I press "Complete"
        And I wait for "Enter your initials" to appear
        And I sign with "sign"
    }
end



Then(/^I do physical inventory for all items$/) do
  while !query("android.widget.EditText text:''").empty?
    query("android.widget.EditText id:'tx_quantity'", {:setText => '1'})

    if query("* marked:'Complete'").empty?
      scroll('recyclerView', :down)
    end
  end

  tap_when_element_exists("* marked:'Complete'")

  steps %Q{
        And I wait for "Enter your initials" to appear
        And I sign with "sign"
    }
  hide_soft_keyboard
end



