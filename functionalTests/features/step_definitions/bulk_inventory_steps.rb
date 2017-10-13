def inventory_page
  @inventory_page ||= page(BulkInventoryPage).await(timeout: 30)
end

Given(/^I see the initial inventory screen$/) do
  inventory_page.isVisible
end

Given(/^I search for "([^"]*)" to add a new lot$/) do |product_name|
  inventory_page.search_product_with_name(product_name)
  inventory_page.open_add_lot_for_product_found
end

Given(/^I search for "([^"]*)" to declare no stock$/) do |product_name|
  inventory_page.search_product_with_name(product_name)
  inventory_page.set_no_stock_for_product_found
end

Given(/^I see total stock on hand for visible product to be equals to (\d+)$/) do |amount|
  inventory_page.assert_stock_on_hand_total_for_product_found_is(amount)
end

Given(/^I delete lot added in position (\d+)$/) do |lot_position|
  zero_based_position = lot_position.to_i - 1
  inventory_page.delete_added_lot_in_position(zero_based_position)
end

When(/^I submit the initial inventory$/) do
  inventory_page.submit
end

Given(/^I save initial inventory$/) do
  inventory_page.save
end

Given(/^the initial inventory list should not contains product "([^"]*)"$/) do |product_name|
  inventory_page.assert_product_is_not_listed(product_name)
end

Given(/^I add non basic products$/) do
  inventory_page.add_products
end
