require 'calabash-android/abase'
require 'calabash-android/calabash_steps'

class HomePage < Calabash::ABase

   def touch_first_number_field
     tap_when_element_exists "android.widget.EditText hint:'Input first number'"
   end

   def touch_second_number_field
     tap_when_element_exists "android.widget.EditText hint:'Input second number'"
   end

   def click_get_result_button
     tap_when_element_exists "button marked:'Get Result'"
   end

   def get_calculate_result(result)
     wait_for { element_exists("TextView {text CONTAINS '#{result}'}")}
     query "TextView {text CONTAINS '#{result}'}"
   end

end
