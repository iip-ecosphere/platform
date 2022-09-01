FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get -y update \
    && apt-get -y install \
    jq \
    git \
    vim \
    nano \
    curl \
    wget \
    cmake \
    unzip \
    tmux \
    net-tools \
    iputils-ping \
    ffmpeg curl \
    python3-pip \
    libzbar0 \
    locales lsb-release \ 
    software-properties-common \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

#=====================Install ROS Noetic=====================
RUN dpkg-reconfigure locales
RUN sh -c 'echo "deb http://packages.ros.org/ros/ubuntu $(lsb_release -sc) main" > /etc/apt/sources.list.d/ros-latest.list'
RUN apt-key adv --keyserver 'hkp://keyserver.ubuntu.com:80' --recv-key C1CF6E31E6BADE8868B172B4F42ED6FBAB17C654
RUN apt-get update \
 && apt-get install -y --no-install-recommends ros-noetic-desktop-full
RUN apt-get install -y --no-install-recommends python3-rosdep python3-rosinstall python3-rosinstall-generator python3-wstool build-essential
RUN rosdep init \
 && rosdep fix-permissions \
 && rosdep update
RUN echo "source /opt/ros/noetic/setup.bash" >> ~/.bashrc

#=============MOVEIT============
RUN apt-get -y update \
    && apt-get install -y --no-install-recommends ros-noetic-catkin python3-catkin-tools python3-osrf-pycommon

RUN apt-get install -y --no-install-recommends ros-noetic-moveit

RUN apt-get install -y --no-install-recommends ros-noetic-ros-control ros-noetic-ros-controllers

#=============Robot driver============
ADD install_ur_driver.sh /home/install_ur_driver.sh
RUN chmod +x /home/install_ur_driver.sh

WORKDIR /home
RUN ./install_ur_driver.sh
RUN echo "source /home/catkin_ws/devel/setup.bash" >> ~/.bashrc
#RUN echo "ROBOT_IP_ADDRESS=192.168.2.102" >> ~/.bashrc

#=============Python script============
ADD requirements.txt /home/requirements.txt
RUN pip install --no-cache-dir -r /home/requirements.txt
RUN git clone -b master https://github.com/tattrongvu/ros_image_transport


#=============JAVA script============
RUN apt-get -y update && apt-get -y install openjdk-8-jdk

#=============AUTOMATIC RUN ROBOT SERVER============
#RUN "sudo source ~/.bashrc"
#ENTRYPOINT sh /home/robot_launch.sh -a $ROBOT_IP_ADDRESS

ADD poweron_robot.sh /home/poweron_robot.sh
RUN chmod +x /home/poweron_robot.sh

ADD restart_tablet_program.sh /home/restart_tablet_program.sh
RUN chmod +x /home/restart_tablet_program.sh

ADD robot_launch.sh /home/robot_launch.sh
RUN chmod +x /home/robot_launch.sh

ENTRYPOINT ./robot_launch.sh && tail -f /dev/null

#CMD ./robot_launch.sh -a $ROBOT_IP_ADDRESS ; sleep infinity
#CMD ls #nohup roslaunch ur_robot_driver ur5e_bringup.launch robot_ip:="$address" & sleep 5
#CMD echo $ROBOT_IP_ADDRESS && tail -f /dev/null
