def requisition_add_products_page
  @requisition_add_products_page ||= page(RequisitionAddProductsPage).await(timeout: 30)
end

Given(/^I search for "([^"]*)" in requisition add products$/) do |product_name|
  requisition_add_products_page.search_for_product(product_name)
end

Given(/^I submit product found with amount (\d+) to requisition report$/) do |amount|
  requisition_add_products_page.set_amount(amount)
  requisition_add_products_page.submit_new_product_to_requisition
end