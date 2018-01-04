def requisition_initial_page
  @requisition_initial_page ||= page(RequisitionInitialPage).await(timeout: 30)
end

Given(/^I wait for requisition page is initialized$/) do
  requisition_initial_page.wait_for_the_screen_to_be_initialized
end



