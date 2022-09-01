#!/usr/bin/python3
import sys
import rospy
import argparse
from motion_planner.arm_controller import MyArmMoveitController
from robotiq_cam import Robotiq_Camera
import os
arm = MyArmMoveitController(control_type='joint')
print("===============POSITION================")
current_pos = arm.arm.get_current_joint_values()
print(current_pos)
print("======================================")
