#!/usr/bin/env ruby
require_relative 'local_test_steps.rb'

update_mis_moz

puts "Running contract tests"
ctResult = run_contract_tests
exit 1 if !ctResult
puts "Finished contract tests"