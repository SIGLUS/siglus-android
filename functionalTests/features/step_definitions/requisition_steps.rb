require 'calabash-android/calabash_steps'
require 'pry'

Then(/^I should see "(\d+)" products$/) do |numberOfItems|
	size = query("ListView","getAdapter","getCount")
	unless (size.first == (numberOfItems.to_i + 1))
		fail(msg="#{size} size")
	end
end

When(/^I enter consultationsNub "(\d+)"/) do |consultationsNub|
  enter_text("BorderedEditText id:'tx_consultation'", consultationsNub)
          hide_soft_keyboard
end

Then(/^I enter QuantityRequested "(\d+)"/) do |requestedNub|
    ets = query("android.widget.EditText id:'et_request_amount' ")
    for et in ets
    touch(et)
    keyboard_enter_text(requestedNub)
    hide_soft_keyboard
    end
end

Then(/^I should see "(\d+)" on index "(\d+)" of "(.*?)" field/) do |num,index,fieldName|
    textView_text = query("android.widget.EditText id:'fieldName' ", :text)[index.to_i-1]
    unless (textView_text.to_i == num.to_i)
        fail(msg="#{num} number")
    end
end

