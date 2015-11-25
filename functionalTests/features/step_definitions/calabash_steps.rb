require 'calabash-android/calabash_steps'
require 'pry'
require File.dirname(__FILE__) + '/env_config'

username = EnvConfig.getConfig()[:username]
password = EnvConfig.getConfig()[:password]

index = 0
pre_name = ""

When /^I enter username "([^\"]+)"$/ do |username|
  element = "android.widget.EditText id:'tx_username'"
  clear_text_in(element)
  enter_text(element, username)
  hide_soft_keyboard
end

When /^I enter password "([^\"]+)"$/ do |password|
  enter_text("android.widget.EditText id:'tx_password'", password)
  hide_soft_keyboard
end

Given(/^I am logged in$/) do
  steps %Q{
		When I enter username "#{username}"
        And I enter password "#{password}"
        And I press "LOG IN"
	}
end

When(/^I select the item called "(.*?)"$/) do |name|
  p "When I select the item called #{name}"
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

  h = query("android.widget.EditText id:'tx_quantity' text:''").last
  touch(h)
  keyboard_enter_text(123)
  hide_soft_keyboard
end

When(/^I select the checkbox$/) do
    wait_for_element_exists("android.widget.CheckBox id:'checkbox' checked:'false'", :timeout => 10)
    checkbox = query("android.widget.CheckBox id:'checkbox' checked:'false'").first
    until checkbox.nil?
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
        Then I wait up to 30 seconds for the "InventoryActivity" screen to appear
        Then I wait for 3 seconds
        When I Select VIA Item
        When I Select MMIA Item
        And I press "Complete"
        Then I wait for the "HomeActivity" screen to appear
	}
end

When(/^I Select MMIA Item$/) do
  steps %Q{
    When I search product by fnm "08S42" and select this item
    When I search product by fnm "08S18Y" and select this item
    When I search product by fnm "08S40" and select this item
    When I search product by fnm "08S36" and select this item
    When I search product by fnm "08S32" and select this item
	}
end

When(/^I Select VIA Item$/) do
  steps %Q{
    When I search product by fnm "01A01" and select this item
    When I search product by fnm "01A02" and select this item
    When I search product by fnm "01A03Z" and select this item
    When I search product by fnm "01A04" and select this item
    When I search product by fnm "01A05" and select this item
	}
end

When(/^I Select initial inventory in Screen$/) do
    product_name = query("android.widget.TextView id:'product_name' ", "getText")
    for name in product_name
            if pre_name!=name
                steps %Q{
                    When I select the item called "#{name}"
    	        }
    	    end
            pre_name=name
    end
end

When(/^I initialize inventory$/) do
    if EnvConfig::STRESS_TEST
        checkBox = query("android.widget.CheckBox id:'checkbox' checked:'false'").first

        while !checkBox.nil?
            steps %Q{
                When I select the checkbox
            }
            scroll("RecyclerView", :down)
            checkBox = query("android.widget.CheckBox id:'checkbox' checked:'false'").first
        end

        steps %Q{
            And I press "Complete"
            Then I wait for the "HomeActivity" screen to appear
        }
    else
       steps %Q{
           Given I have initialized inventory
       }
    end
end

When(/^I search product by fnm "(.*?)" and select this item$/) do |fnm|
    search_bar = query("android.support.v7.widget.SearchView id:'action_search'")
    touch(search_bar)
    enter_text("android.support.v7.widget.SearchView id:'action_search'", fnm)

    steps %Q{
        When I select the item called "#{fnm}"
    }
    clear_text_in(search_bar)
end