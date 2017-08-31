#!/bin/bash

# Detect ip and forward ADB ports outside to outside interface
ip=$(ifconfig  | grep 'inet'| grep -v '127.0.0.1' |  awk '{print $2}')
socat tcp-listen:5554,bind=$ip,fork tcp:127.0.0.1:5554 &
socat tcp-listen:5555,bind=$ip,fork tcp:127.0.0.1:5555 &

# Set up and run emulator
echo "no" | avdmanager  create avd -f -k "system-images;android-19;default;armeabi-v7a" -n test
echo "no" | emulator64-arm -avd test -noaudio -no-window -netfast -no-boot-anim -verbose -gpu off -qemu -usbdevice tablet -vnc :1
