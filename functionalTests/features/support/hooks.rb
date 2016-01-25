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

def reset_local_server_data
  puts "reset local server data..."
  system("cd #{LMIS_MOZ_DIR} && ./build/setup-data.sh")
end

