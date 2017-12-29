def mmia_initial_page
  @mmia_initial_page ||= page(MMIAInitialPage).await(timeout: 30)
end

Given(/^I wait for mmia page is initialized$/) do
  mmia_initial_page.wait_for_the_screen_to_be_initialized
end



