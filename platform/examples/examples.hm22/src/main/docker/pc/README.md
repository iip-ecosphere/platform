1. cd Docker && docker build -t robot .
2. Use this command when use Network Camera to start container: \
docker run --rm --name robot_container -e ROBOT_IP="192.168.2.122" \
-v ${PWD}/:/home/robot --network host --user root -dit robot:latest

3. Use this command when connect Camera directly with the computer via USB port:
    If you want to use imshow function of opencv in the host screen, \
    run this in the host computer: 'xhost +local:docker' or 'xhost +'\

    Then, start container: \
    sudo docker run --rm --privileged --name robot_container \
    -e ROBOT_IP="192.168.2.122" -e DISPLAY=$DISPLAY \
    -v ${PWD}/:/home/robot --network host --user root -dit robot:latest

    OR:
    docker run --rm --name robot_container \
    -e ROBOT_IP="192.168.2.122" \
    --device /dev/video0:/dev/video0 \
    -v /tmp/.X11-unix:/tmp/.X11-unix -e DISPLAY=$DISPLAY \
    -v ${PWD}/:/home/robot \
    --network host --user root -dit robot:latest

4. Then, exec this running container:
    docker exec -it robot_container bash 

    Optional:  tmux new-session -s robot \
        -> followed by ctrl+b,shift+" for splitting the pane \


5. Control with args:\
    Move to base: \
        python3 move_robot.py --pos 20
    Robot Happy:
        python3 move_robot.py --pos 10
    


