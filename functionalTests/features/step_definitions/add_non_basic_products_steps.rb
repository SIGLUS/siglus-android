def add_non_basic_products
  @add_non_basic_products ||= page(AddNonBasicProductsPage).await(timeout: 30)
end

Given(/^I search for "(.*?)" to check$/) do |product_name|
  add_non_basic_products.search_product_with_name(product_name)
  add_non_basic_products.check_product_with_name
end

Given(/^I add selected products$/) do
  add_non_basic_products.add_selected_products
end

