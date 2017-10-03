def lot_page
  @lot_page ||= page(InventoryLotPage).await(timeout: 30)
end

Given(/^I add a new lot with number "([^"]*)", amount (\d+) and expiration date next year$/) do |lot_number, amount|
  lot_page.submit_lot_with(lot_number, amount, get_date_a_year_from_now)
end

Given(/^I add a new lot without lot number and amount (\d+) and expiration date next year$/) do |amount|
  lot_page.submit_lot_without_lot_number(amount, get_date_a_year_from_now)
end

def get_date_a_year_from_now
  today = Time.now
  Time.new(today.year + 1, today.month, today.day)
end
