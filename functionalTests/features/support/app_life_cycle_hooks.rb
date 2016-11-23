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

Before('@regression') do |scenario|
  update_lmis_moz_code
  reset_regression_server_data
end

Before('@weekly') do |scenario|
  update_lmis_moz_code
  reset_regression_server_data
end

Before('@STRESS_TEST') do |scenario|
  update_lmis_moz_code
  reset_regression_server_data
end

def update_lmis_moz_code
  if !Dir.exists?(LMIS_MOZ_DIR)
    system("git clone --depth 1 --branch master https://github.com/clintonhealthaccess/lmis-moz.git --single-branch #{LMIS_MOZ_DIR}")
  else
    system("cd #{LMIS_MOZ_DIR} && git checkout . && git pull -f origin master")
  end
end

def reset_regression_server_data
  puts "reset regression server data..."
  system("cd #{LMIS_MOZ_DIR} && ./data/functional_tests/regression/reset_data.sh")
end