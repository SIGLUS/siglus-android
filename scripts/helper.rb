require_relative 'colors'

def set_emulator_name
  if ENV["EMULATOR_NAME"].nil?
    ENV["EMULATOR_NAME"] = `adb devices | grep -v "List of" | awk -F'\t' '{print $1}'`.split("\n\n").first
  end
end

def starting_open_lmis_containers(docker_compose_file_name)
  system "cd docker && docker-compose -f #{docker_compose_file_name} up -d"
end

def should_stop_execution(initial_attempt, attempts, service)
  if initial_attempt == attempts
    abort("#{service} didn't START on time".red)
  end
end

def check_portal(cmd)
  `#{cmd}`.split("\n").first
end

def is_portal_started(portal_url, initial_attempt, attempts)
  cmd = "curl -o - -s -w \"%{http_code}\n\" \"http://#{portal_url}:8081\""
  status = check_portal(cmd)

  if status == "302"
    puts "OpenLMIS Portal is UP".green
    return
  end

  while status != "302" do
    initial_attempt+=1
    puts "OpenLMIS Portal is not UP yet, waiting for it - attempt(#{initial_attempt} of #{attempts})..."
    status = check_portal(cmd)
    should_stop_execution(initial_attempt, attempts, "Portal")
    sleep 10
  end
end

def setup_data(lmis_moz_dir, open_lmis_db_container_name)
  puts "Copying all scripts into /data/setup/dev".blue
  system "docker exec -it #{open_lmis_db_container_name} mkdir -p /data/setup"
  system "docker cp #{lmis_moz_dir}/functionalTests/data/setup/dev/ #{open_lmis_db_container_name}:/data/setup"
end

def apply_data(open_lmis_db_container_name)
  puts "Applying data to #{open_lmis_db_container_name}".blue
  `docker exec -it #{open_lmis_db_container_name} psql -U postgres --file data/setup/dev/seed.sql -w open_lmis -h localhost`
end

def connect_to_emulator(emulator_name, test_runner_container_name)
  system "docker exec -it #{test_runner_container_name} adb connect #{emulator_name}"
end

def is_emulator_connected(emulator_name, test_runner_container_name)
  puts "Validating the emulator to be connected to ADB in #{test_runner_container_name} container".blue
  devices_connected = `adb devices`.split("\n")[1..-1]
  devices_connected.each do |device|
    if device.include? emulator_name
      return puts "Emulator connected and available".green
    end
  end
  return abort("The emulator is not connected to the container".red)
end

def run_functional_tests(test_runner_container_name, apk_to_use)
  system "docker exec -it #{test_runner_container_name} bash -c 'cd functionalTests && calabash-android run ../app/build/outputs/apk/#{apk_to_use} --tag @dev'"
end

def rename_local_properties
  system "mv local.properties local.properties.bk"
end

def rollback_local_properties
  system "mv local.properties.bk local.properties"
end
