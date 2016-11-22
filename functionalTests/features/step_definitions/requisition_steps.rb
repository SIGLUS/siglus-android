require 'calabash-android/calabash_steps'
require 'pry'

Then(/^I should see "(\d+)" products$/) do |numberOfItems|
	size = query("ListView","getAdapter","getCount")
	unless (size.first == (numberOfItems.to_i + 1))
		fail(msg="#{size} size")
	end
end

Then(/^I enter consultationsNub "(\d+)"/) do |consultationsNub|
  enter_text("android.widget.EditText id:'edit_text'", consultationsNub)
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
    textView_text = query("android.widget.TextView id:'#{fieldName}' ", :text)[index.to_i-1]
    unless (textView_text.to_i == num.to_i)
        fail(msg="#{num} number")
    end
end

Then(/^I should see "(\d+)" in the requisition form/) do |number|
    wait_for_text(number, timeout: 10)
end

And(/^I should see empty consultations number$/) do
    if element_exists("android.widget.EditText id:'edit_text' text:'2015'")
		fail(msg="consultations number not empty")
	end
end


Then(/^I should see consultations number is "(.*?)"$/) do |number|
    if !element_exists("android.widget.EditText id:'edit_text' text:'#{number}'")
		fail(msg="consultations number is not #{number}")
	end
end


Then (/^I swipe to the left in via requisition form$/) do
    pan("* id:'et_request_amount'", :right, from: {x: 0, y: 100}, to: {x: 700, y:100})
end


Then(/^I do physical inventory for via items without archived drugs$/) do
    steps %Q{
        And I do physical inventory with lots with "113" by fnm "01A01"
        And I do physical inventory with lots with "123" by fnm "01A03Z"
        And I do physical inventory with lots with "123" by fnm "01A04Z"
        And I do physical inventory with lots with "123" by fnm "01A05"
        Then I scroll "recyclerView" down to "Complete"
        And I wait for 1 second
        And I press "Complete"
        And I wait for "Enter your initials" to appear
        And I sign with "sign"
    }
end

Then(/^I do physical inventory for via and mmia items$/) do
    steps %Q{
        And I do physical inventory with lots with "113" by fnm "01A01"
        And I do physical inventory with lots with "123" by fnm "01A03Z"
        And I do physical inventory with lots with "123" by fnm "01A04Z"
        And I do physical inventory with lots with "123" by fnm "01A05"
        And I do physical inventory with lots with "123" by fnm "01A02"

        And I do physical inventory with lots with "123" by fnm "08S42B"
        And I do physical inventory with lots with "121" by fnm "08S18Y"
        And I do physical inventory with lots with "123" by fnm "08S40Z"
        And I do physical inventory with lots with "123" by fnm "08S36"
        And I do physical inventory with lots with "123" by fnm "08S32Z"

        Then I scroll "recyclerView" down to "Complete"
        And I wait for 1 second
        And I press "Complete"
        And I wait for "Enter your initials" to appear
        And I sign with "sign"
    }
end