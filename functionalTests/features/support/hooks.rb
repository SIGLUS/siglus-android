LMIS_MOZ_DIR="#{Dir.pwd}/../lmis-moz"

After('~@regression', '~@upgrade_setup', '~@upgrade') do
  reset_local_server_data
end

Before('@clear_app_data') do
  clear_app_data
end

Before('@reinstall_app') do
  uninstall_apps
  install_app(ENV["TEST_APP_PATH"])
  install_app(ENV["APP_PATH"])
end

Before('@change_date') do
  system('adb -s $ADB_DEVICE_ARG shell "su 0 date -s 20160121.130000"')
end

After('@change_date') do
  current_time = Time.now.strftime("%Y%m%d.%H%M%S")
  system("adb -s $ADB_DEVICE_ARG shell 'su 0 date -s #{current_time}'")
end

def reset_local_server_data
  puts "reset local server data..."
  system("cd #{LMIS_MOZ_DIR} && ./build/setup-data.sh")
end

