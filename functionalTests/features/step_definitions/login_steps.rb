def login_page
  @login_page ||= page(LoginPage).await(timeout: 30)
end

When(/^I enter username "([^\"]+)"$/) do |username|
  element = "android.widget.EditText id:'tx_username'"
  query(element, setText: "#{username}")
  hide_soft_keyboard
end

When(/^I enter password "([^\"]+)"$/) do |password|
  enter_text("android.widget.EditText id:'tx_password'", password)
  hide_soft_keyboard
end

Given(/^I try to log in with "(.*?)" "(.*?)"$/) do |username, password|
  steps %Q{
            Then I wait for a second
            When I enter username "#{username}"
            And I enter password "#{password}"
            And I press "LOG IN"
  }
end

Given(/^I log in for the first time$/) do
  login_page.login('core', 'password1')
end

Given(/^I login back into the application$/) do
  start_test_server_in_background
  login_page.login('core', 'password1')
end
