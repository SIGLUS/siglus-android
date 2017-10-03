require 'calabash-android/calabash_steps'

Then /^I navigate back/ do
  tap_when_element_exists("* contentDescription:'Navigate up'")
end