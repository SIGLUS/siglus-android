After do |scenario|
  reset_server_data
end

def reset_server_data
  system("cd #{LMIS_MOZ_DIR} && ./build/setup-data.sh")
end