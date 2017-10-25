require 'json'
require_relative 'colors'
require_relative 'helper'

INSTANCE_ID = ENV['INSTANCE_ID'] ? ENV['INSTANCE_ID'] : 'i-09c821996867a8cfe'
INITIAL_ATTEMPS = 1
MAX_ATTEMPS = 5
START_COMMAND = 'start-instances'
START_STATUS_MESSAGE = 'ok'
STOP_STATUS_MESSAGE = 'stopped'
START_MESSAGE = 'Emulator instance Started'.green
STOP_MESSAGE = 'Emulator instance Stopped'.green
STOP_COMMAND = 'stop-instances'

def manage_emulator_instance(command, status, message)
  puts "manage emulator".yellow
  execute_command(command, INSTANCE_ID)
  instance_status = ''
  initial_attempt = INITIAL_ATTEMPT
  while instance_status != "#{status}"
   initial_attempt +=1
   instance_status = instance_status(INSTANCE_ID)
   should_stop_execution(initial_attempt, MAX_ATTEMPS)
   sleep 60
  end
  puts "#{message}".green
end

def start_emulator_instance
  manage_emulator_instance(START_COMMAND, START_STATUS_MESSAGE, START_MESSAGE)
end

def stop_emulator_instance
  manage_emulator_instance(STOP_COMMAND, STOP_STATUS_MESSAGE, STOP_MESSAGE)
end

def execute_command(command, instance_id)
  system "aws ec2 #{command} --instance-ids #{instance_id}"
end

def instance_status(instance_id)
  command_output = `aws ec2 describe-instance-status --instance-ids #{instance_id}`
  json_string = JSON.parse(command_output)
  if json_string['InstanceStatuses'].empty?
    return 'stopped'
  end
  return json_string['InstanceStatuses'][0]['SystemStatus']['Status']
end
