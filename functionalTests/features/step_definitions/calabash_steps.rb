require 'calabash-android/calabash_steps'
require 'pry'

When /^I enter username "([^\"]+)"$/ do |username|
  enter_text("android.widget.EditText id:'tx_username'", username)
  hide_soft_keyboard
end

When /^I enter password "([^\"]+)"$/ do |password|
  enter_text("android.widget.EditText id:'tx_password'", password)
  hide_soft_keyboard
end

Given(/^I am logged in$/) do
  steps %Q{
		When I enter username "superuser"
    And I enter password "password1"
    And I press "LOG IN"
    Then I should see "DONE"
	}
end

When(/^I select the item called "(.*?)"$/) do |name|
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

  h = query("android.widget.EditText id:'tx_quantity' text:''")
  touch(h)
  keyboard_enter_text("123")
  hide_soft_keyboard
end

Given(/^I am in Home Page$/) do
  steps %Q{
		Given I am logged in
        Then I should see "Initial Inventory"

		When I Select MMIA Item
		When I Select VIA Item

        And I press view with id "btn_done"
        Then I should see "Home Page"
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