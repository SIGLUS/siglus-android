require 'calabash-android/calabash_steps'
require 'pry'

Then (/^I press image button with id "([^\"]*)"$/) do |id|
  tap_when_element_exists("* id:'#{id}'")
end

Then /^text should be "(\d*)" in edit text "([^\"]*)"$/ do |expected_text, id|
  actual_text = query("* id:'#{id}'", :getText).first
  unless expected_text == actual_text
    fail "Text in SOH field is incorrect. Expected '#{expected_text}', but got '#{actual_text}'."
  end
end

Then /^I do not see field with id "(.*?)"$/ do |args1|
     check_element_does_not_exist("view marked:'#{args1}'")
end