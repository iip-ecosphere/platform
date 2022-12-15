echo $1 | sudo -S docker image tag simplemeshtestingapp/ecs:0.1.0 localhost:5001/simplemeshtestingapp/ecs:0.1.0

echo $1 | sudo -S docker image tag simplemeshtestingapp/dflt:0.1.0 localhost:5001/simplemeshtestingapp/dflt:0.1.0

echo $1 | sudo -S docker push localhost:5001/simplemeshtestingapp/ecs:0.1.0

echo $1 | sudo -S docker push localhost:5001/simplemeshtestingapp/dflt:0.1.0