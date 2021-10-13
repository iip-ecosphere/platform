https://www.hivemq.com/docs/hivemq/4.7/user-guide/howtos.html

server:
keytool -genkey -alias qpid -keystore keystore.jks -validity 3650 -storepass a1234567 -keypass a1234567 -keyalg rsa -storetype jks -keysize 2048

The first question asks about your first and last name. This is the common name of your certificate. Please enter the URL under which you will connect with your MQTT clients. For example, broker.yourdomain.com (for production) or localhost (for development). 

for client using a PEM client certificate, e.g., mosquitto:
keytool -exportcert -alias qpid -keystore keystore.jks -rfc -file server.pem

for client using a trust store, e.g., PAHO:
keytool -export -keystore keystore.jks -alias qpid -storepass a1234567 -file hivemq-server.crt
keytool -import -file hivemq-server.crt -alias qpid -keystore client-trust-store.jks -storepass a1234567 -storetype jks