def mmia_form_page
  @mmia_form_page ||= page(MMIAFormPage).await(timeout: 30)
end

Given(/^I swipe "([^"]*)" (\d+) time in MMIA form$/) do |direction, times|
  mmia_form_page.swipe_completely_to_the_right(direction, times)
end

Given(/^I check the quantity for "([^"]*)" is equals to (\d+) in MMIA form$/) do |product, amount|
  mmia_form_page.assert_product_amount_is_equals_to(product, amount)
end
Given(/^I scroll down to the bottom of the MMIA form$/) do
  mmia_form_page.scroll_to_bottom
end

Given(/^I enter patient totals$/) do
  mmia_form_page.set_patient_totals
end

Given(/^I enter regimen totals$/) do
  mmia_form_page.set_regimen_totals
end

Given(/^I enter patient total different from regime total$/) do
  mmia_form_page.set_custom_totals
  end

Given(/^I enter patient total different from regime total$/) do
  mmia_form_page.set_custom_totals
end

Given(/^I add a "([^"]*)"$/) do |regimen_type|
  mmia_form_page.add_regimen(regimen_type)
end

Given(/^I save MMIA form$/) do
  mmia_form_page.save_mmia_form
end

Given(/^I submit MMIA form$/) do
  mmia_form_page.submit_mmia_form
end

Given(/^I add data to new regimens$/) do
  mmia_form_page.add_amount_to_new_regimens
end

