#!/usr/bin/env ruby

require 'pathname'
require_relative 'helper'
require_relative 'colors'


LMIS_MOBILE_DIR = Dir.pwd
path = Pathname.new(Dir.pwd)

if path.basename.to_s == "scripts"
  LMIS_MOBILE_DIR = path.dirname
end
set_emulator_name
OPEN_LMIS_DB_CONTAINER_NAME="open-lmis-db"
OPEN_LMIS_TEST_RUNNER_CONTAINER_NAME="test-runner"
EMULATOR_NAME=ENV["EMULATOR_NAME"]
PORTAL_URL = ENV['PORTAL_URL'] ?  ENV['PORTAL_URL'] : 'localhost'
INITIAL_ATTEMPT= 0
ATTEMPTS = 10

puts "Executing from #{LMIS_MOBILE_DIR}".green

puts "STEP 1 ===> Starting Open LMIS containers \n".yellow

starting_open_lmis_containers

puts "STEP 2 ===> Checking Open LMIS Services \n".yellow

is_portal_started(PORTAL_URL,INITIAL_ATTEMPT, ATTEMPTS)

puts "STEP 3 ===> Setting up data for test \n".yellow

setup_data(LMIS_MOBILE_DIR, OPEN_LMIS_DB_CONTAINER_NAME)

apply_data(OPEN_LMIS_DB_CONTAINER_NAME)

puts "STEP 4 ===> Connecting and validating emulator\n".yellow

connect_to_emulator(EMULATOR_NAME, OPEN_LMIS_TEST_RUNNER_CONTAINER_NAME)
is_emulator_connected(EMULATOR_NAME, OPEN_LMIS_TEST_RUNNER_CONTAINER_NAME)

puts "STEP 5 ===> Running functional tests\n".yellow

rename_local_properties
run_functional_tests(OPEN_LMIS_TEST_RUNNER_CONTAINER_NAME)
rollback_local_properties
