def autogenerate_page
  @autogenerate_page ||= page(AutogenerateLotPage).await(timeout: 30)
end

Given(/^I select to autogenerate lot number$/) do
  autogenerate_page.confirm
end
