def device
  @device ||= DeviceModule::Device.new
end

Given(/^today is "([^"]*)"$/) do |date|
  device.set_date(date)
end
