def physical_inventory_page
  @physical_inventory_page ||= page(PhysicalInventoryPage).await(timeout: 30)
end

Given(/^I see the inventory screen$/) do
  physical_inventory_page.isVisible
end

Given(/^I search for "([^"]*)" in physical inventory$/) do |product|
  physical_inventory_page.search_for_product(product)
  physical_inventory_page.open_new_movement_for_product_found
end

Given(/^I set the amount to (\d+)$/) do |amount|
  physical_inventory_page.set_amount(amount)
end



