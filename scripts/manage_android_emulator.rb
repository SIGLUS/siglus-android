require_relative 'colors'
require_relative 'helper'
require 'json'

INSTANCE_ID = ENV['INSTANCE_ID'] ? ENV['INSTANCE_ID'] : 'i-09c821996867a8cfe'
INITIAL_ATTEMPT = 1
MAX_ATTEMPTS = 60
START_COMMAND = 'start-instances'
START_STATUS_MESSAGE = 'ok'
STOP_COMMAND = 'stop-instances'
STOP_STATUS_MESSAGE = 'stopped'
SERVICE = 'Android Emulator'
START_MESSAGE = "#{SERVICE} instance STARTED".green
STOP_MESSAGE = "#{SERVICE} instance STOPPED".green
INSTANCE_READY_TO_LAUNCH = false

def manage_emulator_instance(command, status, message)
  response = execute_command(command, INSTANCE_ID)
  while response == INSTANCE_READY_TO_LAUNCH
    puts "Retrying start command".yellow
    response = execute_command(command, INSTANCE_ID)
    sleep 10
  end
  instance_status = instance_status(INSTANCE_ID)
  initial_attempt = INITIAL_ATTEMPT
  while not instance_status.eql? status
   instance_status = instance_status(INSTANCE_ID)
   initial_attempt +=1
   sleep 10
   should_stop_execution(initial_attempt, MAX_ATTEMPTS, SERVICE)
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
