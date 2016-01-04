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