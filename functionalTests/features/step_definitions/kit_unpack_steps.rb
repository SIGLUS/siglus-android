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

And(/^I enter quantity for lots of all products in kit$/) do
        lot_list = query("android.support.v7.widget.RecyclerView id:'rv_new_lot_list'")
        while lot_list.empty?
            existing_lot = query("android.support.v7.widget.RecyclerView id:'rv_existing_lot_list'")

            if !existing_lot.empty?
                steps %Q{
                    And I enter lot quantity "1"
                }
                scroll('recyclerView', :down)
            else
               if(!query("android.widget.TextView text:'Seringa descartavel, 10ml c/agulha 21gx1 1/2 3 [MMC00006]'").empty?)
                  break
               end
               steps %Q{
                   And I add a new lot with lot number "TEST-123" and quantity "1"
               }
               scroll('recyclerView', :down)
            end

            if !query("android.support.v7.widget.RecyclerView id:'rv_new_lot_list'").empty?
                scroll('recyclerView', :down)
            end

            if query("android.widget.TextView text:'Seringa descartavel, 10ml c/agulha 21gx1 1/2 3 [MMC00006]'").empty?
                scroll('recyclerView', :down)
            end
        end

        scroll('recyclerView', :down)
        tap_when_element_exists("* marked:'Complete'")


        while !query("android.widget.TextView id:'tv_confirm_no_stock'").empty?
            confirm_no_stock = query("android.widget.TextView id:'tv_confirm_no_stock'").first
            touch(confirm_no_stock)
            q = query("* marked:'Complete'")
            while q.empty?
                scroll('recyclerView', :down)
                q = query("* marked:'Complete'")
            end
            tap_when_element_exists("* marked:'Complete'")
        end
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

Then(/^I should see kit receive number and open number is "(.*?)"$/) do |number|
    unless (element_exists("android.widget.TextView id:'et_via_kit_received_hf'' text:'#{number}'") && element_exists("android.widget.TextView id:'et_via_kit_opened_hf'' text:'#{number}'"))
        fail(msg="quantity invalid")
    end
end

And(/^I enter document number for unpack kit$/) do
    h = query("android.widget.EditText id:'et_document_number'").first
    clear_text_in(h)
    touch(h)
    keyboard_enter_text('1111')
    hide_soft_keyboard
end