def login_page
  page(LoginPage).await(timeout: 30)
end

Given(/^I log in for the first time$/) do
  login_page.login('core', 'password1')
end

Given(/^I log in into the application$/) do
  login_page.login('core', 'password1')
end

Given(/^I login back into the application$/) do
  start_test_server_in_background
  login_page.login('core', 'password1')
end
