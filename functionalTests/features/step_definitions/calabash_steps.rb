require 'calabash-android/calabash_steps'
require 'pry'
require 'date'
require File.dirname(__FILE__) + '/env_config'

username = EnvConfig.getConfig()[:username]
password = EnvConfig.getConfig()[:password]

index = 0
pre_name = ""

def should_skip_validation
    DateTime.now().day >=21 && DateTime.now().day <=25
end

Given /^I cleared App data/ do
    clear_app_data
end

Then /^I navigate back/ do
    tap_when_element_exists("* contentDescription:'Navigate up'")
end

Then /^I scroll down until I see the "([^\"]*)"/ do |text|
  unless has_text?(text)
    scroll_down
  end
end

When /^I enter username "([^\"]+)"$/ do |username|
  element = "android.widget.EditText id:'tx_username'"
  query(element, setText: '')
  enter_text(element, username)
  hide_soft_keyboard
end

When /^I enter password "([^\"]+)"$/ do |password|
  enter_text("android.widget.EditText id:'tx_password'", password)
  hide_soft_keyboard
end

Given(/^I try to log in with "(.*?)" "(.*?)"$/) do |username, password|
  steps %Q{
		When I enter username "#{username}"
        And I enter password "#{password}"
        And I press "LOG IN"
	}
end

And (/^I sign out$/) do
    tap_when_element_exists("* contentDescription:'More options'")
    steps %Q{
        Then I press "Sign Out"
    }
end

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

And(/^I enter quantity "(\d+)" on inventory page$/) do |quantity|
    h = query("android.widget.EditText id:'tx_quantity' text:''").first
    touch(h)
    keyboard_enter_text(quantity)
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
        Then I wait up to 30 seconds for "Initial Inventory" to appear
        Then I wait for 3 seconds
        When I Select VIA Item
        When I Select MMIA Item
        Then I wait for "Complete" to appear
        And I press "Complete"
        Then I wait for "Home Page" to appear
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
            Then I wait for "Complete" to appear
            And I press "Complete"
            Then I wait for "Home Page" to appear
        }
    else
       steps %Q{
           Given I have initialized inventory
       }
    end
end

When(/^I search product by fnm "(.*?)" and select this item with quantity "(.*?)"/) do |fnm,quantity|
    steps %Q{
        When I search drug by fnm "#{fnm}"
    }

    q = query("android.widget.CheckBox id:'checkbox' checked:'false'")
    if !q.empty?
        steps %Q{
            When I select the item called "#{fnm}"
            Then I enter quantity "#{quantity}" on inventory page
        }
    end

    steps %Q{
        And I clean search bar
    }
end

And(/^I clean search bar/) do
    search_bar = query("android.support.v7.widget.SearchView id:'action_search'")
    clear_text_in(search_bar)
end

When(/^I search drug by fnm "(.*?)"$/) do |fnm|
    search_bar = query("android.support.v7.widget.SearchView id:'action_search'")
    touch(search_bar)
    enter_text("android.support.v7.widget.SearchView id:'action_search'", fnm)
end

And(/^I do physical inventory with "(\d+)" by fnm "(.*?)"/) do |quantity,fnm|
    steps %Q{
        When I search drug by fnm "#{fnm}"
        And I enter quantity "#{quantity}" on inventory page
        And I clean search bar
    }
end

And(/^I sign with "(.*?)"$/) do |text|
    enter_text("android.widget.EditText id:'et_signature'", text)
    hide_soft_keyboard

    steps %Q{
        Then I press "Approve"
    }
    hide_soft_keyboard
end

And(/^I sign requisition with "(.*?)" "(.*?)" and complete$/) do |submitSignature, completeSignature|
    enter_text("android.widget.EditText id:'et_signature'", submitSignature)
    hide_soft_keyboard

    steps %Q{
        Then I press "Approve"
        And I wait for 1 second
        Then I press "Continue"
        Then I press "Complete"
    }

    enter_text("android.widget.EditText id:'et_signature'", completeSignature)
    hide_soft_keyboard
    steps %Q{
        Then I press "Approve"
    }
end

