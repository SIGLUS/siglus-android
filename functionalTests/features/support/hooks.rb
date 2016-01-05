After do |scenario|
  reset_server_data
end

Before('@clear_app_data') do
  clear_app_data
end

def reset_server_data
  system("cd #{LMIS_MOZ_DIR} && ./build/setup-data.sh")
end

