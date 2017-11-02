def rapid_test_report_page
 page(RapidTestReportPage).await(timeout: 30)
end

Given(/^I enter quantity for Rapid Test Report$/) do
  rapid_test_report_page.set_consume_values
  rapid_test_report_page.set_positive_values
end

Given(/^I save rapid test report$/) do
  rapid_test_report_page.save_rapid_test_report
end

Given(/^I submit rapid test report$/) do
  rapid_test_report_page.submit_rapid_test_report
end