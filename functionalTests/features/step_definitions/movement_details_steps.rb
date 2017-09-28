def movement_details_page
  page(MovementDetailsPage).await(timeout: 30)
end

Given(/^I add details in destination "([^"]*)" amount (\d+) and signed by "([^"]*)"$/) do |destination, amount, signature|
  movement_details_page.submit_movement_type_details_with(destination, amount, signature)
end

Given(/^I add details in origin "([^"]*)" amount (\d+) and signed by "([^"]*)"$/) do |origin, amount, signature|
  movement_details_page.submit_movement_type_details_with(origin, amount, signature)
end

When(/^I go back (\d+) times$/) do |times|

end
