This folder contains files to setup the UR.

Profinet setup: device io name needs to be set by PLC.

For unknown reasons, transferring the robot program to react on the PLC commands between robots requires the rs485 interface.

-> copy rs485-1.0.urcap to USB stick
-> Hamburger -> System -> URCaps -> + -> select rs485-1.0.urcap from usbstick -> load -> reboot 

Transferring the program:
-> copy plc_dec22usb.* and default.installation to USB stick
-> Load Program -> USB -> plc_dec22usb
-> Save Program -> Home -> plc_dec22usb