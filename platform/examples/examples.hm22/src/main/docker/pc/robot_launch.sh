#! /bin/bash

source /opt/ros/noetic/setup.bash
source /home/catkin_ws/devel/setup.bash

echo 'Launch driver'
nohup roslaunch ur_robot_driver ur5e_bringup.launch robot_ip:="$ROBOT_IP" &
sleep 5
nohup roslaunch ur5e_moveit_config ur5e_moveit_planning_execution.launch &
sleep 5
echo 'Power on Robot'
rosservice call /ur_hardware_interface/dashboard/power_on &&
rosservice call /ur_hardware_interface/dashboard/brake_release
sleep 30
echo 'Launch Program'

rosservice call /ur_hardware_interface/dashboard/stop
rosservice call /ur_hardware_interface/dashboard/quit
rosservice call /ur_hardware_interface/dashboard/connect
rosservice call /ur_hardware_interface/dashboard/play
