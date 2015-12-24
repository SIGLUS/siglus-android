require 'calabash-android/calabash_steps'
require 'pry'
require 'date'
require File.dirname(__FILE__) + '/env_config'

username = EnvConfig.getConfig()[:username]
password = EnvConfig.getConfig()[:password]

index = 0
pre_name = ""

def should_skip_validation
    DateTime.now().day >=21 && DateTime.now().day <=25
end

Then /^I navigate back/ do
    tap_when_element_exists("* contentDescription:'Navigate up'")
end

Then /^I scroll down until I see the "([^\"]*)"/ do |text|
  unless has_text?(text)
    scroll_down
  end
end

And(/^I initialize products with quantity "(\d+)"/) do |quantity|
    checkBox = query("android.widget.CheckBox id:'checkbox' checked:'false'").first

    while !checkBox.nil?
        if index == quantity.to_i then
            break
        end

        steps %Q{
            When I select the checkbox with quantity "#{quantity}"
        }
        scroll("RecyclerView", :down)
        checkBox = query("android.widget.CheckBox id:'checkbox' checked:'false'").first
    end
end

When(/^I search product by fnm "(.*?)" and select this item with quantity "(.*?)"/) do |fnm,quantity|
    steps %Q{
        When I search drug by fnm "#{fnm}"
    }
    q = query("android.widget.CheckBox id:'checkbox' checked:'false'")
    if !q.empty?
        steps %Q{
            When I select the item called "#{fnm}"
            Then I enter quantity "#{quantity}" on inventory page
        }
    end
    steps %Q{
        And I clean search bar
    }
end

And(/^I clean search bar/) do
    search_bar = query("android.support.v7.widget.SearchView id:'action_search'")
    clear_text_in(search_bar)
end

When(/^I search drug by fnm "(.*?)"$/) do |fnm|
    search_bar = query("android.support.v7.widget.SearchView id:'action_search'")
    touch(search_bar)
    enter_text("android.support.v7.widget.SearchView id:'action_search'", fnm)
end

And(/^I sign with "(.*?)"$/) do |text|
    enter_text("android.widget.EditText id:'et_signature'", text)
    hide_soft_keyboard

    steps %Q{
        Then I press "Approve"
    }
    hide_soft_keyboard
end

And(/^I sign requisition with "(.*?)" "(.*?)" and complete$/) do |submitSignature, completeSignature|
    enter_text("android.widget.EditText id:'et_signature'", submitSignature)
    hide_soft_keyboard

    steps %Q{
        Then I press "Approve"
        And I wait for 1 second
        Then I press "Continue"
        Then I press "Complete"
    }

    enter_text("android.widget.EditText id:'et_signature'", completeSignature)
    hide_soft_keyboard
    steps %Q{
        Then I press "Approve"
    }
end

Then(/^I should see total:"(\d+)" on stock list page/) do |expectTotal|
    total = query("android.widget.TextView id:'tv_total'", :text).first
    unless (total.eql?("Total:"+expectTotal))
        fail(msg="Total drug quantity don't equals to expect quantity")
    end
end

And(/^I rotate the device to ([^\"]*)/) do |orientation|
    perform_action('set_activity_orientation', orientation)
end