And(/^I rotate the page to "(.*?)"/) do |orientation|
    perform_action('set_activity_orientation', orientation)
end