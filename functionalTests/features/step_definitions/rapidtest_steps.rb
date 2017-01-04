require 'calabash-android/calabash_steps'
require 'pry'

Then(/^I enter quantity for Rapid Test Report$/) do
            query("android.widget.EditText id:'et_consume_rapid_test_report_grid'", {:setText => '1'})
            query("android.widget.EditText id:'et_positive_rapid_test_report_grid'", {:setText => '1'})
    end