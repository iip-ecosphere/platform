#! /bin/bash

rosservice call /ur_hardware_interface/dashboard/stop
rosservice call /ur_hardware_interface/dashboard/quit
rosservice call /ur_hardware_interface/dashboard/connect
rosservice call /ur_hardware_interface/dashboard/play
