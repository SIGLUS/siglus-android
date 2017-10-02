def select_period_page
  @select_period_page ||= page(RequisitionSelectPeriodPage).await(timeout: 30)
end

Given(/^I select current requisition period$/) do
  select_period_page.select_current_period
  end

Given(/^I press next to continue with the requisition form$/) do
  select_period_page.continue_requisition
end