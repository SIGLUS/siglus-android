require 'calabash-android/calabash_steps'
require 'pry'

Then(/^I select stock card code called "(.*?)"$/) do |name|
      q = query("android.widget.TextView id:'product_name' {text CONTAINS '#{name}'}")
      touch(q.last);
end

And(/^I select a reason "(.*?)" "(.*?)"$/) do |first_reason, second_reason|
    q = query("android.widget.TextView id:'tx_reason'")
    touch(q.last);
    steps %Q{
        Then I press "#{first_reason}"
        Then I press "#{second_reason}"
    	}
end

And(/^I enter received number "(.*?)"$/) do |number|
    touch(query("android.widget.EditText id:'et_received'").last);
    keyboard_enter_text(number)
    hide_soft_keyboard
end

And(/^I enter issued number "(.*?)"$/) do |number|
    q = query("android.widget.EditText id:'et_issued'")
    touch(q.last)
    keyboard_enter_text(number)
    hide_soft_keyboard
end

And(/^I enter negative adjustment number "(.*?)"$/) do |number|
    q = query("android.widget.EditText id:'et_negative_adjustment'")
    touch(q.last)
    keyboard_enter_text(number)
    hide_soft_keyboard
end

And(/^I enter positive adjustment number "(.*?)"$/) do |number|
    q = query("android.widget.EditText id:'et_positive_adjustment'")
    touch(q.last)
    keyboard_enter_text(number)
    hide_soft_keyboard
end

Then(/^I make a movement "(.*?)" "(.*?)" "(.*?)" "(.*?)" "(.*?)"$/) do |stock_card_code, first_reason, second_reason, movement_column, number|
    steps %Q{
        Then I select stock card code called "#{stock_card_code}"
        Then I wait for "Stock Card" to appear
        Then I wait for 1 second
        And I select a reason "#{first_reason}" "#{second_reason}"
    }

    if movement_column.eql? "positive adjustment" or movement_column.eql? "issued"
        steps %Q{
            Then I swipe right
        }
    end

    steps %Q{
        Then I wait for 1 second
        And I enter #{movement_column} number "#{number}"
        And I press "Complete"
        And I sign stock movement with "superuser"
        Then I wait for 1 second
        Then I navigate back
    }
end

And(/^I sign stock movement with "(.*?)"$/) do |text|
    if EnvConfig.getConfig()[:stockMovementSignature]
        enter_text("android.widget.EditText id:'et_signature'", text)
        hide_soft_keyboard

        steps %Q{
            Then I press "Approve"
        }
        hide_soft_keyboard
    end
end

Then(/^I see "(.*?)" in signature field$/) do |text|
    unless element_exists("android.widget.TextView id:'tx_signature' text:'#{text}'")
     		fail(msg="not found #{text} in signature field")
    end
end

Then(/^I make all movements for "(.*?)"$/) do |drugFNM|
    steps %Q{
      And I make a movement "#{drugFNM}" "Entries" "District( DDM)" "Entries" "1"
      And I make a movement "#{drugFNM}" "Entries" "Province ( DPM)" "Entries" "1"
      And I make a movement "#{drugFNM}" "Negative Adjustments" "Drugs in quarantine have expired, returned to Supplier" "Negative Adjustments" "1"
      And I make a movement "#{drugFNM}" "Negative Adjustments" "Damaged on arrival" "Negative Adjustments" "1"
      And I make a movement "#{drugFNM}" "Negative Adjustments" "Loans made from a health facility deposit" "Negative Adjustments" "1"
      And I make a movement "#{drugFNM}" "Negative Adjustments" "Inventory correction in case of over stock on Stock card (Stock on hand is less than stock in stock card)" "Negative Adjustments" "1"
      And I make a movement "#{drugFNM}" "Negative Adjustments" "Product defective, moved to quarantine" "Negative Adjustments" "1"
      And I make a movement "#{drugFNM}" "Positive Adjustments" "Returns from Customers(HF and dependent wards)" "Positive Adjustments" "1"
      And I make a movement "#{drugFNM}" "Positive Adjustments" "Returns of expired drugs (HF and dependent wards)" "Positive Adjustments" "1"
      And I make a movement "#{drugFNM}" "Positive Adjustments" "Donations to Deposit" "Positive Adjustments" "1"
      And I make a movement "#{drugFNM}" "Positive Adjustments" "Loans received at the health facility deposit" "Positive Adjustments" "1"
      And I make a movement "#{drugFNM}" "Positive Adjustments" "Inventory correction in case of under stock on Stock card (Stock on hand is more than stock in stock card)" "Positive Adjustments" "1"
      And I make a movement "#{drugFNM}" "Positive Adjustments" "Returns from Quarantine, in the case of quarantined product being fit for use" "Positive Adjustments" "1"
      And I make a movement "#{drugFNM}" "Issues" "Public pharmacy" "issued" "1"
      And I make a movement "#{drugFNM}" "Issues" "Maternity" "issued" "1"
      And I make a movement "#{drugFNM}" "Issues" "General Ward" "issued" "1"
      And I make a movement "#{drugFNM}" "Issues" "Accident & Emergency" "issued" "1"
      And I make a movement "#{drugFNM}" "Issues" "Mobile unit" "issued" "1"
      And I make a movement "#{drugFNM}" "Issues" "Laboratory" "issued" "1"
      And I make a movement "#{drugFNM}" "Issues" "UATS" "issued" "1"
      And I make a movement "#{drugFNM}" "Issues" "PNCTL" "issued" "1"
      And I make a movement "#{drugFNM}" "Issues" "PAV" "issued" "1"
      And I make a movement "#{drugFNM}" "Issues" "Dental ward" "issued" "1"
    }
end