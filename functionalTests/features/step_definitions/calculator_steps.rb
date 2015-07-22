When(/^I type "(.*?)" into the first number input field$/) do |value|
  @home_page.touch_first_number_field
  keyboard_enter_text value
end

And(/^I type "(.*?)" into the second number input field$/) do |value|
  @home_page.touch_second_number_field
  keyboard_enter_text value
end

And(/^I press "(.*?)" button$/) do |value|
  @home_page.click_get_result_button
end

Then(/^the result message contains "(.*?)"$/) do |value|
  calculate_result = @home_page.get_calculate_result(value)
  raise "Calculation can not get the expected result" if calculate_result.empty?
end
