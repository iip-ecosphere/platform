#! /bin/bash

rosservice call /ur_hardware_interface/dashboard/power_on &&
rosservice call /ur_hardware_interface/dashboard/brake_release
sleep 30
rosservice call /ur_hardware_interface/dashboard/connect && 
rosservice call /ur_hardware_interface/dashboard/play


