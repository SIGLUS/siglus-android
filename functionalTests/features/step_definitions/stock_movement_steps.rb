def movements_page
  page(MovementsPage).await(timeout: 30)
end

Given(/^I add a new movement$/) do
  movements_page.open_new_movement_dialog
end
