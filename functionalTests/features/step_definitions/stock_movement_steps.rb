require 'calabash-android/calabash_steps'
require 'pry'

Then(/^I select stock card called "(.*?)"$/) do |name|
      q = query("android.widget.TextView id:'product_name' text:'#{name}'")
      touch(q.last);
end

And (/^I select a reason$/) do
    q = query("android.widget.TextView id:'tx_reason'")
    touch(q.last);
    steps %Q{
        Then I press "Receive"
        Then I press "Normal requisition/reinforcement"
    	}
end

And (/^I enter received number "(.*?)"$/) do |number|
    touch(query("android.widget.EditText id:'et_received'").last);
    keyboard_enter_text(number)
    hide_soft_keyboard
end

