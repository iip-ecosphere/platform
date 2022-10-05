import sys, os
import rospy
from control_msgs.msg import GripperCommandActionGoal, GripperCommandGoal, GripperCommandAction
import moveit_commander
from copy import deepcopy
import numpy as np
import actionlib
from geometry_msgs.msg import Pose, Point, Quaternion
from tf.transformations import quaternion_from_euler
import math


class MyArmMoveitController:
    def __init__(self,arm_group='manipulator',control_type='joint',args=None):
        #=============INIT MOVEIT=============
        self.args=args
        self.arm_group=arm_group
        self.control_type=control_type

        rospy.init_node('trong_external_control',anonymous=False)
        rospy.loginfo("Starting node trong_external_control")
        rospy.on_shutdown(self.cleanup)

        moveit_commander.roscpp_initialize(sys.argv)
        self.robot = moveit_commander.RobotCommander()
        self.scene = moveit_commander.PlanningSceneInterface()

        #=============MOVE GROUP=============
        self.arm = moveit_commander.MoveGroupCommander(self.arm_group)
        ##self.arm.set_pose_reference_frame('/base_link')
        #self.arm.allow_replanning(True)
        self.arm.set_goal_position_tolerance(0.01)
        self.arm.set_goal_orientation_tolerance(0.1)
        #end_effector_link
        self.ee_link = self.arm.get_end_effector_link()

        self.moveit_step_resolution = 0.01
        self.minimum_movement_thres = 0.005

    def _moveit_plan(self,action):

        current_pose = self.arm.get_current_pose().pose
        self.waypoints = []
        wpose = deepcopy(current_pose)
        wpose.position.x += action[0]
        wpose.position.y += action[1]
        wpose.position.z += action[2]

        wpose.orientation.w += action[3]
        wpose.orientation.x += action[4]
        wpose.orientation.y += action[5]
        wpose.orientation.z += action[6]
        
        self.waypoints.append(deepcopy(wpose))

        plan, fraction = self.arm.compute_cartesian_path(self.waypoints, eef_step=self.moveit_step_resolution,\
                                                        jump_threshold=3.0, avoid_collisions=True)
        return plan, fraction
    
    def path_control(self,action):
        plan, fraction = self._moveit_plan(action)
        print('Fraction: ',fraction)
        if fraction == 1:
            self.arm.execute(plan)
        return True

    def control(self,input):
        if self.control_type=='joint':
            self.joint_control(input)
        if self.control_type=='pose':
            self.pose_control(input)


    def joint_control(self,joint_goal):
        #joint_goal = self.arm.get_current_joint_values()
        self.arm.go(joint_goal, wait=True)
        self.arm.stop()
    
    def pose_control(self,pose_goal):
        self.arm.set_pose_target(pose_goal)
        self.arm.go(wait=True)
        self.arm.stop()
        self.arm.clear_pose_targets()
        return True

    def home_position(self):
        self.arm.set_named_target('start')
        self.arm.go(wait=True)
        self.arm.stop()
        self.start_pose = self.get_current_pose()

    def stop(self):
        self.arm.stop()

    def get_current_pose(self):
        if self.control_type=='pose':
            return self.arm.get_current_pose(self.ee_link).pose
        elif self.control_type=='joint':
            return self.arm.get_current_joint_values()
        
        # return [pose.position.x,pose.position.y,pose.position.z,\
        #     pose.orientation.w,pose.orientation.x,pose.orientation.y,pose.orientation.z]

    def cleanup(self):
        rospy.loginfo("Stopping the robot")

        # Stop any current arm movement
        self.arm.stop()

        #Shut down MoveIt! cleanly
        rospy.loginfo("Shutting down Moveit!")
        moveit_commander.roscpp_shutdown()
        moveit_commander.os._exit(0)