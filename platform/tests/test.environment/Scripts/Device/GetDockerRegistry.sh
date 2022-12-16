cd Files

cat >daemon.json <<EOF
{
  "insecure-registries" : ["$2"]
}
EOF

echo $1 | sudo -S mv daemon.json /etc/docker/

sudo systemctl restart docker
