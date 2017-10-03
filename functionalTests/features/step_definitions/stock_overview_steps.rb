def stock_overview_page
  page(StockOverviewPage).await(timeout: 30)
end

Given(/^I search for "([^"]*)"$/) do |product|
  stock_overview_page.search_for_product(product)
  stock_overview_page.open_new_movement_for_product_found
end