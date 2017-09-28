def movements_type_page
  page(MovementsTypePage).await(timeout: 30)
end

Given(/^I select issue movement for product found$/) do
  movements_type_page.add_new_issue
end

Given(/^I select entry movement for product found$/) do
  movements_type_page.add_new_entry
end

Given(/^I select positive adjustment for product found$/) do
  movements_type_page.add_new_positive_adjustment
end

Given(/^I select negative adjustment for product found$/) do
  movements_type_page.add_new_negative_adjustment
end
