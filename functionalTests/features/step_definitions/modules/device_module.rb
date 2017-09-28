module DeviceModule
  class Device
    def set_date(date)
      if ENV["ADB_DEVICE_ARG"].nil?
        return system("adb shell su 0 date -s #{date}")
      end
      system("adb -s $ADB_DEVICE_ARG shell su 0 date -s #{date}")
    end
  end
end
