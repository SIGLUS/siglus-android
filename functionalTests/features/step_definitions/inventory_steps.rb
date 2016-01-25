require 'calabash-android/calabash_steps'
require 'pry'

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
        Then I wait up to 30 seconds for "Initial Inventory" to appear
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
        Then I wait up to 30 seconds for "Initial Inventory" to appear
        Then I wait for "Initial inventory" to appear
        When I Select MMIA Item
        Then I wait for "Complete" to appear
        And I press "Complete"
        Then I wait for "STOCK CARD OVERVIEW" to appear
	}
end

Given(/^I have initialized inventory with VIA user$/) do
  steps %Q{
        Then I wait up to 30 seconds for "Initial Inventory" to appear
        Then I wait for "Initial inventory" to appear
        When I Select VIA Item
        Then I wait for "Complete" to appear
        And I press "Complete"
        Then I wait for "STOCK CARD OVERVIEW" to appear
	}
end

When(/^I Select MMIA Item$/) do
  steps %Q{
    When I search product by fnm "08S42B" and select this item with quantity "123"
    When I search product by fnm "08S18Y" and select this item with quantity "123"
    When I search product by fnm "08S40Z" and select this item with quantity "123"
    When I search product by fnm "08S36" and select this item with quantity "123"
    When I search product by fnm "08S32Z" and select this item with quantity "123"
	}
end

When(/^I Select VIA Item$/) do
  steps %Q{
    When I search product by fnm "01A01" and select this item with quantity "123"
    When I search product by fnm "01A02" and select this item with quantity "123"
    When I search product by fnm "01A03Z" and select this item with quantity "123"
    When I search product by fnm "01A04Z" and select this item with quantity "123"
    When I search product by fnm "01A05" and select this item with quantity "123"
	}
end

When(/^I initialize inventory$/) do
    if EnvConfig::STRESS_TEST
        steps %Q{
            Then I wait up to 30 seconds for "Initial Inventory" to appear
            Then I wait for "Initial inventory" to appear
            And I initialize "1254" products
            And I press "Complete"
            Then I wait for "STOCK CARD OVERVIEW" to appear
        }
    else
       steps %Q{
           Given I have initialized inventory
       }
    end
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
end

Then(/^I check the initial result quantity on stock overview page/) do
    if EnvConfig::STRESS_TEST
        steps %Q{
            Then I can see stock on hand "1254" in position "1"
            Then I should see total:"1254" on stock list page
        }
    else
        steps %Q{
            Then I can see stock on hand "123" in position "1"
            Then I should see total:"10" on stock list page
        }
    end

end

Then(/^I can see stock on hand "(\d+)" in position "(\d+)"$/) do |number, index|
    quantities = query("android.widget.TextView id:'tv_stock_on_hand'", :text)
    unless quantities.at(index.to_i - 1) == number
        fail "Text in quantity field in position "#{index}" is incorrect."
    end
end


Then(/^I do physical inventory for mmia items$/) do
    steps %Q{
        And I do physical inventory with "121" by fnm "08S42B"
        And I do physical inventory with "123" by fnm "08S18Y"
        And I do physical inventory with "123" by fnm "08S40Z"
        And I do physical inventory with "123" by fnm "08S36"
        And I do physical inventory with "123" by fnm "08S32Z"
        Then I scroll "recyclerView" down to "Complete"
        And I wait for 1 second
        And I press "Complete"
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
        And I sign with "sign"
    }
end



