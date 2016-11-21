require 'calabash-android/calabash_steps'
require 'pry'

Then(/^I do monkey test for "(\d+)" times/) do |times|

  if ENV["ADB_DEVICE_ARG"].nil?
    system("adb  shell 'cd data/data && monkey -p org.openlmis.core.prodsim -v #{times} > /sdcard/monkeytest.log'")
    system("adb pull /sdcard/monkeytest.log ../../monkeytest.log")
  else
    system("adb -s $ADB_DEVICE_ARG shell 'cd data/data && monkey -p org.openlmis.core.prodsim -v #{times} > /sdcard/monkeytest.log'")
    system("adb -s $ADB_DEVICE_ARG pull /sdcard/monkeytest.log ../../monkeytest.log")
  end

  text = File.read("../../monkeytest.log")
  if text.include? "System appears to have crashed"
    fail(msg="monkey test crashed")
  end
end