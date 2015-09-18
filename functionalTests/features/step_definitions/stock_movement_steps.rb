require 'calabash-android/calabash_steps'
require 'pry'

Then(/^I select stock card called "(.*?)"$/) do |name|
      q = query("android.widget.TextView id:'product_name' text:'#{name}'")
      touch(q.last);
end

And (/^I select a reason "(.*?)" "(.*?)"$/) do |first_reason, second_reason|
    q = query("android.widget.TextView id:'tx_reason'")
    touch(q.last);
    steps %Q{
        Then I press "#{first_reason}"
        Then I press "#{second_reason}"
    	}
end

And (/^I enter received number "(.*?)"$/) do |number|
    touch(query("android.widget.EditText id:'et_received'").last);
    keyboard_enter_text(number)
    hide_soft_keyboard
end

And (/^I enter issued number "(.*?)"$/) do |number|
    q = query("android.widget.EditText id:'et_issued'")
    touch(q.last)
    keyboard_enter_text(number)
    hide_soft_keyboard
end

And (/^I enter negative adjustment number "(.*?)"$/) do |number|
    q = query("android.widget.EditText id:'et_negative_adjustment'")
    touch(q.last)
    keyboard_enter_text(number)
    hide_soft_keyboard
end

And (/^I enter positive adjustment number "(.*?)"$/) do |number|
    q = query("android.widget.EditText id:'et_positive_adjustment'")
    touch(q.last)
    keyboard_enter_text(number)
    hide_soft_keyboard
end