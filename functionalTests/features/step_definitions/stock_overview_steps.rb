Then(/^I should see SOH of "(.*?)" is "(.*?)"$/) do |productcode,soh|
    steps %Q{
        When I search "#{productcode}"
    }
    quantities = query("android.widget.TextView id:'tv_stock_on_hand'", :text)
    number=quantities.at(0).to_i
    condition = (number == soh.to_i)

    if condition
       steps %Q{
           And I clean search bar
       }
    else
        fail "Text in quantity field in position "#{index}" is incorrect."
    end
end

Then(/^I should see total:"(\d+)" on stock list page/) do |expectTotal|
    total = query("android.widget.TextView id:'tv_total'", :text).first
    unless (total.eql?("Total:"+expectTotal))
        fail(msg="Total drug quantity don't equals to expect quantity")
    end
end

Then(/^I select drug number (\d+)$/) do |nth|
    drugs = query("android.widget.TextView id:'product_name'")
    drug=drugs[nth.to_i-1]
    if not drug.nil?
        touch(drug)
    else
        fail(msg="There are no drugs in the list")
    end
end