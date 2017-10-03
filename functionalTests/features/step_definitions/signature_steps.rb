def signature_page
  @signature_page ||= page(SignaturePage).await(timeout: 30)
end

Given(/^I sign using "([^"]*)" as initials$/) do |signature|
  signature_page.sign_and_approve_requisition(signature)
end
