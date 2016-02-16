#!/usr/bin/env ruby
require_relative 'local_test_steps.rb'

update_mis_moz

puts "Running st"
stResult = run_st
exit 1 if !stResult
puts "Finished st"