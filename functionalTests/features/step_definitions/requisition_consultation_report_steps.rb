def requisition_consultation_report_page
  @requisition_consultation_report_page ||= page(RequisitionConsultationReportPage).await(timeout: 30)
end

Given(/^I enter (\d+) external consultations performed$/) do |consultations_performed|
  requisition_consultation_report_page.set_consultations_performed(consultations_performed)
end

Given(/^I swipe left to see the theoretical stock$/) do
  requisition_consultation_report_page.swipe_to_theoretical_stock
end

Given(/^I press menu key to add a new product$/) do
  requisition_consultation_report_page.add_product_to_requisition
end

Given(/^I check the quantity for "([^"]*)" is equals to (\d+)$/) do |product, amount|
  requisition_consultation_report_page.assert_product_amount_is_equals_to(product, amount)
end

Given(/^I swipe "([^"]*)" (\d+) times$/) do |direction, times|
  requisition_consultation_report_page.swipe_completely_to_the_right(direction,times)
end

Given(/^I add (\d+) units as quantity requested for each product except the new one added$/) do |quantity|
  requisition_consultation_report_page.set_quantity_requested_per_product(quantity)
end

Given(/^I save requisition form$/) do
  requisition_consultation_report_page.save_requisition
end

Given(/^I press submit for approval$/) do
  requisition_consultation_report_page.submit_requisition_for_approval
end

Then(/^Then I wait up to (\d+) seconds to see "([^"]*)"$/) do |arg1, arg2|
  pending # Write code here that turns the phrase above into concrete actions
end