require 'calabash-android/calabash_steps'

When /^I enter username "([^\"]+)"$/ do |username|
  enter_text("android.widget.EditText id:'tx_username'", username)
end

When /^I enter password "([^\"]+)"$/ do |password|
  enter_text("android.widget.EditText id:'tx_password'", password)
end