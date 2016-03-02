And(/^I rotate the page to "(.*?)"/) do |orientation|
    perform_action('set_activity_orientation', orientation)
end

Given(/^I change device date to "(.*?)"/) do |time|
    if ENV["ADB_DEVICE_ARG"].nil?
        system("adb shell su 0 date -s #{time}")
    else
        system("adb -s $ADB_DEVICE_ARG shell su 0 date -s #{time}")
    end
end

Given(/^I disable wifi/) do
    if ENV["ADB_DEVICE_ARG"].nil?
        system("adb shell svc wifi disable")
    else
        system("adb -s $ADB_DEVICE_ARG shell svc wifi disable")
    end
end

Given(/^I enable wifi/) do
    if ENV["ADB_DEVICE_ARG"].nil?
        system("adb shell svc wifi enable")
    else
        system("adb -s $ADB_DEVICE_ARG shell svc wifi enable")
    end
end

