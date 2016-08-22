require 'calabash-android/calabash_steps'
require 'pry'

Then(/^I should see issued movement "(.*?)"$/) do |number|
    unless (element_exists("android.widget.TextView id:'tv_issued' text:'#{number}'"))
		fail(msg="not found #{number}")
	end
end

Then(/^I should see inventory "(.*?)"$/) do |number|
    unless (element_exists("android.widget.TextView id:'tv_inventory' text:'#{number}'"))
		fail(msg="not found #{number}")
	end
end

And(/^I enter regimen totals$/) do
    while !query("android.widget.EditText id:'et_total' text:''").empty?
        query("android.widget.EditText id:'et_total'", {:setText => '1'})
    end

    steps %Q{
        Then I scroll "scrollView" down to "Submit for Approval"
    }

    while !query("android.widget.EditText id:'et_total' text:''").empty?
        query("android.widget.EditText id:'et_total'", {:setText => '1'})
    end
end

And(/^I add custom regimens and enter total$/) do
    steps %Q{
        Then I press "+ Adult regime"
        Then I wait for "Create regime" to appear
        And I select one drug to create a adult regime
        Then I press "Next"

        Then I wait for "MMIA" to appear
        And I enter regimen totals

        Then I press "+ Child regime"
        Then I wait for "Create regime" to appear
        And I select one drug to create a baby regime
        Then I press "Next"

        Then I wait for "MMIA" to appear
        And I enter regimen totals
    }
end

And(/^I select one drug to create a adult regime$/) do
    q = query("android.widget.CheckBox id:'checkbox' checked:'false'")

    if !q.empty?
        touch(q)
    end
end

And(/^I select one drug to create a baby regime$/) do
    q = query("android.widget.CheckBox id:'checkbox' checked:'false'")

    if !q.empty?
        touch(q)
    end
end


And(/^I enter patient totals$/) do
    q = query("android.widget.EditText id:'et_value'")
    for element in q
        if element.eql? q.first or element.eql? q.last
            next
        end

        if element.eql? q.at(1)
            touch(element)
            keyboard_enter_text(8)
        else
            touch(element)
            keyboard_enter_text(3)
        end
        hide_soft_keyboard
    end
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

When(/^I enter "(.*)" in "Observations"$/) do |text|
    enter_text("android.widget.EditText id:'et_comment'", text)
    hide_soft_keyboard
end

Then(/^I scroll "(.*?)" down to "(.*?)"$/) do |view, text|
    until element_exists("* marked:'#{text}'") do
        scroll(view, :down)
    end
end

And(/^I press delete icon$/) do
    q = query("android.widget.ImageView id:'iv_del'")
    touch(q.first)
end

Then(/^I should see patient total number is "(.*?)"$/) do |number|
    if !element_exists("android.widget.EditText id:'et_value' text:'#{number}'")
		fail(msg="patient total is #{number}")
	end
end



