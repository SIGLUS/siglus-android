require 'calabash-android/calabash_steps'
require 'pry'

Then(/^I select new drug "(.*?)" with SOH "(.*?)" quantity/) do |drugproperty, soh|
  steps %Q{
          When I search drug by fnm "#{drugproperty}"
      }
  q = query("android.widget.CheckBox id:'checkbox' checked:'false'")
  if !q.empty?
    touch(q)
    steps %Q{
              When I select the item called "#{drugproperty}"
              Then I enter quantity "#{soh}" on inventory page
          }
  end
end

Then(/^I add new drug number (\d+) with SOH (\d+) quantity, then I see the added drug in stock overview$/) do |nth, soh|
  nth_drug=query("android.widget.TextView id:'product_name'")[nth.to_i-1]
  nth_checkbox = query("android.widget.CheckBox id:'checkbox' checked:'false'")[nth.to_i-1]
  touch(nth_checkbox)

  steps %Q{
        When I select the item called "#{nth_drug["text"]}"
        Then I enter quantity "#{soh}" on inventory page
        Then I wait for "Complete" to appear
        And I press "Complete"

        And I wait for "Stock Overview" to appear
        When I search stockcard by "#{nth_drug["text"]}"
        Then I see "#{soh}"
      }
end
