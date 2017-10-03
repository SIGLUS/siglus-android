def mmia_add_regimen_page
  @mmia_add_regimen_page ||= page(MMIAAddRegimenPage).await(timeout: 30)
end

Given(/^I search for product "([^"]*)" in MMIA requisition$/) do |product|
  mmia_add_regimen_page.search_for_regimen(product)
  mmia_add_regimen_page.submit_regimen
end