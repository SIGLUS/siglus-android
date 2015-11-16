require 'calabash-android/calabash_steps'
require 'pry'

Then(/^I should see issued movement "(.*?)"$/) do |number|
    unless element_exists("android.widget.TextView id:'tv_issued' text:'#{number}'")
		fail(msg="not found #{number}")
	end
end

Then(/^I should see inventory "(.*?)"$/) do |number|
    unless element_exists("android.widget.TextView id:'tv_inventory' text:'#{number}'")
		fail(msg="not found #{number}")
	end
end

And(/^I enter regimen totals$/) do
    q = query("android.widget.EditText id:'et_total'")
    for element in q
        if !element.eql? q.first
            touch(element)
            keyboard_enter_text(1)
        end
    end
    hide_soft_keyboard
end

And(/^I sign with "(.*?)"$/) do |text|
    enter_text("android.widget.EditText id:'et_signature'", text)
    hide_soft_keyboard
end

And(/^I enter patient totals$/) do
    q = query("android.widget.EditText id:'et_value'")
    for element in q
        if element.eql? q.first or element.eql? q.last
            next
        end

        if element.eql? q.at(1)
            touch(element)
            keyboard_enter_text(6)
        else
            touch(element)
            keyboard_enter_text(3)
        end
    end
    hide_soft_keyboard
end


And(/^I enter patient total different from regime total$/) do
    q = query("android.widget.EditText id:'et_value'")
    for element in q
        if element.eql? q.first or element.eql? q.last
            next
        end

        if element.eql? q.at(1)
            touch(element)
            keyboard_enter_text(3)
        else
            touch(element)
            keyboard_enter_text(3)
        end
    end
    hide_soft_keyboard
end

And(/^I should see empty patient total$/) do
    if element_exists("android.widget.EditText id:'et_value' text:'3'") or element_exists("android.widget.EditText id:'et_value' text:'6'")
		fail(msg="patient total not empty")
	end
end

Then(/^I scroll to "(.*?)"$/) do |text|
    until element_exists("* marked:'#{text}'") do
        scroll("ScrollView", :down)
    end
end

When(/^I enter "(.*)" in "Observations"$/) do |text|
    enter_text("android.widget.EditText id:'et_comment'", text)
    hide_soft_keyboard
end



