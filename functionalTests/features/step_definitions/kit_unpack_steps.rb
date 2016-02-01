require 'calabash-android/calabash_steps'
require 'pry'

And(/^I enter quantity for all products in kit$/) do

        while !query("android.widget.EditText text:''").empty?
            query("android.widget.EditText id:'tx_quantity'", {:setText => '1'})

            if query("android.widget.TextView text:'Seringa descartavel, 10ml c/agulha 21gx1 1/2 3 [MMC00006]'").empty?
                scroll('recyclerView', :down)
            end
        end
        tap_when_element_exists("* marked:'Complete'")
end