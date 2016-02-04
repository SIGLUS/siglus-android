require 'calabash-android/management/adb'
require 'calabash-android/operations'

Before do |scenario|
  start_test_server_in_background
end

After do |scenario|
  puts "scenario #{scenario.name} finished"
  if scenario.failed?
    puts "trying to take screenshot"
    screenshot_embed
    puts "screenshot taken"
  end
  shutdown_test_server
end
