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
  q = query("android.widget.TextView text:'#{name}'")
  while q.empty?
    scroll("RecyclerView", :down)
    q = query("android.widget.TextView text:'#{name}'")
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
    while !checkbox.nil?
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
        Then I wait for 1 second
		When I Select MMIA Item
		When I Select VIA Item

        And I press view with id "btn_done"
        Then I wait for the "HomeActivity" screen to appear
	}
end

When(/^I Select MMIA Item$/) do
  steps %Q{
        When I select the item called "Lamivudina 150mg/Zidovudina 300mg/Nevirapina 200mg [08S42]"
        When I select the item called "Tenofovir 300mg/Lamivudina 300mg/Efavirenze 600mg [08S18Y]"
        When I select the item called "Lamivudina 30mg/ Zidovudina 60mg [08S40Z]"
        When I select the item called "Lamivudina 30mg/Zidovudina 60mg/Nevirapina 50mg [08S42B]"
        When I select the item called "Lopinavir/Ritonavir 80/20 ml Solução Oral [08S39Y]"
	}
end

When(/^I Select VIA Item$/) do
  steps %Q{
        When I select the item called "Acetylsalicylic Acid, tablet 300mg [P1]"
        When I select the item called "Acyclovir, tablet 400mg [P2]"
        When I select the item called "Aminophylline Injection 250mg/10ml [P3]"
        When I select the item called "Amoxicillin (Trihydrate), Dry powder for suspension 125mg/5ml [P4]"
        When I select the item called "Atenolol 50mg tab [P5]"
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

When(/^I Select initial inventory$/) do
    checkBox = query("android.widget.CheckBox id:'checkbox' checked:'false'").first
    while !checkBox.nil?
        steps %Q{
            When I select the checkbox
    	}
        scroll("RecyclerView", :down)
        checkBox = query("android.widget.CheckBox id:'checkbox' checked:'false'").first
    end
end