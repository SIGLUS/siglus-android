def main_page
  @main_page ||= page(MainPage).await(timeout: 30)
end

Then(/^I should see the application main menu screen$/) do
  main_page.isVisible
end
