echo $1 | sudo -S docker image tag imagetype_3/ecs:01 localhost:5001/imagetype_3/ecs:01

echo $1 | sudo -S docker image tag imagetype_3/app:01 localhost:5001/imagetype_3/app:01

echo $1 | sudo -S docker push localhost:5001/imagetype_3/ecs:01

echo $1 | sudo -S docker push localhost:5001/imagetype_3/app:01