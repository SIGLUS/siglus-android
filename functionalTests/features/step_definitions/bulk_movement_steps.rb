require 'calabash-android/calabash_steps'

Then (/^I press button with id "([^\"]*)"$/) do |id|
  btn_action = query("android.widget.Button id:'#{id}'").first
  touch(btn_action)
end

Then /^text should be "(\d*)" in edit text "([^\"]*)"$/ do |expected_text, id|
  actual_text = query("* id:'#{id}'", :getText).first
  unless expected_text == actual_text
    fail "Text in SOH field is incorrect. Expected '#{expected_text}', but got '#{actual_text}'."
  end
end

Then /^I do not see button "(.*?)"$/ do |text|
     check_element_does_not_exist("view marked:'#{text}'")
end

Then(/^I press the button with id "(.*?)" and tag "(.*?)"$/) do |id, tag|
  btn_action = query("android.widget.Button id:'#{id}' tag:'#{tag}'").first
  touch(btn_action)
end

Then(/^I press the view with id "(.*?)" and tag "(.*?)"$/) do |id, tag|
  et_action = query("android.widget.LinearLayout id:'#{id}' tag:'#{tag}'").first
  touch(et_action)
end

Then(/^I insert the text "(.*?)" in the EditText with id "(.*?)"$/) do |text, id|
  edit_text = query("android.widget.EditText id:'#{id}'").first
  touch(edit_text)
  keyboard_enter_text(text)
end

Then (/^I press the button with text "(.*?)"$/) do |text|
  btn_action = query("android.widget.Button text:'#{text}' tag:'#{text}' ").first
  touch(btn_action)
end

Then (/^I write in "(.*?)" field the text "(.*?)"/) do |tag, text|
    edit_text = query("android.widget.EditText tag:'#{tag}'").first
    touch(edit_text)
    keyboard_enter_text(text)
end

Then (/^I press delete button$/) do
  btn_action = query("android.widget.ImageButton id:'btn_delete_lot'").first
  touch(btn_action)
end

Then (/^I press save button$/) do
  btn_action = query("android.widget.LinearLayout id:'btn_save'").first
  touch(btn_action)
end