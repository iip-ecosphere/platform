#!/usr/bin/python3
import sys
import rospy
import argparse
from motion_planner.arm_controller import MyArmMoveitController
from robotiq_cam import Robotiq_Camera
import os
import subprocess
subprocess.call(os.getcwd()+"/restart_tablet_program.sh")


def get_args():
    parser = argparse.ArgumentParser()
    # the environment setting
    #parser.add_argument('--pos', type=str, default='config_ddpg.json', help='pos')
    parser.add_argument('--pos', type=int, default=0, help='20:base 10:happy 11:sad 1:qr 2:left 3:right')
    parser.add_argument('--env', type=str, default="HM22", help='Specify which set of waypoints to use.')
    args = parser.parse_args()

    return args

def get_waypoints(env):
    if env == 'HM22':
        # [3.157431125640869, -1.4926234197667618, 2.1072428862201136, -1.8100797138609828, -1.5690344015704554, 3.171818733215332],
        return {"base":[3.1601462364196777, -1.7288810215392054, 1.7468364874469202, -1.5997868976988734, -1.569063965474264, 3.17183780670166],
                "qr_scan":[3.1426782608032227, -1.3671738666347046, 1.8071330229388636, -1.330320881014206, -1.5543282667743128, 3.124682903289795],
        "left_base":[3.7401022911071777, -0.9819815319827576, 1.4220483938800257, -0.7710850995830079, -2.5506728331195276, 3.201751947402954],
        "left":[3.550473928451538, -0.9812153738788147, 1.571575943623678, -0.7511464518359681, -2.695733372365133, 2.986285448074341],
        "right_base":[2.529592514038086, -1.3158355814269562, 1.856565300618307, -0.6738870900920411, -0.45567018190492803, 3.139937162399292],
        "right":[2.7166385650634766, -1.2147857707789917, 1.925473992024557, -0.8986794513515015, -0.45097095171083623, 3.340585470199585],
        "happy1":[3.1601462364196777, -1.7288810215392054, 1.7468364874469202, -1.5997868976988734, -1.569063965474264, 3.17183780670166],
        "happy2":[3.160130500793457, -2.024938245812887, 1.7280200163470667, -1.5994535885252894, -1.56907827058901, 3.1718437671661377],
        "sad1":[3.1601462364196777, -1.7288810215392054, 1.7468364874469202, -1.5997868976988734, -1.569063965474264, 3.17183780670166],
        "sad2":[3.160335063934326, -2.245683332482809, 2.483582321797506, -1.533996545975544, -1.5693863073932093, 3.1718289852142334]}
        

def check_pos(list1, list2):
    l=len(list1)
    #assert l1==l2
    c=0
    for i in range(l):
      if round(list1[i],2) == round(list2[i],2):
        c+=1
    if c==l:
      return True
    else:
      return False

def return_base_sequence(arm,wp):
    current_pos = arm.arm.get_current_joint_values()
    if check_pos(current_pos,wp['left']):
        arm.control(wp['left_base'])
    elif check_pos(current_pos,wp['right']):
        arm.control(wp['right_base'])
    arm.control(wp['base'])

def move_sequence(arm,target_pos,wp):
    current_pos = arm.arm.get_current_joint_values()
    if check_pos(current_pos,target_pos):
        return
    return_base_sequence(arm,wp)
    if check_pos(target_pos,wp['qr_scan']):
        arm.control(wp['qr_scan'])
    if check_pos(target_pos,wp['left']):
        arm.control(wp['left_base'])
        arm.control(wp['left'])
    if check_pos(target_pos,wp['right']):
        arm.control(wp['right_base'])
        arm.control(wp['right'])


def main(args):
    wp = get_waypoints(args.env)
    happy_waypoint = [wp['base'],wp['happy2'],wp['base']]
    sad_waypoint = [wp['base'], wp['sad2'], wp['base']]
    arm = MyArmMoveitController(control_type='joint')

    cam=Robotiq_Camera(robot_ip_address=os.environ['ROBOT_IP'],blur_threshold=100)

    if args.pos==20:
        print('Move to base position')
        move_sequence(arm,wp['base'],wp)
    elif args.pos==10:
        print('Robot Happy!')
        return_base_sequence(arm,wp)
        for p in happy_waypoint:
            arm.control(p)
    elif args.pos==11:
        print('Robot Sad!')
        return_base_sequence(arm,wp)
        for p in sad_waypoint:
            arm.control(p)
    elif args.pos==1:
        move_sequence(arm,wp['qr_scan'],wp)
        for _ in range(2):
            if cam.capture(prefix='',path='',check_focus=True,write=False):
                break
            else:
                arm.control(wp['base'])
                arm.control(wp['qr_scan'])
    elif args.pos==2:
        move_sequence(arm,wp['left'],wp)
        for _ in range(2):
            if cam.capture(prefix='',path='',check_focus=True,write=False):
                break
            else:
                arm.control(wp['left_base'])
                arm.control(wp['left'])

    elif args.pos==3:
        move_sequence(arm,wp['right'],wp)
        for _ in range(2):
            if cam.capture(prefix='',path='',check_focus=True,write=False):
                break
            else:
                arm.control(wp['right_base'])
                arm.control(wp['right'])
    sys.exit()

if __name__ == "__main__":
    args = get_args()
    main(args)
