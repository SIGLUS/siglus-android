require 'calabash-android/management/adb'
require 'calabash-android/operations'

Before do |scenario|
  start_test_server_in_background
end

After do |scenario|
  shutdown_test_server
end
