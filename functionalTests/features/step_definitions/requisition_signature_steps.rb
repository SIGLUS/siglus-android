def requisition_signature_page
  @requisition_signature_page ||= page(RequisitionSignaturePage).await(timeout: 30)
end

Given(/^I sign using "([^"]*)" as initials$/) do |signature|
  requisition_signature_page.sign_and_approve_requisition(signature)
end
