require 'calabash-android/calabash_steps'
require 'pry'

And(/^I enter quantity for all products in kit$/) do

        while !query("android.widget.EditText text:''").empty?
            query("android.widget.EditText id:'tx_quantity'", {:setText => '1'})

            if query("android.widget.TextView text:'Seringa descartavel, 10ml c/agulha 21gx1 1/2 3 [MMC00006]'").empty?
                scroll('recyclerView', :down)
            end
        end
        scroll('recyclerView', :down)
        tap_when_element_exists("* marked:'Complete'")
end

And(/^I enter quantity "(.*?)" for first product$/) do |quantity|
    h = query("android.widget.EditText id:'tx_quantity'").first
    clear_text_in(h)
    touch(h)
    keyboard_enter_text(quantity)
    hide_soft_keyboard
end

And(/^I should see "(.*?)" in quantity and expected quantity$/) do |number|
    unless (element_exists("android.widget.EditText id:'tx_quantity'' text:'#{number}'") && element_exists("android.widget.TextView id:'stock_on_hand_in_inventory'' text:'#{number}'"))
        fail(msg="quantity invalid")
    end
end

Then(/^I click the last sync banner$/) do
    last_sync_banner=query("android.widget.TextView id:'tx_sync_time'")
    fail "can not find the last sync banner" if last_sync_banner.nil?
    touch(last_sync_banner)
end

Then(/^I should see Complete button in unpack page$/) do
  q= query("android.widget.Button text:'Complete'")
  while q.empty?
    scroll('recyclerView', :down)
    q= query("android.widget.Button text:'Complete'")
  end
end