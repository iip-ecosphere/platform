based on https://www.hivemq.com/blog/end-to-end-encryption-in-the-cloud/

keytool -genkey -alias qpid -keystore keystore.jks -validity 3650 -storepass a1234567 -keypass a1234567 -keyalg rsa -storetype jks
keytool -exportcert -alias qpid -keystore keystore.jks -rfc -file server.pem
openssl req -x509 -newkey rsa:2048 -keyout mqtt-client-key.pem -out mqtt-client-cert.pem -days 3650
openssl x509 -outform der -in mqtt-client-cert.pem -out mqtt-client-cert.crt
 keytool -import -file mqtt-client-cert.crt -alias client -keystore trust-store.jks -storepass changeme

  
  
  