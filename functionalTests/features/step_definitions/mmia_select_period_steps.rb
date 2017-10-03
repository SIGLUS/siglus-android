def select_period_page
  @select_period_page ||= page(MMIASelectPeriodPage).await(timeout: 30)
end

Given(/^I select current MMIA period$/) do
  select_period_page.select_current_period
  end

Given(/^I press next to continue with the MMIA form$/) do
  select_period_page.continue_requisition
end

