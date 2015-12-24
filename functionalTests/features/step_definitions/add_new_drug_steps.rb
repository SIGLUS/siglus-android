require 'calabash-android/calabash_steps'
require 'pry'

Given(/^I have added new drugs/) do
    if EnvConfig::STRESS_TEST
        steps %Q{
            And I initialize products with quantity "100"
        }
    else
       steps %Q{
            When I search product by fnm "08S01ZY" and select this item with quantity "2008"
       }
    end
end

Then(/^I check new drug quantity/) do
    if EnvConfig::STRESS_TEST
        steps %Q{
            Then I should see total:"110" on stock list page
        }
    else
       steps %Q{
            Then I should see total:"11" on stock list page
            When I search product by fnm "08S01ZY" and select this item with quantity "2008"
       }
    end
end

Then(/^I should see total:"(\d+)" on stock list page/) do |expectTotal|
    total = query("android.widget.TextView id:'tv_total'", :text).first
    unless (total.eql?("Total:"+expectTotal))
        fail(msg="Total drug quantity don't equals to expect quantity")
    end
end
