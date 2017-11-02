def rapid_test_select_period_page
  @rapid_test_select_period_page ||= page(RapidTestSelectPeriodPage).await(timeout: 30)
end

Given(/^I press continue working on rapid test report$/) do
  rapid_test_select_period_page.continue_working_on_report
end