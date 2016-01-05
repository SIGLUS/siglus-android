LMIS_MOZ_DIR="#{Dir.pwd}/../lmis-moz"

After do |scenario|
  reset_server_data
end

Before('@clear_app_data') do
  clear_app_data
end

Before('@reinstall_app') do
  uninstall_apps
  install_app(ENV["TEST_APP_PATH"])
  install_app(ENV["APP_PATH"])
end

def reset_server_data
  puts "reset server data..."
  system("cd #{LMIS_MOZ_DIR} && ./build/setup-data.sh")
end

