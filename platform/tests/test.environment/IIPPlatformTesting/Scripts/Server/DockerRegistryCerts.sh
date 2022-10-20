cd Files
mkdir -p certs

localIP=$(hostname -I | cut -d ' ' -f1)

openssl req \
  -newkey rsa:4096 -nodes -sha256 -keyout certs/domain.key \
  -addext "subjectAltName = IP:'$localIP'" \
  -x509 -days 365 -out certs/domain.crt \
  -subj "/C=DE/ST=HI/L=HI/O=UNI.HI/OU=SSE/CN=example.com/emailAddress=UNI@UNI.COM"

echo $1 | sudo -S mkdir -p /etc/docker/certs.d/$localIP\:5001/

echo $1 | sudo -S cp certs/domain.crt /etc/docker/certs.d/$localIP\:5001/ca.crt

echo $1 | sudo -S mkdir Install/DeviceFolder/dockerRegistryCerts/
echo $1 | sudo -S cp certs/domain.crt Install/DeviceFolder/dockerRegistryCerts/domain.crt