def requisition_continue_page
  @requisition_continue_page ||= page(RequisitionContinuePage).await(timeout: 30)
end

Given(/^I press continue to finish approval$/) do
  requisition_continue_page.finish_approval
end