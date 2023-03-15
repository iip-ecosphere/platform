cd Files

cat >daemon.json <<EOF
{
  "insecure-registries" : ["$2"]
}
EOF

echo $1 | sudo -S mv daemon.json /etc/docker/

sudo systemctl restart docker

echo $1 | sudo -S $4 run -d \
  --restart=always \
  --name registry \
  -e REGISTRY_HTTP_ADDR=0.0.0.0:$3 \
  -p $3:$3 \
  registry:2
  