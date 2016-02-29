#!/usr/bin/env ruby
require_relative 'local_test_steps.rb'

update_mis_moz

puts "Running ct && ft"
result = run_ct_and_ft
exit 1 if !result
puts "Finished ct && ft"