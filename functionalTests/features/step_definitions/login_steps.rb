require 'calabash-android/calabash_steps'

When /^I enter username "([^\"]+)"$/ do |username|
  element = "android.widget.EditText id:'tx_username'"
  query(element, setText: "#{username}")
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
