def continue_page
  @continue_page ||= page(ContinuePage).await(timeout: 30)
end

Given(/^I press continue to finish approval$/) do
  continue_page.finish_approval
end