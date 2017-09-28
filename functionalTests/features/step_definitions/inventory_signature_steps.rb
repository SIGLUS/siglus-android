def inventory_signature_page
  @inventory_signature_page ||= page(InventorySignaturePage).await(timeout: 30)
end

Given(/^I approve the physical inventory and sign the report with "([^"]*)"$/) do |signature|
  inventory_signature_page.sign_and_approve_physical_inventory(signature)
end